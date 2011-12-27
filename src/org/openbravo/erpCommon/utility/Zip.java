/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * Zips a file/directory
 * 
 */
public class Zip {
  private static Logger log4j = Logger.getLogger(Zip.class);

  private static void zip(File[] list, ZipOutputStream dest, String relativeDir) throws Exception {
    for (int i = 0; i < list.length; i++) {
      if (list[i].isDirectory()) {
        zip(list[i], dest, relativeDir);
      } else {
        byte[] buf = new byte[1024];
        dest.putNextEntry(new ZipEntry(list[i].toString().replace(relativeDir, "")));
        FileInputStream in = new FileInputStream(list[i].toString());
        int len;
        while ((len = in.read(buf)) > 0) {
          dest.write(buf, 0, len);
        }
        dest.closeEntry();
        in.close();
      }
    }
  }

  private static void zip(File orig, ZipOutputStream dest, String relativeDir) throws Exception {
    File[] list = orig.listFiles(new FilenameFilter() {
      public boolean accept(File f, String s) {
        return !s.equals(".svn");
      }
    });

    zip(list, dest, relativeDir);
  }

  public static void zip(File[] list, String dest, String relativeDir) throws Exception {
    ZipOutputStream destZip = new ZipOutputStream(new FileOutputStream(dest));
    relativeDir = (relativeDir + (relativeDir.endsWith("/") ? "" : "/")).replace("/",
        File.separator);
    zip(list, destZip, relativeDir);
    destZip.close();
    log4j.info("zipped in " + dest);
  }

  public static void zip(String orig, String dest, String relativeDir) throws Exception {
    File file = new File(orig);
    relativeDir = (relativeDir + (relativeDir.endsWith("/") ? "" : "/")).replace("/",
        File.separator);
    ZipOutputStream destZip = new ZipOutputStream(new FileOutputStream(dest));
    zip(file, destZip, relativeDir);
    destZip.close();
    log4j.info("zipped " + orig + " in " + dest);
  }

  public static void zip(String orig, String dest) throws Exception {
    zip(orig, dest, orig);
  }

  public static void unzip(InputStream orig, String dest) throws Exception {
    ZipInputStream zipInputStream = new ZipInputStream(orig);
    ZipEntry entry = null;
    while ((entry = zipInputStream.getNextEntry()) != null) {
      String fileName = dest + "/" + entry.getName();
      File entryFile = new File(fileName);
      // Check whether the directory exists, if not create
      if (entryFile.getParent() != null) {
        File dir = new File(entryFile.getParent());
        if (!dir.exists()) {
          log4j.info("Created dir: " + dir.getAbsolutePath());
          dir.mkdirs();
        }
      }
      // Unzip the file
      log4j.info("unzipping " + fileName);
      FileOutputStream fout = new FileOutputStream(entryFile);
      byte[] buf = new byte[1024];
      int len;
      while ((len = zipInputStream.read(buf)) > 0) {
        fout.write(buf, 0, len);
      }

      fout.close();
      zipInputStream.closeEntry();
    }
    zipInputStream.close();
  }

  public static void unzip(String orig, String dest) throws Exception {
    unzip(new FileInputStream(orig), dest);
  }

  public static void main(String[] args) {
    String obDir = "/ws/modularity/openbravo";
    File file[] = new File[10];
    file[0] = new File(obDir + "/legal");
    file[1] = new File(obDir + "/lib");
    file[2] = new File(obDir + "/modules");
    file[3] = new File(obDir + "/src-core");
    file[4] = new File(obDir + "/src-db");
    file[5] = new File(obDir + "/src-gen");
    file[6] = new File(obDir + "/src-trl");
    file[7] = new File(obDir + "/src-wad");
    file[8] = new File(obDir + "/src");
    file[9] = new File(obDir + "/web");
    log4j.info("Zipping core...");
    try {
      Zip.zip(file, obDir + "/backup_install/core.zip", obDir + "/");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
