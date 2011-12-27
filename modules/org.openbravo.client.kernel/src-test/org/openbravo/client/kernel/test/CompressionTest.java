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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.kernel.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openbravo.client.kernel.JSCompressor;
import org.openbravo.test.base.BaseTest;

/**
 * Test the compression of a static js file.
 * 
 * @author mtaal
 */

public class CompressionTest extends BaseTest {

  public void testCompression() throws Exception {
    final JSCompressor compressor = new JSCompressor();
    final InputStream is = this.getClass().getResourceAsStream("test-compression.js");
    String line;
    final StringBuilder sb = new StringBuilder();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    while ((line = reader.readLine()) != null) {
      sb.append(line).append("\n");
    }
    final String compressed = compressor.compress(sb.toString());
    assertNotNull(compressed);
    assertTrue(sb.length() > (2 * compressed.length()));
    System.err.println(sb.length());
    System.err.println(compressed.length());
    is.close();
  }
}