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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * This class is used to implement Openbravo ERP servlet callouts in a simple manner.
 * <p>
 * To develop a new servlet callout based on this class you only have to create a new java class
 * that extends the method:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * protected void execute(CalloutInfo info) throws ServletException;
 * </pre>
 * 
 * </blockquote>
 * <p>
 * In this method you can develop the logic of the callout and use the infoobject of class
 * <code>CalloutInfo<code/> to access window fields,
 * database and other methods
 * 
 * @author aro
 */
public abstract class SimpleCallout extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(SimpleCallout.class);

  /**
   * Overwrite this method to implement a new servlet callout based in <code>SimlpleCallout</code>
   * 
   * @param info
   *          The {@code CalloutInfo} that contains all helper data to access callout information
   *          and servlet information
   * @throws ServletException
   */
  protected abstract void execute(CalloutInfo info) throws ServletException;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      try {
        printPage(response, vars);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else {
      pageError(response);
    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {

    log.debug("Output: dataSheet");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    CalloutInfo info = new CalloutInfo(vars, getSimpleClassName(), getServletConfig());

    execute(info);

    xmlDocument.setParameter("array", info.finishResult());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String getSimpleClassName() {
    String classname = getClass().getName();
    int i = classname.lastIndexOf(".");
    if (i < 0) {
      return classname;
    } else {
      return classname.substring(i + 1);
    }
  }

  /**
   * Helper class that contains all data to access callout information and servlet information
   */
  protected static class CalloutInfo {

    private StringBuilder result;
    private int rescounter;
    private int selectcounter;
    private final ServletConfig config;
    /**
     * Provides the coder friendly methods to retrieve certain environment, session and servlet call
     * variables.
     */
    public VariablesSecureApp vars;


    private CalloutInfo(VariablesSecureApp vars, String classname, ServletConfig config) {
      this.config = config;
      this.vars = vars;

      result = new StringBuilder();
      result.append("var calloutName='");
      result.append(classname);
      result.append("';\nvar respuesta = new Array(");

      rescounter = 0;
      selectcounter = 0;
    }

    private String finishResult() {
      result.append(");");
      return result.toString();
    }

    /**
     * 
     * Invokes another SimpleCallout. This method allows to divide callouts functionality into
     * several callout classes
     *
     * @param callout
     *          SimpleCallout instance to invoke
     */
    public void executeCallout(SimpleCallout callout) throws ServletException {
      callout.init(config);
      callout.execute(this);
    }

    /**
     *
     * @return The name of field that triggered the callout.
     */
    public String getLastFieldChanged() {
      return vars.getStringParameter("inpLastFieldChanged");
    }

    /**
     * 
     * @return The Tab Id that triggered the callout.
     */
    public String getTabId() {
      return vars.getStringParameter("inpTabId", IsIDFilter.instance);
    }

    /**
     * 
     * @return The Window Id that triggered the callout.
     */
    public String getWindowId() {
      return vars.getStringParameter("inpwindowId", IsIDFilter.instance);
    }

    /**
     * 
     * @param param
     *          The name of the field to get the value.
     * @param filter
     *          Filter used to validate the input against list of allowed inputs.
     * @return The value of a field named param as an {@code String}.
     */
    public String getStringParameter(String param, RequestFilter filter) {
      return vars.getStringParameter(param, filter);
    }

    /**
     * 
     * @param param
     *          The name of the field to get the value.
     * @return The value of a field named param as a {@code BigDecimal}.
     * @throws ServletException
     */
    public BigDecimal getBigDecimalParameter(String param) throws ServletException {
      return new BigDecimal(vars.getNumericParameter(param, "0"));
    }

    /**
     * Starts the inclusion of values of a field named param of type select.
     * 
     * @param param
     *          The name of the select field to set the values.
     */
    public void addSelect(String param) {

      if (rescounter > 0) {
        result.append(',');
      }
      rescounter++;
      result.append("\nnew Array(\"");
      result.append(param);
      result.append("\", ");
      result.append("new Array(");

      selectcounter = 0;
    }

    /**
     * Adds an entry to the select field and marks it as unselected.
     * 
     * @param name
     *          The entry name to add.
     * @param value
     *          The entry value to add.
     */
    public void addSelectResult(String name, String value) {
      addSelectResult(name, value, false);
    }

    /**
     * Adds an entry value to the select field.
     * 
     * @param name
     *          The entry name to add.
     * @param value
     *          The entry value to add.
     * @param selected
     *          Whether this entry field is selected or not.
     */
    public void addSelectResult(String name, String value, boolean selected) {

      if (selectcounter > 0) {
        result.append(',');
      }
      selectcounter++;
      result.append("new Array(\"");
      result.append(name);
      result.append("\", \"");
      result.append(FormatUtilities.replaceJS(value));
      result.append("\",");
      result.append(selected ? "\"true\"" : "\"false\"");
      result.append(")");
    }

    /**
     * Finish the inclusion of values to the select field.
     */
    public void endSelect() {
      if (selectcounter == 0) {
        result.append("null");
      }
      result.append(")");
      result.append(")");
    }

    /**
     * Sets the value of a field named param with the value indicated.
     * 
     * @param param
     *          The name of the field to get the value.
     * @param value
     *          The value to assign to the field.
     */
    public void addResult(String param, Object value) {

      if (rescounter > 0) {
        result.append(',');
      }
      rescounter++;

      result.append("\nnew Array(\"");
      result.append(param);
      result.append("\", ");
      result.append(value == null ? "null" : value.toString());
      result.append(")");
    }

    /**
     * Sets the value of a field named param with the value indicated. This method is useful to set
     * numbers like {@code BigDecimal} objects.
     * 
     * @param param
     *          The name of the field to get the value.
     * @param value
     *          The value to assign to the field.
     */
    public void addResult(String param, String value) {
      addResult(param, (Object) (value == null ? null : "\"" + FormatUtilities.replaceJS(value)
          + "\""));
    }
  }
}
