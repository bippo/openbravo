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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.javascript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

/**
 * Parses a JavaScript file and gets a tree representation of it. Used by
 * {@link JavaScriptAPIChecker}
 * 
 * @author iperdomo
 */
public class JavaScriptParser {

  private File jsFile = null;
  private ScriptOrFnNode nodeTree = null;
  private StringBuffer details = null;

  /**
   * Class constructor
   */
  public JavaScriptParser() {
  }

  /**
   * Sets the file to parse
   * 
   * @param f
   *          a File object of the JavaScript to parse
   */
  public void setFile(File f) {
    jsFile = f;
    nodeTree = null;
    details = null;
  }

  /**
   * @return a tree representation of the parsed JavaScript file
   * @throws IOException
   */
  public ScriptOrFnNode parse() throws IOException {

    if (nodeTree == null) {
      Reader reader = new FileReader(jsFile);

      CompilerEnvirons compilerEnv = new CompilerEnvirons();
      ErrorReporter errorReporter = compilerEnv.getErrorReporter();

      Parser parser = new Parser(compilerEnv, errorReporter);

      String sourceURI;

      try {
        sourceURI = jsFile.getCanonicalPath();
      } catch (IOException e) {
        sourceURI = jsFile.toString();
      }

      nodeTree = parser.parse(reader, sourceURI, 1);
    }
    return nodeTree;
  }

  /**
   * @return a string with the global variables and function definitions
   */
  public String getStringDetails() {
    if (jsFile == null) {
      throw new RuntimeException("You need to specify the file to parse");
    }
    if (details == null) {
      details = new StringBuffer();
      try {
        parse();
      } catch (IOException e) {
        e.printStackTrace();
      }
      for (Node cursor = nodeTree.getFirstChild(); cursor != null; cursor = cursor.getNext()) {
        if (cursor.getType() == Token.FUNCTION) {
          int fnIndex = cursor.getExistingIntProp(Node.FUNCTION_PROP);
          FunctionNode fn = nodeTree.getFunctionNode(fnIndex);
          details.append("FUNCTION: " + fn.getFunctionName() + "\n");
        } else if (cursor.getType() == Token.VAR) {
          Node vn = cursor.getFirstChild();
          details.append("VAR: " + vn.getString() + "\n");
        }
      }
    }
    return details.toString();
  }

  /**
   * Write the details of the js file into a file
   * 
   * @param file
   *          the file to write to
   */
  public void toFile(File file) throws IOException {
    if (jsFile == null) {
      throw new RuntimeException("You need to specify the file to parse");
    }
    BufferedWriter out = new BufferedWriter(new FileWriter(file));
    out.write(getStringDetails());
    out.close();
  }
}
