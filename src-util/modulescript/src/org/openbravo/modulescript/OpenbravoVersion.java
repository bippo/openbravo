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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.modulescript;

/**
 * This class represents an Openbravo ERP version number. It is designed to compare Openbravo ERP
 * version numbers
 * 
 * @author adrian
 */
public class OpenbravoVersion implements Comparable<OpenbravoVersion> {

  private int major1;
  private int major2;
  private int minor;

  /**
   * Creates a new Openbravo version object based on its three version numbers.
   * 
   * @param major1
   *          the major1 version number
   * @param major2
   *          the major2 version number
   * @param minor
   *          the minor version number
   */
  public OpenbravoVersion(int major1, int major2, int minor) {
    this.major1 = major1;
    this.major2 = major2;
    this.minor = minor;
  }

  /**
   * Creates a new Openbravo version object based on its <code>String</code> representation.
   * 
   * @param version
   *          the version representation
   */
  public OpenbravoVersion(String version) {

    String[] numbers = version.split("\\.");

    if (numbers.length != 3) {
      throw new IllegalArgumentException("Version must consist in three numbers separated by .");
    }
    this.major1 = Integer.valueOf(numbers[0]);
    this.major2 = Integer.valueOf(numbers[1]);
    this.minor = Integer.valueOf(numbers[2]);
  }

  /**
   * Gets the Major1 version number
   * 
   * @return The Major1 version number
   */
  public int getMajor1() {
    return major1;
  }

  /**
   * Returns the Major1 version number
   * 
   * @return The Major2 version number
   */
  public int getMajor2() {
    return major2;
  }

  /**
   * Returns the Minor version number
   * 
   * @return The Minor version number
   */
  public int getMinor() {
    return minor;
  }

  public int compareTo(OpenbravoVersion o) {
    if (major1 == o.major1) {
      if (major2 == o.major2) {
        return (minor < o.minor ? -1 : (minor == o.minor ? 0 : 1));
      } else {
        return major2 < o.major2 ? -1 : 1;
      }
    } else {
      return major1 < o.major1 ? -1 : 1;
    }
  }

  @Override
  public String toString() {
    return Integer.toString(major1) + "." + Integer.toString(major2) + "."
        + Integer.toString(minor);
  }
}
