/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
