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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.xml;

/**
 * Contains constants used for standard element/attribute names used in the XML Schema generation
 * and entity XML export.
 * 
 * @see ModelXMLConverter
 * @see EntityXMLConverter
 * 
 * @author mtaal
 */

public class XMLConstants {

  /**
   * The root element in every Openbravo XML document
   */
  public static final String OB_ROOT_ELEMENT = "Openbravo";

  /**
   * The name of the attribute which holds the id of a object (in XML). Is used in the opening tag
   * of a business object and in tags of association properties.
   */
  public static final String ID_ATTRIBUTE = "id";

  /**
   * The name of the attribute which holds the identifier of a object (in XML). Is used in the
   * opening tag of a business object and in tags of association properties.
   */
  public static final String IDENTIFIER_ATTRIBUTE = "identifier";

  /**
   * Is set on the xml tag of a property when the property is transient. Transient properties are
   * exported but are ignored when imported.
   */
  public static final String TRANSIENT_ATTRIBUTE = "transient";

  /**
   * The inactive attribute is used to signal that a certain business object is inactive. A business
   * object tag without this attribute means that the business object is active.
   */
  public static final String INACTIVE_ATTRIBUTE = "inactive";

  /**
   * The reference attribute is used to signal that a certain business object is included in the XML
   * because it was referenced from another Business Object.
   */
  public static final String REFERENCE_ATTRIBUTE = "reference";

  /**
   * The entity-name attribute is used in the generation of the XML Schema.
   */
  public static final String ENTITYNAME_ATTRIBUTE = "entity-name";

  /**
   * A system attribute which is added to the Openbravo opening tag (depends on an option set in the
   * export: {@link EntityXMLConverter#setAddSystemAttributes(boolean)})
   */
  public static final String OB_VERSION_ATTRIBUTE = "ob-version";

  /**
   * @see #OB_VERSION_ATTRIBUTE
   */
  public static final String OB_REVISION_ATTRIBUTE = "ob-revision";

  /**
   * Is added to the root tag to administer the export time
   */
  public static final String DATE_TIME_ATTRIBUTE = "created";

  /**
   * Openbravo namespace
   */
  public static final String OPENBRAVO_NAMESPACE = "http://www.openbravo.com";

  /**
   * XSI namespace
   */
  public static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
}