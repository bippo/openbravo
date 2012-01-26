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

package org.openbravo.test.webservice;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.xml.XMLUtil;
import org.openbravo.test.base.BaseTest;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Base class for webservice tests. Provides several methods to do HTTP REST requests.
 * 
 * @author mtaal
 */

public class BaseWSTest extends BaseTest {

  private static final Logger log = Logger.getLogger(BaseWSTest.class);

  private static final String OB_URL = "http://localhost:8081/openbravo";
  private static final String LOGIN = "Openbravo";
  private static final String PWD = "openbravo";

  private String xmlSchema = null;

  /**
   * Executes a DELETE HTTP request, the wsPart is appended to the {@link #getOpenbravoURL()}.
   * 
   * @param wsPart
   *          the actual webservice part of the url, is appended to the openbravo url (
   *          {@link #getOpenbravoURL()}), includes any query parameters
   * @param expectedResponse
   *          the expected HTTP response code
   */
  protected void doDirectDeleteRequest(String wsPart, int expectedResponse) {
    try {
      final HttpURLConnection hc = createConnection(wsPart, "DELETE");
      hc.connect();
      assertEquals(expectedResponse, hc.getResponseCode());
      assertTrue("Content type not set in delete response", hc.getContentType() != null);
      // disabled, see here: https://issues.openbravo.com/view.php?id=10236
      // assertTrue("Content encoding not set in delete response", hc.getContentEncoding() != null);
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * Execute a REST webservice HTTP request which posts/puts content and returns a XML result. The
   * content is validated against the XML schema retrieved using the /ws/dal/schema webservice call.
   * 
   * @param wsPart
   *          the actual webservice part of the url, is appended to the openbravo url (
   *          {@link #getOpenbravoURL()}), includes any query parameters
   * @param content
   *          the content (XML) to post or put
   * @param expectedResponse
   *          the expected HTTP response code
   * @param expectedContent
   *          the system check that the returned content contains this expectedContent
   * @param method
   *          POST or PUT
   * @return the result from the rest request (i.e. the content of the response), most of the time
   *         an xml string
   */
  protected String doContentRequest(String wsPart, String content, int expectedResponse,
      String expectedContent, String method) {
    try {
      final HttpURLConnection hc = createConnection(wsPart, method);
      final OutputStream os = hc.getOutputStream();
      os.write(content.getBytes("UTF-8"));
      os.flush();
      os.close();
      hc.connect();

      assertEquals(expectedResponse, hc.getResponseCode());

      if (expectedResponse == 500) {
        // no content available anyway
        return "";
      }
      final SAXReader sr = new SAXReader();
      final InputStream is = hc.getInputStream();
      final Document doc = sr.read(is);
      final String retContent = XMLUtil.getInstance().toString(doc);
      if (retContent.indexOf(expectedContent) == -1) {
        log.debug(retContent);
        fail();
      }
      validateXML(retContent);
      return retContent;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * Convenience method to get a value of a specific XML element without parsing the whole xml
   * 
   * @param content
   *          the xml
   * @param tag
   *          the element name
   * @return the value
   */
  protected String getTagValue(String content, String tag) {
    final int index1 = content.indexOf("<" + tag + ">") + ("<" + tag + ">").length();
    if (index1 == -1) {
      return "";
    }
    final int index2 = content.indexOf("</" + tag + ">");
    if (index2 == -1) {
      return "";
    }
    return content.substring(index1, index2);
  }

  /**
   * Executes a GET request and validates the return against the schema. The content is validated
   * against the XML schema retrieved using the /ws/dal/schema webservice call.
   * 
   * @param wsPart
   *          the actual webservice part of the url, is appended to the openbravo url (
   *          {@link #getOpenbravoURL()}), includes any query parameters
   * @param testContent
   *          the system check that the returned content contains this testContent. if null is
   *          passed for this parameter then this check is not done.
   * @param responseCode
   *          the expected HTTP response code
   * @return the content returned from the GET request
   */
  protected String doTestGetRequest(String wsPart, String testContent, int responseCode) {
    return doTestGetRequest(wsPart, testContent, responseCode, true);
  }

  /**
   * Executes a GET request. The content is validated against the XML schema retrieved using the
   * /ws/dal/schema webservice call.
   * 
   * @param wsPart
   *          the actual webservice part of the url, is appended to the openbravo url (
   *          {@link #getOpenbravoURL()}), includes any query parameters
   * @param testContent
   *          the system check that the returned content contains this testContent. if null is
   *          passed for this parameter then this check is not done.
   * @param responseCode
   *          the expected HTTP response code
   * @param validate
   *          if true then the response content is validated against the Openbravo XML Schema
   * @return the content returned from the GET request
   */
  protected String doTestGetRequest(String wsPart, String testContent, int responseCode,
      boolean validate) {
    try {
      final HttpURLConnection hc = createConnection(wsPart, "GET");
      hc.connect();
      final SAXReader sr = new SAXReader();
      final InputStream is = hc.getInputStream();
      final StringBuilder sb = new StringBuilder();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      try {
        final Document doc = sr.read(new StringReader(sb.toString()));
        final String content = XMLUtil.getInstance().toString(doc);
        if (testContent != null && content.indexOf(testContent) == -1) {
          log.debug(content);
          fail();
        }
        assertEquals(responseCode, hc.getResponseCode());
        is.close();
        // do not validate the xml schema itself, this results in infinite loops
        if (validate) {
          validateXML(content);
        }
        return content;
      } catch (Exception e) {
        log.debug(sb.toString());
        throw e;
      }
    } catch (final Exception e) {
      throw new OBException("Exception when executing ws: " + wsPart, e);
    }
  }

  /**
   * Creates a HTTP connection.
   * 
   * @param wsPart
   * @param method
   *          POST, PUT, GET or DELETE
   * @return the created connection
   * @throws Exception
   */
  protected HttpURLConnection createConnection(String wsPart, String method) throws Exception {
    Authenticator.setDefault(new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(LOGIN, PWD.toCharArray());
      }
    });
    log.debug(method + ": " + getOpenbravoURL() + wsPart);
    final URL url = new URL(getOpenbravoURL() + wsPart);
    final HttpURLConnection hc = (HttpURLConnection) url.openConnection();
    hc.setRequestMethod(method);
    hc.setAllowUserInteraction(false);
    hc.setDefaultUseCaches(false);
    hc.setDoOutput(true);
    hc.setDoInput(true);
    hc.setInstanceFollowRedirects(true);
    hc.setUseCaches(false);
    hc.setRequestProperty("Content-Type", "text/xml");
    return hc;
  }

  /**
   * Returns the url of the Openbravo instance. The default value is: {@link #OB_URL}
   * 
   * @return the url of the Openbravo instance.
   */
  protected String getOpenbravoURL() {
    return OB_URL;
  }

  /**
   * Returns the login used to login for the webservice. The default value is {@link #LOGIN}.
   * 
   * @return the login name used to login for the webservice
   */
  protected String getLogin() {
    return LOGIN;
  }

  /**
   * Returns the password used to login into the webservice server. The default value is
   * {@link #PWD}.
   * 
   * @return the password used to login into the webservice, the default is {@link #PWD}
   */
  protected String getPassword() {
    return PWD;
  }

  /**
   * Validates the xml against the generated schema.
   * 
   * @param xml
   *          the xml to validate
   */
  protected void validateXML(String xml) {
    final Reader schemaReader = new StringReader(getXMLSchema());
    final Reader xmlReader = new StringReader(xml);
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      factory.setNamespaceAware(true);

      SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

      factory.setSchema(schemaFactory.newSchema(new Source[] { new StreamSource(schemaReader) }));

      SAXParser parser = factory.newSAXParser();

      XMLReader reader = parser.getXMLReader();
      reader.setErrorHandler(new SimpleErrorHandler());
      reader.parse(new InputSource(xmlReader));
    } catch (Exception e) {
      throw new OBException(e);
    }

  }

  private String getXMLSchema() {
    if (xmlSchema != null) {
      return xmlSchema;
    }

    xmlSchema = doTestGetRequest("/ws/dal/schema", "<xs:element name=\"Openbravo\">", 200, false);
    return xmlSchema;
  }

  public class SimpleErrorHandler implements ErrorHandler {
    public void warning(SAXParseException e) throws SAXException {
    }

    public void error(SAXParseException e) throws SAXException {
      throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
      throw e;
    }
  }

}