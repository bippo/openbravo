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

package org.openbravo.base.structure;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.system.Language;

/**
 * Provides the identifier/title of an object using the {@link Entity#getIdentifierProperties()
 * identifierProperties} of the {@link Entity Entity}.
 * 
 * Note: the getIdentifier can also be generated in the java entity but the current approach makes
 * it possible to change the identifier definition at runtime.
 * 
 * @author mtaal
 */

public class IdentifierProvider implements OBSingleton {

  public static final String SEPARATOR = " - ";
  private static IdentifierProvider instance;

  public static synchronized IdentifierProvider getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(IdentifierProvider.class);
    }
    return instance;
  }

  public static synchronized void setInstance(IdentifierProvider instance) {
    IdentifierProvider.instance = instance;
  }

  private SimpleDateFormat dateFormat = null;
  private SimpleDateFormat dateTimeFormat = null;

  /**
   * Returns the identifier of the object. The identifier is computed using the identifier
   * properties of the Entity of the object. It is translated (if applicable) to the current
   * language
   * 
   * @param o
   *          the object for which the identifier is generated
   * @return the identifier
   */
  public String getIdentifier(Object o) {
    final Language lang = OBContext.getOBContext() != null ? OBContext.getOBContext().getLanguage()
        : null;
    return getIdentifier(o, true, lang);
  }

  // identifyDeep determines if refered to objects are used
  // to identify the object
  private String getIdentifier(Object o, boolean identifyDeep, Language language) {
    // TODO: add support for null fields
    final StringBuilder sb = new StringBuilder();
    final DynamicEnabled dob = (DynamicEnabled) o;
    final String entityName = ((Identifiable) dob).getEntityName();
    final List<Property> identifiers = ModelProvider.getInstance().getEntity(entityName)
        .getIdentifierProperties();

    for (final Property identifier : identifiers) {
      if (sb.length() > 0) {
        sb.append(SEPARATOR);
      }
      Property property = ((BaseOBObject) dob).getEntity().getProperty(identifier.getName());
      Object value;

      if (property.hasDisplayColumn()) {
        Property displayColumnProperty = DalUtil.getPropertyFromPath(property
            .getReferencedProperty().getEntity(), property.getDisplayPropertyName());
        BaseOBObject referencedObject = (BaseOBObject) dob.get(property.getName());
        if (referencedObject == null) {
          continue;
        }
        if (displayColumnProperty.hasDisplayColumn()) {
          // Allowing one level deep of displayed column pointing to references with display column
          value = ((BaseOBObject) dob.get(property.getDisplayPropertyName()))
              .get(displayColumnProperty.getDisplayPropertyName());
        } else if (!displayColumnProperty.isPrimitive()) {
          // Displaying identifier for non primitive properties

          value = ((BaseOBObject) referencedObject.get(property.getDisplayPropertyName()))
              .getIdentifier();
        } else {
          value = ((BaseOBObject) referencedObject)
              .get(property.getDisplayPropertyName(), language);
        }

        // Assign displayColumnProperty to apply formatting if needed
        property = displayColumnProperty;
      } else if (property.isTranslatable()) {
        value = ((BaseOBObject) dob).get(identifier.getName(), language);
      } else if (!property.isPrimitive() && identifyDeep) {
        if (dob.get(property.getName()) != null) {
          value = ((BaseOBObject) dob.get(property.getName())).getIdentifier();
        } else {
          value = "";
        }
      } else {
        value = dob.get(identifier.getName());
      }

      // TODO: add number formatting...
      if (property.isDate() || property.isDatetime()) {
        value = formatDate(property, (Date) value);
      }

      if (value instanceof Identifiable && identifyDeep) {
        sb.append(getIdentifier(value, false, language));
      } else if (value != null) {
        sb.append(value);
      }
    }
    if (identifiers.size() == 0) {
      return entityName + " (" + ((Identifiable) dob).getId() + ")";
    }
    return sb.toString();
  }

  protected String getSeparator() {
    return SEPARATOR;
  }

  private synchronized String formatDate(Property property, Date date) {
    if (date == null) {
      return "";
    }
    if (dateFormat == null) {
      final String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      final String dateTimeFormatString = OBPropertiesProvider.getInstance()
          .getOpenbravoProperties().getProperty("dateTimeFormat.java");
      dateFormat = new SimpleDateFormat(dateFormatString);
      dateTimeFormat = new SimpleDateFormat(dateTimeFormatString);
    }
    if (property.isDatetime()) {
      return dateTimeFormat.format(date);
    } else {
      return dateFormat.format(date);
    }
  }
}