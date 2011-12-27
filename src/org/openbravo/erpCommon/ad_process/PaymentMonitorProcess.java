package org.openbravo.erpCommon.ad_process;

import java.util.List;

import org.hibernate.LockOptions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.quartz.JobExecutionException;

public class PaymentMonitorProcess extends DalBaseProcess {

  private ProcessLogger logger;

  public void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    // Extra check for PaymentMonitor-disabling switch, to build correct message for users
    try {
      try {
        Preferences.getPreferenceValue("PaymentMonitor", true, null, null, OBContext.getOBContext()
            .getUser(), null, null);
        logger
            .log("There is an extension module installed managing the Payment Monitor information.\n");
        logger.log("Core's background process is not executed.\n");
        return;
      } catch (PropertyNotFoundException e) {
        logger.log("Starting Update Paid Amount for Invoices Background Process.\n");
      }
    } catch (PropertyException e) {
      logger.log("PropertyException, there is a conflict for PaymentMonitor property\n");
      return;
    }
    try {
      int counter = 0;
      String whereClause = " as inv where inv.totalPaid <> inv.grandTotalAmount and inv.processed=true";

      final OBQuery<Invoice> obqParameters = OBDal.getInstance().createQuery(Invoice.class,
          whereClause);
      // For Background process execution at system level
      if (OBContext.getOBContext().isInAdministratorMode()) {
        obqParameters.setFilterOnReadableClients(false);
        obqParameters.setFilterOnReadableOrganization(false);
      }
      final List<Invoice> invoices = obqParameters.list();
      for (Invoice invoice : invoices) {
        OBDal.getInstance().getSession().buildLockRequest(LockOptions.NONE)
            .lock(Invoice.ENTITY_NAME, invoice);
        PaymentMonitor.updateInvoice(invoice);
        counter++;
        OBDal.getInstance().getSession().flush();
        OBDal.getInstance().getSession().clear();
        if (counter % 50 == 0) {
          logger.log("Invoices updated: " + counter + "\n");
        }
      }
      if (counter % 50 != 0)
        logger.log("Invoices updated: " + counter + "\n");
    } catch (Exception e) {
      // catch any possible exception and throw it as a Quartz
      // JobExecutionException
      throw new JobExecutionException(e.getMessage(), e);
    }

  }
}