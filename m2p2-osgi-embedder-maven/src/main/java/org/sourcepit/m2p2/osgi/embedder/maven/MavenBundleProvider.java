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

import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.sourcepit.common.maven.model.ArtifactKey;
import org.sourcepit.common.utils.collections.Iterable2;
import org.sourcepit.common.utils.collections.Iterator2;
import org.sourcepit.m2p2.osgi.embedder.BundleProvider;

public class MavenBundleProvider implements BundleProvider<DependencyResolutionException>
{
   private static class JARResolver implements Iterable2<URL, DependencyResolutionException>
   {
      private ArtifactResolver artifactResolver;
      private MavenSession session;
      private List<ArtifactKey> artifacts;
      private List<String> exclusions;

      public JARResolver(ArtifactResolver artifactResolver, MavenSession session, List<ArtifactKey> artifacts,
         List<String> exclusions)
      {
         this.artifactResolver = artifactResolver;
         this.session = session;
         this.artifacts = artifacts;
         this.exclusions = exclusions;
      }

      @Override
      public Iterator2<URL, DependencyResolutionException> iterator() throws DependencyResolutionException
      {
         final DependencyResult result = artifactResolver.resolve(session, artifacts, exclusions);
         final List<ArtifactResult> artifactResults = result.getArtifactResults();
         final List<URL> bundleURIs = new ArrayList<URL>(artifactResults.size());
         for (ArtifactResult artifactResult : artifactResults)
         {
            final Artifact artifact = artifactResult.getArtifact();
            try
            {
               bundleURIs.add(artifact.getFile().toURI().toURL());
            }
            catch (MalformedURLException e)
            {
               throw pipe(e);
            }
         }
         final Iterator<URL> iterator = bundleURIs.iterator();
         return new Iterator2<URL, DependencyResolutionException>()
         {
            @Override
            public boolean hasNext() throws DependencyResolutionException
            {
               return iterator.hasNext();
            }

            @Override
            public URL next() throws DependencyResolutionException
            {
               return iterator.next();
            }
         };
      }
   }

   private JARResolver frameworkJARs, bundleJARs;

   public MavenBundleProvider(ArtifactResolver artifactResolver, MavenSession session,
      List<ArtifactKey> frameworkArtifacts, List<ArtifactKey> bundleArtifacts, List<String> exclusions)
   {
      frameworkJARs = new JARResolver(artifactResolver, session, frameworkArtifacts, exclusions);
      bundleJARs = new JARResolver(artifactResolver, session, bundleArtifacts, exclusions);
   }

   @Override
   public Iterable2<URL, DependencyResolutionException> getFrameworkJARs() throws DependencyResolutionException
   {
      return frameworkJARs;
   }

   @Override
   public Iterable2<URL, DependencyResolutionException> getBundleJARs() throws DependencyResolutionException
   {
      return bundleJARs;
   }

}
