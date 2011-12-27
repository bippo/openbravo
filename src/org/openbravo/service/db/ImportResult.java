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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.db;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;

/**
 * Contains the result of an import action, i.e. warning, error, inserted objects and other import
 * result related information.
 * 
 * @author mtaal
 */

public class ImportResult {

  private List<BaseOBObject> updatedObjects = new ArrayList<BaseOBObject>();
  private List<BaseOBObject> insertedObjects = new ArrayList<BaseOBObject>();
  private String errorMessages;
  private String logMessages;
  private String warningMessages;
  private Throwable exception;

  /**
   * @return true if an Exception is present in the ImportResult or the errormessages are set
   */
  public boolean hasErrorOccured() {
    return exception != null || (errorMessages != null && errorMessages.trim().length() > 0);
  }

  /**
   * The list of objects which have been updated. Note that for these objects the
   * {@link OBDal#save(Object)} method has been called, but the commit is the responsibility of the
   * caller of the import service.
   * <p/>
   * This list is empty if an error has occurred during the import (@see {@link #hasErrorOccured()}.
   * 
   * @return the list of objects which have been updated.
   */
  public List<BaseOBObject> getUpdatedObjects() {
    return updatedObjects;
  }

  /**
   * The list of objects which have been inserted. Note that for these objects the
   * {@link OBDal#save(Object)} method has been called, but the commit is the responsibility of the
   * caller of the import service.
   * <p/>
   * This list is empty if an error has occurred during the import (@see {@link #hasErrorOccured()}.
   * 
   * @return the list of objects which have been inserted.
   */
  public List<BaseOBObject> getInsertedObjects() {
    return insertedObjects;
  }

  /**
   * @return the error messages, if no error messages then null is returned.
   */
  public String getErrorMessages() {
    // append the exception message
    if (getException() != null) {
      if (errorMessages != null) {
        return errorMessages + "\n" + getException().getMessage();
      }
      return getException().getMessage();
    }
    return errorMessages;
  }

  void setErrorMessages(String errorMessages) {
    this.errorMessages = errorMessages;
  }

  /**
   * @return the log messages, if no log messages then null is returned.
   */
  public String getLogMessages() {
    return logMessages;
  }

  void setLogMessages(String logMessages) {
    this.logMessages = logMessages;
  }

  /**
   * @return if an Exception occurred during import then this Exception can be retrieved through
   *         this method
   */
  public Throwable getException() {
    return exception;
  }

  void setException(Throwable exception) {
    this.exception = exception;

  }

  /**
   * @return the warning messages, if no warning messages then null is returned.
   */
  public String getWarningMessages() {
    return warningMessages;
  }

  void setWarningMessages(String warningMessages) {
    this.warningMessages = warningMessages;
  }
}