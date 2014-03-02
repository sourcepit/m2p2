/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.cache;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.internal.p2.artifact.repository.ArtifactRepositoryManager;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.repository.IRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;

public class M2P2ArtifactRepositoryManager extends ArtifactRepositoryManager implements IArtifactRepositoryManager
{
   public M2P2ArtifactRepositoryManager(IProvisioningAgent agent)
   {
      super(agent);
   }

   @Override
   public IArtifactRepository createRepository(URI location, String name, String type, Map<String, String> properties)
      throws ProvisionException
   {
      return super.createRepository(location, name, type, properties);
   }

   @Override
   protected IRepository<IArtifactKey> loadRepository(URI location, IProgressMonitor monitor, String type, int flags)
      throws ProvisionException
   {
      return super.loadRepository(location, monitor, type, flags);
   }

   @Override
   protected IRepository<IArtifactKey> factoryCreate(URI location, String name, String type,
      Map<String, String> properties, IExtension extension) throws ProvisionException
   {
      final IArtifactRepository repository = (IArtifactRepository) super.factoryCreate(location, name, type,
         properties, extension);
      return applyFileCache(repository);
   }

   @Override
   protected IRepository<IArtifactKey> factoryLoad(URI location, IExtension extension, int flags, SubMonitor monitor)
      throws ProvisionException
   {
      final IArtifactRepository repository = (IArtifactRepository) super.factoryLoad(location, extension, flags,
         monitor);
      return applyFileCache(repository);
   }

   private static IRepository<IArtifactKey> applyFileCache(final IArtifactRepository repository)
   {
      if (repository == null)
      {
         return null;
      }

      if (repository instanceof FileCacheArtifactRepository)
      {
         return null;
      }

      if (repository instanceof IFileArtifactRepository)
      {
         return new FileCacheFileArtifactRepository((IFileArtifactRepository) repository);
      }

      return new FileCacheArtifactRepository(repository);
   }
}
