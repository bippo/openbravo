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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The root class for the model types. Used for the bootstrap model (Column, Table, etc.).
 * 
 * @author mtaal
 */

public class ModelObject {

  private String id = null;
  private boolean active = true;
  private String name;
  private Date updated;

  private Map<String, Object> data = new HashMap<String, Object>();

  public void set(String propertyName, Object value) {
    // TODO: externalise the strings
    if (propertyName.compareTo("id") == 0) {
      setId((String) value);
      return;
    } else if (propertyName.compareTo("active") == 0) {
      setActive((Boolean) value);
      return;
    }
    data.put(propertyName, value);
  }

  public Object get(String propertyName) {
    // TODO: externalise the strings
    if (propertyName.compareTo("id") == 0)
      return getId();
    else if (propertyName.compareTo("active") == 0)
      return isActive();
    return data.get(propertyName);
  }

  public boolean isNew() {
    return id == null;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public String getIdentifier() {
    return getClass().getName() + "(" + getId() + ", " + getName() + ")";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public String toString() {
    return this.getClass().getName() + " [id: " + id + ", name: " + name + "]";
  }
}