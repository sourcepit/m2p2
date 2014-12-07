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

import java.util.HashMap;
import java.util.Map;

public class StartConfiguration
{
   private int frameworkStartLevel = 6;

   private int bundleDefaultStartLevel = 4;
   private Map<String, Integer> bundleSymbolicNameToStartLevel = new HashMap<String, Integer>();

   private boolean bundleAutoStart = true;
   private Map<String, Boolean> bundleSymbolicNameToStart = new HashMap<String, Boolean>();

   public int getFrameworkStartLevel()
   {
      return frameworkStartLevel;
   }

   public void setFrameworkStartLevel(int frameworkStartLevel)
   {
      this.frameworkStartLevel = frameworkStartLevel;
   }

   public void setBundleAutoStart(boolean bundleAutoStart)
   {
      this.bundleAutoStart = bundleAutoStart;
   }

   public int getBundleDefaultStartLevel()
   {
      return bundleDefaultStartLevel;
   }

   public void setBundleDefaultStartLevel(int bundleDefaultStartLevel)
   {
      this.bundleDefaultStartLevel = bundleDefaultStartLevel;
   }

   public Map<String, Integer> getBundleSymbolicNameToStartLevel()
   {
      return bundleSymbolicNameToStartLevel;
   }

   public boolean isBundleAutoStart()
   {
      return bundleAutoStart;
   }

   public Map<String, Boolean> getBundleSymbolicNameToStart()
   {
      return bundleSymbolicNameToStart;
   }
}
