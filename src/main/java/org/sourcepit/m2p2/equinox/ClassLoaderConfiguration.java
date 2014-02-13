/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.equinox;

import java.util.ArrayList;
import java.util.List;

public class ClassLoaderConfiguration
{
   private List<String> classNamePatterns = new ArrayList<String>();

   private boolean addClassNameToResourceNamePatterns = true;

   private List<String> resourceNamePatterns = new ArrayList<String>();

   public List<String> getClassNamePatterns()
   {
      return classNamePatterns;
   }

   public boolean isAddClassNameToResourceNamePatterns()
   {
      return addClassNameToResourceNamePatterns;
   }

   public void setAddClassNameToResourceNamePatterns(boolean addClassNameToResourceNamePatterns)
   {
      this.addClassNameToResourceNamePatterns = addClassNameToResourceNamePatterns;
   }

   public List<String> getResourceNamePatterns()
   {
      return resourceNamePatterns;
   }

}
