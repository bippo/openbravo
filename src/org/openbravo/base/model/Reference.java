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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.domaintype.BaseEnumerateDomainType;
import org.openbravo.base.model.domaintype.DateDomainType;
import org.openbravo.base.model.domaintype.DatetimeDomainType;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.model.domaintype.StringDomainType;
import org.openbravo.base.model.domaintype.StringEnumerateDomainType;
import org.openbravo.base.util.OBClassLoader;

/**
 * Used by the {@link ModelProvider ModelProvider}, maps the AD_Reference table in the in-memory
 * model.
 * 
 * @author iperdomo
 */
public class Reference extends ModelObject {
  private static final Logger log = Logger.getLogger(Reference.class);

  // Ids of ReferenceTypes
  public static final String TABLE = "18";
  public static final String TABLEDIR = "19";
  public static final String SEARCH = "30";
  public static final String IMAGE = "32";
  public static final String IMAGE_BLOB = "4AA6C3BE9D3B4D84A3B80489505A23E5";
  public static final String RESOURCE_ASSIGNMENT = "33";
  public static final String PRODUCT_ATTRIBUTE = "35";
  public static final String NO_REFERENCE = "-1";

  // Validation Types
  /**
   * @deprecated validation type is not used anymore
   */
  public static final char TABLE_VALIDATION = 'T';
  /**
   * @deprecated validation type is not used anymore
   */
  public static final char SEARCH_VALIDATION = 'S';
  /**
   * @deprecated validation type is not used anymore
   */
  public static final char LIST_VALIDATION = 'L';

  private static HashMap<String, Class<?>> primitiveTypes;

  static {
    // Mapping reference id with a Java type
    primitiveTypes = new HashMap<String, Class<?>>();

    primitiveTypes.put("10", String.class);
    primitiveTypes.put("11", Long.class);
    primitiveTypes.put("12", BigDecimal.class);
    primitiveTypes.put("13", String.class);
    primitiveTypes.put("14", String.class);
    primitiveTypes.put("15", Date.class);
    primitiveTypes.put("16", Date.class);
    primitiveTypes.put("17", String.class);
    primitiveTypes.put("20", Boolean.class);
    primitiveTypes.put("22", BigDecimal.class);
    primitiveTypes.put("23", byte[].class); // Binary/Blob Data
    primitiveTypes.put("24", Timestamp.class);
    primitiveTypes.put("26", Object.class); // RowID is not used
    primitiveTypes.put("27", Object.class); // Color is not used
    primitiveTypes.put("28", Boolean.class);
    primitiveTypes.put("29", BigDecimal.class);
    primitiveTypes.put("34", String.class);
    primitiveTypes.put("800008", BigDecimal.class);
    primitiveTypes.put("800019", BigDecimal.class);
    primitiveTypes.put("800101", String.class);
  }

  /**
   * @deprecated use {@link PrimitiveDomainType#getPrimitiveType()}.
   */
  @SuppressWarnings({ "rawtypes" })
  public static Class getPrimitiveType(String id) {
    if (primitiveTypes.containsKey(id))
      return primitiveTypes.get(id);
    return Object.class;
  }

  private String modelImpl;
  private DomainType domainType;
  private Reference parentReference;
  private boolean baseReference;

  public boolean isBaseReference() {
    return baseReference;
  }

  public void setBaseReference(boolean baseReference) {
    this.baseReference = baseReference;
  }

  public boolean isPrimitive() {
    return getDomainType() instanceof PrimitiveDomainType;
  }

  public DomainType getDomainType() {
    if (domainType != null) {
      return domainType;
    }
    String modelImplementationClass = getModelImplementationClassName();
    if (modelImplementationClass == null) {
      log.error("Reference " + this + " has a modelImpl which is null, using String as the default");
      modelImplementationClass = StringDomainType.class.getName();
    }
    try {
      final Class<?> clz = OBClassLoader.getInstance().loadClass(modelImplementationClass);
      domainType = (DomainType) clz.newInstance();
      domainType.setReference(this);
    } catch (Exception e) {
      throw new OBException("Not able to create domain type " + getModelImpl() + " for reference "
          + this, e);
    }
    return domainType;
  }

  /**
   * Also calls the parent reference ({@link #getParentReference()}) to find the modelImpl (
   * {@link #getModelImpl()}).
   * 
   * @return the modelImpl or if not set, the value set in the parent.
   */
  public String getModelImplementationClassName() {
    // only call the parent if the parent is a base reference and this is not a basereference
    if (getModelImpl() == null && !isBaseReference() && getParentReference() != null
        && getParentReference().isBaseReference()) {
      return getParentReference().getModelImplementationClassName();
    }
    return getModelImpl();
  }

  public String getModelImpl() {
    return modelImpl;
  }

  public void setModelImpl(String modelImpl) {
    this.modelImpl = modelImpl;
  }

  public Reference getParentReference() {
    return parentReference;
  }

  public void setParentReference(Reference parentReference) {
    this.parentReference = parentReference;
  }

  /**
   * @deprecated validation type not used anymore, returns a space always
   */
  public char getValidationType() {
    log.warn("The validation type concept is not used anymore. The Reference.getValidationType() method is deprecated");
    return ' ';
  }

  /**
   * @deprecated validation type not used anymore
   */
  public void setValidationType(char validationType) {
  }

  /**
   * @deprecated check instance of {@link #getDomainType()}.
   */
  public boolean isDatetime() {
    return getDomainType() instanceof DatetimeDomainType;
  }

  /**
   * @deprecated check instance of {@link #getDomainType()}.
   */
  public boolean isDate() {
    return getDomainType() instanceof DateDomainType;
  }

  /**
   * @deprecated use {@link BaseEnumerateDomainType#addEnumerateValue(Object)}.
   * @see #getDomainType()
   */
  public void addAllowedValue(String value) {
    ((StringEnumerateDomainType) getDomainType()).addEnumerateValue(value);
  }

  /**
   * @deprecated use {@link StringEnumerateDomainType#getEnumerateValues()}.
   * @see #getDomainType()
   */
  public Set<String> getAllowedValues() {
    return ((StringEnumerateDomainType) getDomainType()).getEnumerateValues();
  }

}
