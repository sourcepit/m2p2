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

package org.sourcepit.m2p2.director;
public class EclipseIni
{
   private String fileName;
   private String eol;
   private String encoding;

   private ArgumentModifications appArgs;

   private ArgumentModifications vmArgs;

   public String getFileName()
   {
      return fileName;
   }

   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }

   public String getEOL()
   {
      return eol;
   }

   public void setEOL(String eol)
   {
      this.eol = eol;
   }

   public String getEncoding()
   {
      return encoding;
   }

   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }

   public ArgumentModifications getAppArgs()
   {
      return appArgs;
   }

   public void setAppArgs(ArgumentModifications appArgs)
   {
      this.appArgs = appArgs;
   }

   public ArgumentModifications getVMArgs()
   {
      return vmArgs;
   }

   public void setVMArgs(ArgumentModifications vmArgs)
   {
      this.vmArgs = vmArgs;
   }

}
