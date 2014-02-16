/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder.maven;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.sourcepit.common.maven.testing.EmbeddedMavenEnvironmentTest;
import org.sourcepit.common.maven.testing.MavenExecutionResult2;
import org.sourcepit.common.testing.Environment;
import org.sourcepit.common.utils.props.LinkedPropertiesMap;
import org.sourcepit.common.utils.props.PropertiesMap;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;
import org.sourcepit.m2p2.osgi.embedder.maven.MavenEquinoxFactory;


public class P2Test extends EmbeddedMavenEnvironmentTest
{
   @Inject
   private LegacySupport buildContext;

   @Inject
   private RepositorySystem repositorySystem;

   @Inject
   private MavenEquinoxFactory mavenEquinoxFactory;

   private OSGiEmbedder equinox;

   @Override
   protected Environment newEnvironment()
   {
      return Environment.get("env-test.properties");
   }

   @Override
   protected ClassLoader getClassLoader()
   {
      return new ClassLoader(super.getClassLoader())
      {
         @Override
         public Enumeration<URL> getResources(String name) throws IOException
         {
            if ("META-INF/plexus/components.xml".equals(name))
            {
               List<URL> res = new ArrayList<URL>();
               Enumeration<URL> resources = super.getResources(name);
               int i = 0;
               while (resources.hasMoreElements())
               {
                  URL url = (URL) resources.nextElement();
                  if (i > 0)
                  {
                     res.add(url);
                  }
                  i++;
               }
               return Collections.enumeration(res);
            }
            return super.getResources(name);
         }
      };
   }

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      MavenExecutionResult2 result = buildStubProject(ws.getRoot());
      buildContext.setSession(result.getSession());

      ArtifactRepository repo = repositorySystem.createArtifactRepository("srcpit-thirdparty",
         "http://nexus.sourcepit.org/content/repositories/thirdparty/", null, null, null);

      MavenProject project = buildContext.getSession().getCurrentProject();
      List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
      repos.add(repo);
      project.setRemoteArtifactRepositories(repos);

      final PropertiesMap configuration = readConfig();

      equinox = mavenEquinoxFactory.create(buildContext.getSession(), configuration);
      equinox.start();

      BundleContext bundleContext = equinox.getBundleContext();
      getContainer().addComponent(bundleContext, BundleContext.class, "equinox");

      ServiceReference<IProvisioningAgent> serviceReference = bundleContext
         .getServiceReference(IProvisioningAgent.class);
      IProvisioningAgent provisioningAgent = bundleContext.getService(serviceReference);

      getContainer().addComponent(provisioningAgent, IProvisioningAgent.class, null);
   }

   private PropertiesMap readConfig()
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

   @Override
   @After
   public void tearDown() throws Exception
   {
      try
      {
         equinox.stop(5000);
      }
      finally
      {
         super.tearDown();
      }
   }

   @Test
   public void testFoo() throws Exception
   {
      IProvisioningAgent provisioningAgent = lookup(IProvisioningAgent.class);

      IMetadataRepositoryManager metadataRepositoryManager = (IMetadataRepositoryManager) provisioningAgent
         .getService(IMetadataRepositoryManager.SERVICE_NAME);

      URI uri = new URI("http://download.eclipse.org/technology/m2e/releases");

      IMetadataRepository repository = metadataRepositoryManager.loadRepository(uri, null);

      final IQueryResult<IInstallableUnit> queryResult = repository.query(QueryUtil.ALL_UNITS, null);
      for (IInstallableUnit installableUnit : queryResult)
      {
         System.out.println(installableUnit);
      }
   }

}
