/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.util;

import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.launch.Framework;
import org.sourcepit.m2p2.BundleStartLevelProvider;
import org.sourcepit.m2p2.StartLevelProvider;

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
