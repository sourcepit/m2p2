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

import static org.sourcepit.m2p2.osgi.embedder.BundleContextUtil.getService;
import static org.sourcepit.m2p2.osgi.embedder.maven.equinox.SecurePreferencesUtil.getSecurePreferences;

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.sourcepit.m2p2.osgi.embedder.AbstractOSGiEmbedderLifecycleListener;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;

public class P2RepositoryCredentialsConfigurer extends AbstractOSGiEmbedderLifecycleListener
{
   private final LegacySupport buildContext;

   private final SettingsDecrypter settingsDecrypter;

   public P2RepositoryCredentialsConfigurer(LegacySupport buildContext, SettingsDecrypter settingsDecrypter)
   {
      this.buildContext = buildContext;
      this.settingsDecrypter = settingsDecrypter;
   }

   @Override
   public void bundlesStarted(OSGiEmbedder embeddedEquinox)
   {
      final BundleContext bundleContext = embeddedEquinox.getBundleContext();

      final ISecurePreferences securePreferences = getSecurePreferences(bundleContext);

      final MavenSession session = buildContext.getSession();

      final MavenProject mavenProject = session.getCurrentProject();
      final List<ArtifactRepository> repositories = mavenProject.getRemoteArtifactRepositories();
      final LogService logger = getService(bundleContext, LogService.class);
      final List<Server> servers = session.getSettings().getServers();

      MavenRepositories.applyMavenP2Repositories(securePreferences, settingsDecrypter, servers, repositories, logger);
   }
}
