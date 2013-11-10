/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.maven;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.utils.collections.Iterable2;
import org.sourcepit.common.utils.collections.Iterator2;

public class MavenBundleURIResolver implements Iterable2<URI, DependencyResolutionException>
{
   private final ArtifactResolver artifactResolver;

   private final MavenSession session;

   private final List<ArtifactKey> rootArtifacts;

   public MavenBundleURIResolver(ArtifactResolver artifactResolver, MavenSession session,
      List<ArtifactKey> rootArtifacts)
   {
      this.artifactResolver = artifactResolver;
      this.session = session;
      this.rootArtifacts = rootArtifacts;
   }

   @Override
   public Iterator2<URI, DependencyResolutionException> iterator() throws DependencyResolutionException
   {
      final DependencyResult result = artifactResolver.resolve(session, rootArtifacts);
      final List<ArtifactResult> artifactResults = result.getArtifactResults();
      final List<URI> bundleURIs = new ArrayList<URI>(artifactResults.size());
      for (ArtifactResult artifactResult : artifactResults)
      {
         final Artifact artifact = artifactResult.getArtifact();
         bundleURIs.add(artifact.getFile().toURI());
      }
      final Iterator<URI> iterator = bundleURIs.iterator();
      return new Iterator2<URI, DependencyResolutionException>()
      {
         @Override
         public boolean hasNext() throws DependencyResolutionException
         {
            return iterator.hasNext();
         }

         @Override
         public URI next() throws DependencyResolutionException
         {
            return iterator.next();
         }
      };
   }
}