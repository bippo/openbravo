/*
 ************************************************************************************
 * Copyright (C) 2001-2011 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package rmi;

import java.rmi.server.*;
import java.rmi.registry.*;

import java.io.*;
import java.lang.*;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;
import java.net.*;
import java.rmi.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import rmi.*;
import org.xml.sax.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.FileAppender;

public class RenderFo extends UnicastRemoteObject implements RenderFoI {

  private String strFo;
  static Logger logger = Logger.getLogger(RenderFo.class);

  public RenderFo() throws RemoteException {
  }

  public RenderFo(String strFo) throws RemoteException {
    this.strFo = strFo;
  }

  public Object execute() throws RemoteException {
    return computeRenderFo(strFo);
  }

  public byte[] computeRenderFo(String strFo) throws RemoteException {
    byte[] content = null;
    try {
      StringReader sr = new StringReader(strFo);
      InputSource inputFo = new InputSource(sr);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Driver driver = new Driver(inputFo, out);
      driver.run();
      content = out.toByteArray();
      out.close();
    } catch (Exception e) {
      Throwable t = (Throwable) e;
      logger.error("computeRenderFo exception: " + e.getMessage());
      e.printStackTrace();
    }
    return content;
  }

  // Registration for RMI serving:
  public static void main(String[] args) {
    SimpleLayout layout = new SimpleLayout();

    FileAppender appender = null;

    Date today = new Date();

    // format the date in the form "Wed 27 Aug, 2003"
    SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
    String strToday = formatter.format(today);

    String strFileName = "renderFo" + strToday + ".log";

    try {
      appender = new FileAppender(layout, "@basepath@\\logs\\" + strFileName, false); // eg.
                                                                                      // @basepath@
                                                                                      // = c:\\rmi
    } catch (Exception e) {
    }

    logger.addAppender(appender);
    logger.setLevel((Level) Level.WARN);

    System.setSecurityManager(new RMISecurityManager());

    try {
      RenderFo render = new RenderFo();
      Naming.bind("//@RenderFoAddress@/RenderFo", render);
      logger.warn("RenderFo ready");
    } catch (Exception e) {
      logger.error("Error binding:" + e.getMessage());
      e.printStackTrace();
    }
  }
}
