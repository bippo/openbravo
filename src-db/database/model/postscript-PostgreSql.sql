
CREATE OR REPLACE FUNCTION ad_script_drop_recreate_index(p_seqNoStart numeric)
  RETURNS numeric AS
$BODY$ DECLARE
/*************************************************************************
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
* All portions are Copyright (C) 2001-2008 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
 v_seqNo      NUMERIC := p_seqNoStart;
 v_strTemp VARCHAR(4000):='';
 v_strSql VARCHAR(4000):='';

 Cur_UniqueIndex RECORD;
 Cur_IndexColumns RECORD;
BEGIN
    FOR Cur_UniqueIndex IN (SELECT i.INDEX_NAME, i.TABLE_NAME, i.TABLESPACE_NAME, CONSTRAINT_TYPE
                 FROM USER_INDEXES I left join USER_CONSTRAINTS C1 on c1.INDEX_NAME=I.INDEX_NAME
                 WHERE UNIQUENESS='UNIQUE' AND INDEX_TYPE='NORMAL' AND TABLE_TYPE='TABLE'
               --AND CONSTRAINT_TYPE != 'U'
               ORDER BY INDEX_NAME)

    LOOP
      v_seqNo:=v_seqNo + 1;
      INSERT INTO AD_SCRIPT_SQL VALUES (v_seqNo, 'DROP INDEX '||Cur_UniqueIndex.INDEX_NAME) ;

      IF Cur_UniqueIndex.CONSTRAINT_TYPE != 'P' THEN
           v_strSql:='CREATE INDEX '||Cur_UniqueIndex.INDEX_NAME||' ON '||Cur_UniqueIndex.TABLE_NAME||'(';
           v_strTemp:='';
           FOR Cur_IndexColumns IN
             (SELECT COLUMN_NAME
           FROM USER_IND_COLUMNS
           WHERE INDEX_NAME=Cur_UniqueIndex.INDEX_NAME
           ORDER BY COLUMN_POSITION)
           LOOP
             v_strTemp:=v_strTemp ||','|| Cur_IndexColumns.COLUMN_NAME;
           END LOOP;
           v_strSql:=v_strSql || SUBSTR(v_strTemp, 2, 4000) || ') ';
           INSERT INTO AD_SCRIPT_SQL VALUES(v_seqNo+150000, v_strSql) ;
       END IF;
    END LOOP;
 RETURN v_seqNo;
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION ad_script_execute(param_Message VARCHAR)
  RETURNS varchar AS
$BODY$ DECLARE
/*************************************************************************
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
* All portions are Copyright (C) 2001-2008 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
 v_Message       VARCHAR(4000) := '';
 v_ResultStr     VARCHAR(2000) := '';
 Cur_Script RECORD;
BEGIN
 v_Message := param_Message;
 FOR Cur_Script IN (SELECT STRSQL FROM AD_SCRIPT_SQL ORDER BY SEQNO) LOOP
  BEGIN
    RAISE NOTICE '%', Cur_Script.STRSQL;
    EXECUTE(Cur_Script.STRSQL);
  EXCEPTION
     WHEN OTHERS THEN
        IF (LENGTH(v_ResultStr || ': ' || SQLERRM || ' - ' ) < 1980) THEN
          v_ResultStr := v_ResultStr || ': ' || SQLERRM || ' - ';
        END IF;
       RAISE NOTICE '%',SQLERRM;
  END;
 END LOOP;
 IF( LENGTH(v_ResultStr) > 0 ) THEN
    RAISE NOTICE '%', 'Script errors: ' || v_ResultStr;
 END IF;
 return substr(coalesce(v_ResultStr,'') || coalesce(v_Message,''), 1, 2000);
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION AD_GET_DOC_LE_BU(p_header_table character varying, p_document_id character varying, p_header_column_id character varying, p_type character varying)
 RETURNS varchar AS
$BODY$ DECLARE
/*************************************************************************
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
* All portions are Copyright (C) 2009 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
   v_org_header_id ad_org.ad_org_id%TYPE;
   v_isbusinessunit ad_orgtype.isbusinessunit%TYPE;
   v_islegalentity ad_orgtype.islegalentity%TYPE;
   
 BEGIN
 
   -- Gets the organization and the organization type of the document's header
   EXECUTE 
     'SELECT ad_org.ad_org_id, ad_orgtype.isbusinessunit, ad_orgtype.islegalentity 
     FROM '||p_header_table||', ad_org, ad_orgtype
     WHERE '||p_header_table||'.'||p_header_column_id||'='||''''||p_document_id||''''||' 
     AND ad_org.ad_orgtype_id = ad_orgtype.ad_orgtype_id
     AND '||p_header_table||'.ad_org_id=ad_org.ad_org_id' 
     INTO v_org_header_id, v_isbusinessunit, v_islegalentity;
 
   -- Gets recursively the organization parent until finding a Business Unit or a Legal Entity
   IF (p_type IS NULL) THEN
	   WHILE (v_isbusinessunit='N' AND v_islegalentity='N') LOOP
	     SELECT hh.parent_id, ad_orgtype.isbusinessunit, ad_orgtype.islegalentity
	     INTO v_org_header_id, v_isbusinessunit, v_islegalentity
	     FROM ad_org, ad_orgtype, ad_treenode pp, ad_treenode hh
	     WHERE pp.node_id = hh.parent_id
	     AND hh.ad_tree_id = pp.ad_tree_id
	     AND pp.node_id=ad_org.ad_org_id
	     AND hh.node_id=v_org_header_id
	     AND ad_org.ad_orgtype_id=ad_orgtype.ad_orgtype_id
	     AND ad_org.isready='Y'
	     AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id and hh.ad_client_id=ad_tree.ad_client_id);     
	   END LOOP;
   -- Gets recursively the organization parent until finding a Legal Entity
    ELSIF (p_type='LE') THEN
       WHILE (v_islegalentity='N') LOOP
         SELECT hh.parent_id, ad_orgtype.islegalentity
         INTO v_org_header_id, v_islegalentity
         FROM ad_org, ad_orgtype, ad_treenode pp, ad_treenode hh
         WHERE pp.node_id = hh.parent_id
         AND hh.ad_tree_id = pp.ad_tree_id
         AND pp.node_id=ad_org.ad_org_id
         AND hh.node_id=v_org_header_id
         AND ad_org.ad_orgtype_id=ad_orgtype.ad_orgtype_id
         AND ad_org.isready='Y'
         AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id and hh.ad_client_id=ad_tree.ad_client_id);     
       END LOOP;
    -- Gets recursively the organization parent until finding a Business Unit
    ELSIF (p_type='BU') THEN
       WHILE (v_isbusinessunit='N' AND v_org_header_id<>'0') LOOP
         SELECT hh.parent_id, ad_orgtype.isbusinessunit
         INTO v_org_header_id, v_isbusinessunit
         FROM ad_org, ad_orgtype, ad_treenode pp, ad_treenode hh
         WHERE pp.node_id = hh.parent_id
         AND hh.ad_tree_id = pp.ad_tree_id
         AND pp.node_id=ad_org.ad_org_id
         AND hh.node_id=v_org_header_id
         AND ad_org.ad_orgtype_id=ad_orgtype.ad_orgtype_id
         AND ad_org.isready='Y'
         AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id and hh.ad_client_id=ad_tree.ad_client_id);     
       END LOOP;
       RETURN NULL;
    END IF;
   
   RETURN v_org_header_id;
   
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END
	 
CREATE OR REPLACE FUNCTION AD_ORG_CHK_DOCUMENTS(p_header_table character varying, p_lines_table character varying, p_document_id character varying, p_header_column_id character varying, p_lines_column_id character varying)
   RETURNS numeric AS
$BODY$ DECLARE
/*************************************************************************
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
* All portions are Copyright (C) 2008-2009 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
   v_org_header_id ad_org.ad_org_id%TYPE;
   v_isbusinessunit ad_orgtype.isbusinessunit%TYPE;
   v_islegalentity ad_orgtype.islegalentity%TYPE;
   v_is_included NUMERIC:=0;
   v_dyn_cur VARCHAR(2000);
 
   TYPE_Ref REFCURSOR;
   cur_doc_lines TYPE_REF%TYPE;
 
   v_line_org VARCHAR(32);
   v_org_line_id VARCHAR(32);
 BEGIN
 
   -- Gets the Business Unit or Legal Entity of the document
   SELECT AD_GET_DOC_LE_BU(p_header_table, p_document_id, p_header_column_id, NULL)
   INTO v_org_header_id
   FROM DUAL;
 
   v_dyn_cur:='SELECT DISTINCT('||p_lines_table||'.ad_org_id) AS v_line_org 
	FROM '||p_header_table||', '||p_lines_table||'  
	WHERE '||p_header_table||'.'||p_header_column_id||' = '||p_lines_table||'.'||p_lines_column_id||' 
	AND '||p_lines_table||'.ad_org_id<>'||''''||v_org_header_id||'''
	AND '||p_lines_table||'.'||p_lines_column_id||'='||''''||p_document_id||'''';
 
   OPEN cur_doc_lines FOR EXECUTE v_dyn_cur;   
    LOOP
      FETCH cur_doc_lines INTO v_line_org;
      IF NOT FOUND THEN
        EXIT;
      END IF;

      SELECT ad_orgtype.isbusinessunit, ad_orgtype.islegalentity
      INTO v_isbusinessunit, v_islegalentity
      FROM AD_Org, AD_OrgType
      WHERE AD_Org.AD_OrgType_ID=AD_OrgType.AD_OrgType_ID
      AND AD_Org.AD_Org_ID=v_line_org;

      v_org_line_id:=v_line_org;
      -- Gets recursively the organization parent until finding a Business Unit or a Legal Entity
      WHILE (v_isbusinessunit='N' AND v_islegalentity='N') LOOP
        SELECT hh.parent_id, ad_orgtype.isbusinessunit, ad_orgtype.islegalentity
        INTO v_org_line_id, v_isbusinessunit, v_islegalentity
        FROM ad_org, ad_orgtype, ad_treenode pp, ad_treenode hh
        WHERE pp.node_id = hh.parent_id
        AND hh.ad_tree_id = pp.ad_tree_id
        AND pp.node_id=ad_org.ad_org_id
        AND hh.node_id=v_org_line_id
        AND ad_org.ad_orgtype_id=ad_orgtype.ad_orgtype_id
        AND ad_org.isready='Y'
        AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id AND hh.ad_client_id=ad_tree.ad_client_id);     
      END LOOP;

      IF (v_org_line_id<>v_org_header_id) THEN
        v_is_included:=-1;
      END IF;
      EXIT WHEN v_is_included=-1;
 
    END LOOP; 
   CLOSE cur_doc_lines;
 
   RETURN v_is_included;
 
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION AD_ORG_CHK_DOC_PAYMENTS(p_header_table IN character varying, p_lines_table IN character varying, p_document_id IN character varying, p_header_column_id IN character varying, p_lines_column_id IN character varying, p_lines_column_payment_id IN character varying) 
 RETURNS numeric AS
 $BODY$ DECLARE
/*************************************************************************
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
* All portions are Copyright (C) 2008-2009 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
   v_org_header_id ad_org.ad_org_id%TYPE;
   v_isbusinessunit ad_orgtype.isbusinessunit%TYPE;
   v_islegalentity ad_orgtype.islegalentity%TYPE;
   v_is_included NUMERIC:=0;
   v_dyn_cur VARCHAR(2000);
 
   TYPE_Ref REFCURSOR;
   cur_doc_lines_payment TYPE_REF%TYPE;
 
   v_line_org_payment VARCHAR(32);
   v_org_payment_line_id VARCHAR(32);
 BEGIN
 
   -- Gets the Business Unit or Legal Entity of the document
   SELECT AD_GET_DOC_LE_BU(p_header_table, p_document_id, p_header_column_id, NULL)
   INTO v_org_header_id
   FROM DUAL;
 
   v_dyn_cur:='SELECT DISTINCT(C_DEBT_PAYMENT.ad_org_id) AS v_line_org_payment
    FROM '||p_header_table||', '||p_lines_table||', C_DEBT_PAYMENT
    WHERE '||p_header_table||'.'||p_header_column_id||' = '||p_lines_table||'.'||p_lines_column_id||'
    AND C_DEBT_PAYMENT.C_DEBT_PAYMENT_ID='||p_lines_table||'.'||p_lines_column_payment_id||'
	AND '||p_lines_table||'.ad_org_id<>'||''''||v_org_header_id||'''
    AND '||p_lines_table||'.'||p_lines_column_id||'='||''''||p_document_id||'''';

   -- Check the payments of the lines belong to the same BU or LE as the document header
   OPEN cur_doc_lines_payment FOR EXECUTE v_dyn_cur;
    LOOP
     FETCH cur_doc_lines_payment INTO v_line_org_payment;
     IF NOT FOUND THEN
       EXIT;
     END IF;


     SELECT ad_orgtype.isbusinessunit, ad_orgtype.islegalentity
     INTO v_isbusinessunit, v_islegalentity
     FROM AD_Org, AD_OrgType
     WHERE AD_Org.AD_OrgType_ID=AD_OrgType.AD_OrgType_ID
     AND AD_Org.AD_Org_ID=v_line_org_payment;

      v_org_payment_line_id:=v_line_org_payment;
      -- Gets recursively the organization parent until finding a Business Unit or a Legal Entity
      WHILE (v_isbusinessunit='N' AND v_islegalentity='N') LOOP
        SELECT hh.parent_id, ad_orgtype.isbusinessunit, ad_orgtype.islegalentity
        INTO v_org_payment_line_id, v_isbusinessunit, v_islegalentity
        FROM ad_org, ad_orgtype, ad_treenode pp, ad_treenode hh
        WHERE pp.node_id = hh.parent_id
        AND hh.ad_tree_id = pp.ad_tree_id
        AND pp.node_id=ad_org.ad_org_id
        AND hh.node_id=v_org_payment_line_id
        AND ad_org.ad_orgtype_id=ad_orgtype.ad_orgtype_id
        AND ad_org.isready='Y'
        AND  EXISTS (SELECT 1 FROM ad_tree WHERE ad_tree.treetype='OO' AND hh.ad_tree_id=ad_tree.ad_tree_id AND hh.ad_client_id=ad_tree.ad_client_id);     
      END LOOP;

     IF (v_org_payment_line_id<>v_org_header_id) THEN
       v_is_included:=-1;
     END IF;
     EXIT WHEN v_is_included=-1;
 
    END LOOP; 
   CLOSE cur_doc_lines_payment;
 
  RETURN v_is_included;
 
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE FUNCTION AD_GET_RDBMS()
  RETURNS varchar AS
$BODY$ DECLARE
/*************************************************************************
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
* All portions are Copyright (C) 2009 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
BEGIN
  return 'POSTGRE';
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

CREATE OR REPLACE VIEW AD_INTEGER AS
SELECT a.value::numeric AS value
   FROM generate_series(1, 1024) a(value);
/-- END

CREATE OR REPLACE FUNCTION uuid_generate_v4()
RETURNS uuid
AS '$libdir/uuid-ossp', 'uuid_generate_v4'
VOLATILE STRICT LANGUAGE C;
/-- END

-- Inserts an alert recipient for available updates
-- See issue:  https://issues.openbravo.com/view.php?id=11743
CREATE OR REPLACE FUNCTION pg_temp.insert_recipient()
  RETURNS void AS
$BODY$ DECLARE
/*************************************************************************
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
* All portions are Copyright (C) 2009 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
BEGIN
  INSERT INTO ad_alertrecipient(ad_client_id, ad_org_id, isactive, created, createdby,
                              updated, updatedby, ad_alertrecipient_id, ad_alertrule_id,
                              ad_role_id, sendemail)
       VALUES('0', '0', 'Y', now(), '100', now(), '100', '8CC1347628D148FABA1FC26622F4B070', '1005400000', '0', 'N');
EXCEPTION
WHEN OTHERS THEN
--do nothing
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE;
SELECT pg_temp.insert_recipient();
/-- END

--Inserts role access for new Smartclient register window
--It needs to be done this way until this issue is fixed:  https://issues.openbravo.com/view.php?id=18689
CREATE OR REPLACE FUNCTION pg_temp.insert_register_form_access()
  RETURNS void AS
$BODY$ DECLARE
/*************************************************************************
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
* All portions are Copyright (C) 2009 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
BEGIN
  INSERT INTO OBUIAPP_View_Role_Access(OBUIAPP_View_Role_Access_ID, OBUIAPP_View_Impl_ID, AD_Role_ID, AD_Client_ID,
      AD_Org_ID, IsActive, Created,
      CreatedBy, Updated, UpdatedBy)
         VALUES(get_uuid(), 'FF808081329B023101329B0CE2080013', '0', '0', '0', 'Y', now(), '0', now(), '0');
EXCEPTION
WHEN OTHERS THEN
--do nothing
END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE;
SELECT pg_temp.insert_register_form_access();
/-- END

--update parent reference for old modules
alter table ad_reference disable trigger ad_reference_mod_trg;
/-- END

update ad_reference
   set parentreference_id =( CASE VALIDATIONTYPE WHEN 'S' THEN '30' WHEN 'L' THEN '17' WHEN 'T' THEN '18' end)
   where validationtype in ('S','L','T')
   and parentreference_id is null;
/-- END
 
alter table ad_reference enable trigger ad_reference_mod_trg;
/-- END
 

CREATE OR REPLACE FUNCTION ad_create_audit_triggers(p_pinstance_id character varying)
  RETURNS void AS
$BODY1$ DECLARE 
/*************************************************************************
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
* All portions are Copyright (C) 2009-2010 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
  code TEXT ;
  cur_triggers RECORD;
  cur_tables RECORD;
  cur_cols RECORD;
  triggerName VARCHAR(30); 
  recordIdName VARCHAR(30);
  datatype VARCHAR(30); 
  clientinfo NUMERIC;
  deleted NUMERIC :=0;
  created NUMERIC :=0;
  v_message VARCHAR(500);
  v_isObps NUMERIC;
BEGIN 
  select count(*) 
    into v_isObps
    from ad_system
   where Instance_key is not null
     and activation_key is not null;
     
  if v_isObps = 0 then
    RAISE EXCEPTION '%', '@OBPSNeededForAudit@' ;
  end if;  	
	
  for cur_triggers in (select *
                         from user_triggers
                        where trigger_name like 'au_%') loop
    execute 'DROP TRIGGER '||cur_triggers.trigger_name||' ON '||cur_triggers.table_name;
    execute 'DROP FUNCTION '||cur_triggers.trigger_name||'()';  
    raise notice 'deleting %', cur_triggers.trigger_name;
    deleted := deleted + 1;
  end loop;

  for cur_tables in (select *
                       from ad_table
                      where isfullyaudited = 'Y'
                      AND ISVIEW='N'
                      order by tablename) loop
    
    triggerName := 'AU_'||SUBSTR(cur_tables.tablename,1,23)||'_TRG';
    raise notice '%', triggerName;
    
    select count(*) into clientinfo
      from dual
     where exists (select 1 from ad_column
                    where ad_table_id = cur_tables.ad_table_id
                     and lower(columnname)='ad_client_id')
       and exists (select 1 from ad_column
                    where ad_table_id = cur_tables.ad_table_id
                     and lower(columnname)='ad_org_id');                     
                     
    
    select columnname
      into recordIdName
      from ad_column
     where ad_table_id = cur_tables.ad_table_id
       and iskey='Y';
    
      code := 'create or replace FUNCTION '||triggerName||'() 
RETURNS trigger AS
$BODY$
DECLARE
  V_USER_ID VARCHAR(32); 
  V_PROCESS_TYPE VARCHAR(60);
  V_PROCESS_ID VARCHAR(32);
  V_RECORD_ID VARCHAR(32);
  V_RECORD_REV NUMERIC;
  V_ACTION CHAR(1);
  V_NEW_CHAR VARCHAR(4000) := NULL; 
  V_OLD_CHAR VARCHAR(4000) := NULL; 
  V_NEW_NCHAR VARCHAR(2000) := NULL; 
  V_OLD_NCHAR VARCHAR(2000) := NULL; 
  V_OLD_NUMBER NUMERIC := NULL;
  V_NEW_NUMBER NUMERIC := NULL;
  V_OLD_DATE TIMESTAMP := NULL;
  V_NEW_DATE TIMESTAMP := NULL;
  V_TIME TIMESTAMP;
  V_ORG VARCHAR(32); 
  V_CLIENT VARCHAR(32); 
  V_CHANGE BOOLEAN;
  V_ISAUDITED CHAR(1);
BEGIN 
';

if (cur_tables.ad_table_id != '100') then
code := code ||
'
  SELECT ISFULLYAUDITED
    INTO V_ISAUDITED
    FROM AD_TABLE
   WHERE AD_TABLE_ID = '''||cur_tables.ad_table_id||''';
  IF V_ISAUDITED = ''N'' THEN 
    IF TG_OP = ''DELETE'' THEN RETURN OLD; ELSE RETURN NEW; END IF;
  END IF;
';
end if;

code := code ||
'
  BEGIN
    SELECT AD_USER_ID, PROCESSTYPE, PROCESSID
      INTO V_USER_ID, V_PROCESS_TYPE, V_PROCESS_ID
      FROM AD_CONTEXT_INFO;
  EXCEPTION WHEN OTHERS THEN NULL;
  END;
  
  V_TIME := TO_DATE(NOW());
 
  IF TG_OP = ''UPDATE'' THEN 
    V_RECORD_ID := new.'||recordIdName||';
    V_ACTION := ''U'';';
if (clientinfo!=0) then
code := code ||'
    V_CLIENT := new.AD_CLIENT_ID;
    V_ORG := new.AD_ORG_ID;';
end if;
code := code ||'
  ELSIF TG_OP = ''INSERT'' THEN
    V_RECORD_ID := new.'||recordIdName||';
    V_ACTION := ''I'';';
if (clientinfo!=0) then
code := code ||'
    V_CLIENT := new.AD_CLIENT_ID;
    V_ORG := new.AD_ORG_ID;';
end if;
code := code ||'
  ELSE
    V_RECORD_ID := old.'||recordIdName||';
    V_ACTION := ''D'';';
if (clientinfo!=0) then
code := code ||'
    V_CLIENT := old.AD_CLIENT_ID;
    V_ORG := old.AD_ORG_ID;';
end if;
code := code ||'
  END IF;

SELECT COALESCE(MAX(RECORD_REVISION),0)+1
      INTO V_RECORD_REV
      FROM AD_AUDIT_TRAIL
     WHERE AD_TABLE_ID='''|| cur_tables.ad_table_id||'''
       AND RECORD_ID=V_RECORD_ID;
';
       
    for cur_cols in (select *
                       from user_tab_columns u, aD_column c
                      where table_name = upper(cur_tables.tablename)
                        AND c.ad_table_id = cur_tables.ad_table_id
                        and upper(c.columnname) = u.column_name
                        AND u.data_type != 'BYTEA'
                        and upper(c.columnname) not in ('CREATED','CREATEDBY','UPDATED', 'UPDATEDBY')
			and c.isexcludeaudit='N'
                        order by c.position) loop
      code := code || '
    V_Change := false;';
      if (cur_cols.data_type in ('VARCHAR', 'BPCHAR', 'TEXT')) then
        datatype := 'CHAR';
        code := code || '
   IF TG_OP = ''UPDATE'' THEN
     V_CHANGE = (COALESCE(new.'||cur_cols.COLUMN_NAME||',''.'') != COALESCE(old.'||cur_cols.COLUMN_NAME||',''.'') OR (new.'||cur_cols.COLUMN_NAME||' IS NULL AND old.'||cur_cols.COLUMN_NAME||'=''.'') OR (old.'||cur_cols.COLUMN_NAME||' IS NULL AND new.'||cur_cols.COLUMN_NAME||'=''.'') );
   END IF;';
      elsif (cur_cols.data_type in ('TIMESTAMP')) then
        datatype := 'DATE';
code := code || '
   IF TG_OP = ''UPDATE'' THEN
     V_CHANGE = COALESCE(new.'||cur_cols.COLUMN_NAME||', now()) != COALESCE(old.'||cur_cols.COLUMN_NAME||', now());
   END IF;';
      else
        datatype := 'NUMBER';
        code := code || '
   IF TG_OP = ''UPDATE'' THEN
     V_CHANGE = COALESCE(new.'||cur_cols.COLUMN_NAME||', -1) != COALESCE(old.'||cur_cols.COLUMN_NAME||', -1);
   END IF;
';
      end if;
      
      
      code := code ||
'
  V_CHANGE := V_CHANGE OR (TG_OP = ''DELETE'') OR (TG_OP = ''INSERT'');
  IF (V_CHANGE) THEN
    IF (TG_OP in (''UPDATE'', ''INSERT'')) THEN
      V_NEW_'||datatype||' := new.'||cur_cols.COLUMN_NAME||';
    END IF;
    IF (TG_OP in (''UPDATE'', ''DELETE'')) THEN
      V_OLD_'||datatype||' := old.'||cur_cols.COLUMN_NAME||';
    END IF;
    
    INSERT INTO AD_AUDIT_TRAIL 
           (AD_AUDIT_TRAIL_ID, AD_USER_ID, AD_TABLE_ID, AD_COLUMN_ID, 
           PROCESSTYPE, PROCESS_ID, RECORD_ID, RECORD_REVISION, ACTION, 
           EVENT_TIME, OLD_'||datatype||', NEW_'||datatype||',
           AD_CLIENT_ID, AD_ORG_ID)
          VALUES
           (GET_UUID(), V_USER_ID, '''|| cur_tables.ad_table_id||''', '''||cur_cols.ad_column_id||''', 
           v_process_type, v_process_id, v_record_id, v_record_rev, v_action, 
           v_time, v_old_'||datatype||', v_new_'||datatype||',
           V_CLIENT, V_ORG);
  END IF;
';
    end loop;
 
code := code ||
'IF TG_OP = ''DELETE'' THEN RETURN OLD; ELSE RETURN NEW; END IF; 
END
; $BODY$
  LANGUAGE ''plpgsql'' VOLATILE';
EXECUTE(code);

 code := 
    'CREATE TRIGGER '||triggerName||'
      BEFORE INSERT OR UPDATE OR DELETE
      ON '||cur_cols.table_name||'
      FOR EACH ROW
      EXECUTE PROCEDURE '||triggerName||'()';
      execute(code);
      
    created := created + 1;

  end loop;
  
  v_Message := '@Deleted@: '||deleted||' @Created@: '||created;
  PERFORM AD_UPDATE_PINSTANCE(p_PInstance_ID, NULL, 'N', 1, v_Message) ;
  EXCEPTION
WHEN OTHERS THEN
  v_Message:= '@ERROR=' || SQLERRM;
  RAISE NOTICE '%',v_Message ;
  IF (p_PInstance_ID IS NOT NULL) THEN
     PERFORM AD_UPDATE_PINSTANCE(p_PInstance_ID, NULL, 'N', 0, v_Message) ;
  END IF;
  RETURN;
  
END ; $BODY1$
LANGUAGE 'plpgsql' VOLATILE
/-- END

-- DOW: day of week
CREATE OR REPLACE FUNCTION next_day(p_initDate timestamp without time zone, p_targetDOW numeric)
  RETURNS DATE AS
$BODY$ DECLARE
/*************************************************************************
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
************************************************************************/
  v_Value NUMERIC;
  v_Return DATE;
BEGIN
  v_Value := p_targetDOW - TO_NUMBER(TO_CHAR(EXTRACT('DOW' FROM p_initDate), '9999'), '9999');

  IF (v_Value < 0 ) THEN
    v_Value := v_Value + 7;
  END IF;

  SELECT p_initDate + v_Value
  INTO v_Return
  FROM DUAL;
  
  RETURN v_Return;

END;   $BODY$
  LANGUAGE 'plpgsql' VOLATILE
/-- END

