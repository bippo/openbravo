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
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class CreateFromMultiple extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      vars.getGlobalVariable("inpmInoutId", "CreateFromMultiple|mInoutId");
      vars.getGlobalVariable("inpwindowId", "CreateFromMultiple|windowId", "");
      vars.getGlobalVariable("inpTabId", "CreateFromMultiple|adTabId", "");
      vars.getGlobalVariable("inpcBpartnerId", "CreateFromMultiple|bpartner", "");
      vars.getGlobalVariable("inpmWarehouseId", "CreateFromMultiple|mWarehouseId", "");
      vars.setSessionValue("CreateFromMultiple|adProcessId", "800062");

      printPage_FS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strWindowId = vars.getGlobalVariable("inpWindowId", "CreateFromMultiple|windowId");
      String strSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strKey = vars.getGlobalVariable("inpmInoutId", "CreateFromMultiple|mInoutId");
      String strTabId = vars.getGlobalVariable("inpTabId", "CreateFromMultiple|adTabId");
      String strProcessId = vars.getGlobalVariable("inpadProcessId",
          "CreateFromMultiple|adProcessId");
      String strBpartner = vars.getGlobalVariable("inpcBpartnerId", "CreateFromMultiple|bpartner",
          "");
      String strmWarehouseId = vars.getGlobalVariable("inpmWarehouseId",
          "CreateFromMultiple|mWarehouseId", "");
      vars.removeSessionValue("CreateFromMultiple|mInoutId");
      vars.removeSessionValue("CreateFromMultiple|windowId");
      vars.removeSessionValue("CreateFromMultiple|adTabId");
      vars.removeSessionValue("CreateFromMultiple|adProcessId");
      vars.removeSessionValue("CreateFromMultiple|bpartner");

      callPrintPage(response, vars, strKey, strWindowId, strSOTrx, strTabId, strProcessId,
          strBpartner, strmWarehouseId);
    } else if (vars.commandIn("FIND")) {
      String strKey = vars.getRequiredStringParameter("inpmInoutId");
      String strWindowId = vars.getStringParameter("inpWindowId");
      String strSOTrx = vars.getStringParameter("inpissotrx");
      String strTabId = vars.getStringParameter("inpTabId");
      String strBpartner = vars.getRequestGlobalVariable("inpcBpartnerId",
          "CreateFromMultiple|bpartner");
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId",
          "CreateFromMultiple|mWarehouseId");
      callPrintPage(response, vars, strKey, strWindowId, strSOTrx, strTabId, "", strBpartner,
          strmWarehouseId);
    } else if (vars.commandIn("SAVE")) {
      String strKey = vars.getRequiredStringParameter("inpmInoutId");
      String strWindowId = vars.getStringParameter("inpWindowId");
      String strSOTrx = vars.getStringParameter("inpissotrx");
      String strTabId = vars.getStringParameter("inpTabId");
      OBError myMessage = saveMethod(vars, strKey, strWindowId, strSOTrx);

      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      vars.setMessage(strTabId, myMessage);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private void printPage_FS(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: FrameSet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFromMultiple_FS").createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void callPrintPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String strWindowId, String strSOTrx, String strTabId, String strProcessId,
      String strBpartner, String strmWarehouseId) throws IOException, ServletException {
    if (strSOTrx.equals("Y")) { // Shipment
      printPageShipment(response, vars, strKey, strWindowId, strTabId, strSOTrx, strProcessId,
          strBpartner, strmWarehouseId);
    } else { // Receipt
      printPageReceipt(response, vars, strKey, strWindowId, strTabId, strSOTrx, strProcessId,
          strBpartner, strmWarehouseId);
    }
  }

  protected void printPageReceipt(HttpServletResponse response, VariablesSecureApp vars,
      String strKey, String strWindowId, String strTabId, String strSOTrx, String strProcessId,
      String strBpartner, String strmWarehouseId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Receipt");
    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    String[] discard = { "" };
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFromMultiple_Receipt", discard)
        .createXmlDocument();

    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("help", strHelp);
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("sotrx", strSOTrx);
    xmlDocument.setParameter("bpartner", strBpartner);
    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);

    try {
      ComboTableData comboTableData = new ComboTableData(this, "TABLEDIR", "C_UOM_ID", "", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId), Utility.getContext(
              this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
      xmlDocument.setData("reportC_UOM_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(this, "TABLEDIR", "M_Warehouse_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
          Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strmWarehouseId);
      xmlDocument.setData("reportM_WAREHOUSE_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    CreateFromMultipleReceiptData[] dataW = CreateFromMultipleReceiptData
        .selectAccessibleWarehouses(this, vars.getRole(), vars.getClient());
    if (strmWarehouseId.equals("") && dataW != null && dataW.length > 0)
      strmWarehouseId = dataW[0].id;
    xmlDocument.setData("reportM_LOCATOR_X", "liststructure",
        CreateFromMultipleReceiptData.selectM_Locator_X(this, strmWarehouseId));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  protected void printPageShipment(HttpServletResponse response, VariablesSecureApp vars,
      String strKey, String strWindowId, String strTabId, String strSOTrx, String strProcessId,
      String strBpartner, String strmWarehouseId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Shipment");
    String[] discard = { "" };
    String strProduct = vars.getStringParameter("inpmProductId");
    // String strWarehouse = vars.getStringParameter("inpmWarehouseId");
    String strX = vars.getStringParameter("inpx");
    String strY = vars.getStringParameter("inpy");
    String strZ = vars.getStringParameter("inpz");
    CreateFromMultipleShipmentData[] data = null;
    if (strProduct.equals("") && strmWarehouseId.equals("") && strX.equals("") && strY.equals("")
        && strZ.equals("")) {
      discard[0] = new String("sectionDetail");
      data = new CreateFromMultipleShipmentData[0];
    } else {
      data = CreateFromMultipleShipmentData.select(this, vars.getLanguage(), strBpartner,
          strProduct, strmWarehouseId, strX, strY, strZ,
          Utility.getContext(this, vars, "#User_Client", strWindowId));
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFromMultiple_Shipment", discard)
        .createXmlDocument();

    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("sotrx", strSOTrx);
    xmlDocument.setParameter("mWarehouseId", strmWarehouseId);//
    xmlDocument.setParameter("bpartnerId", strBpartner);
    xmlDocument.setParameter("bpartnerId_DES",
        CreateFromMultipleShipmentData.bpartnerDescription(this, strBpartner));
    xmlDocument.setParameter("productId", strProduct);
    xmlDocument.setParameter("productId_DES",
        CreateFromMultipleShipmentData.productDescription(this, strProduct));
    xmlDocument.setParameter("x", strX);
    xmlDocument.setParameter("y", strY);
    xmlDocument.setParameter("z", strZ);

    try {
      ComboTableData comboTableData = new ComboTableData(this, "TABLEDIR", "M_Warehouse_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
          Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strmWarehouseId);
      xmlDocument.setData("reportM_WAREHOUSE_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }

  OBError saveMethod(VariablesSecureApp vars, String strKey, String strWindowId, String strSOTrx)
      throws IOException, ServletException {
    if (strSOTrx.equals("Y"))
      return saveShipment(vars, strKey, strWindowId);
    else
      return saveReceipt(vars, strKey, strWindowId);
  }

  protected OBError saveReceipt(VariablesSecureApp vars, String strKey, String strWindowId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Receipt");
    String strProduct = vars.getRequiredStringParameter("inpmProductId");
    String strAtributo = vars.getStringParameter("inpmAttributesetinstanceId");
    String strQty = vars.getNumericParameter("inpmovementqty");
    String strUOM = vars.getStringParameter("inpcUomId");
    String strQuantityOrder = vars.getNumericParameter("inpquantityorder");
    String strProductUOM = vars.getStringParameter("inpmProductUomId");
    String strWarehouse = vars.getRequiredStringParameter("inpmWarehouseId");
    String strLocator = vars.getStringParameter("inpmLocatorX");
    String strNumero = vars.getRequiredNumericParameter("inpnumerolineas");

    OBError myMessage = null;
    int count = 0;

    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      int total = Integer.valueOf(strNumero).intValue();
      CreateFromMultipleReceiptData[] locators = CreateFromMultipleReceiptData.select(conn, this,
          vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId),
          Utility.getContext(this, vars, "#User_Org", strWindowId), strWarehouse, strLocator);
      if (locators != null && locators.length > 0) {
        for (count = 0; count < total; count++) {
          String strM_Locator_ID = (count > locators.length - 1) ? "" : locators[count].mLocatorId;
          if (strM_Locator_ID.equals(""))
            break;
          String strSequence = SequenceIdData.getUUID();
          try {
            CreateFromMultipleReceiptData.insert(conn, this, strSequence, vars.getClient(),
                vars.getOrg(), vars.getUser(), strKey, strM_Locator_ID, strProduct, strUOM, strQty,
                strAtributo, strQuantityOrder, strProductUOM);
          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
      }

      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()) + " - "
          + Utility.messageBD(this, "Created", vars.getLanguage()) + ": " + count);
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  protected OBError saveShipment(VariablesSecureApp vars, String strKey, String strWindowId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Shipment");
    String strStorageDetail = vars.getInStringParameter("inpmStorageDetailId", IsIDFilter.instance);
    if (strStorageDetail.equals(""))
      return null;
    OBError myMessage = null;
    Connection conn = null;
    int count = 0;
    try {
      conn = this.getTransactionConnection();
      if (strStorageDetail.startsWith("("))
        strStorageDetail = strStorageDetail.substring(1, strStorageDetail.length() - 1);
      if (!strStorageDetail.equals("")) {
        strStorageDetail = Replace.replace(strStorageDetail, "'", "");
        StringTokenizer st = new StringTokenizer(strStorageDetail, ",", false);
        count = 0;
        while (st.hasMoreTokens()) {
          String strStorageDetailId = st.nextToken().trim();
          String strQty = vars.getNumericParameter("inpmovementqty" + strStorageDetailId);
          String strQtyOrder = vars.getNumericParameter("inpquantityorder" + strStorageDetailId);

          String strSequence = SequenceIdData.getUUID();
          try {
            CreateFromMultipleShipmentData.insert(conn, this, strSequence, vars.getClient(),
                vars.getOrg(), vars.getUser(), strKey, strQty, strQtyOrder, strStorageDetailId);
          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
          count++;
        }
      }
      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()) + " - "
          + Utility.messageBD(this, "Created", vars.getLanguage()) + ": " + count);
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  public String getServletInfo() {
    return "Servlet that presents the button of Create From Multiple";
  } // end of getServletInfo() method
}
