/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleRevision;
import org.sourcepit.common.utils.collections.Functor;

public class StartBundle implements Functor<Bundle, BundleException>
{
   private final BundleStartPolicyProvider bundleStartPolicyProvider;

   public StartBundle(BundleStartPolicyProvider bundleStartPolicyProvider)
   {
      this.bundleStartPolicyProvider = bundleStartPolicyProvider;
   }

   @Override
   public void apply(Bundle bundle) throws BundleException
   {
      if (!isFragment(bundle))
      {
         switch (bundleStartPolicyProvider.getStartPolicy(bundle))
         {
            case FORCE_START :
               bundle.start();
               break;
            case BY_ACTIVATION_POLICY :
               bundle.start(Bundle.START_ACTIVATION_POLICY);
               break;
            case DONT_START :
               break;
            default :
               throw new IllegalStateException();
         }
      }
   }

   private boolean isFragment(Bundle bundle)
   {
      return (bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0;
   }
}