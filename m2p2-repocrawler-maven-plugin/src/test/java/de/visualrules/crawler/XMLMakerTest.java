/*
 * Copyright (C) 2014 Bosch Software Innovations GmbH. All rights reserved.
 */

package de.visualrules.crawler;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Creates a tempory folder and file. Generates several fake urls which looks like the following:
 * "http://www.fakeUrl.de/test/[i]". <br>
 * [i] = an int value that will be incremented. <br>
 * These urls will be handed over to the XMLMaker. After the XMLMaker created the xml files with the generated urls, the
 * method <code>checkElementsOfXMLFile</code> will check if the urls exist in the xml file.
 * 
 * 
 * @author KLK1IMB
 */
public class XMLMakerTest
{
   @Rule
   public TemporaryFolder tmpFolder = new TemporaryFolder();

   @Test
   public void testXMLMaker() throws ParserConfigurationException, SAXException, IOException
   {
      File generatedXMLTmpFile = this.tmpFolder.newFile();
      String generatedXMLTmpFolder = generatedXMLTmpFile.getParent();
      String generatedXMLTmpFileName = generatedXMLTmpFile.getName();
      String repoName = "repo";
      List<String> generatedUrls = generateFakeUrls(20);

      XMLMaker xmlMaker = new XMLMaker(generatedUrls);
      assertTrue("XML File can't be generated",
         xmlMaker.generateXML(generatedXMLTmpFolder, generatedXMLTmpFileName, repoName));
      checkElementsOfXMLFile(generatedUrls, generatedXMLTmpFolder, generatedXMLTmpFileName, repoName);
   }

   private List<String> generateFakeUrls(int countOfUrls)
   {
      List<String> generatedUrls = new ArrayList<String>();
      for (int i = 0; i <= countOfUrls; i++)
      {
         generatedUrls.add("http://www.fakeUrl.de/test/" + i);
      }

      return generatedUrls;
   }

   private void checkElementsOfXMLFile(List<String> foundURLs, String saveFolderPath, String fileName,
      String repositoryName) throws ParserConfigurationException, SAXException, IOException
   {
      File file = new File(saveFolderPath + File.separator + fileName);
      assertTrue("File \"" + file + "\" does not exist", file.exists());

      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder;
      docBuilder = docFactory.newDocumentBuilder();
      // root elements
      Document doc = docBuilder.parse(file);
      NodeList nodes;

      // Check if repo name exists
      if (repositoryName != null)
      {
         if (!repositoryName.isEmpty())
         {
            nodes = doc.getElementsByTagName("repository");
            for (int i = 0; i < nodes.getLength(); i++)
            {
               Node node = nodes.item(i);
               NamedNodeMap attributes = node.getAttributes();
               // attributes.item[0] = "name" attribute in xml element repository
               assertEquals(repositoryName, attributes.item(0).getNodeValue());
            }
         }
      }

      nodes = doc.getElementsByTagName("child");
      //Check if number of xml elements and number of generated urls are the same
      assertEquals("Number of URLs are unequal",nodes.getLength(), foundURLs.size());
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         NamedNodeMap attributes = node.getAttributes();
         // attributes.item[0] = "name" attribute in xml element repository
         String attributeURL = attributes.item(0).getNodeValue();
         assertTrue("Element "+attributeURL+" not found",checkIfElementAvailable(attributeURL, foundURLs));
      }
   }

   private boolean checkIfElementAvailable(String attribute, List<String> foundURLs)
   {
      for (String url : foundURLs)
      {
         if (url.equals(attribute))
         {
            return true;
         }
      }
      return false;
   }
}
