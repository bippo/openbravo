package org.openbravo.erpCommon.utility;

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

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;

class SetPriorityCategoryData implements FieldProvider {

  static Logger log4j = Logger.getLogger(SetPriorityCategoryData.class);
  public String category;
  public String priority;
  public String rownum;

  public String getField(String fieldName) {
    if (fieldName.equals("category")) {
      return ((category == null) ? "" : category);
    } else if (fieldName.equals("priority")) {
      return ((priority == null) ? "" : priority);
    } else if (fieldName.equals("rownum")) {
      return ((rownum == null) ? "" : rownum);
    } else {
      if (log4j.isDebugEnabled())
        log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static SetPriorityCategoryData[] getCategories() {

    Vector<SetPriorityCategoryData> vector = new Vector<SetPriorityCategoryData>(0);
    for (Enumeration<?> e = LogManager.getCurrentLoggers(); e.hasMoreElements();) {
      Logger categoryItem = (Logger) e.nextElement();
      SetPriorityCategoryData setPriorityCategoryData = new SetPriorityCategoryData();
      setPriorityCategoryData.category = categoryItem.getName();
      if (categoryItem.getLevel() != null) {
        setPriorityCategoryData.priority = categoryItem.getLevel().toString();
      }
      if (vector.isEmpty()) {
        vector.addElement(setPriorityCategoryData);
      } else {
        int index = 0;
        while (index < vector.size()) {
          SetPriorityCategoryData cd = vector.get(index);
          if (setPriorityCategoryData.category.compareTo(cd.category) < 0) {
            vector.add(index, setPriorityCategoryData);
            break;
          }
          index++;
        }
        if (index == vector.size())
          vector.addElement(setPriorityCategoryData);
      }
    }
    SetPriorityCategoryData categoryData[] = new SetPriorityCategoryData[vector.size()];
    vector.copyInto(categoryData);
    for (int i = 0; i < categoryData.length; i++) {
      categoryData[i].rownum = "" + (i + 1);
    }
    return (categoryData);
  }

}
