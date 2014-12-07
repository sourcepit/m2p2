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
