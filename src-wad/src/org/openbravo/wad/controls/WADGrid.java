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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.wad.controls;

import java.util.Properties;

public class WADGrid extends WADControl {

  public WADGrid() {
  }

  public WADGrid(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    generateJSCode();
    setValidation("");
  }

  private void generateJSCode() {
    addJSCode("DataGrid", "dojo.require(\"openbravo.widget.DataGrid\");");
    String str = "function updateHeader(liveGrid, offset) {\n"
        + "  var backendPageSize = liveGrid.getBackendPageSize();\n"
        + "  var currPageStart = (liveGrid.metaData.getBackendPage()*backendPageSize);\n"
        + "  var pageFull = (liveGrid.metaData.getTotalRows() >= backendPageSize);\n"
        + "  var firstPage = (liveGrid.metaData.getBackendPage() == 0);\n"
        + "  var res =  \"<nobr class='Main_ToolBar_text_bookmark'>\";\n"
        + "  var strPrevious = getMessage(\"GridPreviousPage\");\n"
        + "  var strNext = getMessage(\"GridNextPage\");\n"
        + "\n"
        + "  if (!firstPage) {\n"
        + "    res = res + \"<a href='#' onclick='gridMovePage(\\\"PREVIOUSPAGE\\\")' class='Main_ToolBar_text_pagerange'>\" + strPrevious + \" \" + backendPageSize + \"</a>&nbsp;&nbsp;\";\n"
        + "  }\n"
        + "  res = res + ((liveGrid.visibleRows>0)?(currPageStart+offset+1):0) + \" - \""
        + "+ (currPageStart+offset+liveGrid.visibleRows) + \" / \" + (currPageStart+liveGrid.metaData.getTotalRows());\n"
        + "  if (pageFull) {\n"
        + "    res = res + \"&nbsp;&nbsp;<a href='#' onclick='gridMovePage(\\\"NEXTPAGE\\\")' class='Main_ToolBar_text_pagerange'>\" + strNext + \" \" + backendPageSize + \"</a>\";\n"
        + "  }\n" + "\n" + "  liveGrid.setGridPaging(!firstPage,pageFull);\n"
        + "  res = res + \"</nobr>\";\n" + "  dojo.byId('bookmark').innerHTML = res;\n" + "}\n";
    addJSCode("updateHeader", str);

    StringBuffer text = new StringBuffer();
    text.append("function onRowDblClick(cell) {\n");
    text.append("  var value = dijit.byId('").append(getData("id"))
        .append("').getSelectedRows();\n");
    text.append("  if (value==null || value==\"\" || value.length>1) return false;\n");
    text.append("  setInputValue('").append(getData("inpKeyName")).append("', value);\n");
    text.append("  return submitCommandForm('EDIT', true, null, document.frmMain.urlwin.value, '_self');\n");
    text.append('}');
    addJSCode("onRowDblClick", text.toString());

    text = new StringBuffer();
    text.append("function getSelectedValues() {\n");
    text.append("  var value = dijit.byId('").append(getData("id"))
        .append("').getSelectedRows();\n");
    text.append("  if (value==null || value.length==0) return \"\";\n");
    text.append("  return value[0];\n");
    text.append('}');
    addJSCode("getSelectedValues", text.toString());

    text = new StringBuffer();
    text.append("function isMultipleSelected() {\n");
    text.append("  var value = dijit.byId('").append(getData("id"))
        .append("').getSelectedRows();\n");
    text.append("  if (value==null || value==\"\") return false;\n");
    text.append("  return (value.length>1);\n");
    text.append('}');
    addJSCode("isMultipleSelected", text.toString());

    text = new StringBuffer();
    text.append("function onGridLoadDo() {\n");
    text.append("  if (selectedRow==null) return true;\n");
    text.append("  if (selectedRow<=0) dijit.byId('").append(getData("id"))
        .append("').goToFirstRow();\n");
    text.append("  else dijit.byId('").append(getData("id")).append("').goToRow(selectedRow);\n");

    text.append("  return true;\n");
    text.append('}');
    addJSCode("onGridLoadDo", text.toString());

    text = new StringBuffer();
    text.append("function setGridFilters(newparams) {\n"
        + "  var params = [], hasFilter = false;\n" + "  params[\"newFilter\"] = \"1\";\n"
        + "  if (newparams!=null && newparams.length>0) {\n"
        + "    var total = newparams.length;\n" + "    for (var i=0;i<total;i++) {\n"
        + "      params[newparams[i][0]] = newparams[i][1];\n"
        + "      hasFilter = hasFilter || (newparams[i][1] !== '' && newparams[i][1] !== '%');\n"
        + "    }\n" + "  }\n");
    text.append("  dijit.byId('").append(getData("id")).append("').setRequestParams(params);\n");
    text.append("  changeSearchIcon(hasFilter);\n");
    text.append("  return true;\n");
    text.append('}');
    addJSCode("setGridFilters", text.toString());

    text = new StringBuffer();
    text.append("function updateGridData() {\n");
    text.append("  dijit.byId('").append(getData("id")).append("').refreshGridData();\n");
    text.append("  return true;\n");
    text.append('}');
    addJSCode("updateGridData", text.toString());

    text = new StringBuffer();
    text.append("function updateGridDataAfterFilter() {\n");
    text.append("  dijit.byId('").append(getData("id"))
        .append("').refreshGridDataAfterFilter();\n");
    text.append("  return true;\n");
    text.append('}');
    addJSCode("updateGridDataAfterFilter", text.toString());

    text = new StringBuffer();
    text.append("function gridMovePage(direction) {\n");
    text.append("  dijit.byId('").append(getData("id")).append("').gridMovePage(direction);\n");
    text.append("  return true;\n");
    text.append("}\n");
    addJSCode("gridMovePage", text.toString());
  }

  public String toString() {
    StringBuffer text = new StringBuffer();
    text.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" class=\"Main_Client_TableRelation\" id=\"grid_table\"><tr><td>");
    text.append("<div id=\"").append(getData("id"))
        .append("\" dojoType=\"openbravo.widget.DataGrid\"\n");
    text.append("      structureUrl=\"../utility/DataGrid.html?Command=STRUCTURE&inpadTabId=");
    text.append(getData("AD_Tab_ID")).append("&inpadWindowId=");
    text.append(getData("AD_Window_ID")).append("&inpAccessLevel=").append(getData("accessLevel"))
        .append("\" \n");
    text.append("      dataUrl=\"../utility/DataGrid.html?Command=DATA&inpadTabId=");
    text.append(getData("AD_Tab_ID")).append("&inpadWindowId=");
    text.append(getData("AD_Window_ID")).append("&inpAccessLevel=").append(getData("accessLevel"))
        .append("\" \n");
    text.append("      updatesUrl=\"../utility/DataGrid.html?Command=UPDATE&inpadTabId=");
    text.append(getData("AD_Tab_ID")).append("&inpadWindowId=");
    text.append(getData("AD_Window_ID")).append("&inpAccessLevel=").append(getData("accessLevel"))
        .append("\" \n");
    // text.append("      numRows=\"").append(getData("NumRows")).append("\" \n");
    text.append("      calculateNumRows=\"true\" \n");
    text.append("      editable=\"").append(getData("editable")).append("\" sortable=\"")
        .append(getData("sortable")).append("\" \n");
    text.append("      deleteable=\"").append(getData("deleteable")).append("\" \n");
    text.append("      onInvalidValue=\"alert\" \n");
    text.append("      onScroll=\"").append(getData("onScrollFunction")).append("\" \n");
    text.append("      onGridLoad=\"").append(getData("onLoadFunction")).append("\"\n");
    text.append("      bufferSize=\"3.0\"\n");
    text.append("      showLineNumbers=\"").append(getData("ShowLineNumbers")).append("\" \n");
    text.append("      offset=\"xx\" sortcols=\"xx\" sortdirs=\"xx\" defaultrow=\"xx\" \n");
    text.append("      maxWidth=\"").append(getData("width")).append("\" \n");
    text.append("      percentageWidthRelativeToId=\"client\" \n");
    text.append("      preventCache=\"true\" useCache=\"true\" cacheContent=\"false\">\n");
    text.append("    </div>\n");
    text.append("</td></tr></table>");
    return text.toString();
  }

  public String toXml() {
    StringBuffer text = new StringBuffer();
    text.append("<PARAMETER id=\"").append(getData("id")).append("\" name=\"")
        .append(getData("id")).append("\" attribute=\"numrows\"/>");
    text.append("<PARAMETER id=\"").append(getData("id")).append("\" name=\"")
        .append(getData("id")).append("_Offset\" attribute=\"offset\"/>");
    text.append("<PARAMETER id=\"").append(getData("id")).append("\" name=\"")
        .append(getData("id")).append("_SortCols\" attribute=\"sortcols\"/>");
    text.append("<PARAMETER id=\"").append(getData("id")).append("\" name=\"")
        .append(getData("id")).append("_SortDirs\" attribute=\"sortdirs\"/>");
    text.append("<PARAMETER id=\"").append(getData("id")).append("\" name=\"")
        .append(getData("id")).append("_Default\" attribute=\"defaultrow\"/>");
    return text.toString();
  }

  public String toJava() {
    StringBuffer text = new StringBuffer();
    text.append("xmlDocument.setParameter(\"").append(getData("id"))
        .append("\", Utility.getContext(this, vars, \"#RecordRange\", windowId));\n");
    text.append("xmlDocument.setParameter(\"").append(getData("id"))
        .append("_Offset\", strOffset);\n");
    text.append("xmlDocument.setParameter(\"").append(getData("id"))
        .append("_SortCols\", positions);\n");
    text.append("xmlDocument.setParameter(\"").append(getData("id"))
        .append("_SortDirs\", directions);\n");
    text.append("xmlDocument.setParameter(\"").append(getData("id"))
        .append("_Default\", selectedRow);\n");
    return text.toString();
  }
}
