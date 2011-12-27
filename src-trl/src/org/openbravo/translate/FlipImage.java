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
 * unde * The contents of this file are subject to the Openbravo  Public  Licenser the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.translate;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class FlipImage {

  public static BufferedImage getImage(String filePath) throws Exception {
    BufferedImage bImage = ImageIO.read(new File(filePath));
    return bImage;
  }

  public static BufferedImage flip(BufferedImage img) {
    int w = img.getWidth();
    int h = img.getHeight();
    BufferedImage dimg = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
    Graphics2D g = dimg.createGraphics();
    g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);
    g.dispose();
    return dimg;
  }

  public static void saveImage(String path, BufferedImage image) throws Exception {
    File imageFile = new File(path);
    String threeLetterFormat = path.substring(path.length() - 3);
    ImageIO.write(image, threeLetterFormat, imageFile);
  }

  public static void proceed(String path) throws Exception {
    BufferedImage im = getImage(path);
    im = flip(im);
    saveImage(path, im);
  }

}