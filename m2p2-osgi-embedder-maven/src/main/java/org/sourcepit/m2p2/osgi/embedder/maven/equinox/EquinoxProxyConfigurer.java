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

package org.sourcepit.m2p2.osgi.embedder.maven.equinox;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;
import static org.sourcepit.m2p2.osgi.embedder.BundleContextUtil.getService;

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
}
