/*
 ************************************************************************************
 * Copyright (C) 2001-2011 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.rmi.Naming;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.fop.apps.Driver;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.ConnectionProviderImpl;
import org.openbravo.database.JNDIConnectionProvider;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.exception.PoolNotFoundException;
import org.openbravo.xmlEngine.XmlEngine;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import rmi.RenderFoI;

/**
 * This class is intended to be extended by the HttpSecureAppServlet and provides methods for basic
 * management of request/response, database connections, transactions, FOP rendering, and others
 * that do not require authentication. It is loaded upon startup of the application server.
 */
public class HttpBaseServlet extends HttpServlet implements ConnectionProvider {
  private static final long serialVersionUID = 1L;
  protected ConnectionProvider myPool;
  public static String strDireccion;
  protected String strReplaceWith;
  protected String strReplaceWithFull;
  protected String strDefaultServlet;
  public XmlEngine xmlEngine = null;
  private String strBaseConfigPath;
  private static String strContext = null;
  private static String prefix = null;
  private static String stcFileProperties = null;
  private boolean isJNDIModeOn;
  protected Logger log4j = Logger.getLogger(this.getClass());
  private String PoolFileName;

  protected ConfigParameters globalParameters;

  /**
   * Loads basic configuration settings that this class and all that extend it require to function
   * properly. Also instantiates XmlEngine object. This method is called upon load of the class,
   * which is configured to be loaded upon start of the application server. See also web.xml
   * (load-on-startup).
   */
  public void init(ServletConfig config) {
    try {
      super.init(config);
      strBaseConfigPath = config.getServletContext().getInitParameter("BaseConfigPath");
      if (prefix == null) {
        prefix = config.getServletContext().getRealPath("/");
        if (prefix == null || prefix.equals("")) {
          // deployment in weblogic through ear file does not deploy
          // phisically
          // the files,
          // so we need to obtain the path through getClass method
          java.net.URL url = this.getClass().getResource("/");
          String mSchemaPath = url.getFile();
          if (mSchemaPath != null || !mSchemaPath.equals("")) {
            String separator = "/";
            int lastSlash = mSchemaPath.lastIndexOf(separator);
            if (lastSlash == -1) {
              separator = "\\";
              lastSlash = mSchemaPath.lastIndexOf(separator);
            }
            prefix = mSchemaPath.substring(0, lastSlash);
            prefix = prefix.substring(0, prefix.lastIndexOf(separator));
            prefix = prefix.substring(0, prefix.lastIndexOf(separator) + 1);
            // lastSlash = mSchemaPath.lastIndexOf(separator);
            // mSchemaPath = mSchemaPath.substring(0, lastSlash);
            // lastSlash = mSchemaPath.lastIndexOf(separator);
            // prefix = mSchemaPath.substring(0, lastSlash+1);
          }
        }
        if (log4j.isDebugEnabled())
          log4j.debug("************************prefix: " + prefix);
        if (strContext == null || strContext.equals("")) {
          String path = "/";
          int secondPath = -1;
          int firstPath = prefix.lastIndexOf(path);
          if (firstPath == -1) {
            path = "\\";
            firstPath = prefix.lastIndexOf(path);
          }
          if (firstPath != -1) {
            secondPath = prefix.lastIndexOf(path, firstPath - 1);
            strContext = prefix.substring(secondPath + 1, firstPath);
          }
        }
        if (log4j.isDebugEnabled())
          log4j.debug("context: " + strContext);
        String file = config.getServletContext().getInitParameter("log4j-init-file");
        if (log4j.isDebugEnabled())
          log4j.debug("Log file: " + file);
        // if the log4j-init-file is not set, then no point in trying
        if (file != null) {
          // PropertyConfigurator.configure(prefix+file);
          PropertyConfigurator.configure(prefix + "/" + strBaseConfigPath + "/" + file);
        }
      }
      stcFileProperties = prefix + "/" + strBaseConfigPath + "/" + "Openbravo.properties";
      // OBProperties.getInstance(stcFileProperties);

      globalParameters = ConfigParameters.retrieveFrom(config.getServletContext());
      strDefaultServlet = globalParameters.strDefaultServlet;

      if (myPool == null) {
        try {
          PoolFileName = config.getServletContext().getInitParameter("PoolFile");
          String strPoolFile = prefix + "/" + strBaseConfigPath + "/" + PoolFileName;
          isJNDIModeOn = isJndiModeOn(strPoolFile);
          makeConnection(config);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      xmlEngine = new XmlEngine(this.myPool);
      xmlEngine.fileBaseLocation = new File(globalParameters.getBaseDesignPath());
      xmlEngine.strReplaceWhat = globalParameters.strReplaceWhat;
      xmlEngine.strReplaceWith = globalParameters.strLocalReplaceWith;
      log4j.debug("Replace attribute value: \"" + xmlEngine.strReplaceWhat + "\" with: \""
          + xmlEngine.strReplaceWith + "\".");
      XmlEngine.strTextDividedByZero = globalParameters.strTextDividedByZero;
      xmlEngine.fileXmlEngineFormat = new File(globalParameters.getXmlEngineFileFormatPath());
      xmlEngine.initialize();

      log4j.debug("Text of divided by zero: " + XmlEngine.strTextDividedByZero);

    } catch (ServletException e) {
      e.printStackTrace();
    }
  }

  /**
   * Initialization of basic servlet variables required by subsequent operations of this class and
   * the ones that extend it. Normally called within the service() method of this class.
   * 
   * @param request
   *          HttpServletRequest object where details of the HTTP request are.
   * @param response
   *          HttpServletResponse object where the response will be written and returned to the
   *          user.
   * @throws IOException
   * @throws ServletException
   */
  public void initialize(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    strDireccion = HttpBaseUtils.getLocalAddress(request);
    String strActualUrl = HttpBaseUtils.getLocalHostAddress(request);
    if (log4j.isDebugEnabled())
      log4j.debug("Server name: " + strActualUrl);
    HttpSession session = request.getSession(true);
    String strLanguage = "";
    try {
      strLanguage = (String) session.getAttribute("#AD_LANGUAGE");
      if (strLanguage == null || strLanguage.trim().equals(""))
        strLanguage = "";
    } catch (Exception e) {
      strLanguage = "";
    }
    xmlEngine.fileBaseLocation = new File(getBaseDesignPath(strLanguage));
    xmlEngine.sessionLanguage = strLanguage;
    strReplaceWith = globalParameters.strLocalReplaceWith.replace("@actual_url@", strActualUrl)
        .replace("@actual_url_context@", strDireccion);
    strReplaceWithFull = strReplaceWith;
    strReplaceWith = HttpBaseUtils.getRelativeUrl(request, strReplaceWith);
    if (log4j.isDebugEnabled())
      log4j.debug("xmlEngine.strReplaceWith: " + strReplaceWith);
    xmlEngine.strReplaceWith = strReplaceWith;

  }

  /**
   * Called by the HttpSecureAppServlet within its service() method to indirectly call the service()
   * method of the HttpServlet base class because HttpBaseServlet.service() is then replaced by the
   * HttpSecureAppServlets one.
   * 
   * @param request
   *          HttpServletRequest object where details of the HTTP request are.
   * @param response
   *          HttpServletResponse object where the response will be written and returned to the
   *          user.
   * @throws IOException
   * @throws ServletException
   */
  public void serviceInitialized(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    super.service(request, response);
  }

  /**
   * A dispatcher method that calls the initialization upon every request to the servlet before it
   * hands over the final dispatchment to the HttpServlet base class.
   * 
   * @param request
   *          HttpServletRequest object where details of the HTTP request are.
   * @param response
   *          HttpServletResponse object where the response will be written and returned to the
   *          user.
   * @throws IOException
   * @throws ServletException
   */
  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    initialize(request, response);
    if (log4j.isDebugEnabled())
      log4j.debug("Call to HttpServlet.service");
    super.service(request, response);
  }

  /**
   * Returns the absolute path to the correct language subfolder within the context's src-loc
   * folder.
   * 
   * @param language
   *          String specifying the language folder required, e.g. es_ES
   * @return String with the absolute path on the local drive.
   */
  protected String getBaseDesignPath(String language) {
    if (log4j.isDebugEnabled()) {
      log4j.debug("*********************Base path: " + globalParameters.strBaseDesignPath);
    }
    String strNewAddBase = globalParameters.strDefaultDesignPath;
    String strFinal = globalParameters.strBaseDesignPath;
    // if (!language.equals("") && !language.equals("en_US")) {
    // strNewAddBase = language;
    // }
    if (!strFinal.endsWith("/" + strNewAddBase)) {
      strFinal += "/" + strNewAddBase;
    }
    if (log4j.isDebugEnabled()) {
      log4j.debug("*********************Base path: " + strFinal);
    }
    return globalParameters.prefix + strFinal;
  }

  /**
   * Redirects all HTTP GET requests to be handled by the doPost method of the extending class.
   * 
   * @param request
   *          HttpServletRequest object where details of the HTTP request are.
   * @param response
   *          HttpServletResponse object where the response will be written and returned to the
   *          user.
   * @throws IOException
   * @throws ServletException
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doPost(request, response);
  }

  public void doGetCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
    doPostCall(request, response);
  }

  public void doPostCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
    return;
  }

  /**
   * Retrieves an open autocommit connection from the connection pool managed by this class.
   * 
   * @return A Connection object containing the open connection.
   * @throws NoConnectionAvailableException
   */
  public Connection getConnection() throws NoConnectionAvailableException {
    return (myPool.getConnection());
  }

  /**
   * Return the bbdd.rdbms property defined within the config/Openbravo.properties configuration
   * file. This property defines the type of the database (ORACLE or POSTGRES).
   * 
   * @return String containing the database type (ORACLE or POSTGRES).
   */
  public String getRDBMS() {
    return (myPool.getRDBMS());
  }

  /**
   * Retrieves an open connection that is not automatically commited from the connection pool
   * managed by this class.
   * 
   * @return A Connection object containing the open connection
   * @throws NoConnectionAvailableException
   * @throws SQLException
   */
  public Connection getTransactionConnection() throws NoConnectionAvailableException, SQLException {
    return myPool.getTransactionConnection();
  }

  /**
   * First commit the connection specified and then store it back into the pool of available
   * connections managed by this class.
   * 
   * @param conn
   *          The Connection object required to be committed and stored back into the pool.
   * @throws SQLException
   */
  public void releaseCommitConnection(Connection conn) throws SQLException {
    myPool.releaseCommitConnection(conn);
  }

  /**
   * First rollback the connection specified and then store it back into the pool of available
   * connections managed by this class.
   * 
   * @param conn
   *          The Connection object required to be rolled back and stored back into the pool.
   * @throws SQLException
   */
  public void releaseRollbackConnection(Connection conn) throws SQLException {
    myPool.releaseRollbackConnection(conn);
  }

  /**
   * Returns a PreparedStatement object that contains the specified strSql prepared on top of a
   * connection retrieved from the poolName pool of connections.
   * 
   * @param poolName
   *          The name of the pool to retrieve the connection from.
   * @param strSql
   *          The SQL statement to prepare.
   * @return PreparedStatement object with the strSql prepared.
   * @throws Exception
   */
  public PreparedStatement getPreparedStatement(String poolName, String strSql) throws Exception {
    return (myPool.getPreparedStatement(poolName, strSql));
  }

  /**
   * Returns a PreparedStatement object that contains the specified strSql prepared on top of a
   * connection retrieved from a default connection pool.
   * 
   * @param strSql
   *          The SQL statement to prepare.
   * @return PreparedStatement object with the strSql prepared.
   * @throws Exception
   */
  public PreparedStatement getPreparedStatement(String strSql) throws Exception {
    return (myPool.getPreparedStatement(strSql));
  }

  /**
   * Returns a PreparedStatement object that contains the specified strSql prepared on top of the
   * connection conn passed to the method.
   * 
   * @param conn
   *          The Connection object containing the connection.
   * @param strSql
   *          The SQL statement to prepare.
   * @return PreparedStatement object with the strSql prepared.
   * @throws SQLException
   */
  public PreparedStatement getPreparedStatement(Connection conn, String strSql) throws SQLException {
    return (myPool.getPreparedStatement(conn, strSql));
  }

  /**
   * Closes the preparedStatement and releases the connection on top of which this statement was
   * prepared.
   * 
   * @param preparedStatement
   *          Object containing prepared statement to release.
   * @throws SQLException
   */
  public void releasePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
    try {
      myPool.releasePreparedStatement(preparedStatement);
    } catch (Exception ex) {
    }
  }

  /**
   * Returns a Statement object for sending SQL statements to the database based on a connection
   * retrieved from the poolName.
   * 
   * @param poolName
   *          The name of the pool to retrieve the connection from.
   * @return Prepared Statement object requested.
   * @throws Exception
   */
  public Statement getStatement(String poolName) throws Exception {
    return (myPool.getStatement(poolName));
  }

  /**
   * Returns a Statement object for sending SQL statements to the database based on a connection
   * retrieved from the default pool of connections.
   * 
   * @return Prepared Statement object requested.
   * @throws Exception
   */
  public Statement getStatement() throws Exception {
    return (myPool.getStatement());
  }

  /**
   * Returns a Statement object for sending SQL statements to the database based on the connection
   * conn provided.
   * 
   * @return Prepared Statement object requested.
   * @throws SQLException
   */
  public Statement getStatement(Connection conn) throws SQLException {
    return (myPool.getStatement(conn));
  }

  /**
   * Closes the statement and releases the connection back into the pool.
   * 
   * @param statement
   *          Object containing the statement to release.
   * @throws SQLException
   */
  public void releaseStatement(Statement statement) throws SQLException {
    myPool.releaseStatement(statement);
  }

  /**
   * Only closes the statement since it is probably part of a series of statements that use the same
   * connection. The connection must be then closed manually after all statements have been closed.
   * 
   * @param statement
   *          Object containing the statement to release.
   * @throws SQLException
   */
  public void releaseTransactionalStatement(Statement statement) throws SQLException {
    myPool.releaseTransactionalStatement(statement);
  }

  /**
   * Only closes the preparedStatement since it is probably part of a series of statements that use
   * the same connection. The connection must be then closed manually after all statements have been
   * closed.
   * 
   * @param preparedStatement
   *          Object containing the prepared statement to release.
   * @throws SQLException
   */
  public void releaseTransactionalPreparedStatement(PreparedStatement preparedStatement)
      throws SQLException {
    myPool.releaseTransactionalPreparedStatement(preparedStatement);
  }

  /**
   * Returns a prepared callable statement for the specified strSql based on the connection
   * retrieved from the poolName.
   * 
   * @param poolName
   *          The name of the pool to retrieve the connection from.
   * @param strSql
   *          The callable SQL statement to prepare.
   * @return CallableStatement object with the strSql prepared.
   * @throws SQLException
   */
  public CallableStatement getCallableStatement(String poolName, String strSql) throws Exception {
    return (myPool.getCallableStatement(poolName, strSql));
  }

  /**
   * Returns a prepared callable statement for the specified strSql based on the connection
   * retrieved from the default pool of connections.
   * 
   * @param strSql
   *          The callable SQL statement to prepare.
   * @return CallableStatement object with the strSql prepared.
   * @throws Exception
   */
  public CallableStatement getCallableStatement(String strSql) throws Exception {
    return (myPool.getCallableStatement(strSql));
  }

  /**
   * Returns a prepared callable statement for the specified strSql based on the connection conn
   * provided.
   * 
   * @param conn
   *          The Connection object containing the connection.
   * @param strSql
   *          The callable SQL statement to prepare.
   * @return CallableStatement object with the strSql prepared.
   * @throws SQLException
   */
  public CallableStatement getCallableStatement(Connection conn, String strSql) throws SQLException {
    return (myPool.getCallableStatement(conn, strSql));
  }

  /**
   * Closes the prepared callableStatement and releases the connection on top of which this callable
   * statement was prepared.
   * 
   * @param callableStatement
   *          Object containing prepared callable statement to release.
   * @throws SQLException
   */
  public void releaseCallableStatement(CallableStatement callableStatement) throws SQLException {
    myPool.releaseCallableStatement(callableStatement);
  }

  /**
   * Not implemented yet.
   * 
   * @return String with "Status unavailable" or "Not implemented yet"
   */
  protected String getPoolStatus() {
    if (myPool instanceof ConnectionProviderImpl) {
      return ((ConnectionProviderImpl) myPool).getStatus();
    } else {
      return "Status unavailable";
    }

  }

  /**
   * Renders the FO input source into a PDF file which is then written directly to OutputStream.
   * 
   * @param strFo
   *          FO source string for generating the PDF.
   * @param out
   *          OutputStream object to which the PDF will be rendered to.
   * @throws ServletException
   */

  protected void renderFO(String strFo, OutputStream out) throws ServletException {
    // Check validity of the certificate
    // Create a trust manager that does not validate certificate chains
    TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
      }

      public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
      }

      @SuppressWarnings("unused")
      // external implementation
      public boolean isServerTrusted(java.security.cert.X509Certificate[] cert) {
        return true;
      }

      @SuppressWarnings("unused")
      // external implementation
      public boolean isClientTrusted(java.security.cert.X509Certificate[] cert) {
        return true;
      }
    } };
    // Install the all-trusting trust manager
    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch (Exception e) {
    }

    try {
      if (log4j.isDebugEnabled())
        log4j.debug("Beginning of renderFO");
      if (globalParameters.haveFopConfig()) {
        File fopFile = new File(globalParameters.getFopConfigPath());
        if (fopFile.exists()) {
          @SuppressWarnings("unused")
          // external implementation
          org.apache.fop.apps.Options options = new org.apache.fop.apps.Options(fopFile);
        }
      }

      final String foTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + strFo;

      if ((globalParameters.strServidorRenderFo == null)
          || (globalParameters.strServidorRenderFo.equals(""))) {

        if (log4j.isDebugEnabled())
          log4j.debug(strFo);

        StringReader sr = new StringReader(foTemplate);

        if (log4j.isDebugEnabled())
          log4j.debug(sr.toString());

        InputSource inputFO = new InputSource(sr);

        if (log4j.isDebugEnabled())
          log4j.debug("Beginning of driver");

        Driver driver = new Driver();
        driver.setLogger(globalParameters.getFopLogger());
        driver.setRenderer(Driver.RENDER_PDF);
        driver.setInputSource(inputFO);

        driver.setOutputStream(out);

        if (log4j.isDebugEnabled())
          log4j.debug("driver.run()");

        driver.run();

        if (log4j.isDebugEnabled())
          log4j.debug("End of renderFO");

        sr.close();
        driver.reset();
        driver = null;
      } else {

        RenderFoI render = (RenderFoI) Naming.lookup("rmi://"
            + globalParameters.strServidorRenderFo + "/RenderFo");

        byte[] content = render.computeRenderFo(strFo);
        out.write(content);
        out.flush();
      }
    } catch (java.lang.IllegalStateException il) {
      return;
    } catch (Exception ex) {
      throw new ServletException(ex.getMessage(), ex);
    }
  }

  /**
   * Renders a PDF directly into a HttpServletResponse. <b>NOTE:</b> If you use this method the
   * 'loading' pop-up window will not be closed.
   * 
   * @throws ServletException
   */
  protected void renderFO(String strFo, HttpServletResponse response) throws ServletException {

    try {

      response.setContentType("application/pdf; charset=UTF-8");
      response.setHeader("Content-Disposition", "attachment");

      renderFO(strFo, response.getOutputStream());

      response.getOutputStream().flush();
      response.getOutputStream().close();

    } catch (Exception ex) {
      throw new ServletException(ex.getMessage(), ex);
    }

  }

  /**
   * Returns an instance of the xerces XML parser.
   * 
   * @return XMLReader object with the parser instance.
   * @throws ServletException
   */
  static XMLReader createParser() throws ServletException {
    String parserClassName = System.getProperty("org.xml.sax.parser");
    if (parserClassName == null) {
      parserClassName = "org.apache.xerces.parsers.SAXParser";
    }
    try {
      return (XMLReader) Class.forName(parserClassName).newInstance();
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  private void makeConnection(ServletConfig config) throws PoolNotFoundException {
    if (myPool != null) {
      try {
        myPool.destroy();
      } catch (Exception ignored) {
      }
      myPool = null;
    }
    try {
      String strPoolFile = prefix + "/" + strBaseConfigPath + "/" + PoolFileName;
      // myPool = new ConnectionProviderImpl(strPoolFile,
      // (!strPoolFile.startsWith("/") &&
      // !strPoolFile.substring(1,1).equals(":")), strContext);
      // Now pool take datasources from a JNDI resource file
      if (isJNDIModeOn) {
        myPool = new JNDIConnectionProvider(strPoolFile,
            (!strPoolFile.startsWith("/") && !strPoolFile.substring(1, 1).equals(":")));
      } else {
        myPool = ConnectionProviderContextListener.getPool(config.getServletContext());
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new PoolNotFoundException(e.getMessage());
    }
  }

  private static boolean isJndiModeOn(String strPoolFile) {
    Properties properties = new Properties();
    String jndiUsage = null;
    try {
      properties.load(new FileInputStream(strPoolFile));
      jndiUsage = properties.getProperty("JNDI.usage");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return ("yes".equals(jndiUsage) ? true : false);
  }

  public String getStatus() {
    return myPool.getStatus();
  }

  public String getServletInfo() {
    return "This servlet adds some functions (connection to the database, xmlEngine, logging) over HttpServlet";
  }

}
