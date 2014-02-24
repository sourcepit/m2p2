/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
