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

package org.sourcepit.m2p2.osgi.embedder;

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
