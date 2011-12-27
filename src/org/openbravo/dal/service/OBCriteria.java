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

package org.openbravo.dal.service;

import static org.openbravo.model.ad.system.Client.PROPERTY_ORGANIZATION;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_CLIENT;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.openbravo.base.model.Entity;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.model.common.enterprise.Organization;

/**
 * This object is an implementation of the Hibernate Criteria interface. It adds transparent client
 * and organization filtering to the Hibernate Criteria. Internally the OBCriteria keeps a Hibernate
 * Criteria object as a delegate. Most calls are delegated to the Hibernate Criteria object after
 * first setting the additional filters.
 * <p/>
 * This class also offers a convenience method to set orderby, the entities refered to from the
 * order by are automatically joined in the query.
 * 
 * @see OBContext#getReadableClients()
 * @see OBContext#getReadableOrganizations()
 * 
 * @author mtaal
 */

public class OBCriteria<E extends BaseOBObject> extends CriteriaImpl {
  private static final long serialVersionUID = 1L;

  private static final Logger log = Logger.getLogger(OBCriteria.class);

  private Entity entity;

  private boolean filterOnReadableClients = true;
  private boolean filterOnReadableOrganization = true;
  private boolean filterOnActive = true;
  private List<OrderBy> orderBys = new ArrayList<OrderBy>();

  // package visible

  public OBCriteria(String entityOrClassName) {
    super(entityOrClassName, (SessionImplementor) SessionHandler.getInstance().getSession());
  }

  public OBCriteria(String entityOrClassName, String alias) {
    super(entityOrClassName, alias, (SessionImplementor) SessionHandler.getInstance().getSession());
  }

  /**
   * See the list() method of the Hibernate Criteria class.
   * 
   * @return the list of Objects retrieved through this Criteria object
   */
  @SuppressWarnings("unchecked")
  public List<E> list() throws HibernateException {
    initialize();
    return super.list();
  }

  /**
   * A convenience method which is not present in the standard Hibernate Criteria object. The count
   * of objects is returned.
   * 
   * @return the count of the objects using the filter set in this Criteria
   */
  public int count() {
    initialize();
    setProjection(Projections.rowCount());
    log.debug("Counting using criteria " + toString());
    final int result = ((Number) uniqueResult()).intValue();
    setProjection(null);
    return result;
  }

  /**
   * See the scroll method on the Hibernate Criteria class.
   */
  public ScrollableResults scroll() throws HibernateException {
    initialize();
    return super.scroll();
  }

  /**
   * See the scroll method on the Hibernate Criteria class.
   */
  public ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException {
    initialize();
    return super.scroll(scrollMode);
  }

  /**
   * See the uniqueResult() method on the Hibernate Criteria class.
   */
  public Object uniqueResult() throws HibernateException {
    initialize();
    return super.uniqueResult();
  }

  void initialize() {
    final OBContext obContext = OBContext.getOBContext();
    final Entity e = getEntity();

    if (!OBContext.getOBContext().isInAdministratorMode()) {
      OBContext.getOBContext().getEntityAccessChecker().checkReadable(e);
    }

    if (isFilterOnReadableOrganization() && e.isOrganizationPartOfKey()) {
      add(Restrictions.in("id." + PROPERTY_ORGANIZATION + ".id",
          obContext.getReadableOrganizations()));

    } else if (isFilterOnReadableOrganization() && e.isOrganizationEnabled()) {
      add(Restrictions.in(PROPERTY_ORGANIZATION + ".id", obContext.getReadableOrganizations()));
    }

    if (isFilterOnReadableClients() && getEntity().isClientEnabled()) {
      add(Restrictions.in(PROPERTY_CLIENT + ".id", obContext.getReadableClients()));
    }

    if (isFilterOnActive() && e.isActiveEnabled()) {
      add(Restrictions.eq(Organization.PROPERTY_ACTIVE, true));
    }

    // add the order by and create a join if necessary
    for (final OrderBy ob : orderBys) {
      final int j = 0;
      String orderOn = ob.getOrderOn();
      if (orderOn.indexOf(".") != -1) {
        final String orderJoin = orderOn.substring(0, orderOn.lastIndexOf("."));
        final String alias = "order_ob_" + j;
        createCriteria(orderJoin, alias, CriteriaSpecification.LEFT_JOIN);
        orderOn = alias + "." + orderOn.substring(orderOn.lastIndexOf(".") + 1);
      }

      if (ob.isAscending()) {
        addOrder(Order.asc(orderOn));
      } else {
        addOrder(Order.desc(orderOn));
      }
    }
  }

  /**
   * Convenience method not present in the standard Hibernate Criteria object.
   * 
   * @param orderOn
   *          the property on which to order, can also be a property of an associated entity (etc.)
   * @param ascending
   *          if true then order ascending, false order descending
   */
  public void addOrderBy(String orderOn, boolean ascending) {
    orderBys.add(new OrderBy(orderOn, ascending));
  }

  /**
   * @return the Entity for which is queried
   * @see Entity
   */
  public Entity getEntity() {
    return entity;
  }

  void setEntity(Entity entity) {
    this.entity = entity;
  }

  /**
   * @return true then when querying (for example call list()) a filter on readable organizations is
   *         added to the query, if false then this is not done
   * @see OBContext#getReadableOrganizations()
   */
  public boolean isFilterOnReadableOrganization() {
    return filterOnReadableOrganization;
  }

  /**
   * Makes it possible to control if a filter on readable organizations should be added to the
   * Criteria automatically. The default is true.
   * 
   * @param filterOnReadableOrganization
   *          if true then when querying (for example call list()) a filter on readable
   *          organizations is added to the query, if false then this is not done
   * @see OBContext#getReadableOrganizations()
   */
  public void setFilterOnReadableOrganization(boolean filterOnReadableOrganization) {
    this.filterOnReadableOrganization = filterOnReadableOrganization;
  }

  /**
   * Filter the results on the active property. Default is true. If set then only objects with
   * isActive true are returned by the Criteria object.
   * 
   * @return true if objects are filtered on isActive='Y', false otherwise
   */
  public boolean isFilterOnActive() {
    return filterOnActive;
  }

  /**
   * Filter the results on the active property. Default is true. If set then only objects with
   * isActive true are returned by the Criteria object.
   * 
   * @param filterOnActive
   *          if true then only objects with isActive='Y' are returned, false otherwise
   */
  public void setFilterOnActive(boolean filterOnActive) {
    this.filterOnActive = filterOnActive;
  }

  /**
   * Filter the results on readable clients (@see OBContext#getReadableClients()). The default is
   * true.
   * 
   * @return if true then only objects from readable clients are returned, if false then objects
   *         from all clients are returned
   */
  public boolean isFilterOnReadableClients() {
    return filterOnReadableClients;
  }

  /**
   * Filter the results on readable clients (@see OBContext#getReadableClients()). The default is
   * true.
   * 
   * @param filterOnReadableClients
   *          if true then only objects from readable clients are returned, if false then objects
   *          from all clients are returned
   */
  public void setFilterOnReadableClients(boolean filterOnReadableClients) {
    this.filterOnReadableClients = filterOnReadableClients;
  }

  // OrderBy to support multiple orderby clauses
  static class OrderBy {
    private final String orderOn;
    private final boolean ascending;

    public OrderBy(String orderOn, boolean ascending) {
      this.orderOn = orderOn;
      this.ascending = ascending;
    }

    public String getOrderOn() {
      return orderOn;
    }

    public boolean isAscending() {
      return ascending;
    }

    @Override
    public String toString() {
      return getOrderOn() + (isAscending() ? " asc " : " desc ");
    }
  }
}
