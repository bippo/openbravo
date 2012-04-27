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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.model.domaintype.StringEnumerateDomainType;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;

/**
 * A representation of the {@link DataSourceField} which can be read from the DataSourceField table
 * or created on the basis of an existing {@link Property} from the in-memory model.
 * 
 * DataSourceProperties are passed into the data source template to generate the data source
 * representation.
 * 
 * This class provides static factory methods for different ways of creating it.
 * 
 * @author mtaal
 */
public class DataSourceProperty {

  private static long DEFAULT_SEQNO = Long.MAX_VALUE;

  /**
   * Create a DataSourceProperty using a model property.
   * 
   * @param property
   *          the property to use to initialize the data source property
   * @return a new DataSourceProperty instance
   */
  public static DataSourceProperty createFromProperty(Property property) {
    final DataSourceProperty dsProperty = new DataSourceProperty();
    dsProperty.setName(property.getName());
    dsProperty.setId(property.isId());
    // note booleans are never mandatory as they only have inputs with 2 values
    dsProperty.setMandatory(!property.isBoolean() && property.isMandatory());
    dsProperty.setAuditInfo(property.isAuditInfo());
    dsProperty.setUpdatable(property.isUpdatable());
    dsProperty.setBoolean(property.isBoolean());
    dsProperty.setAllowedValues(property.getAllowedValues());
    if (property.getAllowedValues() != null && property.getAllowedValues().size() > 0) {
      dsProperty.setValueMap(createValueMap(property.getAllowedValues(), property.getDomainType()
          .getReference().getId()));
    }
    dsProperty.setPrimitive(property.isPrimitive());
    dsProperty.setFieldLength(property.getFieldLength());
    dsProperty.setUIDefinition(UIDefinitionController.getInstance().getUIDefinition(
        property.getColumnId()));
    if (dsProperty.isPrimitive()) {
      dsProperty.setPrimitiveObjectType(property.getPrimitiveObjectType());
      dsProperty.setNumericType(property.isNumericType());
    } else {
      dsProperty.setTargetEntity(property.getTargetEntity());
    }
    return dsProperty;
  }

  /**
   * Create a DataSourceProperty using a {@link DataSourceField} to initialize it.
   * 
   * @param dsField
   *          the data source field used to initialize the new data source property
   * @return a new DataSourceProperty instance
   */
  public static DataSourceProperty createFromDataSourceField(DatasourceField dsField) {
    final DataSourceProperty dsProperty = new DataSourceProperty();
    dsProperty.setName(dsField.getName());
    dsProperty.setId(false);
    dsProperty.setMandatory(false);
    dsProperty.setAuditInfo(false);
    dsProperty.setUpdatable(false);

    // not supported by explicit data source fields
    // dsProperty.setAllowedValues(property.getAllowedValues());

    dsProperty.setUIDefinition(UIDefinitionController.getInstance().getUIDefinition(
        dsField.getReference()));
    final DomainType domainType = getDomainType(dsField.getReference());
    if (domainType instanceof StringEnumerateDomainType) {
      final StringEnumerateDomainType enumDomainType = (StringEnumerateDomainType) domainType;
      dsProperty.setValueMap(createValueMap(enumDomainType.getEnumerateValues(), dsField
          .getReference().getId()));
    } else if (domainType instanceof PrimitiveDomainType) {
      final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) domainType;
      dsProperty.setBoolean(primitiveDomainType.getPrimitiveType() == Boolean.class);
      dsProperty.setPrimitive(true);
      // not supported by explicit data source fields yet
      // dsProperty.setFieldLength(property.getFieldLength());
      dsProperty.setPrimitiveObjectType(primitiveDomainType.getPrimitiveType());
      dsProperty.setNumericType(Number.class.isAssignableFrom(dsProperty.getPrimitiveObjectType()));
    } else {
      // TODO: make use of the column in the dsField
      Check.isTrue(dsField.getTable() != null,
          "Reference is a foreign key reference but the table is not set for this data source field "
              + dsField);
      final Entity targetEntity = ModelProvider.getInstance().getEntity(
          dsField.getTable().getName());
      dsProperty.setTargetEntity(targetEntity);
    }
    return dsProperty;
  }

  /**
   * Gets a Set of allowed values for a List reference
   * 
   * @param reference
   *          List reference
   * @return
   */
  public static Set<String> getAllowedValues(Reference reference) {
    Set<String> allowedValues = new HashSet<String>();

    final String hql = "select al.searchKey from ADList al" + " where al.reference=:ref";
    final Query qry = OBDal.getInstance().getSession().createQuery(hql);
    qry.setParameter("ref", reference);
    for (Object o : qry.list()) {
      final String value = (String) o;
      allowedValues.add(value);
    }
    return allowedValues;
  }

  public static List<RefListEntry> createValueMap(Set<String> allowedValues, String referenceId) {
    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();

    final List<RefListEntry> translatedValues = new ArrayList<RefListEntry>();

    final String readReferenceHql = "select searchKey, name, sequenceNumber from ADList rlist where reference.id=? and rlist.active=true";
    final Query readReferenceQry = OBDal.getInstance().getSession().createQuery(readReferenceHql);
    readReferenceQry.setString(0, referenceId);
    for (Object o : readReferenceQry.list()) {
      final Object[] row = (Object[]) o;
      final String value = (String) row[0];
      final String name = (String) row[1];
      final Long seqNo = (Long) row[2];
      if (allowedValues.contains(value)) {
        final RefListEntry refListEntry = new RefListEntry();
        refListEntry.setLabel(name);
        refListEntry.setValue(value);
        if (seqNo == null) {
          refListEntry.setSeqno(DEFAULT_SEQNO);
        } else {
          refListEntry.setSeqno(seqNo);
        }
        translatedValues.add(refListEntry);
      }
    }

    // set the default if no translation found
    final String hql = "select al.searchKey, trl.name from ADList al, ADListTrl trl where "
        + " al.reference.id=? and trl.listReference=al and trl.language.id=?"
        + " and al.active=true and trl.active=true";
    final Query qry = OBDal.getInstance().getSession().createQuery(hql);
    qry.setString(0, referenceId);
    qry.setString(1, userLanguageId);
    for (Object o : qry.list()) {
      final Object[] row = (Object[]) o;
      for (RefListEntry entry : translatedValues) {
        if (entry.getValue().equalsIgnoreCase((String) row[0])) {
          entry.setLabel((String) row[1]);
        }
      }
    }
    Collections.sort(translatedValues, new RefListEntryComparator());
    return translatedValues;
  }

  private static DomainType getDomainType(Reference reference) {
    try {
      final Class<?> clz = OBClassLoader.getInstance().loadClass(reference.getModelImpl());
      final DomainType domainType = (DomainType) clz.newInstance();
      // can't be set
      // note for our purpose this not need to be set
      // domainType.setReference(reference);
      return domainType;
    } catch (Exception e) {
      throw new OBException("Not able to create domain type for reference " + reference, e);
    }
  }

  private String name;
  private boolean id;
  private boolean mandatory;
  private boolean auditInfo;
  private boolean updatable;
  private boolean isBoolean;
  private Set<String> allowedValues = new HashSet<String>();
  private boolean primitive;
  private int fieldLength;
  private Class<?> primitiveObjectType;
  private Entity targetEntity;
  private boolean numericType;
  private List<RefListEntry> valueMap;
  private UIDefinition uiDefinition;
  private boolean additional = false;

  public String getType() {
    return uiDefinition.getName();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isId() {
    return id;
  }

  public void setId(boolean id) {
    this.id = id;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  public boolean isAuditInfo() {
    return auditInfo;
  }

  public void setAuditInfo(boolean auditInfo) {
    this.auditInfo = auditInfo;
  }

  public boolean isUpdatable() {
    return updatable;
  }

  public void setUpdatable(boolean updatable) {
    this.updatable = updatable;
  }

  public boolean isBoolean() {
    return isBoolean;
  }

  public void setBoolean(boolean isBoolean) {
    this.isBoolean = isBoolean;
  }

  public Set<String> getAllowedValues() {
    return allowedValues;
  }

  public void setAllowedValues(Set<String> allowedValues) {
    this.allowedValues = allowedValues;
  }

  public boolean isPrimitive() {
    return primitive;
  }

  public void setPrimitive(boolean primitive) {
    this.primitive = primitive;
  }

  public int getFieldLength() {
    return fieldLength;
  }

  public void setFieldLength(int fieldLength) {
    this.fieldLength = fieldLength;
  }

  public Class<?> getPrimitiveObjectType() {
    return primitiveObjectType;
  }

  public void setPrimitiveObjectType(Class<?> primitiveObjectType) {
    this.primitiveObjectType = primitiveObjectType;
  }

  public Entity getTargetEntity() {
    return targetEntity;
  }

  public void setTargetEntity(Entity targetEntity) {
    this.targetEntity = targetEntity;
  }

  public boolean isNumericType() {
    return numericType;
  }

  public void setNumericType(boolean numericType) {
    this.numericType = numericType;
  }

  public List<RefListEntry> getValueMapContent() {
    return valueMap;
  }

  public void setValueMap(List<RefListEntry> valueMap) {
    this.valueMap = valueMap;
  }

  public UIDefinition getUIDefinition() {
    return uiDefinition;
  }

  public void setUIDefinition(UIDefinition uiDefinition) {
    this.uiDefinition = uiDefinition;
  }

  public static class RefListEntry {
    private String value;
    private String label;
    private long seqno;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public long getSeqno() {
      return seqno;
    }

    public void setSeqno(long seqno) {
      this.seqno = seqno;
    }

  }

  private static class RefListEntryComparator implements Comparator<RefListEntry> {

    @Override
    public int compare(RefListEntry o1, RefListEntry o2) {
      if (o1.getSeqno() == o2.getSeqno()) {
        return o1.getLabel().compareTo(o2.getLabel());
      }
      final long v1 = o1.getSeqno();
      final long v2 = o2.getSeqno();

      return (v1 < v2 ? -1 : 1);
    }
  }

  public boolean isAdditional() {
    return additional;
  }

  public void setAdditional(boolean additional) {
    this.additional = additional;
  }
}
