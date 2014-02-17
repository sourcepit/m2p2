/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder.maven.equinox;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.osgi.framework.BundleContext;
import org.sourcepit.m2p2.osgi.embedder.AbstractOSGiEmbedderLifecycleListener;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;

public class EquinoxProxyConfigurer extends AbstractOSGiEmbedderLifecycleListener
{
   private final LegacySupport buildContext;

   private final SettingsDecrypter settingsDecrypter;

   public EquinoxProxyConfigurer(LegacySupport buildContext, SettingsDecrypter settingsDecrypter)
   {
      this.buildContext = buildContext;
      this.settingsDecrypter = settingsDecrypter;
   }

   @Override
   public void bundlesStarted(OSGiEmbedder embeddedEquinox)
   {
      final BundleContext bundleContext = embeddedEquinox.getBundleContext();

      final IProxyService proxyService = getService(bundleContext, IProxyService.class);

      try
      {
         MavenProxies.applyMavenProxies(proxyService, settingsDecrypter, buildContext.getSession().getSettings()
            .getProxies());
      }
      catch (CoreException e)
      {
         throw pipe(e);
      }
   }

   private static <S> S getService(BundleContext context, Class<S> serviceType)
   {
      return context.getService(context.getServiceReference(serviceType));
   }
}
