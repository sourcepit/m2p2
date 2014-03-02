/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.mp2p.cache;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.repository.AuthenticationFailedException;
import org.eclipse.equinox.internal.p2.repository.Transport;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;

public class FileCacheTransport extends Transport
{
   public static final ThreadLocal<IArtifactDescriptor> CACHE = new ThreadLocal<IArtifactDescriptor>();

   private File cacheDir;

   private final Transport target;

   public FileCacheTransport(File cacheDir, Transport target)
   {
      this.cacheDir = cacheDir;
      this.target = target;
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
         FileInputStream fis = null;
         try
         {
            fis = new FileInputStream(artifactFile);

            System.out.println("From cache: " + descriptor.getArtifactKey().toExternalForm());
            BufferedInputStream buff = new BufferedInputStream(fis);
            copyLarge(buff, target, new byte[DEFAULT_BUFFER_SIZE]);

            return org.eclipse.core.runtime.Status.OK_STATUS;
         }
         catch (IOException e)
         {
            throw new IllegalStateException(e);
         }
         finally
         {
            if (fis != null)
            {
               try
               {
                  fis.close();
               }
               catch (IOException e)
               {
               }
            }
         }
      }
      else
      {
         FileOutputStream fos = null;
         try
         {
            artifactFile.getParentFile().mkdirs();
            artifactFile.createNewFile();

            fos = new FileOutputStream(artifactFile);

            final MessageDigest md5Digest;
            try
            {
               md5Digest = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException e)
            {
               throw new IllegalStateException(e);
            } //$NON-NLS-1$


            final BufferedOutputStream buff = new BufferedOutputStream(fos);

            FilterOutputStream agent = new FilterOutputStream(target)
            {
               @Override
               public void write(int b) throws IOException
               {
                  buff.write(b);
                  md5Digest.update((byte) b);
                  super.write(b);
               }
            };

            IStatus result = this.target.download(toDownload, agent, monitor);

            buff.flush();
            buff.close();

            byte[] digest = md5Digest.digest();
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < digest.length; i++)
            {
               if ((digest[i] & 0xFF) < 0x10)
                  buf.append('0');
               buf.append(Integer.toHexString(digest[i] & 0xFF));
            }

            String actualMd5 = buf.toString();
            System.out.println(descriptor.getArtifactKey().toExternalForm());
            System.out.println(md5);
            System.out.println(actualMd5);
            System.out.println();

            return result;
         }
         catch (IOException e)
         {
            throw new IllegalStateException(e);
         }
         finally
         {
            if (fos != null)
            {
               try
               {
                  fos.close();
               }
               catch (IOException e)
               {
               }
            }
         }
      }
   }

   private static final int EOF = -1;
   private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

   public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException
   {
      long count = 0;
      int n = 0;
      while (EOF != (n = input.read(buffer)))
      {
         output.write(buffer, 0, n);
         count += n;
      }
      return count;
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
