/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2010 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.apache.log4j.Logger;

public class EMailAuthenticator extends Authenticator {
  static Logger log4j = Logger.getLogger(EMailAuthenticator.class);

  private PasswordAuthentication m_pass;

  public EMailAuthenticator(String username, String password) {
    m_pass = new PasswordAuthentication(username, password);
    if (username == null || username.length() == 0) {
      log4j.error("EMailAuthenticator - Username is NULL");
      Thread.dumpStack();
    }
    if (password == null || password.length() == 0) {
      log4j.error("EMailAuthenticator - Password is NULL");
      Thread.dumpStack();
    }
  }

  protected PasswordAuthentication getPasswordAuthentication() {
    return m_pass;
  }

  public String toString() {
    if (m_pass == null)
      return "EMailAuthenticator[]";
    else
      return "EMailAuthenticator[" + m_pass.getUserName() + "/" + m_pass.getPassword() + "]";
  }

  private String getUserName() {
    return m_pass.getUserName();
  }

  private String getPassword() {
    return m_pass.getPassword();
  }

}
