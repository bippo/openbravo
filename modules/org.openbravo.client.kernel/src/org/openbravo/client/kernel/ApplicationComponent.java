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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.client.kernel.reference.UIDefinitionController.FormatDefinition;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.OBVersion;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * The component responsible for generating the static part of the application js file.
 * 
 * @author mtaal
 */
public class ApplicationComponent extends BaseTemplateComponent {

  private static final String MYOB_UIMODE = "MyOB";

  private FormatDefinition formatDefinition = null;

  private FormatDefinition getFormatDefinition() {
    if (formatDefinition == null) {
      formatDefinition = UIDefinitionController.getInstance().getFormatDefinition("qty",
          UIDefinitionController.INPUTFORMAT_QUALIFIER);
    }
    return formatDefinition;
  }

  public String getDefaultGroupingSymbol() {
    return getFormatDefinition().getGroupingSymbol();
  }

  public String getDefaultDecimalSymbol() {
    return getFormatDefinition().getDecimalSymbol();
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getFormats() {
    return (HashMap<String, String>) RequestContext.get().getSessionAttribute("#formatMap");
  }

  public boolean isTestEnvironment() {
    final String testEnvironmentStr = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("test.environment");
    return testEnvironmentStr != null && "true".equals(testEnvironmentStr);
  }

  public String getDefaultNumericMask() {
    return getFormatDefinition().getFormat();
  }

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, KernelConstants.APPLICATION_TEMPLATE_ID);
  }

  public String getSystemVersion() {
    return KernelUtils.getInstance().getVersionParameters(getModule());
  }

  public String getLanguageId() {
    return OBContext.getOBContext().getLanguage().getId();
  }

  public String getLanguage() {
    return OBContext.getOBContext().getLanguage().getLanguage();
  }

  public String getDateFormat() {
    final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    return props.getProperty(KernelConstants.DATE_FORMAT_PROPERTY, "dd-MM-yyyy");
  }

  public String getDateTimeFormat() {
    final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    return props.getProperty(KernelConstants.DATETIME_FORMAT_PROPERTY, "dd-MM-yyyy HH:mm:ss");
  }

  @Deprecated
  public User getUser() {
    return OBContext.getOBContext().getUser();
  }

  @Deprecated
  public Client getClient() {
    return OBContext.getOBContext().getCurrentClient();
  }

  @Deprecated
  public Organization getOrganization() {
    return OBContext.getOBContext().getCurrentOrganization();
  }

  @Deprecated
  public Role getRole() {
    return OBContext.getOBContext().getRole();
  }

  // Module version parameters are used in hyperlinks to prevent caching in
  // development situations
  public List<ModuleVersionParameter> getModuleVersionParameters() {
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    final List<ModuleVersionParameter> result = new ArrayList<ModuleVersionParameter>();
    for (Module module : modules) {
      final ModuleVersionParameter version = new ModuleVersionParameter();
      final String versionParameter = KernelUtils.getInstance().getVersionParameters(module);
      version.setId(module.getId());
      version.setValue(versionParameter);
      result.add(version);
    }
    return result;
  }

  /**
   * @deprecated the value is retrieved asynchronously via the MyOpenbravoActionHandler instead
   */
  @Deprecated
  public String getCommunityBrandingUrl() {
    return Utility.getCommunityBrandingUrl(MYOB_UIMODE);
  }

  public String getCommunityBrandingStaticUrl() {
    return "utility/" + Utility.STATIC_COMMUNITY_BRANDING_URL;
  }

  public String getButlerUtilsUrl() {
    return Utility.BUTLER_UTILS_URL;
  }

  public String getInstancePurpose() {
    final String purpose = OBDal.getInstance().get(SystemInformation.class, "0")
        .getInstancePurpose();
    if (purpose == null) {
      return "";
    }
    return purpose;
  }

  public String getLicenseType() {
    return ActivationKey.getInstance().getLicenseClass().getCode();
  }

  public String getTrialStringValue() {
    return Boolean.toString(ActivationKey.getInstance().isTrial());
  }

  public String getGoldenStringValue() {
    return Boolean.toString(ActivationKey.getInstance().isGolden());
  }

  public String getVersionDescription() {
    ActivationKey ak = ActivationKey.getInstance();
    String strVersion = OBVersion.getInstance().getMajorVersion();
    strVersion += " - ";
    strVersion += Utility.getListValueName("OBPSLicenseEdition", ak.getLicenseClass().getCode(),
        "en_US");

    if (ak.isTrial()) {
      strVersion += " - ";
      strVersion += Utility.messageBD(new DalConnectionProvider(false), "OPSTrial", OBContext
          .getOBContext().getLanguage().getLanguage());
    }

    strVersion += " - ";
    strVersion += OBVersion.getInstance().getMP();
    return strVersion;
  }

  public static class ModuleVersionParameter {
    private String id;
    private String value;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

  }
}
