/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package de.visualrules.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

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
   private List<String> urlsWithConentFileLatestUrls;
   private File saveFolder;
   private boolean urlsWithContentFileIsEmpty;


   public XMLMaker(List<String> urlsWithContentFile)
   {
      if (!urlsWithContentFile.isEmpty())
      {
         this.urlsWithConentFileLatestUrls = getLatestUrls(urlsWithContentFile, 3);
         for (String string : urlsWithConentFileLatestUrls)
         {
            System.out.println("latest: " + string);
         }
         this.urlsWithContentFile = urlsWithContentFile;
         List<String> unmodifiableUrlsWithContentFile = Collections.unmodifiableList(this.urlsWithConentFileLatestUrls);
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
      //Starts with the second element because the first is always needed 
      for (int i = urlsWithContentFile.size() - 2; i >= 0; i--)
      {
         String urlToCompare;
         // Last element?
         if (i != 0)
         {
            urlToCompare = urlsWithContentFile.get(i - 1);
         }
         else
         {
            break;
         }
         String urlToCheck = urlsWithContentFile.get(i);

         boolean urlRootIsSameAsTmpUrls = childsAreSameAsOneTmpUrl(urlToCheck, tmpSavedUrls);
         //If url root in "urlToCheck" differs to the "urlToCompare" string and the tmpSavedUrls list is full
         if (urlRootIsSameAsTmpUrls == false && (tmpSavedUrls.size() == numberOfUrlPerGroupFinal))
         {
            overallSavedUrls.add(urlToCheck);
            tmpSavedUrls.clear();
            tmpSavedUrls.add(urlToCheck);
         }
         //If url root in "urlToCheck" differs to the "urlToCompare" string and tmpSavedUrls is not full 
         else if (urlRootIsSameAsTmpUrls == false && (tmpSavedUrls.size() < numberOfUrlPerGroupFinal))
         {
            overallSavedUrls.add(urlToCheck);
            tmpSavedUrls.add(urlToCheck);
         }
         //If url root in "urlToCheck" is the same than in the "urlToCompare" string and the tmpSavedUrls list is not full
         else if (urlRootIsSameAsTmpUrls == true && (tmpSavedUrls.size() < numberOfUrlPerGroupFinal))
         {
            overallSavedUrls.add(urlToCheck);
            tmpSavedUrls.add(urlToCheck);
         }
      }
      return overallSavedUrls;
   }

   private boolean childsAreSameAsOneTmpUrl(String urlToCheck, List<String> tmpSavedUrls)
   {
      if (tmpSavedUrls.isEmpty())
      {
         return false;
      }
      else
      {
         String[] splittedTmpUrl = tmpSavedUrls.get(0).split("/");
         String[] splittedUrlToCheck = urlToCheck.split("/");
         // Just compare the root
         for (int i = 0; i < splittedTmpUrl.length - 1; i++)
         {
            if (!(splittedTmpUrl[i].equals(splittedUrlToCheck[i])))
            {
               return false;
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
            new File(saveFolderPath, fileName);

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
