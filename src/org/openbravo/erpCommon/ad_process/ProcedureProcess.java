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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.RDBMSIndependent;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.exception.PoolNotFoundException;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessContext;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;

/**
 * The top level procedure process that all background database procedure processes should extend.
 * 
 * @author awolski
 * 
 */
public abstract class ProcedureProcess extends DalBaseProcess {

  static Logger log = Logger.getLogger(ProcedureProcess.class);

  /**
   * The sql procedure call to execute.
   */
  private String sql;

  /**
   * The parameters required in by the procedure.
   */
  private String[] params;

  /**
   * The types of the parameters (length must match length of parameter array).
   */
  private String[] types;

  /**
   * The Connection to the database used during the execution of the procedure.
   */
  protected ConnectionProvider connection;

  /**
   * A logger object - output will be viewable in the process monitor log.
   */
  protected ProcessLogger logger;

  /**
   * An initialization method is called before executing the procedure. Subclasses of the
   * ProcedureProcess should initialize any parameters (such as client or organization) in this
   * method.
   * 
   * @param bundle
   *          the process bundle containing process and context specific information
   */
  protected abstract void init(ProcessBundle bundle);

  /**
   * The method that will execute when this process fires.
   * 
   * @param bundle
   *          the process bundle containing process and context specific information
   */
  public void doExecute(final ProcessBundle bundle) throws Exception {
    init(bundle);

    if (sql == null) {
      throw new OBException("SQL cannot be null.");
    }

    if (params != null && types != null && params.length != types.length) {
      throw new OBException("Number of parameters not equal to number of parameter types.");
    }

    logger = bundle.getLogger();
    connection = bundle.getConnection();

    CallableStatement st = null;
    if (connection.getRDBMS().equalsIgnoreCase("ORACLE")) {
      try {
        st = connection.getCallableStatement(sql);

        if (params != null) {
          for (int i = 0; i < params.length; i++) {
            String type = types[i];
            String value = params[i];

            int iParameter = i + 1;
            UtilSql.setValue(st, iParameter, 12, null, value);
          }
        }
        st.execute();

      } catch (final SQLException e) {
        log("SQL error in query: " + sql + "Exception: ", e);
        throw new Exception("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());

      } catch (final Exception e) {
        log("Exception in query: " + sql + "Exception: ", e);
        throw new Exception("@CODE=@" + e.getMessage());

      } finally {
        log("Process completed successfully.", bundle.getContext());
        try {
          connection.releasePreparedStatement(st);

        } catch (final Exception ignore) {
          ignore.printStackTrace();
        }
      }

    } else {
      final Vector<String> parametersData = new Vector<String>();
      final Vector<String> parametersTypes = new Vector<String>();

      if (params != null) {
        for (int i = 0; i < params.length; ++i) {
          String type = types[i];
          String value = params[i];

          parametersTypes.addElement(type);
          parametersData.addElement(value);
        }
      }
      try {
        RDBMSIndependent.getCallableResult(null, connection, sql, parametersData, parametersTypes,
            0);

      } catch (final SQLException e) {
        log("SQL error in query: " + sql + "Exception: ", e);
        throw new Exception("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());

      } catch (final NoConnectionAvailableException e) {
        log("Connection error in query: " + sql + "Exception: ", e);
        throw new Exception("@CODE=NoConnectionAvailable");

      } catch (final PoolNotFoundException e) {
        log("Pool error in query: " + sql + "Exception: ", e);
        throw new Exception("@CODE=NoConnectionAvailable");

      } catch (final Exception e) {
        log("Exception in query: " + sql + "Exception: ", e);
        throw new Exception("@CODE=@" + e.getMessage());
      }
      log("Process completed successfully.", bundle.getContext());
    }
  }

  /**
   * Set the sql (procedure) that this process will execute.
   * 
   * @param sql
   *          the sql/procedure to execute
   */
  protected final void setSQL(String sql) {
    this.sql = sql;
  }

  /**
   * Set the procedure's parameters and the parameter types.
   * 
   * @param params
   *          the parameters for the procedure
   * @param types
   *          the types of the parameters
   */
  protected final void setParams(final String[] params, final String[] types) {
    if (params != null && types != null && params.length != types.length) {
      throw new OBException("Number of parameters not equal to number of parameter types.");
    }
    this.params = params;
    this.types = types;
  }

  /**
   * Log an error to the process' output log (used by process monitor).
   * 
   * @param msg
   * @param e
   */
  protected void log(String msg, Exception e) {
    log.error(msg, e);
    logger.log(msg + e.getMessage());
  }

  /**
   * Log a message to the process' output log (used by the process monitor).
   * 
   * @param message
   *          the message to log
   * @param context
   *          the process' execution bundle/context
   */
  protected void log(String message, ProcessContext context) {
    logger.log(message);
  }

}
