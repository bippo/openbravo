package org.openbravo.wad.controls;

public class WadControlLabelBuilder {

  private WADLabelControl labelControl;
  private StringBuffer buffer = new StringBuffer();
  private String basicLabel = "";

  public WadControlLabelBuilder() {
  }

  public WadControlLabelBuilder(WADLabelControl labelControl) {
    this.labelControl = labelControl;
  }

  public void buildLabelControl() {
    buffer = new StringBuffer();
    buffer.append(createStartSpan(labelControl.getColumnName()));
    buffer.append(createInternalContent());
    buffer.append(closeLabelSpan());
  }

  public String getLabelString() {
    return buffer.toString();
  }

  public String getBasicLabelText() {
    return basicLabel;
  }

  private String createStartSpan(String columName) {
    String labelClassId = labelControl.getColumnName() + "_lbl";
    String result = "<span class=\"LabelText\" id=\"" + labelClassId + "\">";
    return result;
  }

  private String createInternalContent() {
    StringBuffer result = new StringBuffer();
    createBasicLabelTag();
    if (labelControl.isLinkable()) {
      result
          .append("<a class=\"LabelLink\" href=\"#\" onclick=\"sendDirectLink(document.frmMain, '");
      result.append(labelControl.getColumnLink());
      result.append("', document.frmMain.");
      result.append(labelControl.getKeyColumnName());
      result.append(".value, '../utility/ReferencedLink.html', document.frmMain.inp");
      result.append(labelControl.getColumnNameInp());
      result.append(".value, '");
      result.append(labelControl.getTableId());
      result
          .append("', '_self', true);return false;\" onmouseover=\"return true;\" onmouseout=\"return true;\" id=\"fieldLink\">");
      result.append(getBasicLabelText());
      result.append("</a>");
    } else {
      result.append(getBasicLabelText());
    }
    return result.toString();
  }

  private String closeLabelSpan() {
    return "&nbsp;</span>";
  }

  private void createBasicLabelTag() {
    StringBuffer labelBuffer = new StringBuffer();
    labelBuffer.append("<span id=\"" + labelControl.getLabelId() + "\">"
        + labelControl.getLabelPlaceHolderText() + "</span>");

    basicLabel = labelBuffer.toString();
  }

}
