/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.mp2p.cache;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.spi.IAgentServiceFactory;

public class M2P2ArtifactRepositoryComponent implements IAgentServiceFactory
{
   public Object createService(IProvisioningAgent agent)
   {
      return new M2P2ArtifactRepositoryManager(agent);
   }
}
