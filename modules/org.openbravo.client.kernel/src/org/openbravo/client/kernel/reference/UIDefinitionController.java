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
package org.openbravo.client.kernel.reference;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.Hibernate;
import org.openbravo.base.model.Column;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Table;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.UserInterfaceDefinition;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.domain.ReferencedTable;

/**
 * Determines which type, editor type and filter editor type is used for a certain reference in the
 * system.
 * 
 * @author mtaal
 */
public class UIDefinitionController extends BaseTemplateComponent {
  private static final Logger log = Logger.getLogger(UIDefinitionController.class);

  private static final String TEMPORARY_DECIMAL_REPLACE = ";;;";

  private static UIDefinitionController instance = new UIDefinitionController();

  public static String SHORTFORMAT_QUALIFIER = "Relation";
  public static String INPUTFORMAT_QUALIFIER = "Edition";
  public static String NORMALFORMAT_QUALIFIER = "Inform";

  private static final String EncryptedStringReferenceID = "16EC6DF4A59747749FDF256B7FBBB058";
  private static final String HashedStringReferenecID = "C5C21C28B39E4683A91779F16C112E40";

  public static UIDefinitionController getInstance() {
    return instance;
  }

  public static void setInstance(UIDefinitionController instance) {
    UIDefinitionController.instance = instance;
  }

  private Map<String, UIDefinition> cachedDefinitions = null;
  private Map<String, UIDefinition> uiDefinitionsByColumnId = null;
  private Map<String, FormatDefinition> formatDefinitions = null;

  public UIDefinition getUIDefinition(String columnId) {
    if (cachedDefinitions == null) {
      setInitCachedDefinitions();
    }
    final UIDefinition uiDefinition = uiDefinitionsByColumnId.get(columnId);
    if (uiDefinition == null) {
      log.warn("NO UIDefinition found for columnId " + columnId);
    }
    return uiDefinition;
  }

  public UIDefinition getUIDefinition(Reference reference) {
    if (cachedDefinitions == null) {
      setInitCachedDefinitions();
    }
    final UIDefinition uiDefinition = cachedDefinitions.get(reference.getId());
    if (uiDefinition == null) {
      log.warn("NO UIDefinition found for reference " + reference.getId());
    }
    return uiDefinition;
  }

  public Collection<UIDefinition> getAllUIDefinitions() {
    if (cachedDefinitions == null) {
      setInitCachedDefinitions();
    }
    return cachedDefinitions.values();
  }

  /**
   * @param formatId
   *          the id used in the format.xml, for example euro
   * @param qualifier
   *          the extra qualifier for example Edition, Relation and Inform
   * @return the format definition as it is set in the format.xml.
   */
  public FormatDefinition getFormatDefinition(String formatId, String qualifier) {
    setInitializeComputeFormatDefinitions();
    return formatDefinitions.get(formatId + qualifier);
  }

  public synchronized void setInitCachedDefinitions() {
    if (cachedDefinitions != null) {
      return;
    }
    final Map<String, UIDefinition> localCachedDefinitions = new HashMap<String, UIDefinition>();
    final Map<String, UIDefinition> localUIDefinitionsByColumn = new HashMap<String, UIDefinition>();

    OBContext.setAdminMode();
    try {
      final OBQuery<Reference> referenceQry = OBDal.getInstance().createQuery(Reference.class, "");
      referenceQry.setFilterOnActive(false);
      for (Reference reference : referenceQry.list()) {
        try {
          final UIDefinition uiDefinition = getUIDefinitionImplementation(reference);
          uiDefinition.setReference(reference);

          // initialize stuff needed later
          if (uiDefinition instanceof FKComboUIDefinition) {
            for (ReferencedTable refTable : reference.getADReferencedTableList()) {
              Hibernate.initialize(refTable.getTable());
            }
          }

          localCachedDefinitions.put(reference.getId(), uiDefinition);
        } catch (Exception e) {
          // just log but continue
          log.error("Exception when creating UIDefinition for reference " + reference, e);
        }
      }

      for (Table table : ModelProvider.getInstance().getTables()) {
        List<Column> cols = table.getColumns();
        for (Column column : cols) {
          String referenceId;
          if (column.getReferenceValue() != null) {
            referenceId = column.getReferenceValue().getId();
          } else {
            referenceId = column.getReference().getId();
          }

          // if one of the old hardcoded pwd-column -> move to new-style reference
          // Companion-code in org.openbravo.base.mode.Property (for for domaintype)
          String colReferenceId = column.getReference().getId();
          if (column.isEncrypted() && colReferenceId != EncryptedStringReferenceID
              && colReferenceId != HashedStringReferenecID) {
            if (column.isDecryptable()) {
              referenceId = EncryptedStringReferenceID;
            } else {
              referenceId = HashedStringReferenecID;
            }
          }
          localUIDefinitionsByColumn.put(column.getId(), localCachedDefinitions.get(referenceId));
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    uiDefinitionsByColumnId = localUIDefinitionsByColumn;
    cachedDefinitions = localCachedDefinitions;
  }

  private UIDefinition getUIDefinitionImplementation(Reference reference) throws Exception {

    if (!reference.getOBCLKERUIDefinitionList().isEmpty()) {
      for (UserInterfaceDefinition uiDefinition : reference.getOBCLKERUIDefinitionList()) {
        if (uiDefinition.isActive()) {
          final Class<?> clz = OBClassLoader.getInstance().loadClass(
              uiDefinition.getImplementationClassname());
          return (UIDefinition) clz.newInstance();
        }
      }
    }
    if (reference.getParentReference() != null && !reference.isBaseReference()
        && reference.getParentReference().isBaseReference()) {
      return getUIDefinitionImplementation(reference.getParentReference());
    }
    // the default
    log.warn("No user interface definition found for reference " + reference);
    return StringUIDefinition.class.newInstance();
  }

  private synchronized Map<String, FormatDefinition> setInitializeComputeFormatDefinitions() {
    if (formatDefinitions != null) {
      return formatDefinitions;
    }
    final Map<String, FormatDefinition> localFormatDefinitions = new HashMap<String, FormatDefinition>();
    final Document doc = OBPropertiesProvider.getInstance().getFormatXMLDocument();
    final Element root = doc.getRootElement();
    for (Object object : root.elements()) {
      final Element element = (Element) object;
      final FormatDefinition formatDefinition = new FormatDefinition();

      formatDefinition.setDecimalSymbol(element.attributeValue("decimal"));
      formatDefinition.setFormat(correctMaskForGrouping(element.attributeValue("formatOutput"),
          element.attributeValue("decimal"), element.attributeValue("grouping")));
      formatDefinition.setGroupingSymbol(element.attributeValue("grouping"));
      localFormatDefinitions.put(element.attributeValue("name"), formatDefinition);
    }
    formatDefinitions = localFormatDefinitions;
    return formatDefinitions;
  }

  private String correctMaskForGrouping(String mask, String decimalSymbol, String groupingSymbol) {
    String localMask = mask.replace(".", TEMPORARY_DECIMAL_REPLACE);
    localMask = localMask.replace(",", groupingSymbol);
    return localMask.replaceAll(TEMPORARY_DECIMAL_REPLACE, decimalSymbol);
  }

  public static class FormatDefinition {
    private String decimalSymbol;
    private String groupingSymbol;
    private String format;

    public String getDecimalSymbol() {
      return decimalSymbol;
    }

    public void setDecimalSymbol(String decimalSymbol) {
      this.decimalSymbol = decimalSymbol;
    }

    public String getGroupingSymbol() {
      return groupingSymbol;
    }

    public void setGroupingSymbol(String groupingSymbol) {
      this.groupingSymbol = groupingSymbol;
    }

    public String getFormat() {
      return format;
    }

    public void setFormat(String format) {
      this.format = format;
    }

  }

}
