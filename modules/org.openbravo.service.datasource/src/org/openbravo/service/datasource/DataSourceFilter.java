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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Defines the method to be executed if a fetch request contains a filterClass parameter. The class
 * is instantiated and the doFilter executed. Is the way to filter parameters before the fetching
 * data from the database
 * 
 * @author iperdomo
 */
public interface DataSourceFilter {

  /**
   * Executed on each datasource doGet request, if a filterClass parameter is present in the
   * parameters map
   */
  public void doFilter(Map<String, String> parameters, HttpServletRequest request);

}
