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

package org.sourcepit.m2p2.director;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

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
