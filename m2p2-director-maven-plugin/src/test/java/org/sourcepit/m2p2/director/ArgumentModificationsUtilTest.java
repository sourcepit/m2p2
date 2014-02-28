/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.director;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ArgumentModificationsUtilTest
{

   @Test
   public void testIsMatch()
   {
      assertTrue(ArgumentModificationsUtil.isMatch("-Xmx3g", "-Xmx3g"));
      assertTrue(ArgumentModificationsUtil.isMatch("-Xmx*", "-Xmx3g"));
      assertTrue(ArgumentModificationsUtil.isMatch("-Xmx.*", "-Xmx.3g"));
      assertFalse(ArgumentModificationsUtil.isMatch("-Xmx.*", "-XmX.3g"));
      assertFalse(ArgumentModificationsUtil.isMatch("-Xmx3g", "-Xmx*"));
   }

   @Test
   public void testRemoveArgs()
   {
      List<String> args;
      List<String> patterns;

      args = new ArrayList<String>();
      args.add("-Xmx3g");
      patterns = new ArrayList<String>();
      patterns.add("-Xmx*");
      ArgumentModificationsUtil.removeArgs(args, patterns);
      assertEquals(0, args.size());

      args = new ArrayList<String>();
      args.add("-foo");
      args.add("bar");
      args.add("murks");
      args.add("-hui");
      patterns = new ArrayList<String>();
      patterns.add("-foo");
      ArgumentModificationsUtil.removeArgs(args, patterns);
      assertEquals("[-hui]", args.toString());

      args = new ArrayList<String>();
      args.add("-arg1");
      args.add("bar");
      args.add("murks");
      args.add("-arg2");
      args.add("-arg3");
      args.add("blub");
      args.add("-arg4");
      args.add("-arg5");
      args.add("blubblub");
      patterns = new ArrayList<String>();
      patterns.add("-arg1");
      patterns.add("-arg3");
      patterns.add("-arg4");
      ArgumentModificationsUtil.removeArgs(args, patterns);
      assertEquals("[-arg2, -arg5, blubblub]", args.toString());
   }
}
