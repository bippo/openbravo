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

package org.openbravo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
  public static int compareVersion(String v1, String v2) {
    if (v1.equals(v2))
      return 0;
    final String[] version1 = v1.split("\\.");
    final String[] version2 = v2.split("\\.");
    final int minorVers = version1.length > version2.length ? version2.length : version1.length;
    for (int i = 0; i < minorVers; i++) {
      if (version1[i].equals(version2[i]))
        continue;
      try {
        return new Integer(version1[i]).compareTo(new Integer(version2[i]));
      } catch (NumberFormatException e) {
        // Not possible to compare
        return -1;
      }
    }
    return 0;
  }

  public static String getVersion(String str) {
    String version = "";
    if (str == null)
      return "";
    final Pattern pattern = Pattern.compile("((\\d+\\.)+)\\d+");
    final Matcher matcher = pattern.matcher(str);
    if (matcher.find()) {
      version = matcher.group();
    }
    return version;
  }
}
