package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RegexFilter;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;

public class SL_AdvPayment_Document extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String strWindowNo = info.getWindowId();
    String strTableNameId = info.getStringParameter("inpkeyColumnId", new RegexFilter(
        "[a-zA-Z0-9_]*_ID"));
    String strDocType_Id = info.getStringParameter("inpcDoctypeId", IsIDFilter.instance);
    String strTableName = strTableNameId.substring(0, strTableNameId.length() - 3);
    String strDocumentNo = Utility.getDocumentNo(this, vars, strWindowNo, strTableName,
        strDocType_Id, strDocType_Id, false, false);
    info.addResult("inpdocumentno", "<" + strDocumentNo + ">");
  }

}
