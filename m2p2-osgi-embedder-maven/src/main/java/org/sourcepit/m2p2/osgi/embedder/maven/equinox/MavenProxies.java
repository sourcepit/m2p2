/*
 * Copyright (C) 2013 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.sourcepit.m2p2.osgi.embedder.maven.equinox;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 * 
 * @author imm0136
 */
public final class MavenProxies
{
   private static final String MAVEN_SETTINGS_SOURCE = "MAVEN_SETTINGS";

   private static final Pattern NON_PROXY_DELIMITERS = Pattern.compile("\\s*[|,]\\s*");

   private MavenProxies()
   {
      super();
   }

   public static void applyMavenProxies(IProxyService proxyService, SettingsDecrypter settingsDecrypter,
      List<Proxy> proxies) throws CoreException
   {
      final List<IProxyData> proxyDatas = new ArrayList<IProxyData>();
      final Set<String> nonProxyHosts = new LinkedHashSet<String>();
      final Map<String, PasswordAuthentication> proxyTypeToAuthenticationMap = new HashMap<String, PasswordAuthentication>();

      getProxySettings(settingsDecrypter, proxies, proxyDatas, nonProxyHosts, proxyTypeToAuthenticationMap);

      final Authenticator authenticator = new Authenticator()
      {
         @Override
         protected PasswordAuthentication getPasswordAuthentication()
         {
            final String proxyType = getProxyType(getRequestingProtocol());
            if (proxyType != null)
            {
               return proxyTypeToAuthenticationMap.get(proxyType);
            }
            return null;
         }
      };
      // not exactly pretty but this is how org.eclipse.core.net does it
      Authenticator.setDefault(authenticator);

      proxyService.setNonProxiedHosts(nonProxyHosts.toArray(new String[nonProxyHosts.size()]));
      proxyService.setProxyData(proxyDatas.toArray(new IProxyData[proxyDatas.size()]));
      proxyService.setProxiesEnabled(true);
      proxyService.setSystemProxiesEnabled(false);
   }

   private static void getProxySettings(SettingsDecrypter settingsDecrypter, List<Proxy> proxies,
      final List<IProxyData> proxyDatas, final Set<String> nonProxyHosts,
      final Map<String, PasswordAuthentication> proxyTypeToAuthenticationMap)
   {
      for (Proxy proxy : proxies)
      {
         final String proxyType = getProxyType(proxy.getProtocol());
         if (proxyType != null)
         {
            proxy = decrypt(settingsDecrypter, proxy);

            ProxyData proxyData = new ProxyData(proxyType);
            proxyData.setHost(proxy.getHost());
            proxyData.setPort(proxy.getPort());
            proxyData.setUserid(proxy.getUsername());
            proxyData.setPassword(proxy.getPassword());
            proxyData.setSource(MAVEN_SETTINGS_SOURCE);
            proxyDatas.add(proxyData);

            nonProxyHosts.addAll(getNonProxyHosts(proxy));

            final String username = proxy.getUsername();
            if (username != null)
            {
               final String password = proxy.getPassword();
               if (password != null)
               {
                  proxyTypeToAuthenticationMap.put(proxyType,
                     new PasswordAuthentication(username, password.toCharArray()));
               }
            }
         }
      }
   }

   private static Collection<String> getNonProxyHosts(Proxy proxy)
   {
      final String nonProxyHosts = proxy.getNonProxyHosts();
      if (nonProxyHosts != null && nonProxyHosts.trim().length() > 0)
      {
         return Arrays.asList(NON_PROXY_DELIMITERS.split(nonProxyHosts.trim()));
      }
      return Collections.<String> emptySet();
   }

   private static Proxy decrypt(SettingsDecrypter settingsDecrypter, Proxy proxy)
   {
      return settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(proxy)).getProxy();
   }

   private static String getProxyType(String protocol)
   {
      final String normalizedProtocol = protocol.trim().toLowerCase(Locale.ENGLISH);

      if ("http".equalsIgnoreCase(normalizedProtocol))
      {
         return IProxyData.HTTP_PROXY_TYPE;
      }

      if ("https".equalsIgnoreCase(normalizedProtocol))
      {
         return IProxyData.HTTPS_PROXY_TYPE;
      }

      if ("socks4".equalsIgnoreCase(normalizedProtocol))
      {
         return IProxyData.SOCKS_PROXY_TYPE;
      }
      
      return null;
   }

}
