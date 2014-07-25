/*
 * Copyright (C) 2014 Bosch Software Innovations GmbH. All rights reserved.
 */

package de.visualrules.crawler;

import java.io.File;
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
         this.urlsWithContentFile = urlsWithContentFile;
         this.urlsWithContentFileIsEmpty = false;
      }
      else
      {
         this.urlsWithContentFileIsEmpty = true;
         System.out.println("!!!XMLGenerator: XMLFile won't be created");
         System.out.println("!!!XMLGenerator: No URLs available");
      }
   }

   public boolean generateXML(String saveFolderPath, String fileName, String repositoryNameInXmlFile)
   {
      if (!this.urlsWithContentFileIsEmpty)
      {
         // Get the folder path
         this.saveFolder = new File(saveFolderPath);
         createSaveFolder(this.saveFolder);
         if (this.saveFolder.exists())
         {
            this.generateFilePath(saveFolderPath, fileName);

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

               String tmpValue = new String(new Integer(this.urlsWithContentFile.size()).toString());
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
               File tmpFile = this.generateFilePath(saveFolderPath, fileName);
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

   private void createSaveFolder(File saveFolder)
   {
      if(!this.saveFolder.exists())
      {
         if(!this.saveFolder.mkdir())
         {
            System.out.println("!!!XMLMaker: Can't create the save folder: "+saveFolder.getAbsolutePath());
         }
      }
   }

   private File generateFilePath(String saveFolderPath, String fileName)
   {
      if (!saveFolderPath.endsWith("/"))
      {
         saveFolderPath += "/";
      }

      return new File(saveFolderPath + fileName);
   }

}
