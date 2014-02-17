/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder;

import java.util.Map;

public abstract class AbstractOSGiEmbedderLifecycleListener implements OSGiEmbedderLifecycleListener
{
   @Override
   public void frameworkPropertiesInitialized(OSGiEmbedder embeddedEquinox, Map<String, String> frameworkProperties)
   {
   }

   @Override
   public void frameworkClassLoaderCreated(OSGiEmbedder embeddedEquinox, ClassLoader frameworkClassLoader)
   {
   }

   @Override
   public void frameworkStarted(OSGiEmbedder embeddedEquinox)
   {
   }

   @Override
   public void bundlesInstalled(OSGiEmbedder embeddedEquinox)
   {
   }

   @Override
   public void bundlesStarted(OSGiEmbedder embeddedEquinox)
   {
   }

   @Override
   public void frameworkAboutToStop(OSGiEmbedder embeddedEquinox)
   {
   }

   @Override
   public void frameworkStopped(OSGiEmbedder embeddedEquinox)
   {
   }
}
