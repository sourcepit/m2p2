/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder.maven;

import static org.sourcepit.common.maven.model.util.MavenModelUtils.parseArtifactKey;
import static org.sourcepit.m2p2.osgi.embedder.StartConfigurationUtil.fromProperties;
import static org.sourcepit.m2p2.osgi.embedder.StartConfigurationUtil.toBundleStartPolicyProvider;
import static org.sourcepit.m2p2.osgi.embedder.StartConfigurationUtil.toStartLevelProvider;

import java.io.File;
import java.io.IOException;
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
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.utils.props.PropertiesSource;
import org.sourcepit.m2p2.osgi.embedder.AbstractOSGiEmbedderLifecycleListener;
import org.sourcepit.m2p2.osgi.embedder.BundleProvider;
import org.sourcepit.m2p2.osgi.embedder.BundleStartPolicyProvider;
import org.sourcepit.m2p2.osgi.embedder.ClassLoadingStrategy;
import org.sourcepit.m2p2.osgi.embedder.FrameworkLocationProvider;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;
import org.sourcepit.m2p2.osgi.embedder.ParentFirstClassLoadingStrategy;
import org.sourcepit.m2p2.osgi.embedder.SharedClassesAndResources;
import org.sourcepit.m2p2.osgi.embedder.StartConfiguration;
import org.sourcepit.m2p2.osgi.embedder.StartLevelProvider;
import org.sourcepit.m2p2.osgi.embedder.TempFrameworkLocationProvider;

@Named
public class MavenEquinoxFactory
{
   @Inject
   private ArtifactResolver artifactResolver;

   private Logger log = LoggerFactory.getLogger(MavenEquinoxFactory.class);

   public OSGiEmbedder create(MavenSession session, PropertiesSource configuration) throws MavenExecutionException
   {
      final StartConfiguration startCfg = fromProperties(configuration, "m2p2");
      final StartLevelProvider startLevelProvider = toStartLevelProvider(startCfg);
      final BundleStartPolicyProvider bundleStartPolicyProvider = toBundleStartPolicyProvider(startCfg);

      final SharedClassesAndResources sharedClassesAndResources = SharedClassesAndResources.fromProperties(
         configuration, "m2p2");

      final ClassLoadingStrategy classLoadingStrategy = new ParentFirstClassLoadingStrategy(
         getClass().getClassLoader(), sharedClassesAndResources);

      final FrameworkLocationProvider frameworkLocationProvider;
      final String workDir = configuration.get("m2p2.workDir");
      if (workDir == null)
      {
         frameworkLocationProvider = new TempFrameworkLocationProvider();
      }
      else
      {
         frameworkLocationProvider = new FrameworkLocationProvider()
         {
            @Override
            public void releaseFrameworkLocation(File frameworkLocation) throws IOException
            {
            }

            @Override
            public File aquireFrameworkLocation() throws IOException
            {
               final File loc = new File(workDir);
               if (!loc.exists())
               {
                  loc.mkdirs();
               }
               return loc;
            }
         };
      }

      final List<ArtifactKey> frameworkArtifacts = getArtifacts(configuration, "m2p2.frameworkArtifacts");
      final List<ArtifactKey> bundleArtifacts = getArtifacts(configuration, "m2p2.bundleArtifacts");

      final List<String> exclusions = getExclusions(configuration, "m2p2.bundleArtifactExclusions");

      final BundleProvider<DependencyResolutionException> bundleProvider = new MavenBundleProvider(artifactResolver,
         session, frameworkArtifacts, bundleArtifacts, exclusions);

      Map<String, String> frameworkProperties = new HashMap<String, String>();
      final OSGiEmbedder embeddedEquinox = new OSGiEmbedder(frameworkLocationProvider, startLevelProvider,
         bundleStartPolicyProvider, bundleProvider, frameworkProperties, classLoadingStrategy);

      embeddedEquinox.addLifecycleListener(new AbstractOSGiEmbedderLifecycleListener()
      {

         @Override
         public void frameworkStarted(OSGiEmbedder embeddedEquinox)
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
      });

      return embeddedEquinox;
   }

   private List<String> getExclusions(PropertiesSource configuration, String key)
   {
      final List<String> exclusions = new ArrayList<String>();
      for (String exclusion : configuration.get(key, "").split(","))
      {
         exclusions.add(exclusion.trim());
      }
      return exclusions;
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
}
