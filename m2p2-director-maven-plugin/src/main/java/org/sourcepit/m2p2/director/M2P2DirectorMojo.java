/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.director;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;

import java.io.InputStream;

import javax.inject.Inject;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.sourcepit.common.utils.props.LinkedPropertiesMap;
import org.sourcepit.common.utils.props.PropertiesMap;
import org.sourcepit.guplex.Guplex;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;
import org.sourcepit.m2p2.osgi.embedder.maven.MavenEquinoxFactory;
import org.sourcepit.m2p2.osgi.embedder.maven.equinox.EquinoxEnvironmentConfigurer;
import org.sourcepit.m2p2.osgi.embedder.maven.equinox.EquinoxProxyConfigurer;
import org.sourcepit.m2p2.osgi.embedder.maven.equinox.P2RepositoryCredentialsConfigurer;

@Mojo(name = "install", defaultPhase = PACKAGE, requiresProject = false)
public class M2P2DirectorMojo extends AbstractMojo
{
   @Component
   private Guplex guplex;

   @Inject
   private LegacySupport buildContext;

   @Inject
   private MavenEquinoxFactory embedderFactory;

   @Inject
   private SettingsDecrypter settingsDecrypter;

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      guplex.inject(this, true);

      final OSGiEmbedder embedder = createOSGiEmbedder();
      embedder.start();
      try
      {

      }
      finally
      {
         embedder.stop(0);
      }
   }

   private OSGiEmbedder createOSGiEmbedder() throws MojoExecutionException
   {
      final MavenSession session = buildContext.getSession();

      final OSGiEmbedder embedder;
      try
      {
         embedder = embedderFactory.create(session, readEmbedderConfiguration());
      }
      catch (MavenExecutionException e)
      {
         throw new MojoExecutionException("Failed to create OSGi embedder.", e);
      }

      embedder.addLifecycleListener(new EquinoxEnvironmentConfigurer());
      embedder.addLifecycleListener(new EquinoxProxyConfigurer(buildContext, settingsDecrypter));
      embedder.addLifecycleListener(new P2RepositoryCredentialsConfigurer(buildContext, settingsDecrypter));

      return embedder;
   }

   private PropertiesMap readEmbedderConfiguration()
   {
      final PropertiesMap propertiesMap = new LinkedPropertiesMap();
      InputStream in = null;
      try
      {
         in = getClass().getResourceAsStream("osgi.properties");
         propertiesMap.load(in);
      }
      finally
      {
         closeQuietly(in);
      }
      return propertiesMap;
   }

}
