/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.director.app;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.internal.p2.director.app.DirectorApplication;
import org.eclipse.equinox.internal.p2.director.app.ILog;
import org.osgi.service.log.LogService;

public class M2P2DirectorApplication extends DirectorApplication
{
   @Override
   public Object start(IApplicationContext context) throws Exception
   {
      final LogService log = Activator.getDefault().getLog();
      setLog(new ILog()
      {
         @Override
         public void log(String message)
         {
            log.log(LogService.LOG_INFO, message);
         }

         @Override
         public void log(IStatus status)
         {
            log.log(getLogLevel(status), status.getMessage(), status.getException());
         }

         private int getLogLevel(IStatus status)
         {
            switch (status.getSeverity())
            {
               case IStatus.ERROR :
                  return LogService.LOG_ERROR;
               case IStatus.WARNING :
                  return LogService.LOG_WARNING;
               default :
                  return LogService.LOG_INFO;
            }
         }

         @Override
         public void close()
         { // noop
         }
      });

      return super.start(context);
   }
}
