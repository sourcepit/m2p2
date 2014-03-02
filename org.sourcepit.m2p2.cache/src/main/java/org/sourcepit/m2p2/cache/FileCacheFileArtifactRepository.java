/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.cache;

import java.io.File;

import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;

public class FileCacheFileArtifactRepository extends FileCacheArtifactRepository implements IFileArtifactRepository
{
   private final IFileArtifactRepository target;

   public FileCacheFileArtifactRepository(IFileArtifactRepository target)
   {
      super(target);
      this.target = target;
   }

   @Override
   public File getArtifactFile(IArtifactKey key)
   {
      return target.getArtifactFile(key);
   }

   @Override
   public File getArtifactFile(IArtifactDescriptor descriptor)
   {
      return target.getArtifactFile(descriptor);
   }

}
