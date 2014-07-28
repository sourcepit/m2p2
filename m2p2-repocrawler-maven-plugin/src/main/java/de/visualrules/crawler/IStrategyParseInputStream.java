/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package de.visualrules.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public interface IStrategyParseInputStream
{
   InputStream parseInputStream(String url);

   void close(InputStream inputStream);
}

class CreateConnection implements IStrategyParseInputStream
{
   private Proxy proxy;

   public CreateConnection(Proxy proxy)
   {
      this.proxy = proxy;
   }

   @Override
   public InputStream parseInputStream(String url)
   {
      InputStream inputStream = null;
      try
      {
         URLConnection openConnection;
         if (this.proxy == null)
         {
            openConnection = new URL(url).openConnection();
         }
         else
         {
            openConnection = new URL(url).openConnection(this.proxy);
         }
         inputStream = openConnection.getInputStream();
         return inputStream;
      }
      catch (IOException e)
      {
         System.out.println("URL unreachable " + url);
         e.printStackTrace();
         return null;
      }
   }

   @Override
   public void close(InputStream inputStream)
   {
      if (inputStream != null)
      {
         try
         {
            inputStream.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }
}
