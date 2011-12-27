set current path=CURRENT USER, system path
/-- END


Begin  
  execute immediate 'Drop table AD_ENABLE_TRIGGERS';
  Exception when others then null;
End;
/-- END

 -- create temporary tables



CREATE OR REPLACE FUNCTION TO_CHAR(CHAR()) returns varchar() source char(varchar())
/-- END 

CREATE OR REPLACE FUNCTION TO_CHAR(VARCHAR()) returns varchar() source varchar(varchar())
/-- END

create or replace FUNCTION GET_UUID RETURN VARCHAR2
AS
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
* All portions are Copyright (C) 2008 Openbravo SLU
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************/
BEGIN
 return hex(sysibm.generate_unique());
END GET_UUID;
/-- END

create or replace FUNCTION AD_DB_MODIFIED(p_Update CHAR) RETURN CHAR

AS
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
  v_Modified char(1);
  --PRAGMA AUTONOMOUS_TRANSACTION; --To allow DML within a function in a select        
BEGIN
   SELECT (CASE WHEN COUNT(*)>0 THEN 'Y' ELSE 'N' END)
    INTO v_Modified
     FROM AD_SYSTEM_INFO
     WHERE LAST_DBUPDATE < (SELECT MAX(LAST_DDL_TIME)
                              FROM USER_OBJECTS);
                              
                     
   BEGIN
   IF p_Update = 'Y' THEN
     UPDATE AD_SYSTEM_INFO
       SET LAST_DBUPDATE = NOW();
   END IF;
   END;
   COMMIT;
   RETURN v_Modified;
   EXCEPTION 
     WHEN OTHERS THEN
       RETURN 'N';
END AD_DB_MODIFIED;
/-- END 
