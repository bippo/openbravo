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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.utils;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.tools.ant.Project;

/**
 * This appender can be used to send log4j to a programmatically set OutputStream. The OutputStream
 * is stored in a ThreadLocal so only log events of the thread itself are send to the OutputStream
 * set by that thread.
 * 
 * @author mtaal
 */
@Deprecated
public class OBLogAppender extends AppenderSkeleton {

  private static final ThreadLocal<OutputStream> outputStreamHolder = new ThreadLocal<OutputStream>();
  private static final ThreadLocal<Project> projectHolder = new ThreadLocal<Project>();
  private static final ThreadLocal<Level> levelHolder = new ThreadLocal<Level>();

  /**
   * @return the ant project held in the threadlocal.
   */
  public static Project getProject() {
    return projectHolder.get();
  }

  public static void setLevel(Level level) {
    levelHolder.set(level);
  }

  /**
   * Sets an ant project in the project threadlocal. Logging events are send to the logger of the
   * project to.
   * 
   * @param project
   *          the ant project
   */
  public static void setProject(Project project) {
    projectHolder.set(project);
  }

  /**
   * Sets the passed OutputStream in a ThreadLocal, this OutputStream is then used by the appender
   * to pass in log4j statements.
   * 
   * @param os
   *          the OutputStream to which log4j events will be send.
   */
  public static void setOutputStream(OutputStream os) {
    outputStreamHolder.set(os);
  }

  /**
   * @return the OutputStream stored in the ThreadLocal, note can be null if no OutputStream has
   *         been set.
   */
  public static OutputStream getOutputStream() {
    return outputStreamHolder.get();
  }

  @Override
  protected void append(LoggingEvent event) {

    try {
      if (projectHolder.get() != null) {
        final String msg;
        if (getLayout() != null) {
          msg = getLayout().format(event);
        } else if (event.getMessage() != null) {
          msg = event.getMessage() + "\n";
        } else {
          msg = " No message for event ";
        }
        logToProject(event.getLevel(), msg);
      }

      if (outputStreamHolder.get() != null) {
        if (event.getLevel().isGreaterOrEqual(levelHolder.get())) {
          if (getLayout() != null) {
            outputStreamHolder.get().write(getLayout().format(event).getBytes());
          } else {
            outputStreamHolder.get().write((event.getMessage().toString() + "\n").getBytes());
          }
          outputStreamHolder.get().flush();
        }
      }
    } catch (final IOException e) {
      // TODO: replace with OBException to log this exception
      // can be done when OBException has been moved to the core
      // lib
      throw new RuntimeException(e);
    }
  }

  private void logToProject(Priority prio, String msg) {
    if (projectHolder.get() == null) {
      return;
    }
    int projectLogLevel = -1;
    switch (prio.toInt()) {
    case Priority.DEBUG_INT:
      projectLogLevel = Project.MSG_DEBUG;
      break;
    case Priority.ERROR_INT:
      projectLogLevel = Project.MSG_ERR;
      break;
    case Priority.FATAL_INT:
      projectLogLevel = Project.MSG_ERR;
      break;
    case Priority.INFO_INT:
      projectLogLevel = Project.MSG_INFO;
      break;
    case Priority.OFF_INT:
      projectLogLevel = Project.MSG_VERBOSE;
      break;
    case Priority.WARN_INT:
      projectLogLevel = Project.MSG_WARN;
      break;
    default:
      throw new IllegalArgumentException("Priority " + prio.toInt() + " unknown");
    }
    projectHolder.get().log(msg, projectLogLevel);
  }

  /**
   * Does not do anything in this implementation.
   */
  public void close() {
  }

  /**
   * @return always returns false
   */
  public boolean requiresLayout() {
    return false;
  }
}
