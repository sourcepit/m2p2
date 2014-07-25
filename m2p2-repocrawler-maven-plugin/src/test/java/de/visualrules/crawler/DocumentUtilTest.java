/*
 * Copyright (C) 2014 Bosch Software Innovations GmbH. All rights reserved.
 */

package de.visualrules.crawler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import de.visualrules.crawler.DocumentUtil;

/**
 * Checks if a url is a hierachical child of the baseURL
 * 
 * @author KLK1IMB
 */
public class DocumentUtilTest
{

   @Test
   public void testIsHierachicalChildURL()
   {
      String baseURL = "http://foo.com/";
      String url = "http://foo.com/hui.htm";
      assertTrue(DocumentUtil.isHierachicalChildURL(baseURL, url));
      assertTrue(DocumentUtil.isHierachicalChildURL(getElement(baseURL, url)));

      baseURL = "http://example.com/";
      url = "http://example.com/hufsadfi.htm";
      assertTrue(DocumentUtil.isHierachicalChildURL(baseURL, url));
      assertTrue(DocumentUtil.isHierachicalChildURL(getElement(baseURL, url)));
      
      baseURL = "http://central.maven.org/maven2/.m2e/connectors/m2e";
      url = "http://central.maven.org/maven2/.m2e/connectors/m2e/1.3.0/";
      assertTrue(DocumentUtil.isHierachicalChildURL(baseURL, url));
      assertTrue(DocumentUtil.isHierachicalChildURL(getElement(baseURL, url)));

      baseURL = "http://example123.com/";
      url = "http://example.com/hufsadfi.htm";
      assertFalse(DocumentUtil.isHierachicalChildURL(baseURL, url));
      assertFalse(DocumentUtil.isHierachicalChildURL(getElement(baseURL, url)));

      baseURL = "http://central.maven.org/maven2/.m2e/connectors/m2e";
      url = "http://example.com/hufsadfi.htm";
      assertFalse(DocumentUtil.isHierachicalChildURL(baseURL, url));
      assertFalse(DocumentUtil.isHierachicalChildURL(getElement(baseURL, url)));

      try{
         DocumentUtil.isHierachicalChildURL("", null);
         fail("No NullPointerException were thrown");
      }
      catch (NullPointerException e)
      {
         
      }
   }

   private Element getElement(String baseURL, String url)
   {
      Document doc = mock(Document.class);
      when(doc.location()).thenReturn(baseURL);

      Element elem = mock(Element.class);
      when(elem.ownerDocument()).thenReturn(doc);
      when(elem.absUrl("href")).thenReturn(url);

      return elem;
   }

}
