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
package org.openbravo.data;

class Parameter {
  boolean boolSequence;
  boolean boolRepeated = false;
  String strName;
  String strInOut;
  int type;
  String strDefault;
  String strValue;
  String strAfter;
  String strText;
  String strIgnoreValue;
  boolean boolOptional = false;

  public Parameter(boolean sequence, String strName, String strDefault, String strInOut,
      String strOptional, String strAfter, String strText, String strIgnoreValue) {
    boolSequence = sequence;
    this.strName = strName;
    this.strDefault = strDefault;
    this.strInOut = strInOut;
    if (strOptional != null)
      boolOptional = true;
    type = java.sql.Types.VARCHAR;
    this.strAfter = strAfter;
    this.strText = strText;
    this.strIgnoreValue = strIgnoreValue;
  }

}
