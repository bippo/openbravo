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
package org.openbravo.reference.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.BuscadorData;

public class UIYesNo extends UIReference {
  public UIYesNo(String reference, String subreference) {
    super(reference, subreference);
  }

  public void generateFilterHtml(StringBuffer strHtml, VariablesSecureApp vars,
      BuscadorData fields, String strTab, String strWindow, ArrayList<String> vecScript,
      Vector<Object> vecKeys) throws IOException, ServletException {
    UIList list = new UIList("17", "47209D76F3EE4B6D84222C5BDF170AA2");
    list.generateFilterHtml(strHtml, vars, fields, strTab, strWindow, vecScript, null);
  }

  public void generateFilterAcceptScript(BuscadorData field, StringBuffer params,
      StringBuffer paramsData) {
    UITableDir tableDir = new UITableDir(reference, subReference);
    tableDir.generateFilterAcceptScript(field, params, paramsData);
  }
}
