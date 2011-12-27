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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class FormatRead extends DefaultHandler {
  Hashtable<String, FormatCouple> hasFormats;

  static Logger log4jFormatRead = Logger.getLogger(FormatRead.class);

  public FormatRead(Hashtable<String, FormatCouple> hasFormats) {
    this.hasFormats = hasFormats;
  }

  public void startElement(java.lang.String uri, java.lang.String name, java.lang.String qName,
      Attributes amap) {
    if (log4jFormatRead.isDebugEnabled())
      log4jFormatRead.debug("FormatRead: startElement is called:" + name);

    if (name.equals("Number")) {
      String formatName = null;
      char decimal = '.';
      char grouping = ',';
      String formatOutput = null;
      String formatInternal = null;

      for (int i = 0; i < amap.getLength(); i++) {
        if (log4jFormatRead.isDebugEnabled())
          log4jFormatRead.debug("  FormatRead (attribute list): attribute name=" + amap.getQName(i)
              + " value=" + amap.getValue(i));
        if (amap.getQName(i).equals("name")) {
          formatName = amap.getValue(i);
        } else if (amap.getQName(i).equals("decimal")) {
          decimal = amap.getValue(i).charAt(0);
        } else if (amap.getQName(i).equals("grouping")) {
          grouping = amap.getValue(i).charAt(0);
        } else if (amap.getQName(i).equals("formatOutput")) {
          formatOutput = amap.getValue(i);
        } else if (amap.getQName(i).equals("formatInternal")) {
          formatInternal = amap.getValue(i);
        }
      }
      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      dfs.setDecimalSeparator(decimal);
      dfs.setGroupingSeparator(grouping);
      DecimalFormatSymbols dfsEsp = new DecimalFormatSymbols();
      dfsEsp.setDecimalSeparator('.');
      dfsEsp.setGroupingSeparator(',');
      FormatCouple fc;
      fc = new FormatCouple(new DecimalFormat(formatOutput, dfs), new DecimalFormat(formatInternal,
          dfsEsp));
      hasFormats.put(formatName, fc);
    } // number
  }
}
