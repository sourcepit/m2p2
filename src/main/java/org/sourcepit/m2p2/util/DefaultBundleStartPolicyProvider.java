/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.util;

import static org.sourcepit.m2p2.BundleStartPolicy.BY_ACTIVATION_POLICY;
import static org.sourcepit.m2p2.BundleStartPolicy.DONT_START;
import static org.sourcepit.m2p2.BundleStartPolicy.FORCE_START;

import java.util.Map;

import org.osgi.framework.Bundle;
import org.sourcepit.m2p2.BundleStartPolicy;
import org.sourcepit.m2p2.BundleStartPolicyProvider;

public class DefaultBundleStartPolicyProvider implements BundleStartPolicyProvider
{
   private final boolean autoStart;

   private final Map<String, Boolean> symbolicNameToStartMap;

   public DefaultBundleStartPolicyProvider(boolean autoStart, Map<String, Boolean> symbolicNameToStartMap)
   {
      this.autoStart = autoStart;
      this.symbolicNameToStartMap = symbolicNameToStartMap;
   }

   @Override
   public BundleStartPolicy getStartPolicy(Bundle bundle)
   {
      final Boolean start = symbolicNameToStartMap.get(bundle.getSymbolicName());
      if (start == null)
      {
         return autoStart ? BY_ACTIVATION_POLICY :DONT_START;
      }
      return start.booleanValue() ? FORCE_START : DONT_START;
   }
}
