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

package org.sourcepit.m2p2.osgi.embedder.maven;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.director.DirectorActivator;
import org.eclipse.equinox.internal.p2.director.Explanation;
import org.eclipse.equinox.internal.p2.director.Projector;
import org.eclipse.equinox.internal.p2.director.QueryableArray;
import org.eclipse.equinox.internal.p2.director.Slicer;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.publisher.PublisherInfo;
import org.eclipse.equinox.p2.publisher.PublisherResult;
import org.eclipse.equinox.p2.publisher.actions.JREAction;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
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
import org.sourcepit.m2p2.osgi.embedder.maven.equinox.EquinoxEnvironmentConfigurer;
import org.sourcepit.m2p2.osgi.embedder.maven.equinox.EquinoxProxyConfigurer;


public class P2Test extends EmbeddedMavenEnvironmentTest
{
   @Inject
   private LegacySupport buildContext;

   @Inject
   private RepositorySystem repositorySystem;

   @Inject
   private SettingsDecrypter settingsDecrypter;

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
   protected File getUserHome()
   {
      // TODO Auto-generated method stub
      return super.getUserHome();
   }

   @Override
   protected File getUserSettingsFile()
   {
      // TODO Auto-generated method stub
      return super.getUserSettingsFile();
   }

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      MavenExecutionResult2 result = buildStubProject(ws.getRoot());
      buildContext.setSession(result.getSession());

      ArtifactRepository repo = repositorySystem.createArtifactRepository("srcpit-public",
         "http://nexus.sourcepit.org/content/groups/public", null, null, null);

      MavenProject project = buildContext.getSession().getCurrentProject();
      List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
      repos.add(repo);

      repositorySystem.injectAuthentication(repos, buildContext.getSession().getSettings().getServers());
      repositorySystem.injectProxy(repos, buildContext.getSession().getSettings().getProxies());

      project.setRemoteArtifactRepositories(repos);

      final PropertiesMap configuration = readConfig();

      equinox = mavenEquinoxFactory.create(buildContext.getSession(), configuration);

      equinox.addLifecycleListener(new EquinoxEnvironmentConfigurer());
      equinox.addLifecycleListener(new EquinoxProxyConfigurer(buildContext, settingsDecrypter));

      equinox.start();

      BundleContext bundleContext = equinox.getBundleContext();
      container.addComponent(bundleContext, BundleContext.class, "equinox");

      ServiceReference<IProvisioningAgent> serviceReference = bundleContext
         .getServiceReference(IProvisioningAgent.class);
      IProvisioningAgent provisioningAgent = bundleContext.getService(serviceReference);

      container.addComponent(provisioningAgent, IProvisioningAgent.class, null);
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
         equinox.stop(0);
      }
      finally
      {
         super.tearDown();
      }
   }

   @Test
   public void testFoo() throws Exception
   {
      IProvisioningAgent provisioningAgent = container.lookup(IProvisioningAgent.class);

      IMetadataRepositoryManager metadataRepositoryManager = (IMetadataRepositoryManager) provisioningAgent
         .getService(IMetadataRepositoryManager.SERVICE_NAME);

      URI uri = new URI("http://download.eclipse.org/releases/luna");

      IMetadataRepository repository = metadataRepositoryManager.loadRepository(uri, null);

      final IQueryResult<IInstallableUnit> queryResult = repository.query(QueryUtil.ALL_UNITS, null);
      for (IInstallableUnit installableUnit : queryResult)
      {
         System.out.println(installableUnit);
      }
   }

   @Test
   public void testSlice() throws Exception
   {
      URI uri = new URI("http://download.eclipse.org/releases/luna");

      IProvisioningAgent agent = container.lookup(IProvisioningAgent.class);

      ProvisioningContext context = new ProvisioningContext(agent);
      context.setArtifactRepositories(new URI[] { uri });
      context.setMetadataRepositories(new URI[] { uri });

      JREAction jreAction = new JREAction(new File("src/test/resources/JavaSE-1.7.profile").getAbsoluteFile());
      PublisherResult results = new PublisherResult();
      jreAction.perform(new PublisherInfo(), results, null);
      Iterator<IInstallableUnit> iterator = results.query(QueryUtil.ALL_UNITS, null).iterator();
      while (iterator.hasNext())
      {
         context.getExtraInstallableUnits().add(iterator.next());
      }

      final List<IQuery<IInstallableUnit>> queries = new ArrayList<IQuery<IInstallableUnit>>(2);
      queries.add(QueryUtil.createIUQuery("org.eclipse.equinox.executable.feature.group"));
      queries.add(QueryUtil.createIUQuery("org.eclipse.core.runtime"));
      final IQuery<IInstallableUnit> query = QueryUtil.createCompoundQuery(queries, false);

      IQueryable<IInstallableUnit> metadata = context.getMetadata(null);
      IQueryResult<IInstallableUnit> queryResult = metadata.query(QueryUtil.createLatestQuery(query), null);


      IInstallableUnit[] roots = queryResult.toArray(IInstallableUnit.class);

      List<Map<String, String>> multiContext = new ArrayList<Map<String, String>>();
      {
         final Map<String, String> filterProperties = new HashMap<String, String>();
         filterProperties.put("osgi.os", "win32");
         filterProperties.put("osgi.ws", "win32");
         filterProperties.put("osgi.arch", "x86_64");
         filterProperties.put("org.eclipse.update.install.features", "false");
         multiContext.add(filterProperties);
      }
      {
         final Map<String, String> filterProperties = new HashMap<String, String>();
         filterProperties.put("osgi.os", "win32");
         filterProperties.put("osgi.ws", "win32");
         filterProperties.put("osgi.arch", "x86");
         filterProperties.put("org.eclipse.update.install.features", "false");
         multiContext.add(filterProperties);
      }
      {
         final Map<String, String> filterProperties = new HashMap<String, String>();
         filterProperties.put("osgi.os", "macosx");
         filterProperties.put("osgi.ws", "cocoa");
         filterProperties.put("osgi.arch", "x86_64");
         filterProperties.put("org.eclipse.update.install.features", "false");
         multiContext.add(filterProperties);
      }

      Slicer slicer = new MultiContextsSlicer(metadata, multiContext, false);

      IQueryable<IInstallableUnit> sliced = slicer.slice(roots, new NullProgressMonitor());


      for (Map<String, String> filterProperties : multiContext)
      {
         IInstallableUnit root = createUnitRequiring("root", Arrays.asList(roots), null);

         Projector projector = new Projector(sliced, filterProperties, new HashSet<IInstallableUnit>(), false);

         DirectorActivator.context = equinox.getBundleContext();
         projector.encode(root, new IInstallableUnit[] {}, new QueryableArray(new IInstallableUnit[0]),
            Arrays.asList(roots), new NullProgressMonitor());
         DirectorActivator.context = null;

         IStatus s = projector.invokeSolver(new NullProgressMonitor());
         if (s.isOK())
         {
            for (IInstallableUnit iu : projector.extractSolution())
            {
               System.out.println(iu);
            }
         }
         else
         {
            for (Explanation e : projector.getExplanation(new NullProgressMonitor()))
            {
               System.err.println(e);
            }
         }

         System.out.println();
      }
   }

   protected static IInstallableUnit createUnitRequiring(String name, Collection<IInstallableUnit> units,
      Collection<IRequirement> additionalRequirements)
   {

      InstallableUnitDescription result = new MetadataFactory.InstallableUnitDescription();
      String time = Long.toString(System.currentTimeMillis());
      result.setId(name + "-" + time);
      result.setVersion(Version.createOSGi(0, 0, 0, time));

      ArrayList<IRequirement> requirements = new ArrayList<IRequirement>();
      if (units != null)
      {
         for (IInstallableUnit unit : units)
         {
            requirements.add(createStrictRequirementTo(unit));
         }
      }
      if (additionalRequirements != null)
      {
         requirements.addAll(additionalRequirements);
      }

      result.addRequirements(requirements);
      return MetadataFactory.createInstallableUnit(result);
   }

   private static IRequirement createStrictRequirementTo(IInstallableUnit unit)
   {
      VersionRange strictRange = new VersionRange(unit.getVersion(), true, unit.getVersion(), true);
      int min = 1;
      int max = Integer.MAX_VALUE;
      boolean greedy = true;
      IRequirement requirement = MetadataFactory.createRequirement(IInstallableUnit.NAMESPACE_IU_ID, unit.getId(),
         strictRange, unit.getFilter(), min, max, greedy);
      return requirement;
   }
}
