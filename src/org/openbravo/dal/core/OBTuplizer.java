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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Subclass;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.PojoEntityTuplizer;
import org.hibernate.type.CompositeType;
import org.hibernate.util.ReflectHelper;
import org.openbravo.base.util.Check;

/**
 * The tuplizer for OBObjects objects. This is class used by Hibernate. It sets the object
 * instantiator (the factory) used by Hibernate.
 * 
 * @see OBInstantiator
 * @author mtaal
 */
public class OBTuplizer extends PojoEntityTuplizer {
  private static final Logger log = Logger.getLogger(OBTuplizer.class);

  private final PersistentClass persistentClass;

  public OBTuplizer(EntityMetamodel entityMetamodel, PersistentClass mappedEntity) {
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
  protected Instantiator buildInstantiator(PersistentClass mappingInfo) {
    return new OBInstantiator(mappingInfo);
  }

  @Override
  protected ProxyFactory buildProxyFactory(PersistentClass thePersistentClass, Getter idGetter,
      Setter idSetter) {
    final Class<?> mappedClass = thePersistentClass.getMappedClass();
    Check.isNotNull(mappedClass, "Mapped class of entity " + thePersistentClass.getEntityName()
        + " is null");

    // determine the id getter and setter methods from the proxy interface
    // (if
    // any)
    // determine all interfaces needed by the resulting proxy
    final HashSet<Object> proxyInterfaces = new HashSet<Object>();
    proxyInterfaces.add(HibernateProxy.class);

    final Class<?> proxyInterface = thePersistentClass.getProxyInterface();

    if (proxyInterface != null && !mappedClass.equals(proxyInterface)) {
      if (!proxyInterface.isInterface()) {
        throw new MappingException("proxy must be either an interface, or the class itself: "
            + getEntityName());
      }
      proxyInterfaces.add(proxyInterface);
    }

    if (mappedClass.isInterface()) {
      proxyInterfaces.add(mappedClass);
    }

    final Iterator<?> iter = thePersistentClass.getSubclassIterator();
    while (iter.hasNext()) {
      final Subclass subclass = (Subclass) iter.next();
      final Class<?> subclassProxy = subclass.getProxyInterface();
      final Class<?> subclassClass = subclass.getMappedClass();
      if (subclassProxy != null && !subclassClass.equals(subclassProxy)) {
        if (proxyInterface == null || !proxyInterface.isInterface()) {
          throw new MappingException("proxy must be either an interface, or the class itself: "
              + subclass.getEntityName());
        }
        proxyInterfaces.add(subclassProxy);
      }
    }

    final Method idGetterMethod = idGetter == null ? null : idGetter.getMethod();
    final Method idSetterMethod = idSetter == null ? null : idSetter.getMethod();

    final Method proxyGetIdentifierMethod = idGetterMethod == null || proxyInterface == null ? null
        : ReflectHelper.getMethod(proxyInterface, idGetterMethod);
    final Method proxySetIdentifierMethod = idSetterMethod == null || proxyInterface == null ? null
        : ReflectHelper.getMethod(proxyInterface, idSetterMethod);

    ProxyFactory pf = buildProxyFactoryInternal(thePersistentClass, idGetter, idSetter);
    try {
      pf.postInstantiate(getEntityName(), mappedClass, proxyInterfaces, proxyGetIdentifierMethod,
          proxySetIdentifierMethod,
          thePersistentClass.hasEmbeddedIdentifier() ? (CompositeType) thePersistentClass
              .getIdentifier().getType() : null);
    } catch (final HibernateException he) {
      log.warn("could not create proxy factory for:" + getEntityName(), he);
      pf = null;
    }
    return pf;
  }

  @Override
  public Class<?> getMappedClass() {
    return persistentClass.getMappedClass();
  }

  @Override
  public Class<?> getConcreteProxyClass() {
    return persistentClass.getMappedClass();
  }
}