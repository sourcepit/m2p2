/*
 * Copyright (C) 2014 Bosch Software Innovations GmbH. All rights reserved.
 */

package de.visualrules.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureExample
{
//   private Object mutex = new Object();


   public static void main(String[] args) throws InterruptedException, ExecutionException
   {
      new FutureExample().doMain();
   }

   private void doMain() throws InterruptedException, ExecutionException
   {
      long start = System.currentTimeMillis();
      
      ExecutorService executor = Executors.newFixedThreadPool(6);

      // final File file = new File("c:\\");

      Callable<Double> random = new Callable<Double>()
      {
         @Override
         public Double call() throws Exception
         {
            final double rnd = Math.random();
            System.out.println(Thread.currentThread().getName() + " " + rnd);
            return Double.valueOf(rnd);
         }
      };

      List<Callable<Double>> tasks = new ArrayList<Callable<Double>>();
      for (int i = 0; i < 10000; i++)
      {
         tasks.add(random);
      }

      List<Future<Double>> futures = executor.invokeAll(tasks);


      double result = 0;
      for (Future<Double> future : futures)
      {
         result += future.get().doubleValue();
      }
      
      System.out.println(result + " in " + (System.currentTimeMillis() - start) + " millis");


      executor.shutdown();
   }

}
