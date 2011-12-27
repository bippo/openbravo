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

package org.openbravo.base.model;

/**
 * Models a simple version of the module present in the database. It is a simple version because
 * only the properties required for the module related functionality are modeled here.
 * 
 * @author mtaal
 */

public class Module extends ModelObject {
  private String name;
  private Integer seqno;
  private String javaPackage;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public Integer getSeqno() {
    return seqno;
  }

  public void setSeqno(Integer seqno) {
    this.seqno = seqno;
  }

  public String getJavaPackage() {
    return javaPackage;
  }

  public void setJavaPackage(String javaPackage) {
    this.javaPackage = javaPackage;
  }
}
