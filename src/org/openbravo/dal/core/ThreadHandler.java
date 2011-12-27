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

package org.openbravo.dal.core;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;

/**
 * A convenience class which can be used as a base class for when specific actions need to be done
 * before or after a thread has run. cleaning up certain threadlocals.
 * 
 * @author mtaal
 */

public abstract class ThreadHandler {
  private static final Logger log = Logger.getLogger(ThreadHandler.class);

  /**
   * Run the thread, this method will call the protected methods doBefore, doAction and doFinal.
   */
  public void run() {
    boolean err = true;
    try {
      log.debug("Thread started --> doBefore");
      doBefore();
      log.debug("Thread --> doAction");
      doAction();
      log.debug("Thread --> Action done");
      err = false;
      // TODO add exception logging/tracing/emailing
      // } catch (Throwable t) {
      // ExceptionHandler.reportThrowable(t, (HttpServletRequest)
      // request);
      // throw new ServletException(t);
    } catch (final ServletException se) {
      if (se.getRootCause() != null) {
        throw new OBException("Exception thrown " + se.getRootCause().getMessage(),
            se.getRootCause());
      } else {
        throw new OBException("Exception thrown " + se.getMessage(), se);
      }
    } catch (final Throwable t) {
      log.error(t.getMessage(), t);
      throw new OBException("Exception thrown " + t.getMessage(), t);
    } finally {
      doFinal(err);
    }
  }

  protected abstract void doBefore() throws Exception;

  protected abstract void doFinal(boolean errorOccured);

  protected abstract void doAction() throws Exception;
}