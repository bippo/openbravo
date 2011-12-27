/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2010 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.Serializable;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public final class AcctSchemaElement implements Serializable {
  private static final long serialVersionUID = 1L;
  static Logger log4jAcctSchemaElement = Logger.getLogger(AcctSchemaElement.class);

  public AcctSchemaElement(String id, String seqNo, String name, String segmentType,
      String C_Element_ID, String defaultValue, String mandatory, String balanced) {
    m_ID = id;
    m_seqNo = seqNo;
    m_name = name;
    m_segmentType = segmentType;
    m_C_Element_ID = C_Element_ID;
    m_defaultValue = defaultValue;
    m_mandatory = mandatory;
    m_balanced = balanced;
  }

  public static ArrayList<Object> getAcctSchemaElementList(ConnectionProvider conn,
      String C_AcctSchema_ID) {
    log4jAcctSchemaElement.debug("AcctSchamaElement.getAcctSchemaElementList - " + C_AcctSchema_ID);
    ArrayList<Object> list = new ArrayList<Object>();
    AcctSchemaElementData[] data;
    try {
      data = AcctSchemaElementData.select(conn, C_AcctSchema_ID);
      AcctSchemaElement e;
      for (int i = 0; i < data.length; i++) {
        String id = data[i].cAcctschemaElementId;
        String seqNo = data[i].seqno;
        String name = data[i].name;
        String segmentType = data[i].elementtype;
        String C_Element_ID = data[i].cElementId;
        String mandatory = data[i].ismandatory;
        String balanced = data[i].isbalanced;
        String defaultValue = "";
        // FIXME: For the sake of clarity we should be SEGMENT_Org
        // and String SEGMENT_Account for the next, etc. What's the
        // point
        // defining constants that we do not use
        if (segmentType.equals("OO"))
          defaultValue = data[i].orgId;
        else if (segmentType.equals("AC"))
          defaultValue = data[i].cElementvalueId;
        else if (segmentType.equals("BP"))
          defaultValue = data[i].cBpartnerId;
        else if (segmentType.equals("PR"))
          defaultValue = data[i].mProductId;
        else if (segmentType.equals("AY"))
          defaultValue = data[i].cActivityId;
        else if (segmentType.equals("LF"))
          defaultValue = data[i].cLocationId;
        else if (segmentType.equals("LT"))
          defaultValue = data[i].cLocationId;
        else if (segmentType.equals("MC"))
          defaultValue = data[i].cCampaignId;
        else if (segmentType.equals("OT"))
          defaultValue = data[i].orgId;
        else if (segmentType.equals("PJ"))
          defaultValue = data[i].cProjectId;
        else if (segmentType.equals("SR"))
          defaultValue = data[i].cSalesregionId;
        else if (segmentType.equals("U1"))
          defaultValue = data[i].cElementvalueId;
        else if (segmentType.equals("U2"))
          defaultValue = data[i].cElementvalueId;
        log4jAcctSchemaElement.debug(seqNo + " " + name + " " + segmentType + "=" + defaultValue);
        if (mandatory.equals("Y") && defaultValue.equals(""))
          log4jAcctSchemaElement
              .warn("AcctSchameElement.getAcctSchemaElementList - No default value for " + name);
        e = new AcctSchemaElement(id, seqNo, name, segmentType, C_Element_ID, defaultValue,
            mandatory, balanced);
        list.add(e);
      }
    } catch (ServletException e) {
      log4jAcctSchemaElement.warn("AcctSchemaElement.getAcctSchemaElementList", e);
    }
    return list;
  }

  public String m_ID;
  public String m_seqNo;
  public String m_name;
  public String m_segmentType;
  public String m_C_Element_ID;
  public String m_defaultValue;
  public String m_mandatory;
  public String m_balanced = "N";
  public String m_active;
  public static final String SEGMENT_Org = "OO";
  public static final String SEGMENT_Account = "AC";
  public static final String SEGMENT_BPartner = "BP";
  public static final String SEGMENT_Product = "PR";
  public static final String SEGMENT_Activity = "AY";
  public static final String SEGMENT_LocationFrom = "LF";
  public static final String SEGMENT_LocationTo = "LT";
  public static final String SEGMENT_Campaign = "MC";
  public static final String SEGMENT_OrgTrx = "OT";
  public static final String SEGMENT_Project = "PJ";
  public static final String SEGMENT_SalesRegion = "SR";
  public static final String SEGMENT_User1 = "U1";
  public static final String SEGMENT_User2 = "U2";
}
