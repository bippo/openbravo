package org.openbravo.client.application.window.servlet;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class CalloutServletConfig implements ServletConfig {

  private String servletName;
  private ServletContext context;

  public CalloutServletConfig(String servletName, ServletContext context) {
    this.servletName = servletName;
    this.context = context;
  }

  @Override
  public String getInitParameter(String name) {
    return context.getInitParameter(name);
  }

  @SuppressWarnings({ "rawtypes" })
  @Override
  public Enumeration getInitParameterNames() {
    return context.getInitParameterNames();
  }

  @Override
  public ServletContext getServletContext() {
    return context;
  }

  @Override
  public String getServletName() {
    return servletName;
  }

}
