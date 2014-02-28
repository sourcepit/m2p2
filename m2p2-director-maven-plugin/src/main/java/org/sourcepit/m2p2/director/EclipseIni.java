/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
