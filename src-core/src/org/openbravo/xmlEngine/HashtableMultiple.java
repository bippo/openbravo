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

import java.util.Vector;

class HashtableMultiple { // class to simulate a Hashtable but with
  // various keys of the same value
  // get returns the first key find but you can iterate to find all.
  Vector<String> vecKeys;
  Vector<Object> vecObjects;

  public HashtableMultiple() {
    vecKeys = new Vector<String>();
    vecObjects = new Vector<Object>();
  }

  public void put(String id, Object ob) {
    vecKeys.add(id);
    vecObjects.add(ob);
  }

  public Object get(String id) {
    Object ob = null;
    int i = 0;
    for (String strKey : vecKeys) {
      if (strKey.equals(id)) {
        return vecObjects.elementAt(i);
      }
      i++;
    }
    return ob;
  }
}
