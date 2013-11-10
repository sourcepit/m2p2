/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.util;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Integer.valueOf;
import static org.junit.Assert.*;
import static org.sourcepit.m2p2.util.StartConfigurationUtil.BUNDLE_AUTO_START;
import static org.sourcepit.m2p2.util.StartConfigurationUtil.BUNDLE_DEFAULT_START_LEVEL;
import static org.sourcepit.m2p2.util.StartConfigurationUtil.BUNDLE_START_CONFIGURATIONS;
import static org.sourcepit.m2p2.util.StartConfigurationUtil.FRAMEWORK_START_LEVEL;

import java.util.Map;

import org.junit.Test;
import org.sourcepit.common.utils.props.LinkedPropertiesMap;
import org.sourcepit.common.utils.props.PropertiesMap;
import org.sourcepit.m2p2.util.StartConfiguration;
import org.sourcepit.m2p2.util.StartConfigurationUtil;

public class StartConfigurationUtilTest
{
   @Test
   public void testStartConfigurationDefaults()
   {
      PropertiesMap properties = new LinkedPropertiesMap();

      StartConfiguration defaultStartCfg = new StartConfiguration();

      StartConfiguration startCfg = StartConfigurationUtil.fromProperties(properties, null);

      assertEquals(defaultStartCfg.getFrameworkStartLevel(), startCfg.getFrameworkStartLevel());
      assertEquals(defaultStartCfg.getBundleDefaultStartLevel(), startCfg.getBundleDefaultStartLevel());
      assertEquals(defaultStartCfg.isBundleAutoStart(), startCfg.isBundleAutoStart());
      assertEquals(defaultStartCfg.getBundleSymbolicNameToStart(), startCfg.getBundleSymbolicNameToStart());
      assertEquals(defaultStartCfg.getBundleSymbolicNameToStartLevel(), startCfg.getBundleSymbolicNameToStartLevel());

   }

   @Test
   public void testFromProperties() throws Exception
   {
      PropertiesMap properties = new LinkedPropertiesMap();
      properties.setInt(FRAMEWORK_START_LEVEL, 7000);
      properties.setInt(BUNDLE_DEFAULT_START_LEVEL, 6000);
      properties.setBoolean(BUNDLE_AUTO_START, false);

      StringBuilder sb = new StringBuilder();
      sb.append("  foo:2:false,\n");
      sb.append("bar:999:true ,\n\n");
      properties.put(BUNDLE_START_CONFIGURATIONS, sb.toString());

      StartConfiguration startCfg = StartConfigurationUtil.fromProperties(properties, null);
      assertEquals(7000, startCfg.getFrameworkStartLevel());
      assertEquals(6000, startCfg.getBundleDefaultStartLevel());
      assertEquals(false, startCfg.isBundleAutoStart());

      Map<String, Boolean> bundleIdToStart = startCfg.getBundleSymbolicNameToStart();
      assertEquals(2, bundleIdToStart.size());
      assertEquals(FALSE, bundleIdToStart.get("foo"));
      assertEquals(TRUE, bundleIdToStart.get("bar"));

      Map<String, Integer> bundleIdToStartLevel = startCfg.getBundleSymbolicNameToStartLevel();
      assertEquals(2, bundleIdToStartLevel.size());
      assertEquals(valueOf(2), bundleIdToStartLevel.get("foo"));
      assertEquals(valueOf(999), bundleIdToStartLevel.get("bar"));
   }

   @Test
   public void testFromPropertiesWithPropertyPrefix() throws Exception
   {
      String prefix = "mööp";

      PropertiesMap properties = new LinkedPropertiesMap();
      properties.setInt(prefix + "." + FRAMEWORK_START_LEVEL, 7000);
      properties.setInt(prefix + "." + BUNDLE_DEFAULT_START_LEVEL, 6000);
      properties.setBoolean(prefix + "." + BUNDLE_AUTO_START, false);

      StringBuilder sb = new StringBuilder();
      sb.append("  foo:2:false,\n");
      sb.append("bar:999:true ,\n\n");
      properties.put(prefix + "." + BUNDLE_START_CONFIGURATIONS, sb.toString());

      StartConfiguration startCfg = StartConfigurationUtil.fromProperties(properties, prefix);
      assertEquals(7000, startCfg.getFrameworkStartLevel());
      assertEquals(6000, startCfg.getBundleDefaultStartLevel());
      assertEquals(false, startCfg.isBundleAutoStart());

      Map<String, Boolean> bundleIdToStart = startCfg.getBundleSymbolicNameToStart();
      assertEquals(2, bundleIdToStart.size());
      assertEquals(FALSE, bundleIdToStart.get("foo"));
      assertEquals(TRUE, bundleIdToStart.get("bar"));

      Map<String, Integer> bundleIdToStartLevel = startCfg.getBundleSymbolicNameToStartLevel();
      assertEquals(2, bundleIdToStartLevel.size());
      assertEquals(valueOf(2), bundleIdToStartLevel.get("foo"));
      assertEquals(valueOf(999), bundleIdToStartLevel.get("bar"));
   }
}
