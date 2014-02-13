/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.maven;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.sourcepit.common.maven.model.util.MavenModelUtils.parseArtifactKey;
import static org.sourcepit.osgi.embedder.StartConfigurationUtil.fromProperties;
import static org.sourcepit.osgi.embedder.StartConfigurationUtil.toBundleStartPolicyProvider;
import static org.sourcepit.osgi.embedder.StartConfigurationUtil.toStartLevelProvider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.utils.props.LinkedPropertiesMap;
import org.sourcepit.common.utils.props.PropertiesMap;
import org.sourcepit.common.utils.props.PropertiesSource;
import org.sourcepit.m2p2.equinox.EmbeddedEquinox;
import org.sourcepit.m2p2.equinox.EmbeddedEquinoxLifecycleListener;
import org.sourcepit.osgi.embedder.BundleProvider;
import org.sourcepit.osgi.embedder.BundleStartPolicyProvider;
import org.sourcepit.osgi.embedder.FrameworkLocationProvider;
import org.sourcepit.osgi.embedder.StartConfiguration;
import org.sourcepit.osgi.embedder.StartLevelProvider;
import org.sourcepit.osgi.embedder.TempFrameworkLocationProvider;

@Named
public class MavenEquinoxFactory
{
   @Inject
   private ArtifactResolver artifactResolver;

   private Logger log = LoggerFactory.getLogger(MavenEquinoxFactory.class);

   public EmbeddedEquinox create(MavenSession session) throws MavenExecutionException
   {
      final PropertiesMap configuration = readConfig();

      final StartConfiguration startCfg = fromProperties(configuration, "m2p2");
      final StartLevelProvider startLevelProvider = toStartLevelProvider(startCfg);
      final BundleStartPolicyProvider bundleStartPolicyProvider = toBundleStartPolicyProvider(startCfg);

      Map<String, String> frameworkProerties = new HashMap<String, String>();
      frameworkProerties.put("osgi.parentClassloader", "fwk");
      // frameworkProerties.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
      frameworkProerties.put(Constants.FRAMEWORK_BOOTDELEGATION, "*");
      // "org.eclipse.core.net.proxy,org.eclipse.core.net.proxy.*,org.eclipse.equinox.p2.core,org.eclipse.equinox.p2.core.spi,org.eclipse.equinox.p2.repository.metadata,org.eclipse.equinox.internal.p2.metadata.repository,org.eclipse.equinox.internal.provisional.p2.core.eventbus,org.eclipse.equinox.internal.p2.repository,org.eclipse.core.runtime.preferences");

      // org.osgi.framework.bootdelegation=sun.*,com.sun.*
      // frameworkProerties.put(Constants.FRAMEWORK_BOOTDELEGATION, null);

      final FrameworkLocationProvider frameworkLocationProvider = new TempFrameworkLocationProvider();
      final List<ArtifactKey> frameworkArtifacts = getArtifacts(configuration, "m2p2.frameworkArtifacts");
      final List<ArtifactKey> bundleArtifacts = getArtifacts(configuration, "m2p2.bundleArtifacts");

      final BundleProvider<DependencyResolutionException> bundleProvider = new MavenBundleProvider(artifactResolver,
         session, frameworkArtifacts, bundleArtifacts);

      final EmbeddedEquinox embeddedEquinox = new EmbeddedEquinox(frameworkLocationProvider, startLevelProvider,
         bundleStartPolicyProvider, bundleProvider, frameworkProerties);

      embeddedEquinox.addLifecycleListener(new EmbeddedEquinoxLifecycleListener()
      {

         @Override
         public void frameworkStarted(EmbeddedEquinox embeddedEquinox)
         {
            final BundleContext bundleContext = embeddedEquinox.getBundleContext();
            final ServiceReference<LogReaderService> reference = bundleContext
               .getServiceReference(LogReaderService.class);

            if (reference != null)
            {
               LogReaderService logService = bundleContext.getService(reference);
               logService.addLogListener(new LoggerAdapter(log));
            }
         }

         @Override
         public void bundlesInstalled(EmbeddedEquinox embeddedEquinox)
         {
         }

         @Override
         public void bundlesStarted(EmbeddedEquinox embeddedEquinox)
         {
         }

         @Override
         public void frameworkAboutToStop(EmbeddedEquinox embeddedEquinox)
         {
         }

         @Override
         public void frameworkStopped(EmbeddedEquinox embeddedEquinox)
         {
         }
      });

      return embeddedEquinox;
   }

   private List<ArtifactKey> getArtifacts(PropertiesSource config, String key)
   {
      final Set<ArtifactKey> artifacts = new LinkedHashSet<ArtifactKey>();
      for (String bundleArtifact : config.get(key, "").split(","))
      {
         artifacts.add(parseArtifactKey(bundleArtifact.trim()).getArtifactKey());
      }
      return new ArrayList<ArtifactKey>(artifacts);
   }

   private PropertiesMap readConfig()
   {
      final PropertiesMap propertiesMap = new LinkedPropertiesMap();
      InputStream in = null;
      try
      {
         in = getClass().getResourceAsStream("osgi.properties");
         propertiesMap.load(in);
      }
      finally
      {
         closeQuietly(in);
      }
      return propertiesMap;
   }
}
