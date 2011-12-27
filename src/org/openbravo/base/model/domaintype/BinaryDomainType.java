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

package org.openbravo.base.model.domaintype;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

/**
 * The type for a binary (image for example) column.
 * 
 * @author mtaal
 */

public class BinaryDomainType extends BasePrimitiveDomainType {

  public Class<?> getPrimitiveType() {
    return byte[].class;
  }

  @Override
  public String convertToString(Object value) {
    try {
      if (value == null) {
        return EMPTY_STRING;
      }
      return new String(Base64.encodeBase64((byte[]) value), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Object createFromString(String strValue) {
    try {
      if (strValue == null || strValue.trim().length() == 0) {
        return null;
      }
      return Base64.decodeBase64(strValue.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public String getXMLSchemaType() {
    return "ob:base64Binary";
  }

}
