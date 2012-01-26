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
package org.openbravo.erpCommon.utility;

import java.sql.Connection;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.utils.Replace;

/**
 * 
 * Create a new attribute set instance value.
 * 
 */
public class AttributeSetInstanceValue {

  private String lot = "";
  private String serno = "";
  private String guaranteedate = "";
  private String locked = "N";
  private String lockDescription = "";
  private String attSetInstanceId = "";

  protected Logger log4j = Logger.getLogger(this.getClass());

  public AttributeSetInstanceValue() {
  }

  public AttributeSetInstanceValue(String strlot, String strserno, String strguaranteedate,
      String strlocked, String strlockDescription) {
    this.lot = strlot == null ? "" : strlot;
    this.serno = strserno == null ? "" : strserno;
    this.guaranteedate = strguaranteedate == null ? "" : strguaranteedate;
    this.locked = strlocked == null ? "" : strlocked;
    this.lockDescription = strlockDescription == null ? "" : strlockDescription;

  }

  public void setLot(String _data) {
    this.lot = _data == null ? "" : _data;
  }

  public String getLot() {
    return ((this.lot == null) ? "" : this.lot);
  }

  public void setSerialNumber(String _data) {
    this.serno = _data == null ? "" : _data;
  }

  public String getSerialNumber() {
    return ((this.serno == null) ? "" : this.serno);
  }

  public void setGuaranteeDate(String _data) {
    this.guaranteedate = _data == null ? "" : _data;
  }

  public String getGuaranteeDate() {
    return ((this.guaranteedate == null) ? "" : this.guaranteedate);
  }

  public void setLockDescription(String _data) {
    this.lockDescription = _data == null ? "" : _data;
  }

  public String getLockDescription() {
    return ((this.lockDescription == null) ? "" : this.lockDescription);
  }

  public void setLocked(String _data) {
    this.locked = _data == null ? "" : _data;
  }

  public String getLocked() {
    return ((this.locked == null) ? "" : this.locked);
  }

  public String getAttSetInstanceId() {
    return ((this.attSetInstanceId == null) ? "" : this.attSetInstanceId);
  }

  /**
   * Checks if the record has attachments associated.
   * 
   * @param conProv
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param data
   *          AttributeSetInstanceValueData with the attribute set info.
   * @param strAttributeSet
   *          String with the record attributeSetId.
   * @param strInstance
   *          String with the instanceId.
   * @param strWindow
   *          String with the windowId.
   * @param strIsSOTrx
   *          String with the isSotrx.
   * @param strProduct
   *          String with the productId.
   * @param attributeValues
   *          Map with the attribute values.
   * @return OBError with the result.
   * @throws ServletException
   */
  public OBError setAttributeInstance(ConnectionProvider conProv, VariablesSecureApp vars,
      String strAttributeSet, String strInstance, String strWindow, String strIsSOTrx,
      String strProduct, Map<String, String> attributeValues) throws ServletException {

    String strNewInstance = "";
    AttributeSet attset = OBDal.getInstance().get(AttributeSet.class, strAttributeSet);

    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    myMessage.setType("Success");
    myMessage.setMessage(Utility.messageBD(conProv, "Success", vars.getLanguage()));
    AttributeSetInstanceValueData[] data = AttributeSetInstanceValueData.select(conProv,
        strAttributeSet);
    if (data == null || data.length == 0) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(conProv, "FindZeroRecords", vars.getLanguage()));
      return myMessage;
    }

    boolean isinstance = !AttributeSetInstanceValueData.isInstanceAttribute(conProv,
        strAttributeSet).equals("0");
    String strDescription = getDescription(conProv, vars, data, strIsSOTrx, strWindow,
        attributeValues);
    Connection conn = null;
    try {
      conn = conProv.getTransactionConnection();
      String description = "", description_first = "";
      if (data[0].islot.equals("Y")) {
        if (!data[0].mLotctlId.equals("") && (strIsSOTrx.equals("N") || strWindow.equals("191"))) {
          lot = AttributeSetInstanceValueData.selectNextLot(conProv, data[0].mLotctlId);
          AttributeSetInstanceValueData.updateLotSequence(conn, conProv, vars.getUser(),
              data[0].mLotctlId);
          description_first += (description_first.equals("") ? "" : "_") + lot;
        } else {
          description_first += (description_first.equals("") ? "" : "_") + "L" + lot;
        }
      }
      if (data[0].isserno.equals("Y")) {
        if (!data[0].mSernoctlId.equals("") && (strIsSOTrx.equals("N") || strWindow.equals("191"))) {
          serno = AttributeSetInstanceValueData.selectNextSerNo(conn, conProv, data[0].mSernoctlId);
          AttributeSetInstanceValueData.updateSerNoSequence(conn, conProv, vars.getUser(),
              data[0].mSernoctlId);
          description_first += (description_first.equals("") ? "" : "_") + serno;
        } else {
          description_first += (description_first.equals("") ? "" : "_") + "#" + serno;
        }
      }
      if (data[0].isguaranteedate.equals("Y")) {
        description_first += (description_first.equals("") ? "" : "_") + guaranteedate;
      }
      if (data[0].islockable.equals("Y")) {
        description_first += (description_first.equals("") ? "" : "_") + lockDescription;
      }
      if (!isinstance) {
        strNewInstance = AttributeSetInstanceValueData.hasIdentical(conProv, strDescription,
            data[0].mAttributesetId);
      }
      boolean hasToUpdate = false;
      if ((!strInstance.equals("")) && (isinstance)) {
        // if it's existent and requestable, it edits it
        hasToUpdate = true;
        if (AttributeSetInstanceValueData.updateHeader(conn, conProv, vars.getUser(),
            data[0].mAttributesetId, serno, lot, guaranteedate, "", locked, lockDescription,
            strInstance) == 0) {
          AttributeSetInstanceValueData.insertHeader(conn, conProv, strInstance, attset.getClient()
              .getId(), attset.getOrganization().getId(), vars.getUser(), data[0].mAttributesetId,
              serno, lot, guaranteedate, "", locked, lockDescription);
        }
      } else if ((isinstance) || (strNewInstance.equals(""))) { // New or
        // editable,if it's requestable or doesn't exist the same one, then it inserts a new one
        hasToUpdate = true;
        strNewInstance = SequenceIdData.getUUID();
        AttributeSetInstanceValueData.insertHeader(conn, conProv, strNewInstance, attset
            .getClient().getId(), attset.getOrganization().getId(), vars.getUser(),
            data[0].mAttributesetId, serno, lot, guaranteedate, "", locked, lockDescription);
      }
      if (hasToUpdate) {
        if (!data[0].elementname.equals("")) {
          for (int i = 0; i < data.length; i++) {
            String strValue = attributeValues.get(replace(data[i].elementname));
            if ((strValue == null || strValue.equals("")) && data[i].ismandatory.equals("Y")) {
              throw new ServletException("Request parameter required: "
                  + replace(data[i].elementname));
            }
            if (strValue == null)
              strValue = "";
            String strDescValue = strValue;
            if (data[i].islist.equals("Y"))
              strDescValue = AttributeSetInstanceValueData.selectAttributeValue(conProv, strValue);
            if (!strNewInstance.equals("")) {
              if (AttributeSetInstanceValueData.update(conn, conProv, vars.getUser(),
                  (data[i].islist.equals("Y") ? strValue : ""), strDescValue, strNewInstance,
                  data[i].mAttributeId) == 0) {
                String strNewAttrInstance = SequenceIdData.getUUID();
                AttributeSetInstanceValueData.insert(conn, conProv, strNewAttrInstance,
                    strNewInstance, data[i].mAttributeId, attset.getClient().getId(), attset
                        .getOrganization().getId(), vars.getUser(),
                    (data[i].islist.equals("Y") ? strValue : ""), strDescValue);
              }
            } else {
              if (AttributeSetInstanceValueData.update(conn, conProv, vars.getUser(),
                  (data[i].islist.equals("Y") ? strValue : ""), strDescValue, strInstance,
                  data[i].mAttributeId) == 0) {
                String strNewAttrInstance = SequenceIdData.getUUID();
                AttributeSetInstanceValueData.insert(conn, conProv, strNewAttrInstance,
                    strInstance, data[i].mAttributeId, attset.getClient().getId(), attset
                        .getOrganization().getId(), vars.getUser(),
                    (data[i].islist.equals("Y") ? strValue : ""), strDescValue);
              }
            }
            description += (description.equals("") ? "" : "_") + strDescValue;
          }
        }
        if (!description_first.equals(""))
          description += (description.equals("") ? "" : "_") + description_first;
        AttributeSetInstanceValueData.updateHeaderDescription(conn, conProv, vars.getUser(),
            description, (strNewInstance.equals("") ? strInstance : strNewInstance));
      }
      conProv.releaseCommitConnection(conn);
      this.attSetInstanceId = (strNewInstance.equals("") ? strInstance : strNewInstance);
    } catch (Exception e) {
      try {
        conProv.releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      log4j.error("Rollback in transaction: ", e);
    }

    return myMessage;
  }

  private String replace(String strIni) {
    // delete characters: " ","&",","
    return Replace.replace(Replace.replace(Replace.replace(
        Replace.replace(Replace.replace(Replace.replace(strIni, "#", ""), " ", ""), "&", ""), ",",
        ""), "(", ""), ")", "");
  }

  private String getDescription(ConnectionProvider conProv, VariablesSecureApp vars,
      AttributeSetInstanceValueData[] data, String strIsSOTrx, String strWindowId,
      Map<String, String> attributeValues) {
    if (data == null || data.length == 0)
      return "";
    String description = "";
    try {
      // AttributeSet header
      String description_first = "";
      if (data[0].islot.equals("Y")) {
        if (!data[0].mLotctlId.equals("") && (strIsSOTrx.equals("N") || strWindowId.equals("191"))) {
          description_first += (description_first.equals("") ? "" : "_") + lot;
        } else
          description_first += (description_first.equals("") ? "" : "_") + "L" + lot;
      }
      if (data[0].isserno.equals("Y")) {
        if (!data[0].mSernoctlId.equals("")
            && (strIsSOTrx.equals("N") || strWindowId.equals("191"))) {
          description_first += (description_first.equals("") ? "" : "_") + serno;
        } else
          description_first += (description_first.equals("") ? "" : "_") + "#" + serno;
      }
      if (data[0].isguaranteedate.equals("Y")) {
        description_first += (description_first.equals("") ? "" : "_") + guaranteedate;
      }
      if (data[0].islockable.equals("Y")) {
        description_first += (description_first.equals("") ? "" : "_") + lockDescription;
      }

      if (!data[0].elementname.equals("")) {
        for (int i = 0; i < data.length; i++) {
          String strValue = attributeValues.get(replace(data[i].elementname));
          if ((strValue == null || strValue.equals("")) && data[i].ismandatory.equals("Y")) {
            throw new ServletException("Request parameter required: "
                + replace(data[i].elementname));
          }
          if (strValue == null)
            strValue = "";
          String strDescValue = strValue;
          if (data[i].islist.equals("Y"))
            strDescValue = AttributeSetInstanceValueData.selectAttributeValue(conProv, strValue);
          description += (description.equals("") ? "" : "_") + strDescValue;
        }
      }
      if (!description_first.equals(""))
        description += (description.equals("") ? "" : "_") + description_first;
    } catch (ServletException e) {
      return "";
    }
    return description;
  }

}
