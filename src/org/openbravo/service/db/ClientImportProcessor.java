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

import java.util.List;

import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.xml.EntityXMLProcessor;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;

/**
 * This ImportProcessor is used during client import. It repairs the names of client, user and a
 * number of other entities to prevent unique constraint violations during client import.
 * 
 * The {@link #replaceValue} method does not contain logic in this class.
 * 
 * @author mtaal
 */

public class ClientImportProcessor implements EntityXMLProcessor {

  private String newName;

  /**
   * @see EntityXMLProcessor#process(List, List)
   */
  public void process(List<BaseOBObject> newObjects, List<BaseOBObject> updatedObjects) {

    // - Client.name
    // - Client.description
    // - Role.name
    // - User.username
    // - Organization.name
    // - Warehouse.name

    String originalName = null;

    // find the original client name
    for (final BaseOBObject bob : newObjects) {
      if (bob instanceof Client) {
        originalName = ((Client) bob).getName();
      }
    }

    // now replace the names in the corresponding entities
    for (final BaseOBObject bob : newObjects) {
      replaceName(originalName, bob);
    }
  }

  /**
   * Always returns the importedValue without changing it.
   * 
   * @see EntityXMLProcessor#replaceValue(BaseOBObject, Property, Object)
   */
  public Object replaceValue(BaseOBObject owner, Property property, Object importedValue) {
    return importedValue;
  }

  private void replaceName(String originalName, BaseOBObject bob) {
    if (newName == null || newName.trim().length() == 0) {
      return;
    }
    if (bob instanceof Client) {
      final Client client = (Client) bob;
      client.setName(newName);
      client.setDescription(newName);
      client.setSearchKey(newName);
    } else if (bob instanceof Role) {
      final Role role = (Role) bob;
      if (role.getName().indexOf(originalName) == -1) {
        role.setName(getNewName() + "_" + role.getName());
      } else {
        role.setName(replace(role.getName(), originalName));
      }
    } else if (bob instanceof User) {
      final User user = (User) bob;
      if (user.getUsername() == null) {
        return;
      }
      if (user.getUsername().indexOf(originalName) == -1) {
        user.setUsername(getNewName() + "_" + user.getUsername());
      } else {
        user.setUsername(replace(user.getUsername(), originalName));
      }
    } else if (bob instanceof Organization) {
      final Organization org = (Organization) bob;
      if (org.getName().indexOf(originalName) == -1) {
        org.setName(getNewName() + "_" + org.getName());
      } else {
        org.setName(replace(org.getName(), originalName));
      }
    } else if (bob instanceof Warehouse) {
      final Warehouse wh = (Warehouse) bob;
      if (wh.getName().indexOf(originalName) == -1) {
        wh.setName(getNewName() + "_" + wh.getName());
      } else {
        wh.setName(replace(wh.getName(), originalName));
      }
      // } else if (bob instanceof GLCategory) {
      // final GLCategory glCategory = (GLCategory) bob;
      // glCategory.setName(replace(glCategory.getName(), originalName));
      // } else if (bob instanceof DocumentType) {
      // final DocumentType docType = (DocumentType) bob;
      // docType.setName(replace(docType.getName(), originalName));
    }
  }

  protected String replace(String currentValue, String orginalName) {
    if (currentValue == null) {
      return null;
    }
    return currentValue.replace(orginalName, newName);
  }

  public String getNewName() {
    return newName;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }
}