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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.process.ProcessInstance;

/**
 * This class is a service class to directly call a stored procedure without using a
 * {@link ProcessInstance}.
 * 
 * @author mtaal
 */
public class CallStoredProcedure {

  private static CallStoredProcedure instance = new CallStoredProcedure();

  public static synchronized CallStoredProcedure getInstance() {
    return instance;
  }

  public static synchronized void setInstance(CallStoredProcedure instance) {
    CallStoredProcedure.instance = instance;
  }

  /**
   * Calls a stored procedure with the specified name. The parameter list is translated in exactly
   * the same parameters for the call so the parameters should be in the correct order and have the
   * correct type as expected by the stored procedure. The parameter types can be any of the
   * primitive types used by Openbravo (Date, Long, String, etc.).
   * 
   * @param name
   *          the name of the stored procedure to call.
   * @param parameters
   *          a list of parameters (null values are allowed)
   * @param types
   *          the list of types of the parameters, only needs to be set if there are null values and
   *          if the null value is something else than a String (which is handled as a default type)
   * @return the stored procedure result.
   */
  public Object call(String name, List<Object> parameters, List<Class<?>> types) {
    final StringBuilder sb = new StringBuilder();
    sb.append("SELECT " + name);
    for (int i = 0; i < parameters.size(); i++) {
      if (i == 0) {
        sb.append("(");
      } else {
        sb.append(",");
      }
      sb.append("?");
    }
    if (parameters.size() > 0) {
      sb.append(")");
    }
    sb.append(" AS RESULT FROM DUAL");
    final Connection conn = OBDal.getInstance().getConnection();
    try {
      final PreparedStatement ps = conn.prepareStatement(sb.toString(),
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      int index = 0;

      for (Object parameter : parameters) {
        final int sqlIndex = index + 1;
        if (parameter == null) {
          if (types == null || types.size() < index) {
            ps.setNull(sqlIndex, Types.NULL);
          } else {
            ps.setNull(sqlIndex, getSqlType(types.get(index)));
          }
        } else if (parameter instanceof String && parameter.toString().equals("")) {
          ps.setNull(sqlIndex, Types.VARCHAR);
        } else if (parameter instanceof Boolean) {
          ps.setObject(sqlIndex, ((Boolean) parameter) ? "Y" : "N");
        } else if (parameter instanceof BaseOBObject) {
          ps.setObject(sqlIndex, ((BaseOBObject) parameter).getId());
        } else if (parameter instanceof Timestamp) {
          ps.setTimestamp(sqlIndex, (Timestamp) parameter);
        } else if (parameter instanceof Date) {
          ps.setDate(sqlIndex, new java.sql.Date(((Date) parameter).getTime()));
        } else {
          ps.setObject(sqlIndex, parameter);
        }
        index++;
      }
      final ResultSet resultSet = ps.executeQuery();
      Object resultValue = null;
      if (resultSet.next()) {
        resultValue = resultSet.getObject("RESULT");
        if (resultSet.wasNull()) {
          resultValue = null;
        }
      }
      resultSet.close();
      ps.close();
      return resultValue;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private int getSqlType(Class<?> clz) {
    if (clz == null) {
      return Types.VARCHAR;
    }
    if (clz == Boolean.class) {
      return Types.VARCHAR;
    } else if (clz == String.class) {
      return Types.VARCHAR;
    } else if (clz == BaseOBObject.class) {
      return Types.VARCHAR;
    } else if (Number.class.isAssignableFrom(clz)) {
      return Types.NUMERIC;
    } else if (clz == Timestamp.class) {
      return Types.TIMESTAMP;
    } else if (Date.class.isAssignableFrom(clz)) {
      return Types.DATE;
    } else if (BaseOBObject.class.isAssignableFrom(clz)) {
      return Types.VARCHAR;
    } else {
      throw new IllegalStateException("Type not supported, please add it here " + clz.getName());
    }
  }
}
