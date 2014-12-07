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
