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

import static org.sourcepit.common.utils.collections.CollectionUtils.foreach;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Constants;
import org.sourcepit.common.utils.collections.Functor;
import org.sourcepit.common.utils.collections.Iterable2;

public class ParentFirstClassLoadingStrategy implements ClassLoadingStrategy
{
   private final ClassLoader classLoader;

   private final SharedClassesAndResources configuration;

   public ParentFirstClassLoadingStrategy(ClassLoader classLoader, SharedClassesAndResources configuration)
   {
      this.classLoader = classLoader;
      this.configuration = configuration;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void adoptFrameworkProperties(Map<String, String> frameworkProperties)
   {
      frameworkProperties.put("osgi.parentClassloader", "fwk");
      // frameworkProerties.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
      frameworkProperties.put(Constants.FRAMEWORK_BOOTDELEGATION, "*");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <E extends Exception> ClassLoader newFrameworkClassLoader(Iterable2<URL, E> jars) throws E
   {
      final List<URL> frameworkJars = new ArrayList<URL>();
      final Functor<URL, E> functor = new Functor<URL, E>()
      {
         @Override
         public void apply(URL uri) throws E
         {
            frameworkJars.add(uri);
         }
      };

      foreach(jars, functor);

      return new BypassClassLoader(frameworkJars, classLoader, toString(configuration.getSharedClasses()),
         toString(configuration.getSharedResource()));
   }

   private static String toString(final Collection<String> namePatterns)
   {
      if (namePatterns.isEmpty())
      {
         return null;
      }
      else
      {
         final StringBuilder sb = new StringBuilder();
         for (String namePattern : namePatterns)
         {
            sb.append(namePattern);
            sb.append(',');
         }
         sb.deleteCharAt(sb.length() - 1);
         return sb.toString();
      }
   }

   @Override
   public void disposeFrameworkClassLoader(ClassLoader frameworkClassLoader)
   {
   }
}
