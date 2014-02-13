/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.equinox;

import static org.sourcepit.common.utils.collections.CollectionUtils.foreach;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.sourcepit.common.utils.collections.Functor;
import org.sourcepit.common.utils.collections.Iterable2;

public class ClassLoaderFactory
{
   public <E extends Exception> ClassLoader newFrameworkClassLoader(Iterable2<URL, E> jars,
      ClassLoaderConfiguration configuration, ClassLoader parent) throws E
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

      final List<String> classNamePatterns = configuration.getClassNamePatterns();

      final Collection<String> resourceNamePatterns = new LinkedHashSet<String>(configuration.getResourceNamePatterns());
      if (configuration.isAddClassNameToResourceNamePatterns())
      {
         for (String classNamePattern : classNamePatterns)
         {
            resourceNamePatterns.add(classNamePattern.replace('.', '/'));
         }
      }

      return new BypassClassLoader(frameworkJars, parent, toString(classNamePatterns), toString(resourceNamePatterns));
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
}
