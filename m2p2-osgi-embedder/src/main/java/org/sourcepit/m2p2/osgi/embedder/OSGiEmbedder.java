/**
 * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.osgi.embedder;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcepit.common.utils.io.Read.FromStream;
import org.sourcepit.common.utils.lang.ThrowablePipe;

public class OSGiEmbedder
{
   private static final Logger LOG = LoggerFactory.getLogger(OSGiEmbedder.class);

   private final FrameworkLocationProvider frameworkLocationProvider;
   private final StartLevelProvider startLevelProvider;
   private final BundleStartPolicyProvider bundleStartPolicyProvider;
   private final BundleProvider<? extends Exception> bundleProvider;
   private final Map<String, String> frameworkProperties;
   private final ClassLoadingStrategy classLoadingStrategy;

   private List<OSGiEmbedderLifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<OSGiEmbedderLifecycleListener>();

   public OSGiEmbedder(FrameworkLocationProvider frameworkLocationProvider, StartLevelProvider startLevelProvider,
      BundleStartPolicyProvider bundleStartPolicyProvider, BundleProvider<? extends Exception> bundleProvider,
      Map<String, String> frameworkProperties, ClassLoadingStrategy classLoadingStrategy)
   {
      this.frameworkLocationProvider = frameworkLocationProvider;
      this.startLevelProvider = startLevelProvider;
      this.bundleStartPolicyProvider = bundleStartPolicyProvider;
      this.bundleProvider = bundleProvider;
      this.frameworkProperties = frameworkProperties;
      this.classLoadingStrategy = classLoadingStrategy;
   }

   public void addLifecycleListener(OSGiEmbedderLifecycleListener lifecycleListener)
   {
      lifecycleListeners.add(lifecycleListener);
   }

   public void removeLifecycleListener(OSGiEmbedderLifecycleListener lifecycleListener)
   {
      lifecycleListeners.remove(lifecycleListener);
   }

   private boolean started;

   private File frameworkLocation;

   private ClassLoader frameworkClassLoader;

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

      classLoadingStrategy.adoptFrameworkProperties(frameworkProerties);

      for (OSGiEmbedderLifecycleListener lifecycleListener : lifecycleListeners)
      {
         lifecycleListener.frameworkPropertiesInitialized(this, frameworkProerties);
      }

      try
      {
         frameworkClassLoader = classLoadingStrategy.newFrameworkClassLoader(bundleProvider.getFrameworkJARs());
      }
      catch (Exception e)
      {
         throw pipe(e);
      }


      final FrameworkFactory frameworkFactory = newFrameworkFactory(frameworkClassLoader);

      framework = frameworkFactory.newFramework(frameworkProerties);
      try
      {
         framework.init();
      }
      catch (BundleException e)
      {
         throw pipe(e);
      }
      
      for (OSGiEmbedderLifecycleListener lifecycleListener : lifecycleListeners)
      {
         lifecycleListener.frameworkInitialized(this);
      }
      
      try
      {
         framework.start();
      }
      catch (BundleException e)
      {
         throw pipe(e);
      }

      for (OSGiEmbedderLifecycleListener lifecycleListener : lifecycleListeners)
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

      for (OSGiEmbedderLifecycleListener lifecycleListener : lifecycleListeners)
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
         LOG.debug("{} {}", bundle.getSymbolicName(), bundle.getState());
      }

      for (OSGiEmbedderLifecycleListener lifecycleListener : lifecycleListeners)
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
         for (OSGiEmbedderLifecycleListener lifecycleListener : lifecycleListeners)
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

         for (OSGiEmbedderLifecycleListener lifecycleListener : lifecycleListeners)
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
         try
         {
            if (frameworkLocation != null)
            {
               frameworkLocationProvider.releaseFrameworkLocation(frameworkLocation);
            }
         }
         catch (Exception e)
         {
            errors.add(e);
         }
         finally
         {
            try
            {
               if (frameworkClassLoader != null)
               {
                  classLoadingStrategy.disposeFrameworkClassLoader(frameworkClassLoader);
               }
            }
            catch (Exception e)
            {
               errors.add(e);
            }
         }

         framework = null;
         frameworkLocation = null;
      }

      errors.throwPipe();
   }

   public File getFrameworkLocation()
   {
      return frameworkLocation;
   }

   public ClassLoader getFrameworkClassLoader()
   {
      return frameworkClassLoader;
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
