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

class FieldLabelsData implements FieldProvider {
  static Logger log4j = Logger.getLogger(FieldLabelsData.class);
  public String adColumnId;
  public String colName;
  public String colColumnname;
  public String elementName;
  public String elementPrintname;
  public String fieldName;
  public String fieldtrlName;
  public String elmttrlName;
  public String elmttrlPrintname;

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("AD_COLUMN_ID") || fieldName.equals("adColumnId"))
      return adColumnId;
    else if (fieldName.equalsIgnoreCase("COL_NAME") || fieldName.equals("colName"))
      return colName;
    else if (fieldName.equalsIgnoreCase("COL_COLUMNNAME") || fieldName.equals("colColumnname"))
      return colColumnname;
    else if (fieldName.equalsIgnoreCase("ELEMENT_NAME") || fieldName.equals("elementName"))
      return elementName;
    else if (fieldName.equalsIgnoreCase("ELEMENT_PRINTNAME")
        || fieldName.equals("elementPrintname"))
      return elementPrintname;
    else if (fieldName.equalsIgnoreCase("FIELD_NAME") || fieldName.equals("fieldName"))
      return fieldName;
    else if (fieldName.equalsIgnoreCase("FIELDTRL_NAME") || fieldName.equals("fieldtrlName"))
      return fieldtrlName;
    else if (fieldName.equalsIgnoreCase("ELMTTRL_NAME") || fieldName.equals("elmttrlName"))
      return elmttrlName;
    else if (fieldName.equalsIgnoreCase("ELMTTRL_PRINTNAME")
        || fieldName.equals("elmttrlPrintname"))
      return elmttrlPrintname;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static FieldLabelsData[] select(ConnectionProvider connectionProvider, String ad_tab_id,
      String language) throws ServletException {
    String strSql = "";
    strSql = strSql

        + "    select       "
        + "    colum.ad_column_id as AD_COLUMN_ID "
        + "    , colum.name as COL_NAME "
        + "    , colum.columnname as COL_COLUMNNAME "
        + "    , (case when w.isSoTrx='N' then elemnt.po_name else elemnt.name end ) as ELEMENT_NAME "
        + "    , (case when w.isSoTrx='N' then elemnt.po_printname else elemnt.printname end ) as ELEMENT_PRINTNAME "
        + "    , field.name as FIELD_NAME         "
        + "    , fieldTrl.name as FIELDTRL_NAME     "
        + "    , (case when w.isSoTrx='N' then elmtTrl.po_name else elemnt.name end ) as ELMTTRL_NAME "
        + "    , (case when w.isSoTrx='N' then elmtTrl.po_printname else elemnt.printname end ) as ELMTTRL_PRINTNAME "
        + "    from         "
        + "    ad_column colum  "
        + "    , ad_field field   "
        + "        left outer join ad_field_trl fieldTrl on field.ad_field_id = fieldtrl.ad_field_id and fieldtrl.ad_language = ? "
        + "    , ad_element elemnt         " + "        left outer join AD_ELEMENT_TRL elmtTrl "
        + "        on elemnt.ad_element_id = elmttrl.ad_element_id and elmttrl.ad_language = ? "
        + "    , ad_window w         " + "    , ad_tab tab   " + "    , ad_module modu "
        + "    where       " + "    colum.ad_table_id = tab.ad_table_id "
        + "    and tab.ad_tab_id = ? " + "    and elemnt.ad_module_id = modu.ad_module_id "
        + "    and w.ad_window_id = tab.ad_window_id       "
        + "    and field.ad_tab_id = tab.ad_tab_id       "
        + "    and colum.ad_column_id = field.ad_column_id "
        + "    and colum.ad_element_id = elemnt.ad_element_id "
        + "    and elemnt.isactive = 'Y'       " + "    and field.isactive = 'Y' " + "    UNION   "
        + "    select    " + "    colum.ad_column_id as AD_COLUMN_ID "
        + "    , colum.name as COL_NAME " + "    , colum.columnname as COL_COLUMNNAME "
        + "    , elemnt.name as ELEMENT_NAME          "
        + "    , elemnt.printname as ELEMENT_PRINTNAME  "
        + "    , elemnt.printname as FIELD_NAME  " + "    , elmttrl.name as FIELDTRL_NAME     "
        + "    , elmttrl.name as ELMTTRL_NAME        "
        + "    , elmttrl.printname as ELMTTRL_PRINTNAME " + "    from               "
        + "    ad_column colum      " + "    , ad_module modu " + "    , ad_element elemnt "
        + "        left outer join AD_ELEMENT_TRL elmtTrl "
        + "        on elemnt.ad_element_id = elmttrl.ad_element_id and elmttrl.ad_language = ? "
        + "    where               " + "    colum.ad_table_id = ( "
        + "      select tab.ad_table_id from ad_tab tab where tab.ad_tab_id = ?) "
        + "    and elemnt.ad_module_id = modu.ad_module_id "
        + "    and colum.ad_element_id = elemnt.ad_element_id "
        + "    and colum.ad_column_id not in (    " + "      select field.ad_column_id  "
        + "      from ad_column colum , ad_field field " + "      where field.ad_tab_id = ?  "
        + "        and colum.ad_column_id = field.ad_column_id) ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, language);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, ad_tab_id);

      result = st.executeQuery();
      while (result.next()) {
        FieldLabelsData objectFieldLabelsData = new FieldLabelsData();
        objectFieldLabelsData.adColumnId = UtilSql.getValue(result, "AD_COLUMN_ID");
        objectFieldLabelsData.colName = UtilSql.getValue(result, "COL_NAME");
        objectFieldLabelsData.colColumnname = UtilSql.getValue(result, "COL_COLUMNNAME");
        objectFieldLabelsData.elementName = UtilSql.getValue(result, "ELEMENT_NAME");
        objectFieldLabelsData.elementPrintname = UtilSql.getValue(result, "ELEMENT_PRINTNAME");
        objectFieldLabelsData.fieldName = UtilSql.getValue(result, "FIELD_NAME");
        objectFieldLabelsData.fieldtrlName = UtilSql.getValue(result, "FIELDTRL_NAME");
        objectFieldLabelsData.elmttrlName = UtilSql.getValue(result, "ELMTTRL_NAME");
        objectFieldLabelsData.elmttrlPrintname = UtilSql.getValue(result, "ELMTTRL_PRINTNAME");
        vector.addElement(objectFieldLabelsData);
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
    FieldLabelsData objectFieldLabelsData[] = new FieldLabelsData[vector.size()];
    vector.copyInto(objectFieldLabelsData);
    return (objectFieldLabelsData);
  }

}
