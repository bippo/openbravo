//Sqlc generated V1.O00-1
package org.openbravo.advpaymentmngt.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class CreateAccountingConfigurationData implements FieldProvider {
static Logger log4j = Logger.getLogger(CreateAccountingConfigurationData.class);
  private String InitRecordNumber="0";
  public String cAcctschemaId;
  public String adClientId;
  public String adOrgId;
  public String adTableId;
  public String name;
  public String cPeriodId;
  public String value;
  public String status;
  public String isdefaultacct;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("C_ACCTSCHEMA_ID") || fieldName.equals("cAcctschemaId"))
      return cAcctschemaId;
    else if (fieldName.equalsIgnoreCase("AD_CLIENT_ID") || fieldName.equals("adClientId"))
      return adClientId;
    else if (fieldName.equalsIgnoreCase("AD_ORG_ID") || fieldName.equals("adOrgId"))
      return adOrgId;
    else if (fieldName.equalsIgnoreCase("AD_TABLE_ID") || fieldName.equals("adTableId"))
      return adTableId;
    else if (fieldName.equalsIgnoreCase("NAME"))
      return name;
    else if (fieldName.equalsIgnoreCase("C_PERIOD_ID") || fieldName.equals("cPeriodId"))
      return cPeriodId;
    else if (fieldName.equalsIgnoreCase("VALUE"))
      return value;
    else if (fieldName.equalsIgnoreCase("STATUS"))
      return status;
    else if (fieldName.equalsIgnoreCase("ISDEFAULTACCT"))
      return isdefaultacct;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static CreateAccountingConfigurationData[] selectAcctSchema(ConnectionProvider connectionProvider)    throws ServletException {
    return selectAcctSchema(connectionProvider, 0, 0);
  }

  public static CreateAccountingConfigurationData[] selectAcctSchema(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "    SELECT C_AcctSchema_ID, ad_client_id, '' as ad_org_id, '' as ad_table_id, '' as name, '' as c_period_id, '' as value, '' as status," +
      "    '' as isdefaultacct" +
      "    FROM C_AcctSchema";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        CreateAccountingConfigurationData objectCreateAccountingConfigurationData = new CreateAccountingConfigurationData();
        objectCreateAccountingConfigurationData.cAcctschemaId = UtilSql.getValue(result, "C_ACCTSCHEMA_ID");
        objectCreateAccountingConfigurationData.adClientId = UtilSql.getValue(result, "AD_CLIENT_ID");
        objectCreateAccountingConfigurationData.adOrgId = UtilSql.getValue(result, "AD_ORG_ID");
        objectCreateAccountingConfigurationData.adTableId = UtilSql.getValue(result, "AD_TABLE_ID");
        objectCreateAccountingConfigurationData.name = UtilSql.getValue(result, "NAME");
        objectCreateAccountingConfigurationData.cPeriodId = UtilSql.getValue(result, "C_PERIOD_ID");
        objectCreateAccountingConfigurationData.value = UtilSql.getValue(result, "VALUE");
        objectCreateAccountingConfigurationData.status = UtilSql.getValue(result, "STATUS");
        objectCreateAccountingConfigurationData.isdefaultacct = UtilSql.getValue(result, "ISDEFAULTACCT");
        objectCreateAccountingConfigurationData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectCreateAccountingConfigurationData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    CreateAccountingConfigurationData objectCreateAccountingConfigurationData[] = new CreateAccountingConfigurationData[vector.size()];
    vector.copyInto(objectCreateAccountingConfigurationData);
    return(objectCreateAccountingConfigurationData);
  }

  public static boolean selectTables(ConnectionProvider connectionProvider, String acctSchemaId, String tableId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        select count(*) as name" +
      "        from c_acctschema_table where c_acctschema_id = ?" +
      "        and ad_table_id = ?";

    ResultSet result;
    boolean boolReturn = false;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, acctSchemaId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, tableId);

      result = st.executeQuery();
      if(result.next()) {
        boolReturn = !UtilSql.getValue(result, "NAME").equals("0");
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(boolReturn);
  }

  public static int insertAcctSchemaTable(Connection conn, ConnectionProvider connectionProvider, String acctSchemaId, String tableId, String clientId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      INSERT INTO c_acctschema_table(" +
      "            c_acctschema_table_id, c_acctschema_id, ad_table_id, ad_client_id," +
      "            ad_org_id, isactive, created, createdby, updated, updatedby," +
      "            ad_createfact_template_id, acctdescription)" +
      "    VALUES (get_uuid(), ?, ?, ?," +
      "            '0', 'Y', now(), '100', now(), '100'," +
      "            null, null)";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, acctSchemaId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, tableId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, clientId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int insertPeriodControl(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      INSERT INTO c_periodcontrol(" +
      "            c_periodcontrol_id, ad_client_id, ad_org_id, isactive, created," +
      "            createdby, updated, updatedby, c_period_id, docbasetype, periodstatus," +
      "            periodaction, processing)" +
      "      select get_uuid(), c_period.ad_client_id as ad_client_id, a.ad_org_id, 'Y', now()," +
      "      '100', now(), '100', c_period.c_period_id, ad_ref_list.value, coalesce(max(periodstatus),'C') as status," +
      "      'N', 'N'" +
      "      from ad_ref_list, c_year, c_period left join c_periodcontrol on c_period.c_period_id = c_periodcontrol.c_period_id, (select ad_org_id from ad_org where isperiodcontrolallowed = 'Y') a" +
      "      where ad_reference_id = '183'" +
      "      and ad_ref_list.isactive='Y'" +
      "      and c_period.c_year_id = c_year.c_year_id" +
      "      and c_calendar_id = (select c_calendar_id from ad_org" +
      "            where ad_org_id = ad_org_getcalendarowner(a.ad_org_id))" +
      "      and not exists (select 1 from c_periodcontrol" +
      "            where c_periodcontrol.c_period_id = c_period.c_period_id" +
      "            and c_periodcontrol.docbasetype = ad_ref_list.value" +
      "            and c_periodcontrol.ad_org_id = a.ad_org_id)" +
      "      group by c_period.ad_client_id, c_period.c_period_id, ad_ref_list.value, a.ad_org_id";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int deleteTableAccess(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      DELETE FROM ad_table_access" +
      "      WHERE ad_table_id = '4D8C3B3C31D1410DA046140C9F024D17'" +
      "        AND isreadonly = 'Y'" +
      "        AND isexclude = 'N'" +
      "        AND created <= (SELECT created FROM ad_tab WHERE ad_tab_id = 'FF8080812F213146012F2135BC25000E')";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static int updateTableDocType(Connection conn, ConnectionProvider connectionProvider)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "      update c_doctype set ad_table_id = 'D1A97202E832470285C9B1EB026D54E2'" +
      "      where docbasetype in ('ARR', 'APP')";

    int updateCount = 0;
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(conn, strSql);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releaseTransactionalPreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }
}
