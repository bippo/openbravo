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

package org.openbravo.base.util;

/**
 * Collection of static utility methods for checking variable state and arguments.
 * 
 * @author mtaal
 */
public class Check {

  /**
   * Always throws a CheckException.
   * 
   * @param message
   *          the message used for the exception
   * @throws CheckException
   */
  public static void fail(String message) {
    throw new CheckException(message);
  }

  /**
   * Throws a CheckException if the value is false, the message is used for creating the Exception.
   * 
   * @param value
   *          should be true otherwise an Exception is thrown
   * @param message
   *          the message used for the Exception
   * @throws CheckException
   */
  public static void isTrue(boolean value, String message) {
    if (!value) {
      throw new CheckException(message);
    }
  }

  /**
   * Throws a CheckException if the value is true, the message is used for creating the Exception.
   * 
   * @param value
   *          should be false otherwise an Exception is thrown
   * @param message
   *          the message used for the Exception
   * @throws CheckException
   */
  public static void isFalse(boolean value, String message) {
    if (value) {
      throw new CheckException(message);
    }
  }

  public static void isNotNull(Object value, String message) {
    if (value == null) {
      throw new CheckException(message);
    }
  }

  /**
   * Throws a CheckException if the value is not null, the message is used for creating the
   * Exception.
   * 
   * @param value
   *          should be null otherwise an Exception is thrown
   * @param message
   *          the message used for the Exception
   * @throws CheckException
   */
  public static void isNull(Object value, String message) {
    if (value != null) {
      throw new CheckException(message);
    }
  }

  /**
   * Throws a CheckException if the value is null or has length 0 (after trimming), the message is
   * used for creating the Exception.
   * 
   * @param value
   *          should be unequal to null and have length > zero otherwise an Exception is thrown
   * @param message
   *          the message used for the Exception
   * @throws CheckException
   */
  public static void notEmpty(String value, String message) {
    if (value == null || value.trim().length() == 0) {
      throw new CheckException(message);
    }
  }

  /**
   * Throws a CheckException if the value is null or has length 0 (after trimming), the message is
   * used for creating the Exception.
   * 
   * @param array
   *          should be unequal to null and have length > zero otherwise an Exception is thrown
   * @param message
   *          the message used for the Exception
   * @throws CheckException
   */
  public static void notEmpty(Object[] array, String message) {
    if (array == null || array.length == 0) {
      throw new CheckException(message);
    }
  }

  /**
   * Checks if the passed object is of the class specified, null values are ignored.
   * 
   * @param obj
   *          should be instanceof the expClass
   * @param expClass
   *          the class used for the check
   * @throws CheckException
   */
  public static void isInstanceOf(Object obj, Class<?> expClass) {
    if (obj == null) {
      return;
    }
    if (!(expClass.isAssignableFrom(obj.getClass()))) {
      throw new CheckException("Expected class: " + expClass.getName() + " but object has class: "
          + obj.getClass().getName());
    }
  }

  /**
   * Checks memory equality, the two objects should be the exact same object.
   * 
   * @param obj1
   *          first object checked
   * @param obj2
   *          second object checked
   * @throws CheckException
   */
  public static void isSameObject(Object obj1, Object obj2) {
    if (obj1 != obj2) {
      throw new CheckException("Objects are not the same");
    }
  }

  /**
   * Checks memory equality, the two objects should not be the exact same object.
   * 
   * @param obj1
   *          first object checked
   * @param obj2
   *          second object checked
   * @throws CheckException
   */
  public static void isNotSameObject(Object obj1, Object obj2) {
    if (obj1 == obj2) {
      throw new CheckException("Objects are not the same");
    }
  }

  private Check() {
  }
}
