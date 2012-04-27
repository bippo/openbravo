/*global $LAB,internetConnection */

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
// = OBCommunityBrandingWidget =
//
// Implements the community branding widget.
//
isc.defineClass('OBCommunityBrandingWidget', isc.OBWidget).addProperties({
  bodyColor: '#e9e9e9',
  versionText: OB.Application.versionDescription,
  headerLabel: null,

  createWindowContents: function () {
    var layout = isc.VStack.create({
      height: '100%',
      width: '100%',
      styleName: '',
      resizeTo: function () {
        var emptySize;
        if (this.separator) {
          emptySize = Math.round((this.width - 155) / 2);
          this.separator.width = emptySize;
        }
        this.Super('resizeTo', arguments);
      }
    });

    if (!OB.Application.brandingWidget) {
      // set a global pointer to ourselves
      OB.Application.brandingWidget = this;
    }

    layout.addMember(OB.Utilities.createLoadingLayout());

    var post = {
      'eventType': 'GET_COMMUNITY_BRANDING_URL',
      'context': {
        'adminMode': 'false'
      },
      'widgets': []
    };

    var me = this;
    var haveInternet = false;
    /*
     * The following LAB.wait(callback) call does not reliably call the callout in case no
     * internet connection is present (so schedule timeout to use local fallback content after 10s)
     */
    var timerNoInternet = setTimeout(function () {
      me.setOBContent(false);
    }, 10000);
    $LAB.script(document.location.protocol + OB.Application.butlerUtilsUrl).wait(function () {
      haveInternet = (typeof internetConnection !== 'undefined');
      // callback did fire so clear timer as its no longer needed
      clearTimeout(timerNoInternet);

      if (haveInternet) {
        OB.RemoteCallManager.call('org.openbravo.client.myob.MyOpenbravoActionHandler', post, {}, function (response, data, request) {
          var communityBrandingUrl = data.url;
          me.setOBContent(haveInternet, communityBrandingUrl);
        });
      } else {
        me.setOBContent(false);
      }
    });

    return layout;
  },

  setOBContent: function (haveInternet, communityBrandingUrl) {
    var url, params = {},
        emptySize, toolTip, purposeStack;

    if (haveInternet) {
      url = document.location.protocol + communityBrandingUrl;
    } else {
      url = OB.Application.contextUrl + OB.Application.communityBrandingStaticUrl;
      params = {
        'uimode': 'MyOB'
      };
    }

    var layout = this.windowContents;

    // remove Loading...
    var loadingBar = layout.members[this.windowContents.members.length - 1];

    this.versionLabel = isc.Label.create({
      contents: this.versionText,
      height: '22px',
      width: '100%',
      styleName: 'OBWidgetCommunityBranding',
      align: 'center'
    });

    var content = isc.HTMLFlow.create({
      contentsType: 'page',
      contentsURL: url,
      contentsURLParams: params,
      height: '324px'
    });

    toolTip = isc.Label.create({
      contents: '',
      height: '5px',
      width: '155px',
      styleName: this.getPurposeStyleClass(),
      prompt: OB.I18N.getLabel('OBKMO_InstancePurpose')
    });

    emptySize = (layout.width - 155) / 2;

    layout.separator = isc.Label.create({
      contents: '',
      height: '5px',
      width: emptySize
    });

    purposeStack = isc.HStack.create({
      height: '24px',
      width: '100%'
    });
    purposeStack.addMembers(layout.separator);
    purposeStack.addMembers(toolTip);
    purposeStack.addMembers(layout.separator);

    layout.destroyAndRemoveMembers(loadingBar);

    layout.addMember(purposeStack);
    layout.addMember(this.versionLabel);
    layout.addMember(content);
  },

  update: function () {
    //FIXME: too expensive
    OB.MyOB.reloadWidgets();
    //    this.versionLabel.clear();
    //    this.versionLabel.contents = this.versionText;
    //    this.versionLabel.styleName = this.getPurposeStyleClass();
    //    this.versionLabel.draw();
  },

  getPurposeStyleClass: function () {
    var purposeCode = OB.Application.purpose;
    if (purposeCode === 'D') {
      return 'OBWidgetCommunityBrandingDevelopment';
    } else if (purposeCode === 'P') {
      return 'OBWidgetCommunityBrandingProduction';
    } else if (purposeCode === 'T') {
      return 'OBWidgetCommunityBrandingTesting';
    } else if (purposeCode === 'E') {
      return 'OBWidgetCommunityBrandingEvaluation';
    } else {
      return 'OBWidgetCommunityBrandingUnknown';
    }
  },

  confirmedClosePortlet: function (ok) {

    if (!ok) {
      this.Super('confirmedClosePortlet', arguments);
      return;
    }

    if (OB.Application.brandingWidget !== this) {
      this.Super('confirmedClosePortlet', arguments);
      return;
    }

    if (OB.Application.licenseType === 'C' || OB.Application.isTrial || OB.Application.isGolden) {
      isc.warn(OB.I18N.getLabel('OBUIAPP_ActivateMessage', [OB.I18N.getLabel('OBKMO_ActivateMessage')]), {
        isModal: true,
        showModalMask: true,
        toolbarButtons: [isc.Dialog.OK]
      });
      return;
    }
    this.Super('confirmedClosePortlet', arguments);
  }
});