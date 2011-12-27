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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.translate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xerces.parsers.SAXParser;
import org.openbravo.database.CPStandAlone;
import org.openbravo.utils.DirFilter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Fernando Iriazabal
 * 
 *         Translate the HTML file of the folder especified
 **/
public class Translate extends DefaultHandler implements LexicalHandler {
  protected static CPStandAlone pool;
  static XMLReader parser;
  static TranslateData[] toLanguage;
  static String actualLanguage;
  static String fileTermination;

  static boolean isHtml = false;

  static String actualTag;
  static String actualFile;
  static String actualPrefix;
  static StringBuffer translationText;
  static int count = 0;
  static ArrayList<String> moduleDirectories;
  static String moduleName = "";
  static String moduleLang = "";
  static String moduleID = "";
  static boolean translateModule = true;
  static final String[] tokens = { "-", ":" };

  static Logger log4j = Logger.getLogger(Translate.class);

  /**
   * Constructor
   * 
   * @param xmlPoolFile
   *          Path to the Openbravo.properties file.
   * @throws ServletException
   */
  public Translate(String xmlPoolFile) throws ServletException {
    pool = new CPStandAlone(xmlPoolFile);
  }

  /**
   * Constructor
   * 
   * @param xmlPoolFile
   *          Path to the Openbravo.properties file.
   * @param _fileTermination
   *          File extension to filter.
   * @throws ServletException
   */
  public Translate(String xmlPoolFile, String _fileTermination) throws ServletException {
    this(xmlPoolFile);
    fileTermination = _fileTermination;
    isHtml = fileTermination.toLowerCase().endsWith("html");
    if (isHtml)
      parser = new org.cyberneko.html.parsers.SAXParser();
    else
      parser = new SAXParser();
    parser.setEntityResolver(new LocalEntityResolver());
    parser.setContentHandler(this);
    toLanguage = TranslateData.systemLanguage(pool);
    if (toLanguage.length == 0)
      log4j.warn("No system languages defined, translation will parse all files.");
  }

  /**
   * Command Line method.
   * 
   * @param argv
   *          List of arguments. There is 2 call ways, with 2 arguments; the first one is the
   *          attribute to indicate if the AD_TEXTINTERFACES must be cleaned ("clean") and the
   *          second one is the Openbravo.properties path. The other way is with more arguments,
   *          where: 0- Openbravo.properties path. 1- File extension. 2- Path where are the files to
   *          translate. 3- Relative path.
   * @throws Exception
   */
  public static void main(String argv[]) throws Exception {
    PropertyConfigurator.configure("log4j.lcf");
    String dirIni;
    boolean boolFilter;
    DirFilter dirFilter = null;
    String relativePath = "";

    if ((argv.length == 2)) {
      if (argv[0].equals("clean")) {
        log4j.debug("clean AD_TEXTINTERFACES");
        final Translate translate = new Translate(argv[1]);
        translate.clean();
        return;
      } else if (argv[0].equals("remove")) {
        log4j.debug("remove AD_TEXTINTERFACES");
        final Translate translate = new Translate(argv[1]);
        translate.remove();
        return;
      }
    }

    if (argv.length < 3) {
      log4j.error("Usage: java Translate Openbravo.properties fileTermination sourceDir");
      return;
    }

    final Translate translate = new Translate(argv[0], argv[1]);

    dirIni = argv[2].replace("\\", "/");

    if (argv.length > 3) {
      moduleDirectories = getDirectories(argv[3]);
      log4j.info("Translation for modules");
    }
    boolFilter = true;
    dirFilter = new DirFilter(fileTermination);
    log4j.info("directory source: " + dirIni);
    log4j.info("file termination: " + fileTermination);

    final File path = new File(dirIni, relativePath);
    if (!path.exists()) {
      log4j.error("Can't find directory: " + dirIni);
      translate.destroy();
      return;
    }
    if (moduleDirectories == null) {
      if (TranslateData.isInDevelopmentModule(pool, "0")) {
        listDir(path, boolFilter, dirFilter, relativePath, true, "", 0, "");
        log4j.info("Translated files for " + fileTermination + ": " + count);
      } else
        log4j.info("Core is not in development: skipping it");
    } else {
      listDir(path, boolFilter, dirFilter, relativePath, false, "", 0, "");
      log4j.info("Translated files for " + fileTermination + ": " + count);
    }
    translate.destroy();
  }

  /**
   * Executes the clean of the AD_TEXTINTERFACES table.
   */
  private void clean() {
    try {
      TranslateData.clean(pool);
    } catch (final Exception e) {
      log4j.error("clean error", e);
    }
  }

  private void remove() {
    try {
      TranslateData.remove(pool);
    } catch (final Exception e) {
      log4j.error("remove error", e);
    }
  }

  /**
   * Receives a list of directories and returns this list as an ArrayList
   * 
   * @param s
   * @return
   */
  private static ArrayList<String> getDirectories(String s) {
    final ArrayList<String> l = new ArrayList<String>();
    final StringTokenizer tok = new StringTokenizer(s, "/");
    while (tok.hasMoreTokens())
      l.add(tok.nextToken());
    return l;
  }

  /**
   * List all the files and folders in the selected path.
   * 
   * @param file
   *          The selected path to list.
   * @param boolFilter
   *          If is filtered.
   * @param dirFilter
   *          Filter to apply.
   * @param relativePath
   *          The relative path.
   */
  private static void listDir(File file, boolean boolFilter, DirFilter dirFilter,
      String relativePath, boolean parse, String parent, int level, String module) {
    File[] list;
    if (boolFilter)
      list = file.listFiles(dirFilter);
    else
      list = file.listFiles();
    for (int i = 0; i < list.length; i++) {
      final File fileItem = list[i];
      if (fileItem.isDirectory()) {
        // if it is a subdirectory then list recursively
        final String prevRelativePath = new String(relativePath);
        relativePath += "/" + fileItem.getName();

        if (log4j.isDebugEnabled())
          log4j.debug("dir: "
              + fileItem.getName()
              + " - parse:"
              + parse
              + " - parent:"
              + parent
              + " - level:"
              + level
              + " - include:"
              + (moduleDirectories != null && moduleDirectories.size() > level ? moduleDirectories
                  .get(level) : "--"));
        if (parse)
          listDir(fileItem, boolFilter, dirFilter, relativePath, true, parent, level + 1, module);
        else {
          if ((moduleDirectories.size() == level + 1)
              && (moduleDirectories.get(level).equals("*") || moduleDirectories.get(level).equals(
                  fileItem.getName()))) {
            log4j.info("Start parsing module: " + parent.replace("/", ""));
            translateModule = true;
            try {
              if (TranslateData.isInDevelopmentModulePack(pool, parent.replace("/", "")))
                listDir(fileItem, boolFilter, dirFilter, relativePath, true, parent + "/"
                    + fileItem.getName(), level + 1, parent.replace("/", ""));
              else
                log4j.info("Module is not in development: skipping it");
            } catch (final Exception e) {
              e.printStackTrace();
            }
          } else if (moduleDirectories.size() > level
              && (moduleDirectories.get(level).equals("*") || moduleDirectories.get(level).equals(
                  fileItem.getName())))
            listDir(fileItem, boolFilter, dirFilter, relativePath, false,
                parent + "/" + fileItem.getName(), level + 1, module);
          // other case don't follow deeping into the tree
        }

        relativePath = prevRelativePath;
      } else {
        try {
          if (parse) {
            if (log4j.isDebugEnabled())
              log4j.debug(list[i] + " Parent: " + fileItem.getParent() + " getName() "
                  + fileItem.getName() + " canonical: " + fileItem.getCanonicalPath());
            for (int h = 0; h < toLanguage.length; h++) {
              actualLanguage = toLanguage[h].name;
              parseFile(list[i], relativePath, parent, module);
            }
            if (toLanguage.length == 0) {
              actualLanguage = "";
              parseFile(list[i], relativePath, parent, module);
            }
          }
        } catch (final IOException e) {
          log4j.error("IOException: " + e);
        }
      }
    }
  }

  /**
   * Parse each file searching the text to translate.
   * 
   * @param fileParsing
   *          File to parse.
   * @param relativePath
   *          The relative path.
   */
  private static void parseFile(File fileParsing, String relativePath, String parent, String module) {
    if (!translateModule)
      return;
    final String strFileName = fileParsing.getName();
    if (log4j.isDebugEnabled())
      log4j.debug("Parsing of " + strFileName);
    final int pos = strFileName.indexOf(fileTermination);
    if (pos == -1) {
      log4j.error("File " + strFileName + " don't have termination " + fileTermination);
      return;
    }
    final String strFileWithoutTermination = strFileName.substring(0, pos);
    if (log4j.isDebugEnabled())
      log4j.debug("File without termination: " + strFileWithoutTermination);

    // In case moduleDirectories has value remove parent from path to keep
    // clean the package
    try {
      moduleName = module.equals("") ? "org.openbravo" : module;
      moduleID = TranslateData.getModuleID(pool, moduleName);
      if (moduleID == null || moduleID.equals("")) {
        log4j.error("Trying to insert element in module " + moduleName
            + " which has no ID, it will be set to core");
        moduleName = "CORE";
        moduleID = "0";
      }
      moduleLang = TranslateData.getModuleLang(pool, moduleID);
    } catch (final ServletException e) {
      e.printStackTrace();
    }

    if (log4j.isDebugEnabled())
      log4j.debug("Module name: " + module + " - oldRelativePath:" + relativePath + " - parent:"
          + parent);
    if (moduleDirectories != null && relativePath.startsWith(parent)) {
      relativePath = relativePath.substring(parent.length());
      if (log4j.isDebugEnabled())
        log4j.debug("new relativePath:" + relativePath);
    }

    actualFile = relativePath + "/" + strFileName;

    log4j.debug("File: " + fileParsing);

    try {
      FileInputStream fis = new FileInputStream(fileParsing);
      InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
      parser.parse(new InputSource(reader));
      if (translateModule)
        count++;
    } catch (final IOException e) {
      log4j.error("file: " + actualFile);
      e.printStackTrace();
    } catch (final Exception e) {
      log4j.error("file: " + actualFile);
      e.printStackTrace();
    }
  }

  /**
   * Parse each attribute of each element in the file. This method decides which ones must been
   * translated.
   * 
   * @param amap
   *          Attributes of the element.
   */
  private void parseAttributes(Attributes amap) {
    if (!translateModule)
      return;
    String type = "";
    String value = "";
    for (int i = 0; i < amap.getLength(); i++) {
      String strAux = amap.getValue(i);
      if (amap.getQName(i).equalsIgnoreCase("type")) {
        type = strAux;
      } else if (amap.getQName(i).equalsIgnoreCase("value")) {
        value = strAux;
      } else if (amap.getQName(i).equalsIgnoreCase("onMouseOver")) {
        if (strAux.toLowerCase().startsWith("window.status='")) {
          int j = strAux.lastIndexOf("';");
          int aux = j;
          while ((j != -1) && (aux = strAux.lastIndexOf("';", j - 1)) != -1) {
            j = aux;
          }
          translate(strAux.substring(15, j));
        }
      } else if (amap.getQName(i).equalsIgnoreCase("alt")) {
        translate(strAux);
      } else if (amap.getQName(i).equalsIgnoreCase("title")) {
        translate(strAux);
      }
    }
    if (value != null && !value.equals("")) {
      if (type.equalsIgnoreCase("button"))
        translate(value);
    }
  }

  /**
   * The start of the document to translate.
   */
  @Override
  public void startDocument() {
  }

  /**
   * The prefix mapping for the file.
   */
  @Override
  public void startPrefixMapping(String prefix, String uri) {
    actualPrefix = " xmlns:" + prefix + "=\"" + uri + "\"";
  }

  /**
   * Method to know if a specific element in the file is parseable or not.
   * 
   * @param tagname
   *          Name of the element.
   * @return True if the element is parseable, false if not.
   */
  private static boolean isParseable(String tagname) {
    if (tagname.equalsIgnoreCase("script"))
      return false;
    else if (fileTermination.equalsIgnoreCase("jrxml")) {
      if (!tagname.equalsIgnoreCase("text") && !tagname.equalsIgnoreCase("textFieldExpression"))
        return false;
    }
    return true;
  }

  /**
   * Start of an element of the file. When the parser finds a new element in the file, it calls to
   * this method.
   */
  @Override
  public void startElement(String uri, String name, String qName, Attributes amap) {// (String name,
    // AttributeList
    // amap) throws
    // SAXException {
    if (!translateModule)
      return;
    if (log4j.isDebugEnabled())
      log4j.info("Configuration: startElement is called: element name=" + qName + " actualtag"
          + actualTag + " trlTxt" + translationText);
    if (actualTag != null && isParseable(actualTag) && translationText != null) {
      translate(translationText.toString());
    }
    translationText = null;
    parseAttributes(amap);
    if (actualPrefix != null && !actualPrefix.equals("")) {
      actualPrefix = "";
    }
    actualTag = name.trim().toUpperCase();
  }

  public void comment(char[] ch, int start, int length) {
  }

  public void endDTD() {
  }

  public void endEntity(String name) {
  }

  public void startDTD(String name, String publicId, String systemId) {
  }

  public void startEntity(String name) {
  }

  /**
   * Method to insert begining of CDATA expresions.
   */
  public void startCDATA() {

  }

  /**
   * Method to insert ends of CDATA expresions.
   */
  public void endCDATA() {

  }

  /**
   * End of an element of the file. When the parser finds the end of an element in the file, it
   * calls to this method.
   */
  @Override
  public void endElement(String uri, String name, String qName) {// (String
    // name)
    // throws
    // SAXException
    // {
    if (!translateModule)
      return;
    if (log4j.isDebugEnabled())
      log4j.debug("Configuration: endElement is called: " + qName);

    if (isParseable(actualTag) && translationText != null) {
      translate(translationText.toString());
    }
    translationText = null;
    actualTag = "";
  }

  /**
   * This method is called by the parser when it finds any content between the start and end
   * element's tags.
   */
  @Override
  public void characters(char[] ch, int start, int length) {// throws
    // SAXException {
    final String chars = new String(ch, start, length);
    if (log4j.isDebugEnabled())
      log4j.debug("Configuration(characters) is called: " + chars);
    if (translationText == null)
      translationText = new StringBuffer();
    translationText.append(chars);
  }

  /**
   * This method is the one in charge of the translation of the found text.
   * 
   * @param ini
   *          String with the text to translate.
   */
  private void translate(String ini) {
    translate(ini, false);
  }

  /**
   * This method is the one in charge of the translation of the found text.
   * 
   * @param ini
   *          String with the text to translate.
   * @param isPartial
   *          Indicates if the text passed is partial text or the complete one found in the element
   *          content.
   */
  private void translate(String ini, boolean isPartial) {
    if (!translateModule)
      return;
    ini = replace(replace(ini.trim(), "\r", ""), "\n", " ");
    ini = ini.trim();
    ini = delSp(ini);
    if (!isPartial && actualTag.equalsIgnoreCase("textFieldExpression")) {
      int pos = ini.indexOf("\"");
      while (pos != -1) {
        ini = ini.substring(pos + 1);
        pos = ini.indexOf("\"");
        if (pos != -1) {
          translate(ini.substring(0, pos), true);
          ini = ini.substring(pos + 1);
        } else
          break;
        pos = ini.indexOf("\"");
      }
      return;
    }
    final Vector<String> translated = new Vector<String>(0);
    boolean aux = true;
    translated.addElement("Y");
    String resultado = ini;
    if (!ini.equals("") && !ini.toLowerCase().startsWith("xx") && !isNumeric(ini)) {
      log4j.debug("Translating " + ini + " for file" + actualFile + " moduleLang:" + moduleLang);
      resultado = tokenize(ini, 0, translated);
      try {
        aux = translated.elementAt(0).equals("Y");
        if (moduleLang == null || moduleLang.equals("")) {
          log4j
              .error("Module is not set as translateable or has not defined language, but has translateable elements");
          log4j
              .error("No translations will be inserted. Set the module as 'is translation requiered' and select a language");
          log4j.error("then execute translation again.");
          translateModule = false;
          return;
        }
        if (!aux) {
          if (TranslateData.existsExpresionModFile(pool, ini, actualFile, moduleLang) == 0
              && TranslateData.existsExpresionModNoFile(pool, ini, moduleLang) == 0
              && TranslateData.existsExpresionNoModFile(pool, ini, actualFile, moduleLang) == 0
              && TranslateData.existsExpresionNoModNoFile(pool, ini, moduleLang) == 0) {

            if (!TranslateData.isInDevelopmentModule(pool, moduleID))
              log4j.error("Module  is not in development, it will be inserted anyway");
            if (log4j.isDebugEnabled())
              log4j.debug("inserting in module:" + moduleName + " - ID:" + moduleID);
            TranslateData.insert(pool, ini, actualFile, moduleID);
            log4j.info("Inserting text: " + ini + "from file: " + actualFile
                + "into ad_textinterfaces");
          }
        }
      } catch (final ServletException e) {
        e.printStackTrace();
      }
    }
    return;
  }

  /**
   * To know if a text is numeric or not.
   * 
   * @param ini
   *          String with the text.
   * @return True if has no letter in the text or false if has any letter.
   */
  private static boolean isNumeric(String ini) {
    boolean isNumericData = true;
    for (int i = 0; i < ini.length(); i++) {
      if (Character.isLetter(ini.charAt(i))) {
        isNumericData = false;
        break;
      }
    }
    return isNumericData;
  }

  /**
   * Replace a char, inside a given text, with another char.
   * 
   * @param strInicial
   *          Text where is the char to replace.
   * @param strReplaceWhat
   *          Char to replace.
   * @param strReplaceWith
   *          Char to replace with.
   * @return String with the replaced text.
   */
  private static String replace(String strInicial, String strReplaceWhat, String strReplaceWith) {
    int index = 0;
    int pos;
    final StringBuffer strFinal = new StringBuffer("");
    do {
      pos = strInicial.indexOf(strReplaceWhat, index);
      if (pos != -1) {
        strFinal.append(strInicial.substring(index, pos) + strReplaceWith);
        index = pos + strReplaceWhat.length();
      } else {
        strFinal.append(strInicial.substring(index));
      }
    } while (index < strInicial.length() && pos != -1);
    return strFinal.toString();
  }

  /**
   * This method remove all the spaces in the string.
   * 
   * @param strIni
   *          String to clean.
   * @return String without spaces.
   */
  private static String delSp(String strIni) {
    boolean sp = false;
    String strFin = "";
    for (int i = 0; i < strIni.length(); i++) {
      if (!sp || strIni.charAt(i) != ' ')
        strFin += strIni.charAt(i);
      sp = (strIni.charAt(i) == ' ');
    }
    return strFin;
  }

  /**
   * This method splits the main string into shortest fragments to translate them separately.
   * 
   * @param ini
   *          String to split
   * @param indice
   *          Index of the separator array to use.
   * @param isTranslated
   *          Indicates if the text has been translated.
   * @return String translated.
   */
  private String tokenize(String ini, int indice, Vector<String> isTranslated) {
    final StringBuffer fin = new StringBuffer();
    try {
      boolean first = true;
      String translated = null;
      TranslateData[] dataTranslated = TranslateData.select(pool, ini.trim(), actualFile,
          actualLanguage);
      if (dataTranslated != null && dataTranslated.length > 0) {
        translated = dataTranslated[0].tr;
        // TranslateData.update(pool,
        // dataTranslated[0].baseDictionaryEntryId, actualLanguage);
        TranslateData.update(pool, dataTranslated[0].adTextinterfacesId);
      }
      if (translated != null && translated.length() > 0) {
        fin.append(translated);
        return fin.toString();
      }
      final StringTokenizer st = new StringTokenizer(ini, tokens[indice], false);
      while (st.hasMoreTokens()) {
        if (first) {
          first = false;
        } else {
          fin.append(tokens[indice]);
        }
        String token = st.nextToken();
        token = token.trim();
        if (log4j.isDebugEnabled())
          log4j.debug("Token of " + ini + " : -" + token + "-");
        translated = null;
        dataTranslated = TranslateData.select(pool, token.trim(), actualFile, actualLanguage);
        if (dataTranslated != null && dataTranslated.length > 0) {
          translated = dataTranslated[0].tr;
          // TranslateData.update(pool,
          // dataTranslated[0].baseDictionaryEntryId, actualLanguage);
          TranslateData.update(pool, dataTranslated[0].adTextinterfacesId);
        }
        if ((translated == null || translated.equals("")) && indice < (tokens.length - 1))
          translated = tokenize(token, indice + 1, isTranslated);
        if (translated == null || translated.equals("")) {
          fin.append(token);
          isTranslated.set(0, "N");
        } else
          fin.append(translated);
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return fin.toString();
  }

  /**
   * The method to close database connection.
   */
  private void destroy() {
    pool.destroy();
  }
}
