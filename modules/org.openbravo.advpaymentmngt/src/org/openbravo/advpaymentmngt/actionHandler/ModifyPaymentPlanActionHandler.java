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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.advpaymentmngt.actionHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_PaymentMonitorProcess;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.ApplicationConstants;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.Fin_OrigPaymentSchedule;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonToDataConverter;

public class ModifyPaymentPlanActionHandler extends BaseProcessActionHandler {

  private final String buttonNewVersion = "newVersion";
  private final String buttonModifyOriginal = "modifyOriginal";
  private final AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
  private static final Logger log4j = Logger.getLogger(ModifyPaymentPlanActionHandler.class);

  @Override
  /**
   * Receives the modified payment plan for a given invoice
   */
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    boolean modifyOriginal = false;
    try {
      jsonRequest = new JSONObject(content);
      String strInvoiceId = jsonRequest.getString("inpcInvoiceId");
      if (strInvoiceId == null || strInvoiceId.isEmpty() || "null".equalsIgnoreCase(strInvoiceId)) {
        strInvoiceId = jsonRequest.getString("C_Invoice_ID");
      }
      Invoice invoice = OBDal.getInstance().get(Invoice.class, strInvoiceId);
      JSONArray gridRows = jsonRequest.getJSONArray(ApplicationConstants.ALL_ROWS_PARAM);
      List<FIN_PaymentSchedule> databaseRows = new ArrayList<FIN_PaymentSchedule>();
      databaseRows = getDatabaseRows(invoice);

      if (jsonRequest.getString(ApplicationConstants.BUTTON_VALUE).equals(buttonModifyOriginal)) {
        modifyOriginal = true;
      } else if (jsonRequest.getString(ApplicationConstants.BUTTON_VALUE).equals(buttonNewVersion)) {
        modifyOriginal = false;
      } else {
        return addMessage(jsonRequest, "@APRM_ButtonNotValid@", "error");
      }

      if (modifyOriginal && paidAnyAmount(invoice)) {
        return addMessage(jsonRequest, "@APRM_AlreadyPaidInvoice@", "error");
      }

      String errorMsg = validateGridAmounts(gridRows, invoice);
      if (errorMsg != null) {
        OBDal.getInstance().rollbackAndClose();
        return addMessage(jsonRequest, errorMsg, "error");
      }
      if (!validateInvoiceAmounts(invoice)) {
        OBDal.getInstance().rollbackAndClose();
        return addMessage(jsonRequest, "@APRM_ExistingPlanIsNotCorrect@", "error");
      }

      List<JSONObject> lToCreate = getNewRows(gridRows);
      List<FIN_PaymentSchedule> lToRemove = getRemovedRows(databaseRows, gridRows);
      List<FIN_PaymentSchedule> lToModify = getModifiedRows(databaseRows, gridRows, lToCreate,
          lToRemove);
      HashMap<FIN_PaymentSchedule, BigDecimal> orders = getOrders(lToRemove, lToModify);
      HashMap<FIN_PaymentDetail, BigDecimal> canceledPSDs = getCanceledPSDs(lToRemove, lToModify);

      removeRows(lToRemove, invoice);
      List<FIN_PaymentSchedule> createdPSs = createRows(lToCreate, invoice);
      createdPSs = modifyRows(lToModify, gridRows, invoice, createdPSs);
      createPSDetails(createdPSs, orders);
      assignCanceled(invoice, canceledPSDs);

      if (!ordersSumsZero(orders, invoice.getFINPaymentScheduleList().get(0))) {
        OBDal.getInstance().rollbackAndClose();
        return addMessage(jsonRequest, "@APRM_AmountNotFullyAllocated@", "error");
      }

      if (modifyOriginal) {
        writeOriginalPlan(invoice);
      }

      if (!validateInvoiceAmounts(invoice)) {
        OBDal.getInstance().rollbackAndClose();
        return addMessage(jsonRequest, "@APRM_AmountMismatch@", "error");
      }
      // As a final step, Payment Monitor information for this invoice is updated.
      FIN_PaymentMonitorProcess.updateInvoice(invoice);
      return addMessage(jsonRequest, "@Success@", "success");
    } catch (ConstraintViolationException e) {
      OBDal.getInstance().rollbackAndClose();
      log4j.error("Exception! " + e);
      String constraint = e.getConstraintName();
      constraint = constraint.substring(constraint.lastIndexOf(".") + 1, constraint.length());
      try {
        return addMessage(jsonRequest, "@" + constraint + "@", "error");
      } catch (Exception ex) {
        log4j.error("Exception! " + ex);
        return jsonRequest;
      }
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log4j.error("Exception! " + e);
      try {
        return addMessage(jsonRequest, "@ProcessRunError@", "error");
      } catch (Exception ex) {
        log4j.error("Exception! " + ex);
        return jsonRequest;
      }
    }
  }

  /**
   * Once the payment schedules are created, this process creates the payment schedule detail
   * elements, assigning the amounts related to orders
   * 
   * @throws Exception
   */
  private void createPSDetails(List<FIN_PaymentSchedule> createdPSs,
      HashMap<FIN_PaymentSchedule, BigDecimal> ordersProvided) throws Exception {
    HashMap<FIN_PaymentSchedule, BigDecimal> orders = ordersProvided;
    if (!correctAmounts(createdPSs, orders)) {
      throw new Exception();
    }

    for (FIN_PaymentSchedule ps : createdPSs) {
      orders = createPaymentScheduleDetail(ps, orders);
    }
  }

  /**
   * Given a list of payment schedule element and a list of amounts associated to orders, creates
   * the payment schedule details for the given payment schedule element
   * 
   */
  private HashMap<FIN_PaymentSchedule, BigDecimal> createPaymentScheduleDetail(
      FIN_PaymentSchedule invoicePS, HashMap<FIN_PaymentSchedule, BigDecimal> ordersProvided) {
    HashMap<FIN_PaymentSchedule, BigDecimal> orders = ordersProvided;
    Iterator<FIN_PaymentSchedule> ite = orders.keySet().iterator();
    BigDecimal amount = getPendingPSAmounts(invoicePS);
    List<FIN_PaymentSchedule> lOrdersToRemove = new ArrayList<FIN_PaymentSchedule>();
    FIN_PaymentSchedule orderPS = null;
    BigDecimal orderAmount = BigDecimal.ZERO;
    if (orders.containsKey(null)) {
      orderAmount = orders.get(null);
    } else {
      lOrdersToRemove.add(null);
    }
    while (amount.compareTo(BigDecimal.ZERO) != 0 && ite.hasNext()) {
      if (lOrdersToRemove.contains(orderPS) || orderAmount.compareTo(BigDecimal.ZERO) == 0) {
        orderPS = ite.next();
        orderAmount = orders.get(orderPS);
      }
      if (amount.abs().compareTo(orderAmount.abs()) >= 0) {
        if (orderAmount.compareTo(BigDecimal.ZERO) != 0) {
          dao.getNewPaymentScheduleDetail(invoicePS, orderPS, orderAmount, BigDecimal.ZERO, null);
        }
        amount = amount.subtract(orderAmount);
        orderAmount = BigDecimal.ZERO;
        lOrdersToRemove.add(orderPS);
      } else {
        if (amount.compareTo(BigDecimal.ZERO) != 0) {
          dao.getNewPaymentScheduleDetail(invoicePS, orderPS, amount, BigDecimal.ZERO, null);
        }
        orderAmount = orderAmount.subtract(amount);
        amount = BigDecimal.ZERO;
      }
      orders.put(orderPS, orderAmount);
    }
    OBDal.getInstance().flush();

    for (FIN_PaymentSchedule ps : lOrdersToRemove) {
      orders.remove(ps);
    }
    return orders;
  }

  /**
   * Returns true in case the sum of order amounts is equal to the sum of PS amounts
   * 
   */
  private boolean correctAmounts(List<FIN_PaymentSchedule> createdPSs,
      HashMap<FIN_PaymentSchedule, BigDecimal> ordersProvided) {
    HashMap<FIN_PaymentSchedule, BigDecimal> orders = ordersProvided;
    BigDecimal psAmount = BigDecimal.ZERO;
    for (FIN_PaymentSchedule ps : createdPSs) {
      OBDal.getInstance().refresh(ps);
      psAmount = psAmount.add(getPendingPSAmounts(ps));
    }

    BigDecimal ordersAmounts = BigDecimal.ZERO;

    Iterator<FIN_PaymentSchedule> ite = orders.keySet().iterator();
    while (ite.hasNext()) {
      ordersAmounts = ordersAmounts.add(orders.get(ite.next()));
    }

    if (ordersAmounts.compareTo(psAmount) != 0) {
      return false;
    }
    return true;
  }

  /**
   * It could happen that the orders associated to the invoice payment plan do have positive and
   * negative amounts. In this case, the order map will have data, but sum of amounts will be zero.
   * In that case, new payment schedule detail elements are created for these amounts
   * 
   */
  private boolean ordersSumsZero(HashMap<FIN_PaymentSchedule, BigDecimal> orders,
      FIN_PaymentSchedule ps) {
    if (orders == null || orders.size() == 0) {
      return true;
    }
    BigDecimal acum = BigDecimal.ZERO;
    Iterator<FIN_PaymentSchedule> ite = orders.keySet().iterator();
    while (ite.hasNext()) {
      FIN_PaymentSchedule orderPS = ite.next();
      BigDecimal orderAmount = orders.get(orderPS);
      if (orderAmount.compareTo(BigDecimal.ZERO) != 0) {
        acum = acum.add(orderAmount);
        dao.getNewPaymentScheduleDetail(ps, orderPS, orderAmount, BigDecimal.ZERO, null);
      }
    }
    if (acum.compareTo(BigDecimal.ZERO) == 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Amounts corresponding to canceled payments are assigned to first payment plan line of the
   * invoice
   * 
   */
  private void assignCanceled(Invoice invoice, HashMap<FIN_PaymentDetail, BigDecimal> canceledPSDs) {

    for (FIN_PaymentSchedule ps : invoice.getFINPaymentScheduleList()) {
      Iterator<FIN_PaymentDetail> ite = canceledPSDs.keySet().iterator();
      while (ite.hasNext()) {
        FIN_PaymentDetail pd = ite.next();
        BigDecimal amount = canceledPSDs.get(pd);
        FIN_PaymentScheduleDetail psd = dao.getNewPaymentScheduleDetail(ps, null, amount,
            BigDecimal.ZERO, pd);
        psd.setCanceled(true);
      }
      return;
    }
  }

  /**
   * Returns the set of payment schedule detail elements for payments that have been canceled
   * 
   */
  private HashMap<FIN_PaymentDetail, BigDecimal> getCanceledPSDs(
      List<FIN_PaymentSchedule> lToRemove, List<FIN_PaymentSchedule> lToModify) {
    HashMap<FIN_PaymentDetail, BigDecimal> mapReturn = new HashMap<FIN_PaymentDetail, BigDecimal>();

    List<FIN_PaymentSchedule> lDBRowsToDeleteOrModify = new ArrayList<FIN_PaymentSchedule>(
        lToRemove);
    lDBRowsToDeleteOrModify.addAll(lToModify);

    for (FIN_PaymentSchedule ps : lDBRowsToDeleteOrModify) {
      for (FIN_PaymentScheduleDetail psd : ps
          .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
        if (psd.isCanceled()) {
          FIN_PaymentDetail pd = psd.getPaymentDetails();
          if (pd != null) {
            mapReturn.put(pd, psd.getAmount());
          }
        }
      }
    }

    return mapReturn;
  }

  /**
   * Modified rows are processed. For each of them, it's PSD and PS lines are deleted, and created
   * again.
   * 
   * @throws Exception
   */
  private List<FIN_PaymentSchedule> modifyRows(List<FIN_PaymentSchedule> lToModify,
      JSONArray gridRows, Invoice invoice, List<FIN_PaymentSchedule> createdPSs) throws Exception {
    List<FIN_PaymentSchedule> lPSsToReturn = createdPSs;
    for (FIN_PaymentSchedule invoicePS : lToModify) {
      // 1) Remove not paid payment schedule detail lines
      OBCriteria<FIN_PaymentScheduleDetail> obcPSD = OBDal.getInstance().createCriteria(
          FIN_PaymentScheduleDetail.class);
      obcPSD.add(Restrictions.eq(FIN_PaymentScheduleDetail.PROPERTY_INVOICEPAYMENTSCHEDULE,
          invoicePS));
      obcPSD.add(Restrictions.isNull(FIN_PaymentScheduleDetail.PROPERTY_PAYMENTDETAILS));
      for (FIN_PaymentScheduleDetail psd : obcPSD.list()) {
        dao.removePaymentScheduleDetail(psd);
      }

      // 2) New values are retrieved
      JSONObject modifiedGridRow = getModifiedRow(gridRows, invoicePS.getId());
      if (modifiedGridRow == null) {
        return new ArrayList<FIN_PaymentSchedule>();
      }

      // 3) New row is created
      BigDecimal outstanding = new BigDecimal(modifiedGridRow.getString("outstanding"));
      Date dueDate = getJSDate(modifiedGridRow.getString("dueDate"));
      FIN_PaymentMethod pm = OBDal.getInstance().get(FIN_PaymentMethod.class,
          modifiedGridRow.getString("paymentMethod"));
      invoicePS.setOutstandingAmount(outstanding);
      invoicePS.setAmount(invoicePS.getPaidAmount().add(outstanding));
      invoicePS.setDueDate(dueDate);
      invoicePS.setFinPaymentmethod(pm);
      OBDal.getInstance().save(invoicePS);
      lPSsToReturn.add(invoicePS);
    }
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(invoice);

    return lPSsToReturn;
  }

  /**
   * Given an invoice payment plan line, this function returns the sum of all the amounts pending to
   * confirm (for example, in awaiting execution status) for that payment plan line
   * 
   */
  private BigDecimal getPendingPSAmounts(FIN_PaymentSchedule invoicePS) {
    BigDecimal result = invoicePS.getAmount();
    for (FIN_PaymentScheduleDetail psd : invoicePS
        .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
      if (!psd.isCanceled() && (psd.getPaymentDetails() != null)) {
        result = result.subtract(psd.getAmount());
      }
    }
    return result;
  }

  /**
   * Given the grid and an ID returns the grid row with that ID, or null if not found
   * 
   * @throws JSONException
   */
  private JSONObject getModifiedRow(JSONArray gridRows, String id) throws JSONException {
    for (int ind = 0; ind < gridRows.length(); ind++) {
      if (gridRows.getJSONObject(ind).getString("id").equals(id)) {
        return gridRows.getJSONObject(ind);
      }
    }
    return null;
  }

  /**
   * Given the new elements created by the user in the grid, this function creates the payment plan
   * lines for those lines.
   * 
   * @throws Exception
   */
  private List<FIN_PaymentSchedule> createRows(List<JSONObject> lToCreate, Invoice invoice)
      throws Exception {
    List<FIN_PaymentSchedule> lToReturn = new ArrayList<FIN_PaymentSchedule>();
    for (JSONObject jo : lToCreate) {
      BigDecimal outstanding = new BigDecimal(jo.getString("outstanding"));
      FIN_PaymentMethod paymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
          jo.getString("paymentMethod"));
      String dueDate = jo.getString("dueDate");
      FIN_PaymentSchedule invoicePS = dao.getNewPaymentSchedule(invoice.getClient(),
          invoice.getOrganization(), invoice, null, invoice.getCurrency(), getJSDate(dueDate),
          paymentMethod, outstanding);
      lToReturn.add(invoicePS);
    }
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(invoice);

    return lToReturn;
  }

  /**
   * Given the set of payment schedule elements to be deleted or modified, this function returns the
   * set of order payment schedule lines associated to any of the given payment schedule elements
   * 
   */
  private HashMap<FIN_PaymentSchedule, BigDecimal> getOrders(List<FIN_PaymentSchedule> lToRemove,
      List<FIN_PaymentSchedule> lToModify) {
    HashMap<FIN_PaymentSchedule, BigDecimal> mapReturn = new HashMap<FIN_PaymentSchedule, BigDecimal>();

    List<FIN_PaymentSchedule> lDBRowsToDeleteOrModify = new ArrayList<FIN_PaymentSchedule>(
        lToRemove);
    lDBRowsToDeleteOrModify.addAll(lToModify);

    for (FIN_PaymentSchedule ps : lDBRowsToDeleteOrModify) {
      for (FIN_PaymentScheduleDetail psd : ps
          .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
        FIN_Payment payment = (psd.getPaymentDetails() == null) ? null : psd.getPaymentDetails()
            .getFinPayment();
        if (!psd.isCanceled() && payment == null) {
          FIN_PaymentSchedule ops = psd.getOrderPaymentSchedule();
          BigDecimal amount = BigDecimal.ZERO;
          if (mapReturn.containsKey(ops)) {
            amount = mapReturn.get(ops);
          }
          mapReturn.put(ops, amount.add(psd.getAmount()));
        }
      }
    }

    return mapReturn;
  }

  /**
   * Removes from database the Payment Schedule elements included in the provided list
   * 
   */
  private void removeRows(List<FIN_PaymentSchedule> lToRemove, Invoice invoice) {
    for (FIN_PaymentSchedule ps : lToRemove) {
      if (ps.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
        dao.removePaymentSchedule(ps);
      } else {
        ps.setOutstandingAmount(BigDecimal.ZERO);
        ps.setAmount(ps.getPaidAmount());
        List<FIN_PaymentScheduleDetail> lPSDs = ps
            .getFINPaymentScheduleDetailInvoicePaymentScheduleList();
        for (int indPSD = 0; indPSD < lPSDs.size(); indPSD++) {
          FIN_PaymentScheduleDetail psd = lPSDs.get(indPSD);
          if (psd.getPaymentDetails() == null)
            dao.removePaymentScheduleDetail(psd);
        }
      }
    }
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(invoice);
  }

  /**
   * Given an invoice, returns the relation of its payment schedule lines that are not fully paid
   * 
   */
  private List<FIN_PaymentSchedule> getDatabaseRows(Invoice invoice) {
    List<FIN_PaymentSchedule> lQuery, lReturn = new ArrayList<FIN_PaymentSchedule>();
    OBCriteria<FIN_PaymentSchedule> obcPS = OBDal.getInstance().createCriteria(
        FIN_PaymentSchedule.class);
    obcPS.add(Restrictions.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, invoice));
    lQuery = obcPS.list();
    for (FIN_PaymentSchedule ps : lQuery) {
      if (ps.getPaidAmount().abs().compareTo(ps.getAmount().abs()) < 0) {
        lReturn.add(ps);
      }
    }
    return lReturn;
  }

  /**
   * Returns the set of JSON Objects that corresponds to new lines
   * 
   * @throws JSONException
   */
  private List<JSONObject> getNewRows(JSONArray gridRows) throws JSONException {
    List<JSONObject> lResult = new ArrayList<JSONObject>();
    for (int ind = 0; ind < gridRows.length(); ind++) {
      String id = gridRows.getJSONObject(ind).getString("id");
      if (StringUtils.isEmpty(id) || id.equals("NEW")) {
        lResult.add(gridRows.getJSONObject(ind));
      }
    }
    return lResult;
  }

  /**
   * Returns the set of IDs corresponding to the rows that where displayed in the grid, and then
   * removed by the user
   * 
   * @throws JSONException
   */

  private List<FIN_PaymentSchedule> getRemovedRows(List<FIN_PaymentSchedule> databaseRows,
      JSONArray gridRows) throws JSONException {
    List<String> lAll = new ArrayList<String>();
    List<String> lExists = new ArrayList<String>();
    List<FIN_PaymentSchedule> lReturn = new ArrayList<FIN_PaymentSchedule>();

    for (int indDB = 0; indDB < databaseRows.size(); indDB++) {
      String idDB = databaseRows.get(indDB).getId();
      lAll.add(idDB);
    }
    for (int indGrid = 0; indGrid < gridRows.length(); indGrid++) {
      String idGrid = gridRows.getJSONObject(indGrid).getString("id");
      if (idGrid != null && !idGrid.equals("")) {
        lExists.add(idGrid);
      }
    }

    lAll.removeAll(lExists);

    for (String id : lAll) {
      lReturn.add(OBDal.getInstance().get(FIN_PaymentSchedule.class, id));
    }

    return lReturn;
  }

  /**
   * Returns the set of database rows that where modified, but not deleted
   * 
   * @throws JSONException
   */
  private List<FIN_PaymentSchedule> getModifiedRows(List<FIN_PaymentSchedule> databaseRows,
      JSONArray gridRows, List<JSONObject> newRows, List<FIN_PaymentSchedule> lToRemove)
      throws JSONException {

    List<FIN_PaymentSchedule> lResult = new ArrayList<FIN_PaymentSchedule>();

    for (FIN_PaymentSchedule ps : databaseRows) {
      if (!(lToRemove.contains(ps))) {
        for (int indGrid = 0; indGrid < gridRows.length(); indGrid++) {
          JSONObject jo = gridRows.getJSONObject(indGrid);
          if (!(newRows.contains(jo))) {
            if (ps.getId().equals(jo.getString("id")) && wasModified(ps, jo)) {
              lResult.add(ps);
            }
          }
        }
      }
    }
    return lResult;
  }

  /**
   * Returns true in case any amount has been already paid, even if it was paid and then cancelled,
   * for this invoice
   * 
   */
  private boolean paidAnyAmount(Invoice invoice) {
    for (FIN_PaymentSchedule ps : invoice.getFINPaymentScheduleList()) {
      for (FIN_PaymentScheduleDetail psd : ps
          .getFINPaymentScheduleDetailInvoicePaymentScheduleList()) {
        if (psd.getAmount().compareTo(BigDecimal.ZERO) != 0 && psd.getPaymentDetails() != null) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true in case the provided Payment Schedule Detail line and JOSN grid line do differ in
   * any value (this means, user modified the original values). If Edit Payment Plan functionality
   * is improved in the future, adding more columns suitable to be modified, this function must take
   * them into account!
   * 
   * @throws JSONException
   */
  private boolean wasModified(FIN_PaymentSchedule ps, JSONObject jsonObject) throws JSONException {
    if (new BigDecimal(jsonObject.getString("outstanding")).compareTo(ps.getOutstandingAmount()) != 0) {
      return true;
    }
    if (!jsonObject.getString("paymentMethod").equals(ps.getFinPaymentmethod().getId())) {
      return true;
    }
    if (!getJSDate(jsonObject.getString("dueDate")).equals(ps.getDueDate())) {
      return true;
    }
    return false;
  }

  /**
   * Replaces the original payment plan of a given invoice, with a copy of the actual payment plan
   * 
   */
  private void writeOriginalPlan(Invoice invoice) {
    List<String> lOPS = new ArrayList<String>();
    for (Fin_OrigPaymentSchedule ops : invoice.getFinOrigPaymentScheduleList()) {
      lOPS.add(ops.getId());
    }
    invoice.setFinOrigPaymentScheduleList(null);
    invoice.setFinOrigPaymentSchedVList(null);
    OBDal.getInstance().save(invoice);
    for (String id : lOPS) {
      OBDal.getInstance().remove(OBDal.getInstance().get(Fin_OrigPaymentSchedule.class, id));
    }
    OBDal.getInstance().refresh(invoice);
    for (FIN_PaymentSchedule ps : invoice.getFINPaymentScheduleList())
      saveOrigPaymentSchedule(ps);
    OBDal.getInstance().flush();
  }

  /**
   * Saves a new original payment schedule line, with same information of a given payment schedule
   * line
   * 
   */
  private void saveOrigPaymentSchedule(FIN_PaymentSchedule ps) {
    final Fin_OrigPaymentSchedule ops = OBProvider.getInstance().get(Fin_OrigPaymentSchedule.class);
    ops.setClient(ps.getClient());
    ops.setOrganization(ps.getOrganization());
    ops.setAmount(ps.getAmount());
    ops.setCurrency(ps.getCurrency());
    ops.setDueDate(ps.getDueDate());
    ops.setInvoice(ps.getInvoice());
    ops.setPaymentMethod(ps.getFinPaymentmethod());
    ops.setPaymentPriority(ps.getFINPaymentPriority());
    OBDal.getInstance().save(ops);
    OBDal.getInstance().flush();
  }

  /**
   * Parses the string to a date using the dateFormat.java property.
   * 
   */
  private static Date getJSDate(String strDate) {
    if (strDate.equals(""))
      return null;
    Field field = OBDal.getInstance().get(Field.class, "B6BB67AE51F31BEBE040A8C091666000");
    Date date = (Date) JsonToDataConverter.convertJsonToPropertyValue(KernelUtils.getInstance()
        .getPropertyFromColumn(field.getColumn()), strDate);
    return date;
  }

  /**
   * Given a JSONObject to be returned, it adds a message to it
   * 
   * @throws JSONException
   */
  private JSONObject addMessage(JSONObject content, String strMessage, String strSeverity)
      throws JSONException {
    JSONObject outPut = content;
    JSONObject message = new JSONObject();
    message.put("severity", strSeverity);
    message.put("text", Utility.parseTranslation(new DalConnectionProvider(),
        new VariablesSecureApp(OBContext.getOBContext().getUser().getId(), OBContext.getOBContext()
            .getCurrentClient().getId(), OBContext.getOBContext().getCurrentOrganization().getId(),
            OBContext.getOBContext().getRole().getId()), OBContext.getOBContext().getLanguage()
            .getLanguage(), strMessage));
    outPut.put("message", message);
    return outPut;
  }

  /**
   * Given an invoice, checks that payment schedule is correct
   * 
   * @throws JSONException
   */
  private boolean validateInvoiceAmounts(Invoice invoice) throws JSONException {
    BigDecimal totalAmount = BigDecimal.ZERO;
    OBDal.getInstance().refresh(invoice);
    List<FIN_PaymentSchedule> lPS = invoice.getFINPaymentScheduleList();
    for (FIN_PaymentSchedule ps : lPS) {
      OBDal.getInstance().refresh(ps);
      totalAmount = totalAmount.add(ps.getAmount());
      BigDecimal psdAmount = BigDecimal.ZERO;
      List<FIN_PaymentScheduleDetail> lPSD = ps
          .getFINPaymentScheduleDetailInvoicePaymentScheduleList();
      for (FIN_PaymentScheduleDetail psd : lPSD) {
        if (!psd.isCanceled()) {
          psdAmount = psdAmount.add(psd.getAmount().add(psd.getWriteoffAmount()));
        }
      }
      if (psdAmount.compareTo(ps.getAmount()) != 0) {
        return false;
      }
    }
    OBDal.getInstance().refresh(invoice);
    return totalAmount.compareTo(invoice.getGrandTotalAmount()) == 0;
  }

  /**
   * Given an invoice, checks that all the amounts in the grid are not lower than paid amount or
   * awaiting execution amount
   * 
   * @throws JSONException
   */
  private String validateGridAmounts(JSONArray gridRows, Invoice invoice) throws JSONException {
    boolean positive = invoice.getFINPaymentScheduleList().get(0).getAmount()
        .compareTo(BigDecimal.ZERO) >= 0;
    for (int indGrid = 0; indGrid < gridRows.length(); indGrid++) {
      JSONObject jo = gridRows.getJSONObject(indGrid);
      BigDecimal outstanding = new BigDecimal(jo.getString("outstanding"));
      BigDecimal awaitingExecution = new BigDecimal(jo.getString("awaitingExecutionAmount"));
      if (awaitingExecution.abs().compareTo(outstanding.abs()) > 0) {
        return "@APRM_AwaitingExecutionAmountError@";
      }
      if (outstanding.compareTo(BigDecimal.ZERO) != 0
          && (positive != (outstanding.compareTo(BigDecimal.ZERO) > 0))) {
        return "@APRM_DifferentSignError@";
      }
    }
    return null;
  }

}
