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

package org.sourcepit.m2p2.cache;

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
