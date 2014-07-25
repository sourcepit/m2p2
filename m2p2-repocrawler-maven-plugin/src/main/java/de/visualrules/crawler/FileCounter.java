/*
 * Copyright (C) 2014 Bosch Software Innovations GmbH. All rights reserved.
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

      List<FileCounter> subTasks = new ArrayList<>();

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
