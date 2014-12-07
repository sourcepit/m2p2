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