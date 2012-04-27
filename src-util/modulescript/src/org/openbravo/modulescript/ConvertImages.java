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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.modulescript;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.database.ConnectionProvider;
import java.io.FileInputStream;
import java.util.Properties;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.imageio.ImageIO;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;

public class ConvertImages extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(ModuleScript.class);
  private Tika tika;
  
  @Override
  public void execute() {
    try {  
      String catalinabase=System.getenv("CATALINA_BASE");
      if(catalinabase==null || catalinabase.equals("")){
        catalinabase=System.getenv("CATALINA_HOME");
        if(catalinabase==null || catalinabase.equals("")){
          return;
        }
      }
      Properties properties = new Properties();
      properties.load(new FileInputStream(getPropertiesFile()));
      File imagesFolder=new File(catalinabase, "/webapps/"+properties.getProperty("context.name")+"/web/images/");
      if(!imagesFolder.exists() || !imagesFolder.isDirectory()){
        return;
      }

      ConnectionProvider cp = getConnectionProvider();
      String sql="SELECT i.imageurl, i.ad_image_id from ad_image i, m_product p where ";
      sql+=" i.ad_image_id=p.ad_image_id and binarydata is null and i.imageurl is not null order by i.ad_image_id ";
      ResultSet rs=cp.getPreparedStatement(sql).executeQuery();
      while(rs.next()){
        String imageurl=rs.getString(1);
        String imageid=rs.getString(2);
        File imageFile=new File(imagesFolder, imageurl);
        if(imageFile.exists()){
          FileInputStream is = new FileInputStream(imageFile);
          long length = imageFile.length();
          byte[] bytes = new byte[(int)length];
          int offset = 0;
          int numRead = 0;
          while (offset < bytes.length
                 && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
              offset += numRead;
          }
          is.close();
          ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
          BufferedImage rImage = ImageIO.read(bis);
          String qupdate="UPDATE ad_image set name='Image', binarydata=?, width=?, height=?, mimetype=? where ad_image_id=?";
          PreparedStatement ps=cp.getPreparedStatement(qupdate);
          ps.setObject(1, bytes);
          ps.setLong(2, rImage.getWidth());
          ps.setLong(3, rImage.getHeight());
          if (tika==null) {
            tika=new Tika();
          }
          ps.setString(4, tika.detect(bytes));
          ps.setString(5, imageid);
          ps.executeUpdate();
          cp.releasePreparedStatement(ps);
        }
      }
    } catch (Exception e) {
      log4j.info("There was an error when converting old images: "+ e.getMessage(),e);
      return;
    }
    return;
  }
}
