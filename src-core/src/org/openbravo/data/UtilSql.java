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

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilSql {

  // setValue and getValue method to be used in sqlc

  public static boolean setValue(PreparedStatement ps, int posicion, int tipo, String strDefault,
      String strValor) {
    try {
      if (strValor == null) {
        strValor = strDefault;
      }
      if (strValor != null) {
        if (strValor.compareTo("") == 0)
          ps.setNull(posicion, tipo);
        else {
          switch (tipo) {
          case 2:
            ps.setLong(posicion, Long.valueOf(strValor).longValue());
            break;
          case 12:
            ps.setString(posicion, strValor);
            break;
          case java.sql.Types.LONGVARCHAR:
            ps.setString(posicion, strValor);
            break;
          case 0:
            ps.setDouble(posicion, Double.valueOf(strValor).doubleValue());
            break;
          }

        }
      } else
        ps.setNull(posicion, tipo);
    } catch (Exception e) {
      e.printStackTrace();
      return (false);
    }
    return (true);
  }

  public static String getValue(ResultSet result, String strField) throws java.sql.SQLException {
    String strValueReturn = result.getString(strField);
    if (result.wasNull())
      strValueReturn = "";
    return strValueReturn;
  }

  public static String getValue(ResultSet result, int pos) throws java.sql.SQLException {
    String strValueReturn = result.getString(pos);
    if (result.wasNull())
      strValueReturn = "";
    return strValueReturn;
  }

  public static String getDateValue(ResultSet result, String strField, String strDateFormat)
      throws java.sql.SQLException {
    // Format the current time.
    String strValueReturn;
    Date date = result.getDate(strField);
    if (result.wasNull()) {
      strValueReturn = "";
    } else {
      // SimpleDateFormat formatter = new SimpleDateFormat ("dd-MM-yyyy");
      SimpleDateFormat formatter = new SimpleDateFormat(strDateFormat);
      strValueReturn = formatter.format(date);
    }
    return strValueReturn;
  }

  public static String getDateValue(ResultSet result, String strField) throws java.sql.SQLException {
    return getDateValue(result, strField, "dd-MM-yyyy");
  }

  public static String getBlobValue(ResultSet result, String strField) throws java.sql.SQLException {
    String strValueReturn = "";
    Blob blob = result.getBlob(strField);
    if (result.wasNull()) {
      strValueReturn = "";
    } else {
      int length = (int) blob.length();
      if (length > 0)
        strValueReturn = new String(blob.getBytes(1, length));
    }
    return strValueReturn;
  }

  public static String getStringCallableStatement(CallableStatement cs, int intField)
      throws java.sql.SQLException {
    String strValueReturn = cs.getString(intField);
    if (strValueReturn == null)
      strValueReturn = "";
    return strValueReturn;
  }
}
