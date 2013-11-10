/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.core;

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

   private static String getPrefixedProperty(String prefix, String property)
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
