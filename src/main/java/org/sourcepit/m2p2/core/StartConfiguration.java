/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.core;

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
