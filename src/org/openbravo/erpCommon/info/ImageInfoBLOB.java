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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.info;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.MimeTypeUtil;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

public class ImageInfoBLOB extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String columnName = vars.getStringParameter("columnName");
    if (columnName == null || columnName.equals(""))
      columnName = vars.getStringParameter("inpColumnName");
    String tableId = vars.getStringParameter("tableId");
    if (tableId == null || tableId.equals("")) {
      tableId = vars.getStringParameter("inpTableId");
    }
    if (tableId == null || tableId.equals("")) {
      String tabId = vars.getStringParameter("inpTabId");
      try {
        OBContext.setAdminMode(true);
        Tab tab = OBDal.getInstance().get(Tab.class, tabId);
        tableId = tab.getTable().getId();
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    String imageID = vars.getStringParameter("inp" + Sqlc.TransformaNombreColumna(columnName));
    if (imageID == null || imageID.equals("")) {
      imageID = vars.getStringParameter("imageId");
    }

    String orgId = vars.getStringParameter("inpOrgId");
    if (orgId == null || orgId.equals("")) {
      orgId = vars.getStringParameter("inpadOrgId");
    }
    if (orgId == null || orgId.equals("")) {
      orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    }

    String parentObjectId = vars.getStringParameter("parentObjectId");
    if (parentObjectId == null || parentObjectId.equals("")) {
      OBContext.setAdminMode(true);
      try {
        Table table = OBDal.getInstance().get(Table.class, vars.getStringParameter("inpTableId"));
        if (table != null) {
          List<Column> cols = table.getADColumnList();
          String keyCol = "";
          for (Column col : cols) {
            if (col.isKeyColumn()) {
              keyCol = col.getDBColumnName();
              break;
            }
          }
          parentObjectId = vars.getStringParameter("inp" + Sqlc.TransformaNombreColumna(keyCol));
        }
      } finally {
        OBContext.restorePreviousMode();
      }

    }
    if (vars.commandIn("DEFAULT")) {

      printPageFrame(response, vars, imageID, tableId, columnName, parentObjectId, orgId);
    } else if (vars.getCommand().equals("SAVE")) {
      OBContext.setAdminMode(true);
      try {
        final FileItem fi = vars.getMultiFile("inpFile");
        byte[] bytea = fi.get();
        Long[] size = Utility.computeImageSize(bytea);
        String mimeType = MimeTypeUtil.getInstance().getMimeTypeName(bytea);
        // Using DAL to write the image data to the database
        Image image;
        if (imageID == null || imageID.equals("")) {
          image = OBProvider.getInstance().get(Image.class);
          Organization org = OBDal.getInstance().get(Organization.class, orgId);
          image.setOrganization(org);
          image.setBindaryData(bytea);
          image.setActive(true);
          image.setName("Image");
          image.setWidth(size[0]);
          image.setHeight(size[1]);
          image.setMimetype(mimeType);
          OBDal.getInstance().save(image);
          OBDal.getInstance().flush();
        } else {
          image = OBDal.getInstance().get(Image.class, imageID);
          image.setActive(true);
          image.setBindaryData(bytea);
          image.setWidth(size[0]);
          image.setHeight(size[1]);
          image.setMimetype(mimeType);
          OBDal.getInstance().flush();
        }
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writeRedirect(writer, image.getId(), columnName);
      } finally {
        OBContext.restorePreviousMode();
      }
    } else if (vars.getCommand().startsWith("SAVE_OB3")) {
      OBContext.setAdminMode(true);
      try {
        final FileItem fi = vars.getMultiFile("inpFile");
        byte[] bytea = fi.get();
        String mimeType = MimeTypeUtil.getInstance().getMimeTypeName(bytea);
        String imageSizeAction = vars.getStringParameter("imageSizeAction");
        String imageId = "";
        Long[] sizeOld = new Long[2];
        Long[] sizeNew = new Long[2];
        if (!mimeType.contains("image")
            || (!mimeType.contains("jpeg") && !mimeType.contains("png")
                && !mimeType.contains("gif") && !mimeType.contains("bmp"))) {
          imageId = "";
          imageSizeAction = "WRONGFORMAT";
          sizeOld[0] = (long) 0;
          sizeOld[1] = (long) 0;
          sizeNew[0] = (long) 0;
          sizeNew[1] = (long) 0;
        } else {

          int newWidth;
          if (vars.getStringParameter("imageWidthValue") != "") {
            newWidth = Integer.parseInt(vars.getStringParameter("imageWidthValue"));
          } else {
            newWidth = 0;
          }
          int newHeight;
          if (vars.getStringParameter("imageHeightValue") != "") {
            newHeight = Integer.parseInt(vars.getStringParameter("imageHeightValue"));
          } else {
            newHeight = 0;
          }

          if (imageSizeAction.equals("ALLOWED") || imageSizeAction.equals("ALLOWED_MINIMUM")
              || imageSizeAction.equals("ALLOWED_MAXIMUM") || imageSizeAction.equals("RECOMMENDED")
              || imageSizeAction.equals("RECOMMENDED_MINIMUM")
              || imageSizeAction.equals("RECOMMENDED_MAXIMUM")) {
            sizeOld[0] = (long) newWidth;
            sizeOld[1] = (long) newHeight;
            sizeNew = Utility.computeImageSize(bytea);
          } else if (imageSizeAction.equals("RESIZE_NOASPECTRATIO")) {
            sizeOld = Utility.computeImageSize(bytea);
            bytea = Utility.resizeImageByte(bytea, newWidth, newHeight, false, false);
            sizeNew = Utility.computeImageSize(bytea);
          } else if (imageSizeAction.equals("RESIZE_ASPECTRATIO")) {
            sizeOld = Utility.computeImageSize(bytea);
            bytea = Utility.resizeImageByte(bytea, newWidth, newHeight, true, true);
            sizeNew = Utility.computeImageSize(bytea);
          } else if (imageSizeAction.equals("RESIZE_ASPECTRATIONL")) {
            sizeOld = Utility.computeImageSize(bytea);
            bytea = Utility.resizeImageByte(bytea, newWidth, newHeight, true, false);
            sizeNew = Utility.computeImageSize(bytea);
          } else {
            sizeOld = Utility.computeImageSize(bytea);
            sizeNew = sizeOld;
          }

          mimeType = MimeTypeUtil.getInstance().getMimeTypeName(bytea);
          // Using DAL to write the image data to the database
          Image image;

          image = OBProvider.getInstance().get(Image.class);
          Organization org = OBDal.getInstance().get(Organization.class, orgId);
          image.setOrganization(org);
          image.setBindaryData(bytea);
          image.setActive(true);
          image.setName("Image");
          image.setWidth(sizeNew[0]);
          image.setHeight(sizeNew[1]);
          image.setMimetype(mimeType);
          OBDal.getInstance().save(image);
          OBDal.getInstance().flush();

          imageId = image.getId();
        }
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        String selectorId = orgId = vars.getStringParameter("inpSelectorId");
        writeRedirectOB3(writer, selectorId, imageId, imageSizeAction, sizeOld, sizeNew);
      } finally {
        OBContext.restorePreviousMode();
      }
    } else if (vars.getCommand().equals("DELETE")) {
      if (imageID != null && !imageID.equals("")) {
        OBContext.setAdminMode(true);
        try {
          Image image = OBDal.getInstance().get(Image.class, imageID);
          Table table = OBDal.getInstance().get(Table.class, tableId);
          Entity entity = ModelProvider.getInstance().getEntityByTableName(table.getDBTableName());
          String propertyName = entity.getPropertyByColumnName(columnName).getName();
          BaseOBObject parentObject = (BaseOBObject) OBDal.getInstance().get(entity.getName(),
              parentObjectId);
          parentObject.set(propertyName, null);
          OBDal.getInstance().flush();
          OBDal.getInstance().remove(image);
        } finally {
          OBContext.restorePreviousMode();
        }
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writeRedirect(writer, "", columnName);
      } else {
        printPageFrame(response, vars, imageID, tableId, columnName, parentObjectId, orgId);
      }
    } else if (vars.getCommand().startsWith("DELETE_OB3")) {
      if (imageID != null && !imageID.equals("")) {
        OBContext.setAdminMode(true);
        try {
          Image image = OBDal.getInstance().get(Image.class, imageID);
          OBDal.getInstance().flush();
          OBDal.getInstance().remove(image);
        } finally {
          OBContext.restorePreviousMode();
        }
      } else {
        printPageFrame(response, vars, imageID, tableId, columnName, parentObjectId, orgId);
      }
    } else {
      pageError(response);
    }
  }

  private void writeRedirectOB3(PrintWriter writer, String selectorId, String imageId,
      String imageSizeAction, Long[] sizeOld, Long[] sizeNew) {
    writer.write("<HTML><BODY><script type=\"text/javascript\">");
    writer.write("top." + selectorId + ".callback('" + imageId + "', '" + imageSizeAction + "', '"
        + sizeOld[0] + "' ,'" + sizeOld[1] + "' ,'" + sizeNew[0] + "' ,'" + sizeNew[1] + "');");
    writer.write("</SCRIPT></BODY></HTML>");

  }

  private void printPageFrame(HttpServletResponse response, VariablesSecureApp vars,
      String imageID, String tableId, String columnName, String parentObjectId, String orgId)
      throws IOException, ServletException {
    String[] discard;
    if (imageID.equals("")) {
      discard = new String[1];
      discard[0] = "divDelete";
    } else
      discard = new String[0];

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/ImageInfoBLOB", discard).createXmlDocument();

    xmlDocument.setParameter("parentObjectId", parentObjectId);
    xmlDocument.setParameter("imageId", imageID);
    xmlDocument.setParameter("inpColumnName", columnName);
    xmlDocument.setParameter("inpOrgId", orgId);
    xmlDocument.setParameter("tableId", tableId);
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void writeRedirect(PrintWriter writer, String imageId, String columnname) {
    writer.println("<html>");
    writer.println("<head>");
    writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
    writer
        .println("<script language=\"JavaScript\" src=\"../../../../../web/js/searchs.js\" type=\"text/javascript\"></script>");
    writer.println("<script language=\"JavaScript\" type=\"text/javascript\">");
    writer.println("function onLoadDo(){");
    writer.println("var parentWindow = parent.opener;");
    writer.println("parentWindow.document.getElementById('" + columnname + "').value = \""
        + imageId + "\";");
    writer.println("parentWindow.document.getElementById('" + columnname
        + "_R').src = \"../utility/ShowImage?id=" + imageId + "\";");
    if (imageId.equals("")) {
      writer.println("parentWindow.document.getElementById('" + columnname
          + "_R').className = \"Image_NotAvailable_medium\"");
    } else {
      writer.println("parentWindow.document.getElementById('" + columnname
          + "_R').className = \"dummyClass_\" + parent.opener.document.getElementById('"
          + columnname + "_R').className;");
    }

    // When deleting an image, reset parent status to not changed in order to avoid trigger Autosave
    if (imageId.equals("")) {
      writer.println("parentWindow.isUserChanges = false;");
      writer.println("parentWindow.document.forms[0].inpLastFieldChanged.value=\"\";");
    }
    writer.println("window.close();");

    writer.println("try { parentWindow.changeToEditingMode('force'); } catch (e) {}");
    writer.println("}");
    writer.println("</script>");
    writer.println("</head>");
    writer.println("<body  onload=\"onLoadDo();\">");
    writer.println("</body>");
    writer.println("</html>");
  }

}