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