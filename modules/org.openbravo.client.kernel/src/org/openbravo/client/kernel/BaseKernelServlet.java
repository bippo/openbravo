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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openbravo.base.secureApp.HttpSecureAppServlet;

/**
 * A servlet base class which handles redirects and other base tasks.
 * 
 * @author mtaal
 */
@SuppressWarnings("deprecation")
public abstract class BaseKernelServlet extends HttpSecureAppServlet {
  protected static final String RESPONSE_HEADER_ETAG = "ETag";
  protected static final String RESPONSE_HEADER_LASTMODIFIED = "Last-Modified";
  protected static final String RESPONSE_HEADER_CACHE_CONTROL = "Cache-Control";
  protected static final String RESPONSE_NO_CACHE = "no-cache";
  protected static final String RESPONSE_HEADER_CONTENTTYPE = "Content-Type";

  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void service(final HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // encapsulate the response to catch any redirects
    // redirects are done by the authentication manager
    final KernelHttpServletResponse localResponse = new KernelHttpServletResponse();
    localResponse.setDelegate(response);
    RequestContext.get().setResponse(localResponse);

    callServiceInSuper(request, localResponse);

    if (localResponse.isDoLogout()) {
      logout(request, localResponse);
    }

    if (localResponse.getRedirectTarget() != null) {
      if (!response.isCommitted()) {
        // will this always work.... not if the writer is already closed
        response.getWriter().write(
            "window.location.href = '" + localResponse.getRedirectTarget() + "';");
        response.setHeader("Content-Type", KernelConstants.JAVASCRIPT_CONTENTTYPE);
        // prevent caching
        response.setHeader(RESPONSE_HEADER_ETAG, "" + System.currentTimeMillis());
        response.setDateHeader(RESPONSE_HEADER_LASTMODIFIED, System.currentTimeMillis());
        response.setHeader(RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_NO_CACHE);
      }
    } else {
      response.setStatus(200);
    }
  }

  protected void callServiceInSuper(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    super.service(request, response);
  }

  protected void invalidateSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
  }

  public static class KernelHttpServletResponse implements HttpServletResponse {
    private HttpServletResponse delegate;

    private String redirectTarget = null;
    private boolean doLogout = false;

    public boolean isDoLogout() {
      return doLogout;
    }

    public void setDoLogout(boolean doLogout) {
      this.doLogout = doLogout;
    }

    public String getRedirectTarget() {
      return redirectTarget;
    }

    public void setRedirectTarget(String redirectTarget) {
      this.redirectTarget = redirectTarget;
    }

    public void addCookie(Cookie arg0) {
      delegate.addCookie(arg0);
    }

    public void addDateHeader(String arg0, long arg1) {
      delegate.addDateHeader(arg0, arg1);
    }

    public void addHeader(String arg0, String arg1) {
      delegate.addHeader(arg0, arg1);
    }

    public void addIntHeader(String arg0, int arg1) {
      delegate.addIntHeader(arg0, arg1);
    }

    public boolean containsHeader(String arg0) {
      return delegate.containsHeader(arg0);
    }

    public String encodeRedirectUrl(String arg0) {
      return delegate.encodeRedirectUrl(arg0);
    }

    public String encodeRedirectURL(String arg0) {
      return delegate.encodeRedirectURL(arg0);
    }

    public String encodeUrl(String arg0) {
      return delegate.encodeUrl(arg0);
    }

    public String encodeURL(String arg0) {
      return delegate.encodeURL(arg0);
    }

    public void flushBuffer() throws IOException {
      delegate.flushBuffer();
    }

    public int getBufferSize() {
      return delegate.getBufferSize();
    }

    public String getCharacterEncoding() {
      return delegate.getCharacterEncoding();
    }

    public String getContentType() {
      return delegate.getContentType();
    }

    public Locale getLocale() {
      return delegate.getLocale();
    }

    public ServletOutputStream getOutputStream() throws IOException {
      return delegate.getOutputStream();
    }

    public PrintWriter getWriter() throws IOException {
      return delegate.getWriter();
    }

    public boolean isCommitted() {
      return delegate.isCommitted();
    }

    public void reset() {
      delegate.reset();
    }

    public void resetBuffer() {
      delegate.resetBuffer();
    }

    public void sendError(int arg0, String arg1) throws IOException {
      delegate.sendError(arg0, arg1);
    }

    public void sendError(int arg0) throws IOException {
      delegate.sendError(arg0);
    }

    public void sendRedirect(String arg0) throws IOException {
      setRedirectTarget(arg0);
    }

    public void setBufferSize(int arg0) {
      delegate.setBufferSize(arg0);
    }

    public void setCharacterEncoding(String arg0) {
      delegate.setCharacterEncoding(arg0);
    }

    public void setContentLength(int arg0) {
      delegate.setContentLength(arg0);
    }

    public void setContentType(String arg0) {
      delegate.setContentType(arg0);
    }

    public void setDateHeader(String arg0, long arg1) {
      delegate.setDateHeader(arg0, arg1);
    }

    public void setHeader(String arg0, String arg1) {
      delegate.setHeader(arg0, arg1);
    }

    public void setIntHeader(String arg0, int arg1) {
      delegate.setIntHeader(arg0, arg1);
    }

    public void setLocale(Locale arg0) {
      delegate.setLocale(arg0);
    }

    public void setStatus(int arg0, String arg1) {
      delegate.setStatus(arg0, arg1);
    }

    public void setStatus(int arg0) {
      delegate.setStatus(arg0);
    }

    public void setDelegate(HttpServletResponse delegate) {
      this.delegate = delegate;
    }
  }
}
