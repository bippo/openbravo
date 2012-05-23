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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.utility;

import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.utils.Replace;

public class FIN_Utility {
  private static final long serialVersionUID = 1L;
  static Logger log4j = Logger.getLogger(Utility.class);
  private static AdvPaymentMngtDao dao;

  /**
   * @see OBDateUtils#getDate(String)
   */
  public static Date getDate(String strDate) {
    try {
      return OBDateUtils.getDate(strDate);
    } catch (ParseException e) {
      log4j.error("Error parsing date", e);
      return null;
    }

  }

  /**
   * @see OBDateUtils#getDateTime(String)
   */
  public static Date getDateTime(String strDate) {
    try {
      return OBDateUtils.getDateTime(strDate);
    } catch (ParseException e) {
      log4j.error("Error parsing date", e);
      return null;
    }
  }

  /**
   * Parses the string of comma separated id's to return a List object of the given class
   * 
   * @param <T>
   * @param t
   *          class of the OBObject the id's belong to
   * @param _strSelectedIds
   *          String containing a comma separated list of id's
   * @return a List object containing the parsed OBObjects
   */
  public static <T extends BaseOBObject> List<T> getOBObjectList(Class<T> t, String _strSelectedIds) {
    dao = new AdvPaymentMngtDao();
    String strSelectedIds = _strSelectedIds;
    final List<T> OBObjectList = new ArrayList<T>();
    // selected scheduled payments list
    if (strSelectedIds.startsWith("("))
      strSelectedIds = strSelectedIds.substring(1, strSelectedIds.length() - 1);
    if (!strSelectedIds.equals("")) {
      strSelectedIds = Replace.replace(strSelectedIds, "'", "");
      StringTokenizer st = new StringTokenizer(strSelectedIds, ",", false);
      while (st.hasMoreTokens()) {
        String strScheduledPaymentId = st.nextToken().trim();
        OBObjectList.add(dao.getObject(t, strScheduledPaymentId));
      }
    }
    return OBObjectList;
  }

  /**
   * 
   * @param _strSelectedIds
   *          Identifiers string list with the following structure: ('ID', 'ID', 'ID')
   * @return Map<K,V> using the ID as key and value <ID,ID> for each identifier.
   */
  public static Map<String, String> getMapFromStringList(String _strSelectedIds) {
    String strSelectedIds = _strSelectedIds;
    final Map<String, String> map = new HashMap<String, String>();
    if (strSelectedIds.startsWith("("))
      strSelectedIds = strSelectedIds.substring(1, strSelectedIds.length() - 1);
    if (!strSelectedIds.equals("")) {
      strSelectedIds = Replace.replace(strSelectedIds, "'", "");
      StringTokenizer st = new StringTokenizer(strSelectedIds, ",", false);
      while (st.hasMoreTokens()) {
        String strItem = st.nextToken().trim();
        map.put(strItem, strItem);
      }
    }
    return map;
  }

  /**
   * Returns a FieldProvider object containing the Scheduled Payments.
   * 
   * @param vars
   * @param selectedScheduledPayments
   *          List of FIN_PaymentSchedule that need to be selected by default
   * @param filteredScheduledPayments
   *          List of FIN_PaymentSchedule that need to unselected by default
   */
  public static FieldProvider[] getShownScheduledPayments(VariablesSecureApp vars,
      List<FIN_PaymentSchedule> selectedScheduledPayments,
      List<FIN_PaymentSchedule> filteredScheduledPayments) {
    final List<FIN_PaymentSchedule> shownScheduledPayments = new ArrayList<FIN_PaymentSchedule>();
    shownScheduledPayments.addAll(selectedScheduledPayments);
    shownScheduledPayments.addAll(filteredScheduledPayments);
    FIN_PaymentSchedule[] FIN_PaymentSchedules = new FIN_PaymentSchedule[0];
    FIN_PaymentSchedules = shownScheduledPayments.toArray(FIN_PaymentSchedules);
    // FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(FIN_PaymentSchedules);

    // FieldProvider[] data = new FieldProviderFactory[selectedScheduledPayments.size()];
    FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(shownScheduledPayments);
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
    // set in administrator mode to be able to access FIN_PaymentSchedule entity
    OBContext.setAdminMode();
    try {

      for (int i = 0; i < data.length; i++) {
        FieldProviderFactory.setField(data[i], "finSelectedPaymentId", (selectedScheduledPayments
            .contains(FIN_PaymentSchedules[i])) ? FIN_PaymentSchedules[i].getId() : "");
        FieldProviderFactory.setField(data[i], "finScheduledPaymentId",
            FIN_PaymentSchedules[i].getId());
        if (FIN_PaymentSchedules[i].getOrder() != null)
          FieldProviderFactory.setField(data[i], "orderNr", FIN_PaymentSchedules[i].getOrder()
              .getDocumentNo());
        if (FIN_PaymentSchedules[i].getInvoice() != null) {
          FieldProviderFactory.setField(data[i], "invoiceNr", FIN_PaymentSchedules[i].getInvoice()
              .getDocumentNo());
          FieldProviderFactory.setField(data[i], "invoicedAmount", FIN_PaymentSchedules[i]
              .getInvoice().getGrandTotalAmount().toString());
        }
        FieldProviderFactory.setField(data[i], "dueDate",
            dateFormater.format(FIN_PaymentSchedules[i].getDueDate()).toString());
        FieldProviderFactory.setField(data[i], "expectedAmount", FIN_PaymentSchedules[i]
            .getAmount().toString());
        String strPaymentAmt = vars.getStringParameter(
            "inpPaymentAmount" + FIN_PaymentSchedules[i].getId(), "");
        FieldProviderFactory.setField(data[i], "paymentAmount", strPaymentAmt);
        FieldProviderFactory.setField(data[i], "rownum", String.valueOf(i));

      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  /**
   * Creates a comma separated string with the Id's of the OBObjects included in the List.
   * 
   * @param <T>
   * @param obObjectList
   *          List of OBObjects
   * @return Comma separated string of Id's
   */
  public static <T extends BaseOBObject> String getInStrList(List<T> obObjectList) {
    StringBuilder strInList = new StringBuilder();
    for (T obObject : obObjectList) {
      if (strInList.length() == 0)
        strInList.append("'" + obObject.getId() + "'");
      else
        strInList.append(", '" + obObject.getId() + "'");
    }
    return strInList.toString();
  }

  /**
   * Creates a comma separated string with the Id's of the Set of Strings. This method is deprecated
   * as it has been added to Utility (core)
   * 
   * @param set
   *          Set of Strings
   * @return Comma separated string of Id's
   */
  @Deprecated
  public static String getInStrSet(Set<String> set) {
    return Utility.getInStrSet(set);
  }

  /**
   * Returns the cause of a trigger exception (BatchupdateException).
   * 
   * Hibernate and JDBC will wrap the exception thrown by the trigger in another exception (the
   * java.sql.BatchUpdateException) and this exception is sometimes wrapped again. Also the
   * java.sql.BatchUpdateException stores the underlying trigger exception in the nextException and
   * not in the cause property.
   * 
   * @param t
   *          exception.
   * @return the underlying trigger message.
   */
  public static String getExceptionMessage(Throwable t) {
    if (t.getCause() instanceof BatchUpdateException
        && ((BatchUpdateException) t.getCause()).getNextException() != null) {
      final BatchUpdateException bue = (BatchUpdateException) t.getCause();
      return bue.getNextException().getMessage();
    }
    return t.getMessage();
  }

  /**
   * Returns the DocumentType defined for the Organization (or parent organization tree) and
   * document category.
   * 
   * @param org
   *          the Organization for which the Document Type is defined. The Document Type can belong
   *          to the parent organization tree of the specified Organization.
   * @param docCategory
   *          the document category of the Document Type.
   * @return the Document Type
   */
  public static DocumentType getDocumentType(Organization org, String docCategory) {
    DocumentType outDocType = null;
    Client client = null;

    OBCriteria<DocumentType> obcDoc = OBDal.getInstance().createCriteria(DocumentType.class);
    obcDoc.setFilterOnReadableClients(false);
    obcDoc.setFilterOnReadableOrganization(false);

    if ("0".equals(org.getId())) {
      client = OBContext.getOBContext().getCurrentClient();
      if ("0".equals(client.getId())) {
        return null;
      }
    } else {
      client = org.getClient();
    }
    obcDoc.add(Restrictions.eq(DocumentType.PROPERTY_CLIENT, client));

    obcDoc
        .add(Restrictions.in("organization.id",
            OBContext.getOBContext().getOrganizationStructureProvider(org.getClient().getId())
                .getParentTree(org.getId(), true)));
    obcDoc.add(Restrictions.eq(DocumentType.PROPERTY_DOCUMENTCATEGORY, docCategory));
    obcDoc.addOrderBy(DocumentType.PROPERTY_DEFAULT, false);
    obcDoc.addOrderBy(DocumentType.PROPERTY_ID, false);
    List<DocumentType> docTypeList = obcDoc.list();
    if (docTypeList != null && docTypeList.size() > 0) {
      outDocType = docTypeList.get(0);
    }
    return outDocType;
  }

  /**
   * Returns the next sequence number of the Document Type defined for the Organization and document
   * category. The current number of the sequence is also updated.
   * 
   * @param docType
   *          Document type of the document
   * @return the next sequence number of the Document Type defined for the Organization and document
   *         category. Null if no sequence is found.
   */
  public static String getDocumentNo(DocumentType docType, String tableName) {
    String nextDocNumber = "";
    if (docType != null) {
      Sequence seq = docType.getDocumentSequence();
      if (seq == null && tableName != null) {
        OBCriteria<Sequence> obcSeq = OBDal.getInstance().createCriteria(Sequence.class);
        obcSeq.add(Restrictions.eq(Sequence.PROPERTY_NAME, tableName));
        if (obcSeq != null && obcSeq.list().size() > 0) {
          seq = obcSeq.list().get(0);
        }
      }
      if (seq != null) {
        if (seq.getPrefix() != null)
          nextDocNumber = seq.getPrefix();
        nextDocNumber += seq.getNextAssignedNumber().toString();
        if (seq.getSuffix() != null)
          nextDocNumber += seq.getSuffix();
        seq.setNextAssignedNumber(seq.getNextAssignedNumber() + seq.getIncrementBy());
        OBDal.getInstance().save(seq);
        // OBDal.getInstance().flush();
      }
    }

    return nextDocNumber;
  }

  /**
   * Returns the next sequence number of the Document Type defined for the Organization and document
   * category. The current number of the sequence is also updated.
   * 
   * @param org
   *          the Organization for which the Document Type is defined. The Document Type can belong
   *          to the parent organization tree of the specified Organization.
   * @param docCategory
   *          the document category of the Document Type.
   * @param tableName
   *          the name of the table from which the sequence will be taken if the Document Type does
   *          not have any sequence associated.
   * @return the next sequence number of the Document Type defined for the Organization and document
   *         category. Null if no sequence is found.
   */
  public static String getDocumentNo(Organization org, String docCategory, String tableName) {
    DocumentType outDocType = getDocumentType(org, docCategory);
    return getDocumentNo(outDocType, tableName);
  }

  /**
   * Gets the available Payment Methods and returns in a String the html code containing all the
   * Payment Methods in the natural tree of the given organization filtered by the Financial
   * Account.
   * 
   * @param strPaymentMethodId
   *          the Payment Method id that will be selected by default in case it is present in the
   *          list.
   * @param strFinancialAccountId
   *          optional Financial Account id to filter the Payment Methods.
   * @param strOrgId
   *          the Organization id the record belongs to.
   * @param isMandatory
   *          boolean parameter to add an extra blank option if the drop-down is optional.
   * @param excludePaymentMethodWithoutAccount
   *          if the strPaymentMethodId is empty or null then depending on this parameter the list
   *          will include payment methods with no Financial Accounts associated or only show the
   *          Payment Methods that belongs to at least on Financial Account
   * @return a String with the html code with the options to fill the drop-down of Payment Methods.
   */
  @Deprecated
  public static String getPaymentMethodList(String strPaymentMethodId,
      String strFinancialAccountId, String strOrgId, boolean isMandatory,
      boolean excludePaymentMethodWithoutAccount) {
    dao = new AdvPaymentMngtDao();
    List<FIN_PaymentMethod> paymentMethods = dao.getFilteredPaymentMethods(strFinancialAccountId,
        strOrgId, excludePaymentMethodWithoutAccount, AdvPaymentMngtDao.PaymentDirection.EITHER);
    String options = getOptionsList(paymentMethods, strPaymentMethodId, isMandatory);
    return options;
  }

  /**
   * Gets the available Payment Methods and returns in a String the html code containing all the
   * Payment Methods in the natural tree of the given organization filtered by the Financial
   * Account.
   * 
   * @param strPaymentMethodId
   *          the Payment Method id that will be selected by default in case it is present in the
   *          list.
   * @param strFinancialAccountId
   *          optional Financial Account id to filter the Payment Methods.
   * @param strOrgId
   *          the Organization id the record belongs to.
   * @param isMandatory
   *          boolean parameter to add an extra blank option if the drop-down is optional.
   * @param excludePaymentMethodWithoutAccount
   *          if the strPaymentMethodId is empty or null then depending on this parameter the list
   *          will include payment methods with no Financial Accounts associated or only show the
   *          Payment Methods that belongs to at least on Financial Account
   * @param isInPayment
   *          specifies the type of payment to get payment methods for. If true, will return payment
   *          methods with Payment In enabled, if false will return payment methods with Payment Out
   *          enabled.
   * @return a String with the html code with the options to fill the drop-down of Payment Methods.
   */
  public static String getPaymentMethodList(String strPaymentMethodId,
      String strFinancialAccountId, String strOrgId, boolean isMandatory,
      boolean excludePaymentMethodWithoutAccount, boolean isInPayment) {
    dao = new AdvPaymentMngtDao();
    List<FIN_PaymentMethod> paymentMethods = dao.getFilteredPaymentMethods(strFinancialAccountId,
        strOrgId, excludePaymentMethodWithoutAccount,
        isInPayment ? AdvPaymentMngtDao.PaymentDirection.IN
            : AdvPaymentMngtDao.PaymentDirection.OUT);
    String options = getOptionsList(paymentMethods, strPaymentMethodId, isMandatory);
    return options;
  }

  /**
   * Gets the available Financial Accounts and returns in a String the html code containing all the
   * Financial Accounts in the natural tree of the given organization filtered by the Payment
   * Method.
   * 
   * @param strPaymentMethodId
   *          optional Payment Method id to filter the Financial Accounts.
   * @param strFinancialAccountId
   *          the Financial Account id that will be selected by default in case it is present in the
   *          list.
   * @param strOrgId
   *          the Organization id the record belongs to.
   * @param strCurrencyId
   *          optional Currency id to filter the Financial Accounts.
   * @return a String with the html code with the options to fill the drop-down of Financial
   *         Accounts.
   */
  @Deprecated
  public static String getFinancialAccountList(String strPaymentMethodId,
      String strFinancialAccountId, String strOrgId, boolean isMandatory, String strCurrencyId) {
    List<FIN_FinancialAccount> financialAccounts = dao.getFilteredFinancialAccounts(
        strPaymentMethodId, strOrgId, strCurrencyId, AdvPaymentMngtDao.PaymentDirection.EITHER);
    String options = getOptionsList(financialAccounts, strFinancialAccountId, isMandatory);
    return options;
  }

  /**
   * Gets the available Financial Accounts and returns in a String the html code containing all the
   * Financial Accounts in the natural tree of the given organization filtered by the Payment
   * Method.
   * 
   * @param strPaymentMethodId
   *          optional Payment Method id to filter the Financial Accounts.
   * @param strFinancialAccountId
   *          the Financial Account id that will be selected by default in case it is present in the
   *          list.
   * @param strOrgId
   *          the Organization id the record belongs to.
   * @param strCurrencyId
   *          optional Currency id to filter the Financial Accounts.
   * @param isInPayment
   *          specifies the type of payment to that is being made. If true, will return accounts
   *          with payment methods that have Payment In enabled, if false will return accounts with
   *          payment methods that have Payment Out enabled.
   * @return a String with the html code with the options to fill the drop-down of Financial
   *         Accounts.
   */
  public static String getFinancialAccountList(String strPaymentMethodId,
      String strFinancialAccountId, String strOrgId, boolean isMandatory, String strCurrencyId,
      boolean isInPayment) {

    List<FIN_FinancialAccount> financialAccounts = dao.getFilteredFinancialAccounts(
        strPaymentMethodId, strOrgId, strCurrencyId,
        isInPayment ? AdvPaymentMngtDao.PaymentDirection.IN
            : AdvPaymentMngtDao.PaymentDirection.OUT);
    String options = getOptionsList(financialAccounts, strFinancialAccountId, isMandatory);
    return options;
  }

  /**
   * Returns a String containing the html code with the options based on the given List of
   * BaseOBObjects
   * 
   * @param <T>
   *          Class that extends BaseOBObject.
   * @param obObjectList
   *          List containing the values to be included in the options.
   * @param selectedValue
   *          value to set as selected by default.
   * @param isMandatory
   *          boolean to add a blank option in the options list.
   * @return a String containing the html code with the options. *
   */
  public static <T extends BaseOBObject> String getOptionsList(List<T> obObjectList,
      String selectedValue, boolean isMandatory) {
    StringBuilder strOptions = new StringBuilder();
    if (!isMandatory)
      strOptions.append("<option value=\"\"></option>");

    for (T obObject : obObjectList) {
      strOptions.append("<option value=\"").append(obObject.getId()).append("\"");
      if (obObject.getId().equals(selectedValue))
        strOptions.append(" selected=\"selected\"");
      strOptions.append(">");
      strOptions.append(escape(obObject.getIdentifier()));
      strOptions.append("</option>");
    }
    return strOptions.toString();
  }

  /**
   * Method to replace special characters to print properly in an html. Changes are: ">" to "&gt"
   * and "<" to "&lt"
   * 
   * @param toEscape
   *          String to be replaced.
   * @return the given String with the special characters replaced.
   */
  private static String escape(String toEscape) {
    String result = toEscape.replaceAll(">", "&gt;");
    result = result.replaceAll("<", "&lt;");
    return result;
  }

  /**
   * Method used to calculate the Day still due for the payment.
   * 
   * @param date
   *          . Due date of the payment.
   * @return dayStillDue. Calculated Day Still due.
   */
  public static Long getDaysToDue(Date date) {
    final Date now = DateUtils.truncate(new Date(), Calendar.DATE);
    return getDaysBetween(now, date);
  }

  /**
   * Returns the amount of days between two given dates
   * 
   * @param endDate
   * @param beginDate
   * @return
   */
  public static Long getDaysBetween(Date beginDate, Date endDate) {
    final TimeZone tz = TimeZone.getDefault();
    final long nowDstOffset = (tz.inDaylightTime(beginDate)) ? tz.getDSTSavings() : 0L;
    final long dateDstOffset = (tz.inDaylightTime(endDate)) ? tz.getDSTSavings() : 0L;
    return (endDate.getTime() + dateDstOffset - beginDate.getTime() - nowDstOffset)
        / DateUtils.MILLIS_PER_DAY;
  }

  public static boolean isAutomaticDepositWithdrawn(FIN_FinancialAccount account,
      FIN_PaymentMethod paymentMethod, boolean isReceipt) {
    FinAccPaymentMethod financialAccountPaymentMethod = new AdvPaymentMngtDao()
        .getFinancialAccountPaymentMethod(account, paymentMethod);
    if (financialAccountPaymentMethod == null)
      return false;
    return isReceipt ? financialAccountPaymentMethod.isAutomaticDeposit()
        : financialAccountPaymentMethod.isAutomaticWithdrawn();
  }

  public static boolean isAutomaticDepositWithdrawn(FIN_Payment payment) {
    return isAutomaticDepositWithdrawn(payment.getAccount(), payment.getPaymentMethod(),
        payment.isReceipt());
  }

  public static boolean isAutomaticDepositWithdrawn(FIN_PaymentProposal paymentProposal) {
    return isAutomaticDepositWithdrawn(paymentProposal.getAccount(),
        paymentProposal.getPaymentMethod(), paymentProposal.isReceipt());
  }

  /**
   * @see OBMessageUtils#messageBD(String)
   */
  public static String messageBD(String strCode) {
    return OBMessageUtils.messageBD(strCode);
  }

  /**
   * Generic OBCriteria.
   * 
   * @param clazz
   *          Class (entity).
   * @param setFilterClient
   *          If true then only objects from readable clients are returned, if false then objects
   *          from all clients are returned
   * @param setFilterOrg
   *          If true then when querying (for example call list()) a filter on readable
   *          organizations is added to the query, if false then this is not done
   * @param values
   *          Value. Property, value and operator.
   * @return All the records that satisfy the conditions.
   */
  public static <T extends BaseOBObject> List<T> getAllInstances(Class<T> clazz,
      boolean setFilterClient, boolean setFilterOrg, Value... values) {
    OBCriteria<T> obc = OBDal.getInstance().createCriteria(clazz);
    obc.setFilterOnReadableClients(setFilterClient);
    obc.setFilterOnReadableOrganization(setFilterOrg);
    for (Value value : values) {
      if (value.getValue() == null && "==".equals(value.getOperator())) {
        obc.add(Restrictions.isNull(value.getField()));
      } else if (value.getValue() == null && "!=".equals(value.getOperator())) {
        obc.add(Restrictions.isNotNull(value.getField()));
      } else if ("==".equals(value.getOperator())) {
        obc.add(Restrictions.eq(value.getField(), value.getValue()));
      } else if ("!=".equals(value.getOperator())) {
        obc.add(Restrictions.ne(value.getField(), value.getValue()));
      } else if ("<".equals(value.getOperator())) {
        obc.add(Restrictions.lt(value.getField(), value.getValue()));
      } else if (">".equals(value.getOperator())) {
        obc.add(Restrictions.gt(value.getField(), value.getValue()));
      } else if ("<=".equals(value.getOperator())) {
        obc.add(Restrictions.le(value.getField(), value.getValue()));
      } else if (">=".equals(value.getOperator())) {
        obc.add(Restrictions.ge(value.getField(), value.getValue()));
      } else {
        obc.add(Restrictions.eq(value.getField(), value.getValue()));
      }
    }
    return obc.list();
  }

  /**
   * Generic OBCriteria with filter on readable clients and organizations active.
   * 
   * @param clazz
   *          Class (entity).
   * @param values
   *          Value. Property, value and operator.
   * @return All the records that satisfy the conditions.
   */
  public static <T extends BaseOBObject> List<T> getAllInstances(Class<T> clazz, Value... values) {
    return getAllInstances(clazz, true, true, values);
  }

  /**
   * Generic OBCriteria.
   * 
   * @param clazz
   *          Class (entity).
   * @param values
   *          Value. Property, value and operator.
   * @return One record that satisfies the conditions.
   */
  public static <T extends BaseOBObject> T getOneInstance(Class<T> clazz, Value... values) {
    OBCriteria<T> obc = OBDal.getInstance().createCriteria(clazz);
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.add(Restrictions.ne(Client.PROPERTY_ID, "0"));
    for (Value value : values) {
      if (value.getValue() == null && "==".equals(value.getOperator())) {
        obc.add(Restrictions.isNull(value.getField()));
      } else if (value.getValue() == null && "!=".equals(value.getOperator())) {
        obc.add(Restrictions.isNotNull(value.getField()));
      } else if ("==".equals(value.getOperator())) {
        obc.add(Restrictions.eq(value.getField(), value.getValue()));
      } else if ("!=".equals(value.getOperator())) {
        obc.add(Restrictions.ne(value.getField(), value.getValue()));
      } else if ("<".equals(value.getOperator())) {
        obc.add(Restrictions.lt(value.getField(), value.getValue()));
      } else if (">".equals(value.getOperator())) {
        obc.add(Restrictions.gt(value.getField(), value.getValue()));
      } else if ("<=".equals(value.getOperator())) {
        obc.add(Restrictions.le(value.getField(), value.getValue()));
      } else if (">=".equals(value.getOperator())) {
        obc.add(Restrictions.ge(value.getField(), value.getValue()));
      } else {
        obc.add(Restrictions.eq(value.getField(), value.getValue()));
      }
    }

    final List<T> listt = obc.list();
    if (listt != null && listt.size() > 0) {
      return listt.get(0);
    } else {
      return null;
    }

  }

  public static BigDecimal getDepositAmount(Boolean isReceipt, BigDecimal amount) {
    BigDecimal deposit = BigDecimal.ZERO;
    if (isReceipt) {
      if (amount.compareTo(BigDecimal.ZERO) == 1) {
        deposit = amount;
      }
      // else received payment was negative so treat as payment
    } else {
      if (amount.compareTo(BigDecimal.ZERO) == -1) {
        // Negative payment out is a deposit
        deposit = amount.abs();
      }
    }
    return deposit;
  }

  public static BigDecimal getPaymentAmount(Boolean isReceipt, BigDecimal amount) {
    BigDecimal payment = BigDecimal.ZERO;
    if (isReceipt) {
      if (amount.compareTo(BigDecimal.ZERO) == -1) {
        // Negative payment in, treat as payment
        payment = amount.abs();
      }
    } else {
      if (amount.compareTo(BigDecimal.ZERO) == 1) {
        payment = amount;
      }
      // else sent payment was negative so treat as deposit
    }
    return payment;

  }

  /**
   * Convert a multi currency amount to a string for display in the UI. If amount has been converted
   * to a different currency, then output that converted amount and currency as well
   * 
   * @param amt
   *          Amount of payment
   * @param currency
   *          Currency payment was made in
   * @param convertedAmt
   *          Amount of payment in converted currency
   * @param convertedCurrency
   *          Currency payment was converted to/from
   * @return String version of amount formatted for display to user
   */
  public static String multiCurrencyAmountToDisplay(BigDecimal amt, Currency currency,
      BigDecimal convertedAmt, Currency convertedCurrency) {
    StringBuffer out = new StringBuffer();
    final UIDefinitionController.FormatDefinition formatDef = UIDefinitionController.getInstance()
        .getFormatDefinition("euro", "Edition");

    String formatWithDot = formatDef.getFormat();
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    try {
      dfs.setDecimalSeparator(formatDef.getDecimalSymbol().charAt(0));
      dfs.setGroupingSeparator(formatDef.getGroupingSymbol().charAt(0));
      // Use . as decimal separator
      final String DOT = ".";
      if (!DOT.equals(formatDef.getDecimalSymbol())) {
        formatWithDot = formatWithDot.replace(formatDef.getGroupingSymbol(), "@");
        formatWithDot = formatWithDot.replace(formatDef.getDecimalSymbol(), ".");
        formatWithDot = formatWithDot.replace("@", ",");
      }
    } catch (Exception e) {
      // If any error use euroEdition default format
      formatWithDot = "#0.00";
    }
    DecimalFormat amountFormatter = new DecimalFormat(formatWithDot, dfs);
    amountFormatter.setMaximumFractionDigits(currency.getStandardPrecision().intValue());

    out.append(amountFormatter.format(amt));
    if (convertedCurrency != null && !currency.equals(convertedCurrency)
        && amt.compareTo(BigDecimal.ZERO) != 0) {
      amountFormatter.setMaximumFractionDigits(convertedCurrency.getStandardPrecision().intValue());
      out.append(" (").append(amountFormatter.format(convertedAmt)).append(" ")
          .append(convertedCurrency.getISOCode()).append(")");
    }

    return out.toString();
  }

  /**
   * Determine the conversion rate from one currency to another on a given date. Will use the spot
   * conversion rate defined by the system for that date
   * 
   * @param fromCurrency
   *          Currency to convert from
   * @param toCurrency
   *          Currency being converted to
   * @param conversionDate
   *          Date conversion is being performed
   * @return A valid conversion rate for the parameters, or null if no conversion rate can be found
   */
  public static ConversionRate getConversionRate(Currency fromCurrency, Currency toCurrency,
      Date conversionDate, Organization org) {
    java.util.List<ConversionRate> conversionRateList;
    ConversionRate conversionRate;
    OBContext.setAdminMode(true);
    try {
      final OBCriteria<ConversionRate> obcConvRate = OBDal.getInstance().createCriteria(
          ConversionRate.class);
      obcConvRate.setFilterOnReadableOrganization(false);
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_ORGANIZATION, org));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, fromCurrency));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, toCurrency));
      obcConvRate.add(Restrictions.le(ConversionRate.PROPERTY_VALIDFROMDATE, conversionDate));
      obcConvRate.add(Restrictions.ge(ConversionRate.PROPERTY_VALIDTODATE, conversionDate));
      conversionRateList = obcConvRate.list();
      if ((conversionRateList != null) && (conversionRateList.size() != 0)) {
        conversionRate = conversionRateList.get(0);
      } else {
        if ("0".equals(org.getId())) {
          conversionRate = null;
        } else {
          return getConversionRate(
              fromCurrency,
              toCurrency,
              conversionDate,
              OBDal.getInstance().get(
                  Organization.class,
                  OBContext.getOBContext().getOrganizationStructureProvider()
                      .getParentOrg(org.getId())));
        }
      }
    } catch (Exception e) {
      log4j.error(e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
    return conversionRate;
  }

  public static int getConversionRatePrecision(VariablesSecureApp vars) {
    try {
      String formatOutput = vars.getSessionValue("#FormatOutput|generalQtyEdition", "#0.######");
      String decimalSeparator = ".";
      if (formatOutput.contains(decimalSeparator)) {
        formatOutput = formatOutput.substring(formatOutput.indexOf(decimalSeparator),
            formatOutput.length());
        return formatOutput.length() - decimalSeparator.length();
      } else {
        return 0;
      }
    } catch (Exception e) {
      log4j.error(e);
      return 6; // by default precision of 6 decimals as is defaulted in Format.xml
    }
  }

  /**
   * Formats a number using the given format, decimal and grouping separator.
   * 
   * @param number
   *          Number to be formatted.
   * @param javaFormat
   *          Java number format pattern.
   * @param _decimalSeparator
   *          Symbol used as decimal separator.
   * @param _groupingSeparator
   *          Symbol used as grouping separator.
   * @return Formatted string.
   */
  public static String formatNumber(BigDecimal number, String javaFormat, String _decimalSeparator,
      String _groupingSeparator) {
    if (StringUtils.isEmpty(javaFormat)) {
      return formatNumber(number);
    }
    String decimalSeparator = _decimalSeparator;
    String groupingSeparator = _groupingSeparator;
    if (StringUtils.isEmpty(decimalSeparator) || StringUtils.isEmpty(groupingSeparator)) {
      decimalSeparator = ".";
      groupingSeparator = ",";
    }
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    DecimalFormat dc;
    try {
      dfs.setDecimalSeparator(decimalSeparator.charAt(0));
      dfs.setGroupingSeparator(groupingSeparator.charAt(0));
      dc = new DecimalFormat(javaFormat, dfs);

    } catch (Exception e) {
      // If any error use euroEdition default format
      dc = new DecimalFormat("#0.00", dfs);
    }
    return dc.format(number);
  }

  /**
   * Formats a number using the euroEdition (see Format.xml) format.
   * 
   * @param number
   *          Number to be formatted.
   * @return Formatted string.
   */
  public static String formatNumber(BigDecimal number) {
    final UIDefinitionController.FormatDefinition formatDef = UIDefinitionController.getInstance()
        .getFormatDefinition("euro", "Edition");

    String formatWithDot = formatDef.getFormat();
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    DecimalFormat amountFormatter;
    try {
      dfs.setDecimalSeparator(formatDef.getDecimalSymbol().charAt(0));
      dfs.setGroupingSeparator(formatDef.getGroupingSymbol().charAt(0));
      // Use . as decimal separator
      final String DOT = ".";
      if (!DOT.equals(formatDef.getDecimalSymbol())) {
        formatWithDot = formatWithDot.replace(formatDef.getGroupingSymbol(), "@");
        formatWithDot = formatWithDot.replace(formatDef.getDecimalSymbol(), ".");
        formatWithDot = formatWithDot.replace("@", ",");
      }
      amountFormatter = new DecimalFormat(formatWithDot, dfs);
    } catch (Exception e) {
      // If any error use euroEdition default format
      amountFormatter = new DecimalFormat("#0.00", dfs);
    }
    return amountFormatter.format(number);
  }

  /**
   * Returns either the Invoice's Document Number or the Invoice's Supplier Reference based on the
   * Organization's configuration. In case the Supplier Reference is empty, the invoice's document
   * number is returned
   * 
   * @param organization
   *          to get its configuration. In case no configuration is available, the invoice's
   *          document number is returned
   * @param invoice
   * @return
   */
  public static String getDesiredDocumentNo(final Organization organization, final Invoice invoice) {
    String invoiceDocNo;
    try {
      // By default take the invoice document number
      invoiceDocNo = invoice.getDocumentNo();

      final String paymentDescription = organization.getOrganizationInformationList().get(0)
          .getAPRMPaymentDescription();
      // In case of a purchase invoice and the Supplier Reference is selected use Reference
      if (paymentDescription.equals("Supplier Reference") && !invoice.isSalesTransaction()) {
        invoiceDocNo = invoice.getOrderReference();
        if (invoiceDocNo == null) {
          invoiceDocNo = invoice.getDocumentNo();
        }
      }
    } catch (Exception e) {
      invoiceDocNo = invoice.getDocumentNo();
    }

    return invoiceDocNo;
  }

  /**
   * Returns if given payment status and related payment schedule detail belong to a confirmed
   * payment
   * 
   */
  public static boolean isPaymentConfirmed(String status, FIN_PaymentScheduleDetail psd) {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add(status);
    parameters.add((psd != null) ? psd.getId() : "");
    String result = (String) CallStoredProcedure.getInstance().call("APRM_ISPAYMENTCONFIRMED",
        parameters, null);

    return "Y".equals(result);
  }

  /**
   * Returns a list of Payment Status. If isConfirmed equals true, then the status returned are
   * confirmed payments. Else they are pending of execution
   * 
   */
  private static List<String> getListPaymentConfirmedOrNot(Boolean isConfirmed) {

    List<String> listPaymentConfirmedOrNot = new ArrayList<String>();
    OBContext.setAdminMode(true);
    try {
      final OBCriteria<org.openbravo.model.ad.domain.List> obCriteria = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.domain.List.class);
      obCriteria.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE + ".id",
          "575BCB88A4694C27BC013DE9C73E6FE7"));
      List<org.openbravo.model.ad.domain.List> adRefList = obCriteria.list();
      for (org.openbravo.model.ad.domain.List adRef : adRefList) {
        if (isConfirmed.equals(isPaymentConfirmed(adRef.getSearchKey(), null))) {
          listPaymentConfirmedOrNot.add(adRef.getSearchKey());
        }
      }
      return listPaymentConfirmedOrNot;
    } catch (Exception e) {
      log4j.error(e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a list confirmed Payment Status
   * 
   */
  public static List<String> getListPaymentConfirmed() {
    return getListPaymentConfirmedOrNot(true);
  }

  /**
   * Returns a list not confirmed Payment Status
   * 
   */
  public static List<String> getListPaymentNotConfirmed() {
    return getListPaymentConfirmedOrNot(false);
  }

  /**
   * Returns the legal entity of the given organization
   * 
   * @param org
   *          organization to get its legal entity
   * @return legal entity (with or without accounting) organization or null if not found
   */
  public static Organization getLegalEntityOrg(final Organization org) {
    try {
      OBContext.setAdminMode(true);
      final OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(org.getClient().getId());
      for (final String orgId : osp.getParentList(org.getId(), true)) {
        final Organization parentOrg = OBDal.getInstance().get(Organization.class, orgId);
        if (parentOrg.getOrganizationType().isLegalEntity()) {
          return parentOrg;
        }
      }
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
