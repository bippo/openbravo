/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.KernelUtilities = {};

// ** {{{ OB.KernelUtilities.handleUserException }}} **
//
// Shows a warning message from the exception. The message is translatable.
//
// Parameters:
// * {{{msg}}}: Translatable message to show
// * {{{params}}}: Optional parameters to add to the message
OB.KernelUtilities.handleUserException = function (msg, params) {
  // todo: make this nice
  OB.I18N.getLabel(msg, params, isc, 'warn');
};

// ** {{{ OB.KernelUtilities.handleSystemException }}} **
//
// Shows a warning message from the exception. The message is not translatable.
//
// Parameters:
// * {{{msg}}}: Message to show
OB.KernelUtilities.handleSystemException = function (msg) {
  // todo: make this nice
  isc.warn('Error occured: ' + msg);
};