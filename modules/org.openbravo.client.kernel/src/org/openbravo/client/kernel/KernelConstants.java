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
package org.openbravo.client.kernel;

/**
 * Defines constants for this module.
 * 
 * @author mtaal
 */
public class KernelConstants {

  /**
   * This prefix is used to make database id work correctly as javascript id's.
   */
  public static final String ID_PREFIX = "_";

  public static final String HTTP_SESSION = "_httpSession";

  public static final String HTTP_REQUEST = "_httpRequest";

  public static final String JAVASCRIPT_CONTENTTYPE = "application/javascript;charset=UTF-8";

  public static final String CSS_CONTENTTYPE = "text/css";

  public static final String KERNEL_COMPONENT_TYPE = "OBCLKER_Kernel";

  public static final String KERNEL_JAVA_PACKAGE = "org.openbravo.client.kernel";

  public static final String ACTION_PARAMETER = "_action";

  public static final String RESOURCE_VERSION_PARAMETER = "_version";
  public static final String RESOURCE_LANGUAGE_PARAMETER = "_language";

  /**
   * @deprecated use {@link #SKIN_PARAMETER}
   */
  public static final String SKIN_VERSION_PARAMETER = "_skinVersion";
  /**
   * @deprecated use {@link #SKIN_DEFAULT}
   */
  public static final String SKIN_VERSION_300 = "3.00";
  /**
   * @deprecated use {@link #SKIN_CLASSIC}
   */
  public static final String SKIN_VERSION_CLASSIC = "2.50_emulation";

  public static final String SKIN_PARAMETER = "_skinVersion";
  public static final String SKIN_DEFAULT = "Default";
  public static final String SKIN_CLASSIC = "250to300Comp";

  public static final String MODE_PARAMETER = "_mode";
  public static final String MODE_PARAMETER_300 = "3.00";
  public static final String MODE_PARAMETER_CLASSIC = "classic";

  public static final String STYLE_SHEET_COMPONENT_ID = "StyleSheetResources";
  public static final String RESOURCE_COMPONENT_ID = "StaticResources";
  public static final String TEST_COMPONENT_ID = "TestResources";
  public static final String DOCUMENT_COMPONENT_ID = "DocumentResources";
  public static final String RESOURCE_CONTEXT_URL_PARAMETER = "#{contextUrl}";

  public static final String JSLINT_DIRECTIVE = "jslint";

  public static final String CONTEXT_URL = "_contextUrl";

  public static final String SERVLET_CONTEXT = "_servletContext";

  public static final String APPLICATION_COMPONENT_ID = "Application";

  public static final String APPLICATION_DYNAMIC_COMPONENT_ID = "ApplicationDynamic";

  public static final String LABELS_COMPONENT_ID = "Labels";

  /**
   * The primary key of the labels template
   */
  public static final String I18N_TEMPLATE_ID = "96DA67B541E243FC9A7C6AEBC3752952";

  /**
   * The primary key of the application template
   */
  public static final String APPLICATION_TEMPLATE_ID = "0627967E56584D3B8B13A3C77ABC48E2";

  /**
   * The primary key of the application dynamic template
   */
  public static final String APPLICATION_DYNAMIC_TEMPLATE_ID = "FF8081812E297131012E2A061112001C";

  /**
   * The primary key of the documentation template
   */
  public static final String DOCUMENTATION_TEMPLATE_ID = "DBAC211182304F7784DAB4EDE7B6AA1D";

  /**
   * Name of dateFormat.java property in Openbravo.properties.
   */
  public static final String DATE_FORMAT_PROPERTY = "dateFormat.java";

  /**
   * Name of dateTimeFormat.java property in Openbravo.properties.
   */
  public static final String DATETIME_FORMAT_PROPERTY = "dateTimeFormat.java";

  /**
   * Name of dateFormat.sql property in Openbravo.properties.
   */
  public static final String SQL_DATE_FORMAT_PROPERTY = "dateFormat.sql";

  /**
   * Name of dateTimeFormat.sql property in Openbravo.properties.
   */
  public static final String SQL_DATETIME_FORMAT_PROPERTY = "dateTimeFormat.sql";

  /**
   * Name of dateFormat.sql property in Openbravo.properties.
   */
  public static final String DATETIME_SQL_FORMAT_PROPERTY = "dateTimeFormat.sql";
}
