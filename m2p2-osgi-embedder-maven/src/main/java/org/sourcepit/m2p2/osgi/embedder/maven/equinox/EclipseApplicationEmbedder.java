/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder.maven.equinox;

import static com.google.common.base.Preconditions.checkState;
import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.util.List;

import javax.inject.Inject;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher;
import org.eclipse.osgi.service.runnable.ApplicationLauncher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.sourcepit.common.utils.props.PropertiesSource;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;
import org.sourcepit.m2p2.osgi.embedder.maven.MavenEquinoxFactory;

public class EclipseApplicationEmbedder
{
   @Inject
   private LegacySupport buildContext;

   @Inject
   private MavenEquinoxFactory embedderFactory;

   @Inject
   private SettingsDecrypter settingsDecrypter;

   private OSGiEmbedder embedder;

   public synchronized void start(PropertiesSource configuration)
   {
      checkState(embedder == null);
      embedder = createOSGiEmbedder(configuration);
      embedder.start();
   }

   public synchronized void launch(List<String> arguments) throws Exception
   {
      checkState(embedder != null);
      final BundleContext bundleContext = embedder.getBundleContext();
      final EclipseAppLauncher appLauncher = new EclipseAppLauncher(bundleContext, false, true, null);
      final ServiceRegistration<?> registration = bundleContext.registerService(ApplicationLauncher.class.getName(),
         appLauncher, null);
      try
      {
         appLauncher.start(null);
      }
      finally
      {
         registration.unregister();
         appLauncher.shutdown();
      }
   }

   public synchronized void stop(long timeout)
   {
      if (embedder != null)
      {
         embedder.stop(timeout);
      }
   }

   private OSGiEmbedder createOSGiEmbedder(PropertiesSource configuration)
   {
      final MavenSession session = buildContext.getSession();

      final OSGiEmbedder embedder;
      try
      {
         embedder = embedderFactory.create(session, configuration);
      }
      catch (MavenExecutionException e)
      {
         throw pipe(e);
      }

      embedder.addLifecycleListener(new EquinoxEnvironmentConfigurer());
      embedder.addLifecycleListener(new EquinoxProxyConfigurer(buildContext, settingsDecrypter));
      embedder.addLifecycleListener(new P2RepositoryCredentialsConfigurer(buildContext, settingsDecrypter));

      return embedder;
   }
}
