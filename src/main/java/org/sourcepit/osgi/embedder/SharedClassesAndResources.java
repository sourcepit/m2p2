/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.osgi.embedder;

import static org.sourcepit.osgi.embedder.StartConfigurationUtil.getPrefixedProperty;

import java.util.ArrayList;
import java.util.List;

import org.sourcepit.common.utils.props.PropertiesSource;

public class SharedClassesAndResources
{
   private List<String> sharedClasses = new ArrayList<String>();

   private List<String> sharedResources = new ArrayList<String>();

   public List<String> getSharedClasses()
   {
      return sharedClasses;
   }

   public List<String> getSharedResource()
   {
      return sharedResources;
   }

   public static SharedClassesAndResources fromProperties(PropertiesSource properties, String prefix)
   {
      final SharedClassesAndResources cfg = new SharedClassesAndResources();

      final boolean sharedClassesToSharedResources = properties.getBoolean(
         getPrefixedProperty(prefix, "sharedClassesToSharedResources"), true);

      final String sharedClasses = properties.get(getPrefixedProperty(prefix, "sharedClasses"));
      if (sharedClasses != null)
      {
         for (String sharedClass : sharedClasses.split(","))
         {
            sharedClass = sharedClass.trim();
            if (!sharedClass.isEmpty())
            {
               cfg.getSharedClasses().add(sharedClass);
               if (sharedClassesToSharedResources)
               {
                  String sharedResource = sharedClass.replace('.', '/');
                  if (!sharedResource.endsWith("*"))
                  {
                     sharedResource = sharedResource + ".class";
                  }
                  cfg.getSharedResource().add(sharedResource);
               }
            }
         }
      }

      final String sharedResources = properties.get(getPrefixedProperty(prefix, "sharedResources"));
      if (sharedResources != null)
      {
         for (String sharedResource : sharedResources.split(","))
         {
            sharedResource = sharedResource.trim();
            if (!sharedResource.isEmpty())
            {
               cfg.getSharedResource().add(sharedResource);
            }
         }
      }

      return cfg;
   }
}
