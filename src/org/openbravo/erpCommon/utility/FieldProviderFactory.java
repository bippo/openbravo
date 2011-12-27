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
package org.openbravo.erpCommon.utility;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;

/**
 * FieldProviderFactory is an utility class to obtain a FieldProvider object from any Object with
 * getter methods.
 * <p>
 * 
 * Its is used invoking the static getFieldProvider or getFieldProviderArray methods with an object
 * or array of objects as parameter. These objects must have getter methods, the
 * FieldProvider.getField method will call the getter with the same name.
 * 
 * Example:
 * 
 * MyClass obj = new MyClass(); FieldProvider fp = FieldProviderFactory.getFieldProvider(obj);
 * String name = fp.getField("name");
 * 
 * This example will call to obj.getName() method.
 * 
 */
public class FieldProviderFactory implements FieldProvider {

  private Object object;
  private HashMap<String, String> properties;
  private static Logger log4j = Logger.getLogger(FieldProviderFactory.class);

  /**
   * Initializes a new FieldProviderFactory for the object
   * 
   * @param obj
   */
  @SuppressWarnings("unchecked")
  public FieldProviderFactory(Object obj) {
    object = obj;
    if (obj instanceof HashMap) {
      properties = (HashMap<String, String>) obj;
    } else {
      properties = new HashMap<String, String>();
    }
  }

  /**
   * This is the implementation for the FieldProvider.getField(String s) method which will be
   * invoked in the object.
   * <p>
   * 
   * Note that for a fieldName it must exist in the object a getter called "getFieldName" "F" is
   * upper case though in the passed parameter can be lower case.
   */
  public String getField(String fieldName) {
    try {
      String rt = properties.get(fieldName);
      if (rt != null) {
        return rt;
      } else {
        String methodName = "get" + fieldName.substring(0, 1).toUpperCase()
            + fieldName.substring(1);
        Method method = object.getClass().getMethod(methodName, new Class[] {});
        return (String) method.invoke(object, new Object[] {});
      }
    } catch (Exception e) {
      log4j.debug("Not found field" + fieldName);
      return null;
    }
  }

  private void setProperty(String name, String value) {
    properties.put(name, value);
  }

  /**
   * Returns a FieldProvider for the getter methods of object
   * 
   * @param obj
   * @return the FieldProvider for the passed obj.
   */
  public static FieldProvider getFieldProvider(Object obj) {
    return new FieldProviderFactory(obj);
  }

  /**
   * Returns an array of FieldProvider objects for the getter methods of the objects
   * 
   * @param obj
   * @return an array of FieldProviders for each of the objects in the passed array
   */
  public static FieldProvider[] getFieldProviderArray(Object[] obj) {
    FieldProvider[] rt = new FieldProviderFactory[obj.length];
    for (int i = 0; i < obj.length; i++) {
      rt[i] = new FieldProviderFactory(obj[i]);
    }
    return rt;
  }

  public static <T extends Object> FieldProvider[] getFieldProviderArray(Collection<T> objs) {
    FieldProvider[] rt = new FieldProviderFactory[objs.size()];
    int i = 0;
    for (Object o : objs) {
      rt[i++] = new FieldProviderFactory(o);
    }
    return rt;
  }

  /**
   * Creates a setter for a field provider
   * 
   * @param fp
   * @param field
   * @param value
   */
  public static void setField(FieldProvider fp, String field, String value) {
    try {
      fp.getClass().getField(field).set(fp, value);
    } catch (NoSuchFieldException e) {
      if (fp instanceof FieldProviderFactory) {
        ((FieldProviderFactory) fp).setProperty(field, value);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setFieldArray(FieldProvider[] fps, String field, String value) {
    for (FieldProvider fp : fps) {
      setField(fp, field, value);
    }

  }

}
