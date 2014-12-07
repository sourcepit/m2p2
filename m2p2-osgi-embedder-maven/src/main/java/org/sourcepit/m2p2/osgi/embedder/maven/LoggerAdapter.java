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

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;

public class LoggerAdapter implements LogListener
{
   private final Logger logger;

   public LoggerAdapter(Logger logger)
   {
      this.logger = logger;
   }

   @Override
   public void logged(LogEntry entry)
   {
      switch (getLevel(entry))
      {
         case LogService.LOG_INFO :
            logger.info(entry.getMessage(), entry.getException());
            break;
         case LogService.LOG_WARNING :
            logger.warn(entry.getMessage(), entry.getException());
            break;
         case LogService.LOG_DEBUG :
            logger.debug(entry.getMessage(), entry.getException());
            break;
         case LogService.LOG_ERROR :
            logger.error(entry.getMessage(), entry.getException());
            break;
         default :
            throw new IllegalStateException();
      }
   }

   private int getLevel(LogEntry entry)
   {
      if (LogService.LOG_INFO == entry.getLevel())
      {
         final String message = entry.getMessage();
         if (message.startsWith("BundleEvent ") || message.startsWith("ServiceEvent ")
            || message.startsWith("FrameworkEvent "))
         {
            return LogService.LOG_DEBUG;
         }
      }
      return entry.getLevel();
   }

}
