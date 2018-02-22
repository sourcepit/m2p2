
package org.sourcepit.m2p2.director;

import org.apache.maven.plugins.annotations.Parameter;

public class TargetEnvironment
{
   // -p2.os linux
   @Parameter
   private String os;

   // -p2.ws gtk
   @Parameter
   private String ws;

   // -p2.arch x86
   @Parameter
   private String arch;

   public String getOs()
   {
      return os;
   }

   public void setOs(String os)
   {
      this.os = os;
   }

   public String getWs()
   {
      return ws;
   }

   public void setWs(String ws)
   {
      this.ws = ws;
   }

   public String getArch()
   {
      return arch;
   }

   public void setArch(String arch)
   {
      this.arch = arch;
   }

   @Override
   public String toString()
   {
      final StringBuilder str = new StringBuilder();
      if (os != null)
      {
         str.append(os);
      }
      if (ws != null)
      {
         if (str.length() != 0)
         {
            str.append('.');
         }
         str.append(ws);
      }
      if (arch != null)
      {
         if (str.length() != 0)
         {
            str.append('.');
         }
         str.append(arch);
      }
      if (str.length() == 0)
      {
         str.append("default");
      }
      return str.toString();
   }


}
