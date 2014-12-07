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

package org.sourcepit.m2p2.osgi.embedder.maven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.equinox.internal.p2.director.Slicer;
import org.eclipse.equinox.internal.p2.metadata.InstallableUnit;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
import org.eclipse.equinox.p2.query.IQueryable;

public class MultiContextsSlicer extends Slicer
{
   final List<Map<String, String>> envs = new ArrayList<Map<String, String>>();

   protected final List<IInstallableUnit> selectionContexts;

   public MultiContextsSlicer(IQueryable<IInstallableUnit> possibilites, Collection<Map<String, String>> contexts,
      boolean considerMetaRequirements)
   {
      this(possibilites, toContextIUs(contexts), considerMetaRequirements);
   }

   private static List<IInstallableUnit> toContextIUs(Collection<Map<String, String>> contexts)
   {
      final List<IInstallableUnit> selectionContexts = new ArrayList<IInstallableUnit>(contexts.size());
      for (Map<String, String> map : contexts)
      {
         selectionContexts.add(InstallableUnit.contextIU(map));
      }
      return selectionContexts;
   }

   public MultiContextsSlicer(IQueryable<IInstallableUnit> possibilites, List<IInstallableUnit> contexts,
      boolean considerMetaRequirements)
   {
      super(possibilites, (IInstallableUnit) null, considerMetaRequirements);
      selectionContexts = contexts;
   }

   @Override
   protected boolean isApplicable(IRequirement req)
   {
      final IMatchExpression<IInstallableUnit> filter = req.getFilter();
      return filter == null || isMatch(filter, selectionContexts);
   }

   @Override
   protected boolean isApplicable(IInstallableUnit iu)
   {
      IMatchExpression<IInstallableUnit> filter = iu.getFilter();
      return filter == null || isMatch(filter, selectionContexts);
   }

   private static boolean isMatch(IMatchExpression<IInstallableUnit> filter, List<IInstallableUnit> selectionContexts)
   {
      for (IInstallableUnit selectionContext : selectionContexts)
      {
         if (filter.isMatch(selectionContext))
         {
            return true;
         }
      }
      return false;
   }

}
