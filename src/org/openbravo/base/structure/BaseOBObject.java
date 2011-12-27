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

package org.openbravo.base.structure;

import java.io.Serializable;
import java.util.List;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.BaseOBObjectDef;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.CheckException;
import org.openbravo.base.validation.ValidationException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.OBInterceptor;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Language;

/**
 * Base business object, the root of the inheritance tree for all business objects. The class model
 * here combines an inheritance structure with interface definitions. The inheritance structure is
 * used to enable some re-use of code. The interfaces are used to tag a certain implementation with
 * the functionality it provides. The outside world should use the interfaces to determine if an
 * object supports specific functionality.
 * 
 * @author mtaal
 */

public abstract class BaseOBObject implements BaseOBObjectDef, Identifiable, DynamicEnabled,
    OBNotSingleton, Serializable {
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger
      .getLogger(BaseOBObject.class);

  private static final long serialVersionUID = 1L;

  private Entity model = null;

  // is used to force an insert of this object. This is usefull if the id of
  // the
  // object should be preserved when it is imported
  private boolean newOBObject = false;

  // contains all the data, data is indexed by the index of the property
  // in the entity, property.getIndexInEntity()
  private Object[] data = null;;

  // computed once therefore an object type
  private Boolean isDerivedReadable;

  // if set to true then derived readable is not checked
  private boolean allowRead = false;

  // Translated data, it is initialized when trying to get translated value for a translatable
  // property
  private BaseOBObject dataTrl;

  // Takes true, when dataTrl has been tried to initialize (regardless it has any value) in this way
  // it is tried only once
  private boolean hasLookedForTrl = false;

  // is used to set default data in a constructor of the generated class
  // without a security check
  protected void setDefaultValue(String propName, Object value) {
    if (!getEntity().hasProperty(propName)) {
      // ignoring warning as this always happens when the database needs to be updated
      // or when uninstalling modules.
      // log.warn("Property " + propName + " does not exist for entity " + getEntityName()
      // + ". This is not necessarily a problem, this can happen when modules are uninstalled.");
      return;
    }
    try {
      getEntity().checkValidPropertyAndValue(propName, value);
      Check.isNotNull(value, "Null default values are not allowed");
      setDataValue(propName, value);
    } catch (ValidationException ve) {
      // do not fail here so that build tasks can still continue
      log.error(ve.getMessage(), ve);
    } catch (CheckException ce) {
      // do not fail here so that build tasks can still continue
      log.error(ce.getMessage(), ce);
    }
  }

  private Object getData(String propName) {
    return getDataValue(getEntity().getProperty(propName));
  }

  private Object getDataValue(Property p) {
    if (data == null) {
      // nothing set in this case anyway
      return null;
    }
    return data[p.getIndexInEntity()];
  }

  private Object getDataValue(Property p, Language language) {
    if (data == null) {
      // nothing set in this case anyway
      return null;
    }

    if (p.isTranslatable() && OBContext.hasTranslationInstalled()) {
      if (!hasLookedForTrl) {
        hasLookedForTrl = true;
        try {
          @SuppressWarnings("unchecked")
          List<BaseOBObject> trl = OBDal.getInstance().getSession()
              .createFilter(this.get(p.getTrlOneToManyProperty().getName()), "where language = ?")
              .setParameter(0, language).list();

          if (!trl.isEmpty()) {
            dataTrl = trl.get(0);
          }
        } catch (Throwable t) {
          // Log error but do not fail here, continue using base language
          log.debug("Error looking for translation of " + p + ". Using base value.", t);
        }
      }

      if (dataTrl != null) {
        return dataTrl.get(p.getTranslationProperty().getName());
      }
    }

    return data[p.getIndexInEntity()];
  }

  private void setDataValue(String propName, Object value) {
    if (data == null) {
      data = new Object[getEntity().getProperties().size()];
    }
    final Property p = getEntity().getProperty(propName);
    if (p.getIndexInEntity() >= data.length) {
      throw new IllegalArgumentException("Property index (" + p.getIndexInEntity()
          + ") is larger than or equal to property list size (" + data.length + "). "
          + "This happens when setting property " + propName + " " + p + " with value " + value
          + " in entity " + getEntity());
    }
    data[p.getIndexInEntity()] = value;
  }

  public Object getId() {
    return get("id");
  }

  public void setId(Object id) {
    set("id", id);
  }

  public abstract String getEntityName();

  public String getIdentifier() {
    return IdentifierProvider.getInstance().getIdentifier(this);
  }

  /**
   * Returns the value of the {@link Property Property} identified by the propName. This method does
   * security checking. If a security violation occurs then a OBSecurityException is thrown.
   * 
   * @see BaseOBObject#get(String, Language)
   * 
   * @param propName
   *          the name of the {@link Property Property} for which the value is requested
   * @throws OBSecurityException
   */
  public Object get(String propName) {
    return get(propName, null);
  }

  /**
   * Returns the value of the {@link Property Property} identified by the propName translating it,
   * if possible, to the language. This method does security checking. If a security violation
   * occurs then a OBSecurityException is thrown.
   * 
   * @see BaseOBObject#get(String)
   * 
   * @param propName
   *          the name of the {@link Property Property} for which the value is requested
   * @param language
   *          language to translate to
   * @return value of the property
   * @throws OBSecurityException
   *           in case property is not readable
   */
  public Object get(String propName, Language language) {
    final Property p = getEntity().getProperty(propName);
    checkDerivedReadable(p);
    if (language != null) {
      return getDataValue(p, language);
    } else {
      return getDataValue(p);
    }
  }

  /**
   * Set a value for the {@link Property Property} identified by the propName. This method checks
   * the correctness of the value and performs security checks.
   * 
   * @param propName
   *          the name of the {@link Property Property} being set
   * @param value
   *          the value being set
   * @throws OBSecurityException
   *           , ValidationException
   */
  public void set(String propName, Object value) {
    final Property p = getEntity().getProperty(propName);
    p.checkIsValidValue(value);
    checkDerivedReadable(p);
    p.checkIsWritable();
    setValue(propName, value);
  }

  protected void checkDerivedReadable(Property p) {
    final OBContext obContext = OBContext.getOBContext();
    // obContext can be null in the OBContext initialize method
    if (!isAllowRead() && obContext != null && obContext.isInitialized()
        && !obContext.isInAdministratorMode()) {
      if (isDerivedReadable == null) {
        // don't check derived readable for views
        isDerivedReadable = obContext.getEntityAccessChecker().isDerivedReadable(getEntity());
      }

      if (isDerivedReadable && !p.allowDerivedRead()) {
        throw new OBSecurityException(
            "Entity "
                + getEntity()
                + " is not directly readable, only id and identifier properties are readable, property "
                + p + " is neither of these.");
      }
    }
  }

  /**
   * Sets a value in the object without any security or validation checking. Should be used with
   * care. Is used by the subclasses and system classes.
   * 
   * @param propName
   *          the name of the {@link Property Property} being set
   * @param value
   */
  public void setValue(String propName, Object value) {
    setDataValue(propName, value);
  }

  /**
   * Returns the value of {@link Property Property} identified by the propName. This method does not
   * do security checking.
   * 
   * @param propName
   *          the name of the property for which the value is requested.
   * @return the value
   */
  public Object getValue(String propName) {
    return getData(propName);
  }

  /**
   * Return the entity of this object. The {@link Entity Entity} again gives access to the
   * {@link Property Properties} of this object.
   * 
   * @return the Entity of this object
   */
  public Entity getEntity() {
    if (model == null) {
      model = ModelProvider.getInstance().getEntity(getEntityName());
    }
    return model;
  }

  /**
   * Validates the content of this object using the property validators.
   * 
   * @throws ValidationException
   */
  public void validate() {
    getEntity().validate(this);
  }

  @Override
  public String toString() {
    final Entity e = getEntity();
    final StringBuilder sb = new StringBuilder();
    // and also display all values
    for (final Property p : e.getIdentifierProperties()) {
      Object value = get(p.getName());
      if (value != null) {
        if (sb.length() == 0) {
          sb.append("(");
        } else {
          sb.append(", ");
        }
        if (value instanceof BaseOBObject) {
          value = ((BaseOBObject) value).getId();
        }
        sb.append(p.getName() + ": " + value);
      }
    }
    if (sb.length() > 0) {
      sb.append(")");
    }
    return getEntityName() + "(" + getId() + ") " + sb.toString();
  }

  /**
   * Returns true if the id is null or the object is set to new explicitly. After flushing the
   * object to the database then new object is set to false.
   * 
   * @return false if the id is set and this is not a new object, true otherwise.
   * 
   * @see OBInterceptor#postFlush(java.util.Iterator)
   */
  public boolean isNewOBObject() {
    return getId() == null || newOBObject;
  }

  public void setNewOBObject(boolean newOBObject) {
    this.newOBObject = newOBObject;
  }

  public boolean isAllowRead() {
    return allowRead;
  }

  /**
   * Sets if the object maybe read also by non-authorized users. Can only be called in admin mode
   * (see {@link OBContext#setAdminMode()}.
   * 
   * @param allowRead
   */
  public void setAllowRead(boolean allowRead) {
    if (!OBContext.getOBContext().isInAdministratorMode()) {
      throw new OBSecurityException("setAllowRead may only be called in admin mode");
    }
    this.allowRead = allowRead;
  }
}
