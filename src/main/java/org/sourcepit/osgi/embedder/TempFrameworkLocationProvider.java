/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.osgi.embedder;

import static org.apache.commons.io.FileUtils.forceDelete;

import java.io.File;
import java.io.IOException;

public class TempFrameworkLocationProvider implements FrameworkLocationProvider
{

   @Override
   public File aquireFrameworkLocation() throws IOException
   {
      File tempDir = File.createTempFile("equinox", "");
      if (!(tempDir.delete() && tempDir.mkdirs()))
      {
         throw new IOException("Could not create temp dir " + tempDir);
      }
      return tempDir;
   }

   @Override
   public void releaseFrameworkLocation(File frameworkLocation) throws IOException
   {
      IOException ioe = null;

      for (int i = 0; i < 12; i++)
      {
         try
         {
            forceDelete(frameworkLocation);
            ioe = null;
            break;
         }
         catch (IOException e)
         {
            ioe = e;
            try
            {
               Thread.sleep(250L);
            }
            catch (InterruptedException e1)
            { // noop
            }
         }
      }

      if (ioe != null)
      {
         throw ioe;
      }
   }

}
