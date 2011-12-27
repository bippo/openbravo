package org.openbravo.erpCommon.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.ddlutils.task.BaseDalInitializingTask;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.utility.Image;

/**
 * 
 * This class loads images from files and stores them in the AD_System_Info table in the database
 * 
 */
public class ImageToDatabaseLoader extends BaseDalInitializingTask {

  private String imagePaths;
  private String basePath;
  private String propertyNames;

  @Override
  public void doExecute() {

    OBContext.setAdminMode();
    try {
      String paths[] = imagePaths.split(",");
      String properties[] = propertyNames.split(",");
      for (int i = 0; i < paths.length; i++) {
        if (OBDal.getInstance().get(SystemInformation.class, "0").get(properties[i]) == null) {
          File f = new File(basePath + File.separator + paths[i]);
          InputStream is = new FileInputStream(f);
          byte[] bytes = new byte[(int) f.length()];
          int offset = 0;
          int numRead = 0;
          while (offset < bytes.length
              && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
          }
          Image image = OBProvider.getInstance().get(Image.class);
          image.setBindaryData(bytes);
          image.setMimetype(MimeTypeUtil.getInstance().getMimeTypeName(bytes));
          Long[] size = Utility.computeImageSize(bytes);
          image.setWidth(size[0]);
          image.setHeight(size[1]);
          image.setName("Image");
          getLog().info("Inserting image with property: " + properties[i]);
          OBDal.getInstance().save(image);
          OBDal.getInstance().get(SystemInformation.class, "0").set(properties[i], image);
          OBDal.getInstance().flush();
        } else {
          getLog().info(
              "Image of property " + properties[i]
                  + " wasn't inserted because it's already in the database.");
        }
      }

      OBDal.getInstance().get(SystemInformation.class, "0")
          .setSupportContact("www.your-it-service.com");

    } catch (Exception e) {
      getLog().error(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public String getImagePaths() {
    return imagePaths;
  }

  public void setImagePaths(String imagePaths) {
    this.imagePaths = imagePaths;
  }

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  public String getPropertyNames() {
    return propertyNames;
  }

  public void setPropertyNames(String propertyNames) {
    this.propertyNames = propertyNames;
  }
}
