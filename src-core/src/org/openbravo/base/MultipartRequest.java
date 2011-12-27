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
package org.openbravo.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.apache.commons.fileupload.FileItem;
import org.openbravo.data.FieldProvider;

public class MultipartRequest implements FieldProvider {
  public VariablesBase vars;
  public String filename;
  public boolean firstRowHeads = false;
  public String format = "C";
  public Vector<Object> vector = new Vector<Object>();
  public FieldProvider[] rows;

  FieldProvider objectFieldProvider[];

  public MultipartRequest() {
  }

  public MultipartRequest(VariablesBase vars, String filename, boolean firstLineHeads,
      String format, FieldProvider[] data) throws IOException {
    init(vars, filename, firstLineHeads, format, data);
    readSubmittedFile();
  }

  public MultipartRequest(VariablesBase vars, InputStream in, boolean firstLineHeads,
      String format, FieldProvider[] data) throws IOException {
    init(vars, "", firstLineHeads, format, data);
    readSubmittedFile(in);
  }

  public MultipartRequest(InputStream in, boolean firstLineHeads, String format,
      FieldProvider[] data) throws IOException {
    init("", firstLineHeads, format, data);
    readSubmittedFile(in);
  }

  public String getField(String index) {
    int i = Integer.valueOf(index).intValue();
    if (i >= vector.size())
      return null;
    return ((String) vector.elementAt(i));
  }

  public void addField(String value) {
    if (vector == null)
      vector = new Vector<Object>();
    vector.addElement(value);
  }

  private String setFormatSeparator(String _format) {
    if (_format.equalsIgnoreCase("F"))
      return ("FIXED");
    else if (_format.equalsIgnoreCase("T"))
      return ("\t");
    else if (_format.equalsIgnoreCase("S"))
      return (";");
    else if (_format.equalsIgnoreCase("P"))
      return ("+");
    else if (_format.equalsIgnoreCase("C"))
      return (",");
    else
      return ("");
  }

  public FieldProvider[] getFieldProvider() {
    return objectFieldProvider;
  }

  public FieldProvider setFieldProvider(String linea) {
    if (format.equalsIgnoreCase("FIXED"))
      return lineFixedSize(linea);
    else if (format.equals(""))
      return lineComplete(linea);
    else
      return lineSeparatorFormated(linea);
  }

  public void init(VariablesBase vars, String filename, boolean firstLineHeads, String format,
      FieldProvider[] data) throws IOException {
    if (vars == null)
      throw new IllegalArgumentException("VariablesBase cannot be null");
    // if (filename==null || filename.equals("")) throw new
    // IllegalArgumentException("filename cannot be null");
    this.vars = vars;
    this.filename = filename;
    this.firstRowHeads = firstLineHeads;
    this.format = setFormatSeparator(format);
    this.rows = data;
  }

  private void init(String filename, boolean firstLineHeads, String format, FieldProvider[] data)
      throws IOException {
    this.filename = filename;
    this.firstRowHeads = firstLineHeads;
    this.format = setFormatSeparator(format);
    this.rows = data;
  }

  public FieldProvider lineFixedSize(String linea) {
    if (linea == null || linea.length() < 1)
      return null;
    MultipartRequest data = new MultipartRequest();
    if (rows == null || rows.length == 0)
      data.addField(linea);
    else {
      for (int i = 0; i < rows.length; i++) {
        int init = Integer.valueOf(rows[i].getField("startno")).intValue();
        int end = Integer.valueOf(rows[i].getField("endno")).intValue();
        if (init > linea.length()) {
          data.addField("");
        } else {
          if (init < 0)
            init = 0;
          if (end < 0 || end < init)
            end = init;
          else if (end > linea.length())
            end = linea.length();
          String actual = linea.substring(init, end);
          data.addField(actual);
        }
      }
    }
    return data;
  }

  public FieldProvider lineSeparatorFormated(String linea) {
    return null;
  }

  public FieldProvider lineComplete(String linea) {
    if (linea == null || linea.length() < 1)
      return null;
    MultipartRequest data = new MultipartRequest();
    data.addField(linea);
    return data;
  }

  protected void readSubmittedFile(InputStream in) throws IOException {

    Vector<FieldProvider> vector = new Vector<FieldProvider>();
    int result = 0;
    String linea = "";
    Vector<Byte> vectorInt = new Vector<Byte>();
    boolean isFirstRow = true;
    while ((result = in.read()) != -1) {
      if (result == 13 || result == 10) {
        if (vectorInt.size() > 0) {
          byte[] b = new byte[vectorInt.size()];
          for (int i = 0; i < vectorInt.size(); i++) {
            Byte bAux = vectorInt.elementAt(i);
            b[i] = bAux.byteValue();
          }
          vectorInt = new Vector<Byte>();
          linea = new String(b, "UTF-8");
          if (!isFirstRow || !firstRowHeads) {
            FieldProvider fieldProvider = setFieldProvider(linea);
            vector.addElement(fieldProvider);
          }
        }
        isFirstRow = false;
      } else {
        byte aux = new Integer(result).byteValue();
        vectorInt.addElement(new Byte(aux));
      }
    }
    if (vectorInt.size() > 0 && (!isFirstRow || !firstRowHeads)) {
      byte[] b = new byte[vectorInt.size()];
      for (int i = 0; i < vectorInt.size(); i++) {
        Byte bAux = vectorInt.elementAt(i);
        b[i] = bAux.byteValue();
      }
      vectorInt = new Vector<Byte>();
      linea = new String(b);
      FieldProvider fieldProvider = setFieldProvider(linea);
      vector.addElement(fieldProvider);
    }
    objectFieldProvider = new FieldProvider[vector.size()];
    vector.copyInto(objectFieldProvider);
  }

  protected void readSubmittedFile() throws IOException {
    FileItem fi = vars.getMultiFile(filename);
    if (fi == null)
      throw new IOException("Invalid filename: " + filename);
    InputStream in = fi.getInputStream();
    if (in == null)
      throw new IOException("Corrupted filename: " + filename);
    readSubmittedFile(in);
  }

}
