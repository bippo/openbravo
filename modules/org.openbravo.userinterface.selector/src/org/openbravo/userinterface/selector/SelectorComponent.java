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
package org.openbravo.userinterface.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.Reference;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.ForeignKeyDomainType;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.util.Check;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.Template;
import org.openbravo.client.kernel.reference.FKComboUIDefinition;
import org.openbravo.client.kernel.reference.NumberUIDefinition;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.Sqlc;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.service.datasource.DataSource;
import org.openbravo.service.datasource.DataSourceConstants;
import org.openbravo.service.datasource.DatasourceField;
import org.openbravo.service.json.JsonConstants;

/**
 * Generates the javascript for a selector using parameters passed in as part of the request.
 * 
 * This class has convenience methods to facilitate the template.
 * 
 * @author mtaal
 */
public class SelectorComponent extends BaseTemplateComponent {

  public static final String SELECTOR_ITEM_PARAMETER = "IsSelectorItem";

  private static final String CSSSIZE = "CssSize";
  private static final String ONECELL = "OneCell";
  private static final String TWOCELLS = "TwoCells";
  private static final String THREECELLS = "ThreeCells";
  private static final String FOURCELLS = "FourCells";
  private static final String FIVECELLS = "FiveCells";
  private static final String CUSTOM_QUERY_DS = "F8DD408F2F3A414188668836F84C21AF";

  private org.openbravo.userinterface.selector.Selector selector;
  private List<SelectorField> selectorFields;
  private List<SelectorFieldTrl> selectorFieldTrls;
  private static OutSelectorField IdOutField;
  private static OutSelectorField IdentifierOutField;
  private String transformedColumnName = null;

  private static Logger log = Logger.getLogger(SelectorComponent.class);

  static {
    IdOutField = new OutSelectorField();
    IdOutField.setOutFieldName(JsonConstants.ID);
    IdOutField.setTabFieldName("");
    IdOutField.setOutSuffix("");

    IdentifierOutField = new OutSelectorField();
    IdentifierOutField.setOutFieldName(JsonConstants.IDENTIFIER);
    IdentifierOutField.setTabFieldName("");
    IdentifierOutField.setOutSuffix("");
  }

  private static DomainType getDomainType(SelectorField selectorField) {
    if (selectorField.getObuiselSelector().getTable() != null
        && selectorField.getProperty() != null) {
      final String entityName = selectorField.getObuiselSelector().getTable().getName();
      final Entity entity = ModelProvider.getInstance().getEntity(entityName);
      final Property property = DalUtil.getPropertyFromPath(entity, selectorField.getProperty());
      Check.isNotNull(property, "Property " + selectorField.getProperty() + " not found in Entity "
          + entity);
      return property.getDomainType();
    } else if (selectorField.getObuiselSelector().getTable() != null
        && selectorField.getObuiselSelector().isCustomQuery()
        && selectorField.getReference() != null) {
      return getDomainType(selectorField.getReference().getId());
    } else if (selectorField.getObserdsDatasourceField().getReference() != null) {
      return getDomainType(selectorField.getObserdsDatasourceField().getReference().getId());
    }
    return null;
  }

  private static DomainType getDomainType(String referenceId) {
    final Reference reference = ModelProvider.getInstance().getReference(referenceId);
    Check.isNotNull(reference, "No reference found for referenceid " + referenceId);
    return reference.getDomainType();
  }

  private static String getPropertyOrDataSourceField(SelectorField selectorField) {
    if (selectorField.getProperty() != null) {
      return selectorField.getProperty();
    }
    if (selectorField.getDisplayColumnAlias() != null) {
      return selectorField.getDisplayColumnAlias();
    }
    if (selectorField.getObserdsDatasourceField() != null) {
      return selectorField.getObserdsDatasourceField().getName();
    }
    throw new IllegalStateException("Selectorfield " + selectorField
        + " has a null datasource and a null property");
  }

  public static String getAdditionalProperties(Selector selector, boolean onlyDisplayField) {
    if (onlyDisplayField
        && (selector.getDisplayfield() == null || !selector.getDisplayfield().isActive())) {
      return "";
    }
    final StringBuilder extraProperties = new StringBuilder();
    for (SelectorField selectorField : selector.getOBUISELSelectorFieldList()) {
      if (onlyDisplayField && selectorField != selector.getDisplayfield()) {
        continue;
      }
      if (!selectorField.isActive()) {
        continue;
      }
      String fieldName = getPropertyOrDataSourceField(selectorField);
      final DomainType domainType = getDomainType(selectorField);
      if (domainType instanceof ForeignKeyDomainType) {
        fieldName = fieldName + "." + JsonConstants.IDENTIFIER;
      }
      if (extraProperties.length() > 0) {
        extraProperties.append(",");
      }
      extraProperties.append(fieldName);
    }
    return extraProperties.toString();
  }

  @Override
  protected Template getComponentTemplate() {
    return getSelector().getObclkerTemplate();
  }

  @Inject
  @ComponentProvider.Qualifier(DataSourceConstants.DS_COMPONENT_TYPE)
  private ComponentProvider componentProvider;

  public boolean isSelectorItem() {
    return hasParameter(SELECTOR_ITEM_PARAMETER);
  }

  public Module getModule() {
    return getSelector().getModule();
  }

  /**
   * Computes the field in the popup which can receive the value entered by the user in the
   * suggestion box, to set the first default filter.
   * 
   * @return the field in the popup to set.
   */
  public String getDefaultPopupFilterField() {
    if (getSelector().isCustomQuery()) {
      if (getSelector().getDisplayfield() != null
          && getSelector().getDisplayfield().getDisplayColumnAlias() != null) {
        return getSelector().getDisplayfield().getDisplayColumnAlias();
      }
      return JsonConstants.IDENTIFIER;
    }
    if (getSelector().getDisplayfield() != null && getSelector().getDisplayfield().isShowingrid()) {
      if (getSelector().getDisplayfield().getProperty() != null) {
        return getSelector().getDisplayfield().getProperty();
      } else {
        return getSelector().getDisplayfield().getObserdsDatasourceField().getName();
      }
    }
    // a very common case, return the first selector field which is part of the
    // identifier
    if (getSelector().getDisplayfield() == null
        || getSelector().getDisplayfield().getProperty().equals(JsonConstants.IDENTIFIER)) {
      final Entity entity = getEntity();
      if (entity != null) {
        for (Property prop : entity.getIdentifierProperties()) {
          for (SelectorField selectorField : getActiveSelectorFields()) {
            if (selectorField.getProperty() != null
                && selectorField.getProperty().equals(prop.getName())) {
              return selectorField.getProperty();
            }
          }
        }
      }
    }
    return JsonConstants.IDENTIFIER;
  }

  public org.openbravo.userinterface.selector.Selector getSelector() {
    if (selector == null) {
      selector = OBDal.getInstance().get(org.openbravo.userinterface.selector.Selector.class,
          getId());
      Check.isNotNull(selector, "No selector found using id " + getId());
      Check.isTrue(selector.isActive(), "Selector " + selector + " is not active anymore");
    }
    return selector;
  }

  private List<SelectorField> getActiveSelectorFields() {
    if (selectorFields == null) {
      selectorFields = OBDao.getActiveOBObjectList(getSelector(),
          Selector.PROPERTY_OBUISELSELECTORFIELDLIST);
    }
    return selectorFields;
  }

  public String getColumnName() {
    Check.isTrue(hasParameter(SelectorConstants.PARAM_COLUMN_NAME), "The "
        + SelectorConstants.PARAM_COLUMN_NAME + " parameter must be set");
    return getParameter(SelectorConstants.PARAM_COLUMN_NAME);
  }

  public String getComboReload() {
    if (!hasParameter(SelectorConstants.PARAM_COMBO_RELOAD)) {
      return "null";
    }
    Check.isTrue(hasParameter(SelectorConstants.PARAM_TAB_ID), "The "
        + SelectorConstants.PARAM_TAB_ID + " parameter must be set");
    final String tabId = getParameter(SelectorConstants.PARAM_TAB_ID);
    return "function(name){reloadComboReloads" + tabId + "(name);}";
  }

  public String getDisabled() {
    if (hasParameter(SelectorConstants.PARAM_DISABLED)) {
      return getParameter(SelectorConstants.PARAM_DISABLED);
    }
    return Boolean.FALSE.toString();
  }

  public String getTargetPropertyName() {
    if (hasParameter(SelectorConstants.PARAM_TARGET_PROPERTY_NAME)) {
      return getParameter(SelectorConstants.PARAM_TARGET_PROPERTY_NAME);
    }
    return "";
  }

  public String getValueField() {
    if (getSelector().getValuefield() != null) {
      String valueField = getPropertyOrDataSourceField(getSelector().getValuefield());
      if (!getSelector().isCustomQuery()) {
        final DomainType domainType = getDomainType(getSelector().getValuefield());
        if (domainType instanceof ForeignKeyDomainType) {
          return valueField + "." + JsonConstants.ID;
        }
      }
      return valueField;
    }

    if (getSelector().getObserdsDatasource() != null) {
      final DataSource dataSource = getSelector().getObserdsDatasource();
      // a complete manual datasource which does not have a table
      // and which has a field defined
      if (dataSource.getTable() == null && !dataSource.getOBSERDSDatasourceFieldList().isEmpty()) {
        final DatasourceField dsField = dataSource.getOBSERDSDatasourceFieldList().get(0);
        return dsField.getName();
      }
    }

    return JsonConstants.ID;
  }

  public String getDisplayField() {
    if (getSelector().getDisplayfield() != null) {
      return getPropertyOrDataSourceField(getSelector().getDisplayfield());
    }

    // try to be intelligent when there is a datasource
    if (getSelector().getObserdsDatasource() != null) {
      final DataSource dataSource = getSelector().getObserdsDatasource();
      // a complete manual datasource which does not have a table
      // and which has a field defined
      if (dataSource.getTable() == null && !dataSource.getOBSERDSDatasourceFieldList().isEmpty()) {
        final DatasourceField dsField = dataSource.getOBSERDSDatasourceFieldList().get(0);
        return dsField.getName();
      }
    }

    // in all other cases use an identifier
    return JsonConstants.IDENTIFIER;
  }

  private boolean isBoolean(SelectorField selectorField) {
    final DomainType domainType = getDomainType(selectorField);
    if (domainType instanceof PrimitiveDomainType) {
      final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) domainType;
      return boolean.class == primitiveDomainType.getPrimitiveType()
          || Boolean.class == primitiveDomainType.getPrimitiveType();
    }
    return false;
  }

  public String getRequired() {
    if (hasParameter(SelectorConstants.PARAM_REQUIRED)) {
      return getParameter(SelectorConstants.PARAM_REQUIRED);
    }
    return Boolean.FALSE.toString();
  }

  public String getCallOut() {
    if (hasParameter(SelectorConstants.PARAM_CALLOUT)) {
      return getParameter(SelectorConstants.PARAM_CALLOUT);
    }
    return "null";
  }

  /**
   * @return true if there is at least one active field shown in grid
   */
  public String getShowSelectorGrid() {
    if (OBDao.getFilteredCriteria(SelectorField.class,
        Restrictions.eq(SelectorField.PROPERTY_OBUISELSELECTOR, getSelector()),
        Restrictions.eq(SelectorField.PROPERTY_SHOWINGRID, true)).count() > 0) {
      return Boolean.TRUE.toString();
    }
    return Boolean.FALSE.toString();
  }

  public String getWhereClause() {
    return getSafeValue(getSelector().getHQLWhereClause());
  }

  public String getTitle() {
    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
    String description = null;
    for (SelectorTrl selectorTrl : getSelector().getOBUISELSelectorTrlList()) {
      final String trlLanguageId = (String) DalUtil.getId(selectorTrl.getLanguageID());
      if (trlLanguageId.equals(userLanguageId)) {
        description = selectorTrl.getDescription();
      }
    }
    if (description != null) {
      return description;
    }

    description = getSelector().getDescription();
    if (description == null) {
      return "";
    }
    return description;
  }

  public String getDataSourceJavascript() {
    final String dataSourceId;

    if (getSelector().getObserdsDatasource() != null) {
      dataSourceId = getSelector().getObserdsDatasource().getId();
    } else if (getSelector().isCustomQuery()) {
      dataSourceId = CUSTOM_QUERY_DS;
    } else {
      Check.isNotNull(getSelector().getTable(),
          "Both the datasource and table are null for this selector: " + selector);
      dataSourceId = getSelector().getTable().getName();
    }

    final Map<String, Object> dsParameters = new HashMap<String, Object>(getParameters());
    dsParameters.put(DataSourceConstants.DS_ONLY_GENERATE_CREATESTATEMENT, true);
    dsParameters.put(DataSourceConstants.MINIMAL_PROPERTY_OUTPUT, true);
    final String extraProperties = getAdditionalProperties(getSelector(), false);
    if (extraProperties.length() > 0) {
      dsParameters.put(JsonConstants.ADDITIONAL_PROPERTIES_PARAMETER, extraProperties.toString());
    }

    final Component component = componentProvider.getComponent(dataSourceId, dsParameters);

    return component.generate();
  }

  public String getNumCols() {
    final String cssSize = getParameter(CSSSIZE);
    if (cssSize == null) {
      return "2";
    }
    if (cssSize.equals(ONECELL)) {
      return "1";
    } else if (cssSize.equals(TWOCELLS)) {
      return "2";
    } else if (cssSize.equals(THREECELLS)) {
      return "3";
    } else if (cssSize.equals(FOURCELLS)) {
      return "4";
    } else if (cssSize.equals(FIVECELLS)) {
      return "5";
    }
    return "2";
  }

  public String getExtraSearchFields() {
    final String displayField = getDisplayField();
    final StringBuilder sb = new StringBuilder();
    for (SelectorField selectorField : getActiveSelectorFields()) {
      String fieldName = getPropertyOrDataSourceField(selectorField);
      if (fieldName.equals(displayField)) {
        continue;
      }
      // prevent booleans as search fields, they don't work
      if (selectorField.isSearchinsuggestionbox() && !isBoolean(selectorField)) {

        // handle the case that the field is a foreign key
        // in that case always show the identifier
        final DomainType domainType = getDomainType(selectorField);
        if (domainType instanceof ForeignKeyDomainType) {
          fieldName = fieldName + "." + JsonConstants.IDENTIFIER;
        }

        if (sb.length() > 0) {
          sb.append(",");
        }
        sb.append("'" + fieldName + "'");
      }
    }
    return sb.toString();
  }

  /*
   * Utility function to get list of fields in a tab which are targets of some selector out field.
   * Done this way as main user (called from View generation) did already retrieve tab+list of
   * fields
   */
  private List<Field> getOutFieldListForSelectorField(String tabId, String selectorFieldId) {
    Tab tab = OBDal.getInstance().get(Tab.class, tabId);
    List<Field> tabFields = tab.getADFieldList();
    List<Field> result = new ArrayList<Field>();
    for (Field f : tabFields) {
      if (f.getObuiselOutfield() != null) {
        String outFieldId = (String) DalUtil.getId(f.getObuiselOutfield());
        if (outFieldId.equals(selectorFieldId)) {
          result.add(f);
        }
      }
    }
    return result;
  }

  public List<OutSelectorField> getOutFields() {
    List<OutSelectorField> outFields = new ArrayList<OutSelectorField>();
    final List<SelectorField> sortedFields = new ArrayList<SelectorField>(getActiveSelectorFields());

    final String tabId = getParameter(SelectorConstants.PARAM_TAB_ID);

    Collections.sort(sortedFields, new SelectorFieldComparator());

    outFields.add(SelectorComponent.IdOutField);
    outFields.add(SelectorComponent.IdentifierOutField);

    try {
      OBContext.setAdminMode();
      for (SelectorField selectorField : sortedFields) {
        if (Boolean.TRUE.equals(selectorField.isOutfield())) {
          if (tabId.equals("")) {
            final OutSelectorField outField = new OutSelectorField();
            outField.setOutFieldName(getPropertyOrDataSourceField(selectorField));
            outField.setTabFieldName("");
            outFields.add(outField);
            outField.setOutSuffix("");
          } else {
            List<Field> outFieldTargetFields = getOutFieldListForSelectorField(tabId,
                selectorField.getId());

            if (outFieldTargetFields.size() == 0 && selectorField.getSuffix() != null
                && !selectorField.getSuffix().equals("")) {
              // "out-fields" with a suffix not necessarily are associated with a field in the tab
              final OutSelectorField outField = new OutSelectorField();
              outField.setOutFieldName(getPropertyOrDataSourceField(selectorField));
              outField.setTabFieldName(getPropertyOrDataSourceField(selectorField));
              outField.setOutSuffix((selectorField.getSuffix() == null ? "" : selectorField
                  .getSuffix()));
              outFields.add(outField);
            }
            for (Field associatedField : outFieldTargetFields) {
              if (associatedField == null) {
                continue;
              }
              if (associatedField.getTab().getId().equals(tabId)) {
                final OutSelectorField outField = new OutSelectorField();
                outField.setOutFieldName(getPropertyOrDataSourceField(selectorField));
                if (getParameter("isSelectorItem") != null) {
                  final Property property = KernelUtils.getInstance().getPropertyFromColumn(
                      associatedField.getColumn(), false);
                  outField.setTabFieldName(property.getName());
                  UIDefinition uiDef = UIDefinitionController.getInstance().getUIDefinition(
                      associatedField.getColumn().getId());
                  if (uiDef instanceof NumberUIDefinition) {
                    String formatStr = ((NumberUIDefinition) uiDef).getFormat();
                    setOutFieldFormat(outField, formatStr);
                  }
                } else {
                  // classic mode
                  outField.setTabFieldName(associatedField.getColumn().getName());
                }
                outField.setOutSuffix((selectorField.getSuffix() == null ? "" : selectorField
                    .getSuffix()));
                outFields.add(outField);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Error gettting out fields for selector "
          + getParameter(SelectorConstants.PARAM_TAB_ID) + ": " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return outFields;
  }

  private void setOutFieldFormat(OutSelectorField outField, String formatStr) {
    outField.setFormatType(formatStr);
  }

  public String getOutHiddenInputPrefix() {
    if (transformedColumnName == null) {
      transformedColumnName = "inp" + Sqlc.TransformaNombreColumna(getColumnName());
    }
    return transformedColumnName;
  }

  public Map<String, String> getHiddenInputs() {
    final Map<String, String> hiddenInputs = new HashMap<String, String>();

    if (getSelector().getTable() == null) {
      return hiddenInputs;
    }

    final String getElementString = "document.getElementById('@id@')";
    final String columnName = getOutHiddenInputPrefix();

    OBContext.setAdminMode();
    try {
      final Criterion selectorConstraint = Restrictions.eq(SelectorField.PROPERTY_OBUISELSELECTOR,
          getSelector());
      final Criterion isOutFieldConstraint = Restrictions.eq(SelectorField.PROPERTY_ISOUTFIELD,
          true);
      final Criterion hasSuffixConstraint = Restrictions.isNotNull(SelectorField.PROPERTY_SUFFIX);
      List<SelectorField> fields = OBDao.getFilteredCriteria(SelectorField.class,
          selectorConstraint, isOutFieldConstraint, hasSuffixConstraint).list();
      for (final SelectorField field : fields) {
        hiddenInputs.put(columnName + field.getSuffix(),
            getElementString.replaceAll("@id@", columnName + field.getSuffix()));
      }
    } catch (Exception e) {
      log.error("Error getting hidden input for selector "
          + getParameter(SelectorConstants.PARAM_TAB_ID) + ": " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return hiddenInputs;
  }

  public List<LocalSelectorField> getPickListFields() {
    // return the displayfield as the first column of the picklist and add all the extra fields with
    // the showInPicklist flag.
    List<LocalSelectorField> pickListFields = new ArrayList<LocalSelectorField>();
    final String displayField = getDisplayField();
    final LocalSelectorField localSelectorField = new LocalSelectorField();
    localSelectorField.setName(displayField);
    localSelectorField.setPickListField(true);
    localSelectorField.setSelectorItem(isSelectorItem());
    pickListFields.add(localSelectorField);
    pickListFields.addAll(getSelectorFields(true, false));
    return pickListFields;
  }

  public List<LocalSelectorField> getSelectorGridFields() {
    return getSelectorFields(false, true);
  }

  private List<LocalSelectorField> getSelectorFields(boolean pickList, boolean popupGrid) {
    final List<LocalSelectorField> result = new ArrayList<LocalSelectorField>();

    final List<SelectorField> sortedFields = new ArrayList<SelectorField>(getActiveSelectorFields());
    Collections.sort(sortedFields, new SelectorFieldComparator());

    for (SelectorField selectorField : sortedFields) {
      if (popupGrid && !selectorField.isShowingrid()) {
        continue;
      }
      if (pickList
          && (!selectorField.isShowInPicklist() || selectorField.equals(getSelector()
              .getDisplayfield()))) {
        continue;
      }
      final LocalSelectorField localSelectorField = new LocalSelectorField();
      localSelectorField.setPickListField(pickList);
      String fieldName = getPropertyOrDataSourceField(selectorField);

      // handle the case that the field is a foreign key
      // in that case always show the identifier
      final DomainType domainType = getDomainType(selectorField);
      if (domainType instanceof ForeignKeyDomainType) {
        String displayField = fieldName + "." + JsonConstants.IDENTIFIER;
        localSelectorField.setDisplayField(displayField);
      }

      localSelectorField.setName(fieldName);
      localSelectorField.setTitle(getTranslatedName(selectorField));
      localSelectorField.setSort(!pickList && selectorField.isSortable());
      localSelectorField.setSelectorItem(isSelectorItem());

      localSelectorField.setFilter(!pickList && selectorField.isFilterable());
      localSelectorField.setDomainType(domainType);
      localSelectorField.setUIDefinition(getUIDefinition(selectorField));
      localSelectorField.setSelectorField(selectorField);

      // determine format
      // if (selectorField.getProperty() != null) {
      // selectorField.getProperty()
      // }

      result.add(localSelectorField);
    }
    return result;
  }

  private List<SelectorFieldTrl> getTranslatedFields() {
    if (selectorFieldTrls != null) {
      return selectorFieldTrls;
    }

    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();

    final String whereClause = " where " + SelectorFieldTrl.PROPERTY_OBUISELSELECTORFIELD + "."
        + SelectorField.PROPERTY_OBUISELSELECTOR + "=:selector and "
        + SelectorFieldTrl.PROPERTY_LANGUAGEID + ".id=:userLanguageId ";

    final OBQuery<SelectorFieldTrl> qry = OBDal.getInstance().createQuery(SelectorFieldTrl.class,
        whereClause);
    qry.setNamedParameter("selector", getSelector());
    qry.setNamedParameter("userLanguageId", userLanguageId);
    selectorFieldTrls = qry.list();

    return selectorFieldTrls;
  }

  private String getTranslatedName(SelectorField selectorField) {
    final SelectorFieldTrl trl = getTranslation(selectorField);
    if (trl == null) {
      return selectorField.getName();
    }
    return trl.getName();
  }

  private SelectorFieldTrl getTranslation(SelectorField selectorField) {

    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();

    for (SelectorFieldTrl selectorFieldTrl : getTranslatedFields()) {
      if (DalUtil.getId(selectorFieldTrl.getObuiselSelectorField()).equals(selectorField.getId())) {
        final String trlLanguageId = (String) DalUtil.getId(selectorFieldTrl.getLanguageID());
        if (trlLanguageId.equals(userLanguageId)) {
          return selectorFieldTrl;
        }
      }
    }

    return null;
  }

  // Used for create a map Out field - Tab field
  public static class OutSelectorField {
    private String tabFieldName;
    private String outFieldName;
    private String suffix;
    private String formatType = "";

    public String getTabFieldName() {
      return tabFieldName;
    }

    public void setTabFieldName(String tabFieldName) {
      this.tabFieldName = tabFieldName;
    }

    public String getOutFieldName() {
      return outFieldName;
    }

    public void setOutFieldName(String outFieldName) {
      this.outFieldName = outFieldName;
    }

    public void setOutSuffix(String suffix) {
      this.suffix = suffix;
    }

    public String getOutSuffix() {
      return suffix;
    }

    public String getFormatType() {
      return formatType;
    }

    public void setFormatType(String formatType) {
      this.formatType = formatType;
    }

  }

  // used to create picklist and grid fields
  public static class LocalSelectorField {
    private String title = " ";
    private String name;
    private String displayField;
    private boolean filter;
    private boolean sort;
    private boolean isSelectorItem;
    private DomainType domainType;
    private UIDefinition uiDefinition;
    private SelectorField selectorField;
    private boolean pickListField;

    public DomainType getDomainType() {
      return domainType;
    }

    public void setDomainType(DomainType domainType) {
      this.domainType = domainType;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDisplayField() {
      return displayField;
    }

    public void setDisplayField(String displayField) {
      this.displayField = displayField;
    }

    public String getFilterEditorProperties() {
      if (getUIDefinition() != null) {
        if (getUIDefinition() instanceof FKComboUIDefinition && isSelectorItem) {
          return ", filterOperator: 'equals', filterOnKeypress: true, canFilter:true, required: false, filterEditorType: 'OBSelectorFilterSelectItem', filterEditorProperties: {entity: '"
              + getEntityName() + "', displayField: '" + JsonConstants.IDENTIFIER + "'}";
        }
        return getUIDefinition().getFilterEditorProperties(null);
      }
      return ", filterEditorType: 'OBTextItem'";
    }

    public List<LocalSelectorFieldProperty> getProperties() {
      final List<LocalSelectorFieldProperty> result = new ArrayList<LocalSelectorFieldProperty>();
      result.add(createLocalSelectorFieldProperty("title", title));
      result.add(createLocalSelectorFieldProperty("name", name));
      // is used at runtime to set canFilter on false for a field
      if (!isPickListField()) {
        if (!filter) {
          result.add(createLocalSelectorFieldProperty("disableFilter", !filter));
        }
        if (!sort) {
          result.add(createLocalSelectorFieldProperty("canSort", sort));
        }
      }
      result.add(createLocalSelectorFieldProperty("type", getType()));
      if ((domainType instanceof PrimitiveDomainType)) {
        final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) domainType;
        if (Date.class.isAssignableFrom(primitiveDomainType.getPrimitiveType())) {
          // TODO: hardcoded width for date
          result.add(createLocalSelectorFieldProperty("width", 100));
        }
      }
      if (domainType instanceof ForeignKeyDomainType) {
        result.add(createLocalSelectorFieldProperty("displayField", displayField));
      }
      return result;
    }

    private LocalSelectorFieldProperty createLocalSelectorFieldProperty(String propName,
        Object propValue) {
      LocalSelectorFieldProperty localSelectorFieldProperty = new LocalSelectorFieldProperty();
      localSelectorFieldProperty.setName(propName);
      if (propValue instanceof String) {
        localSelectorFieldProperty.setStringValue((String) propValue);
      } else {
        localSelectorFieldProperty.setValue("" + propValue);
      }
      return localSelectorFieldProperty;
    }

    public boolean isFilter() {
      return filter;
    }

    public void setFilter(boolean filter) {
      this.filter = filter;
    }

    public boolean isSort() {
      return sort;
    }

    public void setSort(boolean sort) {
      this.sort = sort;
    }

    public void setSelectorItem(boolean isSelectorItem) {
      this.isSelectorItem = isSelectorItem;
    }

    public static class LocalSelectorFieldProperty {
      private String name;
      private String value;

      public String getName() {
        return name;
      }

      public void setName(String name) {
        this.name = name;
      }

      public String getValue() {
        return value;
      }

      public void setStringValue(String value) {
        this.value = "'" + value + "'";
      }

      public void setValue(String value) {
        this.value = value;
      }
    }

    public String getType() {
      if (getUIDefinition() == null) {
        return "text";
      }
      return getUIDefinition().getName();
    }

    public UIDefinition getUIDefinition() {
      return uiDefinition;
    }

    public void setUIDefinition(UIDefinition uiDefinition) {
      this.uiDefinition = uiDefinition;
    }

    public void setSelectorField(SelectorField selectorField) {
      this.selectorField = selectorField;
    }

    public SelectorField getSelectorField() {
      return selectorField;
    }

    public String getEntityName() {
      if (selectorField == null) {
        return null;
      }
      if (selectorField.getObuiselSelector().getTable() != null
          && selectorField.getProperty() != null) {
        final String entityName = selectorField.getObuiselSelector().getTable().getName();
        final Entity entity = ModelProvider.getInstance().getEntity(entityName);
        final Property property = DalUtil.getPropertyFromPath(entity, selectorField.getProperty());
        return property.getTargetEntity().getName();
      }
      return null;
    }

    public boolean isPickListField() {
      return pickListField;
    }

    public void setPickListField(boolean pickListField) {
      this.pickListField = pickListField;
    }

  }

  /**
   * Compares/sorts SelectorField on the {@link SelectorField#getSortno()} property.
   * 
   * @author mtaal
   */
  private class SelectorFieldComparator implements Comparator<SelectorField> {

    @Override
    public int compare(SelectorField field0, SelectorField field1) {
      return (int) (field0.getSortno() - field1.getSortno());
    }

  }

  private UIDefinition getUIDefinition(SelectorField selectorField) {
    if (selectorField.getObuiselSelector().getTable() != null
        && selectorField.getProperty() != null) {
      final String entityName = selectorField.getObuiselSelector().getTable().getName();
      final Entity entity = ModelProvider.getInstance().getEntity(entityName);
      final Property property = DalUtil.getPropertyFromPath(entity, selectorField.getProperty());
      return UIDefinitionController.getInstance().getUIDefinition(property.getColumnId());
    } else if (selectorField.getObuiselSelector().getTable() != null
        && selectorField.getObuiselSelector().isCustomQuery()
        && selectorField.getReference() != null) {
      return UIDefinitionController.getInstance().getUIDefinition(selectorField.getReference());
    } else if (selectorField.getObserdsDatasourceField().getReference() != null) {
      return UIDefinitionController.getInstance().getUIDefinition(
          selectorField.getObserdsDatasourceField().getReference());
    }
    return null;
  }

  private Entity getEntity() {
    if (getSelector().getTable() != null) {
      final String entityName = getSelector().getTable().getName();
      return ModelProvider.getInstance().getEntity(entityName);
    } else if (getSelector().getObserdsDatasource().getTable() != null) {
      final String entityName = getSelector().getObserdsDatasource().getTable().getName();
      return ModelProvider.getInstance().getEntity(entityName);
    }
    return null;
  }
}
