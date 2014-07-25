/*
 * Copyright (C) 2014 Bosch Software Innovations GmbH. All rights reserved.
 */

package de.visualrules.crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public interface IStrategyIsUpdateSite
{
   boolean isUpdateSite(Elements resultLinks);
}

class Files implements IStrategyIsUpdateSite
{
   @Override
   public boolean isUpdateSite(Elements resultLinks)
   {
      List<String> neededFiles = new ArrayList<String>();
      neededFiles.add("content.jar");
      neededFiles.add("content.xml");
      neededFiles.add("compositeContent.jar");
      neededFiles.add("compositeContent.xml");
      neededFiles.add("site.xml");

      List<String> unmodifiableFiles = Collections.unmodifiableList(neededFiles);

      for (Element element : resultLinks)
      {
         for (String neededFile : unmodifiableFiles)
         {
            if (neededFile.equals(element.attr("href")))
            {
               return true;
            }
         }
      }
      return false;
   }
   
}