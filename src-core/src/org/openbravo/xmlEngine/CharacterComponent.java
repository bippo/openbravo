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

import org.xml.sax.Attributes;

class CharacterComponent implements XmlComponentTemplate, XmlComponentValue {
  protected String character;

  public CharacterComponent() {
  }

  public CharacterComponent(String character) {
    this.character = character;
  }

  public CharacterComponent(String name, Attributes amap) {
    character = "<" + name;
    for (int i = 0; i < amap.getLength(); i++) {
      String attname = amap.getQName(i);
      String value = amap.getValue(i);
      character = character + " " + attname + "=\"" + value + "\"";
    }
    character = character + ">";
  }

  public String print() {
    return character;
  }

  public String printPrevious() {
    return character;
  }

  public String printSimple() {
    return character;
  }

  public String printPreviousSimple() {
    return character;
  }

  public XmlComponentValue createXmlComponentValue(XmlDocument xmlDocument) {
    return this; // this class is constant, therefore the XmlComponentValue
    // is it.
  }

}
