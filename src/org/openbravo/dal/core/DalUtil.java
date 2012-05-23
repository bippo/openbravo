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

package org.openbravo.dal.core;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.proxy.HibernateProxy;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.ArgumentException;
import org.openbravo.base.util.Check;

/**
 * Utility class used by the dal layer. It contains several copy methods and methods to retrieve the
 * EntityName and id of an object without forcing a load by Hibernate.
 * 
 * @author mtaal
 */

public class DalUtil {

  private static final String IDENTIFIER_PART_PATH = "_identifier";

  public static final String FIELDSEPARATOR = ".";
  public static final String DOT = ".";

  /**
   * Sorts the passed list according to the identifier of the objects.
   * 
   * @param objects
   *          the objects (must be BaseOBObjects) to sort.
   */
  public static void sortByIdentifier(List<? extends BaseOBObject> objects) {
    Collections.sort(objects, new OBObjectComparator());
  }

  private static class OBObjectComparator implements Comparator<BaseOBObject> {

    @Override
    public int compare(BaseOBObject arg0, BaseOBObject arg1) {
      return arg0.getIdentifier().compareTo(arg1.getIdentifier());
    }
  }

  /**
   * Translates a so-called property path to a property. The passed entity is the starting entity.
   * For example the property: organization.name and entity: Product will result in the
   * Organization.name property to be returned.
   * 
   * @param entity
   *          the start entity for the property path
   * @param propertyPath
   *          the property path, dot-separated property names
   * @return the found property
   */
  public static Property getPropertyFromPath(Entity entity, String propertyPath) {
    final String[] parts = propertyPath.split("\\.");
    Entity currentEntity = entity;
    Property result = null;
    for (String part : parts) {
      // only consider it as an identifier if it is called an identifier and
      // the entity does not accidentally have an identifier property
      // && !currentEntity.hasProperty(part)
      // NOTE disabled for now, there is one special case: AD_Column.IDENTIFIER
      // which is NOT HANDLED
      if (part.equals(IDENTIFIER_PART_PATH)) {
        // pick the first identifier property
        if (currentEntity.getIdentifierProperties().isEmpty()) {
          return null;
        }
        return currentEntity.getIdentifierProperties().get(0);
      }
      if (!currentEntity.hasProperty(part)) {
        return null;
      }
      result = currentEntity.getProperty(part);
      if (result.getTargetEntity() != null) {
        currentEntity = result.getTargetEntity();
      }
    }
    return result;
  }

  /**
   * Finds a property using the db column name and table name.
   * 
   * @param tableName
   *          the table name, is used to find the {@link Entity}
   * @param dbColumnName
   *          is used to find the {@link Property}
   * @return a Property or null if no property found
   */
  public static Property getProperty(String tableName, String dbColumnName) {
    final Entity entity = ModelProvider.getInstance().getEntityByTableName(tableName);
    for (Property property : entity.getProperties()) {
      if (property.getColumnName().equalsIgnoreCase(dbColumnName)) {
        return property;
      }
    }
    return null;
  }

  /**
   * Translates a so-called property path to a property. The passed entity is the starting entity.
   * For example the property: organization.name and entity: Product will result in the
   * Organization.name property to be returned.
   * 
   * @param bob
   *          the start entity for the property path
   * @param propertyPath
   *          the property path, dot-separated property names
   * @return the found property
   */
  public static Object getValueFromPath(BaseOBObject bob, String propertyPath) {
    final String[] parts = propertyPath.split("\\.");
    BaseOBObject currentBob = bob;
    Property result = null;
    Object value = null;
    for (String part : parts) {
      // only consider it as an identifier if it is called an identifier and
      // the entity does not accidentally have an identifier property
      // && !currentEntity.hasProperty(part)
      // NOTE disabled for now, there is one special case: AD_Column.IDENTIFIER
      // which is NOT HANDLED
      if (part.equals(IDENTIFIER_PART_PATH)) {
        return currentBob.getIdentifier();
      }
      final Entity currentEntity = currentBob.getEntity();
      if (!currentEntity.hasProperty(part)) {
        return null;
      }
      value = currentBob.get(part);
      // if there is a next step, just make it
      // if it is last then we stop anyway
      if (value instanceof BaseOBObject) {
        currentBob = (BaseOBObject) value;
      } else {
        return value;
      }
    }
    return result;
  }

  /**
   * Copies a BaseOBObject and all its children (see {@link Entity#getParentProperties() parent}).
   * Referenced objects are not copied. The id of the copied objects are all set to null.
   * 
   * @param source
   *          the list of BaseOBObject which will be copied
   * @return the copied BaseOBObject, child objects are also copied
   */
  public static List<BaseOBObject> copyAll(List<BaseOBObject> source) {
    return copyAll(source, true);
  }

  /**
   * Copies a BaseOBObject and all its children (see {@link Entity#getParentProperties() parent})
   * are copied recursively. Referenced objects are not copied. The resetId parameter controls if
   * the id of the copied objects is nullified.
   * 
   * @param source
   *          the list of BaseOBObject which will be copied
   * @param resetId
   *          if true then the id's of the copied objects will be nullified
   * @return the copied BaseOBObject, child objects are also copied
   */
  public static List<BaseOBObject> copyAll(List<BaseOBObject> source, boolean resetId) {
    final List<BaseOBObject> result = new ArrayList<BaseOBObject>();
    final Map<BaseOBObject, BaseOBObject> fromTo = new HashMap<BaseOBObject, BaseOBObject>();
    for (final BaseOBObject bob : source) {
      result.add(copy(bob, true, resetId, fromTo));
    }
    repairReferences(fromTo);
    return result;
  }

  /**
   * Copies one object and its children. The id of the copied object (and its children) is set to
   * null.
   * 
   * @param source
   *          the object to be copied
   * @return the copied object
   */
  public static BaseOBObject copy(BaseOBObject source) {
    return copy(source, true);
  }

  /**
   * Copies one object. The copyChildren parameter controls if the children of the object are also
   * copied. The id of the copied object (and its children) is set to null.
   * 
   * @param source
   *          the object to be copied
   * @param copyChildren
   *          this parameter controls if the children of the source are also copied (recursively)
   * @return the copied object
   */
  public static BaseOBObject copy(BaseOBObject source, boolean copyChildren) {
    return copy(source, copyChildren, true);
  }

  /**
   * Copies one object. The copyChildren parameter controls if the children of the object are also
   * copied. The resetId parameter controls if the id of the copied object (and its children) is set
   * to null.
   * 
   * @param source
   *          the object to be copied
   * @param copyChildren
   *          this parameter controls if the children of the source are also copied (recursively)
   * @param resetId
   *          if true then the id's of the copied objects will be nullified
   * @return the copied object
   */
  public static BaseOBObject copy(BaseOBObject source, boolean copyChildren, boolean resetId) {
    final Map<BaseOBObject, BaseOBObject> fromTo = new HashMap<BaseOBObject, BaseOBObject>();
    copy(source, copyChildren, resetId, fromTo);
    repairReferences(fromTo);
    return fromTo.get(source);
  }

  /**
   * Convenience method usefull for when implementing a custom copy process.
   * 
   * Walks through all copied objects and if there are references to objects which have been copied
   * then the references are moved to the copied object
   * 
   * See issue https://issues.openbravo.com/view.php?id=8651 which is related to this method.
   * 
   * @param fromTo
   *          map with copy from - copy to relation
   */
  public static void repairReferences(Map<BaseOBObject, BaseOBObject> fromTo) {

    for (BaseOBObject to : fromTo.values()) {
      for (final Property p : to.getEntity().getProperties()) {
        if (p.isPrimitive()) {
          continue;
        }
        final Object value = to.getValue(p.getName());

        if (p.isOneToMany()) {
          @SuppressWarnings("unchecked")
          final List<BaseOBObject> bobs = (List<BaseOBObject>) value;
          for (int i = 0; i < bobs.size(); i++) {
            final BaseOBObject curValue = bobs.get(i);
            if (fromTo.containsKey(curValue)) {
              bobs.set(i, fromTo.get(curValue));
            }
          }
        } else if (fromTo.containsKey(value)) {
          to.setValue(p.getName(), fromTo.get(value));
        }
      }
    }

  }

  /**
   * Copies a single object, including its children (if the relevant parameter is set to true), and
   * resets the id in the copied object (if the relevant parameter is set to true). The source and
   * its copy are added to the fromTo map.
   * 
   * @param source
   *          the object to copy
   * @param copyChildren
   *          if true then all the child objects (which have this object as parent) are copied
   *          recursively
   * @param resetId
   *          if true then the id of the copied object is set to null
   * @param fromTo
   *          the map which maintains the relation between the to-copy and the copied object
   * @return the copied object
   * @see #copy(BaseOBObject, boolean, boolean)
   * @see #repairReferences(Map)
   */
  public static BaseOBObject copy(BaseOBObject source, boolean copyChildren, boolean resetId,
      Map<BaseOBObject, BaseOBObject> fromTo) {

    final BaseOBObject target = (BaseOBObject) OBProvider.getInstance().get(source.getEntityName());
    fromTo.put(source, target);
    for (final Property p : source.getEntity().getProperties()) {
      final Object value = source.getValue(p.getName());
      if (p.isOneToMany()) {
        if (copyChildren && !p.getTargetEntity().isView()) {
          if (p.isChild()) {
            final List<BaseOBObject> targetChildren = new ArrayList<BaseOBObject>();
            target.setValue(p.getName(), targetChildren);
            @SuppressWarnings("unchecked")
            final List<BaseOBObject> sourceChildren = (List<BaseOBObject>) value;
            for (final BaseOBObject sourceChild : sourceChildren) {
              targetChildren.add(copy(sourceChild, copyChildren, resetId, fromTo));
            }
          } else {
            final List<BaseOBObject> targetReferedObjects = new ArrayList<BaseOBObject>();
            target.setValue(p.getName(), targetReferedObjects);
            @SuppressWarnings("unchecked")
            final List<BaseOBObject> sourceReferedObjects = (List<BaseOBObject>) value;
            for (final BaseOBObject sourceReferedObject : sourceReferedObjects) {
              targetReferedObjects.add(sourceReferedObject);
            }
          }
        }
      } else {
        target.setValue(p.getName(), value);
      }
    }
    if (resetId) {
      target.setId(null);
    }
    return target;
  }

  /**
   * This method returns the id of the object without loading/resolving it in Hibernate (if
   * possible). If the object is a HibernateProxy then the id is retrieved from the proxy object
   * instead of calling the method on the 'real' object. This prevents loading of the object through
   * Hibernate.
   * 
   * @param o
   *          the object (either a {@link BaseOBObject BaseOBObject} or a Hibernate Proxy object
   * @return the id, most of the time a String
   */
  public static Serializable getId(Object o) {
    if (o instanceof HibernateProxy) {
      return ((HibernateProxy) o).getHibernateLazyInitializer().getIdentifier();
    }
    if (o instanceof BaseOBObject) {
      return (Serializable) ((BaseOBObject) o).getId();
    }
    throw new ArgumentException("Argument is not a BaseOBObject and not a HibernateProxy "
        + (o != null ? o.getClass().getName() : "it is NULL"));
  }

  /**
   * Returns the entity name (see {@link Entity#getName()} without loading/resolving the object. If
   * the object is a Hibernate proxy then the entity name is retrieved through the proxy and not
   * directly from the object. This prevent unwanted loading of the object.
   * 
   * @param o
   *          the object to get the entity name for, can be a Hibernate proxy or a
   *          {@link BaseOBObject}
   * @return the entity name
   */
  // returns the static member containing the entityname
  // handles hibernate proxies
  // TODO: create a cache!
  // TODO: this can be done nicer with an annotation but then
  // jdk1.5 is a prerequisite
  public static String getEntityName(Object o) {
    if (o instanceof HibernateProxy)
      return getEntityName(((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass());
    return getEntityName(o.getClass());
  }

  // Note: in case the class is retrieved from a object before calling this
  // method
  // then use the above method getEntityName(Object o).
  public static String getEntityName(Class<?> clz) {
    try {
      final Field fld = clz.getField("ENTITY_NAME");
      return (String) fld.get(null);
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * Is used when the value of the foreign-key column should be retrieved. In java the value of a
   * reference is an entity. This method is usefull to get to the actual database foreign key value
   * (often the primary key of the referenced object). This method has a special implementation to
   * prevent unwanted loading of Hibernate Proxies. This loading is not required if only the id of
   * the referenced object needs to be returned.
   * 
   * @param referencingProperty
   *          the property which models the association, it is a property of the refering object
   *          (the owner)
   * @param referedObject
   *          the entity to which is being refered
   * @return the id of the referedObject (most of the time) or if the foreign key is to another
   *         property then the value of that property is returned
   */
  public static Serializable getReferencedPropertyValue(Property referencingProperty,
      Object referedObject) {
    Check.isTrue(referencingProperty.getReferencedProperty() != null, "This property "
        + referencingProperty + " does not have a referenced Property");
    final Property referencedProperty = referencingProperty.getReferencedProperty();
    if (referencedProperty.isId()) {
      if (referedObject instanceof HibernateProxy)
        return ((HibernateProxy) referedObject).getHibernateLazyInitializer().getIdentifier();
      if (referedObject instanceof BaseOBObject)
        return (Serializable) ((BaseOBObject) referedObject).getId();
    } else if (referedObject instanceof BaseOBObject) {
      return (Serializable) ((BaseOBObject) referedObject).get(referencedProperty.getName());
    }

    throw new ArgumentException("Argument is not a BaseOBObject and not a HibernateProxy");
  }
}