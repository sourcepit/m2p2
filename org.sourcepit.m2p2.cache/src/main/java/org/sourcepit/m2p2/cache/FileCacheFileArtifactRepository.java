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
