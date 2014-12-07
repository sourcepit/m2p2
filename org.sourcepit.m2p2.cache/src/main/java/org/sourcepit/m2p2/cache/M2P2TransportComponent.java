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

import org.eclipse.equinox.internal.p2.transport.ecf.RepositoryTransport;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.spi.IAgentServiceFactory;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.service.log.LogService;

public class M2P2TransportComponent implements IAgentServiceFactory
{
   private LogService log;

   private EnvironmentInfo envInfo;

   public void setEnvironmentInfo(EnvironmentInfo envInfo)
   {
      this.envInfo = envInfo;
   }

   public void unsetEnvironmentInfo(EnvironmentInfo envInfo)
   {
      this.envInfo = envInfo;
   }

   public void setLog(LogService log)
   {
      this.log = log;
   }

   public void unsetLog(LogService log)
   {
      this.log = log;
   }

   public Object createService(IProvisioningAgent agent)
   {
      final File dataDir = M2P2CacheManagerComponent.getDataDir(envInfo);
      final File cacheDir = new File(dataDir, "p2-repository-artifacts");
      return new FileCacheTransport(cacheDir, new RepositoryTransport(agent), log);
   }
}
