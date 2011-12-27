/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.process;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.accounting.FIN_FinancialAccountAccounting;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.model.sales.SalesRegion;
import org.openbravo.scheduling.ProcessBundle;

public class FIN_TransactionModify implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;

  public void execute(ProcessBundle bundle) throws Exception {
    dao = new AdvPaymentMngtDao();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", bundle.getContext()
        .getLanguage()));

    OBContext.setAdminMode();
    try {
      String recordID = (String) bundle.getParams().get("FIN_Finacc_Transaction_ID");
      if (recordID == null || "".equals(recordID)) {
        recordID = (String) bundle.getParams().get("Aprm_Finacc_Transaction_V_ID");
      }
      final FIN_FinaccTransaction transaction = dao
          .getObject(FIN_FinaccTransaction.class, recordID);
      // Checks
      if ("Y".equals(transaction.getPosted()) && isTransactionPostingEnabled(transaction)) {
        msg.setType("Error");
        msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
            .getLanguage()));
        msg.setMessage(Utility.parseTranslation(bundle.getConnection(), bundle.getContext()
            .toVars(), bundle.getContext().getLanguage(), "@PostedDocument@"));
        bundle.setResult(msg);
        return;
      }

      if (transaction.getReconciliation() != null
          && "Y".equals(transaction.getReconciliation().getPosted())
          && !isTransactionPostingEnabled(transaction)) {
        msg.setType("Error");
        msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
            .getLanguage()));
        msg.setMessage(Utility.parseTranslation(bundle.getConnection(), bundle.getContext()
            .toVars(), bundle.getContext().getLanguage(), "@APRM_RelatedPostedDocument@"));
        bundle.setResult(msg);
        return;
      }
      GLItem oldGLItem = transaction.getGLItem();
      GLItem newGLItem = oldGLItem;
      final String strGLItemId = (String) bundle.getParams().get("cGlitemId");
      final String strProductId = (String) bundle.getParams().get("mProductId");
      final String strBPartnerId = (String) bundle.getParams().get("cBpartnerId");
      final String strProjectId = (String) bundle.getParams().get("cProjectId");
      final String strCampaignId = (String) bundle.getParams().get("cCampaignId");
      final String strActivityId = (String) bundle.getParams().get("cActivityId");
      final String strSalesRegionId = (String) bundle.getParams().get("cSalesregionId");
      transaction.setProcessed(false);
      OBDal.getInstance().save(transaction);
      OBDal.getInstance().flush();
      if (strGLItemId != null && !"".equals(strGLItemId)) {
        newGLItem = OBDal.getInstance().get(GLItem.class, strGLItemId);
        transaction.setGLItem(OBDal.getInstance().get(GLItem.class, strGLItemId));
      } else {
        transaction.setGLItem(null);
      }
      if (strProductId != null && !"".equals(strProductId)) {
        transaction.setProduct(OBDal.getInstance().get(Product.class, strProductId));
      } else {
        transaction.setProduct(null);
      }
      if (strBPartnerId != null && !"".equals(strBPartnerId)) {
        transaction.setBusinessPartner(OBDal.getInstance()
            .get(BusinessPartner.class, strBPartnerId));
      } else {
        transaction.setBusinessPartner(null);
      }
      if (strProjectId != null && !"".equals(strProjectId)) {
        transaction.setProject(OBDal.getInstance().get(Project.class, strProjectId));
      } else {
        transaction.setProject(null);
      }
      if (strCampaignId != null && !"".equals(strCampaignId)) {
        transaction.setSalesCampaign(OBDal.getInstance().get(Campaign.class, strCampaignId));
      } else {
        transaction.setSalesCampaign(null);
      }
      if (strActivityId != null && !"".equals(strActivityId)) {
        transaction.setActivity(OBDal.getInstance().get(ABCActivity.class, strActivityId));
      } else {
        transaction.setActivity(null);
      }
      if (strSalesRegionId != null && !"".equals(strSalesRegionId)) {
        transaction.setSalesRegion(OBDal.getInstance().get(SalesRegion.class, strSalesRegionId));
      } else {
        transaction.setSalesRegion(null);
      }
      String description = transaction.getDescription();
      String oldGlItemString = Utility.messageBD(bundle.getConnection(), "APRM_GLItem", bundle
          .getContext().getLanguage())
          + ": " + oldGLItem.getName();
      String newGlItemString = Utility.messageBD(bundle.getConnection(), "APRM_GLItem", bundle
          .getContext().getLanguage())
          + ": " + newGLItem.getName();
      if (!description.isEmpty()) {
        description = description.indexOf(oldGlItemString) != -1 ? description.substring(0,
            description.indexOf(oldGlItemString))
            + description.substring(
                oldGlItemString.length() + description.indexOf(oldGlItemString),
                description.length()) : description;
      }
      description = description.isEmpty() ? newGlItemString : description + "\n" + newGlItemString;
      transaction.setDescription(description);
      transaction.setProcessed(true);
      OBDal.getInstance().save(transaction);
      OBDal.getInstance().flush();
      bundle.setResult(msg);
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private boolean isTransactionPostingEnabled(FIN_FinaccTransaction transaction) {
    boolean confirmation = false;
    OBContext.setAdminMode();
    final String TRXTYPE_BPDeposit = "BPD";
    final String TRXTYPE_BPWithdrawal = "BPW";
    final String TRXTYPE_BankFee = "BF";
    try {
      List<FIN_FinancialAccountAccounting> accounts = transaction.getAccount()
          .getFINFinancialAccountAcctList();
      FIN_Payment payment = transaction.getFinPayment();
      if (payment != null) {
        OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
            FinAccPaymentMethod.class);
        obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
        obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
            payment.getPaymentMethod()));
        obCriteria.setFilterOnReadableClients(false);
        obCriteria.setFilterOnReadableOrganization(false);
        List<FinAccPaymentMethod> lines = obCriteria.list();
        for (FIN_FinancialAccountAccounting account : accounts) {
          if (confirmation)
            return confirmation;
          if (payment.isReceipt()) {
            if (("INT").equals(lines.get(0).getUponDepositUse())
                && account.getInTransitPaymentAccountIN() != null)
              confirmation = true;
            else if (("DEP").equals(lines.get(0).getUponDepositUse())
                && account.getDepositAccount() != null)
              confirmation = true;
            else if (("CLE").equals(lines.get(0).getUponDepositUse())
                && account.getClearedPaymentAccount() != null)
              confirmation = true;
          } else {
            if (("INT").equals(lines.get(0).getUponWithdrawalUse())
                && account.getFINOutIntransitAcct() != null)
              confirmation = true;
            else if (("WIT").equals(lines.get(0).getUponWithdrawalUse())
                && account.getWithdrawalAccount() != null)
              confirmation = true;
            else if (("CLE").equals(lines.get(0).getUponWithdrawalUse())
                && account.getClearedPaymentAccountOUT() != null)
              confirmation = true;
          }
        }
      } else {
        for (FIN_FinancialAccountAccounting account : accounts) {
          if (confirmation)
            return confirmation;
          if ((TRXTYPE_BPDeposit.equals(transaction.getTransactionType()) && account
              .getDepositAccount() != null)
              || (TRXTYPE_BPWithdrawal.equals(transaction.getTransactionType()) && account
                  .getWithdrawalAccount() != null)
              || (TRXTYPE_BankFee.equals(transaction.getTransactionType()) && account
                  .getWithdrawalAccount() != null))
            confirmation = true;
        }
      }
    } catch (Exception e) {
      return confirmation;
    } finally {
      OBContext.restorePreviousMode();
    }
    return confirmation;

  }

}
