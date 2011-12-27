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

import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.EntityNameResolver;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.proxy.map.MapProxyFactory;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.entity.AbstractEntityTuplizer;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.DynamicOBObject;

/**
 * The tuplizer for {@link DynamicOBObject} objects. This is class used by Hibernate. It sets the
 * object instantiator (the factory) used by Hibernate.
 * 
 * @see OBInstantiator
 * @author mtaal
 */
public class OBDynamicTuplizer extends AbstractEntityTuplizer {
  private static final Logger log = Logger.getLogger(OBDynamicTuplizer.class);

  private final PersistentClass persistentClass;

  public OBDynamicTuplizer(EntityMetamodel entityMetamodel, PersistentClass mappedEntity) {
    super(entityMetamodel, mappedEntity);
    log.debug("Created tuplizer for "
        + (mappedEntity.getMappedClass() != null ? mappedEntity.getMappedClass().getName()
            : mappedEntity.getEntityName()));
    persistentClass = mappedEntity;
  }

  // this is done in the generated mapping
  // @Override
  // protected Getter buildPropertyGetter(Property mappedProperty,
  // PersistentClass mappedEntity) {
  // return new OBDynamicPropertyHandler.Getter(mappedProperty.getName());
  // }
  //
  // @Override
  // protected Setter buildPropertySetter(Property mappedProperty,
  // PersistentClass mappedEntity) {
  // return new OBDynamicPropertyHandler.Setter(mappedProperty.getName());
  // }

  @Override
  public String determineConcreteSubclassEntityName(Object entityInstance,
      SessionFactoryImplementor factory) {
    BaseOBObject bob = (BaseOBObject) entityInstance;
    return bob.getEntity().getName();
  }

  @Override
  public EntityNameResolver[] getEntityNameResolvers() {
    return null;
  }

  @Override
  protected Instantiator buildInstantiator(PersistentClass mappingInfo) {
    return new OBInstantiator(mappingInfo);
  }

  @Override
  protected ProxyFactory buildProxyFactory(PersistentClass thePersistentClass, Getter idGetter,
      Setter idSetter) {
    ProxyFactory pf = new MapProxyFactory();
    try {
      pf.postInstantiate(getEntityName(), null, null, null, null, null);
    } catch (final HibernateException he) {
      log.warn("could not create proxy factory for:" + getEntityName(), he);
      pf = null;
    }
    return pf;
  }

  @SuppressWarnings("rawtypes")
  public Class getMappedClass() {
    return persistentClass.getMappedClass();
  }

  @SuppressWarnings("rawtypes")
  public Class getConcreteProxyClass() {
    return persistentClass.getMappedClass();
  }

  @Override
  public EntityMode getEntityMode() {
    return EntityMode.POJO;
  }

  @Override
  protected Getter buildPropertyGetter(Property mappedProperty, PersistentClass mappedEntity) {
    return mappedProperty.getGetter(mappedEntity.getMappedClass());
  }

  @Override
  protected Setter buildPropertySetter(Property mappedProperty, PersistentClass mappedEntity) {
    return mappedProperty.getSetter(mappedEntity.getMappedClass());
  }

  public boolean isInstrumented() {
    return false;
  }

}