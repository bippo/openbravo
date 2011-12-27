/*
 ************************************************************************************
 * Copyright (C) 2008-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.uiTranslation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;

class ProcessLabelsData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ProcessLabelsData.class);
  public String processparaid;
  public String processparaname;
  public String processparacolumnname;
  public String processparatrlname;

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("PROCESSPARAID"))
      return processparaid;
    else if (fieldName.equalsIgnoreCase("PROCESSPARANAME"))
      return processparaname;
    else if (fieldName.equalsIgnoreCase("PROCESSPARACOLUMNNAME"))
      return processparacolumnname;
    else if (fieldName.equalsIgnoreCase("PROCESSPARATRLNAME"))
      return processparatrlname;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ProcessLabelsData[] selectOriginalParameters(ConnectionProvider connectionProvider,
      String ad_process_id) throws ServletException {
    String strSql = "";
    strSql = strSql + "		select " + "		  processPara.ad_process_para_id as processParaId "
        + "		  , processPara.name as processParaName "
        + "     , processPara.columnname as processParaColumnName  " + "		from "
        + "		  ad_process_para processPara " + "		where " + "		  processPara.ad_process_id = ? ";

    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_process_id);

      result = st.executeQuery();
      while (result.next()) {
        ProcessLabelsData objectProcessLabelsData = new ProcessLabelsData();
        objectProcessLabelsData.processparaid = UtilSql.getValue(result, "PROCESSPARAID");
        objectProcessLabelsData.processparaname = UtilSql.getValue(result, "PROCESSPARANAME");
        objectProcessLabelsData.processparacolumnname = UtilSql.getValue(result,
            "PROCESSPARACOLUMNNAME");
        vector.addElement(objectProcessLabelsData);
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:", e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:", ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    ProcessLabelsData objectProcessLabelsData[] = new ProcessLabelsData[vector.size()];
    vector.copyInto(objectProcessLabelsData);
    return (objectProcessLabelsData);
  }

  public static ProcessLabelsData[] selectTranslatedParameters(
      ConnectionProvider connectionProvider, String ad_process_id, String language)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "		select " + "		  processPara.ad_process_para_id as processParaId"
        + "		  , processPara.name as processParaName"
        + "     , processPara.columnname as processParaColumnName"
        + "		  , processParaTrl.name as processParaTrlName" + "		from "
        + "		  ad_process_para processPara" + "		  , ad_process_para_trl processParaTrl"
        + "		where " + "		  processPara.ad_process_id = ?"
        + "		  and processPara.ad_process_para_id = processParaTrl.ad_process_para_id"
        + "		  and processParaTrl.ad_language = ?";

    ResultSet result;
    Vector<Object> vector = new Vector<Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_process_id);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);

      result = st.executeQuery();
      while (result.next()) {
        ProcessLabelsData objectProcessLabelsData = new ProcessLabelsData();
        objectProcessLabelsData.processparaid = UtilSql.getValue(result, "PROCESSPARAID");
        objectProcessLabelsData.processparaname = UtilSql.getValue(result, "PROCESSPARANAME");
        objectProcessLabelsData.processparacolumnname = UtilSql.getValue(result,
            "PROCESSPARACOLUMNNAME");
        objectProcessLabelsData.processparatrlname = UtilSql.getValue(result, "PROCESSPARATRLNAME");
        vector.addElement(objectProcessLabelsData);
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:", e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:", ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    ProcessLabelsData objectProcessLabelsData[] = new ProcessLabelsData[vector.size()];
    vector.copyInto(objectProcessLabelsData);
    return (objectProcessLabelsData);
  }
}
