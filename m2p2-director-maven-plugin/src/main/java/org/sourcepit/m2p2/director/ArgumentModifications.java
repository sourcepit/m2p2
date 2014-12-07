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

import static org.sourcepit.m2p2.director.ArgumentModificationsUtil.applyArgModifications;

import java.util.ArrayList;
import java.util.List;

public class ArgumentModifications
{
   private List<String> prepend;
   private List<String> args;
   private List<String> append;
   private List<String> remove;

   public List<String> getPrepend()
   {
      if (prepend == null)
      {
         prepend = new ArrayList<String>();
      }
      return prepend;
   }

   public void setPrepend(List<String> prepend)
   {
      this.prepend = prepend;
   }

   public List<String> getArgs()
   {
      return args;
   }

   public void setArgs(List<String> args)
   {
      this.args = args;
   }

   public List<String> getAppend()
   {
      return append;
   }

   public void setAppend(List<String> append)
   {
      this.append = append;
   }

   public List<String> getRemove()
   {
      return remove;
   }

   public void setRemove(List<String> remove)
   {
      this.remove = remove;
   }

   public void apply(List<String> args)
   {
      applyArgModifications(args, this);
   }
}
