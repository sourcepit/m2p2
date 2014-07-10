/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.director;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.sourcepit.common.utils.io.IO.fileIn;
import static org.sourcepit.common.utils.io.IO.read;
import static org.sourcepit.common.utils.io.IO.zipIn;
import static org.sourcepit.common.utils.xml.XmlUtils.queryNodes;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sourcepit.common.utils.io.Read;
import org.sourcepit.common.utils.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSInput;

public class DetermineRuntimeBundlesTest
{

   //
   // <requires>
   // <import plugin="org.eclipse.equinox.common" version="3.6" match="compatible"/>
   // <import plugin="org.eclipse.equinox.registry" version="3.5" match="compatible"/>
   // <import plugin="org.eclipse.equinox.concurrent" version="1.0" match="compatible"/>
   // <import plugin="org.eclipse.core.jobs" version="3.5" match="compatible"/>
   // </requires>
   //
   // <plugin
   // id="org.eclipse.ecf"
   // download-size="0"
   // install-size="0"
   // version="3.4.0.v20140528-1625"
   // unpack="false"/>

   @Test
   public void testName() throws Exception
   {
      // download current equinox sdk
      // unzip to tmp folder
      // execute test
      // copy paste sysout

      final File targetDir = new File("C:/src/m2p2/m2p2-director-maven-plugin/target");
      final File featuresDir = new File("C:/tmp/equinox-SDK-Luna/features");

      final File pluginsDir = new File("C:/tmp/equinox-SDK-Luna/plugins");

      final File[] featureJars = featuresDir.listFiles(new FilenameFilter()
      {
         @Override
         public boolean accept(File dir, String name)
         {
            return name.endsWith(".jar");
         }
      });

      System.out.println("Inspect features in " + featuresDir);
      System.out.println();
      final Map<Feature, FeatureContents> featureContext = inspectFeatures(featureJars);

      final Set<Plugin> allPlugins = getAllPlugins(featureContext);

      // org.eclipse.core.runtime.feature, version=1.1.1.v20140606-1445]
      // org.eclipse.equinox.compendium.sdk, version=3.10.0.v20140416-2102]
      // org.eclipse.equinox.executable, version=3.6.100.v20140603-1326]
      // org.eclipse.equinox.p2.discovery.feature, version=1.0.200.v20140512-1802]
      // org.eclipse.equinox.p2.rcp.feature, version=1.2.0.v20140523-0116]
      // org.eclipse.equinox.p2.sdk, version=3.9.0.v20140523-0116]
      // org.eclipse.equinox.sdk, version=3.10.0.v20140606-1602]
      // org.eclipse.equinox.server.core, version=1.3.0.v20140606-1445]
      // org.eclipse.equinox.server.jetty, version=1.1.100.v20140416-1649]
      // org.eclipse.equinox.server.p2, version=1.2.100.v20140606-1602]
      // org.eclipse.equinox.serverside.sdk, version=3.10.0.v20140606-1602]
      // org.eclipse.equinox.weaving.sdk, version=1.1.0.v20140529-1734]
      // org.eclipse.equinox.core.sdk, version=3.10.0.v20140606-1445]

      Set<String> roots = new LinkedHashSet<String>();
      roots.add("org.eclipse.ecf.core.feature");
      roots.add("org.eclipse.ecf.core.ssl.feature");
      roots.add("org.eclipse.ecf.filetransfer.feature");
      roots.add("org.eclipse.ecf.filetransfer.httpclient4.feature");
      roots.add("org.eclipse.ecf.filetransfer.httpclient4.ssl.feature");
      roots.add("org.eclipse.ecf.filetransfer.ssl.feature");
      roots.add("org.eclipse.equinox.core.feature");
      roots.add("org.eclipse.equinox.p2.core.feature");
      roots.add("org.eclipse.equinox.p2.extras.feature");


      final Set<Plugin> result = new LinkedHashSet<Plugin>();

      for (Feature feature : featureContext.keySet())
      {
         if (contains(featureContext.get(feature).includes, "org.apache.commons.codec"))
         {
            System.err.println(feature);
         }

         if (!roots.contains(feature.id))
         {
            continue;
         }
         System.out.println("Inspect plugins in " + feature);

         Set<Plugin> plugins = new LinkedHashSet<Plugin>();
         collectPlugins(feature, featureContext, allPlugins, plugins);
         result.addAll(plugins);
      }
      System.out.println();

      Set<String> additionalPlugins = new HashSet<String>();
      additionalPlugins.add("org.eclipse.core.net");
      additionalPlugins.add("org.eclipse.equinox.p2.updatesite");

      for (Plugin plugin : allPlugins)
      {
         if (additionalPlugins.contains(plugin.id))
         {
            result.add(plugin);
         }
      }

      List<Plugin> sorted = new ArrayList<Plugin>(result);
      Collections.sort(sorted, new Comparator<Plugin>()
      {
         @Override
         public int compare(Plugin o1, Plugin o2)
         {
            return o1.id.compareTo(o2.id);
         }
      });

      for (Plugin plugin : sorted)
      {
         final File pluginFile = new File(pluginsDir, plugin.id + "_" + plugin.version + ".jar");
         copyFile(pluginFile, new File(targetDir, "plugins/" + pluginFile.getName()));
      }

      for (Plugin plugin : sorted)
      {
         System.out.println("    srcpit.mavenized:" + plugin.id + ":jar:" + plugin.version + ",\\");
      }
   }

   private boolean contains(List<IU> includes, String string)
   {
      for (IU iu : includes)
      {
         if (iu.id.equals(string))
         {
            return true;
         }
      }
      // TODO Auto-generated method stub
      return false;
   }

   private static Set<Plugin> getAllPlugins(final Map<Feature, FeatureContents> featureContext)
   {
      final Set<Plugin> allPlugins = new HashSet<Plugin>();
      for (FeatureContents fc : featureContext.values())
      {
         for (IU iu : fc.includes)
         {
            if (iu instanceof Plugin)
            {
               allPlugins.add((Plugin) iu);
            }
         }
      }
      return allPlugins;
   }

   private static Map<Feature, FeatureContents> inspectFeatures(final File[] featureJars)
   {
      final Map<Feature, FeatureContents> featureContext = new LinkedHashMap<Feature, FeatureContents>();
      for (File featureJar : featureJars)
      {
         inspectFeature(featureContext, featureJar);
      }
      return featureContext;
   }

   private static void collectPlugins(Feature feature, Map<Feature, FeatureContents> featuresContext,
      Collection<Plugin> pluginsContext, Set<Plugin> plugins)
   {
      FeatureContents contents = featuresContext.get(feature);
      for (IU iu : contents.includes)
      {
         if (iu instanceof Feature)
         {
            collectPlugins((Feature) iu, featuresContext, pluginsContext, plugins);
         }
         else
         {
            plugins.add((Plugin) iu);
         }
      }

      for (Requirement requirement : contents.requirements)
      {
         if (requirement instanceof FeatureRequirement)
         {
            final Feature f = resolve((FeatureRequirement) requirement, featuresContext.keySet());
            if (f == null)
            {
               System.err.println("Unresolvable feature requirement " + requirement);
            }
            else
            {
               collectPlugins(f, featuresContext, pluginsContext, plugins);
            }
         }
         else
         {
            final Plugin p = resolve((PluginRequirement) requirement, pluginsContext);
            if (p == null)
            {
               System.err.println("Unresolvable plugin requirement " + requirement);
            }
            else
            {
               plugins.add(p);
            }
         }
      }
   }

   private static Feature resolve(FeatureRequirement requirement, Collection<Feature> featureContext)
   {
      final List<Feature> result = new ArrayList<Feature>();

      // id matches
      final String requiredId = requirement.id;
      for (Feature feature : featureContext)
      {
         if (requiredId.equals(feature.id))
         {
            result.add(feature);
         }
      }

      final String requiredVersion = requirement.version;
      final Iterator<Feature> it = result.iterator();
      while (it.hasNext())
      {
         final Feature feature = it.next();

         if (requiredVersion.compareTo(feature.version) > 0)
         {
            it.remove();
         }
      }

      return result.isEmpty() ? null : result.get(0);
   }

   private static Plugin resolve(PluginRequirement requirement, Collection<Plugin> pluginsContext)
   {
      final List<Plugin> result = new ArrayList<Plugin>();

      // id matches
      final String requiredId = requirement.id;
      for (Plugin plugin : pluginsContext)
      {
         if (requiredId.equals(plugin.id))
         {
            result.add(plugin);
         }
      }

      final String requiredVersion = requirement.version;
      final Iterator<Plugin> it = result.iterator();
      while (it.hasNext())
      {
         final Plugin plugin = it.next();

         if (requiredVersion.compareTo(plugin.version) > 0)
         {
            it.remove();
         }
      }

      return result.isEmpty() ? null : result.get(0);
   }

   private static Feature inspectFeature(final Map<Feature, FeatureContents> featureContents, final File featureJar)
   {
      final Document featureXml = read(new Read.FromStream<Document>()
      {
         @Override
         public Document read(InputStream inputStream) throws Exception
         {
            return XmlUtils.readXml(inputStream);
         }
      }, zipIn(fileIn(featureJar), "feature.xml"));

      return inspectFeature(featureContents, featureXml);
   }

   private static Feature inspectFeature(final Map<Feature, FeatureContents> features, final Document featureXml)
   {
      Element featureElem = (Element) featureXml.getElementsByTagName("feature").item(0);
      final Feature feature = new Feature();
      initIU(featureElem, feature);

      final FeatureContents featureContents = new FeatureContents();
      featureContents.includes = new ArrayList<IU>();
      featureContents.requirements = new ArrayList<Requirement>();
      collectIncludesAndRequirements(featureXml, featureContents.includes, featureContents.requirements);

      features.put(feature, featureContents);

      return feature;
   }

   private static void collectIncludesAndRequirements(final Document featureXml, final List<IU> includes,
      final List<Requirement> requirements)
   {
      // includes
      for (Node node : queryNodes(featureXml, "/feature/includes"))
      {
         final IU feature = new Feature();
         initIU((Element) node, feature);
         includes.add(feature);
      }
      for (Node node : queryNodes(featureXml, "/feature/plugin"))
      {
         final IU plugin = new Plugin();
         initIU((Element) node, plugin);
         includes.add(plugin);
      }

      // requirements
      for (Node node : queryNodes(featureXml, "/feature/requires/import[@plugin]"))
      {
         final Requirement requirement = new PluginRequirement();
         final Element element = (Element) node;
         requirement.id = element.getAttribute("plugin");
         final String v = element.getAttribute("version");
         requirement.version = isNullOrEmpty(v) ? "0.0.0" : v;
         final String m = element.getAttribute("match");
         requirement.match = isNullOrEmpty(m) ? "greaterOrEqual" : m;
         requirements.add(requirement);
      }
      for (Node node : queryNodes(featureXml, "/feature/requires/import[@feature]"))
      {
         final Requirement requirement = new FeatureRequirement();
         final Element element = (Element) node;
         requirement.id = element.getAttribute("feature");
         final String v = element.getAttribute("version");
         requirement.version = isNullOrEmpty(v) ? "0.0.0" : v;
         final String m = element.getAttribute("match");
         requirement.match = isNullOrEmpty(m) ? "greaterOrEqual" : m;
         requirements.add(requirement);
      }
   }

   private static void initIU(final Element element, final IU feature)
   {
      feature.id = element.getAttribute("id");
      feature.version = element.getAttribute("version");
   }

   static class FeatureContents
   {
      List<IU> includes;
      List<Requirement> requirements;
   }

   static class FeatureRequirement extends Requirement
   {
   }

   static class PluginRequirement extends Requirement
   {
   }

   static class Requirement
   {
      String id;
      String version;
      String match;

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         result = prime * result + ((match == null) ? 0 : match.hashCode());
         result = prime * result + ((version == null) ? 0 : version.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         Requirement other = (Requirement) obj;
         if (id == null)
         {
            if (other.id != null)
               return false;
         }
         else if (!id.equals(other.id))
            return false;
         if (match == null)
         {
            if (other.match != null)
               return false;
         }
         else if (!match.equals(other.match))
            return false;
         if (version == null)
         {
            if (other.version != null)
               return false;
         }
         else if (!version.equals(other.version))
            return false;
         return true;
      }

      @Override
      public String toString()
      {
         return getClass().getSimpleName() + " [id=" + id + ", version=" + version + ", match=" + match + "]";
      }


   }

   static class Feature extends IU
   {
   }

   static class Plugin extends IU
   {
   }

   static class IU
   {
      String id;
      String version;

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         result = prime * result + ((version == null) ? 0 : version.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         IU other = (IU) obj;
         if (id == null)
         {
            if (other.id != null)
               return false;
         }
         else if (!id.equals(other.id))
            return false;
         if (version == null)
         {
            if (other.version != null)
               return false;
         }
         else if (!version.equals(other.version))
            return false;
         return true;
      }

      @Override
      public String toString()
      {
         return getClass().getSimpleName() + " [id=" + id + ", version=" + version + "]";
      }


   }
}
