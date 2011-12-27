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

//: c15:rmi:PerfectTimeI.java
// The PerfectTime remote interface.
package rmi;

import java.rmi.*;
import java.io.*;
import org.xml.sax.*;

public interface RenderFoI extends Remote {
  byte[] computeRenderFo(String strFo) throws RemoteException;
} // /:~