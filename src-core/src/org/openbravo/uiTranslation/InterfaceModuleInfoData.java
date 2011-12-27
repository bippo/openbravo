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

class InterfaceModuleInfoData implements FieldProvider {
  static Logger log4j = Logger.getLogger(InterfaceModuleInfoData.class);
  public String moduleid;
  public String modulelanguage;
  public String name;
  public String description;
  public String help;

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("MODULEID"))
      return moduleid;
    else if (fieldName.equalsIgnoreCase("MODULELANGUAGE"))
      return modulelanguage;
    else if (fieldName.equalsIgnoreCase("NAME"))
      return name;
    else if (fieldName.equalsIgnoreCase("DESCRIPTION"))
      return description;
    else if (fieldName.equalsIgnoreCase("HELP"))
      return help;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static InterfaceModuleInfoData[] selectTabModuleLang(
      ConnectionProvider connectionProvider, String ad_tab_id) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      SELECT "
        + "	  type.name, type.description, type.help, module.ad_module_id as moduleId, module.ad_language as moduleLanguage "
        + "	FROM " + "	  ad_module module, ad_tab type " + "	WHERE "
        + "	  module.ad_module_id = type.ad_module_id " + "	  and type.ad_tab_id = ? ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

      result = st.executeQuery();
      while (result.next()) {
        InterfaceModuleInfoData objectInterfaceModuleInfoData = new InterfaceModuleInfoData();
        objectInterfaceModuleInfoData.moduleid = UtilSql.getValue(result, "MODULEID");
        objectInterfaceModuleInfoData.modulelanguage = UtilSql.getValue(result, "MODULELANGUAGE");
        objectInterfaceModuleInfoData.name = UtilSql.getValue(result, "NAME");
        objectInterfaceModuleInfoData.description = UtilSql.getValue(result, "DESCRIPTION");
        objectInterfaceModuleInfoData.help = UtilSql.getValue(result, "HELP");
        vector.addElement(objectInterfaceModuleInfoData);
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
    InterfaceModuleInfoData objectInterfaceModuleInfoData[] = new InterfaceModuleInfoData[vector
        .size()];
    vector.copyInto(objectInterfaceModuleInfoData);
    return (objectInterfaceModuleInfoData);
  }

  public static InterfaceModuleInfoData[] selectProcessModuleLang(
      ConnectionProvider connectionProvider, String ad_tab_id) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      SELECT "
        + "	  type.name, type.description, type.help, module.ad_module_id as moduleId, module.ad_language as moduleLanguage "
        + "	FROM " + "	  ad_module module, ad_process type " + "	WHERE "
        + "	  module.ad_module_id = type.ad_module_id " + "	  and type.ad_process_id = ? ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

      result = st.executeQuery();
      while (result.next()) {
        InterfaceModuleInfoData objectInterfaceModuleInfoData = new InterfaceModuleInfoData();
        objectInterfaceModuleInfoData.moduleid = UtilSql.getValue(result, "MODULEID");
        objectInterfaceModuleInfoData.modulelanguage = UtilSql.getValue(result, "MODULELANGUAGE");
        objectInterfaceModuleInfoData.name = UtilSql.getValue(result, "NAME");
        objectInterfaceModuleInfoData.description = UtilSql.getValue(result, "DESCRIPTION");
        objectInterfaceModuleInfoData.help = UtilSql.getValue(result, "HELP");
        vector.addElement(objectInterfaceModuleInfoData);
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
    InterfaceModuleInfoData objectInterfaceModuleInfoData[] = new InterfaceModuleInfoData[vector
        .size()];
    vector.copyInto(objectInterfaceModuleInfoData);
    return (objectInterfaceModuleInfoData);
  }

}
