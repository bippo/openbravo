/*
 ************************************************************************************
 * Copyright (C) 2001-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.xmlEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xerces.parsers.SAXParser;
import org.openbravo.database.ConnectionProvider;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class XmlEngine extends HttpServlet {
  private static final long serialVersionUID = 1L;
  XMLReader xmlParser;
  XMLReader htmlParser;
  Hashtable<String, XmlTemplate> hasXmlTemplate;
  Stack<XmlTemplate> stcRead;
  Hashtable<String, FormatCouple> formatHashtable;
  Hashtable<String, Vector<ReplaceElement>> replaceHashtable;
  String strDriverDefault;
  String strUrlDefault;
  String strBaseLocation;
  String strFormatFile;
  public File fileXmlEngineFormat;
  public File fileBaseLocation;
  public String sessionLanguage;
  public String strReplaceWhat;
  public String strReplaceWith;
  public boolean isResource = false;

  ServletConfig configXMLEngine;

  static public String strTextDividedByZero;

  static Logger log4jXmlEngine = Logger.getLogger(XmlEngine.class);
  static Logger log4jReloadXml = Logger.getLogger("reloadXml");

  ConnectionProvider connProvider;

  public XmlEngine(ConnectionProvider connProvider) {
    this.connProvider = connProvider;
  }

  public XmlEngine() {
    // init();
  }

  public void init(ServletConfig config) throws ServletException {
    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("XmlEngine v0.846-2");
    super.init(config);
    configXMLEngine = config;
    configureLog4j(getInitParameter("fileConfigurationLog4j"));
    strBaseLocation = getInitParameter("BaseLocation");
    strDriverDefault = getInitParameter("driver");
    strUrlDefault = getInitParameter("URL");
    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("driver: " + strDriverDefault + " URL: " + strUrlDefault);
    strFormatFile = getInitParameter("FormatFile");
    fileBaseLocation = new File(strBaseLocation);
    fileXmlEngineFormat = new File(fileBaseLocation, strFormatFile);
    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("BaseLocation: " + strBaseLocation);
    strReplaceWhat = getInitParameter("ReplaceWhat");
    strReplaceWith = getInitParameter("ReplaceWith");
    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("Replace attribute value: \"" + strReplaceWhat + "\" with: \""
          + strReplaceWith + "\".");
    strTextDividedByZero = getInitParameter("TextDividedByZero");
    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("TextDividedByZero: " + strTextDividedByZero);
    try {
      if (log4jXmlEngine.isDebugEnabled())
        log4jXmlEngine.debug("fileBaseLocation: " + fileBaseLocation.getCanonicalPath());
    } catch (IOException e) {
      log4jXmlEngine.error("Error in BaseLocation: " + strBaseLocation);
    }
    initialize();
  }

  private void loadParams() {
    /*
     * // decimal separator:. thousands separator: , DecimalFormatSymbols dfs = new
     * DecimalFormatSymbols(); log4jXmlEngine.info("InternationalCurrencySymbol: " +
     * dfs.getInternationalCurrencySymbol()); log4jXmlEngine.info("CurrencySymbol: " +
     * dfs.getCurrencySymbol()); dfs.setDecimalSeparator('.'); dfs.setGroupingSeparator(',');
     * DecimalFormatSymbols dfsSpanish = new DecimalFormatSymbols();
     * dfsSpanish.setDecimalSeparator(','); dfsSpanish.setGroupingSeparator('.'); // various
     * formats, pending of create a configuration file FormatCouple fc; fc= new FormatCouple(new
     * DecimalFormat("#,##0.00",dfs), new DecimalFormat("#0.00",dfs));
     * formatHashtable.put("euroInform", fc); fc = new FormatCouple(new DecimalFormat("#0.00",dfs),
     * new DecimalFormat("#0.00",dfs)); formatHashtable.put("euroEdition", fc); fc = new
     * FormatCouple(new DecimalFormat("#,##0",dfs), new DecimalFormat("#0",dfs));
     * formatHashtable.put("integerInform", fc); fc = new FormatCouple(new DecimalFormat("#0",dfs),
     * new DecimalFormat("#0",dfs)); formatHashtable.put("integerEdition", fc); fc = new
     * FormatCouple(new DecimalFormat("#,##0.##",dfs),new DecimalFormat("#0.00",dfs));
     * formatHashtable.put("existenciasInforme", fc);
     */
    replaceHashtable = new Hashtable<String, Vector<ReplaceElement>>();
    Vector<ReplaceElement> htmlReplaceVector = new Vector<ReplaceElement>();
    htmlReplaceVector.addElement(new ReplaceElement("&", "&amp;")); // this
    // must
    // be
    // the
    // first
    // for
    // not
    // replace
    // the
    // next
    // &
    htmlReplaceVector.addElement(new ReplaceElement("\"", "&quot;"));
    // htmlReplaceVector.addElement(new ReplaceElement("'","\\'"));
    htmlReplaceVector.addElement(new ReplaceElement("\n", " "));
    htmlReplaceVector.addElement(new ReplaceElement("\r", " "));
    htmlReplaceVector.addElement(new ReplaceElement("<", "&lt;"));
    htmlReplaceVector.addElement(new ReplaceElement(">", "&gt;"));
    htmlReplaceVector.addElement(new ReplaceElement("®", "&reg;"));
    htmlReplaceVector.addElement(new ReplaceElement("€", "&euro;"));
    htmlReplaceVector.addElement(new ReplaceElement("ñ", "&ntilde;"));
    htmlReplaceVector.addElement(new ReplaceElement("Ñ", "&Ntilde;"));
    replaceHashtable.put("html", htmlReplaceVector);
    Vector<ReplaceElement> foReplaceVector = new Vector<ReplaceElement>();
    foReplaceVector.addElement(new ReplaceElement("&", "&#38;"));
    foReplaceVector.addElement(new ReplaceElement("<", "&#60;"));
    foReplaceVector.addElement(new ReplaceElement(">", "&#62;"));
    foReplaceVector.addElement(new ReplaceElement("\\", "&#92;"));
    foReplaceVector.addElement(new ReplaceElement("º", "&#186;"));
    foReplaceVector.addElement(new ReplaceElement("ª", "&#170;"));
    foReplaceVector.addElement(new ReplaceElement("®", "&#174;"));
    foReplaceVector.addElement(new ReplaceElement("€", "&#8364;"));
    foReplaceVector.addElement(new ReplaceElement("\n", "&#10;"));
    replaceHashtable.put("fo", foReplaceVector);
    Vector<ReplaceElement> htmlPreformatedReplaceVector = new Vector<ReplaceElement>();
    htmlPreformatedReplaceVector.addElement(new ReplaceElement("&", "&amp;"));
    htmlPreformatedReplaceVector.addElement(new ReplaceElement("\"", "&quot;"));
    // htmlPreformatedReplaceVector.addElement(new
    // ReplaceElement("'","\\'"));
    htmlPreformatedReplaceVector.addElement(new ReplaceElement("<", "&lt;"));
    htmlPreformatedReplaceVector.addElement(new ReplaceElement(">", "&gt;"));
    htmlPreformatedReplaceVector.addElement(new ReplaceElement("\n", "<BR>"));
    htmlPreformatedReplaceVector.addElement(new ReplaceElement("\r", " "));
    htmlPreformatedReplaceVector.addElement(new ReplaceElement("®", "&reg;"));
    replaceHashtable.put("htmlPreformated", htmlPreformatedReplaceVector);
    Vector<ReplaceElement> htmlHelpReplaceVector = new Vector<ReplaceElement>();
    htmlHelpReplaceVector.addElement(new ReplaceElement("\n", "<BR>"));
    htmlHelpReplaceVector.addElement(new ReplaceElement("\r", ""));
    replaceHashtable.put("htmlHelp", htmlHelpReplaceVector);
    Vector<ReplaceElement> htmlPreformatedTextareaReplaceVector = new Vector<ReplaceElement>();
    htmlPreformatedTextareaReplaceVector.addElement(new ReplaceElement("&", "&amp;"));
    htmlPreformatedTextareaReplaceVector.addElement(new ReplaceElement("\"", "&quot;"));
    // htmlPreformatedTextareaReplaceVector.addElement(new
    // ReplaceElement("'","\\'"));
    htmlPreformatedTextareaReplaceVector.addElement(new ReplaceElement("<", "&lt;"));
    htmlPreformatedTextareaReplaceVector.addElement(new ReplaceElement(">", "&gt;"));
    // htmlPreformatedTextareaReplaceVector.addElement(new
    // ReplaceElement("\n","<BR>"));
    // htmlPreformatedTextareaReplaceVector.addElement(new
    // ReplaceElement("\r","<BR>"));
    htmlPreformatedTextareaReplaceVector.addElement(new ReplaceElement("®", "&reg;"));
    replaceHashtable.put("htmlPreformatedTextarea", htmlPreformatedTextareaReplaceVector);
    Vector<ReplaceElement> htmlJavaScriptReplaceVector = new Vector<ReplaceElement>();
    htmlJavaScriptReplaceVector.addElement(new ReplaceElement("'", "\\'"));
    htmlJavaScriptReplaceVector.addElement(new ReplaceElement("\"", "&quot;"));
    htmlJavaScriptReplaceVector.addElement(new ReplaceElement("\n", "\\n"));
    replaceHashtable.put("htmlJavaScript", htmlJavaScriptReplaceVector);
  }

  public void initialize() {
    hasXmlTemplate = new Hashtable<String, XmlTemplate>(); // vector of
    // XmlTemplates
    stcRead = new Stack<XmlTemplate>(); // stack of XmlTemplates not readed
    formatHashtable = new Hashtable<String, FormatCouple>();
    XMLReader xmlParserFormat = new SAXParser();
    xmlParserFormat.setContentHandler(new FormatRead(formatHashtable));
    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("XmlEngine file formats: " + strFormatFile);
    // File fileXmlEngineFormat = new File (fileBaseLocation,
    // strFormatFile);
    // String strFormatFile =
    // "c:\\Apps\\src\\org\\openbravo\\data\\examples\\FormatExample1.xml";
    // File fileXmlEngineFormat = new File (strFormatFile);
    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("fileXmlEngineFormat: " + fileXmlEngineFormat.toString());
    try {
      // xmlParserFormat.parse(new InputSource(new
      // FileReader(fileXmlEngineFormat)));
      xmlParserFormat.parse(new InputSource(new InputStreamReader(new FileInputStream(
          fileXmlEngineFormat), "UTF-8")));
    } catch (FileNotFoundException e) {
      log4jXmlEngine.error("not found fileXmlEngineFormat: " + fileXmlEngineFormat + "\n"
          + e.getMessage());
      return;
    } catch (IOException e) {
      log4jXmlEngine.error("IOException in fileXmlEngineFormat: " + fileXmlEngineFormat);
      e.printStackTrace();
      return;
    } catch (Exception e) {
      log4jXmlEngine.error("Exception in fileXmlEngineFormat: " + fileXmlEngineFormat);
      e.printStackTrace();
      return;
    }
    loadParams();
  }

  /**
   * this function reads a file that defines a XmlTemplate without any discard
   * 
   * @param strXmlTemplateFile
   *          A configuration file of the XmlTemplate in XML format
   */
  public XmlTemplate readXmlTemplate(String strXmlTemplateFile) {
    return readXmlTemplate(strXmlTemplateFile, new String[0]);
  }

  /**
   * this function reads a file that defines a XmlTemplate with a vector of discard
   * 
   * @param strXmlTemplateFile
   *          A configuration file of the XmlTemplate in XML format
   * @param discard
   *          A vector of Strings with the names of the discards in the template file. The elements
   *          with a id equal to a discard are not readed
   */
  public synchronized XmlTemplate readXmlTemplate(String strXmlTemplateFile, String[] discard) {
    String xmlTemplateName = strXmlTemplateFile;
    xmlTemplateName = fileBaseLocation.getName() + xmlTemplateName;
    for (int i = 0; i < discard.length; i++) {
      xmlTemplateName = xmlTemplateName + "?" + discard[i];
    }
    if (log4jReloadXml.isDebugEnabled()) {
      initialize();
      log4jReloadXml.debug("XmlEngine 29-11-2001 Initialized");
    }
    return readAllXmlTemplates(xmlTemplateName, strXmlTemplateFile, discard);
  }

  /**
   * this function add the XmlTemplate to the list of XmlTemplates and read all the XmlTemplates
   * that there are in the Stack. The XmlTemplates are added to the Stack in the addXmlTemplate
   * function or in the readFile function
   * 
   * @param strXmlTemplateName
   *          The name that identifie the XmlTemplate
   * @param strXmlTemplateFile
   *          The configuration file of the XmlTemplate
   * @param discard
   *          A vector of Strings with the names of the discards in the template
   */
  XmlTemplate readAllXmlTemplates(String strXmlTemplateName, String strXmlTemplateFile,
      String[] discard) {
    XmlTemplate xmlTemplate = addXmlTemplate(strXmlTemplateName, strXmlTemplateFile, discard);
    while (!stcRead.empty()) {
      XmlTemplate xmlTemplateRead = stcRead.pop();
      readFile(xmlTemplateRead);
    }
    return xmlTemplate;
  }

  /**
   * this function add the XmlTemplate to the list of XmlTemplates or return an existing XmlTemplate
   * if it was found in the list
   * 
   * @param strXmlTemplateName
   *          The name that identifie the XmlTemplate
   * @param strXmlTemplateFile
   *          The configuration file of the XmlTemplate
   * @param discard
   *          A vector of Strings with the names of the discards in the template
   */
  private XmlTemplate addXmlTemplate(String strXmlTemplateName, String strXmlTemplateFile,
      String[] discard) {
    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("Adding: " + strXmlTemplateName);
    XmlTemplate xmlTemplate = hasXmlTemplate.get(strXmlTemplateName);
    if (xmlTemplate != null) {
      return xmlTemplate;
    }

    log4jXmlEngine.debug("Before the new XmlTemplate");
    xmlTemplate = new XmlTemplate(strXmlTemplateName, strXmlTemplateFile, discard, this);
    xmlTemplate.configuration.strDriverDefault = strDriverDefault;
    xmlTemplate.configuration.strUrlDefault = strUrlDefault;
    hasXmlTemplate.put(strXmlTemplateName, xmlTemplate);
    log4jXmlEngine.debug("created de new XmlTemplate");
    stcRead.push(xmlTemplate);
    log4jXmlEngine.debug("push xmlTemplate");
    return xmlTemplate;
  }

  /**
   * this function read the XmlTemplate
   * 
   * @param xmlTemplate
   *          The XmlTemplate object
   */
  private void readFile(XmlTemplate xmlTemplate) {
    xmlParser = new SAXParser();
    htmlParser = new org.cyberneko.html.parsers.SAXParser();

    // parser of the configuration file
    xmlParser.setContentHandler(xmlTemplate.configuration);
    String strFile = xmlTemplate.fileConfiguration() + ".xml";
    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("XmlEngine name: " + strFile);
    File fileXmlEngineConfiguration = null;
    if (!isResource) {
      fileXmlEngineConfiguration = new File(fileBaseLocation, strFile);
      if (log4jXmlEngine.isDebugEnabled()) {
        log4jXmlEngine
            .debug("fileXmlEngineConfiguration: " + fileXmlEngineConfiguration.toString());
        log4jXmlEngine.debug("Parent fileXmlEngineConfiguration: "
            + fileXmlEngineConfiguration.getParent());
      }
    }
    xmlTemplate.clear();
    try {
      // if (!isResource) xmlParser.parse(new InputSource(new
      // FileReader(fileXmlEngineConfiguration)));
      if (!isResource)
        xmlParser.parse(new InputSource(new InputStreamReader(new FileInputStream(
            fileXmlEngineConfiguration), "UTF-8")));
      else
        xmlParser.parse(new InputSource(ClassLoader.getSystemResourceAsStream(strFile)));
    } catch (FileNotFoundException e) {
      if (!isResource)
        log4jXmlEngine.error("not found fileXmlEngineConfiguration: " + fileXmlEngineConfiguration
            + "\n" + e.getMessage());
      else
        log4jXmlEngine.error("not found fileXmlEngineConfiguration: " + strFile + "\n"
            + e.getMessage());
      return;
    } catch (IOException e) {
      if (!isResource)
        log4jXmlEngine.error("IOException in fileXmlEngineConfiguration: "
            + fileXmlEngineConfiguration);
      else
        log4jXmlEngine.error("IOException in fileXmlEngineConfiguration: " + strFile);
      e.printStackTrace();
      return;
    } catch (Exception e) {
      if (!isResource)
        log4jXmlEngine.error("Exception in fileXmlEngineConfiguration: "
            + fileXmlEngineConfiguration);
      else
        log4jXmlEngine.error("Exception in fileXmlEngineConfiguration: " + strFile);
      e.printStackTrace();
      return;
    }

    // parser of the template file
    int posExtension = xmlTemplate.configuration.strTemplate.lastIndexOf(".");
    XMLReader templateParser;
    if (xmlTemplate.configuration.strTemplate.substring(posExtension).equals(".html")) {
      if (log4jXmlEngine.isDebugEnabled())
        log4jXmlEngine.debug("Html file: -"
            + xmlTemplate.configuration.strTemplate.substring(posExtension) + "-");
      templateParser = htmlParser;
    } else {
      if (log4jXmlEngine.isDebugEnabled())
        log4jXmlEngine.debug("Xml file: -"
            + xmlTemplate.configuration.strTemplate.substring(posExtension) + "-");
      templateParser = xmlParser;
    }
    templateParser.setContentHandler(xmlTemplate);
    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("XmlEngine file template: " + xmlTemplate.configuration.strTemplate);
    File fileXmlEngineTemplate = null;
    String strPath = "";
    if (!isResource) {
      fileXmlEngineTemplate = new File(fileXmlEngineConfiguration.getParent(),
          xmlTemplate.configuration.strTemplate);
      if (log4jXmlEngine.isDebugEnabled())
        log4jXmlEngine.debug("fileXmlEngineTemplate: " + fileXmlEngineTemplate.toString());
    } else {
      int finPath = -1;
      if ((finPath = strFile.lastIndexOf("/")) != -1) {
        strPath = strFile.substring(0, finPath);
        if (!strPath.endsWith("/"))
          strPath += "/";
      }
    }
    try {
      // if (!isResource) templateParser.parse(new InputSource(new
      // FileReader(fileXmlEngineTemplate)));
      if (!isResource)
        templateParser.parse(new InputSource(new InputStreamReader(new FileInputStream(
            fileXmlEngineTemplate), "UTF-8")));
      else
        templateParser.parse(new InputSource(ClassLoader.getSystemResourceAsStream(strPath
            + xmlTemplate.configuration.strTemplate)));
    } catch (FileNotFoundException e) {
      if (!isResource)
        log4jXmlEngine.error("not found fileXmlEngineTemplate: " + fileXmlEngineTemplate + "\n"
            + e.getMessage());
      else
        log4jXmlEngine.error("not found fileXmlEngineTemplate: " + strPath
            + xmlTemplate.configuration.strTemplate + "\n" + e.getMessage());
      return;
    } catch (IOException e) {
      if (!isResource)
        log4jXmlEngine.error("IOException in fileXmlEngineTemplate: " + fileXmlEngineTemplate);
      else
        log4jXmlEngine.error("IOException in fileXmlEngineTemplate: " + strPath
            + xmlTemplate.configuration.strTemplate);
      e.printStackTrace();
      return;
    } catch (Exception e) {
      if (!isResource)
        log4jXmlEngine.error("Exception in fileXmlEngineTemplate: " + fileXmlEngineTemplate);
      else
        log4jXmlEngine.error("Exception in fileXmlEngineTemplate: " + strPath
            + xmlTemplate.configuration.strTemplate);
      e.printStackTrace();
      return;
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String strReportName = request.getParameter("report");
    String strReload = request.getParameter("reload");
    if (strReload != null) {
      initialize();
    }
    Report report = readReportConfiguration(strReportName);
    // SQL connect();
    for (DataValue elementDataValue : report.xmlDocument.hasDataValue.values()) {
      elementDataValue.connect(); // SQL
      for (Enumeration<Object> e2 = elementDataValue.vecParameterValue.elements(); e2
          .hasMoreElements();) {
        ParameterValue parameter = (ParameterValue) e2.nextElement();
        parameter.strValue = request.getParameter(parameter.parameterTemplate.strName);
        if (parameter.strValue == null) {
          if (log4jXmlEngine.isDebugEnabled())
            log4jXmlEngine.debug("getParameter: default assigned");
          parameter.strValue = parameter.parameterTemplate.strDefault;
        }
        if (log4jXmlEngine.isDebugEnabled())
          log4jXmlEngine.debug("getParameter: " + parameter.parameterTemplate.strName + " valor: "
              + parameter.strValue);
      }
    }

    // Connection of the subreports
    for (XmlDocument subXmlDocument : report.xmlDocument.hasSubXmlDocuments.values()) {
      for (DataValue elementDataValue : subXmlDocument.hasDataValue.values()) {
        elementDataValue.connect(); // SQL
      }
    }

    // Parameter of the report (not for the SQL query's)
    for (ParameterValue parameter : report.xmlDocument.hasParameterValue.values()) {
      parameter.strValue = request.getParameter(parameter.parameterTemplate.strName);
      if (parameter.strValue == null) {
        log4jXmlEngine.debug("getParameter of: " + parameter.parameterTemplate.strName
            + " default assigned");
        parameter.strValue = parameter.parameterTemplate.strDefault;
      }
      log4jXmlEngine.debug("getParameter: " + parameter.parameterTemplate.strName + " value: "
          + parameter.strValue);
    }

    // Label of the report (not for the SQL query's)
    for (LabelValue label : report.xmlDocument.hasLabelValue.values()) {
      log4jXmlEngine.debug("getting labelValues for report.xmlDocument");
      label.strValue = request.getParameter(label.labelTemplate.strName);
      if (label.strValue == null) {
        log4jXmlEngine.debug("getLabel of: " + label.labelTemplate.strName + " default assigned");
        label.strValue = label.labelTemplate.strName;
      }
      log4jXmlEngine
          .debug("getLabel: " + label.labelTemplate.strName + " value: " + label.strValue);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String strBlank = request.getParameter("blank");
    if (strBlank != null) {
      out.println(report.xmlDocument.print(strBlank));
    } else {
      out.println(report.xmlDocument.print());
    }
    out.close();
  }

  @SuppressWarnings({ "rawtypes" })
  // It's not possible to cast from xmlTemplate to Report
  void connect() {
    for (Enumeration e1 = hasXmlTemplate.elements(); e1.hasMoreElements();) {
      Report report = (Report) e1.nextElement(); // use XmlDocument
      for (Enumeration<DataValue> e2 = report.xmlDocument.hasDataValue.elements(); e2
          .hasMoreElements();) {
        DataValue elementDataValue = e2.nextElement();
        elementDataValue.connect();
      }
    }
  }

  @SuppressWarnings({ "rawtypes" })
  // It's not possible to cast from xmlTemplate to Report
  void closeConnections() {
    for (Enumeration e1 = hasXmlTemplate.elements(); e1.hasMoreElements();) {
      Report report = (Report) e1.nextElement();
      for (DataValue elementDataValue : report.xmlDocument.hasDataValue.values()) {
        elementDataValue.closeConnection();
      }
    }
  }

  public void destroy() {
    closeConnections();
  }

  static void configureLog4j(String file) {
    if (file != null) {
      PropertyConfigurator.configure(file);
    } else {
      PropertyConfigurator.configure("log4j.lcf");
    }
  }

  public static void main(String argv[]) {
    int i;
    configureLog4j(null);
    String strFile;
    if (argv.length < 1) {
      log4jXmlEngine.error("Usage: java XmlEngine [driver URL] file");
      return;
    }
    XmlEngine xmlEngine = new XmlEngine();
    if (argv[0].equals("-d")) {
      xmlEngine.strDriverDefault = argv[1];
      xmlEngine.strUrlDefault = argv[2];
      strFile = argv[3];
      i = 3;
    } else {
      strFile = argv[0];
      i = 0;
    }

    xmlEngine.initialize();
    Report report = xmlEngine.readReportConfiguration(strFile);
    for (DataValue elementDataValue : report.xmlDocument.hasDataValue.values()) {
      for (Enumeration<Object> e2 = elementDataValue.vecParameterValue.elements(); e2
          .hasMoreElements();) {
        ParameterValue parameter = (ParameterValue) e2.nextElement();
        i++;
        parameter.strValue = argv[i];
        if (parameter.strValue == null) {
          if (log4jXmlEngine.isDebugEnabled())
            log4jXmlEngine.debug("Parameter(main): default assigned");
          parameter.strValue = parameter.parameterTemplate.strDefault;
        }
        if (log4jXmlEngine.isDebugEnabled())
          log4jXmlEngine.debug("Parameter(main): " + parameter.parameterTemplate.strName
              + " valor: " + parameter.strValue);
      }
      for (Enumeration<Object> e3 = elementDataValue.vecLabelValue.elements(); e3.hasMoreElements();) {
        LabelValue labelValue = (LabelValue) e3.nextElement();
        i++;
        labelValue.strValue = argv[1];
        if (labelValue.strValue == null) {
          if (log4jXmlEngine.isDebugEnabled())
            log4jXmlEngine.debug("Label(main): default assigned");
          labelValue.strValue = labelValue.labelTemplate.strName;
        }
        if (log4jXmlEngine.isDebugEnabled())
          log4jXmlEngine.debug("Label(main): " + labelValue.labelTemplate.strName + " valor: "
              + labelValue.strValue);
      }
    }

    if (log4jXmlEngine.isDebugEnabled())
      log4jXmlEngine.debug("Hashtable: ");
    for (String id : report.xmlDocument.xmlTemplate.configuration.hashtable.vecKeys) {
      IDComponent iDComponent = (IDComponent) report.xmlDocument.xmlTemplate.configuration.hashtable
          .get(id);
      if (log4jXmlEngine.isDebugEnabled())
        log4jXmlEngine.debug("id: " + id + " type: " + iDComponent.type());
    }
    if (log4jXmlEngine.isDebugEnabled()) {
      log4jXmlEngine.debug("Template: " + report.xmlDocument.xmlTemplate.configuration.strTemplate);
      log4jXmlEngine.debug(report.xmlDocument.print());
    }
  }

  // XmlEngineNP: classes for compatibilizing with the Rrports version
  Report readReportConfiguration(String strReportFile) {
    return readReportConfiguration(strReportFile, new String[0]);
  }

  Report readReportConfiguration(String strReportFile, String[] discard) {
    return new Report(strReportFile, discard, this);
  }

}
