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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class CalloutHttpServletResponse implements HttpServletResponse {
  HttpServletResponse wrappedResponse;
  StringWriter sWriter = new StringWriter();
  PrintWriter writer = new PrintWriter(sWriter);

  public CalloutHttpServletResponse(HttpServletResponse wrappedResponse) {
    this.wrappedResponse = wrappedResponse;
  }

  public String getOutputFromWriter() {
    return sWriter.toString();
  }

  public void addCookie(Cookie arg0) {
    wrappedResponse.addCookie(arg0);
  }

  public void addDateHeader(String arg0, long arg1) {
    wrappedResponse.addDateHeader(arg0, arg1);
  }

  public void addHeader(String arg0, String arg1) {
    wrappedResponse.addHeader(arg0, arg1);
  }

  public void addIntHeader(String arg0, int arg1) {
    wrappedResponse.addIntHeader(arg0, arg1);
  }

  public boolean containsHeader(String arg0) {
    return wrappedResponse.containsHeader(arg0);
  }

  @Deprecated
  public String encodeRedirectUrl(String arg0) {
    return wrappedResponse.encodeRedirectUrl(arg0);
  }

  public String encodeRedirectURL(String arg0) {
    return wrappedResponse.encodeRedirectURL(arg0);
  }

  @Deprecated
  public String encodeUrl(String arg0) {
    return wrappedResponse.encodeUrl(arg0);
  }

  public String encodeURL(String arg0) {
    return wrappedResponse.encodeURL(arg0);
  }

  public void flushBuffer() throws IOException {
    wrappedResponse.flushBuffer();
  }

  public int getBufferSize() {
    return wrappedResponse.getBufferSize();
  }

  public String getCharacterEncoding() {
    return wrappedResponse.getCharacterEncoding();
  }

  public String getContentType() {
    return wrappedResponse.getContentType();
  }

  public Locale getLocale() {
    return wrappedResponse.getLocale();
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return wrappedResponse.getOutputStream();
  }

  public PrintWriter getWriter() throws IOException {
    return writer;
  }

  public boolean isCommitted() {
    return wrappedResponse.isCommitted();
  }

  public void reset() {
    wrappedResponse.reset();
  }

  public void resetBuffer() {
    wrappedResponse.resetBuffer();
  }

  public void sendError(int arg0, String arg1) throws IOException {
    wrappedResponse.sendError(arg0, arg1);
  }

  public void sendError(int arg0) throws IOException {
    wrappedResponse.sendError(arg0);
  }

  public void sendRedirect(String arg0) throws IOException {
    wrappedResponse.sendRedirect(arg0);
  }

  public void setBufferSize(int arg0) {
    wrappedResponse.setBufferSize(arg0);
  }

  public void setCharacterEncoding(String arg0) {
    wrappedResponse.setCharacterEncoding(arg0);
  }

  public void setContentLength(int arg0) {
    wrappedResponse.setContentLength(arg0);
  }

  public void setContentType(String arg0) {
    wrappedResponse.setContentType(arg0);
  }

  public void setDateHeader(String arg0, long arg1) {
    wrappedResponse.setDateHeader(arg0, arg1);
  }

  public void setHeader(String arg0, String arg1) {
    wrappedResponse.setHeader(arg0, arg1);
  }

  public void setIntHeader(String arg0, int arg1) {
    wrappedResponse.setIntHeader(arg0, arg1);
  }

  public void setLocale(Locale arg0) {
    wrappedResponse.setLocale(arg0);
  }

  @Deprecated
  public void setStatus(int arg0, String arg1) {
    wrappedResponse.setStatus(arg0, arg1);
  }

  public void setStatus(int arg0) {
    wrappedResponse.setStatus(arg0);
  }

}
