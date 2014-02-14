/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
