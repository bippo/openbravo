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
package org.openbravo.client.kernel;

import java.util.Date;

import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.module.Module;

/**
 * A component can be anything from a visualization of a single field to a full ui with forms and
 * grids, it can also be just a java-script component which provides logic but no user interface
 * (for example a data source). All components have in common that they have a String representation
 * which can be loaded and executed by the browser. This means practically that a component is
 * expressed in java-script.
 * 
 * A component has a distinct id which consists of several parts:
 * <ol>
 * <li>the id of the module which provides the view</li>
 * <li>the type of component (for example, selector, grid, etc.)</li>
 * <li>the database id (or other unique identifier) of the component</li>
 * <li>a version number which is basically a string used to support client side caching of
 * components</li>
 * </ol>
 * The id of the component starts with a / and each part is separated with a /, to follow a url like
 * form. an example: org.openbravo.userinterface.selector/selector/FF1231321DASDASDADS/123123123
 * 
 * @author mtaal
 */
public interface Component {

  /**
   * Generates the content of the component which is sent to the client for execution/rendering.
   * 
   * @return the generated javascript which is send back to the client
   */
  public String generate();

  /**
   * @return the id of the component, see the id description in the root of this class.
   */
  public String getId();

  /**
   * @return the last modified date of any data element used to generate the response, if null is
   *         returned then the current date/time is assumed.
   */
  public Date getLastModified();

  /**
   * An ETag is a hash-like string which is used to determine if content has changed since the last
   * request for the content. See <a href="http://www.infoq.com/articles/etags">this</a> link for
   * more information.
   * 
   * Note for language specific components the etag has to encode the language (id) also! See (
   * {@link OBContext#getLanguage()}) to get the current language.
   * 
   * @return a unique hash for the content generated
   */
  public String getETag();

  /**
   * @return the module providing this component
   */
  public Module getModule();

  /**
   * @return the content type passed in the response header, for example:
   *         application/javascript;charset=UTF-8
   */
  public String getContentType();

  /**
   * @return true if the generated output is javascript
   */
  public boolean isJavaScriptComponent();

  /**
   * @return true if the component's module or a module of a subcomponent is in development
   * @see Module#isInDevelopment()
   */
  public boolean isInDevelopment();
}
