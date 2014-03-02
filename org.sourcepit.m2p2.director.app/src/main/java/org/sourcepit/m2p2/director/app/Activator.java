/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.director.app;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class Activator implements BundleActivator
{
   private static Activator activator;

   private LogService log;

   private ServiceReference<LogService> logServiceReference;

   @Override
   public void start(BundleContext context) throws Exception
   {
      activator = this;
      logServiceReference = context.getServiceReference(LogService.class);
      log = context.getService(logServiceReference);
   }

   @Override
   public void stop(BundleContext context) throws Exception
   {
      log = null;
      context.ungetService(logServiceReference);
      activator = null;
   }

   public static Activator getDefault()
   {
      return activator;
   }

   public LogService getLog()
   {
      return log;
   }

}
