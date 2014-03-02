/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.cache;

import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.repository.Transport;
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

   public IQueryResult<IArtifactKey> query(IQuery<IArtifactKey> query, IProgressMonitor monitor)
   {
      return target.query(query, monitor);
   }

   public Object getAdapter(Class adapter)
   {
      if (adapter.isAssignableFrom(this.getClass()))
      {
         return this;
      }
      return target.getAdapter(adapter);
   }

   public IArtifactDescriptor createArtifactDescriptor(IArtifactKey key)
   {
      return target.createArtifactDescriptor(key);
   }

   public IArtifactKey createArtifactKey(String classifier, String id, Version version)
   {
      return target.createArtifactKey(classifier, id, version);
   }

   public void addDescriptor(IArtifactDescriptor descriptor)
   {
      target.addDescriptor(descriptor);
   }

   public void addDescriptor(IArtifactDescriptor descriptor, IProgressMonitor monitor)
   {
      target.addDescriptor(descriptor, monitor);
   }

   public void addDescriptors(IArtifactDescriptor[] descriptors)
   {
      target.addDescriptors(descriptors);
   }

   public void addDescriptors(IArtifactDescriptor[] descriptors, IProgressMonitor monitor)
   {
      target.addDescriptors(descriptors, monitor);
   }

   public boolean contains(IArtifactDescriptor descriptor)
   {
      return target.contains(descriptor);
   }

   public boolean contains(IArtifactKey key)
   {
      return target.contains(key);
   }

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

   private Transport getTransport()
   {
      return (Transport) getProvisioningAgent().getService(Transport.SERVICE_NAME);
   }


   public URI getLocation()
   {
      return target.getLocation();
   }

   public String getName()
   {
      return target.getName();
   }

   public String getType()
   {
      return target.getType();
   }

   public IStatus getRawArtifact(IArtifactDescriptor descriptor, OutputStream destination, IProgressMonitor monitor)
   {
      return target.getRawArtifact(descriptor, destination, monitor);
   }

   public String getVersion()
   {
      return target.getVersion();
   }

   public String getDescription()
   {
      return target.getDescription();
   }

   public String getProvider()
   {
      return target.getProvider();
   }

   public IArtifactDescriptor[] getArtifactDescriptors(IArtifactKey key)
   {
      return target.getArtifactDescriptors(key);
   }

   public Map<String, String> getProperties()
   {
      return target.getProperties();
   }

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

      public IArtifactKey getArtifactKey()
      {
         return target.getArtifactKey();
      }

      public void perform(IArtifactRepository sourceRepository, IProgressMonitor monitor)
      {
         target.perform(this.sourceRepository, monitor);
      }

      public IStatus getResult()
      {
         return target.getResult();
      }
   }

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

   public IProvisioningAgent getProvisioningAgent()
   {
      return target.getProvisioningAgent();
   }

   public boolean isModifiable()
   {
      return target.isModifiable();
   }

   public OutputStream getOutputStream(IArtifactDescriptor descriptor) throws ProvisionException
   {
      return target.getOutputStream(descriptor);
   }

   public String setProperty(String key, String value)
   {
      return target.setProperty(key, value);
   }

   public String setProperty(String key, String value, IProgressMonitor monitor)
   {
      return target.setProperty(key, value, monitor);
   }

   public IQueryable<IArtifactDescriptor> descriptorQueryable()
   {
      return target.descriptorQueryable();
   }

   public void removeAll()
   {
      target.removeAll();
   }

   public void removeAll(IProgressMonitor monitor)
   {
      target.removeAll(monitor);
   }

   public void removeDescriptor(IArtifactDescriptor descriptor)
   {
      target.removeDescriptor(descriptor);
   }

   public void removeDescriptor(IArtifactDescriptor descriptor, IProgressMonitor monitor)
   {
      target.removeDescriptor(descriptor, monitor);
   }

   public void removeDescriptor(IArtifactKey key)
   {
      target.removeDescriptor(key);
   }

   public void removeDescriptor(IArtifactKey key, IProgressMonitor monitor)
   {
      target.removeDescriptor(key, monitor);
   }

   public void removeDescriptors(IArtifactDescriptor[] descriptors)
   {
      target.removeDescriptors(descriptors);
   }

   public void removeDescriptors(IArtifactDescriptor[] descriptors, IProgressMonitor monitor)
   {
      target.removeDescriptors(descriptors, monitor);
   }

   public void removeDescriptors(IArtifactKey[] keys)
   {
      target.removeDescriptors(keys);
   }

   public void removeDescriptors(IArtifactKey[] keys, IProgressMonitor monitor)
   {
      target.removeDescriptors(keys, monitor);
   }

   public IStatus executeBatch(IRunnableWithProgress runnable, IProgressMonitor monitor)
   {
      return target.executeBatch(runnable, monitor);
   }

}
