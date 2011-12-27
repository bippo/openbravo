/*
 ************************************************************************************
 * Copyright (C) 2001-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.xmlEngine;

/**
 * A class with the elements of the estack of the elements of the XML template.
 **/

class StackElement {
  String strName;
  boolean isSection;
  boolean skipCharacters;
  SectionTemplate previousSection;
  boolean printEnabled = true;

  public StackElement(String strName) {
    this.strName = strName;
    this.isSection = false;
    this.skipCharacters = false;
  }

  public void setSection(SectionTemplate previousSection) {
    isSection = true;
    this.previousSection = previousSection;
  }

  public boolean isSection() {
    return isSection;
  }

  public String name() {
    return strName;
  }

  public SectionTemplate section() {
    return previousSection;
  }

  public void setSkipCharacters() {
    this.skipCharacters = true;
  }

  public boolean skipCharacters() {
    return skipCharacters;
  }

  public void setPrintEnabled(boolean printEnabled) {
    this.printEnabled = printEnabled;
  }

  public boolean printEnabled() {
    return printEnabled;
  }

}
