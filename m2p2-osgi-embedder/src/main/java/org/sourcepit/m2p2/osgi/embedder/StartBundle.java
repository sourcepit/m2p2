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