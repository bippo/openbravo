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
package org.openbravo.utils;

public class Replace {

  public static String replace(String strInicial, String strReplaceWhat, String strReplaceWith) {
    int index = 0;
    int pos;
    if (strInicial == null || strInicial.equals(""))
      return strInicial;
    else if (strReplaceWhat == null || strReplaceWhat.equals(""))
      return strInicial;
    else if (strReplaceWith == null)
      strReplaceWith = "";
    StringBuffer strFinal = new StringBuffer("");
    do {
      pos = strInicial.indexOf(strReplaceWhat, index);
      if (pos != -1) {
        strFinal.append(strInicial.substring(index, pos) + strReplaceWith);
        index = pos + strReplaceWhat.length();
      } else {
        strFinal.append(strInicial.substring(index));
      }
    } while (index < strInicial.length() && pos != -1);
    return strFinal.toString();
  }

  public static String delChars(String str, char[] delChars) {
    int length = str.length();
    int charLength = delChars.length;
    StringBuilder result = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      char current = str.charAt(i);
      boolean del = false;
      for (int j = 0; j < charLength; j++) {
        if (current == delChars[j]) {
          del = true;
          break;
        }
      }
      if (!del) {
        result.append(current);
      }
    }
    return result.toString();
  }

}// End of class
