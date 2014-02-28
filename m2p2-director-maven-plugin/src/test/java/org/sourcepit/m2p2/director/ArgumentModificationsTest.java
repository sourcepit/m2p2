/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.director;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.sourcepit.common.utils.io.IO;

public class ArgumentModificationsTest
{
   @Test
   public void testApply()
   {
      ArgumentModifications argMods = new ArgumentModifications();

      List<String> args = new ArrayList<String>();
      args.add("-foo");
      args.add("bar");

      argMods.setArgs(Collections.<String> emptyList());
      argMods.apply(args);
      assertEquals(0, args.size());

      args = new ArrayList<String>();
      args.add("-foo");
      args.add("bar");

      argMods.setArgs(asList("hans", "wurst"));
      argMods.apply(args);
      assertEquals("[hans, wurst]", args.toString());

      argMods.setArgs(null);

      argMods.setRemove(asList("-foo"));
      argMods.setPrepend(asList("-foo", "foo"));
      argMods.setAppend(asList("hans", "wurst"));

      args = new ArrayList<String>();
      args.add("-foo");
      args.add("bar");
      args.add("-bar");
      args.add("foo");

      argMods.apply(args);
      assertEquals("[-foo, foo, -bar, foo, hans, wurst]", args.toString());
   }
}
