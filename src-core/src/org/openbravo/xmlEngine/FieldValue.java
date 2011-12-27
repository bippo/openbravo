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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.openbravo.utils.Replace;

class FieldValue implements XmlComponentValue {
  protected FieldTemplate fieldTemplate;
  private String fieldValue;
  private String previousFieldValue;

  static Logger log4jFieldValue = Logger.getLogger(FieldValue.class);

  public FieldValue(FieldTemplate fieldTemplate, XmlDocument xmlDocument) {
    this.fieldTemplate = fieldTemplate;
  }

  private String replace(String strIni) { // repeated in Parameter
    if (fieldTemplate.vecReplace != null) {
      String strFin = strIni;
      for (ReplaceElement replaceElement : fieldTemplate.vecReplace) {
        strFin = Replace.replace(strFin, replaceElement.replaceWhat, replaceElement.replaceWith);
      }
      return strFin;
    } else {
      return strIni;
    }
  }

  public void read(ResultSet result) {
    try {
      savePrevious(); // 04-12-2001
      fieldValue = replace(result.getString(fieldTemplate.name()));
    } catch (SQLException e) {
      log4jFieldValue.error("SQL error in read(" + fieldTemplate.name() + ": " + e);
    }
  }

  public void read(String value) {
    savePrevious(); // 04-12-2001
    log4jFieldValue.debug("Read Field: " + fieldTemplate.name() + " \tValue: " + fieldValue);
    fieldValue = replace(value);
  }

  public void setBlank(String strBlank) {
    fieldValue = strBlank;
  }

  public void savePrevious() {
    previousFieldValue = fieldValue;
  }

  public String print() {
    BigDecimal total = BigDecimal.ZERO;
    if (fieldValue == null || fieldValue.equals("") || fieldValue.equalsIgnoreCase("NULL"))
      return ""; // if the string is empty then Double.parseDouble cannot
    // be done
    else if (fieldTemplate.formatOutput != null) {
      try {
        total = new BigDecimal(fieldValue);
      } catch (Exception e) {
        log4jFieldValue.error("FieldValue.print() - Could not parse to double the string: "
            + fieldValue + "\n" + e);
      }
      return fieldTemplate.formatOutput.format(total);
    } else {
      return fieldValue;
    }
  }

  public String printSimple() {
    if (fieldValue == null || fieldValue.equals(""))
      return ""; // if the string is empty then Double.parseDouble cannot
    // be done
    else if (fieldTemplate.formatSimple != null) {
      return fieldTemplate.formatSimple.format(new BigDecimal(fieldValue));
    } else {
      return fieldValue;
    }
  }

  public String printPrevious() {
    if (previousFieldValue == null || previousFieldValue.equals(""))
      return "";
    else if (fieldTemplate.formatOutput != null) {
      return fieldTemplate.formatOutput.format(new BigDecimal(previousFieldValue));
    } else {
      return previousFieldValue;
    }
  }

  public String printPreviousSimple() {
    if (previousFieldValue == null || previousFieldValue.equals(""))
      return "";
    else if (fieldTemplate.formatSimple != null) {
      return fieldTemplate.formatSimple.format(new BigDecimal(previousFieldValue));
    } else {
      return previousFieldValue;
    }
  }

  public boolean check() {
    return fieldValue.equals(previousFieldValue);
  }
}
