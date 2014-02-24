/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder.maven.equinox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.osgi.service.environment.EnvironmentInfo;

public class EclipseEnvironmentInfo implements EnvironmentInfo
{
   private final EnvironmentInfo envInfo;
   private final Method setAllArgs;
   private final Method setAppArgs;
   private final Method setFrameworkArgs;

   private EclipseEnvironmentInfo(EnvironmentInfo envInfo, Method setAllArgs, Method setAppArgs, Method setFrameworkArgs)
   {
      this.envInfo = envInfo;
      this.setAllArgs = setAllArgs;
      this.setAppArgs = setAppArgs;
      this.setFrameworkArgs = setFrameworkArgs;
   }

   public static EclipseEnvironmentInfo newEclipseEnvironmentInfo(ClassLoader frameworkClassLoader)
   {
      final Class<?> clazz = loadEclipseEnvironmentInfo(frameworkClassLoader);
      final EnvironmentInfo envInfo = (EnvironmentInfo) invoke(getMethod(clazz, "getDefault"), null);
      final Method setAllArgs = getMethod(clazz, "setAllArgs", String[].class);
      final Method setAppArgs = getMethod(clazz, "setAppArgs", String[].class);
      final Method setFrameworkArgs = getMethod(clazz, "setFrameworkArgs", String[].class);
      return new EclipseEnvironmentInfo(envInfo, setAllArgs, setAppArgs, setFrameworkArgs);
   }

   private static Object invoke(Method method, Object obj, Object... args)
   {
      try
      {
         return method.invoke(obj, args);
      }
      catch (IllegalAccessException e)
      {
         throw new IllegalStateException(e);
      }
      catch (InvocationTargetException e)
      {
         final Throwable t = e.getTargetException();
         if (t instanceof RuntimeException)
         {
            throw (RuntimeException) t;
         }
         if (t instanceof Error)
         {
            throw (Error) t;
         }
         throw new IllegalStateException(t);
      }
   }

   private static Class<?> loadEclipseEnvironmentInfo(ClassLoader frameworkClassLoader)
   {
      final Class<?> clazz;
      try
      {
         clazz = frameworkClassLoader.loadClass(org.eclipse.core.runtime.internal.adaptor.EclipseEnvironmentInfo.class
            .getName());
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalStateException(e);
      }
      try
      {
         final Class<?> badClazz = EclipseEnvironmentInfo.class.getClassLoader().loadClass(
            org.eclipse.core.runtime.internal.adaptor.EclipseEnvironmentInfo.class.getName());
         if (badClazz == clazz)
         {
            throw new IllegalStateException();
         }
      }
      catch (ClassNotFoundException e)
      {
      }
      return clazz;
   }

   private static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes)
   {
      try
      {
         return clazz.getMethod(name, parameterTypes);
      }
      catch (NoSuchMethodException e)
      {
         throw new IllegalStateException(e);
      }
   }

   public void setAllArgs(String[] allArgs)
   {
      invoke(this.setAllArgs, null, (Object) allArgs);
   }

   public void setAppArgs(String[] appArgs)
   {
      invoke(this.setAppArgs, null, (Object) appArgs);
   }

   public void setFrameworkArgs(String[] frameworkArgs)
   {
      invoke(this.setFrameworkArgs, null, (Object) frameworkArgs);
   }

   public String[] getCommandLineArgs()
   {
      return envInfo.getCommandLineArgs();
   }

   public String[] getFrameworkArgs()
   {
      return envInfo.getFrameworkArgs();
   }

   public String[] getNonFrameworkArgs()
   {
      return envInfo.getNonFrameworkArgs();
   }

   public String getOSArch()
   {
      return envInfo.getOSArch();
   }

   public String getNL()
   {
      return envInfo.getNL();
   }

   public String getOS()
   {
      return envInfo.getOS();
   }

   public String getWS()
   {
      return envInfo.getWS();
   }

   public boolean inDebugMode()
   {
      return envInfo.inDebugMode();
   }

   public boolean inDevelopmentMode()
   {
      return envInfo.inDevelopmentMode();
   }

   public String getProperty(String key)
   {
      return envInfo.getProperty(key);
   }

   public String setProperty(String key, String value)
   {
      return envInfo.setProperty(key, value);
   }
}
