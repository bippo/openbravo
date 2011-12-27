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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

/**
 * Defines constants for this module.
 * 
 * @author mtaal
 */
public class DataSourceConstants {

  /**
   * If passed as a parameter then only date properties are generated as part of the datasource,
   * because these properties need transformation from string to date and back.
   */
  public static final String MINIMAL_PROPERTY_OUTPUT = "minOutputProperty";

  public static final String OPERATION_TYPE_PARAM = "_operationType";
  public static final String FETCH_OPERATION = "fetch";

  public static final String NEW_PARAM = "_new";
  public static final String URL_NAME_PARAM = "mapping_name";
  public static final String DS_NAME_PARAM = "dataSourceName";
  public static final String DS_ID = "_dataSourceId";

  public static final String DS_COMPONENT_TYPE = "OBSERDS_Datasource";

  public static final String DS_CREATE = "_create";

  public static final String DS_ONLY_GENERATE_CREATESTATEMENT = "_onlyGenerateCreateStatement";

  public static final String DS_CLASS_NAME = "_className";

  public static final String DS_FILTERCLASS_PARAM = "filterClass";

  /**
   * The primary key of the data source template used to render default datasources.
   */
  public static final String DS_TEMPLATE_ID = "2BAD445C2A0343C58E455F9BD379C690";
}
