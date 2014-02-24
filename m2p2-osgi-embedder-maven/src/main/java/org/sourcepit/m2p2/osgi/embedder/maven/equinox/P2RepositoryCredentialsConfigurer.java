/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
