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

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public class DirFilter implements FilenameFilter {
  String afn;
  List<String> files;

  public DirFilter(String afn) {
    this.afn = afn;
  }

  public DirFilter(List<String> files) {
    this.files = files;
  }

  public boolean accept(File dir, String name) {
    if (files != null) {
      return (files.contains(name)) || new File(dir, name).isDirectory();
    } else {
      boolean boolReturn;
      // the name is obtained only to compare it with the filename and not
      // with all the names in all the path
      String f = new File(name).getName();
      // returns true if the filter agrees or if it is a directory
      boolReturn = f.indexOf(afn, f.length() - afn.length()) != -1
          || new File(dir, name).isDirectory();
      return boolReturn;
    }
  }
}
