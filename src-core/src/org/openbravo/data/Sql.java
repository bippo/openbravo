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

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

class Sql {
  String sqlName;
  String sqlReturn;
  String sqlDefaultReturn;
  String sqlStatic;
  String sqlConnection;
  String executeType;
  String sqlType;
  String strSQL;
  String strSqlComments;
  String sqlObject;
  String sqlClass;
  String sqlImport;
  Vector<Parameter> vecParameter; // vector of Parameter's
  Vector<Object> vecFieldAdded; // vector of fields added to the Class
  String strSequenceName = null;
  boolean boolOptional = false;
  static Logger log4j = Logger.getLogger(Sql.class); // log4j

  public Sql() {
    vecParameter = new Vector<Parameter>();
    vecFieldAdded = new Vector<Object>();
  }

  public Parameter addParameter(boolean sequence, String strName, String strDefault,
      String strInOut, String strOptional, String strAfter, String strText, String strIgnoreValue) {
    if (log4j.isDebugEnabled())
      log4j.debug("addParameter sequence: " + sequence + " name: " + strName);
    if (strOptional != null)
      boolOptional = true;
    if (log4j.isDebugEnabled())
      log4j.debug("previous new Parameter");
    Parameter parameterNew = new Parameter(sequence, strName, strDefault, strInOut, strOptional,
        strAfter, strText, strIgnoreValue);
    if (log4j.isDebugEnabled())
      log4j.debug("called new Parameter");
    for (Enumeration<Parameter> e = vecParameter.elements(); e.hasMoreElements();) {
      Parameter parameter = e.nextElement();
      if (log4j.isDebugEnabled())
        log4j.debug("parameter: " + parameter.strName);
      if (parameter.strName.equals(strName)) {
        parameterNew.boolRepeated = true;
      }
    }
    if (log4j.isDebugEnabled())
      log4j.debug("previous new vecParameter.addElement");
    vecParameter.addElement(parameterNew);
    if (log4j.isDebugEnabled())
      log4j.debug("called new vecParameter.addElement");
    return parameterNew;
  }
}
