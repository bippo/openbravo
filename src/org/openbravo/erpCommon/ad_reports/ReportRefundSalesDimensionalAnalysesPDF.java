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

package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.IsPositiveIntFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportRefundSalesDimensionalAnalysesPDF extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportRefundSalesDimensionalAnalyses|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportRefundSalesDimensionalAnalyses|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "ReportRefundSalesDimensionalAnalyses|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "ReportRefundSalesDimensionalAnalyses|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup",
          "ReportRefundSalesDimensionalAnalyses|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportRefundSalesDimensionalAnalyses|partner", IsIDFilter.instance);
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory",
          "ReportRefundSalesDimensionalAnalyses|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportRefundSalesDimensionalAnalyses|product", IsIDFilter.instance);
      // hardcoded to numeric in switch in the code
      String strNotShown = vars.getInStringParameter("inpNotShown", IsPositiveIntFilter.instance);
      String strShown = vars.getInStringParameter("inpShown", IsPositiveIntFilter.instance);
      String strOrg = vars.getRequestGlobalVariable("inpOrg",
          "ReportRefundSalesDimensionalAnalyses|org");
      String strsalesrepId = vars.getRequestGlobalVariable("inpSalesrepId",
          "ReportRefundSalesDimensionalAnalyses|salesrep");
      String strmWarehouseId = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportRefundSalesDimensionalAnalyses|warehouseId");
      String strOrder = vars.getRequestGlobalVariable("inpOrder",
          "ReportRefundSalesDimensionalAnalyses|order");
      String strMayor = vars.getStringParameter("inpMayor", "");
      String strMenor = vars.getStringParameter("inpMenor", "");
      String strRatioMayor = vars.getStringParameter("inpRatioMayor", "");
      String strRatioMenor = vars.getStringParameter("inpRatioMenor", "");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "ReportRefundSalesDimensionalAnalyses|currency", strUserCurrencyId);
      printPagePdf(request, response, vars, strComparative, strDateFrom, strDateTo,
          strPartnerGroup, strcBpartnerId, strProductCategory, strmProductId, strNotShown,
          strShown, strDateFromRef, strDateToRef, strOrg, strsalesrepId, strmWarehouseId, strOrder,
          strMayor, strMenor, strRatioMayor, strRatioMenor, strCurrencyId);
    } else
      pageErrorPopUp(response);
  }

  private void printPagePdf(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strComparative, String strDateFrom, String strDateTo,
      String strPartnerGroup, String strcBpartnerId, String strProductCategory,
      String strmProductId, String strNotShown, String strShown, String strDateFromRef,
      String strDateToRef, String strOrg, String strsalesrepId, String strmWarehouseId,
      String strOrder, String strMayor, String strMenor, String strRatioMayor,
      String strRatioMenor, String strCurrencyId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print pdf");
    XmlDocument xmlDocument = null;
    String strOrderby = "";
    if (log4j.isDebugEnabled())
      log4j.debug("********************************" + strComparative);
    String[] discard = { "", "", "", "", "", "", "" };
    String[] discard1 = { "selEliminarBody1", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard" };
    if (strOrg.equals(""))
      strOrg = vars.getOrg();
    if (strComparative.equals("Y"))
      discard1[0] = "selEliminarBody2";
    String strTitle = "";
    strTitle = Utility.messageBD(this, "From", vars.getLanguage()) + " " + strDateFrom + " "
        + Utility.messageBD(this, "To", vars.getLanguage()) + " " + strDateTo;
    if (!strPartnerGroup.equals(""))
      strTitle = strTitle + ", " + Utility.messageBD(this, "ForBPartnerGroup", vars.getLanguage())
          + " " + ReportRefundSalesDimensionalAnalysesData.selectBpgroup(this, strPartnerGroup);
    if (!strProductCategory.equals(""))
      strTitle = strTitle
          + ", "
          + Utility.messageBD(this, "ProductCategory", vars.getLanguage())
          + " "
          + ReportRefundSalesDimensionalAnalysesData
              .selectProductCategory(this, strProductCategory);
    if (!strsalesrepId.equals(""))
      strTitle = strTitle + " " + Utility.messageBD(this, "And", vars.getLanguage()) + " "
          + Utility.messageBD(this, "TheSalesRep", vars.getLanguage()) + " "
          + ReportRefundSalesDimensionalAnalysesData.selectSalesrep(this, strsalesrepId);
    if (!strmWarehouseId.equals(""))
      strTitle = strTitle + " " + Utility.messageBD(this, "And", vars.getLanguage()) + " "
          + Utility.messageBD(this, "TheWarehouse", vars.getLanguage()) + " "
          + ReportRefundSalesDimensionalAnalysesData.selectMwarehouse(this, strmWarehouseId);

    ReportRefundSalesDimensionalAnalysesData[] data = null;
    String[] strShownArray = { "", "", "", "", "", "", "" };
    if (strShown.startsWith("("))
      strShown = strShown.substring(1, strShown.length() - 1);
    if (!strShown.equals("")) {
      strShown = Replace.replace(strShown, "'", "");
      strShown = Replace.replace(strShown, " ", "");
      StringTokenizer st = new StringTokenizer(strShown, ",", false);
      int intContador = 0;
      while (st.hasMoreTokens()) {
        strShownArray[intContador] = st.nextToken();
        intContador++;
      }

    }
    String[] strTextShow = { "", "", "", "", "", "", "" };
    int intDiscard = 0;
    int intAuxDiscard = -1;
    for (int i = 0; i < 7; i++) {
      if (strShownArray[i].equals("1")) {
        strTextShow[i] = "C_BP_GROUP.NAME";
        discard[i] = "10";
        intDiscard++;
      } else if (strShownArray[i].equals("2")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER('C_Bpartner', TO_CHAR(C_BPARTNER.C_BPARTNER_ID), '"
            + vars.getLanguage() + "')";
        discard[i] = "10";
        intDiscard++;
      } else if (strShownArray[i].equals("3")) {
        strTextShow[i] = "M_PRODUCT_CATEGORY.NAME";
        discard[i] = "10";
        intDiscard++;
      } else if (strShownArray[i].equals("4")) {
        strTextShow[i] = "AD_COLUMN_IDENTIFIER('M_Product', TO_CHAR(M_PRODUCT.M_PRODUCT_ID), '"
            + vars.getLanguage() + "')";
        discard[i] = "10";
        intAuxDiscard = i;
        intDiscard++;
      } else if (strShownArray[i].equals("5")) {
        strTextShow[i] = "C_ORDER.DOCUMENTNO";
        discard[i] = "10";
        intDiscard++;
      } else if (strShownArray[i].equals("6")) {
        strTextShow[i] = "AD_USER.FIRSTNAME||' '||' '||AD_USER.LASTNAME";
        discard[i] = "10";
        intDiscard++;
      } else if (strShownArray[i].equals("7")) {
        strTextShow[i] = "M_WAREHOUSE.NAME";
        discard[i] = "10";
        intDiscard++;
      } else {
        strTextShow[i] = "''";
        discard[i] = "0.1";
      }
    }
    if (intDiscard != 0 || intAuxDiscard != -1) {
      int k = 1;
      if (intDiscard == 1) {
        strOrderby = " ORDER BY NIVEL" + k + ",";
      } else {
        strOrderby = " ORDER BY ";
      }
      while (k < intDiscard) {
        strOrderby = strOrderby + "NIVEL" + k + ",";
        k++;
      }
      if (k == 1) {
        if (strOrder.equals("Normal")) {
          strOrderby = " ORDER BY NIVEL" + k;
        } else if (strOrder.equals("Amountasc")) {
          strOrderby = " ORDER BY LINENETAMT ASC";
        } else if (strOrder.equals("Amountdesc")) {
          strOrderby = " ORDER BY LINENETAMT DESC";
        } else if (strOrder.equals("Ratioasc")) {
          strOrderby = " ORDER BY RATIO ASC";
        } else if (strOrder.equals("Ratiodesc")) {
          strOrderby = " ORDER BY RATIO DESC";
        } else {
          strOrderby = "1";
        }
      } else {
        if (strOrder.equals("Normal")) {
          strOrderby += "NIVEL" + k;
        } else if (strOrder.equals("Amountasc")) {
          strOrderby += "LINENETAMT ASC";
        } else if (strOrder.equals("Amountdesc")) {
          strOrderby += "LINENETAMT DESC";
        } else if (strOrder.equals("Ratioasc")) {
          strOrderby += "RATIO ASC";
        } else if (strOrder.equals("Ratiodesc")) {
          strOrderby += "RATIO DESC";
        } else {
          strOrderby = "1";
        }
      }

    } else {
      strOrderby = " ORDER BY 1";
    }
    String strHaving = "";
    if (!strMayor.equals("") && !strMenor.equals("")) {
      strHaving = " HAVING SUM(LINENETAMT) > " + strMayor + " AND SUM(LINENETAMT) < " + strMenor;
    } else if (!strMayor.equals("") && strMenor.equals("")) {
      strHaving = " HAVING SUM(LINENETAMT) > " + strMayor;
    } else if (strMayor.equals("") && !strMenor.equals("")) {
      strHaving = " HAVING SUM(LINENETAMT) < " + strMenor;
    } else {
    }
    if (strHaving.equals("")) {
      if (!strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving = " HAVING C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor
            + " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else if (!strRatioMayor.equals("") && strRatioMenor.equals("")) {
        strHaving = " HAVING C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor;
      } else if (strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving = " HAVING C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else {
      }
    } else {
      if (!strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving += " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor
            + " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else if (!strRatioMayor.equals("") && strRatioMenor.equals("")) {
        strHaving += " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor;
      } else if (strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving += " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else {
      }
    }
    strOrderby = strHaving + strOrderby;

    // Checks if there is a conversion rate for each of the transactions of
    // the report
    String strConvRateErrorMsg = "";
    OBError myMessage = null;
    myMessage = new OBError();
    if (strComparative.equals("Y")) {
      try {
        data = ReportRefundSalesDimensionalAnalysesData.select(this, strCurrencyId, strTextShow[0],
            strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4], strTextShow[5],
            strTextShow[6],
            Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
            Utility.getContext(this, vars, "#User_Client", "ReportRefundSalesDimensionalAnalyses"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strPartnerGroup,
            strcBpartnerId, strProductCategory, strmProductId, strsalesrepId, strmWarehouseId,
            strDateFromRef, DateTimeData.nDaysAfter(this, strDateToRef, "1"), strOrderby);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
    } else {
      try {
        data = ReportRefundSalesDimensionalAnalysesData.selectNoComparative(this, strCurrencyId,
            strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4],
            strTextShow[5], strTextShow[6],
            Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg),
            Utility.getContext(this, vars, "#User_Client", "ReportRefundSalesDimensionalAnalyses"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strPartnerGroup,
            strcBpartnerId, strProductCategory, strmProductId, strsalesrepId, strmWarehouseId,
            strOrderby);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
    }
    strConvRateErrorMsg = myMessage.getMessage();
    // If a conversion rate is missing for a certain transaction, an error
    // message window pops-up.
    if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
      advisePopUp(request, response, "ERROR",
          Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
          strConvRateErrorMsg);
    } else { // Otherwise, the report is launched
      if (log4j.isDebugEnabled())
        log4j.debug("*******************PDF" + strOrderby);
      if (data.length == 0 || data == null) {
        data = ReportRefundSalesDimensionalAnalysesData.set();
      } else {
        int contador = intDiscard;
        if (intAuxDiscard != -1)
          contador = intAuxDiscard;
        int k = 1;
        if (strComparative.equals("Y")) {
          for (int j = contador; j > 0; j--) {
            discard1[k] = "fieldTotalQtyNivel" + String.valueOf(j);
            discard1[k + 12] = "fieldTotalRefundQtyNivel" + String.valueOf(j);
            discard1[k + 24] = "fieldUomsymbol" + String.valueOf(j);
            discard1[k + 6] = "fieldTotalRefQtyNivel" + String.valueOf(j);
            discard1[k + 18] = "fieldTotalRefRefundQtyNivel" + String.valueOf(j);
            k++;
          }
        } else {
          for (int j = contador; j > 0; j--) {
            discard1[k] = "fieldNoncomparativeTotalQtyNivel" + String.valueOf(j);
            discard1[k + 10] = "fieldNoncomparativeTotalRefundQtyNivel" + String.valueOf(j);
            discard1[k + 20] = "fieldNoncomparativeUomsymbol" + String.valueOf(j);
            k++;
          }
        }

      }
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_reports/ReportRefundSalesDimensionalAnalysesEditionPDF",
          discard1).createXmlDocument();

      xmlDocument.setParameter("eliminar2", discard[1]);
      xmlDocument.setParameter("eliminar3", discard[2]);
      xmlDocument.setParameter("eliminar4", discard[3]);
      xmlDocument.setParameter("eliminar5", discard[4]);
      xmlDocument.setParameter("eliminar6", discard[5]);
      xmlDocument.setParameter("eliminar7", discard[6]);
      xmlDocument.setParameter("title", strTitle);
      String strCurISOSym = Utility.stringISOSymbol(this, strCurrencyId);
      strCurISOSym = strCurISOSym.replace('(', ' ');
      strCurISOSym = strCurISOSym.replace(')', ' ');
      xmlDocument.setParameter("convisosym", strCurISOSym);
      xmlDocument.setParameter("constante", "100");
      if (strComparative.equals("Y")) {
        xmlDocument.setData("structure1", data);
      } else {
        xmlDocument.setData("structure2", data);
      }
      String strResult = xmlDocument.print();
      renderFO(strResult, request, response);
    }
  }

  public String getServletInfo() {
    return "Servlet ReportRefundSalesDimensionalAnalyses. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
