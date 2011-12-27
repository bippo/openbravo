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
package org.openbravo.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xerces.parsers.SAXParser;
import org.openbravo.utils.DirFilter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Create a java file with functions based on sql sentences
 * 
 **/
public class Sqlc extends DefaultHandler {
  private static final String VERSION = "V1.O00-1";
  String sqlcName;
  String sqlcPackage = null;
  String sqlcAccessModifier = ""; // i.e. "" or public
  Stack<String> stcElement; // Stack of Elements
  String strElement;
  String strDriver;
  String strURL;
  String strDBUser;
  String strDBPassword;
  String javaDateFormat;
  String queryExecutionStrategy;
  static String javaFileName;
  Connection connection;
  Sql sql;
  String strComments;
  PreparedStatement preparedStatement;
  ResultSet result;
  ResultSetMetaData rsmd;
  int numCols;
  boolean first = true;
  OutputStreamWriter out;
  StringBuffer out1; // to be fixed: out1 and out2 are two auxiliar buffers...
  StringBuffer out2;
  PrintWriter printWriterTxt;
  Parameter parameterSql;
  boolean error;
  boolean importRDBMSIndependent = false;
  boolean importJavaUtil = false;
  boolean writeTxtFiles = false;
  StringBuffer buffer;
  static ArrayList<String> includeDirectories;
  static int errorNum;
  private static boolean queryWithOptionalParameterTypeNone = false;
  private static boolean queryWithOptionalParameterWithoutType = false;
  private static boolean queryWithOptionalParameterTypeArgument = false;

  static Logger log4j = Logger.getLogger(Sqlc.class); // log4j

  private Sqlc() {
    init();
  }

  private void init() {
    sql = new Sql();
    stcElement = new Stack<String>();
    first = true;
    sqlcPackage = null;
    strComments = null;
    sqlcAccessModifier = "";
  }

  public static void main(String argv[]) throws Exception {
    PropertyConfigurator.configure("log4j.lcf");
    String dirIni;
    String dirFin;
    boolean boolFilter;
    String strFilter;
    DirFilter dirFilter = null;
    errorNum = 0;

    if (argv.length < 1) {
      log4j
          .error("Usage: java org.openbravo.data.Sqlc connection.xml [fileTermination [sourceDir destinyDir [write_txt_files]]]");
      return;
    }

    final Sqlc sqlc = new Sqlc();
    XMLReader parser;
    parser = new SAXParser();
    if (argv.length <= 4)
      sqlc.writeTxtFiles = false;
    else
      sqlc.writeTxtFiles = argv[4].equalsIgnoreCase("true");
    parser.setContentHandler(sqlc);
    final String strFileConnection = argv[0];
    sqlc.readProperties(strFileConnection);

    // the first parameter is the directory where the search is done
    if (argv.length <= 2)
      dirIni = ".";
    else
      dirIni = argv[2];

    if (argv.length <= 3)
      dirFin = dirIni;
    else
      dirFin = argv[3];

    // include only directories (and sub-directories under this one) with
    // this pattern, the packaging will be from this point, not from call
    // point
    if (argv.length <= 4)
      includeDirectories = null;
    else
      includeDirectories = getDirectories(argv[4]);

    // the second parameter is the string-chain to make the filter
    // the file must end with this string-chain in order to be recognized
    boolFilter = true; // there always must be a termination
    if (argv.length <= 1)
      strFilter = ".xml";
    else
      strFilter = argv[1];
    String listOfFiles = System.getProperty("sqlc.listOfFiles", null);
    if (listOfFiles == null) {
      dirFilter = new DirFilter(strFilter);
    } else {
      List<String> files = new ArrayList<String>();
      files.addAll(Arrays.asList(listOfFiles.split(",")));
      dirFilter = new DirFilter(files);
    }
    log4j.info("directory source: " + dirIni);
    log4j.info("directory destiny: " + dirFin);
    log4j.info("file termination: " + strFilter);
    log4j.info("file connection: " + strFileConnection);
    log4j.info("Write TXT Files: " + sqlc.writeTxtFiles);

    sqlc.connect(strFileConnection);

    // use specified queryExecutionModel
    if (sqlc.queryExecutionStrategy.equals("optimized")) {
      queryWithOptionalParameterWithoutType = true;
      queryWithOptionalParameterTypeNone = true;
      queryWithOptionalParameterTypeArgument = true;
    }

    final File path = new File(dirIni);
    if (!path.exists()) {
      log4j.error("Directory does not exist: " + dirIni);
      return;
    }
    final File fileFin = new File(dirFin);
    if (!fileFin.exists()) {
      log4j.error("Directory does not exist: " + dirFin);
      return;
    }
    listDir(path, boolFilter, dirFilter, sqlc, parser, strFilter, fileFin,
        (includeDirectories == null), "", 0);

    sqlc.closeConnection();

    if (errorNum > 0) {
      log4j.error(errorNum + " errors found!");
      System.exit(1); // exit with error
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

  // list of files and directories related to a File
  private static void listDir(File file, boolean boolFilter, DirFilter dirFilter, Sqlc sqlc,
      XMLReader parser, String strFilter, File fileFin, boolean parse, String parent, int level) {

    File[] list;

    if (boolFilter)
      list = file.listFiles(dirFilter);
    else
      list = file.listFiles();
    for (int i = 0; i < list.length; i++) {
      final File fileItem = list[i];
      if (fileItem.isDirectory()) {
        if (log4j.isDebugEnabled())
          log4j
              .debug("dir: "
                  + fileItem.getName()
                  + " - parse:"
                  + parse
                  + " - parent:"
                  + parent
                  + " - level:"
                  + level
                  + " - include:"
                  + (includeDirectories != null && includeDirectories.size() > level ? includeDirectories
                      .get(level) : "--"));
        // if it is a subdirectory then list recursively
        if (parse)
          listDir(fileItem, boolFilter, dirFilter, sqlc, parser, strFilter, fileFin, true, parent,
              level + 1);
        else {
          if ((includeDirectories.size() == level + 1)
              && (includeDirectories.get(level).equals("*") || includeDirectories.get(level)
                  .equals(fileItem.getName())))
            listDir(fileItem, boolFilter, dirFilter, sqlc, parser, strFilter, fileFin, true,
                fileItem.getParent() + "/" + fileItem.getName(), level + 1);
          else if (includeDirectories.size() > level
              && (includeDirectories.get(level).equals("*") || includeDirectories.get(level)
                  .equals(fileItem.getName())))
            listDir(fileItem, boolFilter, dirFilter, sqlc, parser, strFilter, fileFin, false,
                parent, level + 1);
          // other case don't follow deeping into the tree
        }
      } else {
        try {
          if (log4j.isDebugEnabled())
            log4j.debug(list[i] + " Parent: " + fileItem.getParent() + " getName() "
                + fileItem.getName() + " canonical: " + fileItem.getCanonicalPath());
          if (parse)
            parseSqlFile(list[i], sqlc, parser, strFilter, fileFin, parent);
        } catch (final IOException e) {
          log4j.error("IOException: ", e);
        }
      }
    }
  }

  /*
   * replace dirIni.toString() with file look for termination only at the end
   */

  private static void parseSqlFile(File fileParsing, Sqlc sqlc, XMLReader parser, String strFilter,
      File fileFin, String parent) {
    parent = parent.replace("\\", "/");
    final String strFileName = fileParsing.getName();
    if (log4j.isDebugEnabled())
      log4j.debug("Parsing of " + strFileName);
    sqlc.init();
    if (log4j.isDebugEnabled())
      log4j.debug("new Sql");
    final int pos = strFileName.indexOf(strFilter);
    if (pos == -1) {
      log4j.error("File " + strFileName + " don't have termination " + strFilter);
      return;
    }
    final String strFileWithoutTermination = strFileName.substring(0, pos);
    if (log4j.isDebugEnabled())
      log4j.debug("File without termination: " + strFileWithoutTermination);
    if (strFileWithoutTermination.equalsIgnoreCase("SQLC")) {
      log4j.error("Name Sqlc not allowed for a file");
      return;
    }
    try {

      String parentDir = fileParsing.getParent().replace("\\", "/");
      // In case includeDirectories has value remove parent from path to
      // keep clean the package
      if (includeDirectories != null && parentDir.startsWith(parent))
        parentDir = parentDir.substring(parent.length());

      final File dirJava = new File(fileFin, parentDir);
      if (log4j.isDebugEnabled())
        log4j.debug("parentDir:" + parentDir + " - javadir:" + dirJava + " - parent:" + parent);
      dirJava.mkdirs();
      javaFileName = TransformaNombreFichero(strFileWithoutTermination);
      final File fileJava = new File(dirJava, javaFileName + ".java");
      File fileTxt = null;
      if (sqlc.writeTxtFiles)
        fileTxt = new File(fileParsing.getParent(), strFileWithoutTermination + ".txt");
      if ((!fileJava.exists()) || (fileParsing.lastModified() > fileJava.lastModified())) {
        if (log4j.isDebugEnabled())
          log4j.debug(" time file parsed: " + fileParsing.lastModified() + " time file java: "
              + fileParsing.lastModified());
        final FileOutputStream resultsFile = new FileOutputStream(fileJava);
        sqlc.out = new OutputStreamWriter(resultsFile, "UTF-8");
        sqlc.out1 = new StringBuffer();
        sqlc.out2 = new StringBuffer();
        /*
         * FileWriter resultsFile = new FileWriter(fileJava); sqlc.out = new
         * PrintWriter(resultsFile);
         */
        FileWriter resultsFileTxt = null;
        if (sqlc.writeTxtFiles) {
          resultsFileTxt = new FileWriter(fileTxt);
          sqlc.printWriterTxt = new PrintWriter(resultsFileTxt);
        }
        log4j.info("File: " + fileParsing + " \tprocessed");
        final java.util.Date date = new java.util.Date(); // there is date in
        // java.sql.*
        if (log4j.isDebugEnabled())
          log4j.debug("Time: " + date.getTime());

        sqlc.error = false;
        try {
          parser.parse(new InputSource(new FileReader(fileParsing)));
        } catch (final IOException e) {
          log4j.error("Error parsing xsql file", e);
        } catch (final SAXException e) {
          log4j.error("Error parsing xsql file", e);
        } catch (final Exception e) {
          log4j.error("Error parsing xsql file", e);
        }
        if (!sqlc.first) {
          sqlc.printEndClass();
        }
        if (sqlc.importJavaUtil) {
          sqlc.out1.append("import java.util.*;\n");
        }
        if (sqlc.importRDBMSIndependent) {
          sqlc.out1.append("import org.openbravo.database.RDBMSIndependent;\n");
          sqlc.out1.append("import org.openbravo.exception.*;\n");
        }
        sqlc.out.write(sqlc.out1.toString());
        sqlc.out.write(sqlc.out2.toString());
        sqlc.out.flush();
        sqlc.importJavaUtil = false;
        sqlc.importRDBMSIndependent = false;
        resultsFile.close();
        if (resultsFileTxt != null)
          resultsFileTxt.close();
        if (sqlc.error) {
          fileJava.delete();
          if (fileTxt != null) {
            fileTxt.delete();
          }
        }
      } else {
        if (log4j.isDebugEnabled())
          log4j.debug("File: " + fileParsing + " \tskipped");
      }
    } catch (final IOException e) {
      log4j.error("Problem closing the file", e);
    }
  }

  private void pushElement(String name) {
    stcElement.push(name);
    strElement = name;
  }

  private void popElement() {
    strElement = stcElement.pop();
    if (!stcElement.isEmpty())
      strElement = stcElement.peek();
  }

  @Override
  public void startElement(java.lang.String uri, java.lang.String name, java.lang.String qName,
      Attributes amap) { // throws SAXException {
    readBuffer();
    pushElement(qName);
    if (log4j.isDebugEnabled())
      log4j.debug("Configuration: startElement is called: element  name=" + name);
    if (log4j.isDebugEnabled())
      log4j.debug("Configuration: startElement is called: element qName=" + qName);
    if (name.equals("SqlMethod")) {
      sql.sqlStatic = "true";
      sql.sqlConnection = "false";
      String sqlPackage = null;
      final int size = amap.getLength();
      for (int i = 0; i < size; i++) {
        if (amap.getQName(i).equals("name")) {
          sql.sqlName = amap.getValue(i);
        } else if (amap.getQName(i).equals("return")) {
          sql.sqlReturn = amap.getValue(i).trim();
          if (sql.sqlReturn.equalsIgnoreCase("STRING") || sql.sqlReturn.equalsIgnoreCase("BOOLEAN")
              || sql.sqlReturn.equalsIgnoreCase("DATE") || sql.sqlReturn.equalsIgnoreCase("SINGLE")
              || sql.sqlReturn.equalsIgnoreCase("MULTIPLE")) {
            sql.executeType = "executeQuery";
          } else if (sql.sqlReturn.equalsIgnoreCase("ROWCOUNT")
              || sql.sqlReturn.equalsIgnoreCase("SEQUENCE")) {
            sql.executeType = "executeUpdate";
          } else if (sql.sqlReturn.equalsIgnoreCase("OBJECT")) {
            sql.executeType = "execute";
          } else {
            log4j.error("return not permited");
          }
        } else if (amap.getQName(i).equals("default")) {
          sql.sqlDefaultReturn = amap.getValue(i);
        } else if (amap.getQName(i).equals("static")) {
          sql.sqlStatic = amap.getValue(i);
        } else if (amap.getQName(i).equals("connection")) { // it shows
          // that the
          // connection
          // is sent
          // through
          // the
          // window
          sql.sqlConnection = amap.getValue(i);
        } else if (amap.getQName(i).equals("type")) {
          sql.sqlType = amap.getValue(i);
        } else if (amap.getQName(i).equals("object")) {
          sql.sqlObject = amap.getValue(i);
        } else if (amap.getQName(i).equals("package")) {
          sqlPackage = amap.getValue(i);
        } else if (amap.getQName(i).equals("import")) {
          sql.sqlImport = amap.getValue(i);
        }
      }
      if (sqlPackage != null)
        sql.sqlClass = sqlPackage + "." + sql.sqlObject;
      else
        sql.sqlClass = sql.sqlObject;
    } else if (name.equals("SqlClass")) {
      final int size = amap.getLength();
      for (int i = 0; i < size; i++) {
        if (amap.getQName(i).equals("name")) {
          sqlcName = amap.getValue(i);
        } else if (amap.getQName(i).equals("package")) {
          sqlcPackage = amap.getValue(i);
        } else if (amap.getQName(i).equals("accessModifier")) {
          sqlcAccessModifier = amap.getValue(i);
        }
      }
    } else if (name.equals("Parameter")) {
      String strName = null;
      String strDefault = null;
      String strInOut = "in";
      String strOptional = null;
      String strAfter = null;
      String strText = null;
      String strIgnoreValue = null;
      final int size = amap.getLength();
      for (int i = 0; i < size; i++) {
        if (amap.getQName(i).equals("name")) {
          strName = amap.getValue(i);
        } else if (amap.getQName(i).equals("default")) {
          strDefault = amap.getValue(i);
        } else if (amap.getQName(i).equals("type")) {
          strInOut = amap.getValue(i);
        } else if (amap.getQName(i).equals("optional")) {
          strOptional = amap.getValue(i);
        } else if (amap.getQName(i).equals("after")) {
          strAfter = amap.getValue(i);
        } else if (amap.getQName(i).equals("text")) {
          strText = amap.getValue(i);
        } else if (amap.getQName(i).equals("ignoreValue")) {
          strIgnoreValue = amap.getValue(i);
        }
      }
      if (log4j.isDebugEnabled())
        log4j.debug("Configuration: call to addParameter ");
      parameterSql = sql.addParameter(false, strName, strDefault, strInOut, strOptional, strAfter,
          strText, strIgnoreValue);
    } else if (name.equals("Field")) {
      FieldAdded field = null;
      final int size = amap.getLength();
      for (int i = 0; i < size; i++) {
        if (amap.getQName(i).equals("name")) {
          field = new FieldAdded(amap.getValue(i));
          sql.vecFieldAdded.addElement(field);
        } else if (amap.getQName(i).equals("value")) {
          field.strValue = amap.getValue(i);
        }
      }
    } else if (name.equals("Sequence")) {
      final int size = amap.getLength();
      for (int i = 0; i < size; i++) {
        if (amap.getQName(i).equals("name")) {
          sql.strSequenceName = amap.getValue(i);
        }
      }
      parameterSql = sql
          .addParameter(true, sql.strSequenceName, null, null, null, null, null, null);
    }
  }

  @Override
  public void endElement(java.lang.String uri, java.lang.String name, java.lang.String qName) { // throws
    // SAXException
    // {
    readBuffer();
    if (log4j.isDebugEnabled())
      log4j.debug("Configuration: call to endElement: " + name);
    if (log4j.isDebugEnabled())
      log4j.debug("(before pop) Element: " + strElement);
    popElement();
    if (log4j.isDebugEnabled())
      log4j.debug("(after pop) Element: " + strElement);
    if (name.equals("SqlMethod")) {
      if (sql.sqlType.equals("constant")) {
        try {
          printFunctionConstant();
        } catch (final IOException ex) {
          log4j.error("Error in printFunctionConstant", ex);
        }
      } else {
        query();
        if (first) {
          first = false;
          try {
            printInitClass();
          } catch (final IOException ex) {
            log4j.error("Error in printInitClass", ex);
          }
        }
        try {
          printFunctionSql();
        } catch (final IOException ex) {
          log4j.error("Error in printFunctionSql", ex);
        }
      }
      sql = new Sql();
    }
  }

  @Override
  public void characters(char[] ch, int start, int lengthc) throws SAXException {
    if (buffer == null)
      buffer = new StringBuffer();
    buffer.append(ch, start, lengthc);
  }

  private void readBuffer() {
    if (buffer != null) {
      final String strBuffer = buffer.toString();
      if (log4j.isDebugEnabled())
        log4j.debug("Configuration(" + strElement + "): characters are  called: " + strBuffer);
      if (strElement.equals("Sql")) {
        sql.strSQL = strBuffer;
      } else if (strElement.equals("SqlClassComment")) {
        strComments = strBuffer;
      } else if (strElement.equals("SqlMethodComment")) {
        sql.strSqlComments = strBuffer;
      } else if (strElement.equals("Parameter")) {
        parameterSql.strText = strBuffer.replaceAll("\\x0D", " ").replaceAll("\\x0A", " ");
      }
      buffer = null;
    }
  }

  private void connect(String file) throws ClassNotFoundException, SQLException {
    final Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(file));
      strDriver = properties.getProperty("bbdd.driver");
      strURL = properties.getProperty("bbdd.url");
      strDBUser = properties.getProperty("bbdd.user");
      strDBPassword = properties.getProperty("bbdd.password");
      if (properties.getProperty("bbdd.rdbms").equalsIgnoreCase("POSTGRE"))
        strURL += "/" + properties.getProperty("bbdd.sid");
      // read from properties file
      queryExecutionStrategy = properties.getProperty("sqlc.queryExecutionStrategy");
      // override with value passed from command line/build.xml invocation
      queryExecutionStrategy = System.getProperty("sqlc.queryExecutionStrategy",
          queryExecutionStrategy);
      // if strategy is not set, default to optimized
      if (queryExecutionStrategy == null) {
        queryExecutionStrategy = "optimized";
      }
    } catch (final IOException e) {
      log4j.error("Error reading propery file", e);
    }

    log4j.info("QueryExecutionStrategy: " + queryExecutionStrategy);
    log4j.info("Loading driver: " + strDriver);
    Class.forName(strDriver);
    log4j.info("Driver loaded");
    if (strDBUser == null || strDBUser.equals(""))
      connection = DriverManager.getConnection(strURL);
    else
      connection = DriverManager.getConnection(strURL, strDBUser, strDBPassword);
    log4j.info("connect made");
  }

  private void closeConnection() {
    try {
      connection.close();
    } catch (final SQLException e) {
      log4j.error("SQL error in closeConnection: " + e);
    }
  }

  private void query() {
    // try to build a more complete query string, but appending the optional parameters where this
    // is possible, this should lead to improved execution time of the query and compile time (here
    // in Sqlc)
    StringBuilder querySql = new StringBuilder();

    int posSQL = 0;
    for (final Parameter parameter : sql.vecParameter) {
      if (parameter.boolOptional) {
        if (parameter.strAfter == null) {
          parameter.strAfter = "WHERE";
          parameter.strText = parameter.strName + " = ? AND";
        }
        if (parameter.strName != null && !parameter.strInOut.equals("out")) {
          int posFinalAfter = posFinal(sql.strSQL, parameter.strAfter);
          if (posFinalAfter != -1) {
            querySql.append(imprimirSubstring2(sql.strSQL, posSQL, posFinalAfter));
            posSQL = posFinalAfter;
            if (parameter.strInOut.equals("none")) {
              if (queryWithOptionalParameterTypeNone) {
                querySql.append(parameter.strText).append('\n');
              }
            } else if (parameter.strInOut.equals("argument")) {
              if (queryWithOptionalParameterTypeArgument) {
                // heuristic: search for strText ending in "_ID in"
                if (parameter.strText.trim().endsWith("_ID IN")) {
                  querySql.append(parameter.strText);
                  querySql.append("('1')");
                  querySql.append('\n');
                }
              }
            } else if (parameter.strInOut.equals("replace")) {
              // don't append replace type parameters
            } else { // without type
              if (queryWithOptionalParameterWithoutType) {
                querySql.append(parameter.strText).append('\n');
              }
            }
          } else if (parameter.strInOut.equals("replace")) {
            // don't append replace type parameters
          }
        }
      }
    }
    querySql.append(imprimirSubstring2(sql.strSQL, posSQL, sql.strSQL.length()));

    try {
      if (preparedStatement != null)
        preparedStatement.close();
      preparedStatement = connection.prepareStatement(querySql.toString());
      if (log4j.isDebugEnabled())
        log4j.debug("Prepared statement: " + sql.strSQL);

      /*
       * Commented because it is not a supported operation ResultSetMetaData rsmdPS =
       * preparedStatement.getMetaData (); // Get the number of columns in the result set int
       * numColsPS = rsmdPS.getColumnCount (); if (log4j.isDebugEnabled())
       * log4j.debug("number of columns in PS: " + numColsPS);
       */
      int i = 1;
      for (final Parameter parameter : sql.vecParameter) {
        boolean isParameterWithoutType = !parameter.strInOut.equals("out")
            && !parameter.strInOut.equals("none") && !parameter.strInOut.equals("argument")
            && !parameter.strInOut.equals("replace");
        if (!parameter.boolOptional
            || (parameter.boolOptional && (isParameterWithoutType && queryWithOptionalParameterWithoutType))) {
          if (parameter.type == java.sql.Types.INTEGER) {
            if (parameter.strDefault == null) {
              if (log4j.isDebugEnabled())
                log4j.debug("setInt: " + i + " value: 0");
              preparedStatement.setInt(i, 0);
            } else {
              if (log4j.isDebugEnabled())
                log4j.debug("setInt: " + i + " value: " + parameter.strDefault);
              preparedStatement.setInt(i, Integer.parseInt(parameter.strDefault));
            }
            preparedStatement.setInt(i, Integer.parseInt(parameter.strDefault));
          } else if (parameter.type == java.sql.Types.VARCHAR) {
            if (parameter.strDefault == null) {
              if (log4j.isDebugEnabled())
                log4j.debug("setString: " + i + " value: null");
              preparedStatement.setNull(i, java.sql.Types.VARCHAR);
            } else {
              if (log4j.isDebugEnabled())
                log4j.debug("setString: " + i + " value: " + parameter.strDefault);
              preparedStatement.setString(i, parameter.strDefault);
            }
          }
          i++;
        }
      }
      // Get the ResultSetMetaData. This will be used for
      // the column headings
      if (sql.executeType.equals("executeQuery")) {
        result = preparedStatement.executeQuery();
        if (log4j.isDebugEnabled())
          log4j.debug("query done");
        rsmd = result.getMetaData();
        // Get the number of columns in the result set
        numCols = rsmd.getColumnCount();
        if (log4j.isDebugEnabled())
          log4j.debug("number of columns: " + numCols);
      } // else
      // rsmd = preparedStatement.getMetaData ();
    } catch (final SQLException e) {
      error = true;
      errorNum++;
      log4j.error("SQL error in query: " + querySql, e);
    } catch (final Exception e) {
      error = true;
      errorNum++;
      log4j.error("Error in query.", e);
    }
  }

  private void printInitClass() throws IOException {
    if (this.writeTxtFiles)
      printTxtFile();
    out1.append("//Sqlc generated " + VERSION + "\n"); // v0.011");
    if (sqlcPackage != null) {
      out1.append("package " + sqlcPackage + ";\n");
    }
    out1.append("\n");
    out1.append("import java.sql.*;\n");
    out1.append("\n");
    out1.append("import org.apache.log4j.Logger;\n");
    out1.append("\n");
    out1.append("import javax.servlet.ServletException;\n");
    out1.append("\n");
    out1.append("import org.openbravo.data.FieldProvider;\n");
    out1.append("import org.openbravo.database.ConnectionProvider;\n");
    out1.append("import org.openbravo.data.UtilSql;\n");

    if (sql.sqlImport != null) {
      out2.append("import " + sql.sqlImport + ";\n");
    }
    out2.append("\n");

    final String[] strCommentsVector = stringToVector(strComments, false);
    for (int i = 0; i < strCommentsVector.length; i++) {
      if (i == 0) {
        out2.append("/**\n" + strCommentsVector[i] + "\n");
      } else {
        out2.append(" *" + strCommentsVector[i] + "\n");
      }
      if (i == strCommentsVector.length - 1) {
        out2.append(" */\n");
      }
    }
    if (!javaFileName.equals(sqlcName))
      throw new IOException("File name for xsql class " + javaFileName
          + " is different than the class name defined inside the file: " + sqlcName);
    if (sqlcAccessModifier.length() > 0) {
      out2.append(sqlcAccessModifier);
      out2.append(" ");
    }
    out2.append("class " + sqlcName + " implements FieldProvider {\n");
    out2.append("static Logger log4j = Logger.getLogger(" + sqlcName + ".class);\n");
    try {
      // Display column headings
      if (log4j.isDebugEnabled())
        log4j.debug("Number of columns: " + numCols);
      out2.append("  private String InitRecordNumber=\"0\";\n");
      for (int i = 1; i <= numCols; i++) {
        out2.append("  public String ");
        out2.append(TransformaNombreColumna(rsmd.getColumnLabel(i)));
        out2.append(";\n");
      }
      for (final Enumeration<Object> e = sql.vecFieldAdded.elements(); e.hasMoreElements();) {
        final FieldAdded fieldAdded = (FieldAdded) e.nextElement();
        out2.append("  public String ");
        out2.append(fieldAdded.strName);
        out2.append(";\n");
      }
    } catch (final SQLException e) {
      log4j.error("SQL Exception error:" + e);
    }
    out2.append("\n");
    out2.append("  public String getInitRecordNumber() {\n");
    out2.append("    return InitRecordNumber;\n");
    out2.append("  }\n");
    // the getField function
    out2.append("\n");
    out2.append("  public String getField(String fieldName) {\n");
    try {
      // Display column headings
      if (log4j.isDebugEnabled())
        log4j.debug("Number of columns in getField: " + numCols);
      for (int i = 1; i <= numCols; i++) {
        final String columnLabel = rsmd.getColumnLabel(i);
        final String transformedColumnLabel = TransformaNombreColumna(columnLabel);
        if (i == 1) {
          out2.append("    if ");
        } else {
          out2.append("    else if ");
        }
        out2.append("(fieldName.equalsIgnoreCase(\"");
        out2.append(columnLabel);
        if (!columnLabel.equalsIgnoreCase(transformedColumnLabel))
          out2.append("\") || fieldName.equals(\"" + transformedColumnLabel);
        out2.append("\"))\n");
        out2.append("      return " + transformedColumnLabel + ";\n");
      }
      for (final Enumeration<Object> e = sql.vecFieldAdded.elements(); e.hasMoreElements();) {
        final FieldAdded fieldAdded = (FieldAdded) e.nextElement();
        out2.append("    else if ");
        out2.append("(fieldName.equals(\"");
        out2.append(fieldAdded.strName + "\"))\n");
        out2.append("      return " + fieldAdded.strName + ";\n");
      }
    } catch (final SQLException e) {
      log4j.error("SQL Exception error:" + e);
    }
    out2.append("   else {\n");
    out2.append("     log4j.debug(\"Field does not exist: \" + fieldName);\n");
    out2.append("     return null;\n");
    out2.append("   }\n");
    out2.append(" }\n");
  }

  private void printTxtFile() {
    try {
      // Display column headings
      if (log4j.isDebugEnabled())
        log4j.debug("Printing txt File: " + numCols);
      for (int i = 1; i <= numCols; i++) {
        printWriterTxt.print(rsmd.getColumnLabel(i));
        if (i == numCols) {
          printWriterTxt.println("");
        } else {
          printWriterTxt.print(", ");
        }
      }
      printWriterTxt.println("");
      for (int i = 1; i <= numCols; i++) {
        printWriterTxt.println("<FIELD id=\""
            + TransformaNombreColumna("field_" + rsmd.getColumnLabel(i)) + "\">"
            + TransformaNombreColumna(rsmd.getColumnLabel(i)) + "</FIELD>");
      }
      printWriterTxt.println("");
      for (int i = 1; i <= numCols; i++) {
        printWriterTxt.println("      <Parameter name=\""
            + TransformaNombreColumna(rsmd.getColumnLabel(i)) + "\"/>");
      }
      printWriterTxt.println("");
      printWriterTxt.println("  " + sqlcName + " getEditVariables(VariablesSapp vars) {");
      printWriterTxt.println("    " + sqlcName + " data = new " + sqlcName + "();");
      for (int i = 1; i <= numCols; i++) {
        printWriterTxt.println("    data." + TransformaNombreColumna(rsmd.getColumnLabel(i))
            + " = vars.getStringParameter(\""
            + TransformaNombreColumna("inp_" + rsmd.getColumnLabel(i)) + "\");");
      }
      printWriterTxt.println("    data.a1Usuario = vars.getUser();");
      printWriterTxt.println("    data.a1Rol = vars.getRole();");
      printWriterTxt.println("    return data;");
      printWriterTxt.println("  }");
    } catch (final SQLException e) {
      log4j.error("SQL Exception error:" + e);
    }
  }

  private void printFunctionConstant() throws IOException {
    printHeadFunctionSql(false, false, false);
    out2.append("    " + sqlcName + " object" + sqlcName + "[] = new " + sqlcName + "[1];\n");
    out2.append("    object" + sqlcName + "[0] = new " + sqlcName + "();\n");
    try {
      for (int i = 1; i <= numCols; i++) {
        final String strNameLabel = rsmd.getColumnLabel(i);
        out2.append("    object" + sqlcName + "[0]." + TransformaNombreColumna(strNameLabel)
            + " = ");
        boolean printedParameter = false;
        for (final Parameter parameter : sql.vecParameter) {
          if (parameter.strName.equals(TransformaNombreColumna(strNameLabel)) && !printedParameter) {
            out2.append(TransformaNombreColumna(strNameLabel) + ";\n");
            printedParameter = true;
          }
        }
        if (!printedParameter) {
          out2.append("\"\";\n");
        }
      }
    } catch (final SQLException e) {
      log4j.error("SQL Exception error:" + e);
    }
    out2.append("    return object" + sqlcName + ";\n");
    out2.append("  }\n");
  }

  private void printSQLBody() throws IOException {
    // codigo para imprimir trozos de Sql opcionales
    int posSQL = 0;
    out2.append("    String strSql = \"\";\n");
    for (final Parameter parameter : sql.vecParameter) {
      if (parameter.boolOptional) {
        if (parameter.strAfter == null) {
          parameter.strAfter = "WHERE";
          parameter.strText = parameter.strName + " = ? AND";
        }
        if (parameter.strName != null && // se deben imprimir los
            // repetidos, quitar:
            // !parameter.boolRepeated
            // &&
            !parameter.strInOut.equals("out")) {
          int posFinalAfter = posFinal(sql.strSQL, parameter.strAfter);
          if (posFinalAfter != -1) {
            imprimirSubstring(sql.strSQL, posSQL, posFinalAfter, out);
            posSQL = posFinalAfter
                + (parameter.strInOut.equals("replace") ? parameter.strText.length() : 0);
            if (parameter.strInOut.equals("none")) {
              out2.append("    strSql = strSql + ((" + parameter.strName + ".equals(\""
                  + parameter.strName + "\"))?\" " + parameter.strText + " \":\"\");\n");
            } else if (parameter.strInOut.equals("argument")) {
              out2.append("    strSql = strSql + ((" + parameter.strName + "==null || "
                  + parameter.strName + ".equals(\"\")");
              if (parameter.strIgnoreValue != null) {
                out2.append(" || " + parameter.strName + ".equals(\"" + parameter.strIgnoreValue
                    + "\") ");
              }
              out2.append(")?\"\":\" " + parameter.strText + "\" + " + parameter.strName + ");\n");
            } else if (parameter.strInOut.equals("replace")) {
              out2.append("    strSql = strSql + ((" + parameter.strName + "==null || "
                  + parameter.strName + ".equals(\"\")");
              if (parameter.strIgnoreValue != null) {
                out2.append(" || " + parameter.strName + ".equals(\"" + parameter.strIgnoreValue
                    + "\") ");
              }
              out2.append(")?\"\":" + parameter.strName + ");\n");

            } else {
              out2.append("    strSql = strSql + ((" + parameter.strName + "==null || "
                  + parameter.strName + ".equals(\"\")");
              if (parameter.strIgnoreValue != null) {
                out2.append(" || " + parameter.strName + ".equals(\"" + parameter.strIgnoreValue
                    + "\") ");
              }
              out2.append(")?\"\":\" " + parameter.strText + " \");\n");
            }
          } else if (parameter.strInOut.equals("replace")) {
            posFinalAfter = 0;
            out2.append("    strSql = strSql + ((" + parameter.strName + "==null || "
                + parameter.strName + ".equals(\"\"))?\"\":" + parameter.strName + ");\n");
          } else {
            log4j.error(sqlcName + "." + sql.sqlName + " position after = \"" + parameter.strAfter
                + "\" for optional parameter " + parameter.strName + " not found in xsql file!");
          }
        }
      }
    }
    imprimirSubstring(sql.strSQL, posSQL, sql.strSQL.length(), out);

    out2.append("\n");

    if (sql.executeType.equals("executeQuery")) {
      out2.append("    ResultSet result;\n");
    }
    if (sql.sqlReturn.equalsIgnoreCase("MULTIPLE")) {
      importJavaUtil = true;
      out2.append("    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);\n");
    } else if (sql.sqlReturn.equalsIgnoreCase("SINGLE")) {
      out2.append("    " + sqlcName + " object" + sqlcName + " = new " + sqlcName + "();\n");
    } else if (sql.sqlReturn.equalsIgnoreCase("STRING")) {
      out2.append("    String strReturn = ");
      if (sql.sqlDefaultReturn != null) {
        out2.append("\"" + sql.sqlDefaultReturn + "\";\n");
      } else {
        out2.append("null;\n"); // if the return value is not specified
        // then set to null
      }
    } else if (sql.sqlReturn.equalsIgnoreCase("BOOLEAN")) {
      out2.append("    boolean boolReturn = ");
      if (sql.sqlDefaultReturn != null) {
        out2.append(sql.sqlDefaultReturn + ";\n");
      } else {
        out2.append("false;\n"); // if the return value is not specified
        // then set to false
      }
    } else if (sql.sqlReturn.equalsIgnoreCase("DATE")) {
      out2.append("    String dateReturn = ");
      if (sql.sqlDefaultReturn != null) {
        out2.append(sql.sqlDefaultReturn + ";\n");
      } else {
        out2.append("null;\n"); // if the return value is not specified
        // then set to null
      }
    } else if (sql.sqlReturn.equalsIgnoreCase("OBJECT")) {
      // out2.append(
      // "    SqlRespuestaCS objectSqlRespuestaCS = new SqlRespuestaCS();"
      // );
      out2.append("    " + sql.sqlClass + " object" + sql.sqlObject + " = new " + sql.sqlClass
          + "();\n");
    }
    if (sql.executeType.equals("executeUpdate")) {
      out2.append("    int updateCount = 0;\n");
    }

    if (sql.sqlType.equals("preparedStatement")) {
      out2.append("    PreparedStatement st = null;\n");
    } else if (sql.sqlType.equals("statement")) {
      out2.append("    Statement st = null;\n");
    } else if (sql.sqlType.equals("callableStatement")) {
      out2.append("    CallableStatement st = null;\n");
      out2.append("    if (connectionProvider.getRDBMS().equalsIgnoreCase(\"ORACLE\")) {\n");
    }
    out2.append("\n");
  }

  private void printSQLParameters() throws IOException {
    final StringBuffer aux = new StringBuffer();
    boolean declareiParameter = false;

    aux.append("    try {\n");
    if (sql.sqlType.equals("preparedStatement")) {
      aux.append("    st = connectionProvider.getPreparedStatement(");
      if (sql.sqlConnection.equals("true"))
        aux.append("conn, ");
      aux.append("strSql);\n");
    } else if (sql.sqlType.equals("statement")) {
      aux.append("    st = connectionProvider.getStatement(");
      if (sql.sqlConnection.equals("true"))
        aux.append("conn");
      aux.append(");\n");
    } else if (sql.sqlType.equals("callableStatement")) {
      aux.append("      st = connectionProvider.getCallableStatement(");
      if (sql.sqlConnection.equals("true"))
        aux.append("conn, ");
      aux.append("strSql);\n");
    }
    // set value of parameters
    for (final Parameter parameter : sql.vecParameter) {
      if (parameter.boolSequence) {
        declareiParameter = true;
        aux.append("      iParameter++; st.setLong(iParameter, keySequence);\n");
      } else if (parameter.strName != null) {
        if (parameter.strInOut.equals("out") || parameter.strInOut.equals("inOut")) {
          declareiParameter = true;
          aux.append("      int iParameter" + parameter.strName + " = iParameter + 1;\n");
        }
        if (parameter.boolOptional) {
          aux.append("      if (" + parameter.strName + " != null && !(" + parameter.strName
              + ".equals(\"\"))");
          if (parameter.strIgnoreValue != null) {
            aux.append(" && !(" + parameter.strName + ".equals(\"" + parameter.strIgnoreValue
                + "\"))");
          }
          aux.append(") {\n");
          aux.append("  ");
        }
        if (parameter.strInOut.equals("in") || parameter.strInOut.equals("inOut")) {
          declareiParameter = true;
          aux.append("      iParameter++; UtilSql.setValue(st, iParameter, " + parameter.type
              + ", ");
          if (parameter.strDefault == null) {
            aux.append("null, ");
          } else {
            aux.append("\"" + parameter.strDefault + "\", ");
          }
          aux.append(parameter.strName + ");\n");
        }
        if (parameter.strInOut.equals("out")) {
          declareiParameter = true;
          aux.append("      iParameter++; st.registerOutParameter(iParameter, " + parameter.type
              + ");\n");
        } else if (parameter.strInOut.equals("inOut")) { // in this case
          // iParamter
          // is
          // increased
          // ;
          declareiParameter = true;
          aux.append("      st.registerOutParameter(iParameter, " + parameter.type + ");\n");
        }
        if (parameter.boolOptional) {
          aux.append("      }\n");
        }
      }
    }

    aux.append("\n");
    if (declareiParameter)
      out2.append("    int iParameter = 0;\n");
    out2.append(aux.toString());
  }

  private void printFunctionSql() throws IOException {
    boolean boolSequence = false;
    // *** Call to the argument-less creation header, who calls the header
    // with them
    if (sql.sqlReturn.equalsIgnoreCase("MULTIPLE")) { // parameters por
      // paging the output
      // && 1==2
      printHeadFunctionSql(true, false, false);
      out2.append("    return ");
      printCallFunctionSql(true);
      out2.append(";\n");
      out2.append("  }\n");
      boolSequence = true;
    }

    printHeadFunctionSql(true, boolSequence, false);
    // Sequences
    if (sql.strSequenceName != null) {
      out2.append("    long keySequence = 0;\n");
      out2.append("    String strSql1 = \"SELECT " + sql.strSequenceName);
      out2.append(".NEXTVAL AS KEY_" + sql.strSequenceName.replace(".", "_") + " FROM DUAL\";\n");
      out2.append("    PreparedStatement psl = null;\n");
      out2.append("    try {\n");
      out2.append("      psl = connectionProvider.getPreparedStatement(");
      if (sql.sqlConnection.equals("true"))
        out2.append("conn, ");
      out2.append("strSql1);\n");
      out2.append("      ResultSet resultKey;\n");
      out2.append("      resultKey = psl.executeQuery();\n");
      out2.append("      if(resultKey.next()){\n");
      out2.append("        keySequence = resultKey.getLong(\"KEY_"
          + sql.strSequenceName.replace(".", "_") + "\");\n");
      out2.append("      }\n");
      out2.append("      resultKey.close();\n");
      out2.append("    } catch(SQLException e){\n");
      out2.append("      log4j.error(\"SQL error in query: \" + strSql1 + \"Exception:\"+ e);\n");
      out2.append("      throw new ServletException(\"@CODE=\" + Integer.toString(e.getErrorCode()) + \"@\" + e.getMessage());\n");
      out2.append("    } catch(Exception ex){\n");
      out2.append("      log4j.error(\"Exception in query: \" + strSql1 + \"Exception:\"+ ex);\n");
      out2.append("      throw new ServletException(\"@CODE=@\" + ex.getMessage());\n");
      out2.append("    } finally {\n");
      out2.append("      try {\n");
      if (!sql.sqlConnection.equals("true")) {
        if (sql.sqlType.equals("statement"))
          out2.append("        connectionProvider.releaseStatement(psl);\n");
        else
          out2.append("        connectionProvider.releasePreparedStatement(psl);\n");
      } else {
        if (sql.sqlType.equals("statement"))
          out2.append("       connectionProvider.releaseTransactionalStatement(psl);\n");
        else if (sql.sqlType.equalsIgnoreCase("preparedstatement"))
          out2.append("        connectionProvider.releaseTransactionalPreparedStatement(psl);\n");
      }
      out2.append("      } catch(Exception ignore){\n");
      out2.append("        ignore.printStackTrace();\n");
      out2.append("      }\n");
      out2.append("    }\n");
      out2.append("\n");
    }

    printSQLBody();
    printSQLParameters();

    if (sql.executeType.equals("executeQuery")) {
      out2.append("      result = st." + sql.executeType + "(");
      if (sql.sqlType.equals("statement"))
        out2.append("strSql");
      out2.append(");\n");
      if (sql.sqlReturn.equalsIgnoreCase("MULTIPLE")) {
        out2.append("      long countRecord = 0;\n");
        // paging
        out2.append("      long countRecordSkip = 1;\n");
        out2.append("      boolean continueResult = true;\n");
        out2.append("      while(countRecordSkip < firstRegister && continueResult) {\n");
        out2.append("        continueResult = result.next();\n");
        out2.append("        countRecordSkip++;\n");
        out2.append("      }\n");
        // paging
        out2.append("      while(continueResult && result.next()) {\n");
        out2.append("        countRecord++;\n");
        out2.append("        " + sqlcName + " object" + sqlcName + " = new " + sqlcName + "();\n");
      } else {
        out2.append("      if(result.next()) {\n");
      }
      try {
        if (sql.sqlReturn.equalsIgnoreCase("STRING")) {
          out2.append("        strReturn = UtilSql.getValue(result, \"" + rsmd.getColumnLabel(1)
              + "\");\n");
        } else if (sql.sqlReturn.equalsIgnoreCase("BOOLEAN")) {
          // getBoolean works different in ORACLE and PostgreSQL (2 is
          // true for Oracle and false for PostgreSQL),
          // so we make the comparation here as strings with true if
          // the result is different of 0.
          // out2.append("        boolReturn = result.getBoolean(\"" +
          // rsmd.getColumnLabel(1) +"\");\n");
          out2.append("        boolReturn = !UtilSql.getValue(result, \"" + rsmd.getColumnLabel(1)
              + "\").equals(\"0\");\n");
        } else if (sql.sqlReturn.equalsIgnoreCase("DATE")) {
          out2.append("        dateReturn = UtilSql.getDateValue(result, \""
              + rsmd.getColumnLabel(1) + "\", \"" + javaDateFormat + "\");\n");
        } else {
          for (int i = 1; i <= numCols; i++) {
            if (log4j.isDebugEnabled())
              log4j.debug("Columna: " + rsmd.getColumnName(i) + " tipo: " + rsmd.getColumnType(i));
            if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP || rsmd.getColumnType(i) == 91) {
              out2.append("        object" + sqlcName + "."
                  + TransformaNombreColumna(rsmd.getColumnLabel(i))
                  + " = UtilSql.getDateValue(result, \"" + rsmd.getColumnLabel(i) + "\", \""
                  + javaDateFormat + "\");\n");
            } else if (rsmd.getColumnType(i) == java.sql.Types.BLOB) {
              out2.append("        object" + sqlcName + "."
                  + TransformaNombreColumna(rsmd.getColumnLabel(i))
                  + " = UtilSql.getBlobValue(result, \"" + rsmd.getColumnLabel(i) + "\");\n");
            } else {
              out2.append("        object" + sqlcName + "."
                  + TransformaNombreColumna(rsmd.getColumnLabel(i))
                  + " = UtilSql.getValue(result, \"" + rsmd.getColumnLabel(i) + "\");\n");
            }
          }
          for (final Enumeration<Object> e = sql.vecFieldAdded.elements(); e.hasMoreElements();) {
            final FieldAdded fieldAdded = (FieldAdded) e.nextElement();
            if (fieldAdded.strValue.equals("count"))
              out2.append("        object" + sqlcName + "." + fieldAdded.strName
                  + " = Long.toString(countRecord);\n");
            else if (fieldAdded.strValue.equals("void"))
              out2.append("        object" + sqlcName + "." + fieldAdded.strName + " = \"\";\n");
          }
          if (sql.sqlReturn.equalsIgnoreCase("MULTIPLE"))
            out2.append("        object" + sqlcName
                + ".InitRecordNumber = Integer.toString(firstRegister);\n");
        }
      } catch (final SQLException e) {
        log4j.error("SQL Exception error:", e);
      }
      if (sql.sqlReturn.equalsIgnoreCase("MULTIPLE")) {
        out2.append("        vector.addElement(object" + sqlcName + ");\n");
        // paging
        out2.append("        if (countRecord >= numberRegisters && numberRegisters != 0) {\n");
        out2.append("          continueResult = false;\n");
        out2.append("        }\n");
        // paging
      }
      out2.append("      }\n");
      out2.append("      result.close();\n");
    } else if (sql.executeType.equals("executeUpdate")) {
      out2.append("      updateCount = st." + sql.executeType + "(");
      if (sql.sqlType.equals("statement"))
        out2.append("strSql");
      out2.append(");\n");
    } else if (sql.executeType.equals("execute")) {
      out2.append("      st." + sql.executeType + "();\n");
      if (sql.sqlReturn.equalsIgnoreCase("OBJECT")) {
        for (final Parameter parameter : sql.vecParameter) {
          if (!parameter.boolSequence && parameter.strName != null) {
            if (parameter.strInOut.equals("out") || parameter.strInOut.equals("inOut")) {
              out2.append("      object" + sql.sqlObject + "." + parameter.strName);
              // out2.append(" = st.getString(" + jParameter +
              // ");"); //", " + parameter.type+ ");");
              // out2.append(" = st.getString( iParameter" +
              // parameter.strName + ");"); //", " +
              // parameter.type+ ");");
              out2.append("= UtilSql.getStringCallableStatement(st, iParameter" + parameter.strName
                  + ");\n");
            }
          }
        }
      }
    }
    out2.append("    } catch(SQLException e){\n");
    out2.append("      log4j.error(\"SQL error in query: \" + strSql + \"Exception:\"+ e);\n");
    out2.append("      throw new ServletException(\"@CODE=\" + Integer.toString(e.getErrorCode()) + \"@\" + e.getMessage());\n");
    out2.append("    } catch(Exception ex){\n");
    out2.append("      log4j.error(\"Exception in query: \" + strSql + \"Exception:\"+ ex);\n");
    out2.append("      throw new ServletException(\"@CODE=@\" + ex.getMessage());\n");
    out2.append("    } finally {\n");
    out2.append("      try {\n");
    if (!sql.sqlConnection.equals("true")) {
      if (sql.sqlType.equals("statement"))
        out2.append("        connectionProvider.releaseStatement(st);\n");
      else
        out2.append("        connectionProvider.releasePreparedStatement(st);\n");
    } else {
      if (sql.sqlType.equals("statement"))
        out2.append("        connectionProvider.releaseTransactionalStatement(st);\n");
      else if (sql.sqlType.equalsIgnoreCase("preparedstatement"))
        out2.append("        connectionProvider.releaseTransactionalPreparedStatement(st);\n");
    }
    out2.append("      } catch(Exception ignore){\n");
    out2.append("        ignore.printStackTrace();\n");
    out2.append("      }\n");
    out2.append("    }\n");
    if (sql.sqlType.equals("callableStatement")) {
      out2.append("    }\n");
      out2.append("    else {\n");
      importJavaUtil = true;
      out2.append("      Vector<String> parametersData = new Vector<String>();\n");
      out2.append("      Vector<String> parametersTypes = new Vector<String>();\n");
      int outParams = 0;
      String outParamName = "";
      final StringBuffer paramsReceipt = new StringBuffer();
      for (final Parameter parameter : sql.vecParameter) {
        if (!parameter.strInOut.equals("none") && !parameter.strInOut.equals("argument")
            && !parameter.strInOut.equals("replace")) {
          out2.append("      parametersData.addElement("
              + (parameter.strInOut.equalsIgnoreCase("out") ? "\"" + parameter.strName + "\""
                  : parameter.strName) + ");\n");
          out2.append("      parametersTypes.addElement(\"" + parameter.strInOut + "\");\n");
          if (parameter.strInOut.equals("out")) {
            outParamName = parameter.strName;
            paramsReceipt.append("      object").append(sql.sqlObject).append(".")
                .append(outParamName).append(" = (String) vecTotal.elementAt(").append(outParams)
                .append(");\n");
            outParams++;
          }
        }
      }
      if (outParams > 0)
        out2.append("      Vector<String> vecTotal = new Vector<String>();\n");
      out2.append("      try {\n");
      if (outParams > 0)
        out2.append("        vecTotal = ");
      importRDBMSIndependent = true;
      out2.append("      RDBMSIndependent.getCallableResult("
          + (sql.sqlConnection.equals("true") ? "conn" : "null")
          + ", connectionProvider, strSql, parametersData, parametersTypes, " + outParams + ");\n");
      if (outParams > 0)
        out2.append(paramsReceipt.toString());
      out2.append("      } catch(SQLException e){\n");
      out2.append("        log4j.error(\"SQL error in query: \" + strSql + \"Exception:\"+ e);\n");
      out2.append("        throw new ServletException(\"@CODE=\" + Integer.toString(e.getErrorCode()) + \"@\" + e.getMessage());\n");
      out2.append("      } catch(NoConnectionAvailableException ec){\n");
      out2.append("        log4j.error(\"Connection error in query: \" + strSql + \"Exception:\"+ ec);\n");
      out2.append("        throw new ServletException(\"@CODE=NoConnectionAvailable\");\n");
      out2.append("      } catch(PoolNotFoundException ep){\n");
      out2.append("        log4j.error(\"Pool error in query: \" + strSql + \"Exception:\"+ ep);\n");
      out2.append("        throw new ServletException(\"@CODE=NoConnectionAvailable\");\n");
      out2.append("      } catch(Exception ex){\n");
      out2.append("        log4j.error(\"Exception in query: \" + strSql + \"Exception:\"+ ex);\n");
      out2.append("        throw new ServletException(\"@CODE=@\" + ex.getMessage());\n");
      out2.append("      }\n");
      out2.append("    }\n");
    }

    if (sql.sqlReturn.equalsIgnoreCase("MULTIPLE")) {
      out2.append("    " + sqlcName + " object" + sqlcName + "[] = new " + sqlcName
          + "[vector.size()];\n");
      out2.append("    vector.copyInto(object" + sqlcName + ");\n");
    }

    if (sql.sqlReturn.equalsIgnoreCase("MULTIPLE")) {
      out2.append("    return(object" + sqlcName + ");\n");
    } else if (sql.sqlReturn.equalsIgnoreCase("SINGLE")) {
      out2.append("    return(object" + sqlcName + ");\n");
    } else if (sql.sqlReturn.equalsIgnoreCase("STRING")) {
      out2.append("    return(strReturn);\n");
    } else if (sql.sqlReturn.equalsIgnoreCase("BOOLEAN")) {
      out2.append("    return(boolReturn);\n");
    } else if (sql.sqlReturn.equalsIgnoreCase("DATE")) {
      out2.append("    return(dateReturn);\n");
    } else if (sql.sqlReturn.equalsIgnoreCase("ROWCOUNT")) {
      out2.append("    return(updateCount);\n");
    } else if (sql.sqlReturn.equalsIgnoreCase("SEQUENCE")) {
      out2.append("    return(Long.toString(keySequence));\n");
    } else if (sql.sqlReturn.equalsIgnoreCase("OBJECT")) {
      out2.append("    return(object" + sql.sqlObject + ");\n");
    }
    out2.append("  }\n");
  }

  private void printHeadFunctionSql(boolean printProviderConnection, boolean boolPagin,
      boolean boolSequence) throws IOException {
    out2.append("\n");
    final String[] strSqlCommentsVector = stringToVector(sql.strSqlComments, false);
    for (int i = 0; i < strSqlCommentsVector.length; i++) {
      if (i == 0) {
        out2.append("/**\n" + strSqlCommentsVector[i] + "\n");
      } else {
        out2.append(" *" + strSqlCommentsVector[i] + "\n");
      }
      if (i == strSqlCommentsVector.length - 1) {
        out2.append(" */\n");
      }
    }
    out2.append("  public ");
    if (sql.sqlStatic.equals("true")) {
      out2.append("static ");
    }
    if (sql.sqlReturn.equalsIgnoreCase("MULTIPLE")) {
      out2.append(sqlcName + "[] ");
    } else if (sql.sqlReturn.equalsIgnoreCase("SINGLE")) {
      out2.append(sqlcName + " ");
    } else if (sql.sqlReturn.equalsIgnoreCase("STRING")) {
      out2.append("String ");
    } else if (sql.sqlReturn.equalsIgnoreCase("BOOLEAN")) {
      out2.append("boolean ");
    } else if (sql.sqlReturn.equalsIgnoreCase("DATE")) {
      out2.append("String ");
    } else if (sql.sqlReturn.equalsIgnoreCase("SEQUENCE")) {
      out2.append("String ");
    } else if (sql.sqlReturn.equalsIgnoreCase("ROWCOUNT")) {
      out2.append("int ");
    } else if (sql.sqlReturn.equalsIgnoreCase("OBJECT")) {
      out2.append(sql.sqlClass + " ");
    }
    out2.append(sql.sqlName + "(");
    boolean firstParameter = true;
    if (sql.sqlConnection.equals("true")) {
      firstParameter = false;
      out2.append("Connection conn");
    }
    if (printProviderConnection) {
      if (firstParameter) {
        firstParameter = false;
      } else {
        out2.append(", ");
      }
      out2.append("ConnectionProvider connectionProvider");
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Parameters numbering");
    for (final Parameter parameter : sql.vecParameter) {
      if (sql.sqlStatic.equals("true")) {
        if (parameter.strName != null && !parameter.boolRepeated && !parameter.boolSequence
            && !parameter.strInOut.equals("out")) {
          if (firstParameter) {
            firstParameter = false;
          } else {
            out2.append(", ");
          }
          out2.append("String " + parameter.strName);
        }
      }
    }
    if (boolPagin) { // parameters por paging the output
      if (firstParameter) {
        firstParameter = false;
      } else {
        out2.append(", ");
      }
      out2.append("int firstRegister, int numberRegisters");
    } // parameters por paging the output

    if (boolSequence) {
      if (firstParameter) {
        firstParameter = false;
      } else {
        out2.append(", ");
      }
      out2.append("String keyValue, String keyName, int numberRegisters");
    }
    out2.append(")");
    out2.append("    throws ServletException {\n");
  }

  private void printCallFunctionSql(boolean printProviderConnection) throws IOException {
    out2.append(sql.sqlName + "(");
    boolean firstParameter = true;
    if (sql.sqlConnection.equals("true")) {
      firstParameter = false;
      out2.append("conn");
    }
    if (printProviderConnection) {
      if (firstParameter) {
        firstParameter = false;
      } else {
        out2.append(", ");
      }
      out2.append("connectionProvider");
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Parameters numbering");
    for (final Parameter parameter : sql.vecParameter) {
      if (sql.sqlStatic.equals("true")) {
        if (parameter.strName != null && !parameter.boolRepeated && !parameter.boolSequence
            && !parameter.strInOut.equals("out")) {
          if (firstParameter) {
            firstParameter = false;
          } else {
            out2.append(", ");
          }
          out2.append(parameter.strName);
        }
      }
    }
    if (firstParameter) {
      firstParameter = false;
    } else {
      out2.append(", ");
    }
    out2.append("0, 0");
    out2.append(")");
  }

  private void printEndClass() throws IOException {
    out2.append("}\n");
  }

  private static int posFinal(String strSQL, String strPattern) {
    int index = strSQL.indexOf(strPattern);
    if (index != -1)
      index = index + strPattern.length();
    return index;
  }

  private void imprimirSubstring(String strSQL, int posIni, int posFin, OutputStreamWriter out)
      throws IOException {
    final String[] strSqlVector = stringToVector(strSQL.substring(posIni, posFin), true);
    for (int i = 0; i < strSqlVector.length; i++) {
      if (i == 0) {
        out2.append("    strSql = strSql + \n");
      }
      if (i < strSqlVector.length - 1) {
        out2.append("      \"" + strSqlVector[i] + "\" +\n");
      } else {
        out2.append("      \"" + strSqlVector[i] + "\";\n");
      }
    }
  }

  private static StringBuilder imprimirSubstring2(final String strSQL, int posIni, int posFin) {
    StringBuilder res = new StringBuilder();
    final String[] strSqlVector = stringToVector(strSQL.substring(posIni, posFin), true);
    for (int i = 0; i < strSqlVector.length; i++) {
      res.append(strSqlVector[i]).append('\n');
    }
    return res;
  }

  /**
   * Convert a string with the character 0A (10 decimal) in an array of the text separated by this
   * character
   **/
  private static String[] stringToVector(String strSQL, boolean suppressBlankLines) {
    final byte tab[] = { 10 };
    final String strTab = new String(tab);
    final Vector<String> vector = new Vector<String>();
    if (strSQL == null) {
      return new String[0];
    }
    final StringTokenizer tok = new StringTokenizer(strSQL, strTab);
    while (tok.hasMoreTokens()) {
      final String sql = tok.nextToken();
      if (suppressBlankLines && sql.trim().equals("")) {
        continue;
      }
      vector.addElement(sql);
    }
    final String[] strSqlVector = new String[vector.size()];
    vector.copyInto(strSqlVector);
    return strSqlVector;
  }

  static public String TransformaNombreColumna(String strColumn) {
    return TransformaNombreColumna(strColumn, false);
  }

  static public String TransformaNombreFichero(String strFile) {
    return TransformaNombreColumna(strFile, true);
  }

  static public String TransformaNombreColumna(String strName, boolean isFile) {
    final int numChars = strName.length();
    final StringBuilder result = new StringBuilder(numChars);
    boolean underscore = false;
    for (int i = 0; i < numChars; i++) {
      final char curr = strName.charAt(i);
      if (i == 0) {
        if (isFile) {
          result.append(Character.toUpperCase(curr));
        } else {
          result.append(Character.toLowerCase(curr));
        }
      } else {
        if (curr == '_')
          underscore = true;
        else {
          if (underscore) {
            result.append(Character.toUpperCase(curr));
            underscore = false;
          } else {
            if (isFile) {
              result.append(curr);
            } else {
              result.append(Character.toLowerCase(curr));
            }
          }
        }
      }
    }
    return result.toString();
  }

  private void readProperties(String strFileProperties) {
    // Read properties file.
    final Properties properties = new Properties();
    try {
      log4j.info("strFileProperties: " + strFileProperties);
      properties.load(new FileInputStream(strFileProperties));
      javaDateFormat = properties.getProperty("dateFormat.java");
      log4j.info("javaDateFormat: " + javaDateFormat);
    } catch (final IOException e) {
      // catch possible io errors from readLine()
      log4j.error("Error loading property file", e);
    }
  }
}

/*
 * - names from Oracle to Java- distintos type="preparedStatement, callableStatement, statement" -
 * treat statement- distintos return="string, boolean, void, single(?), multiple" >- inserts and
 * updates with their return values- parameters in sequences: sequence="nombreSecuencia"- <parameter
 * type="Long" name="strExpediente" default="0" inOut="in, out, inout"> No valor </parameter>-
 * function names- convert the SQL chain in various lines: strSql = strSql + " " the linebreak
 * character is 0A (10 decimal)- write commentaries to the functions and classes - read type- read
 * sequence in parameters - read connection parameters of getConnection- return value when it is the
 * value of a sequence - commentaries for the parameters- do not write repeated parameters
 */
