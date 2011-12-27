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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.core;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.TypeHelper;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.jdbc.BorrowedConnectionProxy;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.database.SessionInfo;

/**
 * The DalSessionFactory directly delegates all calls to a real SessionFactory except for the calls
 * to open a session in that case an extra action is done to set session information in the database
 * (and then the call is forwarded to the 'real' SessionFactory).
 * 
 * @author mtaal
 * @see SessionFactoryController
 */
@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
public class DalSessionFactory implements SessionFactory {

  private static final long serialVersionUID = 1L;

  private SessionFactory delegateSessionFactory;

  /**
   * NOTE: Openbravo requires normal application code to use the DalSessionFactory and not the real
   * underlying Hibernate SessionFactory.
   * 
   * @return the underlying real sessionfactory
   */
  public SessionFactory getDelegateSessionFactory() {
    return delegateSessionFactory;
  }

  public void setDelegateSessionFactory(SessionFactory delegateSessionFactory) {
    this.delegateSessionFactory = delegateSessionFactory;
  }

  public void close() throws HibernateException {
    delegateSessionFactory.close();
  }

  public void evict(Class persistentClass, Serializable id) throws HibernateException {
    delegateSessionFactory.evict(persistentClass, id);
  }

  public void evict(Class persistentClass) throws HibernateException {
    delegateSessionFactory.evict(persistentClass);
  }

  public void evictCollection(String roleName, Serializable id) throws HibernateException {
    delegateSessionFactory.evictCollection(roleName, id);
  }

  public void evictCollection(String roleName) throws HibernateException {
    delegateSessionFactory.evictCollection(roleName);
  }

  public void evictEntity(String entityName, Serializable id) throws HibernateException {
    delegateSessionFactory.evictEntity(entityName, id);
  }

  public void evictEntity(String entityName) throws HibernateException {
    delegateSessionFactory.evictEntity(entityName);
  }

  public void evictQueries() throws HibernateException {
    delegateSessionFactory.evictQueries();
  }

  public void evictQueries(String cacheRegion) throws HibernateException {
    delegateSessionFactory.evictQueries(cacheRegion);
  }

  public Map getAllClassMetadata() throws HibernateException {
    return delegateSessionFactory.getAllClassMetadata();
  }

  public Map getAllCollectionMetadata() throws HibernateException {
    return delegateSessionFactory.getAllCollectionMetadata();
  }

  public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
    return delegateSessionFactory.getClassMetadata(persistentClass);
  }

  public ClassMetadata getClassMetadata(String entityName) throws HibernateException {
    return delegateSessionFactory.getClassMetadata(entityName);
  }

  public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
    return delegateSessionFactory.getCollectionMetadata(roleName);
  }

  public Session getCurrentSession() throws HibernateException {
    return delegateSessionFactory.getCurrentSession();
  }

  public Set getDefinedFilterNames() {
    return delegateSessionFactory.getDefinedFilterNames();
  }

  public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
    return delegateSessionFactory.getFilterDefinition(filterName);
  }

  public Reference getReference() throws NamingException {
    return delegateSessionFactory.getReference();
  }

  public Statistics getStatistics() {
    return delegateSessionFactory.getStatistics();
  }

  public boolean isClosed() {
    return delegateSessionFactory.isClosed();
  }

  /**
   * Note method sets user session information in the database and opens a connection for this.
   */
  public Session openSession() throws HibernateException {
    // NOTE: workaround for this issue:
    // http://opensource.atlassian.com/projects/hibernate/browse/HHH-3529
    final Session session = delegateSessionFactory.openSession();
    final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(BorrowedConnectionProxy.class.getClassLoader());
      final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
      Connection conn = ((SessionImplementor) session).connection();
      SessionInfo.initDB(conn, props.getProperty("bbdd.rdbms"));
      SessionInfo.setDBSessionInfo(conn);
      try {
        final String dbSessionConfig = props.getProperty("bbdd.sessionConfig");
        PreparedStatement pstmt = conn.prepareStatement(dbSessionConfig);
        pstmt.executeQuery();
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    } finally {
      Thread.currentThread().setContextClassLoader(currentLoader);
    }
    return session;
  }

  /**
   * Note method sets user session information in the database and opens a connection for this.
   */
  public Session openSession(Connection connection, Interceptor interceptor) {
    // NOTE: workaround for this issue:
    // http://opensource.atlassian.com/projects/hibernate/browse/HHH-3529
    final Session session = delegateSessionFactory.openSession(connection, interceptor);
    final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(BorrowedConnectionProxy.class.getClassLoader());
      Connection conn = ((SessionImplementor) session).connection();
      SessionInfo.initDB(conn, OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("bbdd.rdbms"));
      SessionInfo.setDBSessionInfo(conn);
    } finally {
      Thread.currentThread().setContextClassLoader(currentLoader);
    }
    return session;
  }

  /**
   * Note method sets user session information in the database and opens a connection for this.
   */
  public Session openSession(Connection connection) {
    // NOTE: workaround for this issue:
    // http://opensource.atlassian.com/projects/hibernate/browse/HHH-3529
    final Session session = delegateSessionFactory.openSession(connection);
    final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(BorrowedConnectionProxy.class.getClassLoader());
      Connection conn = ((SessionImplementor) session).connection();
      SessionInfo.initDB(conn, OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("bbdd.rdbms"));
      SessionInfo.setDBSessionInfo(conn);
    } finally {
      Thread.currentThread().setContextClassLoader(currentLoader);
    }
    return session;
  }

  /**
   * Note method sets user session information in the database and opens a connection for this.
   */
  public Session openSession(Interceptor interceptor) throws HibernateException {
    // NOTE: workaround for this issue:
    // http://opensource.atlassian.com/projects/hibernate/browse/HHH-3529
    final Session session = delegateSessionFactory.openSession(interceptor);
    final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(BorrowedConnectionProxy.class.getClassLoader());
      Connection conn = ((SessionImplementor) session).connection();
      SessionInfo.initDB(conn, OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("bbdd.rdbms"));
      SessionInfo.setDBSessionInfo(conn);
    } finally {
      Thread.currentThread().setContextClassLoader(currentLoader);
    }
    return session;
  }

  /**
   * Note method sets user session information in the database and opens a connection for this.
   */
  public StatelessSession openStatelessSession() {
    // NOTE: workaround for this issue:
    // http://opensource.atlassian.com/projects/hibernate/browse/HHH-3529
    final StatelessSession session = delegateSessionFactory.openStatelessSession();
    final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(BorrowedConnectionProxy.class.getClassLoader());
      Connection conn = ((SessionImplementor) session).connection();
      SessionInfo.initDB(conn, OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("bbdd.rdbms"));
      SessionInfo.setDBSessionInfo(conn);
    } finally {
      Thread.currentThread().setContextClassLoader(currentLoader);
    }
    return session;
  }

  /**
   * Note method sets user session information in the database and opens a connection for this.
   */
  public StatelessSession openStatelessSession(Connection connection) {
    // NOTE: workaround for this issue:
    // http://opensource.atlassian.com/projects/hibernate/browse/HHH-3529
    final StatelessSession session = delegateSessionFactory.openStatelessSession(connection);
    final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(BorrowedConnectionProxy.class.getClassLoader());
      Connection conn = ((SessionImplementor) session).connection();
      SessionInfo.initDB(conn, OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("bbdd.rdbms"));
      SessionInfo.setDBSessionInfo(conn);
    } finally {
      Thread.currentThread().setContextClassLoader(currentLoader);
    }
    return session;
  }

  public Cache getCache() {
    return delegateSessionFactory.getCache();
  }

  public boolean containsFetchProfileDefinition(String name) {
    return delegateSessionFactory.containsFetchProfileDefinition(name);
  }

  public TypeHelper getTypeHelper() {
    return delegateSessionFactory.getTypeHelper();
  }
}
