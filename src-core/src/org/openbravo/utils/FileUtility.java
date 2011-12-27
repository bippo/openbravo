/*
 ************************************************************************************
 * Copyright (C) 2001-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

/**
 * @author Fernando Iriazabal
 * @version $Revision: 1.1 $
 */
public class FileUtility {
  private String dir;
  private String filename;
  static Logger log4j = Logger.getLogger(FileUtility.class);

  public FileUtility() {
  }

  public FileUtility(String path, String name) throws IOException {
    this(path, name, true);
  }

  public FileUtility(String path, String name, boolean newFile) throws IOException {
    this(path, name, newFile, false);
  }

  public FileUtility(String path, String name, boolean newFile, boolean readonly)
      throws IOException {
    if (path == null || path.equals(""))
      throw new IllegalArgumentException("directory cannot be null");
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("file name cannot be null");

    File fpath = new File(path);
    if (!fpath.isDirectory())
      throw new IllegalArgumentException("Not a directory: " + path);
    if ((newFile || !readonly) && !fpath.canWrite())
      throw new IllegalArgumentException("Not writable: " + path);

    if (newFile) {
      File file = new File(path, name);
      if (file.canRead())
        throw new IllegalArgumentException("file: " + path + "\\" + name + " already exists");
    }

    dir = path;
    filename = name;
  }

  public boolean ByteArrayToFile(ByteArrayOutputStream in) throws IOException {
    if (in == null)
      return false;
    File f = new File(dir, filename);
    FileOutputStream fos = new FileOutputStream(f);
    in.writeTo(fos);
    fos.close();
    return true;
  }

  public boolean StringToFile(String in) throws IOException {
    if (in == null)
      return false;
    File f = new File(dir, filename);
    FileWriter fileWriterData = new FileWriter(f);
    PrintWriter printWriterData = new PrintWriter(fileWriterData);
    printWriterData.print(in);
    printWriterData.close();
    fileWriterData.close();
    return true;
  }

  public void dumpFile(OutputStream outputstream) {
    byte abyte0[] = new byte[4096];
    try {
      BufferedInputStream bufferedinputstream = new BufferedInputStream(new FileInputStream(
          new File(dir, filename)));
      int i;
      while ((i = bufferedinputstream.read(abyte0, 0, 4096)) != -1)
        outputstream.write(abyte0, 0, i);
      bufferedinputstream.close();
    } catch (Exception exception) {
    }
  }

  public boolean deleteFile() throws IOException {
    File f = new File(dir, filename);
    return f.delete();
  }

  public static int copy(File source, File destiny, DirFilter dirFilter, boolean discardHidden,
      boolean overwrite) throws Exception {
    File[] list;
    int total = 0;
    if (dirFilter != null)
      list = source.listFiles(dirFilter);
    else
      list = source.listFiles();
    for (int i = 0; i < list.length; i++) {
      File fileItem = list[i];
      if (fileItem.isDirectory()) {
        if (!discardHidden || !fileItem.isHidden()/*
                                                   * !fileItem.getName(). startsWith(".")
                                                   */) {
          File faux = new File(destiny, fileItem.getName());
          faux.mkdir();
          // if it is a directory then it is recursively listed
          total += copy(fileItem, faux, dirFilter, discardHidden, overwrite);
        }
      } else {
        File fileAux = new File(destiny, fileItem.getName());
        if (overwrite || !fileAux.exists() || fileItem.lastModified() > fileAux.lastModified()) {
          copyFile(fileItem, fileAux);
          total++;
        }
      }
    }
    return total;
  }

  public static void copyFile(File in, File out) throws Exception {
    FileChannel src = null, cp = null;
    try {
      src = new FileInputStream(in).getChannel();
      cp = new FileOutputStream(out).getChannel();
      long size = src.size();
      MappedByteBuffer buf = src.map(FileChannel.MapMode.READ_ONLY, 0, size);
      cp.write(buf);
    } finally {
      if (src != null)
        src.close();
      if (cp != null)
        cp.close();
    }
  }

  /*
   * public void copyFile(File in, File out) throws Exception { //FileInputStream fis = new
   * FileInputStream(in); BufferedReader fileBuffer = new BufferedReader(new FileReader(in));
   * FileOutputStream fos = new FileOutputStream(out); OutputStreamWriter printWriterData = new
   * OutputStreamWriter(fos, "UTF-8"); String nextLine = fileBuffer.readLine(); while (nextLine !=
   * null) { printWriterData.write(nextLine); printWriterData.write("\n"); nextLine =
   * fileBuffer.readLine(); } printWriterData.flush(); fos.close(); fileBuffer.close();
   * /while((i=fis.read(buf))!=-1) { fos.write(buf, 0, i); } fis.close(); fos.close(); }
   */

  public static void delete(File source) throws Exception {
    File[] list = source.listFiles();
    for (int i = 0; i < list.length; i++) {
      File fileItem = list[i];
      if (fileItem.isDirectory()) {
        delete(fileItem);
      }
      fileItem.delete();
    }
  }

  public boolean exists() {
    File f = new File(dir, filename);
    return f.exists();
  }
}
