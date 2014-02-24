/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.director;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;
import static org.sourcepit.m2p2.osgi.embedder.BundleContextUtil.getService;
import static org.sourcepit.m2p2.osgi.embedder.maven.equinox.EclipseEnvironmentInfo.newEclipseEnvironmentInfo;
import static org.sourcepit.m2p2.osgi.embedder.maven.equinox.SecurePreferencesUtil.getSecurePreferences;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.osgi.service.runnable.ApplicationLauncher;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.sourcepit.common.utils.props.LinkedPropertiesMap;
import org.sourcepit.common.utils.props.PropertiesMap;
import org.sourcepit.guplex.Guplex;
import org.sourcepit.m2p2.osgi.embedder.BundleContextUtil;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;
import org.sourcepit.m2p2.osgi.embedder.maven.MavenEquinoxFactory;
import org.sourcepit.m2p2.osgi.embedder.maven.equinox.EclipseEnvironmentInfo;
import org.sourcepit.m2p2.osgi.embedder.maven.equinox.EquinoxEnvironmentConfigurer;
import org.sourcepit.m2p2.osgi.embedder.maven.equinox.EquinoxProxyConfigurer;
import org.sourcepit.m2p2.osgi.embedder.maven.equinox.MavenRepositories;

@Mojo(name = "install", defaultPhase = PACKAGE, requiresProject = false)
public class M2P2DirectorMojo extends AbstractMojo
{
   @Component
   private Guplex guplex;

   @Inject
   private LegacySupport buildContext;

   @Inject
   private MavenEquinoxFactory embedderFactory;

   @Inject
   private SettingsDecrypter settingsDecrypter;


   // -application org.eclipse.equinox.p2.director

   // -repository http://download.eclipse.org/eclipse/updates/3.6
   @Parameter(required = true)
   private List<Repository> repositories;

   // -installIU org.eclipse.sdk.ide
   @Parameter(required = true)
   private List<InstallIU> installIUs;

   // -tag InitialState
   @Parameter(defaultValue = "InitialState")
   private String tag;

   // -destination d:/eclipse/
   @Parameter(defaultValue = "${project.build.directory}/eclipse")
   private File destination;

   // -profile SDKProfile
   @Parameter(defaultValue = "SDKProfile")
   private String profile;

   // -profileProperties org.eclipse.update.install.features=true
   @Parameter
   private Properties profileProperties;

   // -bundlepool d:/eclipse/
   @Parameter
   private File bundlepool;

   // -p2.os linux
   @Parameter
   private String os;

   // -p2.ws gtk
   @Parameter
   private String ws;

   // -p2.arch x86
   @Parameter
   private String arch;

   // -roaming
   @Parameter(defaultValue = "true")
   private boolean roaming;


   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      guplex.inject(this, true);

      final OSGiEmbedder embedder = createOSGiEmbedder();
      embedder.start();
      try
      {
         final BundleContext bundleContext = embedder.getBundleContext();

         final List<String> arguments = new ArrayList<String>();
         arguments.add("-application");
         arguments.add("org.eclipse.equinox.p2.director");

         arguments.add("-repository");
         arguments.add(toArgument(applyRepositories(bundleContext)));

         arguments.add("-installIUs");
         arguments.add(toArgument(installIUs));

         arguments.add("-destination");
         arguments.add(destination.toString());

         arguments.add("-tag");
         arguments.add(tag);

         arguments.add("-profile");
         arguments.add(profile);

         if (profileProperties != null)
         {
            arguments.add("-profileProperties");
            arguments.add(toArgument(profileProperties));
         }

         if (bundlepool != null)
         {
            arguments.add("-bundlepool");
            arguments.add(bundlepool.toString());
         }

         if (os != null)
         {
            arguments.add("-p2.os");
            arguments.add(os);
         }

         if (ws != null)
         {
            arguments.add("-p2.ws");
            arguments.add(ws);
         }

         if (arch != null)
         {
            arguments.add("-p2.arch");
            arguments.add(arch);
         }

         if (roaming)
         {
            arguments.add("-roaming");
         }

         // set new env args
         final EclipseEnvironmentInfo envInfo = newEclipseEnvironmentInfo(embedder.getFrameworkClassLoader());
         final String[] args = arguments.toArray(new String[arguments.size()]);
         envInfo.setAllArgs(args);
         envInfo.setAppArgs(args);
         envInfo.setFrameworkArgs(args);

         // reset org.eclipse.equinox.internal.app.CommandLineArgs
         final Bundle equinoxApp = BundleContextUtil.getBundle(bundleContext, "org.eclipse.equinox.app");
         equinoxApp.stop();
         equinoxApp.start();

         final EclipseAppLauncher appLauncher = new EclipseAppLauncher(bundleContext, false, true, null);
         final ServiceRegistration<?> registration = bundleContext.registerService(ApplicationLauncher.class.getName(),
            appLauncher, null);
         appLauncher.start(null);
         registration.unregister();
         appLauncher.shutdown();
      }
      catch (Exception e)
      {
         throw new MojoExecutionException("Failed to launch eclipse application.", e);
      }
      finally
      {
         embedder.stop(0);
      }
   }

   private Collection<URI> applyRepositories(final BundleContext bundleContext)
   {
      final Map<String, String> repositories = new LinkedHashMap<String, String>();
      for (Repository repository : this.repositories)
      {
         repositories.put(repository.getId(), repository.getUrl());
      }

      final ISecurePreferences securePreferences = getSecurePreferences(bundleContext);

      final MavenSession session = buildContext.getSession();

      final LogService logger = getService(bundleContext, LogService.class);
      final List<Server> servers = session.getSettings().getServers();

      return MavenRepositories.applyMavenP2Repositories(securePreferences, settingsDecrypter, servers, repositories,
         logger);
   }

   private static <V> String toArgument(Collection<V> values)
   {
      final StringBuilder sb = new StringBuilder();
      for (V value : values)
      {
         sb.append(value.toString());
         sb.append(',');
      }
      sb.deleteCharAt(sb.length() - 1);
      return sb.toString();
   }

   private static <K, V> String toArgument(Map<K, V> map)
   {
      final StringBuilder sb = new StringBuilder();
      for (Entry<K, V> entry : map.entrySet())
      {
         sb.append(entry.getKey());
         sb.append('=');
         sb.append(entry.getValue());
         sb.append(',');
      }
      sb.deleteCharAt(sb.length() - 1);
      return sb.toString();
   }

   private OSGiEmbedder createOSGiEmbedder() throws MojoExecutionException
   {
      final MavenSession session = buildContext.getSession();

      final OSGiEmbedder embedder;
      try
      {
         embedder = embedderFactory.create(session, readEmbedderConfiguration());
      }
      catch (MavenExecutionException e)
      {
         throw new MojoExecutionException("Failed to create OSGi embedder.", e);
      }

      embedder.addLifecycleListener(new EquinoxEnvironmentConfigurer());
      embedder.addLifecycleListener(new EquinoxProxyConfigurer(buildContext, settingsDecrypter));
      // embedder.addLifecycleListener(new P2RepositoryCredentialsConfigurer(buildContext, settingsDecrypter));

      return embedder;
   }

   private PropertiesMap readEmbedderConfiguration()
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
