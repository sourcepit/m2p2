/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.cache;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.FileUtils.moveFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.repository.AuthenticationFailedException;
import org.eclipse.equinox.internal.p2.repository.Transport;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.osgi.service.log.LogService;

public class FileCacheTransport extends Transport
{
   public static final ThreadLocal<IArtifactDescriptor> CACHE = new ThreadLocal<IArtifactDescriptor>();

   private File cacheDir;

   private final Transport target;

   private final LogService log;

   public FileCacheTransport(File cacheDir, Transport target, LogService log)
   {
      this.cacheDir = cacheDir;
      this.target = target;
      this.log = log;
   }

   public IStatus download(URI toDownload, OutputStream target, long startPos, IProgressMonitor monitor)
   {
      return this.target.download(toDownload, target, startPos, monitor);
   }

   public IStatus download(URI toDownload, OutputStream target, IProgressMonitor monitor)
   {
      final IArtifactDescriptor descriptor = CACHE.get();
      if (descriptor == null)
      {
         return this.target.download(toDownload, target, monitor);
      }

      final String md5 = descriptor.getProperty(IArtifactDescriptor.DOWNLOAD_MD5);
      if (md5 == null)
      {
         return this.target.download(toDownload, target, monitor);
      }

      final File artifactFile = new File(cacheDir, md5);
      if (artifactFile.exists())
      {
         return fromCache(target, descriptor, artifactFile);
      }
      else
      {
         return cache(toDownload, target, monitor, descriptor, artifactFile, md5);
      }
   }

   private IStatus cache(URI toDownload, OutputStream target, IProgressMonitor monitor,
      final IArtifactDescriptor descriptor, File artifactFile, String md5)
   {
      log.log(LogService.LOG_INFO, "Downloading " + descriptor.getArtifactKey().toExternalForm());

      final MessageDigest md5Digest = newMd5Digest();

      final File tmpFile;
      try
      {
         tmpFile = createTempFile(artifactFile);
      }
      catch (IOException e)
      {
         throw new IllegalStateException(e);
      }

      final IStatus result;

      DigestOutputStream out = null;
      try
      {
         out = new DigestOutputStream(new CopyOutputStream(target, new BufferedOutputStream(new FileOutputStream(
            tmpFile))), md5Digest);
         result = this.target.download(toDownload, out, monitor);
         out.flush();
      }
      catch (IOException e)
      {
         deleteQuietly(tmpFile);
         throw new IllegalStateException(e);
      }
      finally
      {
         IOUtils.closeQuietly(out);
      }

      final String actualMd5 = toHexString(out.getMessageDigest().digest());
      if (!md5.equals(actualMd5))
      {
         log.log(LogService.LOG_WARNING, "Unable to cache artifact " + descriptor.getArtifactKey().toExternalForm()
            + " due to checksum verification failure. Expected " + md5 + " but was " + actualMd5 + ".");
         deleteQuietly(tmpFile);
      }
      else
      {
         try
         {
            deleteFile(artifactFile);
            moveFile(tmpFile, artifactFile);
         }
         catch (IOException e)
         {
            throw new IllegalStateException(e);
         }
      }

      return result;
   }

   private File createTempFile(File file) throws IOException
   {
      final File dir = file.getParentFile();
      forceMkdir(dir);
      return File.createTempFile(file.getName() + "_", ".tmp", dir);
   }

   private void deleteFile(File file) throws IOException
   {
      try
      {
         forceDelete(file);
      }
      catch (FileNotFoundException e)
      {
      }
   }

   private static String toHexString(byte[] digest)
   {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < digest.length; i++)
      {
         if ((digest[i] & 0xFF) < 0x10)
            buf.append('0');
         buf.append(Integer.toHexString(digest[i] & 0xFF));
      }
      return buf.toString();
   }

   private static MessageDigest newMd5Digest()
   {
      final MessageDigest md5Digest;
      try
      {
         md5Digest = MessageDigest.getInstance("MD5");
      }
      catch (NoSuchAlgorithmException e)
      {
         throw new IllegalStateException(e);
      }
      return md5Digest;
   }

   private static final class CopyOutputStream extends FilterOutputStream
   {
      private OutputStream[] outs;

      public CopyOutputStream(OutputStream out, OutputStream... outs)
      {
         super(out);
         this.outs = outs == null ? new OutputStream[0] : outs;
      }

      @Override
      public void write(int b) throws IOException
      {
         super.write(b);
         for (OutputStream out : outs)
         {
            out.write(b);
         }
      }

      @Override
      public void flush() throws IOException
      {
         try
         {
            super.flush();
         }
         finally
         {
            for (OutputStream out : outs)
            {
               try
               {
                  out.flush();
               }
               catch (IOException e)
               {
               }
            }
         }
      }

      @Override
      public void close() throws IOException
      {
         try
         {
            super.close();
         }
         finally
         {
            for (OutputStream out : outs)
            {
               try
               {
                  out.close();
               }
               catch (IOException e)
               {
               }
            }
         }
      }
   }

   private IStatus fromCache(OutputStream target, final IArtifactDescriptor descriptor, final File artifactFile)
   {
      InputStream in = null;
      try
      {
         in = new FileInputStream(artifactFile);
         log.log(LogService.LOG_INFO, "Downloading " + descriptor.getArtifactKey().toExternalForm() + " (cached)");
         IOUtils.copyLarge(new BufferedInputStream(in), target);
         return org.eclipse.core.runtime.Status.OK_STATUS;
      }
      catch (IOException e)
      {
         throw new IllegalStateException(e);
      }
      finally
      {
         IOUtils.closeQuietly(in);
      }
   }

   public InputStream stream(URI toDownload, IProgressMonitor monitor) throws FileNotFoundException, CoreException,
      AuthenticationFailedException
   {
      return target.stream(toDownload, monitor);
   }

   public long getLastModified(URI toDownload, IProgressMonitor monitor) throws CoreException, FileNotFoundException,
      AuthenticationFailedException
   {
      return target.getLastModified(toDownload, monitor);
   }

}
