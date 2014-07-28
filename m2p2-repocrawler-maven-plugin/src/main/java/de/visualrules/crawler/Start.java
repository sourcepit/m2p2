/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package de.visualrules.crawler;

import java.io.IOException;

public class Start
{
   public static void main(String[] args) throws IOException
   {
      Crawler crawler = new Crawler("cache", 3128);
      crawler.start("http://central.maven.org/maven2/.m2e/connectors/m2e/", "D:\\xmlfiles", "test.xml", "");
   }
}
