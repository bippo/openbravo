/*
 * ************************************************************************ The
 * contents of this file are subject to the Openbravo Public License Version 1.1
 * (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2001-2010 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 * ***********************************************************************
 */
package org.openbravo.erpCommon.utility.poc;

public enum EmailAddressType {
  UNDEFINED(0), FROM(1), TO(2), CC(3), BCC(4);

  private int _value;

  private EmailAddressType(int value) {
    _value = value;
  }

  public int getValue() {
    return _value;
  }

  public String getStringValue() {
    return "" + _value;
  }
}
