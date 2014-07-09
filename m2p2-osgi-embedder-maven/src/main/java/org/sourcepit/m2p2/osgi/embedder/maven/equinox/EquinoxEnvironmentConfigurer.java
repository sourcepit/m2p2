/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder.maven.equinox;

import static org.sourcepit.common.utils.lang.Exceptions.pipe;
import static org.sourcepit.m2p2.osgi.embedder.maven.equinox.EclipseEnvironmentInfo.newEclipseEnvironmentInfo;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.sourcepit.m2p2.osgi.embedder.AbstractOSGiEmbedderLifecycleListener;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;

public class EquinoxEnvironmentConfigurer extends AbstractOSGiEmbedderLifecycleListener
{
   @Override
   public void frameworkInitialized(OSGiEmbedder embeddedEquinox)
   {
      final Collection<String> nonFrameworkArgs = new LinkedHashSet<String>();
      nonFrameworkArgs.add("-eclipse.keyring");
      final File secureStorage = new File(embeddedEquinox.getFrameworkLocation(), "secure_storage");
      try
      {
         secureStorage.createNewFile();
      }
      catch (IOException e)
      {
         throw pipe(e);
      }
      secureStorage.deleteOnExit();
      nonFrameworkArgs.add(secureStorage.getAbsolutePath());

      EclipseEnvironmentInfo envInfo = newEclipseEnvironmentInfo(embeddedEquinox.getBundleContext());
      envInfo.setAppArgs(nonFrameworkArgs.toArray(new String[nonFrameworkArgs.size()]));
   }
}
