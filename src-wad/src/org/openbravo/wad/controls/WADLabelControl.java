package org.openbravo.wad.controls;

public class WADLabelControl {

  public static int FIELD_LABEL = 0;
  public static int FIELD_GROUP_LABEL = 1;
  public static int BUTTON_LABEL = 2;

  private int labelType = 0;
  private String tabId = "";
  private String fieldId = "";
  private String columnId = "";
  private String fieldGroupId = "";
  private String columnName = "";
  private String labelId = "";
  private String labelPlaceHolderText = "";
  private String baseLabelText = "";
  private String trlLabeltext = "";
  private String keyColumnName = "";

  private String columnNameInp = "";
  private String tableId = "";
  private boolean isLinkable = false;

  private String columnLink;

  public WADLabelControl() {
  }

  public WADLabelControl(int labelType, String tabId, String fieldId, String columnId,
      String columnName, String baseLabelText, String trlLabelText, String linkable,
      String keyColumnName, String columnNameInp, String AD_Table_ID, String columnLink) {

    setTabId(tabId);
    setFieldId(fieldId);
    setColumnId(columnId);
    setColumnName(columnName);
    setBaseLabelText(baseLabelText);
    setTrlLabeltext(trlLabelText);
    setLinkable(linkable);
    setLabelType(labelType);
    setColumnNameInp(columnNameInp);
    setKeyColumnName(keyColumnName);
    setTableId(AD_Table_ID);
    setColumnLink(columnLink);

  }

  public String getColumnLink() {
    return columnLink;
  }

  public void setColumnLink(String columnLink) {
    this.columnLink = columnLink;
  }

  public int getLabelType() {
    return labelType;
  }

  public void setLabelType(int type) {
    labelType = type;
    if (labelType == FIELD_GROUP_LABEL) {
      if (fieldGroupId != null && !fieldGroupId.trim().equals(""))
        setLabelId(fieldGroupId);
      if (columnName != null && !columnName.trim().equals(""))
        setLabelPlaceHolderText(columnName);
    }
  }

  public String getTabId() {
    return tabId;
  }

  public void setTabId(String tabId) {
    if (tabId != null && !tabId.trim().equals("")) {
      this.tabId = tabId;
    }
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    if (fieldId != null && !fieldId.trim().equals("")) {
      this.fieldId = fieldId;
    }
  }

  public String getColumnId() {
    return columnId;
  }

  public void setColumnId(String columnId) {
    if (columnId != null && !columnId.trim().equals("")) {
      this.columnId = columnId;
      if (labelType == FIELD_LABEL || labelType == BUTTON_LABEL) {
        setLabelId(this.columnId);
        // setLabelPlaceHolderText(this.columnName);
      }
    }
  }

  public String getFieldGroupId() {
    return fieldGroupId;
  }

  public void setFieldGroupId(String fieldGroupId) {
    if (fieldGroupId != null && !fieldGroupId.trim().equals("")) {
      this.fieldGroupId = fieldGroupId;
      setLabelId(fieldGroupId);
    }
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    if (columnName != null && !columnName.trim().equals("")) {
      this.columnName = columnName;
      if (labelType == FIELD_LABEL || labelType == BUTTON_LABEL) {
        if (getLabelId() == null || getLabelId().equals(""))
          setLabelId(this.columnName);
        setLabelPlaceHolderText(this.columnName);
      } else if (labelType == FIELD_GROUP_LABEL) {
        if (fieldGroupId != null && !fieldGroupId.trim().equals(""))
          setLabelId(fieldGroupId);
        setLabelPlaceHolderText(this.columnName);
      }
    }
  }

  public String getLabelId() {
    return labelId;
  }

  private void setLabelId(String textId) {
    String tempLabelId = (labelType == FIELD_GROUP_LABEL) ? "fldgrp::" : "lbl::";
    tempLabelId = tempLabelId + textId;
    this.labelId = tempLabelId;
  }

  public String getLabelPlaceHolderText() {
    return labelPlaceHolderText;
  }

  private void setLabelPlaceHolderText(String text) {
    String tempPlaceholder = "#";
    tempPlaceholder = tempPlaceholder + text + "#";
    this.labelPlaceHolderText = tempPlaceholder;
  }

  public String getBaseLabelText() {
    return baseLabelText;
  }

  public void setBaseLabelText(String baseLabelText) {
    if (baseLabelText != null && !baseLabelText.trim().equals("")) {
      this.baseLabelText = baseLabelText;
    }
  }

  public String getTrlLabeltext() {
    return trlLabeltext;
  }

  public void setTrlLabeltext(String trlLabeltext) {
    if (trlLabeltext != null && !trlLabeltext.trim().equals("")) {
      this.trlLabeltext = trlLabeltext;
    }
  }

  public boolean isLinkable() {
    return isLinkable;
  }

  public void setLinkable(String linkable) {
    isLinkable = false;
    if (linkable != null && linkable.equalsIgnoreCase("y"))
      isLinkable = true;
  }

  public void setLinkable(boolean linkable) {
    isLinkable = linkable;
  }

  public String getKeyColumnName() {
    return keyColumnName;
  }

  public void setKeyColumnName(String keyColumnName) {
    if (keyColumnName != null && !keyColumnName.trim().equals("")) {
      this.keyColumnName = keyColumnName;
    }
  }

  public String getColumnNameInp() {
    return columnNameInp;
  }

  public void setColumnNameInp(String columnNameInp) {
    if (columnNameInp != null && !columnNameInp.trim().equals("")) {
      this.columnNameInp = columnNameInp;
    }
  }

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    if (tableId != null && !tableId.trim().equals("")) {
      this.tableId = tableId;
    }
  }

  public String toLabelXML() {
    StringBuffer labelText = new StringBuffer();
    if (getLabelId() != null && !getLabelId().equals("") && getLabelPlaceHolderText() != null
        && !getLabelPlaceHolderText().equals("")) {
      labelText.append("<LABEL id=\"").append(getLabelId());
      labelText.append("\" name=\"").append(getLabelId());
      labelText.append("\" replace=\"" + getLabelPlaceHolderText() + "\">");
      labelText.append(getColumnName()).append("lbl");
      labelText.append("</LABEL>");
    } else {
      labelText.append("");
    }

    return labelText.toString();
  }

}
