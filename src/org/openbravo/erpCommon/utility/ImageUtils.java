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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.Image;

/**
 * Contains several methods used in the Image related servlets
 * 
 * This class was contributed by Francois Le Roux
 */
class ImageUtils {

  private static final String ETAG_ALGORITHM = "MD5";
  protected static final String RESPONSE_HEADER_ETAG = "ETag";
  protected static final String RESPONSE_HEADER_LASTMODIFIED = "Last-Modified";
  protected static final String RESPONSE_HEADER_CACHE_CONTROL = "Cache-Control";
  protected static final String RESPONSE_NO_CACHE = "no-cache";
  protected static final String RESPONSE_HEADER_CONTENTTYPE = "Content-Type";
  private static final String REQUEST_HEADER_IFNONEMATCH = "If-None-Match";
  private static final String REQUEST_HEADER_IFMODIFIEDSINCE = "If-Modified-Since";
  private static Logger log4j = Logger.getLogger(ImageUtils.class);

  /**
   * Checks if the content is to be send in response.
   * 
   * This uses HTTP ETag technology and sets required headers.
   */
  private static boolean isImageResponseRequired(final HttpServletRequest req,
      final HttpServletResponse resp, final String resourceName) {
    final String match = req.getHeader(REQUEST_HEADER_IFNONEMATCH);
    final String etag = getHashDigestTag(resourceName);

    resp.setHeader(RESPONSE_HEADER_ETAG, etag);

    return !etag.equals(match);
  }

  /**
   * Calculates the ETag of the resourceName
   */
  private static String getHashDigestTag(final String resourceName) {
    try {
      MessageDigest dig = MessageDigest.getInstance(ETAG_ALGORITHM);

      final byte[] digest = dig.digest(resourceName.getBytes());

      return DigestUtils.md5Hex(digest);

    } catch (NoSuchAlgorithmException nsae) {
      log4j.error("Cannot find ETag algorithm " + ETAG_ALGORITHM, nsae);
    }

    return resourceName;
  }

  /**
   * Outputs the image/content to the response
   */
  public static void outputImageResource(final HttpServletRequest req,
      final HttpServletResponse resp, final String imageType) throws IOException, ServletException {
    try {
      OBContext.setAdminMode(true);

      // enforce cache validation/checks every time
      resp.addHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);
      VariablesSecureApp vars = new VariablesSecureApp(req);

      Image img = null;
      if (imageType == "logo") {
        String logo = vars.getStringParameter("logo");
        String org = vars.getStringParameter("orgId");
        img = Utility.getImageLogoObject(logo, org);
        if (img == null) {
          byte[] imageFileContent = Utility.getImageLogo(logo, org);
          String mimeType = MimeTypeUtil.getInstance().getMimeTypeName(imageFileContent);
          resp.setContentType(mimeType);
          OutputStream out = resp.getOutputStream();
          resp.setContentLength(imageFileContent.length);
          out.write(imageFileContent);
          out.close();
          return;
        }
      } else {
        img = Utility.getImageObject(vars.getStringParameter("id"));
      }
      String imageID = "IMGTAG" + img.getUpdated().toString();

      if (ImageUtils.isImageResponseRequired(req, resp, imageID)) {

        // read the image data
        byte[] imgByte = img.getBindaryData();

        // write the mimetype
        String mimeType = img.getMimetype();// write the mimetype
        if (mimeType == null) {
          mimeType = MimeTypeUtil.getInstance().getMimeTypeName(img.getBindaryData());
          if (img != null) {
            // If there is an OBContext, we attempt to save the MIME type of the image
            updateMimeType(img.getId(), mimeType);
          }
        }

        if (!mimeType.equals("")) {
          resp.setContentType(mimeType);
        }

        // write the image
        OutputStream out = resp.getOutputStream();
        resp.setContentLength(imgByte.length);
        out.write(imgByte);
        out.close();

      } else {
        resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
        resp.setDateHeader(RESPONSE_HEADER_LASTMODIFIED,
            req.getDateHeader(REQUEST_HEADER_IFMODIFIEDSINCE));

      }
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * This method updates the MIME type of an image, using SQL. DAL cannot be used because sometimes
   * there is no OBContext (specifically, it happens for images in the Login page)
   */
  private static void updateMimeType(String id, String mimeType) {
    PreparedStatement ps = null;
    try {
      ps = OBDal.getInstance().getConnection(true)
          .prepareStatement("UPDATE ad_image SET mimetype=? WHERE ad_image_id=?");
      ps.setString(1, mimeType);
      ps.setString(2, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      log4j.error("Couldn't update mime information of image", e);
    } finally {
      try {
        if (ps != null) {
          ps.close();
        }
      } catch (SQLException e) {
        // ignore
      }
    }
  }
}
