/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package org.sourcepit.mp2p.cache;

import java.io.File;

import org.eclipse.equinox.internal.p2.repository.CacheManager;
import org.eclipse.equinox.internal.p2.repository.Transport;
import org.eclipse.equinox.internal.provisional.p2.core.eventbus.IProvisioningEventBus;
import org.eclipse.equinox.p2.core.IAgentLocation;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.spi.IAgentServiceFactory;
import org.eclipse.osgi.service.environment.EnvironmentInfo;

public class M2P2CacheManagerComponent implements IAgentServiceFactory
{
   private EnvironmentInfo envInfo;

   public void setEnvironmentInfo(EnvironmentInfo envInfo)
   {
      this.envInfo = envInfo;
   }

   public void unsetEnvironmentInfo(EnvironmentInfo envInfo)
   {
      this.envInfo = envInfo;
   }

   public Object createService(IProvisioningAgent agent)
   {
      final File dataDir = getDataDir(envInfo);

      final File cacheDir = new File(dataDir, "p2-repository-metadata");

      final IProvisioningEventBus eventBus = (IProvisioningEventBus) agent
         .getService(IProvisioningEventBus.SERVICE_NAME);
      CacheManager cache = new CacheManager((IAgentLocation) agent.getService(IAgentLocation.SERVICE_NAME),
         (Transport) agent.getService(Transport.SERVICE_NAME))
      {
         @Override
         protected File getCacheDirectory()
         {
            return cacheDir;
         }
      };

      cache.setEventBus(eventBus);
      return cache;
   }

   public static File getDataDir(final EnvironmentInfo envInfo)
   {
      String dataArea = envInfo.getProperty("m2p2.data.area");
      if (dataArea == null)
      {
         dataArea = envInfo.getProperty("user.home") + "/.m2p2";
      }

      final File dataAreaDir = new File(dataArea);
      dataAreaDir.mkdirs();

      return dataAreaDir;
   }

}
