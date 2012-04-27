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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = Event Handler =
//
// Contains code which is called for page level events. The mouse down event
// is handled to set the correct active view.
//
(function (OB, isc) {

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }

  function EventHandler() {}

  EventHandler.prototype = {

    mouseDown: function (canvas) {
      var lastEvent = isc.EventHandler.lastEvent,
          checkName = lastEvent.nativeTarget ? lastEvent.nativeTarget.name : null,
          index = checkName ? checkName.indexOf('_') : -1;
      // this code assumes that there is a name attribute on the html element
      // which points to the formitem
      // happens with compount formitems, such as date
      // in that case the formitem name consists of the fieldname followed
      // by the 
      if (index !== -1) {
        checkName = checkName.substring(0, index);
      }

      // handle a click on a formitem
      if (isc.isA.DynamicForm(canvas) && checkName) {
        return this.processEvent(canvas.getField(checkName));
      } else {
        return this.processEvent(canvas);
      }
    },

    // at this point target can be a canvas or a formitem
    processEvent: function (target) {
      var onClickTarget = null,
          lastEvent = isc.EventHandler.lastEvent;

      // handle a special case:
      // https://issues.openbravo.com/view.php?id=17439
      // when setting the active view we loose the click
      if (lastEvent && lastEvent.eventType === 'mouseDown' && lastEvent.DOMevent && lastEvent.DOMevent.target && lastEvent.DOMevent.target.onclick) {
        onClickTarget = lastEvent.DOMevent.target;
      }

      if (!target) {
        return true;
      }
      if (target.pane && target.pane.setAsActiveView) {
        target.pane.setAsActiveView();
        return true;
      }

      // when clicking in the tabbar
      if (target.tabSet && target.tabSet.getSelectedTab() && target.tabSet.getSelectedTab().pane && target.tabSet.getSelectedTab().pane.setAsActiveView) {
        target.tabSet.getSelectedTab().pane.setAsActiveView();
        return true;
      }

      do {
        if (target.view && target.view.setAsActiveView && target.view.isVisible()) {
          // don't do this if already activec
          if (target.view.isActiveView()) {
            onClickTarget = null;
          }
          target.view.setAsActiveView();
          if (onClickTarget) {
            onClickTarget.onclick();
          }
          return true;
        }
        // a direct click in a form item
        if (isc.isA.FormItem(target)) {
          var view = OB.Utilities.determineViewOfFormItem(target);
          if (view && view.setAsActiveView) {
            view.lastFocusedItem = target;
            view.setAsActiveView();
            return true;
          }
        }
        if (target.mouseDownCancelParentPropagation) { // Added to be able to scroll the toolbar without focusing top level view
          target = null;
        } else if (!target.parentElement && target.grid) {
          target = target.grid;
        } else {
          target = target.parentElement;
        }
      } while (target);
      return true;
    }
  };

  OB.EventHandler = new EventHandler();
  isc.Page.setEvent(isc.EH.MOUSE_DOWN, OB.EventHandler, null, 'mouseDown');
}(OB, isc));