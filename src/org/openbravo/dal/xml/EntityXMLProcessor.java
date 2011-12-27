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

package org.openbravo.dal.xml;

import java.util.List;

import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;

/**
 * The entity xml processor is used to intercept specific actions during import and xml conversion
 * of business objects.
 * 
 * @author mtaal
 */

public interface EntityXMLProcessor {

  /**
   * This method is called after the import process has parsed the xml and created the in-memory
   * object graph of objects which are inserted and updated in the database.
   * 
   * This method can access the database using the Data Access Layer. It will operate in the same
   * transaction as the import process itself.
   * 
   * @param newObjects
   *          the list of objects which will be inserted into the database
   * @param updatedObjects
   *          the list of objects which will be updated in the database
   */
  public void process(List<BaseOBObject> newObjects, List<BaseOBObject> updatedObjects);

  /**
   * This method allows you to correct a value just before it is being set in an object which is
   * being imported.
   * 
   * This method is called just before a new primitive or refernence value is set in an imported
   * object. This is called during an import of an object so the object maybe in an invalid state.
   * 
   * This method is not called for one-to-many properties.
   * 
   * @param owner
   *          the owner object of the property
   * @param property
   *          the property being set
   * @param importedValue
   *          the value converted from the import xml
   * @return a new value which is used by the import process
   */
  public Object replaceValue(BaseOBObject owner, Property property, Object importedValue);
}