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
package org.openbravo.wad;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.data.Sqlc;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.wad.controls.WADControl;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.xmlEngine.XmlEngine;

/**
 * Utility class used by Wad.java and WadActionButton.java
 * 
 * @author Fernando Iriazabal
 * 
 */
class WadActionButton {

  /**
   * Generates the action button call for the java of the window.
   * 
   * @param conn
   *          Object with the database connection implementation.
   * @param strTab
   *          Id of the tab
   * @param tabName
   *          The tab name.
   * @param keyName
   *          The name of the key.
   * @param isSOTrx
   *          If is a sales tab.
   * @param window
   *          The id of the window.
   * @return Array of ActionButtonRelationData with the info to build the source.
   */
  public static ActionButtonRelationData[] buildActionButtonCall(ConnectionProvider conn,
      String strTab, String tabName, String keyName, String isSOTrx, String window) {
    ActionButtonRelationData[] fab = null;
    try {
      fab = ActionButtonRelationData.select(conn, strTab);
    } catch (final ServletException e) {
      return null;
    }
    if (fab != null) {
      for (int i = 0; i < fab.length; i++) {
        final Vector<Object> vecFields = new Vector<Object>();
        final Vector<Object> vecParams = new Vector<Object>();
        final Vector<Object> vecTotalFields = new Vector<Object>();
        if (fab[i].realname.equalsIgnoreCase("DocAction")
            || fab[i].realname.equalsIgnoreCase("PaymentRule")
            || (fab[i].realname.equalsIgnoreCase("Posted") && fab[i].adProcessId.equals(""))
            || (fab[i].realname.equalsIgnoreCase("CreateFrom") && fab[i].adProcessId.equals(""))
            || fab[i].realname.equalsIgnoreCase("ChangeProjectStatus"))
          fab[i].xmlid = "";
        fab[i].realname = FormatUtilities.replace(fab[i].realname);
        fab[i].columnname = Sqlc.TransformaNombreColumna(fab[i].columnname);
        fab[i].setsession = getFieldsSession(fab[i]);
        fab[i].htmltext = getFieldsLoad(fab[i], vecFields, vecTotalFields);
        fab[i].javacode = getPrintPageJavaCode(conn, fab[i], vecFields, vecParams, isSOTrx, window,
            tabName, false, fab[i].adProcessId);
        fab[i].comboparacode = getComboParaCode(conn, fab[i].adProcessId, strTab);
        final StringBuffer fields = new StringBuffer();
        final StringBuffer fieldsHeader = new StringBuffer();
        for (int j = 0; j < vecFields.size(); j++) {
          fields.append(", " + vecFields.elementAt(j));
          fieldsHeader.append(", String " + vecFields.elementAt(j));
        }
        fab[i].htmlfields = fields.toString();
        fab[i].htmlfieldsHeader = fieldsHeader.toString();
        ProcessRelationData[] data = null;
        if (!fab[i].adProcessId.equals("") && !fab[i].adProcessId.equals("177")) {
          try {
            data = ProcessRelationData.selectParameters(conn, "", fab[i].adProcessId);
          } catch (final ServletException e) {
          }
          if (fab[i].realname.equalsIgnoreCase("ChangeProjectStatus")) {
            fab[i].processParams = "PInstanceProcessData.insertPInstanceParam(this, pinstance, \"0\", \"ChangeProjectStatus\", strchangeprojectstatus, vars.getClient(), vars.getOrg(), vars.getUser());\n";
            vecParams.addElement("changeprojectstatus");
          } else
            fab[i].processParams = "";
          fab[i].processParams += getProcessParamsJava(conn, data, fab[i], vecParams, false);
          fab[i].processCode = "ActionButtonData.process" + fab[i].adProcessId
              + "(this, pinstance);\n";
        }
        fab[i].additionalCode = getAdditionalCode(fab[i], tabName, keyName);
      }
    }
    return fab;
  }

  /**
   * Generates the action button call for java processes of the window.
   * 
   * @param conn
   *          Object with the database connection implementation.
   * @param strTab
   *          Id of the tab
   * @param tabName
   *          The tab name.
   * @param keyName
   *          The name of the key.
   * @param isSOTrx
   *          If is a sales tab.
   * @param window
   *          The id of the window.
   * @return Array of ActionButtonRelationData with the info to build the source.
   */
  public static ActionButtonRelationData[] buildActionButtonCallJava(ConnectionProvider conn,
      String strTab, String tabName, String keyName, String isSOTrx, String window) {
    ActionButtonRelationData[] fab = null;
    try {
      fab = ActionButtonRelationData.selectJava(conn, strTab);
    } catch (final ServletException e) {
      return null;
    }
    if (fab != null) {
      for (int i = 0; i < fab.length; i++) {
        final Vector<Object> vecFields = new Vector<Object>();
        final Vector<Object> vecParams = new Vector<Object>();
        final Vector<Object> vecTotalFields = new Vector<Object>();
        if (fab[i].realname.equalsIgnoreCase("DocAction")
            || fab[i].realname.equalsIgnoreCase("PaymentRule")
            || (fab[i].realname.equalsIgnoreCase("Posted") && fab[i].adProcessId.equals(""))
            || (fab[i].realname.equalsIgnoreCase("CreateFrom") && fab[i].adProcessId.equals(""))
            || fab[i].realname.equalsIgnoreCase("ChangeProjectStatus"))
          fab[i].xmlid = "";
        fab[i].realname = FormatUtilities.replace(fab[i].realname);
        fab[i].columnname = Sqlc.TransformaNombreColumna(fab[i].columnname);
        fab[i].htmltext = getFieldsLoad(fab[i], vecFields, vecTotalFields);
        fab[i].setsession = getFieldsSession(fab[i]);
        fab[i].javacode = getPrintPageJavaCode(conn, fab[i], vecFields, vecParams, isSOTrx, window,
            tabName, false, fab[i].adProcessId);
        fab[i].comboparacode = getComboParaCode(conn, fab[i].adProcessId, strTab);
        final StringBuffer fields = new StringBuffer();
        final StringBuffer fieldsHeader = new StringBuffer();
        for (int j = 0; j < vecFields.size(); j++) {
          fields.append(", " + vecFields.elementAt(j));
          fieldsHeader.append(", String " + vecFields.elementAt(j));
        }
        fab[i].htmlfields = fields.toString();
        fab[i].htmlfieldsHeader = fieldsHeader.toString();
        ProcessRelationData[] data = null;
        if (!fab[i].adProcessId.equals("") && !fab[i].adProcessId.equals("177")) {
          try {
            data = ProcessRelationData.selectParameters(conn, "", fab[i].adProcessId);
          } catch (final ServletException e) {
          }

          fab[i].processParams = "";
          fab[i].processParams += getProcessParamsJava(conn, data, fab[i], vecParams, true);
          fab[i].processCode = "new " + fab[i].classname + "().execute(pb);";
        }
      }
    }
    return fab;
  }

  private static String getComboParaCode(ConnectionProvider conn, String processId, String tabId) {
    String result = "";
    ActionButtonRelationData[] params = null;
    try {
      params = ActionButtonRelationData.selectComboParams(conn, tabId, processId);
    } catch (final ServletException e) {
      return "";
    }
    for (ActionButtonRelationData para : params) {
      result += "p.put(\"" + para.columnname + "\", vars.getStringParameter(\"inp"
          + Sqlc.TransformaNombreColumna(para.columnname) + "\"));\n";
    }
    return result;
  }

  /**
   * Generates the action button call for the java of the menu processes.
   * 
   * @param conn
   *          Object with the database connection implementation.
   * @return Array of ActionButtonRelationData with the info to build the source.
   */
  public static ActionButtonRelationData[] buildActionButtonCallGenerics(ConnectionProvider conn) {
    ActionButtonRelationData[] fab = null;
    try {
      fab = ActionButtonRelationData.selectGenerics(conn);
    } catch (final ServletException e) {
      return null;
    }
    if (fab != null) {
      for (int i = 0; i < fab.length; i++) {
        final Vector<Object> vecFields = new Vector<Object>();
        final Vector<Object> vecParams = new Vector<Object>();
        final Vector<Object> vecTotalFields = new Vector<Object>();
        if (fab[i].realname.equalsIgnoreCase("DocAction")
            || fab[i].realname.equalsIgnoreCase("PaymentRule")
            || (fab[i].realname.equalsIgnoreCase("Posted") && fab[i].adProcessId.equals(""))
            || (fab[i].realname.equalsIgnoreCase("CreateFrom") && fab[i].adProcessId.equals(""))
            || fab[i].realname.equalsIgnoreCase("ChangeProjectStatus"))
          fab[i].xmlid = "";
        fab[i].realname = FormatUtilities.replace(fab[i].realname);
        fab[i].columnname = Sqlc.TransformaNombreColumna(fab[i].columnname);
        fab[i].htmltext = getFieldsLoad(fab[i], vecFields, vecTotalFields);
        fab[i].javacode = getPrintPageJavaCode(conn, fab[i], vecFields, vecParams, "", "", "",
            true, "");
        final StringBuffer fields = new StringBuffer();
        final StringBuffer fieldsHeader = new StringBuffer();
        for (int j = 0; j < vecFields.size(); j++) {
          fields.append(", " + vecFields.elementAt(j));
          fieldsHeader.append(", String " + vecFields.elementAt(j));
        }
        fab[i].htmlfields = fields.toString();
        fab[i].htmlfieldsHeader = fieldsHeader.toString();
        ProcessRelationData[] data = null;
        if (!fab[i].adProcessId.equals("") && !fab[i].adProcessId.equals("177")) {
          try {
            data = ProcessRelationData.selectParameters(conn, "", fab[i].adProcessId);
          } catch (final ServletException e) {
          }
          if (fab[i].realname.equalsIgnoreCase("ChangeProjectStatus")) {
            fab[i].processParams = "PInstanceProcessData.insertPInstanceParam(this, pinstance, \"0\", \"ChangeProjectStatus\", strchangeprojectstatus, vars.getClient(), vars.getOrg(), vars.getUser());\n";
            vecParams.addElement("changeprojectstatus");
          } else
            fab[i].processParams = "";
          fab[i].processParams += getProcessParamsJava(conn, data, fab[i], vecParams, false);
          fab[i].processCode = "ActionButtonData.process" + fab[i].adProcessId
              + "(this, pinstance);\n";
        }
        fab[i].additionalCode = getAdditionalCode(fab[i], "", "");
      }
    }
    return fab;
  }

  public static ActionButtonRelationData[] buildActionButtonCallGenericsJava(ConnectionProvider conn) {
    ActionButtonRelationData[] fab = null;
    try {
      fab = ActionButtonRelationData.selectGenericsJava(conn);
    } catch (final ServletException e) {
      return null;
    }
    if (fab != null) {
      for (int i = 0; i < fab.length; i++) {
        final Vector<Object> vecFields = new Vector<Object>();
        final Vector<Object> vecParams = new Vector<Object>();
        final Vector<Object> vecTotalFields = new Vector<Object>();

        fab[i].realname = FormatUtilities.replace(fab[i].realname);
        fab[i].columnname = Sqlc.TransformaNombreColumna(fab[i].columnname);
        fab[i].htmltext = getFieldsLoad(fab[i], vecFields, vecTotalFields);
        fab[i].javacode = getPrintPageJavaCode(conn, fab[i], vecFields, vecParams, "", "", "",
            true, "");
        final StringBuffer fields = new StringBuffer();
        final StringBuffer fieldsHeader = new StringBuffer();
        for (int j = 0; j < vecFields.size(); j++) {
          fields.append(", " + vecFields.elementAt(j));
          fieldsHeader.append(", String " + vecFields.elementAt(j));
        }
        fab[i].htmlfields = fields.toString();
        fab[i].htmlfieldsHeader = fieldsHeader.toString();
        ProcessRelationData[] data = null;

        if (!fab[i].adProcessId.equals("") && !fab[i].adProcessId.equals("177")) {
          try {
            data = ProcessRelationData.selectParameters(conn, "", fab[i].adProcessId);
          } catch (final ServletException e) {
          }
          fab[i].processParams = getProcessParamsJava(conn, data, fab[i], vecParams, true);
        }

        fab[i].processCode = "new " + fab[i].classname + "().execute(pb);";
      }
    }
    return fab;
  }

  /**
   * Adds some fields to the vector of tab's fields, depending on the column that it is processing.
   * 
   * @param columnname
   *          The name of the column.
   * @param vecFields
   *          Vector with the fields.
   */
  public static void getFieldsLoad(String columnname, Vector<Object> vecFields) {
    if (columnname.equalsIgnoreCase("DocAction")) {
      vecFields.addElement("DocStatus");
      vecFields.addElement("AD_Table_ID");
    } else if (columnname.equalsIgnoreCase("CreateFrom")) {
      vecFields.addElement("AD_Table_ID");
    } else if (columnname.equalsIgnoreCase("Posted")) {
      vecFields.addElement("AD_Table_ID");
      vecFields.addElement("Posted");
    } else if (columnname.equalsIgnoreCase("ChangeProjectStatus")) {
      vecFields.addElement("ProjectStatus");
    }
  }

  /**
   * Adds some fields to the vector of tab's fields, depending on the column that it is processing.
   * 
   * @param fd
   *          Object with the column info.
   * @param vecFields
   *          Vector of fields in vars format.
   * @param vecTotalFields
   *          Vector of fields.
   * @return String with the java call.
   */
  public static String getFieldsLoad(ActionButtonRelationData fd, Vector<Object> vecFields,
      Vector<Object> vecTotalFields) {
    if (fd == null)
      return "";
    String processId = fd.adProcessId;
    final StringBuffer html = new StringBuffer();
    if (fd.columnname.equalsIgnoreCase("DocAction")) {
      html.append("String strdocstatus = vars.getSessionValue(\"button").append(processId)
          .append(".inpdocstatus\");\n");
      vecFields.addElement("strdocstatus");
      vecTotalFields.addElement("DocStatus");
      html.append("String stradTableId = \"" + fd.adTableId + "\";\n");
      vecFields.addElement("stradTableId");
      vecTotalFields.addElement("AD_Table_ID");
    } else if (fd.columnname.equalsIgnoreCase("CreateFrom") && fd.adProcessId.equals("")) {
      html.append("String stradTableId = \"" + fd.adTableId + "\";\n");
      vecFields.addElement("stradTableId");
      vecTotalFields.addElement("AD_Table_ID");
    } else if (fd.columnname.equalsIgnoreCase("Posted") && fd.adProcessId.equals("")) {
      html.append("String stradTableId = \"" + fd.adTableId + "\";\n");
      vecFields.addElement("stradTableId");
      vecTotalFields.addElement("AD_Table_ID");
    } else if (fd.columnname.equalsIgnoreCase("ChangeProjectStatus")) {
      html.append("String strprojectstatus = vars.getSessionValue(\"button").append(processId)
          .append(".inpprojectstatus\");\n");
      vecFields.addElement("strprojectstatus");
      vecTotalFields.addElement("ProjectStatus");
    }
    return html.toString();
  }

  private static String getFieldsSession(ActionButtonRelationData fd) {
    if (fd == null)
      return "";
    String processId = fd.adProcessId;
    final StringBuffer result = new StringBuffer();
    if (fd.columnname.equalsIgnoreCase("DocAction")) {
      result.append("vars.setSessionValue(\"button").append(processId)
          .append(".inpdocstatus\", vars.getRequiredStringParameter(\"inpdocstatus\"));\n");
    } else if (fd.columnname.equalsIgnoreCase("ChangeProjectStatus")) {
      result.append("vars.setSessionValue(\"button").append(processId)
          .append(".inpprojectstatus\", vars.getRequiredStringParameter(\"inpprojectstatus\"));\n");
    }
    return result.toString();
  }

  /**
   * Auxiliar method that generates the printPage java code of the action button.
   * 
   * @param conn
   *          Object with the database connection.
   * @param fd
   *          Object with the button column info
   * @param vecFields
   *          Vector with the fields.
   * @param vecParams
   *          Vector with the parameters.
   * @param isSOTrx
   *          If is sales tab.
   * @param window
   *          Id of the window.
   * @param tabName
   *          Name of the tab.
   * @param genericActionButton
   *          Indicates whether it is generic or column action button
   * @param processId
   *          Id for the current process
   * @return String with the java code.
   */
  public static String getPrintPageJavaCode(ConnectionProvider conn, ActionButtonRelationData fd,
      Vector<Object> vecFields, Vector<Object> vecParams, String isSOTrx, String window,
      String tabName, boolean genericActionButton, String processId) {
    if (fd == null)
      return "";

    final StringBuffer html = new StringBuffer();
    for (int i = 0; i < vecFields.size(); i++) {
      String field = vecFields.elementAt(i).toString();
      field = field.substring(3);
      html.append("xmlDocument.setParameter(\"" + field + "\", str" + field + ");\n");
    }

    if (!fd.adProcessId.equals("")) {
      try {
        final ProcessRelationData[] data = ProcessRelationData.selectParameters(conn, "",
            fd.adProcessId);
        boolean hasComboParameter = false;
        html.append("    try {\n");
        for (int i = 0; i < data.length; i++) {
          if (data[i].adReferenceId.equals("17") || data[i].adReferenceId.equals("18")
              || data[i].adReferenceId.equals("19"))
            hasComboParameter = true;
        }
        if (hasComboParameter) {
          html.append("    ComboTableData comboTableData = null;\n");
        }
        for (int i = 0; i < data.length; i++) {
          String strDefault = "";
          html.append("    xmlDocument.setParameter(\"");
          // html.append(Sqlc.TransformaNombreColumna(data[i].columnname));
          html.append(data[i].columnname);
          html.append("\", ");
          if (data[i].defaultvalue.equals("") || data[i].defaultvalue.indexOf("@") == -1) {
            strDefault = "\"" + data[i].defaultvalue + "\"";
          } else if (data[i].defaultvalue.startsWith("@SQL=")) {
            strDefault = (tabName.equals("") ? "ActionButtonSQLDefault" : tabName)
                + "Data.selectActP" + data[i].id + "_"
                + FormatUtilities.replace(data[i].columnname);
            strDefault += "(this"
                + WadUtility.getWadContext(data[i].defaultvalue, vecFields, vecParams, null, false,
                    isSOTrx, window);
            strDefault += ")";
          } else {
            strDefault = WadUtility.getTextWadContext(data[i].defaultvalue, vecFields, vecParams,
                null, false, isSOTrx, window);
          }
          html.append(strDefault).append(");\n");
          if (data[i].adReferenceId.equals("17") || data[i].adReferenceId.equals("18")
              || data[i].adReferenceId.equals("19")) {
            html.append("    comboTableData = new ComboTableData(vars, this, \"");
            html.append(data[i].adReferenceId).append("\", \"");
            html.append(data[i].columnname).append("\", \"");
            html.append(data[i].adReferenceValueId).append("\", \"");
            html.append(data[i].adValRuleId).append("\", ");

            html.append("Utility.getContext(this, vars, \"#AccessibleOrgTree\", \"\"), ");

            html.append("Utility.getContext(this, vars, \"#User_Client\", \"\"), 0");
            html.append(");\n");
            html.append("    Utility.fillSQLParameters(this, vars, ")
                .append(
                    genericActionButton ? "null" : "(FieldProvider) vars.getSessionObject(\"button"
                        + processId + ".originalParams\")").append(", comboTableData, windowId, ")
                .append(strDefault).append(");\n");
            html.append("    xmlDocument.setData(\"report");
            // html.append(Sqlc.TransformaNombreColumna(data[i].columnname));
            html.append(data[i].columnname);
            html.append("\", \"liststructure\", comboTableData.select(false));\n");
            html.append("comboTableData = null;\n");
          } else if (data[i].adReferenceId.equals("15")) {
            html.append("    xmlDocument.setParameter(\"").append(data[i].columnname)
                .append("_Format\", vars.getSessionValue(\"#AD_SqlDateFormat\"));\n");
          } else if (data[i].adReferenceId.equals("30") || data[i].adReferenceId.equals("35")
              || data[i].adReferenceId.equals("25") || data[i].adReferenceId.equals("31")
              || data[i].adReferenceId.equals("800011")) {
            html.append("    xmlDocument.setParameter(\"");
            // html.append(Sqlc.TransformaNombreColumna(data[i].columnname));
            html.append(data[i].columnname);
            html.append("R\", ");
            if (!tabName.equals("")) {
              html.append(tabName);
              html.append("Data.selectActDef");
              html.append(FormatUtilities.replace(data[i].columnname));
              html.append("(this");
              html.append(((data[i].defaultvalue.equals("") || data[i].defaultvalue.indexOf("@") == -1) ? ", \""
                  + data[i].defaultvalue + "\""
                  : WadUtility.getWadContext(data[i].defaultvalue, vecFields, vecParams, null,
                      false, isSOTrx, window)));
              html.append(")");
            } else {
              html.append("\"\"");
            }
            html.append(");\n");
          }
          vecParams.addElement(data[i].columnname);
        }
        html.append("    } catch (Exception ex) {\n");
        html.append("      throw new ServletException(ex);\n");
        html.append("    }\n");
      } catch (final ServletException e) {
      }
    }
    if (fd.columnname.equalsIgnoreCase("DocAction")) {
      html.append("xmlDocument.setParameter(\"processId\", \"" + fd.adProcessId + "\");\n");
      String strAux = "";
      try {
        strAux = ActionButtonRelationData.processDescription(conn, fd.adProcessId);
      } catch (final ServletException e) {
      }
      html.append("xmlDocument.setParameter(\"processDescription\", \"" + strAux + "\");\n");
      html.append("xmlDocument.setParameter(\"docaction\", (strdocaction.equals(\"--\")?\"CL\":strdocaction));\n");
      html.append("FieldProvider[] dataDocAction = ActionButtonUtility.docAction(this, vars, strdocaction, \""
          + fd.adReferenceValueId + "\", strdocstatus, strProcessing, stradTableId);\n");
      html.append("xmlDocument.setData(\"reportdocaction\", \"liststructure\", dataDocAction);\n");
      html.append("StringBuffer dact = new StringBuffer();\n");
      html.append("if (dataDocAction!=null) {\n");
      html.append("  dact.append(\"var arrDocAction = new Array(\\n\");\n");
      html.append("  for (int i=0;i<dataDocAction.length;i++) {\n");
      html.append("    dact.append(\"new Array(\\\"\" + dataDocAction[i].getField(\"id\") + \"\\\", \\\"\" + dataDocAction[i].getField(\"name\") + \"\\\", \\\"\" + dataDocAction[i].getField(\"description\") + \"\\\")\\n\");\n");
      html.append("    if (i<dataDocAction.length-1) dact.append(\",\\n\");\n");
      html.append("  }\n");
      html.append("  dact.append(\");\");\n");
      html.append("} else dact.append(\"var arrDocAction = null\");\n");
      html.append("xmlDocument.setParameter(\"array\", dact.toString());\n");
    } else if (fd.columnname.equalsIgnoreCase("ChangeProjectStatus")) {
      String strAux = "";
      html.append("xmlDocument.setParameter(\"processId\", \"" + fd.adProcessId + "\");\n");
      try {
        strAux = ActionButtonRelationData.processDescription(conn, fd.adProcessId);
      } catch (final ServletException e) {
      }
      html.append("xmlDocument.setParameter(\"processDescription\", \"" + strAux + "\");\n");
      html.append("xmlDocument.setParameter(\"projectaction\", strchangeprojectstatus);\n");
      html.append("FieldProvider[] dataProjectAction = ActionButtonUtility.projectAction(this, vars, strchangeprojectstatus, \""
          + fd.adReferenceValueId + "\", strprojectstatus);\n");
      html.append("xmlDocument.setData(\"reportprojectaction\", \"liststructure\", dataProjectAction);\n");
      html.append("StringBuffer dact = new StringBuffer();\n");
      html.append("if (dataProjectAction!=null) {\n");
      html.append("  dact.append(\"var arrProjectAction = new Array(\\n\");\n");
      html.append("  for (int i=0;i<dataProjectAction.length;i++) {\n");
      html.append("    dact.append(\"new Array(\\\"\" + dataProjectAction[i].getField(\"id\") + \"\\\", \\\"\" + dataProjectAction[i].getField(\"name\") + \"\\\", \\\"\" + dataProjectAction[i].getField(\"description\") + \"\\\")\\n\");\n");
      html.append("    if (i<dataProjectAction.length-1) dact.append(\",\\n\");\n");
      html.append("  }\n");
      html.append("  dact.append(\");\");\n");
      html.append("} else dact.append(\"var arrProjectAction = null\");\n");
      html.append("xmlDocument.setParameter(\"array\", dact.toString());\n");
    } else if (fd.columnname.equalsIgnoreCase("PaymentRule")) {
    }
    return html.toString();
  }

  /**
   * Returns the process call needed to fill the ad_pinstance's tables to execute the procedure.
   * 
   * @param data
   *          Array with the parameters of the process.
   * @param fd
   *          Object with the column information.
   * @param vecParams
   *          Vector of parameters.
   * @return String with all the calls.
   */
  public static String getProcessParamsJava(ConnectionProvider conn, ProcessRelationData[] data,
      ActionButtonRelationData fd, Vector<Object> vecParams, boolean isGenericJava) {
    if (fd == null)
      return "";
    final StringBuffer html = new StringBuffer();

    if (data != null) {
      for (int i = 0; i < data.length; i++) {
        html.append("String str" + Sqlc.TransformaNombreColumna(data[i].columnname));

        WADControl control = WadUtility.getWadControlClass(conn, data[i].reference,
            data[i].adReferenceValueId);
        if (control.isNumericType()) {
          html.append(" = vars.getNumericParameter");
        } else {
          html.append(" = vars.getStringParameter");
        }

        html.append("(\"inp" + Sqlc.TransformaNombreColumna(data[i].columnname) + "\"");
        if (data[i].adReferenceId.equals("20"))
          html.append(", \"N\"");
        html.append(");\n");
        if (isGenericJava) {
          html.append("params.put(\"").append(Sqlc.TransformaNombreColumna(data[i].columnname))
              .append("\", str").append(Sqlc.TransformaNombreColumna(data[i].columnname))
              .append(");\n");
        } else {
          html.append("PInstanceProcessData.insertPInstanceParam"
              + (control.isNumericType() ? "Number" : (control.isDate() ? "Date" : (control
                  .isTime() ? "Time" : ""))) + "(this, pinstance, \"" + data[i].seqno + "\", \""
              + data[i].columnname + "\", str" + Sqlc.TransformaNombreColumna(data[i].columnname)
              + ", vars.getClient(), vars.getOrg(), vars.getUser());\n");
        }
        vecParams.addElement(Sqlc.TransformaNombreColumna(data[i].columnname));
      }
    }
    return html.toString();
  }

  /**
   * Generates the aditional code needed for some specifics processes.
   * 
   * @param fd
   *          Object with the column info.
   * @param tabName
   *          Name of the tab.
   * @param keyName
   *          Name of the key.
   * @return String with the specific code.
   */
  public static String getAdditionalCode(ActionButtonRelationData fd, String tabName, String keyName) {
    if (fd == null)
      return "";
    final StringBuffer html = new StringBuffer();
    if (fd.columnname.equalsIgnoreCase("DocAction")) {
      html.append(tabName + "Data.updateDocAction(this, strdocaction, str" + keyName + ");\n");
      /*
       * } else if (fd.columnname.equalsIgnoreCase("ChangeProjectStatus")) { html.append(tabName +
       * "Data.updateChangeProjectStatus(this, strchangeprojectstatus, str" + keyName + ");\n");
       */
    } else if (fd.columnname.equalsIgnoreCase("PaymentRule")) {
    }
    return html.toString();
  }

  /**
   * Generates the info to create the sql for the action button
   * 
   * @param conn
   *          Object with the database connection.
   * @param strTab
   *          Id of the tab.
   * @return Array of ActionButtonRelationData objects with the info.
   */
  public static ActionButtonRelationData[] buildActionButtonSQL(ConnectionProvider conn,
      String strTab) {
    ActionButtonRelationData[] fab = null;
    try {
      fab = ActionButtonRelationData.selectDocAction(conn, strTab);
    } catch (final ServletException e) {
      return null;
    }
    if (fab == null)
      return null;
    for (int i = 0; i < fab.length; i++) {
      fab[i].realname = FormatUtilities.replace(fab[i].realname);
      fab[i].columnname = Sqlc.TransformaNombreColumna(fab[i].columnname);
    }
    return fab;
  }

  /**
   * Generates the xml file for the action button
   * 
   * @param conn
   *          Object with the database connection.
   * @param xmlEngine
   *          The XmlEngine object to manage the templates.
   * @param fileDir
   *          Path where is gonna be created the xml file.
   * @param fd
   *          Object with the column info.
   * @param vecFields
   *          Vector with the fields.
   * @param max_textbox_length
   *          Max length for the textbox controls.
   * @throws ServletException
   * @throws IOException
   */
  public static void buildXml(ConnectionProvider conn, XmlEngine xmlEngine, File fileDir,
      FieldsData fd, Vector<Object> vecFields, int max_textbox_length) throws ServletException,
      IOException {
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/wad/Configuration_ActionButton").createXmlDocument();
    xmlDocument.setParameter("class", (fd.columnname + fd.reference) + ".html");
    final ProcessRelationData[] data = ProcessRelationData.selectParameters(conn, "", fd.reference);

    {
      final StringBuffer html = new StringBuffer();
      if (vecFields != null) {
        for (int i = 0; i < vecFields.size(); i++) {
          html.append("<PARAMETER id=\"" + vecFields.elementAt(i) + "\" name=\""
              + vecFields.elementAt(i) + "\" attribute=\"value\"/>\n");
        }
      }
      xmlDocument.setParameter("additionalFields", html.toString());
    }

    final StringBuffer html = new StringBuffer();
    final StringBuffer labelsHTML = new StringBuffer();
    if (data != null) {
      for (int i = 0; i < data.length; i++) {
        WADControl auxControl = null;
        try {
          auxControl = WadUtility.getControl(conn, data[i], false, (fd.columnname + fd.reference),
              "", xmlEngine, false, false, false, false);
          auxControl.setData("IsParameter", "Y");
        } catch (final Exception ex) {
          throw new ServletException(ex);
        }
        html.append(auxControl.toXml()).append("\n");

        final String labelXML = auxControl.toLabelXML();
        if (!labelXML.trim().equals(""))
          labelsHTML.append(auxControl.toLabelXML()).append("\n");
      }
    }

    xmlDocument.setParameter("column", html.toString());
    xmlDocument.setParameter("labels", labelsHTML.toString());
    WadUtility.writeFile(fileDir, (fd.columnname + fd.reference) + ".xml",
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xmlDocument.print());
  }

  /**
   * Generates the html file for the action button.
   * 
   * @param conn
   *          Object with the database connection.
   * @param xmlEngine
   *          XmlEngine object to manage the templates.
   * @param fileDir
   *          Path where is gonna be created the file.
   * @param fd
   *          Object with the column info.
   * @param vecFields
   *          Vector with the fields.
   * @param max_textbox_length
   *          Max length for the textbox controls.
   * @param max_size_edition_1_columns
   *          Max size for the one column in the edition mode.
   * @param strLanguage
   *          Language to translate.
   * @param isGeneric
   *          Indicates if is a generic action button or not.
   * @param jsDateFormat
   *          Date format for js.
   * @param vecReloads
   * @throws ServletException
   * @throws IOException
   */
  public static void buildHtml(ConnectionProvider conn, XmlEngine xmlEngine, File fileDir,
      FieldsData fd, Vector<Object> vecFields, int max_textbox_length,
      int max_size_edition_1_columns, String strLanguage, boolean isGeneric, String jsDateFormat,
      Vector<Object> vecReloads) throws ServletException, IOException {
    final String[] discard = { "", "isGeneric", "fieldDiscardProcess", "" };
    if (fd.xmltext.equals(""))
      discard[0] = "helpDiscard";
    if (isGeneric)
      discard[1] = "isNotGeneric";
    if (fd.isjasper.equals("Y"))
      discard[2] = "fieldDiscardJasper";

    if (fd.isautosave.equals("N")) {
      discard[3] = "reloadOpener";
    }

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/wad/Template_ActionButton", discard).createXmlDocument();
    final ProcessRelationData[] efd = ProcessRelationData.selectParameters(conn, strLanguage,
        fd.reference);
    xmlDocument.setParameter("tab", fd.realname);
    xmlDocument.setParameter("columnname", fd.columnname + fd.reference);
    xmlDocument.setParameter("processDescription", fd.tablename);
    xmlDocument.setParameter("processHelp", fd.xmltext);
    xmlDocument.setParameter("adProcessId", fd.reference);

    {
      final StringBuffer html = new StringBuffer();
      xmlDocument.setParameter("additionalFields", html.toString());
    }

    if (efd != null) {

      vecFields.addElement(fd.columnname);
      for (int i = 0; i < efd.length; i++)
        vecFields.addElement(efd[i].columnname);

      final Properties importsCSS = new Properties();
      final Properties importsJS = new Properties();
      final Properties javaScriptFunctions = new Properties();
      final StringBuffer validations = new StringBuffer();
      final StringBuffer onload = new StringBuffer();
      final StringBuffer html = new StringBuffer();
      for (int i = 0; i < efd.length; i++) {
        WADControl auxControl = null;
        try {
          auxControl = WadUtility.getControl(conn, efd[i], false, (fd.columnname + fd.reference),
              strLanguage, xmlEngine, false,
              WadUtility.isInVector(vecReloads, efd[i].getField("columnname")), false, false);
        } catch (final Exception ex) {
          throw new ServletException(ex);
        }

        html.append("<tr><td class=\"TitleCell\">").append(auxControl.toLabel().replace("\n", ""))
            .append("</td>\n");
        html.append("<td class=\"").append(auxControl.getType()).append("_ContentCell\"");
        if (Integer.valueOf(auxControl.getData("DisplayLength")).intValue() > (max_size_edition_1_columns / 2)) {
          html.append(" colspan=\"2\"");
          auxControl.setData("CssSize", "TwoCells");
        } else {
          auxControl.setData("CssSize", "OneCell");
        }
        html.append(">");
        html.append(auxControl.toString());
        html.append("</td>\n");
        if (auxControl.getData("CssSize").equals("OneCell"))
          html.append("<td></td>\n");
        html.append("<td></td>\n");
        html.append("</tr>\n");
        // Getting JavaScript
        {
          final Vector<String[]> auxJavaScript = auxControl.getJSCode();
          if (auxJavaScript != null) {
            for (int j = 0; j < auxJavaScript.size(); j++) {
              final String[] auxObj = auxJavaScript.elementAt(j);
              javaScriptFunctions.setProperty(auxObj[0], auxObj[1]);
            }
          }
        } // End getting JavaScript
        // Getting css imports
        {
          final Vector<String[]> auxCss = auxControl.getCSSImport();
          if (auxCss != null) {
            for (int j = 0; j < auxCss.size(); j++) {
              final String[] auxObj = auxCss.elementAt(j);
              importsCSS.setProperty(auxObj[0], auxObj[1]);
            }
          }
        } // End getting css imports
        // Getting js imports
        {
          final Vector<String[]> auxJs = auxControl.getImport();
          if (auxJs != null) {
            for (int j = 0; j < auxJs.size(); j++) {
              final String[] auxObj = auxJs.elementAt(j);
              importsJS.setProperty(auxObj[0], auxObj[1]);
            }
          }
        } // End getting js imports
        if (!auxControl.getValidation().equals(""))
          validations.append(auxControl.getValidation()).append("\n");
        if (!auxControl.getOnLoad().equals(""))
          onload.append(auxControl.getOnLoad()).append("\n");
      }
      xmlDocument.setParameter("fields", html.toString());
      final StringBuffer sbImportCSS = new StringBuffer();
      for (final Enumeration<?> e = importsCSS.propertyNames(); e.hasMoreElements();) {
        final String _name = (String) e.nextElement();
        sbImportCSS.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"")
            .append(importsCSS.getProperty(_name)).append("\"/>\n");
      }
      xmlDocument.setParameter("importCSS", sbImportCSS.toString());
      final StringBuffer sbImportJS = new StringBuffer();
      boolean hasCalendar = false;
      boolean calendarInserted = false;
      boolean calendarLangInserted = false;
      for (final Enumeration<?> e = importsJS.propertyNames(); e.hasMoreElements();) {
        final String _name = (String) e.nextElement();
        if (_name.startsWith("calendar"))
          hasCalendar = true;
        if (!_name.equals("calendarLang") || calendarInserted) {
          sbImportJS.append("<script language=\"JavaScript\" src=\"")
              .append(importsJS.getProperty(_name))
              .append("\" type=\"text/javascript\"></script>\n");
          if (_name.equals("calendarLang"))
            calendarLangInserted = true;
        }
        if (_name.equals("calendar"))
          calendarInserted = true;
      }
      if (hasCalendar && !calendarLangInserted)
        sbImportJS.append("<script language=\"JavaScript\" src=\"")
            .append(importsJS.getProperty("calendarLang"))
            .append("\" type=\"text/javascript\"></script>\n");
      xmlDocument.setParameter("importJS", sbImportJS.toString());
      final StringBuffer script = new StringBuffer();
      for (final Enumeration<?> e = javaScriptFunctions.propertyNames(); e.hasMoreElements();) {
        final String _name = (String) e.nextElement();
        script.append(javaScriptFunctions.getProperty(_name)).append("\n");
      }
      script.append("\nfunction validateClient(action, form, value) {\n");
      script.append("  var frm=document.frmMain;\n");
      script.append(validations);
      script.append("  setProcessingMode('popup', true);\n");
      script.append("  return true;\n");
      script.append("}\n");

      script.append("\nfunction onloadClient() {\n");
      script.append("  var frm=document.frmMain;\n");
      script.append("  var key = frm.inpKey;");
      script.append(onload);
      script.append("  return true;\n");
      script.append("}\n");

      script.append("\nfunction reloadComboReloads").append(fd.reference)
          .append("(changedField) {\n");
      script
          .append("  submitCommandForm(changedField, false, null, '../ad_callouts/ComboReloadsProcessHelper.html', 'hiddenFrame', null, null, true);\n");
      script.append("  return true;\n");
      script.append("}\n");

      xmlDocument.setParameter("script", script.toString());
    }
    WadUtility.writeFile(fileDir, (fd.columnname + fd.reference) + ".html", xmlDocument.print());
  }

  /**
   * Searchs a field in a vector.
   * 
   * @param vecFields
   *          Vector with the fields.
   * @param token
   *          The field to search.
   * @return String with the name of the field.
   */
  public static String findField(Vector<Object> vecFields, String token) {
    if (vecFields == null)
      return "";
    for (int i = 0; i < vecFields.size(); i++) {
      final String field = vecFields.elementAt(i).toString();
      if (field.equalsIgnoreCase(token))
        return field;
    }
    return "";
  }

}
