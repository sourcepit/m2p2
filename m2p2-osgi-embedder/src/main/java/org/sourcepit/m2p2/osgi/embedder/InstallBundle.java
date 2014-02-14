/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder;

import java.net.URL;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.sourcepit.common.utils.collections.Functor;

public class InstallBundle implements Functor<URL, BundleException>
{
   private final BundleContext bundleContext;

   public InstallBundle(BundleContext bundleContext)
   {
      this.bundleContext = bundleContext;
   }

   @Override
   public void apply(URL bundleURL) throws BundleException
   {
      bundleContext.installBundle(bundleURL.toString());
   }
}