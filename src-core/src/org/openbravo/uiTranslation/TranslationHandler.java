package org.openbravo.uiTranslation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;

public class TranslationHandler {

  public static final int ADWINDOW = 1, MANWINDOW = 2, PROCESS = 3;
  private int xmlDocumentType;

  private static final Logger log4j = Logger.getLogger(TranslationHandler.class);
  private XmlDocument xmlDocument;
  private ConnectionProvider conn;
  private String moduleLang = "";
  private String language = "";
  private String tabId = "";
  private String fileName = "";
  private String baseDesignPath = "";
  private String reportString = "";
  private String documentTypeId = "";

  private HashMap<String, String> formLabels;
  private boolean continueTranslationProcess = true;

  public TranslationHandler(ConnectionProvider connProvider, XmlDocument xmlDoc) {
    this.conn = connProvider;
    xmlDocument = xmlDoc;
  }

  public TranslationHandler(ConnectionProvider con) {
    conn = con;
  }

  // set the language from the current users profile
  public String getLanguage() {
    return language;
  }

  public void setLanguage(String lang) {
    if (lang != null && !lang.equals("")) {
      language = lang.replace("defaultLang", "").replace("=", "").replace("\"", "")
          .replace(";", "").trim();
    } else {
      continueTranslationProcess = false;
    }
    log4j.debug("setLanguage(String lang) - language: " + language);
  }

  public void setModuleLanguage(String modLang) {
    if (modLang != null) {
      moduleLang = modLang;
    }
  }

  // set the tabId for AD generated tab that requires translation
  public String getTabId() {
    return tabId;
  }

  public void setTabId(String tabId) {
    if (tabId != null && !tabId.equals("")) {
      setXmlDocumentType(ADWINDOW);
      this.tabId = tabId;
    }
  }

  // set the filename to be parsed and check for translation strings;
  public void setFileName(String filename) {
    if (filename != null && !filename.equals("")) {
      this.fileName = filename;
    }
  }

  public void setXmlDocumentType(int type) {
    xmlDocumentType = type;
  }

  public void setXmlDocumentTypeId(String id) {
    documentTypeId = id;
  }

  // return the results from parsing and translating text values
  public HashMap<String, String> getFormLabels() {
    return formLabels;
  }

  public boolean continueTRLProcess() {
    return continueTranslationProcess;
  }

  // use this method to publicly call the translation generation process.
  public void generateTranslations() {
    log4j.debug("generateTranslations()");
    if (conn != null) {
      processTranslations();
    }
  }

  public void prepareFile(String reportName, String lang, File file, String baseDesignPath1) {
    setFileName(reportName.replaceAll("//", "/"));
    setBaseDesignPath(baseDesignPath1);
    setLanguage(lang);
    generateTranslations();
    createInputStream(file);
  }

  public InputStream getInputStream() {
    InputStream stream = null;
    byte[] input;
    try {
      input = reportString.getBytes("UTF-8");
      stream = new ByteArrayInputStream(input);
    } catch (UnsupportedEncodingException e) {
      log4j.debug("Error caught when generating input stream. " + e.getMessage());
      e.printStackTrace();
    }
    return stream;
  }

  public HashMap<String, String> getWindowLabels() {
    FieldLabelsData[] fieldLabels;
    HashMap<String, String> labelMap = new HashMap<String, String>();
    try {
      if (conn != null) {
        fieldLabels = FieldLabelsData.select(conn, tabId, language);
        for (int i = 0; i < fieldLabels.length; i++) {
          FieldLabelsData label = fieldLabels[i];
          if (label.fieldtrlName != null && !label.fieldtrlName.equals("")) {
            labelMap.put(label.fieldName, label.fieldtrlName);
          } else {
            labelMap.put(label.fieldName, label.fieldName);
          }
        }
      }
    } catch (ServletException e) {
      e.printStackTrace();
    }
    return labelMap;
  }

  private void createInputStream(File reportFile) {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(reportFile);
    } catch (FileNotFoundException e) {
      log4j.debug("Error caught tryng to read file into InputStream: " + e.getMessage());
      e.printStackTrace();
    }
    InputStreamReader inpRe = null;
    try {
      inpRe = new InputStreamReader(inputStream, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log4j.error(e);
      e.printStackTrace();
    }
    StringBuffer buffer = new StringBuffer();
    BufferedReader bre = new BufferedReader(inpRe);
    String inputLine = null;
    String prefix = "<text><![CDATA[";
    String suffix = "]]></text>";
    try {
      while ((inputLine = bre.readLine()) != null) {
        if (inputLine.contains(prefix)) {
          int start = inputLine.indexOf(prefix);
          String text = inputLine.substring(start);
          text = text.replace(prefix, "");
          int end = text.indexOf(suffix);
          if (end > 0) {
            text = text.substring(0, end);
          }
          String result = formLabels.get(text);
          if (formLabels.containsKey(text)) {
            inputLine = inputLine.replace(prefix + text + suffix, prefix + result + suffix);
          }
        }
        // write translated line including original newline
        buffer.append(inputLine);
        buffer.append("\n");
      }
    } catch (IOException e1) {
      log4j.debug("Error caught tryng to read file: " + e1.getMessage());
      e1.printStackTrace();
    }

    reportString = buffer.toString();
  }

  private void processTranslations() {

    if (documentTypeId != null && !documentTypeId.equals("")) {
      setProcessLabels();
    }
    if (tabId != null && !tabId.equals("")) {
      setTabLabels();
    } else if (fileName != null && !fileName.equals("")) {
      // look for fileName for all manual windows (including processes)
      String textFileName = fileName.replace(baseDesignPath, "");
      if (textFileName.contains("?")) {
        String suffix = textFileName.substring(textFileName.lastIndexOf("."));
        String prefix = textFileName.substring(0, textFileName.indexOf("?"));
        textFileName = prefix + suffix;
      }
      formLabels = TranslationUtils.processFormLabels(conn, textFileName, language);
    }
  }

  private void setTabLabels() {
    InterfaceInfo info = new InterfaceInfo();
    info.setId(tabId);
    info.setInterfaceType(InterfaceInfo.TAB);
    info = TranslationUtils.getModuleLang(conn, info);
    setModuleLanguage(info.getModuleLanguage());
    WindowLabel[] labels = TranslationUtils.processWindowLabels(conn, tabId, language, moduleLang);
    for (int labelCount = 0; labelCount < labels.length; labelCount++) {
      WindowLabel label = labels[labelCount];
      xmlDocument.setLabel(label.getOriginalLabel(), label.getTranslatedLabel());
    }
  }

  private void setProcessLabels() {
    InterfaceInfo uiInfo = new InterfaceInfo();
    uiInfo.setId(documentTypeId);
    uiInfo.setInterfaceType(InterfaceInfo.PROCESS);
    uiInfo = TranslationUtils.getModuleLang(conn, uiInfo);
    setModuleLanguage(uiInfo.getModuleLanguage());
    if (!language.equalsIgnoreCase(uiInfo.getModuleLanguage())) {
      uiInfo = TranslationUtils.getInterfaceHeaderTrlInfo(conn, uiInfo, language);
    }
    WindowLabel[] labels = TranslationUtils.processProcessLabels(conn, language, uiInfo);
    xmlDocument.setParameter("processName", uiInfo.getTitle());
    xmlDocument.setParameter("processDescription", uiInfo.getDescription());
    xmlDocument.setParameter("processHelp", uiInfo.getHelp());
    for (int labelCount = 0; labelCount < labels.length; labelCount++) {
      WindowLabel label = labels[labelCount];
      xmlDocument.setLabel(label.getOriginalLabel(), label.getTranslatedLabel());
    }
  }

  private void setBaseDesignPath(String baseDesignPath1) {
    if (baseDesignPath1 != null)
      this.baseDesignPath = baseDesignPath1.replaceAll("//", "/");
  }

}
