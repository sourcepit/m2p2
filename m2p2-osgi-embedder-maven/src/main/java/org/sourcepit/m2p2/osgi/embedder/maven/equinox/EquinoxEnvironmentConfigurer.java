/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder.maven.equinox;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.internal.adaptor.EclipseEnvironmentInfo;
import org.sourcepit.m2p2.osgi.embedder.AbstractOSGiEmbedderLifecycleListener;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;

public class EquinoxEnvironmentConfigurer extends AbstractOSGiEmbedderLifecycleListener
{
   @Override
   public void frameworkClassLoaderCreated(OSGiEmbedder embeddedEquinox, ClassLoader frameworkClassLoader)
   {
      final Collection<String> nonFrameworkArgs = new LinkedHashSet<String>();
      nonFrameworkArgs.add("-eclipse.keyring");
      final File secureStorage = new File(embeddedEquinox.getFrameworkLocation(), "secure_storage");
      try
      {
         secureStorage.createNewFile();
      }
      catch (IOException e)
      {
         throw pipe(e);
      }
      secureStorage.deleteOnExit();
      nonFrameworkArgs.add(secureStorage.getAbsolutePath());
      
      setNonFrameworkArgs(frameworkClassLoader, nonFrameworkArgs);
   }

   private static void setNonFrameworkArgs(ClassLoader frameworkClassLoader, final Collection<String> nonFrameworkArgs)
   {
      Class<?> clazz;
      try
      {
         clazz = frameworkClassLoader.loadClass(EclipseEnvironmentInfo.class.getName());
      }
      catch (ClassNotFoundException e)
      {
         throw pipe(e);
      }

      final Object appArgs = nonFrameworkArgs.toArray(new String[nonFrameworkArgs.size()]);
      try
      {
         clazz.getMethod("setAppArgs", String[].class).invoke(null, appArgs);
      }
      catch (IllegalAccessException e)
      {
         throw pipe(e);
      }
      catch (InvocationTargetException e)
      {
         throw pipe(e);
      }
      catch (NoSuchMethodException e)
      {
         throw pipe(e);
      }
   }
}
