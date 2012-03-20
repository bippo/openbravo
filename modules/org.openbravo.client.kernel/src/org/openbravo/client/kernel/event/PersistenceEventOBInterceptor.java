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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.event;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.Traceable;

/**
 * The interceptor which listens to persistence events and passes them on to observers.
 * 
 * @author mtaal
 */
@ApplicationScoped
public class PersistenceEventOBInterceptor extends EmptyInterceptor {

  private static final long serialVersionUID = 1L;

  @Inject
  private Event<EntityNewEvent> entityNewEventProducer;

  @Inject
  private Event<EntityUpdateEvent> entityUpdateEventProducer;

  @Inject
  private Event<EntityDeleteEvent> entityDeleteEventProducer;

  @Inject
  private Event<TransactionBeginEvent> transactionBeginEventProducer;

  @Inject
  private Event<TransactionCompletedEvent> transactionCompletedEventProducer;

  public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames,
      Type[] types) {
    final EntityDeleteEvent entityEvent = new EntityDeleteEvent();
    entityEvent.setTargetInstance((BaseOBObject) entity);
    entityEvent.setPropertyNames(propertyNames);
    entityEvent.setCurrentState(state);
    entityEvent.setTypes(types);
    entityEvent.setId((String) id);
    entityDeleteEventProducer.fire(entityEvent);
  }

  @Override
  public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
      Object[] previousState, String[] propertyNames, Type[] types) {
    if (isNew(entity)) {
      return sendNewEvent(entity, id, currentState, propertyNames, types);
    } else {
      return sendUpdateEvent(entity, id, currentState, previousState, propertyNames, types);
    }
  }

  @Override
  public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
      Type[] types) {
    return sendNewEvent(entity, id, state, propertyNames, types);
  }

  private boolean sendNewEvent(Object entity, Serializable id, Object[] state,
      String[] propertyNames, Type[] types) {
    final EntityNewEvent entityEvent = new EntityNewEvent();
    entityEvent.setTargetInstance((BaseOBObject) entity);
    entityEvent.setPropertyNames(propertyNames);
    entityEvent.setCurrentState(state);
    entityEvent.setTypes(types);
    entityEvent.setId((String) id);
    entityNewEventProducer.fire(entityEvent);
    return entityEvent.isStateUpdated();
  }

  private boolean sendUpdateEvent(Object entity, Serializable id, Object[] currentState,
      Object[] previousState, String[] propertyNames, Type[] types) {
    final EntityUpdateEvent entityEvent = new EntityUpdateEvent();
    entityEvent.setTargetInstance((BaseOBObject) entity);
    entityEvent.setPropertyNames(propertyNames);
    entityEvent.setCurrentState(currentState);
    entityEvent.setPreviousState(previousState);
    entityEvent.setTypes(types);
    entityEvent.setId((String) id);
    entityUpdateEventProducer.fire(entityEvent);
    return entityEvent.isStateUpdated();
  }

  private boolean isNew(Object entity) {
    final Traceable t = (Traceable) entity;
    return t.getCreatedBy() == null;
  }

  @Override
  public void afterTransactionBegin(Transaction tx) {
    final TransactionBeginEvent event = new TransactionBeginEvent();
    event.setTransaction(tx);
    transactionBeginEventProducer.fire(event);
  }

  @Override
  public void afterTransactionCompletion(Transaction tx) {
    final TransactionCompletedEvent event = new TransactionCompletedEvent();
    event.setTransaction(tx);
    transactionCompletedEventProducer.fire(event);
  }

}
