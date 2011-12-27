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

class BandStyleVO {
  private String fontName;

  private int fontSize;

  private Color foreColor;

  private boolean bold;

  private boolean italic;

  private boolean underline;

  public BandStyleVO() {
    this(null, new Color(0, 0, 0), 9, false, false, false);
  }

  public BandStyleVO(String fontName, Color foreColor, int fontSize, boolean bold, boolean italic,
      boolean underline) {
    super();
    this.fontName = fontName;
    this.foreColor = foreColor;
    this.bold = bold;
    this.italic = italic;
    this.underline = underline;
    this.fontSize = fontSize;
  }

  public String getFontName() {
    return fontName;
  }

  public int getFontSize() {
    return fontSize;
  }

  public Color getForeColor() {
    return foreColor;
  }

  public boolean isBold() {
    return bold;
  }

  public boolean isItalic() {
    return italic;
  }

  public boolean isUnderline() {
    return underline;
  }

  public void setBold(boolean bold) {
    this.bold = bold;
  }

  public void setFontName(String fontName) {
    this.fontName = fontName;
  }

  public void setFontSize(int size) {
    this.fontSize = size;
  }

  public void setForeColor(Color foreColor) {
    this.foreColor = foreColor;
  }

  public void setItalic(boolean italic) {
    this.italic = italic;
  }

  public void setUnderline(boolean underline) {
    this.underline = underline;
  }

}
