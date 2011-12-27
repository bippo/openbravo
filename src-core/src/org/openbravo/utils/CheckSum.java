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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

public class CheckSum {
  private String obDir;
  private Properties properties;

  public CheckSum(String dir) {
    obDir = dir.replace("\\", "/");
    if (!obDir.endsWith("/"))
      obDir += "/";
    properties = new Properties();
    File propertiesFile = new File(obDir + "/config/checksums");
    if (propertiesFile.exists()) {
      try {
        properties.load(new FileInputStream(propertiesFile));
      } catch (Exception e) {
        // do nothing, just do not read properties
      }
    }
  }

  private void getCheckSum(MessageDigest cs, File f) throws Exception {
    if (f.isDirectory()) {
      File[] list = f.listFiles(new FilenameFilter() {
        public boolean accept(File file, String s) {
          return !s.equals(".svn") && !s.endsWith(".orig");
        }
      });

      // sort files alphabetically to ensure the order does not change from one checksum to another
      Arrays.sort(list, new Comparator<File>() {
        public int compare(final File f1, final File f2) {
          return f1.toString().compareTo(f2.toString());
        }
      });

      for (File element : list)
        getCheckSum(cs, element);
    } else {

      FileInputStream is = new FileInputStream(f);
      byte[] bytes = new byte[1024];
      int len = 0;

      // Checksum file directly
      while ((len = is.read(bytes)) >= 0) {
        cs.update(bytes, 0, len);
      }
      is.close();
    }
  }

  private String getCheckSum(String[] files) throws Exception {
    MessageDigest cs = MessageDigest.getInstance("MD5");
    for (String fileName : files) {
      File file = new File(fileName);
      if (file.exists())
        getCheckSum(cs, file);
    }
    return new BigInteger(1, cs.digest()).toString(16);
  }

  private String[] getFiles(String type) {
    if (type.equals("md5.db.all")) {
      String rt[] = { obDir + "src-db/database/model", obDir + "src-db/database/sourcedata" };
      return rt;
    } else if (type.equals("md5.db.structure")) {
      String rt[] = { obDir + "src-db/database/model", obDir + "modules" };
      return rt;
    } else if (type.equals("md5.db.sourcedata")) {
      String rt[] = { obDir + "src-db/database/sourcedata", obDir + "modules" };
      return rt;
    } else if (type.equals("md5.wad")) {
      String rt[] = { obDir + "src-wad/src" };
      return rt;
    } else if (type.equals("md5.skins")) {
      String rt[] = { obDir + "web/skins" };
      return rt;
    } else {
      String rt[] = new String[0];
      return rt;
    }
  }

  private void saveCheckSum() throws Exception {

    FileOutputStream file = new FileOutputStream(obDir + "/config/checksums");
    properties.store(file, "Checksums for build tasks comparation");
  }

  public String calculateCheckSum(String type) {
    try {
      String[] files = getFiles(type);
      String checkSum = getCheckSum(files);
      properties.setProperty(type, checkSum);
      saveCheckSum();
      return checkSum;
    } catch (Exception e) {
      e.printStackTrace();
      return "0";
    }
  }

  public String calculateCheckSumWithoutSaving(String type) {
    try {
      String[] files = getFiles(type);
      String checkSum = getCheckSum(files);
      return checkSum;
    } catch (Exception e) {
      e.printStackTrace();
      return "0";
    }
  }

  public String calculateCheckSumDBStructure() {
    return calculateCheckSum("md5.db.structure");
  }

  public String calculateCheckSumDBSourceData() {
    return calculateCheckSum("md5.db.sourcedata");
  }

  public String calculateCheckSumWad() {
    return calculateCheckSum("md5.wad");
  }

  public String calculateCheckSkins() {
    return calculateCheckSum("md5.sinks");
  }

  public String getCheckSumDBSTructure() {
    return properties.getProperty("md5.db.structure", "0");
  }

  public String getCheckSumDBSourceData() {
    return properties.getProperty("md5.db.sourcedata", "0");
  }

  public String getCheckSumWad() {
    return properties.getProperty("md5.wad", "0");
  }

  public String getCheckSum(String type) {
    return properties.getProperty(type, "0");
  }

  public String getCheckSumDatabase() {
    return calculateCheckSum("md5.db.all");
  }

  public static void main(String[] args) {
    CheckSum cs = new CheckSum("/home/openbravo/ws/trunk/openbravo");
    cs.calculateCheckSum("md5.skins");
  }

}
