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

/*  Error management for XmlEngine
 To add a new error, in the location where the error is produced include:
 ErrorManagement.error(nn, location [, exception]);
 In this file include the block
 case nn:
 errorText = "Description of the error";
 // comments of the error
 break;
 */
import org.apache.log4j.Logger;

class ErrorManagement {

  static Logger log4jErrorManagement = Logger.getLogger(ErrorManagement.class);

  public static void error(int i, String locationText) {
    error(i, locationText, null);
  }

  public static void error(int i, String locationText, Exception e) {
    String errorText = "";
    switch (i) {
    case 101:
      errorText = "Data not defined for structure";
      // The setData has not been made in the structure of a XmlDocument
      // or a sql sentece was not specified in the configuration file
      break;
    case 102:
      errorText = "Not found fileXmlEngineConfiguration";
      // the .xml file does not exist
      break;
    case 103:
      errorText = "IOException in fileXmlEngineConfiguration";
      break;
    case 104:
      errorText = "Exception in parsing of fileXmlEngineConfiguration";
      // error when parsing the .xml file, for example badly nested tags
      // or badly closed
      break;
    case 105:
      errorText = "File of template not defined";
      // no template found in the xml file
      break;
    case 106:
      errorText = "Not found fileXmlEngineTemplate";
      // the template file does not exist (.html, .xml, ...)
      break;
    case 107:
      errorText = "IOException in fileXmlEngineTemplate";
      break;
    case 108:
      errorText = "Exception in parsing of fileXmlEngineTemplate";
      // error when parsing the .xml file, for example badly nested tags
      // or badly closed
      break;
    }
    log4jErrorManagement.error(errorText);
    log4jErrorManagement.error("in " + locationText);
    if (log4jErrorManagement.isDebugEnabled() && e != null) {
      e.getMessage();
      e.printStackTrace();
    }

  }

}
