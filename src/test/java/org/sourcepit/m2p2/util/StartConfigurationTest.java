/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sourcepit.m2p2.util.StartConfiguration;

public class StartConfigurationTest
{
   @Test
   public void testDefaults()
   {
      StartConfiguration startCfg = new StartConfiguration();
      assertEquals(6, startCfg.getFrameworkStartLevel());
      assertEquals(4, startCfg.getBundleDefaultStartLevel());
      assertEquals(true, startCfg.isBundleAutoStart());
      assertNotNull(startCfg.getBundleSymbolicNameToStart());
      assertNotNull(startCfg.getBundleSymbolicNameToStartLevel());
   }

}
