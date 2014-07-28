/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package de.visualrules.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class FileCounter extends RecursiveTask<Integer>
{

   /**
    * Comment for <code>serialVersionUID</code>
    */
   private static final long serialVersionUID = 1L;
   private final File folder;

   public FileCounter(File folder)
   {
      this.folder = folder;
   }

   @Override
   protected Integer compute()
   {
      int counter = 0;

      List<FileCounter> subTasks = new ArrayList<FileCounter>();

      File[] files = folder.listFiles();

      if (files == null)
      {
         return Integer.valueOf(0);
      }

      for (File file : files)
      {
         if (file.isDirectory())
         {
            FileCounter subTask = new FileCounter(file);
            subTasks.add(subTask);

            subTask.fork();
         }
         else
         {
            counter++;
         }
      }
      // ForkJoinTask.i


      for (FileCounter subTask : subTasks)
      {
         counter += subTask.join().intValue();
      }

      return Integer.valueOf(counter);

   }
 
}
