/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.sourcepit.common.utils.collections.Functor;
import org.sourcepit.m2p2.StartLevelProvider;

public class ConfigureBundleStartLevel implements Functor<Bundle, RuntimeException>
{
   private final StartLevelProvider startLevelProvider;

   public ConfigureBundleStartLevel(StartLevelProvider startLevelProvider)
   {
      this.startLevelProvider = startLevelProvider;
   }

   @Override
   public void apply(Bundle bundle)
   {
      final int startLevel = startLevelProvider.getBundleStartLevel(bundle);
      bundle.adapt(BundleStartLevel.class).setStartLevel(startLevel);
   }
}