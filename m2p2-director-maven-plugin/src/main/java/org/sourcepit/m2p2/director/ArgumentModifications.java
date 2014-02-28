/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
