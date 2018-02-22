/*
 * Copyright 2014 Bernd Vogt and others.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sourcepit.m2p2.director;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;
import static org.sourcepit.common.utils.io.IO.buffOut;
import static org.sourcepit.common.utils.io.IO.fileOut;
import static org.sourcepit.common.utils.io.IO.write;
import static org.sourcepit.m2p2.director.EclipseIniUtil.applyDefaults;
import static org.sourcepit.m2p2.director.EclipseIniUtil.parse;
import static org.sourcepit.m2p2.director.EclipseIniUtil.save;
import static org.sourcepit.m2p2.osgi.embedder.BundleContextUtil.getService;
import static org.sourcepit.m2p2.osgi.embedder.maven.equinox.EclipseEnvironmentInfo.newEclipseEnvironmentInfo;
import static org.sourcepit.m2p2.osgi.embedder.maven.equinox.SecurePreferencesUtil.getSecurePreferences;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.service.runnable.ApplicationLauncher;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.sourcepit.common.utils.io.Write.ToStream;
import org.sourcepit.common.utils.props.LinkedPropertiesMap;
import org.sourcepit.common.utils.props.PropertiesMap;
import org.sourcepit.m2p2.osgi.embedder.AbstractOSGiEmbedderLifecycleListener;
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
   private List<String> installIUs;

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

   @Parameter
   private List<TargetEnvironment> envs;

   // -roaming
   @Parameter(defaultValue = "false")
   private boolean roaming;

   @Parameter(defaultValue = "${project.build.sourceEncoding}")
   private String defaultEncoding;

   @Parameter
   private EclipseIni eclipseIni;

   @Parameter(defaultValue = "${project.build.directory}/p2-director")
   private File p2DirectorWorkDir;

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      if (defaultEncoding == null || defaultEncoding.equals("${project.build.sourceEncoding}"))
      {
         defaultEncoding = Charset.defaultCharset().name();
      }

      final OSGiEmbedder embedder = createOSGiEmbedder();
      embedder.start();
      try
      {
         final BundleContext bundleContext = embedder.getBundleContext();

         final Collection<URI> repositories = applyRepositories(bundleContext);

         for (TargetEnvironment env : envs)
         {
            final List<String> arguments = createArguments(repositories, env);

            final StringBuilder sb = new StringBuilder();
            for (String argument : arguments)
            {
               sb.append(argument);
               sb.append(' ');
            }
            sb.deleteCharAt(sb.length() - 1);

            getLog().info(sb.toString());

            // set new env args
            final EclipseEnvironmentInfo envInfo = newEclipseEnvironmentInfo(bundleContext);
            final String[] args = arguments.toArray(new String[arguments.size()]);
            envInfo.setAllArgs(args);
            envInfo.setAppArgs(args);
            envInfo.setFrameworkArgs(args);

            // reset org.eclipse.equinox.internal.app.CommandLineArgs
            final Bundle equinoxApp = BundleContextUtil.getBundle(bundleContext, "org.eclipse.equinox.app");
            equinoxApp.stop();
            equinoxApp.start();

            final FrameworkLog log = BundleContextUtil.getService(bundleContext, FrameworkLog.class);
            final EclipseAppLauncher appLauncher = new EclipseAppLauncher(bundleContext, false, true, log, null);
            final ServiceRegistration<?> registration = bundleContext
               .registerService(ApplicationLauncher.class.getName(), appLauncher, null);
            appLauncher.start(null);
            registration.unregister();
            appLauncher.shutdown();

         }
      }
      catch (Exception e)
      {
         throw new MojoExecutionException("Failed to launch eclipse application.", e);
      }
      finally
      {
         embedder.stop(0);
      }

      for (TargetEnvironment env : envs)
      {
         if (eclipseIni != null && eclipseIni.getAppArgs() != null && eclipseIni.getVMArgs() != null)
         {
            try
            {
               adoptEclipseIni(env);
            }
            catch (IOException e)
            {
               throw new MojoExecutionException("Failed to adopt eclipse.ini.", e);
            }
         }
      }
   }

   private List<String> createArguments(final Collection<URI> repositories, TargetEnvironment env)
   {
      final List<String> arguments = new ArrayList<String>();
      arguments.add("-application");
      arguments.add("org.sourcepit.mp2p.director");

      arguments.add("-repository");
      arguments.add(toArgument(repositories));

      arguments.add("-installIUs");
      arguments.add(toArgument(installIUs));

      arguments.add("-tag");
      arguments.add(tag);

      arguments.add("-profile");
      arguments.add(profile);

      if (profileProperties == null)
      {
         profileProperties = new Properties();
         profileProperties.setProperty("org.eclipse.update.install.features", "true");
      }

      if (profileProperties != null)
      {
         arguments.add("-profileProperties");
         arguments.add(toArgument(profileProperties));
      }

      if (env.getOs() != null)
      {
         arguments.add("-p2.os");
         arguments.add(env.getOs());
      }

      if (env.getWs() != null)
      {
         arguments.add("-p2.ws");
         arguments.add(env.getWs());
      }

      if (env.getArch() != null)
      {
         arguments.add("-p2.arch");
         arguments.add(env.getArch());
      }

      arguments.add("-destination");
      arguments.add(new File(destination, env.toString()).toString());

      if (roaming)
      {
         arguments.add("-roaming");
      }
      return arguments;
   }

   private void adoptEclipseIni(TargetEnvironment env) throws IOException
   {
      final File destination = new File(this.destination, env.toString());
      
      applyDefaults(destination, eclipseIni, defaultEncoding);

      final List<String> appArgs = new ArrayList<String>();
      final List<String> vmArgs = new ArrayList<String>();
      parse(destination, eclipseIni, appArgs, vmArgs);

      final ArgumentModifications appArgMods = eclipseIni.getAppArgs();
      if (appArgMods != null)
      {
         appArgMods.apply(appArgs);
      }
      final ArgumentModifications vmArgMods = eclipseIni.getVMArgs();
      if (vmArgMods != null)
      {
         vmArgMods.apply(vmArgs);
      }

      save(destination, eclipseIni, appArgs, vmArgs);
   }

   public static void writeLines(File file, final String encoding, final String eol, List<String> lines)
      throws IOException
   {
      write(new ToStream<List<String>>()
      {
         @Override
         public void write(OutputStream out, List<String> lines) throws Exception
         {
            final Writer writer = new OutputStreamWriter(out, encoding);
            for (String line : lines)
            {
               writer.write(line);
               writer.write(eol);
            }
         }
      }, buffOut(fileOut(file)), lines);
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
         final PropertiesMap conf = readEmbedderConfiguration();
         conf.put("m2p2.workDir", p2DirectorWorkDir.getAbsolutePath());
         embedder = embedderFactory.create(session, conf);
      }
      catch (MavenExecutionException e)
      {
         throw new MojoExecutionException("Failed to create OSGi embedder.", e);
      }

      embedder.addLifecycleListener(new AbstractOSGiEmbedderLifecycleListener()
      {
         @Override
         public void frameworkPropertiesInitialized(OSGiEmbedder embeddedEquinox,
            Map<String, String> frameworkProperties)
         {
            final String localRepoDir = buildContext.getSession().getLocalRepository().getBasedir();

            final File dataArea = new File(localRepoDir, ".cache/m2p2");
            dataArea.mkdirs();

            frameworkProperties.put("m2p2.data.area", dataArea.getAbsolutePath());
            super.frameworkPropertiesInitialized(embeddedEquinox, frameworkProperties);
         }
      });

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
