/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package de.visualrules.mojo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;

import de.visualrules.crawler.Crawler;

/**
 * <p>
 * Starts the crawler(for update sites) as a maven plugin
 * </p>
 * 
 * <p>
 * To implement this maven plugin put the following code in your pom.xml
 * </p>
 * 
 * <pre>
 * {@code
 * <plugin>
 *     <groupId>de.visualrules.tools</groupId>
 *     <artifactId>m2e-connectors-collector-reloaded</artifactId>
 *     <version>0.0.1-SNAPSHOT</version>
 *     <configuration>
 *          <repository>
               <id>foo</id>
               <url>http://central.maven.org/maven2/.m2e/connectors/m2e/1.3.0</url>
           </repository>
 *         <pathOutputFolder>D:\xmlfiles</pathOutputFolder>
 *         <outputFileName>test.xml</outputFileName>
 *         <repositoryNameInXMLFile>testRepo</repositoryNameInXMLFile>
 *     </configuration>
 *     <executions>
 *         <execution>
 *             <id>after clean</id>
 *             <phase>clean</phase>
 *             <goals>
 *                 <goal>startcrawler</goal>
 *             </goals>
 *         </execution>
 *     </executions>
 * </plugin>
 * }
 * </pre>
 *
 * 
 * The needed <b>configuration elements (or parameters)</b> to start the crawler can be found in the
 * {@code <configuration>} tag
 * 
 * <p>
 * In the table below is a description of the elements
 * </p>
 * <p>
 * <table>
 * <tr>
 * <th>Elements</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>{@code <pathOutputFolder>}</td>
 * <td><b>Required</b> - In this folder the generated xml file will be saved</td>
 * </tr>
 * <tr>
 * <td>{@code <outputFileName>}</td>
 * <td><b>Required</b> - This is the name of the generated xml file</td>
 * </tr>
 * <tr>
 * <td>{@code <repository>}</td>
 * <td><b>Required</b> - Typ {@code Repository} = Maven Repository</td>
 * </tr>
 * <tr>
 * <td>{@code <repositoryNameInXMLFile>}</td>
 * <td><b>Optional</b> - It is the name attribute in the repository element of the xml file</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author KLK1IMB
 */
@Mojo(name = "startcrawler")
public class StartCrawler extends AbstractMojo
{
   @Parameter(required = true)
   private String pathOutputFolder;
   @Parameter(required = true)
   private String outputFileName;
   @Parameter
   private String repositoryNameInXMLFile;
   @Parameter(required = true)
   private Repository repository;
   @Inject
   private LegacySupport buildContext;
   @Inject
   private RepositorySystem repositorySystem;

   @Override
   public void execute() throws MojoExecutionException
   {
      RepositorySystemSession repositorySession = buildContext.getRepositorySession();

      final ArtifactRepository artifactRepository;
      try
      {
         artifactRepository = repositorySystem.buildArtifactRepository(repository);
      }
      catch (InvalidRepositoryException e)
      {
         throw new MojoExecutionException("Unable to build artifact repository  " + repository, e);
      }

      List<ArtifactRepository> repos = Collections.singletonList(artifactRepository);
      repositorySystem.injectProxy(repositorySession, repos);
      repositorySystem.injectAuthentication(repositorySession, repos);

      final String url = artifactRepository.getUrl();

      getLog().info("---------Crawler plugin");
      getLog().info("---Starting crawler plugin for m2e update sites with the following parameters");
      getLog().info(
         "---Parameters:\nURL: \"" + url + "\"\nOutput foler: \"" + this.pathOutputFolder + "\"\noutput file name: \""
            + this.outputFileName + "\"");
      Crawler crawler;
      // Proxy settings set?
      if (artifactRepository.getProxy() == null)
      {
         crawler = new Crawler();
      }
      else
      {
         String proxyHost = artifactRepository.getProxy().getHost();
         int proxyPort = artifactRepository.getProxy().getPort();
         getLog().info("---Proxy settings:\nProxy: \"" + proxyHost + "\"\nPort: \"" + proxyPort + "\"");
         crawler = new Crawler(proxyHost, proxyPort);
      }

      try
      {
         crawler.start(url, this.pathOutputFolder, this.outputFileName, this.repositoryNameInXMLFile);
      }
      catch (IOException e)
      {
         throw new MojoExecutionException("Error while crawling for p2 repositories", e);
      }
   }
}
