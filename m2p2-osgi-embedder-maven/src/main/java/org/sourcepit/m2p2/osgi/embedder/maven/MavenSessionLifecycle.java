/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder.maven;

import javax.inject.Inject;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sourcepit.guplex.Guplex;
import org.sourcepit.m2p2.osgi.embedder.OSGiEmbedder;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "m2p2")
public class MavenSessionLifecycle extends AbstractMavenLifecycleParticipant
{
   @Requirement
   private Guplex guplex;

   @Inject
   private MavenEquinoxFactory equinoxFactory;

   @Override
   public void afterProjectsRead(MavenSession session) throws MavenExecutionException
   {
      guplex.inject(this, true);

      final OSGiEmbedder embeddedEquinox = equinoxFactory.create(session);
      embeddedEquinox.start();

      final MavenExecutionRequest request = session.getRequest();

      final ChainedExecutionListener executionListener = new ChainedExecutionListener(request.getExecutionListener())
      {
         @Override
         public void sessionEnded(ExecutionEvent event)
         {
            // unchain
            request.setExecutionListener(getExecutionListeners());
            // dispose
            embeddedEquinox.stop(15000L);
            super.sessionEnded(event);
         }
      };

      request.setExecutionListener(executionListener);
   }

}
