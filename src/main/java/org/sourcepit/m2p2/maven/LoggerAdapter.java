/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.maven;

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
