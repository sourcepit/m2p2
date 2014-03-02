
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
