/**
 * Copyright (c) 2014 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.m2p2.director;

import static java.nio.charset.Charset.defaultCharset;
import static org.sourcepit.common.utils.io.IO.buffIn;
import static org.sourcepit.common.utils.io.IO.buffOut;
import static org.sourcepit.common.utils.io.IO.fileIn;
import static org.sourcepit.common.utils.io.IO.fileOut;
import static org.sourcepit.common.utils.io.IO.read;
import static org.sourcepit.common.utils.io.IO.write;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.sourcepit.common.utils.io.Read.FromStream;
import org.sourcepit.common.utils.io.Write.ToStream;

public final class EclipseIniUtil
{
   private EclipseIniUtil()
   {
      super();
   }

   public static File getFile(File dir, EclipseIni eclipseIni)
   {
      final String fileName = eclipseIni.getFileName() == null ? "eclipse.ini" : eclipseIni.getFileName();
      return new File(dir, fileName);
   }

   public static void applyDefaults(File dir, EclipseIni eclipseIni, String defaultEncoding) throws IOException
   {
      if (eclipseIni.getFileName() == null)
      {
         eclipseIni.setFileName("eclipse.ini");
      }

      if (eclipseIni.getEncoding() == null)
      {
         eclipseIni.setEncoding(defaultEncoding == null ? defaultCharset().name() : defaultEncoding);
      }

      if (eclipseIni.getEOL() == null)
      {
         eclipseIni.setEOL(detectLineSeparator(getFile(dir, eclipseIni), eclipseIni.getEncoding()));
      }
   }

   private static String detectLineSeparator(File file, final String encoding) throws IOException
   {
      return read(new FromStream<String>()
      {
         @Override
         public String read(InputStream in) throws Exception
         {
            final Reader reader = new InputStreamReader(in, encoding);

            char current = (char) reader.read();
            while (current > -1)
            {
               if ((current == '\n') || (current == '\r'))
               {
                  final StringBuilder sb = new StringBuilder(2);
                  sb.append(current);
                  char next = (char) reader.read();
                  if ((next == '\r') || (next == '\n'))
                  {
                     sb.append(next);
                  }
                  return sb.toString();
               }
               current = (char) reader.read();
            }

            return null;
         }
      }, buffIn(fileIn(file)));
   }

   public static void parse(File dir, EclipseIni eclipseIni, List<String> appArgs, List<String> vmArgs)
      throws IOException
   {
      final File file = getFile(dir, eclipseIni);
      if (file.exists())
      {
         final List<String> lines = readLines(dir, eclipseIni);
         List<String> args = appArgs;
         for (String line : lines)
         {
            if ("-vmargs".equals(line))
            {
               args = vmArgs;
               continue;
            }
            args.add(line);
         }
      }
   }

   private static List<String> readLines(File dir, final EclipseIni eclipseIni) throws IOException
   {
      return read(new FromStream<List<String>>()
      {
         @Override
         public List<String> read(InputStream in) throws IOException
         {
            final BufferedReader br = new BufferedReader(new InputStreamReader(in, eclipseIni.getEncoding()));
            final List<String> list = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null)
            {
               if (line.trim().length() > 0)
               {
                  list.add(line);
               }
            }
            return list;

         }
      }, fileIn(getFile(dir, eclipseIni)));
   }

   public static void save(File dir, final EclipseIni eclipseIni, final List<String> appArgs, final List<String> vmArgs)
      throws IOException
   {
      write(new ToStream<Void>()
      {
         @Override
         public void write(OutputStream out, Void content) throws IOException
         {
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, eclipseIni.getEncoding()));
            for (String arg : appArgs)
            {
               writer.write(arg);
               writer.write(eclipseIni.getEOL());
            }
            if (!vmArgs.isEmpty())
            {
               writer.write("-vmargs");
               writer.write(eclipseIni.getEOL());
               for (String arg : vmArgs)
               {
                  writer.write(arg);
                  writer.write(eclipseIni.getEOL());
               }
            }
            writer.flush();
         }
      }, buffOut(fileOut(getFile(dir, eclipseIni))), null);
   }
}
