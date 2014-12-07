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

package org.sourcepit.m2p2.osgi.embedder.maven.equinox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.framework.BundleContext;
import org.sourcepit.m2p2.osgi.embedder.BundleContextUtil;

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

   public static EclipseEnvironmentInfo newEclipseEnvironmentInfo(BundleContext bundleContext)
   {
      final EnvironmentInfo envInfo = BundleContextUtil.getService(bundleContext, EnvironmentInfo.class);

      final Class<?> clazz = envInfo.getClass();

      if (!EquinoxConfiguration.class.getName().equals(clazz.getName()))
      {
         throw new IllegalStateException("Unexpected impl for EnvironmentInfo.");
      }
      
      if (EquinoxConfiguration.class == clazz)
      {
         throw new IllegalStateException("Class EquinoxConfiguration leaked into container.");
      }

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
      invoke(this.setAllArgs, envInfo, (Object) allArgs);
   }

   public void setAppArgs(String[] appArgs)
   {
      invoke(this.setAppArgs, envInfo, (Object) appArgs);
   }

   public void setFrameworkArgs(String[] frameworkArgs)
   {
      invoke(this.setFrameworkArgs, envInfo, (Object) frameworkArgs);
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
