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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public class LeftTabsBar {
  private static Logger log4j = Logger.getLogger(LeftTabsBar.class);
  private ConnectionProvider conn;
  private String language = "en_US";
  private String servlet_action = "";
  private String base_direction = "";

  public LeftTabsBar(ConnectionProvider _conn, String _language, String _action,
      String _baseDirection) {
    this.conn = _conn;
    this.language = _language;
    this.servlet_action = _action;
    this.base_direction = _baseDirection;
  }

  public String editionTemplate() {
    return editionTemplate(false);
  }

  public String editionTemplate(boolean isNew) {
    String strClassEdition = "Main_LeftTabsBar_ButtonRight_Icon_edition" + (isNew ? "_new" : "")
        + "_selected";
    StringBuffer text = new StringBuffer();
    text.append("<table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_ContentPane_LeftTabsBar\" id=\"tdLeftTabsBars\">\n");

    if (!Utility.isNewUI()) {
      // show only for old UI
      text.append("  <tr>\n");
      text.append("    <td class=\"Main_LeftTabsBar_bg_body\">\n");
      text.append("      <table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_LeftTabsBar\">\n");
      text.append("        <tr>\n");
      text.append("          <td class=\"Main_LeftTabsBar_bg_top\">\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td>\n");
      text.append("              <a class=\"Main_LeftTabsBar_ButtonLeft\" href=\"#\" onclick=\"menuShowHide('buttonMenu');return false;\">\n");
      text.append(
          "                <img class=\"Main_LeftTabsBar_ButtonLeft_Icon Main_LeftTabsBar_ButtonLeft_Icon_arrow_hide\" src=\"")
          .append(base_direction)
          .append("/images/blank.gif\" border=\"0\" id=\"buttonMenu\"></img>\n");
      text.append("              </a>\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td class=\"Main_LeftTabsBar_bg_separator_cell\">\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td>\n");
      text.append("                <div class=\"Main_LeftTabsBar_ButtonRight_selected\">\n");
      text.append("                  <img class=\"Main_LeftTabsBar_ButtonRight_Icon ")
          .append(strClassEdition)
          .append(
              "\" alt=\"" + Utility.messageBD(this.conn, "Form View", this.language)
                  + "\" title=\"" + Utility.messageBD(this.conn, "Form View", this.language)
                  + "\" src=\"").append(base_direction)
          .append("/images/blank.gif\" border=\"0\" id=\"linkButtonEdition\"></img>\n");
      text.append("                </div>\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td>\n");
      text.append(
          "                <a class=\"Main_LeftTabsBar_ButtonRight\" href=\"#\" onClick=\"submitCommandForm('RELATION', isUserChanges, null, '")
          .append(servlet_action)
          .append("', '_self', null, true);return false;\" id=\"buttonRelation\">\n");
      text.append(
          "                  <img class=\"Main_LeftTabsBar_ButtonRight_Icon Main_LeftTabsBar_ButtonRight_Icon_relation\" alt=\""
              + Utility.messageBD(this.conn, "Grid View", this.language)
              + "\" title=\""
              + Utility.messageBD(this.conn, "Grid View", this.language) + "\" src=\"")
          .append(base_direction).append("/images/blank.gif\" border=\"0\"></img>\n");
      text.append("                </a>\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("      </table>\n");
      text.append("    </td>\n");
      text.append("  </tr>\n");
      text.append("  <tr class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
      text.append("    <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
      text.append("    </td>\n");
      text.append("  </tr>\n");
    }
    text.append("</table>\n");
    return text.toString();
  }

  public String relationTemplate() {
    StringBuffer text = new StringBuffer();
    text.append("<table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_ContentPane_LeftTabsBar\" id=\"tdLeftTabsBars\">\n");

    if (!Utility.isNewUI()) {
      // show only for old UI
      text.append("  <tr>\n");
      text.append("    <td class=\"Main_LeftTabsBar_bg_body\">\n");
      text.append("      <table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_LeftTabsBar\">\n");
      text.append("        <tr>\n");
      text.append("          <td class=\"Main_LeftTabsBar_bg_top\">\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td>\n");
      text.append("              <a class=\"Main_LeftTabsBar_ButtonLeft\" href=\"#\" onclick=\"menuShowHide('buttonMenu');return false;\">\n");
      text.append(
          "                <img class=\"Main_LeftTabsBar_ButtonLeft_Icon Main_LeftTabsBar_ButtonLeft_Icon_arrow_hide\" src=\"")
          .append(base_direction)
          .append("/images/blank.gif\" border=\"0\" id=\"buttonMenu\"></img>\n");
      text.append("              </a>\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td class=\"Main_LeftTabsBar_bg_separator_cell\">\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td>\n");
      text.append(
          "                <a class=\"Main_LeftTabsBar_ButtonRight\" href=\"#\" onClick=\"submitCommandForm('EDIT', true, null, '")
          .append(servlet_action)
          .append("', '_self', null, false);return false;\" id=\"buttonEdition\">\n");
      text.append(
          "                  <img class=\"Main_LeftTabsBar_ButtonRight_Icon Main_LeftTabsBar_ButtonRight_Icon_edition\" alt=\""
              + Utility.messageBD(this.conn, "Form View", this.language)
              + "\" title=\""
              + Utility.messageBD(this.conn, "Form View", this.language) + "\" src=\"")
          .append(base_direction).append("/images/blank.gif\" border=\"0\"></img>\n");
      text.append("                </a>\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td>\n");
      text.append("                <div class=\"Main_LeftTabsBar_ButtonRight_selected\">\n");
      text.append(
          "                  <img class=\"Main_LeftTabsBar_ButtonRight_Icon Main_LeftTabsBar_ButtonRight_Icon_relation_selected\" alt=\""
              + Utility.messageBD(this.conn, "Grid View", this.language)
              + "\" title=\""
              + Utility.messageBD(this.conn, "Grid View", this.language) + "\" src=\"")
          .append(base_direction).append("/images/blank.gif\" border=\"0\"></img>\n");
      text.append("                </div>\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("      </table>\n");
      text.append("    </td>\n");
      text.append("  </tr>\n");
      text.append("  <tr class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
      text.append("    <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
      text.append("    </td>\n");
      text.append("  </tr>\n");
    }
    text.append("</table>\n");
    return text.toString();
  }

  public String manualTemplate() {
    StringBuffer text = new StringBuffer();
    text.append("<table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_ContentPane_LeftTabsBar\" id=\"tdLeftTabsBars\">\n");
    if (!Utility.isNewUI()) {
      // show only for old UI
      text.append("  <tr>\n");
      text.append("    <td class=\"Main_LeftTabsBar_bg_body\">\n");
      text.append("      <table cellpadding=\"0\" cellspacing=\"0\" class=\"Main_LeftTabsBar\">\n");
      text.append("        <tr>\n");
      text.append("          <td class=\"Main_LeftTabsBar_bg_top\">\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td>\n");
      text.append("              <a class=\"Main_LeftTabsBar_ButtonLeft\" href=\"#\" onclick=\"menuShowHide('buttonMenu');return false;\">\n");
      text.append(
          "                <img class=\"Main_LeftTabsBar_ButtonLeft_Icon Main_LeftTabsBar_ButtonLeft_Icon_arrow_hide\" src=\"")
          .append(base_direction)
          .append("/images/blank.gif\" border=\"0\" id=\"buttonMenu\"></img>\n");
      text.append("              </a>\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td class=\"Main_LeftTabsBar_bg_separator_cell\">\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("        <tr>\n");
      text.append("          <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
      text.append("          </td>\n");
      text.append("        </tr>\n");
      text.append("      </table>\n");
      text.append("    </td>\n");
      text.append("  </tr>\n");
      text.append("  <tr class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
      text.append("    <td class=\"Main_LeftTabsBar_bg_empty_cell\">\n");
      text.append("    </td>\n");
      text.append("  </tr>\n");
    }
    text.append("</table>\n");
    return text.toString();
  }
}
