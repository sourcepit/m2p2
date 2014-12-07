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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.ArtifactFilterManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.filter.AndDependencyFilter;
import org.eclipse.aether.util.filter.ExclusionsDependencyFilter;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.sourcepit.common.maven.artifact.ArtifactFactory;
import org.sourcepit.common.maven.model.ArtifactKey;

@Named
public class ArtifactResolver
{
   @Inject
   private ArtifactFilterManager artifactFilterManager;

   @Inject
   private RepositorySystem repositorySystem;

   @Inject
   private ArtifactFactory artifactFactory;


   public DependencyResult resolve(MavenSession session, List<ArtifactKey> rootArtifacts, List<String> exclusions)
      throws DependencyResolutionException
   {
      final List<Dependency> bundleArtifacts = new ArrayList<Dependency>(rootArtifacts.size());
      for (ArtifactKey rootArtifact : rootArtifacts)
      {
         final Artifact artifact = artifactFactory.createArtifact(rootArtifact);
         bundleArtifacts.add(new Dependency(artifact, null));
      }
      return resolveDependencies(session, bundleArtifacts, exclusions);
   }

   private DependencyResult resolveDependencies(MavenSession session, List<Dependency> bundleArtifacts,
      List<String> exclusions) throws DependencyResolutionException
   {
      final List<RemoteRepository> repositories = getRemoteRepositories(session);

      final CollectRequest collectRequest = new CollectRequest(bundleArtifacts, null, repositories);

      final List<DependencyFilter> filters = new ArrayList<DependencyFilter>(2);
      filters.add(new ScopeDependencyFilter("provided", "test"));
      filters.add(new ExclusionsDependencyFilter(artifactFilterManager.getCoreArtifactExcludes()));

      if (exclusions != null && !exclusions.isEmpty())
      {
         filters.add(new ExclusionsDependencyFilter(exclusions));
      }

      final DependencyRequest dependencyRequest = new DependencyRequest(collectRequest,
         new AndDependencyFilter(filters));

      return repositorySystem.resolveDependencies(session.getRepositorySession(), dependencyRequest);
   }


   private List<RemoteRepository> getRemoteRepositories(MavenSession session)
   {
      final Set<RemoteRepository> repositories = new HashSet<RemoteRepository>();
      for (MavenProject project : session.getProjects())
      {
         repositories.addAll(project.getRemoteProjectRepositories());
      }
      return new ArrayList<RemoteRepository>(repositories);
   }


}
