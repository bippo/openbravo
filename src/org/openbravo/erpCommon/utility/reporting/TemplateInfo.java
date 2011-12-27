/*
 * ************************************************************************ The
 * contents of this file are subject to the Openbravo Public License Version 1.1
 * (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2001-2010 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 * ***********************************************************************
 */
package org.openbravo.erpCommon.utility.reporting;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;

public class TemplateInfo {
  private static Logger log4j = Logger.getLogger(TemplateInfo.class);

  private String _TemplateLocation;
  private String _TemplateFilename;
  private String _ReportFilename;
  private String _ShowLogo;
  private String _ShowCompanyData;
  private String _HeaderMargin;
  private TemplateData[] templates;
  private EmailDefinition _DefaultEmailDefinition;
  private Map<String, EmailDefinition> _EmailDefinitions;

  public class EmailDefinition {
    private String _Subject;
    private String _Body;
    private String _Language;
    private boolean _IsDefault;

    public EmailDefinition(EmailDefinitionData emailDefinitionData) {
      _Subject = emailDefinitionData.getField("subject");
      _Body = emailDefinitionData.getField("body");
      _Language = emailDefinitionData.getField("ad_language");
      if (emailDefinitionData.getField("isdefault") != null) {
        _IsDefault = emailDefinitionData.getField("isdefault") == "Y" ? true : false;
      }
    }

    public String getSubject() {
      return _Subject;
    }

    public String getBody() {
      return _Body;
    }

    public String getLanguage() {
      return _Language;
    }

    public boolean isDefault() {
      return _IsDefault;
    }
  }

  public TemplateInfo(ConnectionProvider connectionProvider, String docTypeId, String orgId,
      String strLanguage, String templateId) throws ServletException, ReportingException {
    templates = TemplateData.getDocumentTemplates(connectionProvider, docTypeId, orgId);
    final TemplateData template = getSelectedTemplate(templates, templateId);
    if (templates.length > 0) {
      setTemplateLocation(template.getField("template_location"));
      _TemplateFilename = template.getField("template_filename");
      _ReportFilename = template.getField("report_filename");
      _ShowLogo = template.getField("show_logo");
      _ShowCompanyData = template.getField("show_company_data");
      _HeaderMargin = template.getField("header_margin");

      // READ EMAIL DEFINITIONS!!!!
      _EmailDefinitions = new HashMap<String, EmailDefinition>();
      final EmailDefinitionData[] emailDefinitionsData = EmailDefinitionData.getEmailDefinitions(
          connectionProvider, orgId, template.id);
      if (emailDefinitionsData.length > 0) {
        for (final EmailDefinitionData emailDefinitionData : emailDefinitionsData) {
          final EmailDefinition emailDefinition = new EmailDefinition(emailDefinitionData);
          _EmailDefinitions.put(emailDefinition.getLanguage(), emailDefinition);

          if (emailDefinition.isDefault())
            _DefaultEmailDefinition = emailDefinition;
        }
        if (_DefaultEmailDefinition == null && !_EmailDefinitions.isEmpty()) {
          _DefaultEmailDefinition = _EmailDefinitions.values().iterator().next();
        }
      } else
        throw new ReportingException(Utility.messageBD(connectionProvider, "NoEmailDefinitions",
            strLanguage) + template.id);
    } else
      throw new ServletException(Utility.messageBD(connectionProvider, "NoDocumentTypeTemplate",
          strLanguage) + docTypeId);
  }

  private TemplateData getSelectedTemplate(TemplateData[] templates, String templateId) {
    if ("default".equals(templateId)) {
      return (templates.length == 0) ? new TemplateData() : templates[0];
    } else {
      for (int i = 0; i < templates.length; i++) {
        final TemplateData template = templates[i];
        if (templateId.equals(template.id)) {
          return template;
        }
      }
    }

    return null;
  }

  TemplateData[] getTemplates() {
    return templates;
  }

  void setTemplates(TemplateData[] templates) {
    this.templates = templates;
  }

  String getTemplate() {
    return _TemplateLocation + "/" + _TemplateFilename;
  }

  void setTemplateLocation(String templateLocation) {
    _TemplateLocation = templateLocation;
    // Make sure the location always ends with a / character
    if (!_TemplateLocation.endsWith("/"))
      _TemplateLocation = _TemplateLocation + "/";
    if (log4j.isDebugEnabled())
      log4j.debug("Template location is set to: " + _TemplateLocation);
  }

  String getTemplateLocation() {
    return _TemplateLocation;
  }

  String getTemplateFilename() {
    return _TemplateFilename;
  }

  public String getReportFilename() {
    return _ReportFilename;
  }

  public String getShowLogo() {
    return _ShowLogo;
  }

  public String getShowCompanyData() {
    return _ShowCompanyData;
  }

  public String getHeaderMargin() {
    return _HeaderMargin;
  }

  EmailDefinition getEmailDefinition(String language) throws ReportingException {
    EmailDefinition emailDefinition = _EmailDefinitions.get(language);
    if (emailDefinition == null) {
      log4j.info("No email definition found for language " + language
          + ". Using default email definition");
      emailDefinition = _DefaultEmailDefinition;
    }

    if (emailDefinition == null)
      throw new ReportingException("No email definition available.");
    return emailDefinition;
  }

  public EmailDefinition get_DefaultEmailDefinition() {
    return _DefaultEmailDefinition;
  }

  void set_DefaultEmailDefinition(EmailDefinition defaultEmailDefinition) {
    _DefaultEmailDefinition = defaultEmailDefinition;
  }
}
