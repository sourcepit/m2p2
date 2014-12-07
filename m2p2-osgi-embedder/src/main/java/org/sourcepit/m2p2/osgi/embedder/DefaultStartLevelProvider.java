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

import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.launch.Framework;

public class DefaultStartLevelProvider implements StartLevelProvider, BundleStartLevelProvider
{
   private final int frameworkStartLevel;

   private final int bundleDefaultStartLevel;

   private final Map<String, Integer> symbolicNameToStartLevelMap;

   public DefaultStartLevelProvider(int frameworkStartLevel, int bundleDefaultStartLevel,
      Map<String, Integer> symbolicNameToStartLevelMap)
   {
      this.frameworkStartLevel = frameworkStartLevel;
      this.bundleDefaultStartLevel = bundleDefaultStartLevel;
      this.symbolicNameToStartLevelMap = symbolicNameToStartLevelMap;
   }

   @Override
   public int getFrameworkStartLevel(Framework framework)
   {
      return frameworkStartLevel;
   }

   @Override
   public int getBundleStartLevel(Bundle bundle)
   {
      final Integer startLevel = symbolicNameToStartLevelMap.get(bundle.getSymbolicName());
      if (startLevel != null)
      {
         return startLevel.intValue();
      }
      return bundleDefaultStartLevel;
   }

}
