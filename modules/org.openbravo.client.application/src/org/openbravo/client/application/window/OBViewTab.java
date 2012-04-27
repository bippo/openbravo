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
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.application.ApplicationConstants;
import org.openbravo.client.application.ApplicationUtils;
import org.openbravo.client.application.DynamicExpressionParser;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.Template;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.model.ad.domain.ModelImplementationMapping;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.TabTrl;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.service.datasource.DataSourceConstants;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.utils.FormatUtilities;

/**
 * Represents the Openbravo Tab (form and grid combination).
 * 
 * @author mtaal
 */
public class OBViewTab extends BaseTemplateComponent {

  private static final Logger log = Logger.getLogger(OBViewTab.class);
  private static final String DEFAULT_TEMPLATE_ID = "B5124C0A450D4D3A867AEAC7DF64D6F0";
  protected static final Map<String, String> TEMPLATE_MAP = new HashMap<String, String>();

  static {
    // Map: WindowType - Template
    TEMPLATE_MAP.put("OBUIAPP_PickAndExecute", "FF808181330BD14F01330BD34EA00008");
  }

  private Entity entity;
  private Tab tab;
  private String tabTitle;
  private List<OBViewTab> childTabs = new ArrayList<OBViewTab>();
  private OBViewTab parentTabComponent;
  private String parentProperty = null;
  private List<ButtonField> buttonFields = null;
  private List<IconButton> iconButtons = null;
  private boolean buttonSessionLogic;
  private boolean isRootTab;
  private String uniqueString = "" + System.currentTimeMillis();

  @Inject
  private OBViewFieldHandler fieldHandler;

  @Inject
  @ComponentProvider.Qualifier(DataSourceConstants.DS_COMPONENT_TYPE)
  private ComponentProvider dsComponentProvider;

  public String getDataSourceJavaScript() {
    final String dsId = getDataSourceId();
    final Map<String, Object> dsParameters = new HashMap<String, Object>(getParameters());
    dsParameters.put(DataSourceConstants.DS_ONLY_GENERATE_CREATESTATEMENT, true);
    if ("OBUIAPP_PickAndExecute".equals(tab.getWindow().getWindowType())) {
      dsParameters.put(DataSourceConstants.DS_CLASS_NAME, "OBPickAndExecuteDataSource");
    } else {
      dsParameters.put(DataSourceConstants.DS_CLASS_NAME, "OBViewDataSource");
    }
    dsParameters.put(DataSourceConstants.MINIMAL_PROPERTY_OUTPUT, true);
    final Component component = dsComponentProvider.getComponent(dsId, dsParameters);
    return component.generate();
  }

  protected Template getComponentTemplate() {
    final String windowType = tab.getWindow().getWindowType();
    if (TEMPLATE_MAP.containsKey(windowType)) {
      return OBDal.getInstance().get(Template.class, TEMPLATE_MAP.get(windowType));
    }
    return OBDal.getInstance().get(Template.class, DEFAULT_TEMPLATE_ID);
  }

  public OBViewFieldHandler getFieldHandler() {
    return fieldHandler;
  }

  public List<OtherField> getOtherFields() {
    final List<OtherField> otherFields = new ArrayList<OBViewTab.OtherField>();
    for (Field fld : fieldHandler.getIgnoredFields()) {
      if (fld.getColumn() == null) {
        continue;
      }
      otherFields.add(new OtherField(fld.getColumn()));
    }

    // Adding PK as additional field
    if (entity.getIdProperties().size() == 1) {
      Property pkProperty = entity.getIdProperties().get(0);
      OtherField pkField = new OtherField(pkProperty);
      pkField.inpColumnName = pkProperty.getColumnName();
      pkField.session = true;
      otherFields.add(pkField);
    }
    return otherFields;
  }

  public void addChildTabComponent(OBViewTab childTabComponent) {
    childTabComponent.setParentTabComponent(this);
    childTabs.add(childTabComponent);
  }

  public boolean getDefaultEditMode() {
    return tab.isDefaultEditMode() == null ? false : tab.isDefaultEditMode();
  }

  public String getMapping250() {
    return Utility.getTabURL(tab.getId(), "none", false);
  }

  public List<ButtonField> getButtonFields() {
    if (buttonFields != null) {
      return buttonFields;
    }
    buttonFields = new ArrayList<ButtonField>();
    final List<Field> adFields = new ArrayList<Field>(tab.getADFieldList());
    Collections.sort(adFields, new FormFieldComparator());
    for (Field fld : adFields) {
      if (fld.isActive() && fld.isDisplayed()) {
        if (!(ApplicationUtils.isUIButton(fld))) {
          continue;
        }
        ButtonField btn = new ButtonField(fld);
        buttonFields.add(btn);
        if (btn.sessionLogic) {
          buttonSessionLogic = true;
        }
      }
    }
    return buttonFields;
  }

  public List<IconButton> getIconButtons() {
    if (iconButtons != null) {
      return iconButtons;
    }

    iconButtons = new ArrayList<IconButton>();

    // Print/email button
    if (tab.getProcess() != null) {
      iconButtons.addAll(getPrintEmailButtons());
    }

    // Audit trail button
    if (!ActivationKey.getInstance().isActive() || tab.getTable().isFullyAudited()) {
      IconButton auditBtn = new IconButton();
      auditBtn.type = "audit";
      auditBtn.label = Utility.messageBD(new DalConnectionProvider(false), "AuditTrail", OBContext
          .getOBContext().getLanguage().getLanguage());
      auditBtn.action = "OB.ToolbarUtils.showAuditTrail(this.view);";
      iconButtons.add(auditBtn);
    }

    // Tree button
    if (tab.isTreeIncluded()) {
      IconButton treeBtn = new IconButton();
      treeBtn.type = "tree";
      treeBtn.label = Utility.messageBD(new DalConnectionProvider(false), "Tree", OBContext
          .getOBContext().getLanguage().getLanguage());
      treeBtn.action = "OB.ToolbarUtils.showTree(this.view);";
      iconButtons.add(treeBtn);
    }

    return iconButtons;
  }

  private Collection<? extends IconButton> getPrintEmailButtons() {
    List<IconButton> btns = new ArrayList<IconButton>();

    PrintButton printBtn = new PrintButton();
    btns.add(printBtn);

    if (printBtn.hasEmail) {
      IconButton emailBtn = new IconButton();
      emailBtn.type = "email";
      emailBtn.label = Utility.messageBD(new DalConnectionProvider(false), "Email", OBContext
          .getOBContext().getLanguage().getLanguage());
      emailBtn.action = printBtn.action.replace("print.html", "send.html");
      btns.add(emailBtn);
    }

    return btns;
  }

  public String getParentProperty() {
    if (parentTabComponent == null) {
      return "";
    }
    if (parentProperty != null) {
      return parentProperty;
    }
    if (tab.getTable().getId().equals(parentTabComponent.getTab().getTable().getId())) {
      parentProperty = getEntity().getIdProperties().get(0).getName();
    } else {
      parentProperty = ApplicationUtils.getParentProperty(tab, parentTabComponent.getTab());
    }
    return parentProperty;
  }

  public boolean getDeleteableTable() {
    return tab.getTable().isDeletableRecords();
  }

  public String getViewForm() {
    // force a load all the columns of the table
    getTab().getTable().getADColumnList().size();

    final OBViewFormComponent viewFormComponent = createComponent(OBViewFormComponent.class);
    viewFormComponent.setParameters(getParameters());
    viewFormComponent.setParentProperty(getParentProperty());
    viewFormComponent.setFieldHandler(fieldHandler);
    return viewFormComponent.generate();
  }

  public String getViewGrid() {
    // force a load all the columns of the table
    getTab().getTable().getADColumnList().size();

    final OBViewGridComponent viewGridComponent = createComponent(OBViewGridComponent.class);
    viewGridComponent.setParameters(getParameters());
    viewGridComponent.setTab(tab);
    viewGridComponent.setViewTab(this);
    viewGridComponent.setApplyTransactionalFilter(isRootTab()
        && this.tab.getWindow().getWindowType().equals("T"));
    return viewGridComponent.generate();
  }

  public OBViewTab getParentTabComponent() {
    return parentTabComponent;
  }

  public void setParentTabComponent(OBViewTab parentTabComponent) {
    this.parentTabComponent = parentTabComponent;
  }

  public List<OBViewTab> getChildTabs() {
    return childTabs;
  }

  private boolean hasAlwaysVisibleChildTab() {
    boolean hasVisibleChildTab = false;
    for (OBViewTab childTab : this.getChildTabs()) {
      if (!childTab.getAcctTab() && !childTab.getTrlTab()) {
        hasVisibleChildTab = true;
        break;
      }
    }
    return hasVisibleChildTab;
  }

  private boolean hasAccountingChildTab() {
    boolean hasAccountingChildTab = false;
    for (OBViewTab childTab : this.getChildTabs()) {
      if (childTab.getAcctTab()) {
        hasAccountingChildTab = true;
        break;
      }
    }
    return hasAccountingChildTab;
  }

  private boolean hasTranslationChildTab() {
    boolean hasTranslationChildTab = false;
    for (OBViewTab childTab : this.getChildTabs()) {
      if (childTab.getTrlTab()) {
        hasTranslationChildTab = true;
        break;
      }
    }
    return hasTranslationChildTab;
  }

  public String getHasChildTabsProperty() {
    String hasChildTabs = null;
    if (this.hasAlwaysVisibleChildTab()) {
      hasChildTabs = "true";
    } else {
      boolean hasAcctChildTab = this.hasAccountingChildTab();
      boolean hasTrlChildTab = this.hasTranslationChildTab();
      if (hasAcctChildTab && hasTrlChildTab) {
        hasChildTabs = "(OB.PropertyStore.get('ShowTrl', this.windowId) === 'Y') || (OB.PropertyStore.get('ShowAcct', this.windowId) === 'Y')";
      } else if (hasAcctChildTab) {
        hasChildTabs = "(OB.PropertyStore.get('ShowAcct', this.windowId) === 'Y')";
      } else { // hasTrlChildTab == true
        hasChildTabs = "(OB.PropertyStore.get('ShowTrl', this.windowId) === 'Y')";
      }
    }
    return hasChildTabs;
  }

  public Tab getTab() {
    return tab;
  }

  public void setTab(Tab tab) {
    this.tab = tab;
    fieldHandler.setTab(tab);
  }

  public boolean isTabSet() {
    return tab != null;
  }

  public String getTabId() {
    return tab.getId();
  }

  public String getModuleId() {
    return tab.getModule().getId();
  }

  private Entity getEntity() {
    if (entity == null) {
      entity = ModelProvider.getInstance().getEntity(tab.getTable().getName());
    }
    return entity;
  }

  public String getEntityName() {
    return getEntity().getName();
  }

  public String getTabTitle() {
    if (tabTitle == null) {
      final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
      for (TabTrl tabTrl : tab.getADTabTrlList()) {
        final String trlLanguageId = (String) DalUtil.getId(tabTrl.getLanguage());
        if (trlLanguageId.equals(userLanguageId)) {
          tabTitle = tabTrl.getName();
        }
      }
      if (tabTitle == null) {
        tabTitle = tab.getName();
      }
    }
    return tabTitle;
  }

  public String getDataSourceId() {
    return tab.getTable().getName();
  }

  public String getSelectionFunction() {
    if (tab.getOBUIAPPSelection() != null) {
      return tab.getOBUIAPPSelection();
    }
    return "";
  }

  public void setTabTitle(String tabTitle) {
    this.tabTitle = tabTitle;
  }

  public boolean getAcctTab() {
    return tab.isAccountingTab();
  }

  public boolean getTrlTab() {
    return tab.isTranslationTab();
  }

  public String getTableId() {
    return tab.getTable().getId();
  }

  public Property getKeyProperty() {
    for (Property prop : getEntity().getProperties()) {
      if (prop.isId()) {
        return prop;
      }
    }
    throw new IllegalStateException("Entity " + getEntityName() + " does not have an id property");
  }

  public String getKeyPropertyType() {
    return UIDefinitionController.getInstance().getUIDefinition(getKeyProperty().getColumnId())
        .getName();
  }

  public String getKeyColumnName() {
    return getKeyProperty().getColumnName();
  }

  public String getKeyInpName() {
    return "inp" + Sqlc.TransformaNombreColumna(getKeyProperty().getColumnName());
  }

  public String getWindowId() {
    return tab.getWindow().getId();
  }

  public boolean isButtonSessionLogic() {
    if (buttonFields == null) {
      // Generate buttons fields if they haven't been already generated, to calculate
      // buttonSessionLogic
      getButtonFields();
    }
    return buttonSessionLogic;
  }

  public void setUniqueString(String uniqueString) {
    this.uniqueString = uniqueString;
  }

  public String getProcessViews() {
    StringBuilder views = new StringBuilder();
    for (ButtonField f : getButtonFields()) {
      if ("".equals(f.getWindowId())) {
        continue;
      }
      final StandardWindowComponent processWindow = createComponent(StandardWindowComponent.class);
      processWindow.setParameters(getParameters());
      processWindow.setUniqueString(uniqueString);
      processWindow.setWindow(OBDal.getInstance().get(Window.class, f.getWindowId()));
      views.append(processWindow.generate()).append("\n");
    }
    return views.toString();
  }

  public boolean isAllowAdd() {
    if (tab.isObuiappCanAdd() != null) {
      return tab.isObuiappCanAdd();
    }
    return false;
  }

  public boolean isAllowDelete() {
    if (tab.isObuiappCanDelete() != null) {
      return tab.isObuiappCanDelete();
    }
    return false;
  }

  public boolean isShowSelect() {
    if (tab.isObuiappShowSelect() != null) {
      return tab.isObuiappShowSelect();
    }
    return true;
  }

  public String getNewFunction() {
    if (tab.getOBUIAPPNewFn() != null) {
      return tab.getOBUIAPPNewFn();
    }
    return "";
  }

  public String getRemoveFunction() {
    if (tab.getObuiappRemovefn() != null) {
      return tab.getObuiappRemovefn();
    }
    return "";
  }

  public class ButtonField {
    private static final String AD_DEF_ERROR = "AD definition error: process parameter (%s) is using %s reference without %s";
    private String id;
    private String label;
    private String url;
    private String propertyName;
    private List<Value> labelValues = null;
    private boolean autosave;
    private String showIf = "";
    private String readOnlyIf = "";
    private boolean sessionLogic = false;
    private boolean modal = true;
    private String processId = "";
    private String windowId = "";
    private String windowTitle = "";
    private boolean newDefinition = false;

    public ButtonField(Field fld) {
      id = fld.getId();
      label = OBViewUtil.getLabel(fld);
      Column column = fld.getColumn();

      propertyName = KernelUtils.getInstance().getPropertyFromColumn(column).getName();
      autosave = column.isAutosave();

      // Define command
      Process process = null;

      if (column.getOBUIAPPProcess() != null) {
        // new process definition has more precedence
        org.openbravo.client.application.Process newProcess = column.getOBUIAPPProcess();
        processId = newProcess.getId();
        url = "/";
        command = newProcess.getJavaClassName();
        newDefinition = true;

        if ("OBUIAPP_PickAndExecute".equals(newProcess.getUIPattern())) {
          // TODO: modal should be a parameter in the process definition?
          modal = false;
          for (org.openbravo.client.application.Parameter p : newProcess.getOBUIAPPParameterList()) {
            processParameter(p);
          }
        }
      } else if (column.getProcess() != null) {
        process = column.getProcess();
        String manualProcessMapping = null;
        for (ModelImplementation impl : process.getADModelImplementationList()) {
          if (impl.isDefault()) {
            for (ModelImplementationMapping mapping : impl.getADModelImplementationMappingList()) {
              if (mapping.isDefault()) {
                manualProcessMapping = mapping.getMappingName();
                break;
              }
            }
            break;
          }
        }

        if (manualProcessMapping == null) {
          // Standard UI process
          url = Utility.getTabURL(fld.getTab().getId(), "E", false);
          command = "BUTTON" + FormatUtilities.replace(column.getDBColumnName())
              + column.getProcess().getId();
        } else {
          url = manualProcessMapping;
          command = "DEFAULT";
        }

        modal = Utility.isModalProcess(process);
        processId = process.getId();

      } else {
        String colName = column.getDBColumnName();
        if ("Posted".equalsIgnoreCase(colName) || "CreateFrom".equalsIgnoreCase(colName)) {
          command = "BUTTON" + colName;
          url = Utility.getTabURL(fld.getTab().getId(), "E", false);
        }
      }

      if (labelValues == null) {
        labelValues = new ArrayList<Value>();
        if (column.getReferenceSearchKey() != null) {
          for (org.openbravo.model.ad.domain.List valueList : column.getReferenceSearchKey()
              .getADListList()) {
            labelValues.add(new Value(valueList));
          }
        }
      }

      // Display Logic
      if (fld.getDisplayLogic() != null) {
        final DynamicExpressionParser parser = new DynamicExpressionParser(fld.getDisplayLogic(),
            tab);
        showIf = parser.getJSExpression();
        if (parser.getSessionAttributes().size() > 0) {
          sessionLogic = true;
        }
      }

      // Read only logic
      if (fld.getColumn().getReadOnlyLogic() != null) {
        final DynamicExpressionParser parser = new DynamicExpressionParser(fld.getColumn()
            .getReadOnlyLogic(), tab);
        readOnlyIf = parser.getJSExpression();
        if (parser.getSessionAttributes().size() > 0) {
          sessionLogic = true;
        }
      }
    }

    private void processParameter(org.openbravo.client.application.Parameter p) {

      if (p.getReference().getId().equals(ApplicationConstants.WINDOW_REFERENCE_ID)) {
        if (p.getReferenceSearchKey().getOBUIAPPRefWindowList().size() == 0
            || p.getReferenceSearchKey().getOBUIAPPRefWindowList().get(0).getWindow() == null) {
          log.error(String.format(AD_DEF_ERROR, p.getId(), "Window", "window"));
        } else {
          setWindowId(p.getReferenceSearchKey().getOBUIAPPRefWindowList().get(0).getWindow()
              .getId());
          setWindowTitle(p.getName());
        }
        return;
      } else if (p.getReference().getId().equals(ApplicationConstants.BUTTON_LIST_REFERENCE_ID)) {
        labelValues = new ArrayList<Value>();
        for (org.openbravo.model.ad.domain.List valueList : p.getReferenceSearchKey()
            .getADListList()) {
          labelValues.add(new Value(valueList));
        }
        if (labelValues.size() == 0) {
          log.error(String.format(AD_DEF_ERROR, p.getId(), "Button List", "a list associated"));
        }
        return;
      }
      log.error("Trying to use a yet not implemented reference: " + p.getReference());
    }

    public boolean isAutosave() {
      return autosave;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public boolean getHasLabelValues() {
      return !labelValues.isEmpty();
    }

    public List<Value> getLabelValues() {
      return labelValues;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getCommand() {
      return command;
    }

    public void setCommand(String command) {
      this.command = command;
    }

    private String command;

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getShowIf() {
      return showIf;
    }

    public String getReadOnlyIf() {
      return readOnlyIf;
    }

    public boolean isModal() {
      return modal;
    }

    public String getProcessId() {
      return processId;
    }

    public boolean isNewDefinition() {
      return newDefinition;
    }

    public void setNewDefinition(boolean newDefinition) {
      this.newDefinition = newDefinition;
    }

    public String getWindowId() {
      return windowId;
    }

    public void setWindowId(String windowId) {
      this.windowId = windowId;
    }

    public String getWindowTitle() {
      return windowTitle;
    }

    public void setWindowTitle(String windowTitle) {
      this.windowTitle = windowTitle;
    }

    public class Value {
      private String value;
      private String labelValue;

      public Value(org.openbravo.model.ad.domain.List valueList) {
        labelValue = OBViewUtil.getLabel(valueList, valueList.getADListTrlList());
        value = valueList.getSearchKey();
      }

      public String getValue() {
        return value;
      }

      public String getLabel() {
        return labelValue;
      }
    }
  }

  public class IconButton {
    protected String action;
    protected String type;
    protected String label;

    public String getAction() {
      return action;
    }

    public String getType() {
      return type;
    }

    public String getLabel() {
      return label;
    }
  }

  public class PrintButton extends IconButton {
    public boolean hasEmail;

    public PrintButton() {
      Process process = tab.getProcess();
      String processUrl = "";
      for (ModelImplementation mo : process.getADModelImplementationList()) {
        if (mo.isDefault() && ("P".equals(mo.getAction()) || "R".equals(mo.getAction()))) {
          for (ModelImplementationMapping mom : mo.getADModelImplementationMappingList()) {
            if (mom.isDefault()) {
              processUrl = ".." + mom.getMappingName();
              break;
            }
          }
          break;
        }
      }
      if (processUrl.isEmpty()) {
        processUrl = process.getSearchKey() + ".pdf";
      }
      if (processUrl.indexOf("/") == -1) {
        processUrl = "/" + FormatUtilities.replace(processUrl);
      }

      hasEmail = processUrl.contains("orders") || processUrl.contains("invoices")
          || processUrl.contains("payments");

      type = "print";
      action = "OB.ToolbarUtils.print(this.view, '" + processUrl + "', " + process.isDirectPrint()
          + ");";
      label = Utility.messageBD(new DalConnectionProvider(false), "Print", OBContext.getOBContext()
          .getLanguage().getLanguage());
    }
  }

  public boolean isRootTab() {
    return isRootTab;
  }

  public void setRootTab(boolean isRootTab) {
    this.isRootTab = isRootTab;
  }

  public boolean isShowParentButtons() {
    return tab.isShowParentsButtons();
  }

  private class FormFieldComparator implements Comparator<Field> {

    /**
     * Fields with null sequence number are in the bottom of the form. In case multiple null
     * sequences, it is sorted by field UUID.
     */
    @Override
    public int compare(Field arg0, Field arg1) {
      Long arg0Position = arg0.getSequenceNumber();
      Long arg1Position = arg1.getSequenceNumber();

      if (arg0Position == null && arg1Position == null) {
        return arg0.getId().compareTo(arg1.getId());
      } else if (arg0Position == null) {
        return 1;
      } else if (arg1Position == null) {
        return -1;
      }

      return (int) (arg0Position - arg1Position);
    }

  }

  public class OtherField {
    private Property property;
    private boolean session;
    private String inpColumnName;

    private OtherField(Column col) {
      this(KernelUtils.getInstance().getPropertyFromColumn(col, false));
    }

    private OtherField(Property property) {
      this.property = property;
      session = property.isStoredInSession();
      inpColumnName = "inp" + Sqlc.TransformaNombreColumna(property.getColumnName());
    }

    public String getPropertyName() {
      return property.getName();
    }

    public String getInpColumnName() {
      return inpColumnName;
    }

    public String getDbColumnName() {
      return property.getColumnName();
    }

    public String getType() {
      return UIDefinitionController.getInstance().getUIDefinition(property.getColumnId()).getName();
    }

    public boolean getSession() {
      return session;
    }
  }
}
