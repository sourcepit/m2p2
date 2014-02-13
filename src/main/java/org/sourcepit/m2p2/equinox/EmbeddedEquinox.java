/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.equinox;

import static com.google.common.base.Preconditions.checkState;
import static org.sourcepit.common.utils.collections.CollectionUtils.foreach;
import static org.sourcepit.common.utils.io.IO.cpIn;
import static org.sourcepit.common.utils.io.IO.read;
import static org.sourcepit.common.utils.lang.Exceptions.newThrowablePipe;
import static org.sourcepit.common.utils.lang.Exceptions.pipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.sourcepit.common.utils.io.Read.FromStream;
import org.sourcepit.common.utils.lang.ThrowablePipe;
import org.sourcepit.osgi.embedder.BundleProvider;
import org.sourcepit.osgi.embedder.BundleStartPolicyProvider;
import org.sourcepit.osgi.embedder.ConfigureBundleStartLevel;
import org.sourcepit.osgi.embedder.FrameworkLocationProvider;
import org.sourcepit.osgi.embedder.InstallBundle;
import org.sourcepit.osgi.embedder.StartBundle;
import org.sourcepit.osgi.embedder.StartLevelProvider;

public class EmbeddedEquinox
{
   private final FrameworkLocationProvider frameworkLocationProvider;
   private final StartLevelProvider startLevelProvider;
   private final BundleStartPolicyProvider bundleStartPolicyProvider;
   private final BundleProvider<? extends Exception> bundleProvider;
   private final Map<String, String> frameworkProperties;

   private List<EmbeddedEquinoxLifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<EmbeddedEquinoxLifecycleListener>();

   public EmbeddedEquinox(FrameworkLocationProvider frameworkLocationProvider, StartLevelProvider startLevelProvider,
      BundleStartPolicyProvider bundleStartPolicyProvider, BundleProvider<? extends Exception> bundleProvider,
      Map<String, String> frameworkProperties)
   {
      this.frameworkLocationProvider = frameworkLocationProvider;
      this.startLevelProvider = startLevelProvider;
      this.bundleStartPolicyProvider = bundleStartPolicyProvider;
      this.bundleProvider = bundleProvider;
      this.frameworkProperties = frameworkProperties;
   }

   public void addLifecycleListener(EmbeddedEquinoxLifecycleListener lifecycleListener)
   {
      lifecycleListeners.add(lifecycleListener);
   }

   public void removeLifecycleListener(EmbeddedEquinoxLifecycleListener lifecycleListener)
   {
      lifecycleListeners.remove(lifecycleListener);
   }

   private boolean started;

   private File frameworkLocation;

   private Framework framework;

   public synchronized void start()
   {
      checkState(!started, "EmbeddedEquinox has already been started.");
      started = true;

      try
      {
         frameworkLocation = frameworkLocationProvider.aquireFrameworkLocation();
      }
      catch (IOException e)
      {
         throw pipe(e);
      }

      final Map<String, String> frameworkProerties = new HashMap<String, String>(frameworkProperties);
      frameworkProerties.put("osgi.install.area", frameworkLocation.getAbsolutePath().toString());
      frameworkProerties.put("osgi.configuration.area", new File(frameworkLocation, "configuration").getAbsolutePath());

      final ClassLoaderConfiguration classLoaderConfiguration = new ClassLoaderConfiguration();
      final List<String> classNamePatterns = classLoaderConfiguration.getClassNamePatterns();
      classNamePatterns.add("org.osgi.**");
      classNamePatterns.add(IProgressMonitor.class.getName());
      classNamePatterns.add(IStatus.class.getName());
      classNamePatterns.add(CoreException.class.getName());
      classNamePatterns.add("org.eclipse.equinox.p2.core.*");
      classNamePatterns.add("org.eclipse.equinox.p2.metadata.*");
      classNamePatterns.add("org.eclipse.equinox.p2.repository.*");
      classNamePatterns.add("org.eclipse.equinox.p2.repository.metadata.*");
      classNamePatterns.add("org.eclipse.equinox.p2.repository.artifact.*");
      classNamePatterns.add("org.eclipse.equinox.p2.metadata.expression.*");
      classNamePatterns.add("org.eclipse.equinox.p2.query.*");

      final ClassLoaderFactory classLoaderFactory = new ClassLoaderFactory();

      final ClassLoader foreignClassLoader = getClass().getClassLoader();
      final ClassLoader frameworkClassLoader;
      try
      {
         frameworkClassLoader = classLoaderFactory.newFrameworkClassLoader(bundleProvider.getFrameworkJARs(),
            classLoaderConfiguration, foreignClassLoader);
      }
      catch (Exception e)
      {
         throw pipe(e);
      }

      final FrameworkFactory frameworkFactory = newFrameworkFactory(frameworkClassLoader);

      framework = frameworkFactory.newFramework(frameworkProerties);
      try
      {
         framework.start();
      }
      catch (BundleException e)
      {
         throw pipe(e);
      }

      for (EmbeddedEquinoxLifecycleListener lifecycleListener : lifecycleListeners)
      {
         lifecycleListener.frameworkStarted(this);
      }

      final BundleContext bundleContext = framework.getBundleContext();

      try
      {
         foreach(bundleProvider.getBundleJARs(), new InstallBundle(bundleContext));
      }
      catch (Exception e)
      {
         throw pipe(e);
      }

      foreach(bundleContext.getBundles(), new ConfigureBundleStartLevel(startLevelProvider));

      for (EmbeddedEquinoxLifecycleListener lifecycleListener : lifecycleListeners)
      {
         lifecycleListener.bundlesInstalled(this);
      }

      try
      {
         foreach(bundleContext.getBundles(), new StartBundle(bundleStartPolicyProvider));
      }
      catch (BundleException e)
      {
         throw pipe(e);
      }


      Semaphore semaphore = new Semaphore(0);
      framework.adapt(FrameworkStartLevel.class).setStartLevel(startLevelProvider.getFrameworkStartLevel(framework),
         new StartupEventListener(semaphore, FrameworkEvent.STARTLEVEL_CHANGED));
      try
      {
         semaphore.acquire();
      }
      catch (InterruptedException e)
      {
      }

      for (Bundle bundle : bundleContext.getBundles())
      {
         System.out.println(bundle.getSymbolicName() + ": " + bundle.getState());
      }

      for (EmbeddedEquinoxLifecycleListener lifecycleListener : lifecycleListeners)
      {
         lifecycleListener.bundlesStarted(this);
      }
   }

   static class StartupEventListener implements SynchronousBundleListener, FrameworkListener
   {
      private final Semaphore semaphore;
      private final int frameworkEventType;

      public StartupEventListener(Semaphore semaphore, int frameworkEventType)
      {
         this.semaphore = semaphore;
         this.frameworkEventType = frameworkEventType;
      }

      public void bundleChanged(BundleEvent event)
      {
         if (event.getBundle().getBundleId() == 0 && event.getType() == BundleEvent.STOPPING)
            semaphore.release();
      }

      public void frameworkEvent(FrameworkEvent event)
      {
         if (event.getType() == frameworkEventType)
            semaphore.release();
      }
   }

   public synchronized void stop(long timeout)
   {
      checkState(started, "EmbeddedEquinox has not been started yet.");
      started = false;

      final ThrowablePipe errors = newThrowablePipe();

      try
      {
         for (EmbeddedEquinoxLifecycleListener lifecycleListener : lifecycleListeners)
         {
            try
            {
               lifecycleListener.frameworkAboutToStop(this);
            }
            catch (RuntimeException e)
            {
               errors.add(e);
            }
         }

         try
         {
            framework.stop();
            framework.waitForStop(timeout);
         }
         catch (InterruptedException e)
         { // noop
         }
         catch (BundleException e)
         {
            errors.add(e);
         }

         for (EmbeddedEquinoxLifecycleListener lifecycleListener : lifecycleListeners)
         {
            try
            {
               lifecycleListener.frameworkStopped(this);
            }
            catch (RuntimeException e)
            {
               errors.add(e);
            }
         }
      }
      finally
      {
         if (frameworkLocation != null)
         {
            try
            {
               frameworkLocationProvider.releaseFrameworkLocation(frameworkLocation);
            }
            catch (IOException e)
            {
               errors.add(e);
            }
         }

         framework = null;
         frameworkLocation = null;
      }

      errors.throwPipe();
   }

   public BundleContext getBundleContext()
   {
      return framework.getBundleContext();
   }

   private static FrameworkFactory newFrameworkFactory(final ClassLoader classLoader)
   {
      final FromStream<FrameworkFactory> fromStream = new FromStream<FrameworkFactory>()
      {
         @Override
         public FrameworkFactory read(InputStream inputStream) throws Exception
         {
            final BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            for (String s = r.readLine(); s != null; s = r.readLine())
            {
               s = s.trim();
               // Try to load first non-empty, non-commented line.
               if ((s.length() > 0) && (s.charAt(0) != '#'))
               {
                  return (FrameworkFactory) classLoader.loadClass(s).newInstance();
               }
            }
            return null;
         }
      };
      return read(fromStream, cpIn(classLoader, "META-INF/services/org.osgi.framework.launch.FrameworkFactory"));
   }
}
