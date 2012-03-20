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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import net.matthaynes.jsmin.JSMin;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;

/**
 * Compresses a JavaScript source using JSMin <br>
 * http://code.google.com/p/jsmin-ant-task/
 * 
 * @author mtaal
 * @author iperdomo
 */
public class JSCompressor {
  private static final Logger log = Logger.getLogger(JSCompressor.class);

  private static JSCompressor instance;

  public static synchronized JSCompressor getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(JSCompressor.class);
    }
    return instance;
  }

  public static synchronized void setInstance(JSCompressor instance) {
    JSCompressor.instance = instance;
  }

  public String compress(String content) {

    try {
      final ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes("UTF-8"));
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final JSMin jsMin = new JSMin(bais, baos);
      jsMin.jsmin();
      return baos.toString("UTF-8");
    } catch (Throwable t) {
      log.error(t.getMessage(), t);
      return content;
    }
  }
}
