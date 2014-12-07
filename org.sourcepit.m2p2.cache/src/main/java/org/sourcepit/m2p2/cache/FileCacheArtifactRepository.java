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

import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.repository.IRunnableWithProgress;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRequest;

public class FileCacheArtifactRepository implements IArtifactRepository
{
   private final IArtifactRepository target;

   public FileCacheArtifactRepository(IArtifactRepository target)
   {
      this.target = target;
   }

   @Override
   public IQueryResult<IArtifactKey> query(IQuery<IArtifactKey> query, IProgressMonitor monitor)
   {
      return target.query(query, monitor);
   }

   @Override
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public Object getAdapter(Class adapter)
   {
      if (adapter.isAssignableFrom(this.getClass()))
      {
         return this;
      }
      return target.getAdapter(adapter);
   }

   @Override
   public IArtifactDescriptor createArtifactDescriptor(IArtifactKey key)
   {
      return target.createArtifactDescriptor(key);
   }

   @Override
   public IArtifactKey createArtifactKey(String classifier, String id, Version version)
   {
      return target.createArtifactKey(classifier, id, version);
   }

   @Override
   @SuppressWarnings("deprecation")
   public void addDescriptor(IArtifactDescriptor descriptor)
   {
      target.addDescriptor(descriptor);
   }

   @Override
   public void addDescriptor(IArtifactDescriptor descriptor, IProgressMonitor monitor)
   {
      target.addDescriptor(descriptor, monitor);
   }

   @Override
   @SuppressWarnings("deprecation")
   public void addDescriptors(IArtifactDescriptor[] descriptors)
   {
      target.addDescriptors(descriptors);
   }

   @Override
   public void addDescriptors(IArtifactDescriptor[] descriptors, IProgressMonitor monitor)
   {
      target.addDescriptors(descriptors, monitor);
   }

   @Override
   public boolean contains(IArtifactDescriptor descriptor)
   {
      return target.contains(descriptor);
   }

   @Override
   public boolean contains(IArtifactKey key)
   {
      return target.contains(key);
   }

   @Override
   public IStatus getArtifact(IArtifactDescriptor descriptor, OutputStream destination, IProgressMonitor monitor)
   {
      final IArtifactDescriptor current = FileCacheTransport.CACHE.get();
      if (current == descriptor)
      {
         return target.getArtifact(descriptor, destination, monitor);
      }

      if (current != null)
      {
         throw new IllegalArgumentException("artifact descriptor allready set");
      }

      FileCacheTransport.CACHE.set(descriptor);
      try
      {
         return target.getArtifact(descriptor, destination, monitor);
      }
      finally
      {
         FileCacheTransport.CACHE.set(null);
      }
   }

   @Override
   public URI getLocation()
   {
      return target.getLocation();
   }

   @Override
   public String getName()
   {
      return target.getName();
   }

   @Override
   public String getType()
   {
      return target.getType();
   }

   @Override
   public IStatus getRawArtifact(IArtifactDescriptor descriptor, OutputStream destination, IProgressMonitor monitor)
   {
      return target.getRawArtifact(descriptor, destination, monitor);
   }

   @Override
   public String getVersion()
   {
      return target.getVersion();
   }

   @Override
   public String getDescription()
   {
      return target.getDescription();
   }

   @Override
   public String getProvider()
   {
      return target.getProvider();
   }

   @Override
   public IArtifactDescriptor[] getArtifactDescriptors(IArtifactKey key)
   {
      return target.getArtifactDescriptors(key);
   }

   @Override
   public Map<String, String> getProperties()
   {
      return target.getProperties();
   }

   @Override
   public String getProperty(String key)
   {
      return target.getProperty(key);
   }

   private class ArtifactRequest implements IArtifactRequest
   {
      private final IArtifactRequest target;
      private final IArtifactRepository sourceRepository;

      public ArtifactRequest(IArtifactRequest target, IArtifactRepository sourceRepository)
      {
         this.target = target;
         this.sourceRepository = sourceRepository;
      }

      @Override
      public IArtifactKey getArtifactKey()
      {
         return target.getArtifactKey();
      }

      @Override
      public void perform(IArtifactRepository sourceRepository, IProgressMonitor monitor)
      {
         target.perform(this.sourceRepository, monitor);
      }

      @Override
      public IStatus getResult()
      {
         return target.getResult();
      }
   }

   @Override
   public IStatus getArtifacts(IArtifactRequest[] requests, IProgressMonitor monitor)
   {
      final IArtifactRequest[] wrappedRequests = new IArtifactRequest[requests.length];
      for (int i = 0; i < requests.length; i++)
      {
         final IArtifactRequest request = requests[i];
         final ArtifactRequest wrappedRequest;
         if (request instanceof ArtifactRequest)
         {
            wrappedRequest = new ArtifactRequest(((ArtifactRequest) request).target, this);
         }
         else
         {
            wrappedRequest = new ArtifactRequest(request, this);
         }
         wrappedRequests[i] = wrappedRequest;
      }
      return target.getArtifacts(wrappedRequests, monitor);
   }

   @Override
   public IProvisioningAgent getProvisioningAgent()
   {
      return target.getProvisioningAgent();
   }

   @Override
   public boolean isModifiable()
   {
      return target.isModifiable();
   }

   @Override
   public OutputStream getOutputStream(IArtifactDescriptor descriptor) throws ProvisionException
   {
      return target.getOutputStream(descriptor);
   }

   @Override
   public String setProperty(String key, String value)
   {
      return target.setProperty(key, value);
   }

   @Override
   public String setProperty(String key, String value, IProgressMonitor monitor)
   {
      return target.setProperty(key, value, monitor);
   }

   @Override
   public IQueryable<IArtifactDescriptor> descriptorQueryable()
   {
      return target.descriptorQueryable();
   }

   @Override
   @SuppressWarnings("deprecation")
   public void removeAll()
   {
      target.removeAll();
   }

   @Override
   public void removeAll(IProgressMonitor monitor)
   {
      target.removeAll(monitor);
   }

   @Override
   @SuppressWarnings("deprecation")
   public void removeDescriptor(IArtifactDescriptor descriptor)
   {
      target.removeDescriptor(descriptor);
   }

   @Override
   public void removeDescriptor(IArtifactDescriptor descriptor, IProgressMonitor monitor)
   {
      target.removeDescriptor(descriptor, monitor);
   }

   @Override
   @SuppressWarnings("deprecation")
   public void removeDescriptor(IArtifactKey key)
   {
      target.removeDescriptor(key);
   }

   @Override
   public void removeDescriptor(IArtifactKey key, IProgressMonitor monitor)
   {
      target.removeDescriptor(key, monitor);
   }

   @Override
   @SuppressWarnings("deprecation")
   public void removeDescriptors(IArtifactDescriptor[] descriptors)
   {
      target.removeDescriptors(descriptors);
   }

   @Override
   public void removeDescriptors(IArtifactDescriptor[] descriptors, IProgressMonitor monitor)
   {
      target.removeDescriptors(descriptors, monitor);
   }

   @Override
   @SuppressWarnings("deprecation")
   public void removeDescriptors(IArtifactKey[] keys)
   {
      target.removeDescriptors(keys);
   }

   @Override
   public void removeDescriptors(IArtifactKey[] keys, IProgressMonitor monitor)
   {
      target.removeDescriptors(keys, monitor);
   }

   @Override
   public IStatus executeBatch(IRunnableWithProgress runnable, IProgressMonitor monitor)
   {
      return target.executeBatch(runnable, monitor);
   }

}
