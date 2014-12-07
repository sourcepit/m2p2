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
