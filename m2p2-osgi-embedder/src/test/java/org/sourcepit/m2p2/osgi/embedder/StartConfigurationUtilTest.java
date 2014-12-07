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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;
import static org.sourcepit.m2p2.osgi.embedder.StartConfigurationUtil.BUNDLE_AUTO_START;
import static org.sourcepit.m2p2.osgi.embedder.StartConfigurationUtil.BUNDLE_DEFAULT_START_LEVEL;
import static org.sourcepit.m2p2.osgi.embedder.StartConfigurationUtil.BUNDLE_START_CONFIGURATIONS;
import static org.sourcepit.m2p2.osgi.embedder.StartConfigurationUtil.FRAMEWORK_START_LEVEL;

import java.util.Map;

import org.junit.Test;
import org.sourcepit.common.utils.props.LinkedPropertiesMap;
import org.sourcepit.common.utils.props.PropertiesMap;
import org.sourcepit.m2p2.osgi.embedder.StartConfiguration;
import org.sourcepit.m2p2.osgi.embedder.StartConfigurationUtil;

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
