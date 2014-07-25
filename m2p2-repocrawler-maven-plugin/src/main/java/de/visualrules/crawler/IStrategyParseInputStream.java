/*
 * Copyright (C) 2014 Bosch Software Innovations GmbH. All rights reserved.
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
