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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.system;

import java.sql.Connection;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.test.base.BaseTest;

/**
 * Test the ErrorTextParser class logic.
 * 
 * @author huehner
 */

public class ErrorTextParserTest extends BaseTest {

  public void testDuplicatePrimaryKey() throws Exception {
    doErrorTextParserTest(1);
  }

  public void testNotNull() throws Exception {
    // test disabled on pgsql, as its error message text in not-null case cannot be parsed so far
    if (getConnectionProvider().getRDBMS().equals("ORACLE")) {
      doErrorTextParserTest(2);
    }
  }

  public void testBoolean() throws Exception {
    doErrorTextParserTest(3);
  }

  public void testUniqueSingleField() throws Exception {
    doErrorTextParserTest(4);
  }

  public void testUniqueMultipleFields() throws Exception {
    doErrorTextParserTest(5);
  }

  public void testFKInsert() throws Exception {
    doErrorTextParserTest(6);
  }

  public void testFKDelete() throws Exception {
    doErrorTextParserTest(7);
  }

  public void testNonBooleanCheck() throws Exception {
    doErrorTextParserTest(8);
  }

  public void testPGSpanish() throws Exception {
    // only test on pgsql, as specifically testing against es_ES/pgsql error messsage
    if (getConnectionProvider().getRDBMS().equals("POSTGRE")) {
      doErrorTextParserTestWithoutDB(1);
    }
  }

  private void doErrorTextParserTest(int testCase) throws Exception {
    ConnectionProvider conn = getConnectionProvider();
    VariablesSecureApp vars = new VariablesSecureApp("", "", "");
    Connection con = conn.getTransactionConnection();

    // set core to development to avoid the _mod_ trigger restrictions
    ErrorTextParserTestData.setCoreInDevelopment(con, conn);

    String errorMessage = "";
    String expectedErrorMessage = "";
    try {
      switch (testCase) {
      case 1:
        expectedErrorMessage = "Internal Error: Duplicate primary key/uuid. Your record has not been saved into the table User/Contact";
        ErrorTextParserTestData.insertUserPK(con, conn, "0", "N", "name");
        break;
      case 2:
        expectedErrorMessage = "The column Active is mandatory and cannot be left empty.";
        ErrorTextParserTestData.insertUser(con, conn, null, "name");
        break;
      case 3:
        expectedErrorMessage = "Only values 'Y'or 'N' may be entered into the field Active.";
        ErrorTextParserTestData.insertUser(con, conn, "B", "name");
        break;
      case 4:
        expectedErrorMessage = "There is already a Client with the same Name. Name must be unique. You must change the values entered.";
        ErrorTextParserTestData.insertClientWithName(con, conn, "System", "System");
        break;
      case 5:
        expectedErrorMessage = "There is already a Month Translation with the same (Language, Month). (Language, Month) must be unique. You must change the values entered.";
        ErrorTextParserTestData.insertMonthTrl(con, conn);
        break;
      case 6:
        expectedErrorMessage = "This record cannot be deleted because it is associated with other existing elements. Please see Linked Items";
        ErrorTextParserTestData.insertUserWithClient(con, conn, "42", "Y", "Openbravo");
        break;
      case 7:
        expectedErrorMessage = "This record cannot be deleted because it is associated with other existing elements. Please see Linked Items";
        ErrorTextParserTestData.deleteClient(con, conn, "0");
        break;
      case 8:
        expectedErrorMessage = "There is a constraint defined that was not satisfied. Please check the data entered";
        ErrorTextParserTestData.insertProcess(con, conn, "value", "name", "test");
        break;
      }

    } catch (ServletException se) {
      errorMessage = se.getMessage();
    } finally {
      conn.releaseRollbackConnection(con);
    }

    OBError trlError = Utility.translateError(conn, vars, "en_US", errorMessage);

    assertEquals(expectedErrorMessage, trlError.getMessage());
  }

  private void doErrorTextParserTestWithoutDB(int testCase) throws Exception {
    String errorMessage = "";
    String expectedMessage = "";
    ConnectionProvider conn = getConnectionProvider();
    VariablesSecureApp vars = new VariablesSecureApp("", "", "");

    switch (testCase) {
    case 1:
      expectedMessage = "This record cannot be deleted because it is associated with other existing elements. Please see Linked Items";
      errorMessage = "inserción o actualización en la tabla «c_bpartner» viola la llave foránea «c_bpartner_c_bp_group»";
      break;
    }

    OBError trlError = Utility.translateError(conn, vars, "en_US", errorMessage);
    assertEquals(expectedMessage, trlError.getMessage());

  }

}