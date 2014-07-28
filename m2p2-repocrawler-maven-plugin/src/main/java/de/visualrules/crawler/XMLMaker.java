/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package de.visualrules.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

/**
 * Generates a XMLFile based on the output of the MyCrawler object
 * 
 * XMLGenerator
 * 
 * @author KLK1IMB
 */
public class XMLMaker
{
   private List<String> urlsWithContentFile;
   private File saveFolder;
   private boolean urlsWithContentFileIsEmpty;


   public XMLMaker(List<String> urlsWithContentFile)
   {
      if (!urlsWithContentFile.isEmpty())
      {
         //Just save 4 items of every link group
         //e. g. 
         /* http://central.maven.org/maven2/.m2e/connectors/m2e/1.6.0/N/1.6.0.20140628-1445/ < not saved
            http://central.maven.org/maven2/.m2e/connectors/m2e/1.6.0/N/1.6.0.20140708-0459/ < not saved
            http://central.maven.org/maven2/.m2e/connectors/m2e/1.6.0/N/1.6.0.20140708-0741/ < saved
            http://central.maven.org/maven2/.m2e/connectors/m2e/1.6.0/N/1.6.0.20140709-1511/ < saved
            http://central.maven.org/maven2/.m2e/connectors/m2e/1.6.0/N/1.6.0.20140712-1411/ < saved
            http://central.maven.org/maven2/.m2e/connectors/m2e/1.6.0/N/1.6.0.20140720-0901/ < saved*/
         this.urlsWithContentFile = Collections.unmodifiableList(getLatestUrls(urlsWithContentFile, 4));
         this.urlsWithContentFileIsEmpty = false;
      }
      else
      {
         this.urlsWithContentFileIsEmpty = true;
         System.out.println("!!!XMLGenerator: XMLFile won't be created");
         System.out.println("!!!XMLGenerator: No URLs available");
      }
   }

   private List<String> getLatestUrls(List<String> urlsWithContentFile, int numberOfUrlPerGroup)
   {
      final int numberOfUrlPerGroupFinal = numberOfUrlPerGroup;
      java.util.Collections.sort(urlsWithContentFile);
      List<String> tmpSavedUrls = new ArrayList<String>();
      List<String> overallSavedUrls = new ArrayList<String>();

      String firstElement = urlsWithContentFile.get(urlsWithContentFile.size() - 1);
      tmpSavedUrls.add(firstElement);
      overallSavedUrls.add(firstElement);
      // Starts with the second element because the first is always needed
      for (int i = urlsWithContentFile.size() - 2; i >= 0; i--)
      {
         String urlToCheck = urlsWithContentFile.get(i);

         boolean urlRootIsSameAsTmpUrls = childsAreSameAsTmpUrls(urlToCheck, tmpSavedUrls);
         // If url root in "urlToCheck" differs to the urls that are saved in "tmpSavedUrls"
         if (urlRootIsSameAsTmpUrls == false && (tmpSavedUrls.size() <= numberOfUrlPerGroupFinal))
         {
            overallSavedUrls.add(urlToCheck);
            tmpSavedUrls.clear();
            tmpSavedUrls.add(urlToCheck);
         }
         // If url root in "urlToCheck" is the same than an url from the "tmpSavedUrl" list and the tmpSavedUrls list is
         // not
         // full
         else if (urlRootIsSameAsTmpUrls == true && (tmpSavedUrls.size() < numberOfUrlPerGroupFinal))
         {
            overallSavedUrls.add(urlToCheck);
            tmpSavedUrls.add(urlToCheck);
         }
      }
      return overallSavedUrls;
   }

   private boolean childsAreSameAsTmpUrls(String urlToCheck, List<String> tmpSavedUrls)
   {
      if (tmpSavedUrls.isEmpty())
      {
         return false;
      }
      else
      {
         for (int i = 0; i < tmpSavedUrls.size(); i++)
         {
            String[] splittedTmpUrl = tmpSavedUrls.get(i).split("/");
            String[] splittedUrlToCheck = urlToCheck.split("/");
            // Just compare the root
            for (int z = 0; z < splittedTmpUrl.length - 1; z++)
            {
               if (!(splittedTmpUrl[z].equals(splittedUrlToCheck[z])))
               {
                  return false;
               }
            }
         }
         return true;
      }
   }

   public void saveFoundLatestUrl(final int numberOfUrlPerGroupFinal, List<String> tmpSavedUrls,
      List<String> overallSavedUrls, String actualUrl)
   {
      if (tmpSavedUrls.size() == numberOfUrlPerGroupFinal)
      {
         overallSavedUrls.addAll(tmpSavedUrls);
      }
      else
      {
         tmpSavedUrls.add(actualUrl);
      }
   }

   public boolean generateXML(String saveFolderPath, String fileName, String repositoryNameInXmlFile)
      throws IOException
   {
      if (!this.urlsWithContentFileIsEmpty)
      {
         // Get the folder path
         this.saveFolder = new File(saveFolderPath);
         FileUtils.forceMkdir(this.saveFolder);
         if (this.saveFolder.exists())
         {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            try
            {
               DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

               // root elements
               Document doc = docBuilder.newDocument();

               ProcessingInstruction createProcessingInstruction = doc.createProcessingInstruction(
                  "compositeMetadataRepository", "");
               createProcessingInstruction.setTextContent("version=\"1.0.0\"");
               doc.appendChild(createProcessingInstruction);

               Element rootElement = doc.createElement("repository");
               doc.appendChild(rootElement);

               if (!repositoryNameInXmlFile.isEmpty())
               {
                  Attr attrRepo = doc.createAttribute("name");
                  attrRepo.setValue(repositoryNameInXmlFile);
                  rootElement.setAttributeNode(attrRepo);

               }

               Attr attrType = doc.createAttribute("type");
               attrType.setValue("org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository");

               Attr attrVersion = doc.createAttribute("version");
               attrVersion.setValue("1.0.0");


               rootElement.setAttributeNode(attrType);
               rootElement.setAttributeNode(attrVersion);

               // <children>
               Element children = doc.createElement("children");
               rootElement.appendChild(children);

               // set Attribut for <children>
               Attr attr = doc.createAttribute("size");

               // String tmpValue = new String(new Integer(this.urlsWithContentFile.size()).toString());
               String tmpValue = Integer.toString(this.urlsWithContentFile.size());
               attr.setValue(tmpValue);
               children.setAttributeNode(attr);

               // <child>
               for (String url : this.urlsWithContentFile)
               {
                  Element child = doc.createElement("child");
                  children.appendChild(child);

                  Attr attrChild = doc.createAttribute("location");
                  attrChild.setValue(url);
                  child.setAttributeNode(attrChild);
               }

               // write the content into xml file
               TransformerFactory transformerFactory = TransformerFactory.newInstance();
               Transformer transformer = transformerFactory.newTransformer();
               transformer.setOutputProperty(OutputKeys.INDENT, "yes");

               transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
               // For OutputPropertiesFactory you need to add org.apache.xml.serializer
               // transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT,"3");
               DOMSource source = new DOMSource(doc);
               File tmpFile = new File(saveFolderPath, fileName);
               StreamResult result = new StreamResult(tmpFile);

               // Output to console for testing
               // StreamResult result = new StreamResult(System.out);

               transformer.transform(source, result);
               System.out.println("-----------------------------");
               System.out.println("-----------------------------");
               System.out.println("File saved! Path: " + tmpFile.getAbsolutePath());

               return true;
            }
            catch (ParserConfigurationException e)
            {
               e.printStackTrace();
               return false;
            }
            catch (TransformerConfigurationException e)
            {
               e.printStackTrace();
               return false;
            }
            catch (TransformerException e)
            {
               e.printStackTrace();
               return false;
            }
         }
         else
         {
            System.out.println("!!!XMLMaker: Save folder does not exist");
            return false;
         }
      }
      else
      {
         return false;
      }
   }

}
