/*
 * Copyright (C) 2014 Bosch Software Innovations GmbH. All rights reserved.
 */

package de.visualrules.crawler;

import java.io.IOException;
import java.net.Proxy;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public final class DocumentUtil
{
   private DocumentUtil()
   {
      super();
   }

   /**
    * Returns null if the connection couldn't be established
    * 
    * @return
    */
   public static Document configureDocument(String url, Proxy proxy, IStrategyParseInputStream strategy)
   {
      try
      {
         return Jsoup.parse(strategy.parseInputStream(url), null, url);
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public static void collectHierachicalChildURLs(Elements resultLinks, Set<String> urls)
   {
      for (Element result : resultLinks)
      {
         if (isHierachicalChildURL(result))
         {
            if (!isBinary(result))
            {
               urls.add(result.absUrl("href"));
            }
         }
      }
   }

   static boolean isHierachicalChildURL(Element result)
   {
      final String baseURL = result.ownerDocument().location();
      final String someURL = result.absUrl("href");
      return isHierachicalChildURL(baseURL, someURL);
   }

   static boolean isHierachicalChildURL(final String baseURL, final String someURL)
   {
      return someURL.startsWith(baseURL);
   }

   private static boolean isBinary(Element result)
   {
      if (result.attr("href").endsWith("/"))
      {
         return false;
      }
      else
      {
         return true;
      }
   }
}
