/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.director;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;
import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.sourcepit.common.utils.props.LinkedPropertiesMap;
import org.sourcepit.common.utils.props.PropertiesMap;
import org.sourcepit.guplex.Guplex;
import org.sourcepit.m2p2.osgi.embedder.AbstractOSGiEmbedderLifecycleListener;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;
import org.sourcepit.m2p2.osgi.embedder.maven.MavenEquinoxFactory;

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

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      guplex.inject(this, true);

      final OSGiEmbedder embedder = createOSGiEmbedder();
      embedder.start();
      try
      {

      }
      finally
      {
         embedder.stop(0);
      }
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

      embedder.addLifecycleListener(new AbstractOSGiEmbedderLifecycleListener()
      {
         @Override
         public void bundlesStarted(OSGiEmbedder embeddedEquinox)
         {
            final BundleContext bundleContext = embeddedEquinox.getBundleContext();

            final IProxyService proxyService = getService(bundleContext, IProxyService.class);

            try
            {
               MavenProxies.applyMavenProxies(proxyService, settingsDecrypter, session.getSettings().getProxies());
            }
            catch (CoreException e)
            {
               throw pipe(e);
            }

            final ISecurePreferences securePreferences = getSecurePreferences(bundleContext);

            final MavenProject mavenProject = session.getCurrentProject();
            final List<ArtifactRepository> repositories = mavenProject.getRemoteArtifactRepositories();
            final LogService logger = getService(bundleContext, LogService.class);
            final List<Server> servers = session.getSettings().getServers();

            MavenRepositories.applyMavenP2Repositories(securePreferences, settingsDecrypter, servers, repositories,
               logger);
         }

         private ISecurePreferences getSecurePreferences(final BundleContext bundleContext)
         {
            try
            {
               final Class<?> clazz = getBundle(bundleContext, "org.eclipse.equinox.security").loadClass(
                  SecurePreferencesFactory.class.getName());
               return (ISecurePreferences) clazz.getMethod("getDefault").invoke(null);
            }
            catch (ClassNotFoundException e)
            {
               throw pipe(e);
            }
            catch (IllegalAccessException e)
            {
               throw pipe(e);
            }
            catch (InvocationTargetException e)
            {
               throw pipe(e);
            }
            catch (NoSuchMethodException e)
            {
               throw pipe(e);
            }
         }
      });

      return embedder;
   }

   private static Bundle getBundle(BundleContext bundleContext, String symbolicName)
   {
      for (Bundle bundle : bundleContext.getBundles())
      {
         if (symbolicName.equals(bundle.getSymbolicName()))
         {
            return bundle;
         }
      }
      return null;
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

   private static <S> S getService(BundleContext context, Class<S> serviceType)
   {
      return context.getService(context.getServiceReference(serviceType));
   }

}
