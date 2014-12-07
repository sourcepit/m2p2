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
