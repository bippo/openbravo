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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;

/**
 * The request context serves multiple purposes:
 * <ul>
 * <li>Provides the request and response object through a ThreadLocal. This means that these do not
 * have to passed as parameters to methods.</li>
 * <li>If there is no request/response object available then Mock versions are used. This makes it
 * possible to run logic in a test environment outside of the servlet container.</li>
 * <li>Makes it possible to set request parameters to simulate a request from a client to a server.</li>
 * </ul>
 * 
 * Note: compiling through ant gives a deprecated api usage warning, this seems to be a javac bug:
 * http://bugs.sun.com/view_bug.do?bug_id=6460147
 * 
 * @author mtaal
 */
@SuppressWarnings("deprecation")
public class RequestContext {
  private static ServletContext servletContext = new LocalServletContext();

  private static ThreadLocal<RequestContext> instance = new ThreadLocal<RequestContext>();

  public static ServletContext getServletContext() {
    return servletContext;
  }

  public static void setServletContext(ServletContext aServletContext) {
    servletContext = aServletContext;
  }

  public static RequestContext get() {
    if (instance.get() == null) {
      instance.set(new RequestContext());
    }
    return instance.get();
  }

  public static void clear() {
    instance.remove();
  }

  private HttpServletRequest request;
  private String requestContent = null;
  private HttpServletResponse response;

  public String getRequestContent() {
    if (requestContent != null) {
      return requestContent;
    }

    try {
      final BufferedReader reader = getRequest().getReader();
      if (reader == null) {
        return "";
      }
      String line;
      final StringBuilder sb = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        if (sb.length() > 0) {
          sb.append("\n");
        }
        sb.append(line);
      }
      requestContent = sb.toString();
      return requestContent;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  public String getRequestParameter(String name) {
    return getRequest().getParameter(name);
  }

  public Map<?, ?> getParameterMap() {
    return getRequest().getParameterMap();
  }

  public void setRequestParameter(String name, String value) {
    ((HttpServletRequestWrapper) getRequest()).setParameter(name, value);
  }

  public Object getSessionAttribute(String name) {
    return getSession().getAttribute(name.toUpperCase());
  }

  public Enumeration<?> getSessionAttributeNames() {
    return getSession().getAttributeNames();
  }

  public void setSessionAttribute(String name, Object value) {
    getSession().setAttribute(name.toUpperCase(), value);
  }

  public void removeSessionAttribute(String name) {
    getSession().removeAttribute(name.toUpperCase());
  }

  public HttpSession getSession() {
    return getRequest().getSession();
  }

  public HttpServletRequest getRequest() {
    if (!(request instanceof HttpServletRequestWrapper)) {
      final HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper();
      wrapper.setDelegate(request);
      request = wrapper;
    }
    return request;
  }

  public void setRequestContent(String requestContent) {
    ((HttpServletRequestWrapper) getRequest()).setRequestContent(requestContent);
  }

  public VariablesSecureApp getVariablesSecureApp() {
    return new VariablesSecureApp(getRequest());
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public HttpServletResponse getResponse() {
    if (response == null) {
      response = new HttpServletResponseWrapper();
    }
    return response;
  }

  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }

  public static class HttpServletRequestWrapper implements HttpServletRequest {
    private HttpServletRequest delegate;
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private Map<String, String> parameters = new HashMap<String, String>();
    private HttpSession localSession = null;
    private String requestContent;
    private ServletInputStream inputStream;

    public void setParameter(String arg0, String arg1) {
      parameters.put(arg0, arg1);
    }

    public Object getAttribute(String arg0) {
      if (delegate == null) {
        return attributes.get(arg0);
      }
      return delegate.getAttribute(arg0);
    }

    public Enumeration<?> getAttributeNames() {
      if (delegate == null) {
        final List<String> names = new ArrayList<String>();
        names.addAll(attributes.keySet());
        return Collections.enumeration(names);
      }
      return delegate.getAttributeNames();
    }

    public String getAuthType() {
      return delegate.getAuthType();
    }

    public String getCharacterEncoding() {
      return delegate.getCharacterEncoding();
    }

    public int getContentLength() {
      return delegate.getContentLength();
    }

    public String getContentType() {
      if (delegate == null) {
        return "text/html";
      }
      return delegate.getContentType();
    }

    public String getContextPath() {
      return delegate.getContextPath();
    }

    public Cookie[] getCookies() {
      return delegate.getCookies();
    }

    public long getDateHeader(String arg0) {
      return delegate.getDateHeader(arg0);
    }

    public String getHeader(String arg0) {
      return delegate.getHeader(arg0);
    }

    public Enumeration<?> getHeaderNames() {
      return delegate.getHeaderNames();
    }

    public Enumeration<?> getHeaders(String arg0) {
      return delegate.getHeaders(arg0);
    }

    public ServletInputStream getInputStream() throws IOException {
      if (delegate == null) {
        if (inputStream == null) {
          if (requestContent == null) {
            inputStream = new ServletInputStream() {
              @Override
              public int read() throws IOException {
                // TODO Auto-generated method stub
                return 0;
              }
            };
          } else {
            LocalServletInputStream servletInputStream = new LocalServletInputStream();
            servletInputStream.setContent(requestContent);
            inputStream = servletInputStream;
          }
        }
        return inputStream;
      }
      return delegate.getInputStream();
    }

    public int getIntHeader(String arg0) {
      return delegate.getIntHeader(arg0);
    }

    public String getLocalAddr() {
      return delegate.getLocalAddr();
    }

    public Locale getLocale() {
      return delegate.getLocale();
    }

    public Enumeration<?> getLocales() {
      return delegate.getLocales();
    }

    public String getLocalName() {
      return delegate.getLocalName();
    }

    public int getLocalPort() {
      return delegate.getLocalPort();
    }

    public String getMethod() {
      return delegate.getMethod();
    }

    public String getParameter(String arg0) {
      if (delegate == null || parameters.containsKey(arg0)) {
        return parameters.get(arg0);
      }
      return delegate.getParameter(arg0);
    }

    public Map<?, ?> getParameterMap() {
      return parameters;
    }

    @SuppressWarnings("unchecked")
    public Enumeration<?> getParameterNames() {
      final List<String> names = new ArrayList<String>();
      names.addAll(parameters.keySet());
      if (delegate != null) {
        names.addAll(delegate.getParameterMap().keySet());
      }
      return Collections.enumeration(names);
    }

    public String[] getParameterValues(String arg0) {
      if (delegate == null || parameters.containsKey(arg0)) {
        final String value = parameters.get(arg0);
        if (value == null) {
          return null;
        }
        final String[] strArray = { value };
        return strArray;
      }
      return delegate.getParameterValues(arg0);
    }

    public String getPathInfo() {
      return delegate.getPathInfo();
    }

    public String getPathTranslated() {
      return delegate.getPathTranslated();
    }

    public String getProtocol() {
      return delegate.getProtocol();
    }

    public String getQueryString() {
      return delegate.getQueryString();
    }

    public BufferedReader getReader() throws IOException {
      return delegate.getReader();
    }

    public String getRealPath(String arg0) {
      return delegate.getRealPath(arg0);
    }

    public String getRemoteAddr() {
      return delegate.getRemoteAddr();
    }

    public String getRemoteHost() {
      return delegate.getRemoteHost();
    }

    public int getRemotePort() {
      return delegate.getRemotePort();
    }

    public String getRemoteUser() {
      return delegate.getRemoteUser();
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
      return delegate.getRequestDispatcher(arg0);
    }

    public String getRequestedSessionId() {
      return delegate.getRequestedSessionId();
    }

    public String getRequestURI() {
      if (delegate == null) {
        return "";
      }
      return delegate.getRequestURI();
    }

    public StringBuffer getRequestURL() {
      return delegate.getRequestURL();
    }

    public String getScheme() {
      return delegate.getScheme();
    }

    public String getServerName() {
      return delegate.getServerName();
    }

    public int getServerPort() {
      return delegate.getServerPort();
    }

    public String getServletPath() {
      return delegate.getServletPath();
    }

    public HttpSession getSession() {
      if (delegate == null) {
        if (localSession == null) {
          localSession = new HttpSessionWrapper();
        }
        return localSession;
      }
      return delegate.getSession();
    }

    public HttpSession getSession(boolean arg0) {
      if (delegate == null) {
        if (localSession == null && !arg0) {
          return null;
        }
        return getSession();
      }
      return delegate.getSession(arg0);
    }

    public Principal getUserPrincipal() {
      return delegate.getUserPrincipal();
    }

    public boolean isRequestedSessionIdFromCookie() {
      return delegate.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromUrl() {
      return delegate.isRequestedSessionIdFromUrl();
    }

    public boolean isRequestedSessionIdFromURL() {
      return delegate.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdValid() {
      return delegate.isRequestedSessionIdValid();
    }

    public boolean isSecure() {
      return delegate.isSecure();
    }

    public boolean isUserInRole(String arg0) {
      return delegate.isUserInRole(arg0);
    }

    public void removeAttribute(String arg0) {
      if (delegate == null) {
        attributes.remove(arg0);
        return;
      }
      delegate.removeAttribute(arg0);
    }

    public void setAttribute(String arg0, Object arg1) {
      if (delegate == null) {
        attributes.put(arg0, arg1);
        return;
      }
      delegate.setAttribute(arg0, arg1);
    }

    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
      delegate.setCharacterEncoding(arg0);
    }

    public HttpServletRequest getDelegate() {
      return delegate;
    }

    public void setDelegate(HttpServletRequest delegate) {
      this.delegate = delegate;
    }

    public String getRequestContent() {
      return requestContent;
    }

    public void setRequestContent(String requestContent) {
      this.requestContent = requestContent;
    }
  }

  public static class HttpSessionWrapper implements HttpSession {

    private Map<String, Object> attributes = new HashMap<String, Object>();

    @Override
    public Object getAttribute(String arg0) {
      return attributes.get(arg0);
    }

    @Override
    public Enumeration<?> getAttributeNames() {
      return Collections.enumeration(attributes.keySet());
    }

    @Override
    public long getCreationTime() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public String getId() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public long getLastAccessedTime() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public int getMaxInactiveInterval() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public ServletContext getServletContext() {
      return servletContext;
    }

    /**
     * @deprecated
     */
    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Object getValue(String arg0) {
      return null;
    }

    @Override
    public String[] getValueNames() {
      return null;
    }

    @Override
    public void invalidate() {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean isNew() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void putValue(String arg0, Object arg1) {
      // TODO Auto-generated method stub

    }

    @Override
    public void removeAttribute(String arg0) {
      setAttribute(arg0, null);
    }

    @Override
    public void removeValue(String arg0) {
      // TODO Auto-generated method stub
    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
      attributes.put(arg0, arg1);
    }

    @Override
    public void setMaxInactiveInterval(int arg0) {
      // TODO Auto-generated method stub
    }
  }

  public static class HttpServletResponseWrapper implements HttpServletResponse {
    private StringWriter sWriter = new StringWriter();
    private PrintWriter writer = new PrintWriter(sWriter);

    public String getResponseContent() {
      writer.flush();
      sWriter.flush();
      return sWriter.toString();
    }

    @Override
    public void addCookie(Cookie arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void addDateHeader(String arg0, long arg1) {
      // TODO Auto-generated method stub

    }

    @Override
    public void addHeader(String arg0, String arg1) {
      // TODO Auto-generated method stub

    }

    @Override
    public void addIntHeader(String arg0, int arg1) {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean containsHeader(String arg0) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public String encodeRedirectUrl(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String encodeRedirectURL(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String encodeUrl(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String encodeURL(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void sendError(int arg0, String arg1) throws IOException {
      // TODO Auto-generated method stub

    }

    @Override
    public void sendError(int arg0) throws IOException {
      // TODO Auto-generated method stub

    }

    @Override
    public void sendRedirect(String arg0) throws IOException {
      // TODO Auto-generated method stub

    }

    @Override
    public void setDateHeader(String arg0, long arg1) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setHeader(String arg0, String arg1) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setIntHeader(String arg0, int arg1) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setStatus(int arg0, String arg1) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setStatus(int arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void flushBuffer() throws IOException {
      // TODO Auto-generated method stub

    }

    @Override
    public int getBufferSize() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public String getCharacterEncoding() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getContentType() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Locale getLocale() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
      return writer;
    }

    @Override
    public boolean isCommitted() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void reset() {
      // TODO Auto-generated method stub

    }

    @Override
    public void resetBuffer() {
      // TODO Auto-generated method stub

    }

    @Override
    public void setBufferSize(int arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setCharacterEncoding(String arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setContentLength(int arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setContentType(String arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setLocale(Locale arg0) {
      // TODO Auto-generated method stub

    }
  }

  private static class LocalServletInputStream extends ServletInputStream {
    private ByteArrayInputStream localInputStream;

    public void setContent(String content) {
      try {
        localInputStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
      } catch (Exception e) {
        throw new OBException(e);
      }
    }

    public int available() {
      return localInputStream.available();
    }

    public void close() throws IOException {
      localInputStream.close();
    }

    public boolean equals(Object obj) {
      return localInputStream.equals(obj);
    }

    public int hashCode() {
      return localInputStream.hashCode();
    }

    public void mark(int readAheadLimit) {
      localInputStream.mark(readAheadLimit);
    }

    public boolean markSupported() {
      return localInputStream.markSupported();
    }

    public int read() {
      return localInputStream.read();
    }

    public int read(byte[] b, int off, int len) {
      return localInputStream.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
      return localInputStream.read(b);
    }

    public void reset() {
      localInputStream.reset();
    }

    public long skip(long n) {
      return localInputStream.skip(n);
    }

    public String toString() {
      return localInputStream.toString();
    }
  }

  public static class LocalServletContext implements ServletContext {

    private Map<String, Object> attributes = new HashMap<String, Object>();

    @Override
    public Object getAttribute(String arg0) {
      return attributes.get(arg0);
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Enumeration getAttributeNames() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ServletContext getContext(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    public String getContextPath() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getInitParameter(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Enumeration getInitParameterNames() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getMajorVersion() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public String getMimeType(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getMinorVersion() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getRealPath(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public URL getResource(String arg0) throws MalformedURLException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public InputStream getResourceAsStream(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Set getResourcePaths(String arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getServerInfo() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Servlet getServlet(String arg0) throws ServletException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getServletContextName() {
      // TODO Auto-generated method stub
      return null;
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Enumeration getServletNames() {
      // TODO Auto-generated method stub
      return null;
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Enumeration getServlets() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void log(Exception arg0, String arg1) {
      // TODO Auto-generated method stub

    }

    @Override
    public void log(String arg0, Throwable arg1) {
      // TODO Auto-generated method stub

    }

    @Override
    public void log(String arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void removeAttribute(String arg0) {
      attributes.remove(arg0);
    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
      attributes.put(arg0, arg1);
    }
  }

}
