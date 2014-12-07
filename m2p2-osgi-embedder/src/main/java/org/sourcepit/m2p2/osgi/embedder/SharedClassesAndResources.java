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

package org.sourcepit.m2p2.osgi.embedder;

import static org.sourcepit.m2p2.osgi.embedder.StartConfigurationUtil.getPrefixedProperty;

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
