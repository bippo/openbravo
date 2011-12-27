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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.model.ad.system.Client;

/**
 * Export reference data of clients defined by the clients parameter.
 * 
 */
public class ExportReferenceDataTask extends ReferenceDataTask {
  private static final Logger log = Logger.getLogger(ExportReferenceDataTask.class);

  private String clients;

  @Override
  protected void doExecute() {
    System.setProperty("line.separator", "\n");
    final File exportDir = getReferenceDataDir();
    for (final Client client : getClientObjects()) {
      final Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put(DataExportService.CLIENT_ID_PARAMETER_NAME, client.getId());

      final File exportFile = new File(exportDir, getExportFileName(client.getName()));
      if (exportFile.exists()) {
        exportFile.delete();
      }
      try {
        final OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(exportFile),
            "UTF-8");
        log.info("Exporting client " + client.getName());
        DataExportService.getInstance().exportClientToXML(parameters, false, fw);
        fw.close();
      } catch (final IOException e) {
        throw new OBException(e);
      }
    }
  }

  @Override
  public String getClients() {
    return clients;
  }

  @Override
  public void setClients(String clients) {
    this.clients = clients;
  }

  private List<Client> getClientObjects() {
    final List<Client> result = new ArrayList<Client>();
    for (final String clientStr : getClients().split(",")) {
      result.add(getClient(clientStr.trim()));
    }
    return result;
  }

  private Client getClient(String clientValue) {
    final org.openbravo.dal.service.OBCriteria<Client> obc = org.openbravo.dal.service.OBDal
        .getInstance().createCriteria(Client.class);
    obc.add(Restrictions.eq(Client.PROPERTY_SEARCHKEY, clientValue));
    final List<Client> result = obc.list();
    if (result.size() == 0) {
      throw new OBException("No client found using " + clientValue + " as the value in the query");
    }
    if (result.size() > 1) {
      throw new OBException("More than one client found using " + clientValue
          + " as the value in the query");
    }
    return result.get(0);
  }

  // replace everything except alphabetical characters
  private String getExportFileName(String clientName) {
    final char[] nameChars = clientName.toCharArray();
    for (int i = 0; i < nameChars.length; i++) {
      final char c = nameChars[i];
      // Only allow valid characters
      final boolean allowedChar = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
      if (!allowedChar) {
        nameChars[i] = '_';
      }
    }
    return new String(nameChars) + ".xml";
  }
}
