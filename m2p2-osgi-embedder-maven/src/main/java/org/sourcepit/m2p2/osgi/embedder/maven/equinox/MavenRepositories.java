/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.m2p2.osgi.embedder.maven.equinox;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.equinox.p2.repository.IRepository;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.osgi.service.log.LogService;


/**
 * @author imm0136
 */
public final class MavenRepositories
{
   private MavenRepositories()
   {
      super();
   }

   public static Collection<URI> applyMavenP2Repositories(final ISecurePreferences securePreferences,
      final SettingsDecrypter settingsDecrypter, final List<Server> servers,
      final List<ArtifactRepository> repositories, LogService logger)
   {
      final Map<String, String> repoMap = new HashMap<String, String>();
      for (ArtifactRepository repo : repositories)
      {
         if ("p2".equals(repo.getLayout().getId()))
         {
            repoMap.put(repo.getId(), repo.getUrl());
         }
      }
      return applyMavenP2Repositories(securePreferences, settingsDecrypter, servers, repoMap, logger);

   }

   public static Collection<URI> applyMavenP2Repositories(final ISecurePreferences securePreferences,
      final SettingsDecrypter settingsDecrypter, final List<Server> servers, final Map<String, String> repositories,
      LogService logger)
   {
      final Map<String, Server> idToServerMap = new HashMap<String, Server>();
      for (Server server : servers)
      {
         server = decrypt(settingsDecrypter, server);
         idToServerMap.put(server.getId(), server);
      }

      Map<String, Server> hostServerMap = new HashMap<String, Server>();
      try
      {
         for (String serverId : idToServerMap.keySet())
         {
            String repositoryUrl = repositories.get(serverId);
            if (null != repositoryUrl)
            {
               Server server = idToServerMap.get(serverId);
               URL url = new URL(repositoryUrl);
               String host = url.getHost();
               hostServerMap.put(host, server);
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

      final Collection<URI> p2Repositories = new LinkedHashSet<URI>();
      for (String repositoryUrl : repositories.values())
      {
         final URI uri = getURI(repositoryUrl);
         String host = uri.getHost();
         Server server = hostServerMap.get(host);
         p2Repositories.add(uri);
         if (server != null)
         {
            final String username = server.getUsername();
            final String password = server.getPassword();
            setCredentials(securePreferences, uri, username, password, logger);
         }
      }
      return p2Repositories;
   }

   private static URI getURI(String uri)
   {
      try
      {
         return normalize(new URI(uri.replace('\\', '/')));
      }
      catch (URISyntaxException e)
      {
         throw new IllegalArgumentException(e);
      }
   }

   private static Server decrypt(final SettingsDecrypter settingsDecrypter, Server server)
   {
      return settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(server)).getServer();
   }

   private static void setCredentials(ISecurePreferences securePreferences, URI location, String username,
      String password, LogService logger)
   {
      // if URI is not opaque, just getting the host may be enough
      String host = location.getHost();
      if (host == null)
      {
         String scheme = location.getScheme();
         if (URIUtil.isFileURI(location) || scheme == null)
         {
            // If the URI references a file, a password could possibly be needed for the directory
            // (it could be a protected zip file representing a compressed directory) - in this
            // case the key is the path without the last segment.
            // Using "Path" this way may result in an empty string - which later will result in
            // an invalid key.
            host = new Path(location.toString()).removeLastSegments(1).toString();
         }
         else
         {
            // it is an opaque URI - details are unknown - can only use entire string.
            host = location.toString();
         }
      }
      String nodeKey;
      try
      {
         nodeKey = URLEncoder.encode(host, "UTF-8"); //$NON-NLS-1$
      }
      catch (UnsupportedEncodingException e2)
      {
         // fall back to default platform encoding
         try
         {
            // Uses getProperty "file.encoding" instead of using deprecated URLEncoder.encode(String location)
            // which does the same, but throws NPE on missing property.
            String enc = System.getProperty("file.encoding"); //$NON-NLS-1$
            if (enc == null)
            {
               throw new UnsupportedEncodingException("No UTF-8 encoding and missing system property: file.encoding"); //$NON-NLS-1$
            }
            nodeKey = URLEncoder.encode(host, enc);
         }
         catch (UnsupportedEncodingException e)
         {
            throw new RuntimeException(e);
         }
      }
      String nodeName = IRepository.PREFERENCE_NODE + '/' + nodeKey;

      ISecurePreferences prefNode = securePreferences.node(nodeName);

      try
      {
         if (!username.equals(prefNode.get(IRepository.PROP_USERNAME, username))
            || !password.equals(prefNode.get(IRepository.PROP_PASSWORD, password)))
         {
            logger.log(LogService.LOG_INFO, "Redefining access credentials for repository host " + host);
         }
         prefNode.put(IRepository.PROP_USERNAME, username, false);
         prefNode.put(IRepository.PROP_PASSWORD, password, false);
      }
      catch (StorageException e)
      {
         throw new RuntimeException(e);
      }
   }

   private static URI normalize(URI location)
   {
      // remove trailing slashes
      try
      {
         String path = location.getPath();
         if (path != null && path.endsWith("/"))
         {
            return new URI(location.getScheme(), location.getAuthority(), path.substring(0, path.length() - 1),
               location.getQuery(), location.getFragment());
         }
         else
         {
            return location;
         }
      }
      catch (URISyntaxException e)
      {
         throw new IllegalArgumentException(e);
      }
   }
}
