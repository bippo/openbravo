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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.util.HashMap;
import java.util.Map;

public class OBError {
  private String type = "";
  private String title = "";
  private String message = "";
  private boolean connectionAvailable = true;

  public OBError() {
  }

  public void setType(String _data) {
    if (_data == null)
      _data = "";
    this.type = _data;
  }

  public String getType() {
    return ((this.type == null) ? "Hidden" : this.type);
  }

  public void setTitle(String _data) {
    if (_data == null)
      _data = "";
    this.title = _data;
  }

  public String getTitle() {
    return ((this.title == null) ? "" : this.title);
  }

  public void setMessage(String _data) {
    if (_data == null)
      _data = "";
    this.message = _data;
  }

  public String getMessage() {
    return ((this.message == null) ? "" : this.message);
  }

  public void setConnectionAvailable(boolean _data) {
    this.connectionAvailable = _data;
  }

  public boolean isEmpty() {
    return (getTitle().equals("") && getMessage().equals("") && getType().equals(""));
  }

  public void setError(OBError e) {
    setTitle(BasicUtility.formatMessageBDToHtml(e.getTitle()));
    setMessage(BasicUtility.formatMessageBDToHtml(e.getMessage()));
    setType(e.getType());
    setConnectionAvailable(e.isConnectionAvailable());
  }

  public boolean isConnectionAvailable() {
    return this.connectionAvailable;
  }

  public Map<String, String> toMap() {
    Map<String, String> o = new HashMap<String, String>();
    o.put("title", title);
    o.put("message", message);
    o.put("type", type);
    return o;
  }
}
