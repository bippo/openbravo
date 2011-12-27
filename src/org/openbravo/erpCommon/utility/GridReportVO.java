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
 * All portions are Copyright (C) 2007-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.awt.Color;
import java.util.List;

import org.openbravo.data.FieldProvider;

class GridReportVO {
  private String jrxmlTemplate;
  private String sql;
  private String title;
  private String context;
  private List<?> columns;
  private FieldProvider[] fieldProvider;
  private BandStyleVO headerBandStyle;
  private BandStyleVO detailBandStyle;
  private int totalWidth;
  private Boolean pagination;
  private String strDateFormat;

  public GridReportVO() {
  }

  public GridReportVO(String jrxmlTemplate, FieldProvider[] fieldProvider, String title,
      List<?> columns, String context, int totalWidth, String strDateFormat) {
    super();
    this.jrxmlTemplate = jrxmlTemplate;
    this.fieldProvider = fieldProvider;
    this.title = title;
    this.context = context;
    this.columns = columns;
    this.headerBandStyle = new BandStyleVO(null, new Color(65, 134, 18), 9, true, false, false);
    this.detailBandStyle = new BandStyleVO();
    this.totalWidth = totalWidth;
    this.strDateFormat = strDateFormat;
  }

  public List<?> getColumns() {
    return columns;
  }

  public BandStyleVO getDetailBandStyle() {
    return detailBandStyle;
  }

  public BandStyleVO getHeaderBandStyle() {
    return headerBandStyle;
  }

  public String getJrxmlTemplate() {
    return jrxmlTemplate;
  }

  public String getSql() {
    return sql;
  }

  public String getContext() {
    return context;
  }

  public String getTitle() {
    return title;
  }

  public FieldProvider[] getFieldProvider() {
    return fieldProvider;
  }

  public int getTotalWidth() {
    return totalWidth;
  }

  public Boolean getPagination() {
    return pagination;
  }

  public String getDateFormat() {
    return strDateFormat;
  }

  public void setColumns(List<?> columns) {
    this.columns = columns;
  }

  public void setDetailBandStyle(BandStyleVO detailBandStyle) {
    this.detailBandStyle = detailBandStyle;
  }

  public void setHeaderBandStyle(BandStyleVO headerBandStyle) {
    this.headerBandStyle = headerBandStyle;
  }

  public void setJrxmlTemplate(String jrxmlTemplate) {
    this.jrxmlTemplate = jrxmlTemplate;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setFieldProvider(FieldProvider[] fieldProvider) {
    this.fieldProvider = fieldProvider;
  }

  public void setTotalWidth(int totalWidth) {
    this.totalWidth = totalWidth;
  }

  public void setPagination(Boolean pagination) {
    this.pagination = pagination;
  }

  public void setDateFormat(String strDateFormat) {
    this.strDateFormat = strDateFormat;
  }

}
