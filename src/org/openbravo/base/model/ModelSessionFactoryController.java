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

package org.openbravo.base.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.Type;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.util.Check;

/**
 * Initializes and provides the session factory for the model layer. It uses fixed mappings for
 * Table, Column etc..
 * 
 * @author mtaal
 */

public class ModelSessionFactoryController extends SessionFactoryController {

  private List<Class<?>> additionalClasses = new ArrayList<Class<?>>();

  @Override
  protected void mapModel(Configuration cfg) {
    cfg.addClass(Table.class);
    cfg.addClass(Package.class);
    cfg.addClass(Column.class);
    cfg.addClass(Reference.class);
    cfg.addClass(RefSearch.class);
    cfg.addClass(RefTable.class);
    cfg.addClass(RefList.class);
    cfg.addClass(Module.class);
    for (Class<?> clz : additionalClasses) {
      cfg.addClass(clz);
    }
  }

  @Override
  protected void setInterceptor(Configuration configuration) {
    configuration.setInterceptor(new LocalInterceptor());
  }

  // an interceptor which fails on all updates
  private class LocalInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames,
        Type[] types) {
      return false;
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames,
        Type[] types) {
      Check.fail("The model session factory is not allowed to " + "remove model data.");
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
        Object[] previousState, String[] propertyNames, Type[] types) {
      for (int i = 0; i < currentState.length; i++) {
        final Object current = currentState[i];
        final Object previous = previousState[i];
        boolean changed = false;
        if (current instanceof Boolean || previous instanceof Boolean) {
          changed = getBoolValue(current) != getBoolValue(previous);
        } else if (current != null && previous == null) {
          changed = true;
        } else if (current == null && previous != null) {
          changed = true;
        } else if (current != null && previous != null && !current.equals(previous)) {
          changed = true;
        }
        if (changed) {
          Check.fail("Model session is not allowed to update info. " + " The instance "
              + entity.getClass().getName() + " with id " + id + " was changed on property "
              + propertyNames[i] + ", previous value " + previousState[i] + " new value "
              + currentState[i]);
        }
      }
      Check.fail("Model session is not allowed to update info. " + " The instance "
          + entity.getClass().getName() + " with id " + id + " was changed");
      return false;
    }

    private boolean getBoolValue(Object value) {
      if (value == null) {
        return false;
      }
      return ((Boolean) value).booleanValue();
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
        Type[] types) {
      Check.fail("The model session factory is not allowed to " + "create model data.");
      return false;
    }

    @Override
    public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
      Check.fail("The model session factory is not allowed to " + "update model data.");
    }

    @Override
    public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
      Check.fail("The model session factory is not allowed to " + "update model data.");
    }

    @Override
    public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
      Check.fail("The model session factory is not allowed to " + "update model data.");
    }
  }

  public List<Class<?>> getAdditionalClasses() {
    return additionalClasses;
  }

  public void addAdditionalClasses(Class<?> clz) {
    additionalClasses.add(clz);
  }
}
