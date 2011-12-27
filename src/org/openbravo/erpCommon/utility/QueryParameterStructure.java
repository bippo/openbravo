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

import java.util.Hashtable;

class QueryParameterStructure {
  private Hashtable<String, String> data = new Hashtable<String, String>();

  public QueryParameterStructure() {
  }

  public QueryParameterStructure(String _name, String _fieldName, String _type) {
    this(_name, _fieldName, _type, "");
  }

  public QueryParameterStructure(String _name, String _fieldName, String _type, String _realName) {
    setData("name", _name);
    setData("fieldName", _fieldName);
    setData("type", _type);
    setData("realName", _realName);
  }

  public void setData(String name, String value) {
    if (name == null)
      return;
    if (this.data == null)
      this.data = new Hashtable<String, String>();
    if (value == null || value.equals(""))
      this.data.remove(name);
    else
      this.data.put(name, value);
  }

  public String getData(String name) {
    return data.get(name);
  }

  public String getType() {
    return getData("type");
  }

  public String getField() {
    return getData("fieldName");
  }

  public String getName() {
    return getData("name");
  }

  public String getRealName() {
    return getData("realName");
  }

  public String toString() {
    StringBuffer text = new StringBuffer();
    text.append(getData("name"));
    return text.toString();
  }
}
