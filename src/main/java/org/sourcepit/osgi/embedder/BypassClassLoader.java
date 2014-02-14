/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.osgi.embedder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;

import org.sourcepit.common.utils.path.PathMatcher;

public class BypassClassLoader extends URLClassLoader
{
   private final ClassLoader foreignClassLoader;
   private final PathMatcher classNamePatterns;
   private PathMatcher resourceNamePatterns;

   public BypassClassLoader(List<URL> urls, ClassLoader foreignClassLoader, String classNamePatterns,
      String resourceNamePatterns)
   {
      this(urls.toArray(new URL[urls.size()]), foreignClassLoader, classNamePatterns, resourceNamePatterns, null);
   }

   public BypassClassLoader(URL[] urls, ClassLoader foreignClassLoader, String classNamePatterns,
      String resourceNamePatterns)
   {
      this(urls, foreignClassLoader, classNamePatterns, resourceNamePatterns, null);
   }

   public BypassClassLoader(URL[] urls, ClassLoader foreignClassLoader, String classNamePatterns,
      String resourceNamePatterns, ClassLoader parent)
   {
      super(urls, parent);
      this.foreignClassLoader = foreignClassLoader;
      this.classNamePatterns = classNamePatterns == null ? null : PathMatcher.parsePackagePatterns(classNamePatterns);
      this.resourceNamePatterns = resourceNamePatterns == null ? null : PathMatcher.parse(resourceNamePatterns, "/", ",");
   }

   @Override
   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      if (classNamePatterns != null && classNamePatterns.isMatch(name))
      {
         return foreignClassLoader.loadClass(name);
      }
      return super.loadClass(name);
   }

   @Override
   public URL getResource(String name)
   {
      if (resourceNamePatterns != null && resourceNamePatterns.isMatch(name))
      {
         return foreignClassLoader.getResource(name);
      }
      return super.getResource(name);
   }

   @Override
   public InputStream getResourceAsStream(String name)
   {
      if (resourceNamePatterns != null && resourceNamePatterns.isMatch(name))
      {
         return foreignClassLoader.getResourceAsStream(name);
      }
      return super.getResourceAsStream(name);
   }

   @Override
   public Enumeration<URL> getResources(String name) throws IOException
   {
      if (resourceNamePatterns != null && resourceNamePatterns.isMatch(name))
      {
         return foreignClassLoader.getResources(name);
      }
      return super.getResources(name);
   }
}