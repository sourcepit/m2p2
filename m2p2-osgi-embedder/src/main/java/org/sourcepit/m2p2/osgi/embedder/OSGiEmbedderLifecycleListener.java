/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder;

import java.util.Map;

public interface OSGiEmbedderLifecycleListener
{
   void frameworkPropertiesInitialized(OSGiEmbedder embeddedEquinox, Map<String, String> frameworkProperties);

   void frameworkClassLoaderCreated(OSGiEmbedder embeddedEquinox, ClassLoader frameworkClassLoader);

   void frameworkStarted(OSGiEmbedder embeddedEquinox);

   void bundlesInstalled(OSGiEmbedder embeddedEquinox);

   void bundlesStarted(OSGiEmbedder embeddedEquinox);

   void frameworkAboutToStop(OSGiEmbedder embeddedEquinox);

   void frameworkStopped(OSGiEmbedder embeddedEquinox);
}
