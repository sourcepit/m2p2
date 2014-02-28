/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.director;

import java.util.Iterator;
import java.util.List;

import org.sourcepit.common.utils.path.PathMatcher;

public final class ArgumentModificationsUtil
{
   private ArgumentModificationsUtil()
   {
      super();
   }

   public static boolean isMatch(List<String> patterns, String value)
   {
      for (String pattern : patterns)
      {
         if (isMatch(pattern, value))
         {
            return true;
         }
      }
      return false;
   }

   public static boolean isMatch(String pattern, String value)
   {
      if (pattern.contains("*"))
      {
         final StringBuilder regex = new StringBuilder();
         for (String string : pattern.split("\\*"))
         {
            regex.append(PathMatcher.escRegEx(string));
            regex.append(".*");
         }
         return value.matches(regex.toString());
      }
      return pattern.equals(value);
   }

   public static void removeArgs(List<String> args, final List<String> argPatterns)
   {
      int idx = 0;
      for (Iterator<String> it = args.iterator(); it.hasNext();)
      {
         final String arg = (String) it.next();
         if (isMatch(argPatterns, arg))
         {
            it.remove();
            idx--;

            if (arg.startsWith("-"))
            {
               while (removeValue(args, idx + 1, it))
               {
               }
            }

         }
         idx++;
      }
   }

   private static boolean removeValue(List<String> args, int idx, Iterator<String> it)
   {
      if (args.size() > idx)
      {
         final String nextArg = args.get(idx);
         if (!nextArg.startsWith("-"))
         {
            it.next();
            it.remove();
            return true;
         }
      }
      return false;
   }

   public static void applyArgModifications(List<String> args, ArgumentModifications modifications)
   {
      if (modifications.getArgs() != null)
      {
         args.clear();
         args.addAll(modifications.getArgs());
         return;
      }

      final List<String> remove = modifications.getRemove();
      if (remove != null)
      {
         removeArgs(args, remove);
      }

      final List<String> prepend = modifications.getPrepend();
      if (prepend != null)
      {
         int idx = 0;
         for (String arg : prepend)
         {
            args.add(idx, arg);
            idx++;
         }
      }

      final List<String> append = modifications.getAppend();
      if (append != null)
      {
         args.addAll(append);
      }
   }
}
