/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.sourcepit.mp2p.cache;

import java.io.File;

import org.eclipse.equinox.internal.p2.transport.ecf.RepositoryTransport;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.spi.IAgentServiceFactory;
import org.eclipse.osgi.service.environment.EnvironmentInfo;

public class M2P2TransportComponent implements IAgentServiceFactory
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
      final File dataDir = M2P2CacheManagerComponent.getDataDir(envInfo);
      final File cacheDir = new File(dataDir, "p2-repository-artifacts");
      return new FileCacheTransport(cacheDir, new RepositoryTransport(agent));
   }
}
