/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package de.visualrules.crawler;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


/**
 * Finds Eclipse update sites. If a link has got a content.xml, content.jar, compositeContent.xml, compositeContent.jar,
 * site.xml it is an Eclipse update site and it will be saved in the <code>updateSites</code> <code>ArrayList</code> of
 * the method <code>compute</code>.
 * 
 * @author KLK1IMB
 */
public class Finder extends RecursiveTask<List<String>>
{
   /**
    * Comment for <code>serialVersionUID</code>
    */
   private static final long serialVersionUID = 1L;
   private final String url;
   private Set<String> visitedURLs;
   private Document doc;
   private Proxy proxy;
   private IStrategyIsUpdateSite strategy;

   /**
    * Constructor without proxy settings
    * 
    * @param url
    * @param visitedURLs
    * @param strategy Defines whether it should search after files or html sites. See <code>StrategyIsUpdateSite</code>
    */
   public Finder(String url, Set<String> visitedURLs, IStrategyIsUpdateSite strategy)
   {
      this(url, visitedURLs, null, strategy);
   }

   /**
    * Constructor with proxy settings
    * 
    * @param url
    * @param visitedURLs
    * @param strategy Defines whether it should search after files or html sites. See <code>StrategyIsUpdateSite</code>
    */
   public Finder(String url, Set<String> visitedURLs, Proxy proxy, IStrategyIsUpdateSite strategy)
   {
      this.url = url;
      this.visitedURLs = visitedURLs;
      this.proxy = proxy;
      this.strategy = strategy;
   }

   @Override
   protected List<String> compute()
   {
      List<String> updateSites = new ArrayList<String>();
      List<Finder> subTasks;

      if (shouldCompute())
      {
         this.doc = DocumentUtil.configureDocument(url, proxy, new CreateConnection(this.proxy));
         if (this.doc != null)
         {
            Elements resultLinks = doc.select("a");

            subTasks = new ArrayList<Finder>();
            if (!this.strategy.isUpdateSite(resultLinks))
            {
               Set<String> urls = new LinkedHashSet<String>();
               DocumentUtil.collectHierachicalChildURLs(resultLinks, urls);

               for (String url : urls)
               {
                  Finder subTask;
                  subTask = new Finder(url, this.visitedURLs, this.proxy, strategy);
                  subTasks.add(subTask);
                  subTask.fork();
               }
            }
            else
            {
               // System.out.println("---added link: " + url);
               updateSites.add(url);
            }

            for (Finder subTask : subTasks)
            {
               updateSites.addAll(subTask.join());
            }
         }
         return updateSites;
      }
      else
      {
         System.out.println("Already visited");
         return Collections.emptyList();
      }

   }

   private boolean shouldCompute()
   {
      synchronized (visitedURLs)
      {
         if (!visitedURLs.contains(url))
         {
            visitedURLs.add(url);
            return true;
         }
      }
      return false;
   }

   // private boolean isUpdateSite(Elements resultLinks)
   // {
   // List<String> neededFiles = new ArrayList<String>();
   // neededFiles.add("content.jar");
   // neededFiles.add("content.xml");
   // neededFiles.add("compositeContent.jar");
   // neededFiles.add("compositeContent.xml");
   // neededFiles.add("site.xml");
   //
   // List<String> unmodifiableFiles = Collections.unmodifiableList(neededFiles);
   //
   // for (Element element : resultLinks)
   // {
   // for (String neededFile : unmodifiableFiles)
   // {
   // if (neededFile.equals(element.attr("href")))
   // {
   // return true;
   // }
   // }
   // }
   // return false;
   // }

}
