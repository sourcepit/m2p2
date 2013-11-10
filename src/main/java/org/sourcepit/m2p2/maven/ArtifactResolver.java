/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.maven;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.ArtifactFilterManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.filter.AndDependencyFilter;
import org.sonatype.aether.util.filter.ExclusionsDependencyFilter;
import org.sonatype.aether.util.filter.ScopeDependencyFilter;
import org.sourcepit.common.maven.aether.ArtifactFactory;
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


   public DependencyResult resolve(MavenSession session, List<ArtifactKey> rootArtifacts)
      throws DependencyResolutionException
   {
      final List<Dependency> bundleArtifacts = new ArrayList<Dependency>(rootArtifacts.size());
      for (ArtifactKey rootArtifact : rootArtifacts)
      {
         final Artifact artifact = artifactFactory.createArtifact(rootArtifact);
         bundleArtifacts.add(new Dependency(artifact, null));
      }
      return resolveDependencies(session, bundleArtifacts);
   }

   private DependencyResult resolveDependencies(MavenSession session, List<Dependency> bundleArtifacts)
      throws DependencyResolutionException
   {
      final List<RemoteRepository> repositories = getRemoteRepositories(session);

      final CollectRequest collectRequest = new CollectRequest(bundleArtifacts, null, repositories);

      final List<DependencyFilter> filters = new ArrayList<DependencyFilter>(2);
      filters.add(new ScopeDependencyFilter("provided", "test"));
      filters.add(new ExclusionsDependencyFilter(artifactFilterManager.getCoreArtifactExcludes()));

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
