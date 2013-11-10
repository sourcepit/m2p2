/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.maven;


import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;

public class ChainedExecutionListener implements ExecutionListener
{
   private final static ExecutionListener NULL_LISTENER = new AbstractExecutionListener()
   { // noop
   };

   private final ExecutionListener executionListeners;

   public ChainedExecutionListener(ExecutionListener executionListeners)
   {
      this.executionListeners = executionListeners == null ? NULL_LISTENER : executionListeners;
   }

   public ExecutionListener getExecutionListeners()
   {
      return executionListeners;
   }

   public void projectDiscoveryStarted(ExecutionEvent event)
   {
      executionListeners.projectDiscoveryStarted(event);
   }

   public void sessionStarted(ExecutionEvent event)
   {
      executionListeners.sessionStarted(event);
   }

   public void sessionEnded(ExecutionEvent event)
   {
      executionListeners.sessionEnded(event);
   }

   public void projectSkipped(ExecutionEvent event)
   {
      executionListeners.projectSkipped(event);
   }

   public void projectStarted(ExecutionEvent event)
   {
      executionListeners.projectStarted(event);
   }

   public void projectSucceeded(ExecutionEvent event)
   {
      executionListeners.projectSucceeded(event);
   }

   public void projectFailed(ExecutionEvent event)
   {
      executionListeners.projectFailed(event);
   }

   public void mojoSkipped(ExecutionEvent event)
   {
      executionListeners.mojoSkipped(event);
   }

   public void mojoStarted(ExecutionEvent event)
   {
      executionListeners.mojoStarted(event);
   }

   public void mojoSucceeded(ExecutionEvent event)
   {
      executionListeners.mojoSucceeded(event);
   }

   public void mojoFailed(ExecutionEvent event)
   {
      executionListeners.mojoFailed(event);
   }

   public void forkStarted(ExecutionEvent event)
   {
      executionListeners.forkStarted(event);
   }

   public void forkSucceeded(ExecutionEvent event)
   {
      executionListeners.forkSucceeded(event);
   }

   public void forkFailed(ExecutionEvent event)
   {
      executionListeners.forkFailed(event);
   }

   public void forkedProjectStarted(ExecutionEvent event)
   {
      executionListeners.forkedProjectStarted(event);
   }

   public void forkedProjectSucceeded(ExecutionEvent event)
   {
      executionListeners.forkedProjectSucceeded(event);
   }

   public void forkedProjectFailed(ExecutionEvent event)
   {
      executionListeners.forkedProjectFailed(event);
   }
}
