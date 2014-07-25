/*
 * Copyright (C) 2014 Bosch Software Innovations GmbH. All rights reserved.
 */

package de.visualrules.crawler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

/**
 * Starts the <code>Finder</code> class to get the eclipse update sites of the given url
 * 
 * @author KLK1IMB
 */
public class Crawler
{
   private final Proxy proxy;

   public Crawler()
   {
      this.proxy = null;
   }

   public Crawler(String proxy, int port)
   {
      this.proxy = this.setProxySettings(proxy, port);
   }

   private Proxy setProxySettings(String proxyAddress, int port)
   {
      return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(proxyAddress, port));
   }

   /**
    * This method begins with the start of the search after the update sites. <br>
    * It initializes the <code>Finder</code> and the <code>XMLMaker</code>. <br>
    * With the <code>Finder</code> the search after update sites will be started. <br>
    * The <code>XMLMaker</code> will take the found URLs and it will save them in a XML file.
    * 
    * @param url - URL to the page that should be searched
    * @param saveFolderPath - Path to the folder in which the XML file should be saved
    * @param fileName - Filename of the generated xml file
    * @param repositoryNameInXMLFile - It is the name attribute in the repository element of the xml file
    * @throws IOException 
    */
   public void start(String url, String saveFolderPath, String fileName, String repositoryNameInXMLFile) throws IOException
   {
      long start = System.currentTimeMillis();

      List<String> updateSites = startFinder(url);

      if (repositoryNameInXMLFile == null)
      {
         repositoryNameInXMLFile = "";
      }

      if (!updateSites.isEmpty())
      {
         generateXML(saveFolderPath, fileName, repositoryNameInXMLFile, updateSites);
      }
      else
      {
         // Try it with "/" at the end of the URL
         updateSites = startFinder(url + "/");
         if (!updateSites.isEmpty())
         {
            generateXML(saveFolderPath, fileName, repositoryNameInXMLFile, updateSites);
         }
         else
         {
            System.out.println("No URLs to an update site found");
         }
      }
      System.out.println("Time: " + (System.currentTimeMillis() - start) + " millis");
   }

   private void generateXML(String saveFolderPath, String fileName, String repositoryNameInXMLFile,
      List<String> updateSites) throws IOException
   {
      // Print the results
      System.out.println("-------------Saved Links-----------------");
      System.out.println("-------------Saved Links-----------------");
      for (String string : updateSites)
      {
         System.out.println(string);
      }

      XMLMaker xmlMaker = new XMLMaker(updateSites);
      xmlMaker.generateXML(saveFolderPath, fileName, repositoryNameInXMLFile);
   }

   private List<String> startFinder(String url)
   {
      // Start Finder (crawling)
      Set<String> visitedURLs = Collections.synchronizedSet(new HashSet<String>());
      // How many threads
      ForkJoinPool forkJoinPool = new ForkJoinPool(4);
      Finder finder = new Finder(url, visitedURLs, proxy, new Files());

      List<String> updateSites = forkJoinPool.invoke(finder);
      return updateSites;
   }
}
