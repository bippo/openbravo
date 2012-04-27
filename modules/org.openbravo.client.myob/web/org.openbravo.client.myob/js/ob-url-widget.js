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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// = OBUrlWidget =
//
// A widget which gets its contents directly from an url.
//
isc.defineClass('OBUrlWidget', isc.OBWidget).addProperties({
  contentSource: null,
  createWindowContents: function () {
    if (!this.contentSource) {
      this.contentSource = this.evaluateContents(this.parameters.src);
    }

    if (this.contentSource.indexOf('butler.openbravo.com') !== -1) {
      this.contentSource = document.location.protocol + this.contentSource.substring(this.contentSource.indexOf('//'));
    }

    return isc.HTMLFlow.create({
      contentsType: 'page',
      contentsURL: this.contentSource,
      height: '100%',
      width: '100%'
    });
  },
  refresh: function () {
    if (this.parameters.src) {
      this.contentSource = this.evaluateContents(this.parameters.src);
    }
    this.windowContents.setContentsURL(this.contentSource);
  }
});