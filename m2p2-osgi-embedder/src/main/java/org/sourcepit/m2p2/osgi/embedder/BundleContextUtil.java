/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public final class BundleContextUtil
{
   private BundleContextUtil()
   {
      super();
   }

   public static Bundle getBundle(BundleContext bundleContext, String symbolicName)
   {
      for (Bundle bundle : bundleContext.getBundles())
      {
         if (symbolicName.equals(bundle.getSymbolicName()))
         {
            return bundle;
         }
      }
      return null;
   }

   public static <S> S getService(BundleContext context, Class<S> serviceType)
   {
      return context.getService(context.getServiceReference(serviceType));
   }
}
