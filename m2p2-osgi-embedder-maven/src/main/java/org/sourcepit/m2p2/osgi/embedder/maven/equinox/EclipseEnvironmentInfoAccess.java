/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder.maven.equinox;
public class EclipseEnvironmentInfoAccess
{
   private final ClassLoader frameworkClassLoader;
   
   public EclipseEnvironmentInfoAccess(ClassLoader frameworkClassLoader)
   {
      this.frameworkClassLoader = frameworkClassLoader;
   }
}