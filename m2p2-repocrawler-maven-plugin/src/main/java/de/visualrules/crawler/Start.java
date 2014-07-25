/*
 * Copyright (C) 2014 Bosch Software Innovations GmbH. All rights reserved.
 */

package de.visualrules.crawler;

public class Start
{
   public static void main(String[] args)
   {
      Crawler crawler = new Crawler("cache", 3128);
      crawler.start("http://central.maven.org/maven2/.m2e/connectors/m2e/1.3.0/N/1.3.0.20121201-0238/", "D:\\xmlfiles", "test.xml", "");
   }
}
