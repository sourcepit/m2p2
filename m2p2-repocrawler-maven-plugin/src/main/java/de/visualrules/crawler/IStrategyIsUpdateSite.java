/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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