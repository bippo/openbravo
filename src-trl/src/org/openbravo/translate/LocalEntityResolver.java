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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  _Ville Lindfors_____________________________________.
 ************************************************************************
 */
package org.openbravo.translate;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Local entity resolver for known entities.
 * 
 * This provides currently DTD only for Jasper Reports. To add other DTDs add code to identify them
 * based on public id or system id and write code to get the resource from local directory.
 * 
 */
public class LocalEntityResolver implements EntityResolver {

  public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
      IOException {

    if (systemId.equals("http://jasperreports.sourceforge.net/dtds/jasperreport.dtd")) {
      // return a special input source using the system classloader.
      return new InputSource(
          System.class.getResourceAsStream("/net/sf/jasperreports/engine/dtds/jasperreport.dtd"));
    } else {
      // Use default behavior.
      return null;
    }
  }

}
