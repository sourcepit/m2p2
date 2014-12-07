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

import static org.sourcepit.m2p2.osgi.embedder.BundleStartPolicy.BY_ACTIVATION_POLICY;
import static org.sourcepit.m2p2.osgi.embedder.BundleStartPolicy.DONT_START;
import static org.sourcepit.m2p2.osgi.embedder.BundleStartPolicy.FORCE_START;

import java.util.Map;

import org.osgi.framework.Bundle;

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
         return autoStart ? BY_ACTIVATION_POLICY : DONT_START;
      }
      return start.booleanValue() ? FORCE_START : DONT_START;
   }
}
