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

import static org.sourcepit.common.utils.lang.Exceptions.pipe;
import static org.sourcepit.m2p2.osgi.embedder.BundleContextUtil.getBundle;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.osgi.framework.BundleContext;

public final class SecurePreferencesUtil
{
   private SecurePreferencesUtil()
   {
      super();
   }

   public static ISecurePreferences getSecurePreferences(final BundleContext bundleContext)
   {
      try
      {
         final Class<?> clazz = getBundle(bundleContext, "org.eclipse.equinox.security").loadClass(
            SecurePreferencesFactory.class.getName());
         return (ISecurePreferences) clazz.getMethod("getDefault").invoke(null);
      }
      catch (ClassNotFoundException e)
      {
         throw pipe(e);
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
