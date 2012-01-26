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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.manufacturing.cost.CostcenterVersion;
import org.openbravo.model.manufacturing.transaction.WorkRequirement;
import org.openbravo.model.manufacturing.transaction.WorkRequirementOperation;
import org.openbravo.model.materialmgmt.transaction.ProductionPlan;
import org.openbravo.model.materialmgmt.transaction.ProductionTransaction;
import org.openbravo.scheduling.ProcessBundle;

public class CreateWorkEffort implements org.openbravo.scheduling.Process {

  private static final Logger log4j = Logger.getLogger(CreateWorkEffort.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {

    final String strWorkRequirement = (String) bundle.getParams().get("MA_Workrequirement_ID");
    final String strdate = (String) bundle.getParams().get("date");
    String strStartTime = (String) bundle.getParams().get("starttime");
    String strEndTime = (String) bundle.getParams().get("endtime");
    final ConnectionProvider conn = bundle.getConnection();
    final VariablesSecureApp vars = bundle.getContext().toVars();

    try {

      // ConvertVariables
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");

      SimpleDateFormat dateformater = new SimpleDateFormat(dateFormat);
      SimpleDateFormat dateTimeformater = new SimpleDateFormat("yyyy-MM-dd");

      Date date = dateformater.parse(strdate);
      String dateformatTime = dateTimeformater.format(date);
      if (strStartTime == null || strStartTime.equals("")) {
        strStartTime = "00:00:00";
      }
      if (strEndTime == null || strEndTime.equals("")) {
        strEndTime = "00:00:00";
      }
      Timestamp starttime = Timestamp.valueOf(dateformatTime + " " + strStartTime + ".0");
      Timestamp endtime = Timestamp.valueOf(dateformatTime + " " + strEndTime + ".0");

      // Search Phases To Be Created
      WorkRequirement workReq = OBDal.getInstance().get(WorkRequirement.class, strWorkRequirement);

      OBCriteria<WorkRequirementOperation> workReqOpCriteria = OBDal.getInstance().createCriteria(
          WorkRequirementOperation.class);
      workReqOpCriteria.add(Restrictions.eq(WorkRequirementOperation.PROPERTY_WORKREQUIREMENT,
          workReq));
      workReqOpCriteria.add(Restrictions.le(WorkRequirementOperation.PROPERTY_STARTINGDATE, date));
      workReqOpCriteria.add(Restrictions.eq(WorkRequirementOperation.PROPERTY_CLOSED, false));
      workReqOpCriteria.addOrderBy(WorkRequirementOperation.PROPERTY_SEQUENCENUMBER, true);

      List<WorkRequirementOperation> workReqOpList = workReqOpCriteria.list();

      int counter = 0;
      for (WorkRequirementOperation wrOp : workReqOpList) {
        // Check if exits one not processed;

        OBCriteria<ProductionPlan> productionPlanCriteria = OBDal.getInstance().createCriteria(
            ProductionPlan.class);
        productionPlanCriteria.add(Restrictions.eq(ProductionPlan.PROPERTY_WRPHASE, wrOp));
        productionPlanCriteria.createAlias(ProductionPlan.PROPERTY_PRODUCTION, "pro");
        productionPlanCriteria.add(Restrictions.eq("pro."
            + ProductionTransaction.PROPERTY_MOVEMENTDATE, date));
        productionPlanCriteria.add(Restrictions.eq("pro."
            + ProductionTransaction.PROPERTY_PROCESSED, false));
        List<ProductionPlan> pplanList = productionPlanCriteria.list();

        if (pplanList.isEmpty()) {
          counter++;
          // Create ProductionTransaction
          ProductionTransaction productionTransaction = OBProvider.getInstance().get(
              ProductionTransaction.class);
          productionTransaction.setClient(wrOp.getClient());
          productionTransaction.setOrganization(wrOp.getOrganization());
          productionTransaction.setMovementDate(date);
          productionTransaction.setStartingTime(starttime);
          productionTransaction.setEndingTime(endtime);
          String documentNo = Utility.getDocumentNo(conn, wrOp.getClient().getId(), "M_Production",
              true);
          productionTransaction.setDocumentNo(documentNo);

          OBDal.getInstance().save(productionTransaction);
          OBDal.getInstance().flush();

          // Crete ProductionPlan
          ProductionPlan productionPlan = OBProvider.getInstance().get(ProductionPlan.class);
          productionPlan.setProduction(productionTransaction);
          productionPlan.setOrganization(productionTransaction.getOrganization());
          productionPlan.setClient(productionTransaction.getClient());
          // Only one line per ProductionTransaction
          productionPlan.setLineNo(10L);
          productionPlan.setWRPhase(wrOp);
          productionPlan.setProductionplandate(date);

          productionPlan.setRunTime(0L);
          productionPlan.setClosephase(false);
          BigDecimal requeriedQty = wrOp.getQuantity().subtract(wrOp.getCompletedQuantity());

          if (wrOp.isCreateStandards()) {
            productionPlan.setProductionQuantity(requeriedQty);
            BigDecimal estimatedTime = BigDecimal.ZERO;
            if (wrOp.getEstimatedTime() != null && wrOp.getQuantity() != null
                && wrOp.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
              estimatedTime = wrOp.getEstimatedTime().divide(wrOp.getQuantity())
                  .multiply(requeriedQty);
            }
            productionPlan.setEstimatedTime(estimatedTime.longValue());
          } else {
            productionPlan.setProductionQuantity(BigDecimal.ZERO);
            productionPlan.setEstimatedTime(0L);
          }

          productionPlan.setRejectedQuantity(0L);
          productionPlan.setCostCenterUse(BigDecimal.ZERO);

          productionPlan.setStartingTime(starttime);
          productionPlan.setEndingTime(endtime);

          productionPlan.setRequiredQuantity(requeriedQty.longValue());
          productionPlan.setProcessUnit(wrOp.getWorkRequirement().getProcessUnit());
          if (wrOp.getWorkRequirement().getConversionRate() != null
              && wrOp.getWorkRequirement().getConversionRate().compareTo(BigDecimal.ZERO) != 0) {
            productionPlan.setConversionRate(wrOp.getWorkRequirement().getConversionRate());
          }

          // Get CostCenterVersion
          OBCriteria<CostcenterVersion> costcenterVersionCriteria = OBDal.getInstance()
              .createCriteria(CostcenterVersion.class);
          costcenterVersionCriteria.add(Restrictions.eq(CostcenterVersion.PROPERTY_COSTCENTER, wrOp
              .getActivity().getCostCenter()));
          costcenterVersionCriteria.add(Restrictions.lt(CostcenterVersion.PROPERTY_VALIDFROMDATE,
              date));
          costcenterVersionCriteria.addOrderBy(CostcenterVersion.PROPERTY_VALIDFROMDATE, false);
          List<CostcenterVersion> costcenterVersionList = costcenterVersionCriteria.list();
          if (!costcenterVersionList.isEmpty()) {
            productionPlan.setCostCenterVersion(costcenterVersionList.get(0));
          }

          productionPlan.setOutsourced((wrOp.isOutsourced() == null) ? false : wrOp.isOutsourced());

          OBDal.getInstance().save(productionPlan);
          OBDal.getInstance().flush();

          if (wrOp.isCreateStandards()) {
            String strProcessId = "FF80818132A4F6AD0132A573DD7A0021";
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("M_ProductionPlan_ID", productionPlan.getId());
            ProcessBundle pb = new ProcessBundle(strProcessId, vars).init(conn);
            pb.setParams(params);
            new org.openbravo.erpCommon.ad_actionButton.CreateStandards().execute(pb);
            OBError pbResult = (OBError) pb.getResult();
            if (!pbResult.getType().equals("Success")) {
              throw new OBException(pbResult.getMessage());
            }
          }

        }

      }

      final OBError msg = new OBError();

      msg.setType("Success");
      msg.setTitle(Utility.messageBD(conn, "Success", bundle.getContext().getLanguage()));
      msg.setMessage(counter + " "
          + Utility.messageBD(conn, "WorkEffortCreated", bundle.getContext().getLanguage())
          + strdate);
      bundle.setResult(msg);
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log4j.error("Error creating work effort", e);
      final OBError msg = new OBError();
      msg.setType("Error");
      if (e instanceof org.hibernate.exception.GenericJDBCException) {
        msg.setMessage(((org.hibernate.exception.GenericJDBCException) e).getSQLException()
            .getNextException().getMessage());
      } else if (e instanceof org.hibernate.exception.ConstraintViolationException) {
        msg.setMessage(((org.hibernate.exception.ConstraintViolationException) e).getSQLException()
            .getNextException().getMessage());
      } else {
        msg.setMessage(e.getMessage());
      }
      msg.setTitle(Utility.messageBD(conn, "Error", bundle.getContext().getLanguage()));
      bundle.setResult(msg);
    }
  }

}
