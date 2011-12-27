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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.translate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author David Alsasua
 * 
 *         Performs required modifications in order to convert a skin to use it with RTL languages
 **/
public class RTLSkin {
  public static final String VERSION = "V1.0";
  static int count = 0;
  private static String srcDirRTLSkin = "";
  private static String srcDirLTRSkin = "";
  private static String srcDirOrig = "";
  private static Vector<String> vImagesToFlip;

  static Logger log4j = Logger.getLogger(Translate.class);

  /**
   * Command Line method.
   * 
   * @param argv
   *          List of arguments: 0- Path where are the skin files to modify (RTL folder of the
   *          skin). 1- Path where are the skin files to modify (LTR folder of the skin).
   * @throws Exception
   */
  public static void main(String argv[]) throws Exception {
    PropertyConfigurator.configure("log4j.lcf");

    if (argv.length != 2) {
      log4j.error("Usage: java RTLSkin pathRTLSkinFolder pathLTRSkinFolder");
      for (int i = 0; i < argv.length; i++)
        log4j.error(i + "- " + argv[i]);
      return;
    }
    srcDirRTLSkin = argv[0];
    srcDirLTRSkin = argv[1];

    log4j.info("RTL Skin directory source: " + srcDirRTLSkin);
    log4j.info("LTR Skin directory source: " + srcDirLTRSkin);

    File fileRTLSrc = new File(srcDirRTLSkin, "");
    if (!fileRTLSrc.exists()) {
      log4j.error("Can´t find directory: " + srcDirRTLSkin);
      return;
    }

    File fileLTRSrc = new File(srcDirLTRSkin, "");
    if (!fileLTRSrc.exists()) {
      log4j.error("Can´t find directory: " + srcDirLTRSkin);
      return;
    }

    String[] files = fileRTLSrc.list();
    File fImagesToFlip;
    BufferedReader in;
    String line;
    File fileLTRSrcDeep, fileRTLSrcDeep;

    for (int i = 0; i < files.length; i++) {
      String pathModule = "";
      log4j.info("Processing RTL skin " + files[i]);
      vImagesToFlip = new Vector<String>();
      fImagesToFlip = new File(srcDirRTLSkin + "/" + files[i] + "/RTLFlippedImages.txt");
      if (!fImagesToFlip.exists()) {
        // Not founded the file with images to flip. Is it a module
        // skin?
        File strDirModule = new File(srcDirRTLSkin + "/" + files[i], "");
        if (strDirModule.list().length == 1 && strDirModule.isDirectory()) {
          fImagesToFlip = new File(srcDirRTLSkin + "/" + files[i] + "/" + strDirModule.list()[0]
              + "/RTLFlippedImages.txt");
          pathModule = srcDirRTLSkin + "/" + files[i] + "/" + strDirModule.list()[0];
        } else {
          log4j.error("Not Founded File with images to flip! " + srcDirRTLSkin + "/" + files[i]
              + "/RTLFlippedImages.txt");
          log4j.error("Continuing with the rest of the skins.");
          continue;
        }
      }

      try {
        in = new BufferedReader(new FileReader(fImagesToFlip));
      } catch (java.io.FileNotFoundException e) {
        // Not founded the file with images to flip.
        log4j.error("Not Founded File with images to flip! " + e);
        log4j.error("Continuing with the rest of the skins.");
        continue;
      }

      while ((line = in.readLine()) != null) {
        vImagesToFlip.add(line.toLowerCase());
      }
      in.close();

      log4j.info("Processing RTL skin " + files[i]);
      fileRTLSrcDeep = new File(srcDirRTLSkin + "/" + files[i], "");
      runFolders(fileRTLSrcDeep, files[i], "", "RTL");
    }

    files = fileLTRSrc.list();

    for (int i = 0; i < files.length; i++) {
      log4j.info("Processing LTR skin " + files[i]);
      fileLTRSrcDeep = new File(srcDirLTRSkin + "/" + files[i], "");
      runFolders(fileLTRSrcDeep, files[i], "", "LTR");
    }

    log4j.info("Modified files: " + count);
  }

  /**
   * Runs through all the files of the specified path reads and, if appropriate, modifies the file.
   * 
   * @param srcDir
   *          Path where are the files to modify (rtl folder of the skin).
   * @param strSkinFolderName
   *          Path to the folder that contains the original skin -where no change will be
   *          performed-.
   * @param relativePath
   *          Relative Path.
   * @param folderContentType
   *          whether skin or javascript folder is passed in srcDir
   */
  public static void runFolders(File srcDir, String strSkinFolderName, String relativePath,
      String folderContentType) {

    File[] list = srcDir.listFiles();

    for (int i = 0; i < list.length; i++) {
      File fileItem = list[i];
      if (fileItem.isDirectory()) {
        String prevRelativePath = new String(relativePath);
        relativePath += "/" + fileItem.getName();
        runFolders(fileItem, strSkinFolderName, relativePath, folderContentType);
        relativePath = prevRelativePath;
      } else {
        try {
          if (log4j.isDebugEnabled())
            log4j.debug(list[i] + " Parent: " + fileItem.getParent() + " getName() "
                + fileItem.getName() + " canonical: " + fileItem.getCanonicalPath());
          if (folderContentType == "RTL") {
            count++;
            modifySkin(list[i]);
            if (list[i].toString().toLowerCase().endsWith("css"))
              applySpriteTags(list[i], strSkinFolderName, "RTL");
          } else if (folderContentType == "LTR") {
            count++;
            if (list[i].toString().toLowerCase().endsWith("css"))
              applySpriteTags(list[i], strSkinFolderName, "LTR");
          }
        } catch (Exception e) {
          log4j.error("IOException: " + e);
        }
      }
    }
  }

  /**
   * Modifies text of sprite tags
   * 
   * @param file
   *          File to be modified
   * @param skinName
   *          Name of the skin
   * @param textDirection
   *          RTL or LTR
   */
  public static void applySpriteTags(File file, String skinName, String textDirection)
      throws Exception {
    BufferedReader in = new BufferedReader(new FileReader(file));
    Vector<String> vLines = new Vector<String>();
    String line, fileName, midLine = "";
    fileName = file.toString().substring(file.toString().lastIndexOf("/") + 1,
        file.toString().length() - 4);
    int initLTR, initRTL, midLTR, midRTL, endLTR, endRTL;

    while ((line = in.readLine()) != null) {
      if (line.indexOf("xxspritexx") != -1) {

        line = line.replace("xxspritexx", fileName + "_" + skinName + "_" + textDirection
            + "_Sprite")
            + "\n";
        midLine = "";
        initLTR = line.indexOf("sprite-alignment-ltr");
        midLTR = line.indexOf(":", initLTR);
        endLTR = line.indexOf(";", initLTR);
        initRTL = line.indexOf("sprite-alignment-rtl");
        midRTL = line.indexOf(":", initRTL);
        endRTL = line.indexOf(";", initRTL);

        if (initRTL != -1 || initLTR != -1) {
          if (initRTL == -1) {
            initRTL = line.length();
          } else if (textDirection.equals("RTL")) {
            midLine = line.substring(initRTL, midRTL - 4) + ":"
                + line.substring(midRTL + 1, endRTL) + ";";
          }
          if (initLTR == -1) {
            initLTR = line.length();
          } else if (textDirection.equals("LTR")) {
            midLine = line.substring(initLTR, midLTR - 4) + ":"
                + line.substring(midLTR + 1, endLTR) + ";";
          }

          line = line.substring(0, Math.min(initLTR, initRTL)) + midLine
              + line.substring(Math.max(endRTL, endLTR) + 1, line.length());
        }
        vLines.add(line + "\n");
      } else {
        vLines.add(line + "\n");
      }
    }
    in.close();

    BufferedWriter out = new BufferedWriter(new FileWriter(file));
    for (String lineToWrite : vLines) {
      out.write(lineToWrite);
    }
    out.close();
  }

  /**
   * Perform the necessary changes to a skin file
   * 
   * @param file
   *          File to be modified
   */
  public static void modifySkin(File file) {
    if (file.toString().toLowerCase().endsWith("png")
        || file.toString().toLowerCase().endsWith("gif")) {
      // Actions to be taken when a png or gif file is found
      // check if image must be flipped or not
      for (String fileName : vImagesToFlip) {
        if (file.toString().toLowerCase().indexOf(fileName) != -1) {
          try {
            FlipImage.proceed(file.toString());
          } catch (Exception e) {
            log4j.error("RTLSkin.modifySkin error in file " + file.toString() + ": " + e);
          }
        }
      }
    }
    if (file.toString().toLowerCase().endsWith("css")) {
      // Actions with css files
      if (file.toString().indexOf("Openbravo_ERP_") != -1) {
        try {
          addLine(file, "html {direction:rtl;}");
        } catch (Exception e) {
          log4j.error("RTLSkin.modifySkin error in file " + file.toString() + ": " + e);
        }
      }
      try {
        processCSS(file);
      } catch (Exception e) {
        log4j.error("RTLSkin.modifySkin error in file " + file.toString() + ": " + e);
      }
    }
  }

  /**
   * Performs the required changes to a css file.
   * 
   * @param file
   *          CSS file to be modified
   */
  public static void processCSS(File file) throws Exception {
    BufferedReader in = new BufferedReader(new FileReader(file));
    Vector<String> vLines = new Vector<String>();
    String line;
    int init, end;

    while ((line = in.readLine()) != null) {
      if ((init = line.indexOf("/*~RTL ")) != -1) {
        end = line.indexOf("*/");
        if (end == -1)
          end = line.length();
        vLines.add(line.substring(init + 7, end) + "\n");
      } else {
        vLines.add(line + "\n");
      }
    }
    in.close();

    BufferedWriter out = new BufferedWriter(new FileWriter(file));
    for (String lineToWrite : vLines) {
      out.write(lineToWrite);
    }
    out.close();
  }

  /**
   * Adds a line at the end of a text file.
   * 
   * @param file
   *          File where line will be added at the end.
   * @param lineToAdd
   *          Line to be added to the file.
   */
  public static void addLine(File file, String lineToAdd) throws Exception {
    BufferedReader in = new BufferedReader(new FileReader(file));
    Vector<String> vLines = new Vector<String>();
    String line;

    while ((line = in.readLine()) != null) {
      vLines.add(line + "\n");
    }
    in.close();

    BufferedWriter out = new BufferedWriter(new FileWriter(file));
    for (String lineToWrite : vLines) {
      out.write(lineToWrite);
    }
    out.write(lineToAdd);
    out.close();
  }

}
