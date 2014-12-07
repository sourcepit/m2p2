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

import org.sourcepit.common.utils.props.PropertiesSource;

public final class StartConfigurationUtil
{
   public static final String FRAMEWORK_START_LEVEL = "frameworkStartLevel";
   public static final String BUNDLE_AUTO_START = "bundleAutoStart";
   public static final String BUNDLE_DEFAULT_START_LEVEL = "bundleDefaultStartLevel";
   public static final String BUNDLE_START_CONFIGURATIONS = "bundleStartConfigurations";

   private StartConfigurationUtil()
   {
      super();
   }

   public static StartConfiguration fromProperties(PropertiesSource properties, String prefix)
   {
      final StartConfiguration startCfg = new StartConfiguration();

      final int frameworkStartLevel = properties.getInt(getPrefixedProperty(prefix, FRAMEWORK_START_LEVEL),
         startCfg.getFrameworkStartLevel());
      startCfg.setFrameworkStartLevel(frameworkStartLevel);

      final boolean bundleAutoStart = properties.getBoolean(getPrefixedProperty(prefix, BUNDLE_AUTO_START),
         startCfg.isBundleAutoStart());
      startCfg.setBundleAutoStart(bundleAutoStart);

      final int bundleDefaultStartLevel = properties.getInt(getPrefixedProperty(prefix, BUNDLE_DEFAULT_START_LEVEL),
         startCfg.getBundleDefaultStartLevel());
      startCfg.setBundleDefaultStartLevel(bundleDefaultStartLevel);

      final String bundleStartConfigurations = properties.get(getPrefixedProperty(prefix, BUNDLE_START_CONFIGURATIONS));
      if (bundleStartConfigurations != null)
      {
         final Map<String, Integer> symbolicNameToStartLevelMap = startCfg.getBundleSymbolicNameToStartLevel();
         final Map<String, Boolean> symbolicNameToStartMap = startCfg.getBundleSymbolicNameToStart();
         for (String bundleStartConfiguration : bundleStartConfigurations.split(","))
         {
            final String bundleStartCfg = bundleStartConfiguration.trim();
            if (!bundleStartCfg.isEmpty())
            {
               final String[] split = bundleStartCfg.split(":");
               final String symbolicName = split[0];
               final Integer startLevel = Integer.valueOf(split[1].trim());
               final Boolean start = Boolean.valueOf(split[2].trim());
               symbolicNameToStartLevelMap.put(symbolicName, startLevel);
               symbolicNameToStartMap.put(symbolicName, start);
            }
         }
      }

      return startCfg;
   }

   static String getPrefixedProperty(String prefix, String property)
   {
      if (prefix == null || prefix.isEmpty())
      {
         return property;
      }
      return prefix + "." + property;
   }

   public static StartLevelProvider toStartLevelProvider(StartConfiguration startCfg)
   {
      return new DefaultStartLevelProvider(startCfg.getFrameworkStartLevel(), startCfg.getBundleDefaultStartLevel(),
         startCfg.getBundleSymbolicNameToStartLevel());
   }

   public static BundleStartPolicyProvider toBundleStartPolicyProvider(StartConfiguration startCfg)
   {
      return new DefaultBundleStartPolicyProvider(startCfg.isBundleAutoStart(), startCfg.getBundleSymbolicNameToStart());
   }
}
