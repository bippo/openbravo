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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.HttpBaseServlet;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.secureApp.OrgTree;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.Sqlc;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.LicenseClass;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.model.ad.system.ClientInformation;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.uiTranslation.TranslationHandler;
import org.openbravo.utils.FileUtility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.utils.Replace;

/**
 * @author Fernando Iriazabal
 * 
 *         Utility class
 */
public class Utility {
  static Logger log4j = Logger.getLogger(Utility.class);

  public static final String COMMUNITY_BRANDING_URL = "//butler.openbravo.com/heartbeat-server/org.openbravo.butler.communitybranding/CommunityBranding.html";
  public static final String STATIC_COMMUNITY_BRANDING_URL = "StaticCommunityBranding.html";
  public static final String BUTLER_UTILS_URL = "//butler.openbravo.com/web/static-content/js/ob-utils.js";

  private static List<String> autosaveExcludedPackages = null;
  private static List<String> autosaveExcludedClasses = null;

  // List of excludes packages and classes from Autosave
  // TODO: Define the autosave behavior at object level
  static {
    autosaveExcludedPackages = new ArrayList<String>();
    autosaveExcludedClasses = new ArrayList<String>();
    autosaveExcludedPackages.add("org.openbravo.erpCommon.info");
    autosaveExcludedPackages.add("org.openbravo.erpCommon.ad_callouts");
    autosaveExcludedClasses.add("org.openbravo.erpCommon.utility.PopupLoading");
  }

  /**
   * Computes the community branding url on the basis of system information. Note the returned url
   * does not contain the protocol part, it starts with //... So the caller has to prepend it with
   * the needed protocol part (i.e. http: or https:).
   * 
   * @param uiMode
   *          valid values are: 2.50 or MyOB
   */
  public static String getCommunityBrandingUrl(String uiMode) {
    String strLicenseClass = LicenseClass.COMMUNITY.getCode();
    OBContext.setAdminMode();
    try {
      strLicenseClass = ActivationKey.getInstance().getLicenseClass().getCode();

      String purpose = "";

      try {
        purpose = OBDal.getInstance().get(SystemInformation.class, "0").getInstancePurpose();
      } catch (Exception e) {
        log4j.error("Error getting instance purpose", e);
      }

      StringBuilder url = new StringBuilder(COMMUNITY_BRANDING_URL);
      url.append("?licenseClass=" + strLicenseClass);
      url.append("&trial=" + (ActivationKey.getInstance().isTrial() ? "Y" : "N"));
      url.append("&version=" + OBVersion.getInstance().getMajorVersion());
      url.append("&uimode=" + uiMode);
      url.append("&language=" + OBContext.getOBContext().getLanguage().getLanguage());
      url.append("&systemIdentifier=" + SystemInfo.getSystemIdentifier());
      url.append("&macIdentifier=" + SystemInfo.getMacAddress());
      url.append("&databaseIdentifier=" + SystemInfo.getDBIdentifier());
      url.append("&internetConnection=" + (HttpsUtils.isInternetAvailable() ? "Y" : "N"));
      url.append("&systemDate=" + (new SimpleDateFormat("yyyyMMdd")).format(new Date()));
      url.append("&purpose=" + purpose);
      return url.toString();
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Checks if a class is excluded from the autosave process
   * 
   * @param canonicalName
   * @return True is the class is excluded or false if not.
   */
  public static boolean isExcludedFromAutoSave(String canonicalName) {
    final int lastPos = canonicalName.lastIndexOf(".");
    final String packageName = canonicalName.substring(0, lastPos);
    return autosaveExcludedPackages.contains(packageName)
        || autosaveExcludedClasses.contains(canonicalName);
  }

  /**
   * Checks if a getNumericParameters is needed based on a reference. Deprecated, use UIReference.
   * 
   * @param reference
   * @return true if the passed reference represents a numeric type, false otherwise.
   */
  @Deprecated
  public static boolean isNumericParameter(String reference) {
    return (!Utility.isID(reference) && (Utility.isDecimalNumber(reference) || Utility
        .isIntegerNumber(reference)));
  }

  /**
   * Checks if the reference is an ID. Deprecated, use UIReference.
   * 
   * @param reference
   *          String with the reference
   * @return True if is a ID reference
   */
  @Deprecated
  public static boolean isID(String reference) {
    if (reference == null || reference.equals("")) {
      return false;
    }
    return Integer.valueOf(reference).intValue() == 13;
  }

  /**
   * Checks if the references is a decimal number type. Deprecated, use UIReference.
   * 
   * @param reference
   *          String with the reference.
   * @return True if is a decimal or false if not.
   */
  @Deprecated
  public static boolean isDecimalNumber(String reference) {
    if (reference == null || reference.equals(""))
      return false;
    switch (Integer.valueOf(reference).intValue()) {
    case 12:
    case 22:
    case 29:
    case 80008:
      return true;
    }
    return false;
  }

  /**
   * Checks if the references is an integer number type. Deprecated, use UIReference.
   * 
   * @param reference
   *          String with the reference.
   * @return True if is an integer or false if not.
   */
  @Deprecated
  public static boolean isIntegerNumber(String reference) {
    if (reference == null || reference.equals(""))
      return false;
    switch (Integer.valueOf(reference).intValue()) {
    case 11:
    case 13:
    case 25:
      return true;
    }
    return false;
  }

  /**
   * Checks if the references is a datetime type. Deprecated, use UIReference.
   * 
   * @param reference
   *          String with the reference.
   * @return True if is a datetime or false if not.
   */
  @Deprecated
  public static boolean isDateTime(String reference) {
    if (reference == null || reference.equals(""))
      return false;
    switch (Integer.valueOf(reference).intValue()) {
    case 15:
    case 16:
    case 24:
      return true;
    }
    return false;
  }

  /**
   * Returns an String with the date in the specified format
   * 
   * @param date
   *          Date to be formatted.
   * @param pattern
   *          Format expected for the output.
   * @return String formatted.
   */
  public static String formatDate(Date date, String pattern) {
    final SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
    return dateFormatter.format(date);
  }

  /**
   * Checks if the record has attachments associated.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param strTab
   *          String with the tab id.
   * @param recordId
   *          String with the record id.
   * @return True if the record has attachments or false if not.
   * @throws ServletException
   */
  public static boolean hasTabAttachments(ConnectionProvider conn, VariablesSecureApp vars,
      String strTab, String recordId) throws ServletException {
    return UtilityData.hasTabAttachments(conn, Utility.getContext(conn, vars, "#User_Client", ""),
        Utility.getContext(conn, vars, "#AccessibleOrgTree", ""), strTab, recordId);
  }

  /**
   * @see Utility#messageBD(ConnectionProvider, String, String, boolean)
   */
  public static String messageBD(ConnectionProvider conn, String strCode, String strLanguage) {
    return messageBD(conn, strCode, strLanguage, true);
  }

  /**
   * Translate the given code into some message from the application dictionary.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param strCode
   *          String with the code to search.
   * @param strLanguage
   *          String with the translation language.
   * @param escape
   *          Escape \n and " characters
   * @return String with the translated message.
   */
  public static String messageBD(ConnectionProvider conn, String strCode, String strLanguage,
      boolean escape) {
    String strMessage = "";
    if (strLanguage == null || strLanguage.equals(""))
      strLanguage = "en_US";

    try {
      log4j.debug("Utility.messageBD - Message Code: " + strCode);
      strMessage = MessageBDData.message(conn, strLanguage, strCode);
    } catch (final Exception ignore) {
      log4j.error("Error getting message", ignore);
    }
    log4j.debug("Utility.messageBD - Message description: " + strMessage);
    if (strMessage == null || strMessage.equals("")) {
      try {
        strMessage = MessageBDData.columnname(conn, strLanguage, strCode);
      } catch (final Exception e) {
        log4j.error("Error getting message", e);
        strMessage = strCode;
      }
    }
    if (strMessage == null || strMessage.equals("")) {
      strMessage = strCode;
    }
    if (escape) {
      strMessage = Replace.replace(Replace.replace(strMessage, "\n", "\\n"), "\"", "&quot;");
    }
    return strMessage;
  }

  /**
   * 
   * Formats a message String into a String for html presentation. Escapes the &, <, >, " and ®, and
   * replace the \n by <br/>
   * and \r for space.
   * 
   * IMPORTANT! : this method is designed to transform the output of Utility.messageBD method, and
   * this method replaces \n by \\n and \" by &quote. Because of that, the first replacements revert
   * this previous replacements.
   * 
   * @param message
   *          message with java formating
   * @return html format message
   */
  public static String formatMessageBDToHtml(String message) {
    return Replace.replace(Replace.replace(Replace.replace(Replace.replace(Replace.replace(Replace
        .replace(Replace.replace(Replace.replace(Replace.replace(
            Replace.replace(Replace.replace(message, "\\n", "\n"), "&quot", "\""), "&", "&amp;"),
            "\"", "&quot;"), "<", "&lt;"), ">", "&gt;"), "\n", "<br/>"), "\r", " "), "®", "&reg;"),
        "&lt;![CDATA[", "<![CDATA["), "]]&gt;", "]]>");
  }

  /**
   * Gets the value of the given preference.
   * 
   * @param vars
   *          Handler for the session info.
   * @param context
   *          String with the preference.
   * @param window
   *          String with the window id.
   * @return String with the value.
   */
  public static String getPreference(VariablesSecureApp vars, String context, String window) {
    if (context == null || context.equals(""))
      throw new IllegalArgumentException("getPreference - require context");
    String retValue = "";

    retValue = vars.getSessionValue("P|" + window + "|" + context);
    if (retValue.equals(""))
      retValue = vars.getSessionValue("P|" + context);

    return (retValue);
  }

  /**
   * Gets the transactional range defined.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param window
   *          String with the window id.
   * @return String with the value.
   */
  public static String getTransactionalDate(ConnectionProvider conn, VariablesSecureApp vars,
      String window) {
    String retValue = "";

    try {
      retValue = getContext(conn, vars, "Transactional$Range", window);
    } catch (final IllegalArgumentException ignored) {
    }

    if (retValue.equals(""))
      return "1";
    return retValue;
  }

  /**
   * Gets a value from the context. For client 0 is always added (used for references), to check if
   * it must by added or not use the getContext with accesslevel method.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param context
   *          String with the parameter to search.
   * @param window
   *          String with the window id.
   * @return String with the value.
   */
  public static String getContext(ConnectionProvider conn, VariablesSecureApp vars, String context,
      String window) {
    if (context == null || context.equals(""))
      throw new IllegalArgumentException("getContext - require context");
    String retValue = "";

    if (!context.startsWith("#") && !context.startsWith("$")) {
      retValue = getPreference(vars, context, window);
      if (!window.equals("") && retValue.equals(""))
        retValue = vars.getSessionValue(window + "|" + context);
      if (retValue.equals(""))
        retValue = vars.getSessionValue("#" + context);
      if (retValue.equals(""))
        retValue = vars.getSessionValue("$" + context);
    } else {
      try {
        if (context.equalsIgnoreCase("#Date"))
          return DateTimeData.today(conn);
      } catch (final ServletException e) {
      }
      retValue = vars.getSessionValue(context);

      final String userLevel = vars.getSessionValue("#User_Level");

      if (context.equalsIgnoreCase("#AccessibleOrgTree")) {
        if (!retValue.equals("'0'") && !retValue.startsWith("'0',")
            && retValue.indexOf(",'0'") == -1) {// add *
          retValue = "'0'" + (retValue.equals("") ? "" : ",") + retValue;
        }
      }

      if (context.equalsIgnoreCase("#User_Org")) {
        if (userLevel.contains("S") || userLevel.equals(" C"))
          return "'0'"; // force org *

        if (userLevel.equals("  O")) { // remove *
          if (retValue.equals("'0'"))
            retValue = "";
          else if (retValue.startsWith("'0',"))
            retValue = retValue.substring(4);
          else
            retValue = retValue.replace(",'0'", "");
        } else { // add *
          if (!retValue.equals("0") && !retValue.startsWith("'0',")
              && retValue.indexOf(",'0'") == -1) {// Any: current
            // list and *
            retValue = "'0'" + (retValue.equals("") ? "" : ",") + retValue;
          }
        }
      }

      if (context.equalsIgnoreCase("#User_Client")) {
        if (retValue != "'0'" && !retValue.startsWith("'0',") && retValue.indexOf(",'0'") == -1) {
          retValue = "'0'" + (retValue.equals("") ? "" : ",") + retValue;
        }
      }
    }

    return retValue;
  }

  /**
   * Gets a value from the context. Access level values: 1 Organization 3 Client/Organization 4
   * System only 6 System/Client 7 All
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param context
   *          String with the parameter to search.
   * @param strWindow
   *          String with the window id.
   * @param accessLevel
   * @return String with the value.
   */
  public static String getContext(ConnectionProvider conn, VariablesSecureApp vars, String context,
      String strWindow, int accessLevel) {
    if (context == null || context.equals(""))
      throw new IllegalArgumentException("getContext - require context");
    String retValue = "";

    if (!context.startsWith("#") && !context.startsWith("$")) {
      retValue = getPreference(vars, context, strWindow);
      if (!strWindow.equals("") && retValue.equals(""))
        retValue = vars.getSessionValue(strWindow + "|" + context);
      if (retValue.equals(""))
        retValue = vars.getSessionValue("#" + context);
      if (retValue.equals(""))
        retValue = vars.getSessionValue("$" + context);
    } else {
      try {
        if (context.equalsIgnoreCase("#Date"))
          return DateTimeData.today(conn);
      } catch (final ServletException e) {
      }

      retValue = vars.getSessionValue(context);

      final String userLevel = vars.getSessionValue("#User_Level");
      if (context.equalsIgnoreCase("#AccessibleOrgTree")) {
        if (!retValue.equals("0") && !retValue.startsWith("'0',") && retValue.indexOf(",'0'") == -1) {// add
          // *
          retValue = "'0'" + (retValue.equals("") ? "" : ",") + retValue;
        }
      }
      if (context.equalsIgnoreCase("#User_Org")) {
        if (accessLevel == 4 || accessLevel == 6)
          return "'0'"; // force to be org *

        Window window;
        OBContext.setAdminMode();
        try {
          window = org.openbravo.dal.service.OBDal.getInstance().get(Window.class, strWindow);
          if (window.getWindowType().equals("T")) {
            String transactionalOrgs = OrgTree.getTransactionAllowedOrgs(retValue);
            if (transactionalOrgs.equals(""))
              // Will show no organizations into the organization's field of the transactional
              // windows
              return "'-1'";
            else
              return transactionalOrgs;
          } else {
            if ((accessLevel == 1) || (userLevel.equals("  O"))) { // No
              // *:
              // remove
              // 0
              // from
              // current
              // list
              if (retValue.equals("'0'"))
                retValue = "";
              else if (retValue.startsWith("'0',"))
                retValue = retValue.substring(4);
              else
                retValue = retValue.replace(",'0'", "");
            } else {// Any: add 0 to current list
              if (!retValue.equals("'0'") && !retValue.startsWith("'0',")
                  && retValue.indexOf(",'0'") == -1) {// Any:
                // current
                // list
                // and *
                retValue = "'0'" + (retValue.equals("") ? "" : ",") + retValue;
              }
            }
          }
        } finally {
          OBContext.restorePreviousMode();
        }
      }

      if (context.equalsIgnoreCase("#User_Client")) {
        if (accessLevel == 4) {
          if (userLevel.contains("S"))
            return "'0'"; // force client 0
          else
            return "";
        }

        if ((accessLevel == 1) || (accessLevel == 3)) { // No 0
          if (userLevel.contains("S"))
            return "";
          if (retValue.equals("'0'"))
            retValue = "";
          else if (retValue.startsWith("'0',"))
            retValue = retValue.substring(2);
          else
            retValue = retValue.replace(",'0'", "");
        } else if (userLevel.contains("S")) { // Any: add 0
          if (retValue != "'0'" && !retValue.startsWith("'0',") && retValue.indexOf(",'0'") == -1) {
            retValue = "'0'" + (retValue.equals("") ? "" : ",") + retValue;
          }
        }
      }
    }
    log4j.debug("getContext(" + context + "):.. " + retValue);
    return retValue;
  }

  /**
   * Returns the list of referenceables organizations from the current one. This includes all its
   * ancestors and descendants.
   * 
   * @param vars
   * @param currentOrg
   * @return comma delimited Stirng of referenceable organizations.
   */
  public static String getReferenceableOrg(VariablesSecureApp vars, String currentOrg) {
    final OrgTree tree = (OrgTree) vars.getSessionObject("#CompleteOrgTree");
    return tree.getLogicPath(currentOrg).toString();
  }

  /**
   * Returns the list of referenceables organizations from the current one. This includes all its
   * ancestors and descendants. This method takes into account accessLevel and user level: useful to
   * calculate org list for child tabs
   * 
   * @param conn
   * @param vars
   * @param currentOrg
   * @param window
   * @param accessLevel
   * @return the list of referenceable organizations, comma delimited
   */
  public static String getReferenceableOrg(ConnectionProvider conn, VariablesSecureApp vars,
      String currentOrg, String window, int accessLevel) {
    if (accessLevel == 4 || accessLevel == 6)
      return "'0'"; // force to be org *
    final Vector<String> vComplete = getStringVector(getReferenceableOrg(vars, currentOrg));
    final Vector<String> vAccessible = getStringVector(getContext(conn, vars, "#User_Org", window,
        accessLevel));
    return getVectorToString(getIntersectionVector(vComplete, vAccessible));
  }

  /**
   * Returns the organization list for selectors, two cases are possible: <br>
   * <li>Organization is empty (null or ""): accessible list of organizations will be returned. This
   * case is used in calls from filters to selectors. <li>Organization is not empty: referenceable
   * from current organization list of organizations will be returned. This is the way it is called
   * from wad windows.
   * 
   * @param conn
   *          Handler for the database connection
   * @param vars
   * @param currentOrg
   * @return the organization list for selectors
   */
  public static String getSelectorOrgs(ConnectionProvider conn, VariablesSecureApp vars,
      String currentOrg) {
    if ((currentOrg == null) || (currentOrg.equals("")))
      return getContext(conn, vars, "#AccessibleOrgTree", "Selectors");
    else
      return getReferenceableOrg(vars, currentOrg);
  }

  /**
   * Gets a default value.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param columnname
   *          String with the column name.
   * @param context
   *          String with the parameter.
   * @param window
   *          String with the window id.
   * @param defaultValue
   *          with the default value.
   * @param sessionData
   *          FieldProvider with the data stored in session
   * @return String with the value.
   */
  public static String getDefault(ConnectionProvider conn, VariablesSecureApp vars,
      String columnname, String context, String window, String defaultValue,
      FieldProvider sessionData) {
    if (columnname == null || columnname.equals(""))
      return "";

    if (sessionData != null) {
      final String sessionValue = sessionData.getField(columnname);
      if (sessionValue != null) {
        return sessionValue;
      }
    }

    String defStr = getPreference(vars, columnname, window);
    if (!defStr.equals(""))
      return defStr;

    if (context.indexOf("@") == -1) // Tokenize just when contains @
      defStr = context;
    else {
      final StringTokenizer st = new StringTokenizer(context, ",;", false);
      while (st.hasMoreTokens()) {
        final String token = st.nextToken().trim();
        if (token.indexOf("@") == -1)
          defStr = token;
        else
          defStr = parseContext(conn, vars, token, window);
        if (!defStr.equals(""))
          return defStr;
      }
    }
    if (defStr.equals(""))
      defStr = vars.getSessionValue("#" + columnname);
    if (defStr.equals(""))
      defStr = vars.getSessionValue("$" + columnname);
    if (defStr.equals("") && defaultValue != null)
      defStr = defaultValue;
    log4j.debug("getDefault(" + columnname + "): " + defStr);
    return defStr;
  }

  /**
   * Overloaded method for backwards compatibility
   */
  public static String getDefault(ConnectionProvider conn, VariablesSecureApp vars,
      String columnname, String context, String window, String defaultValue) {
    return Utility.getDefault(conn, vars, columnname, context, window, defaultValue, null);
  }

  /**
   * Returns a Vector<String> composed by the comma separated elements in String s
   * 
   * @param s
   * @return the list of String obtained by converting the comma delimited String
   */
  public static Vector<String> getStringVector(String s) {
    final Vector<String> v = new Vector<String>();
    final StringTokenizer st = new StringTokenizer(s, ",", false);
    while (st.hasMoreTokens()) {
      final String token = st.nextToken().trim();
      if (!v.contains(token))
        v.add(token);
    }
    return v;
  }

  /**
   * Returns a Vector<String> with the elements that appear in both v1 and v2 Vectors
   * 
   * @param v1
   * @param v2
   * @return the combination of v1 and v2 without duplicates
   */
  public static Vector<String> getIntersectionVector(Vector<String> v1, Vector<String> v2) {
    final Vector<String> v = new Vector<String>();
    for (int i = 0; i < v1.size(); i++) {
      if (v2.contains(v1.elementAt(i)) && !v.contains(v1.elementAt(i)))
        v.add(v1.elementAt(i));
    }
    return v;
  }

  /**
   * Returns the elements in Vector v as an String separating with commas the elements
   * 
   * @param v
   * @return a comma delimited String
   */
  public static String getVectorToString(Vector<String> v) {
    final StringBuffer s = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      if (s.length() != 0)
        s.append(", ");
      s.append(v.elementAt(i));
    }
    return s.toString();
  }

  /**
   * Parse the given string searching the @ elements to translate with the correct values.
   * Parameters are directly inserted in the returned String.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param context
   *          String to parse.
   * @param window
   *          String with the window id.
   * @return String parsed.
   */
  public static String parseContext(ConnectionProvider conn, VariablesSecureApp vars,
      String context, String window) {
    return parseContext(conn, vars, context, window, null);
  }

  /**
   * Parse the given string searching the @ elements to translate with the correct values. If params
   * is not null it adds a ? symbol in the context and adds the actual value to the params Vector,
   * other case the actual value is replaced in the context.
   */
  public static String parseContext(ConnectionProvider conn, VariablesSecureApp vars,
      String context, String window, Vector<String> params) {
    if (context == null || context.equals(""))
      return "";
    final StringBuffer strOut = new StringBuffer();
    String value = new String(context);
    String token, defStr;
    int i = value.indexOf("@");
    while (i != -1) {
      strOut.append(value.substring(0, i));
      value = value.substring(i + 1);
      final int j = value.indexOf("@");
      if (j == -1) {
        strOut.append(value);
        return strOut.toString();
      }
      token = value.substring(0, j);
      defStr = getContext(conn, vars, token, window);
      if (defStr.equals(""))
        return "";
      if (params != null) {
        params.add(defStr);
        strOut.append("?");
      } else {
        strOut.append(defStr);
      }
      value = value.substring(j + 1);
      i = value.indexOf("@");
    }
    return strOut.toString();
  }

  /**
   * Gets the document number from the database.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param WindowNo
   *          Window id.
   * @param TableName
   *          Table name.
   * @param C_DocTypeTarget_ID
   *          Id of the doctype target.
   * @param C_DocType_ID
   *          id of the doctype.
   * @param onlyDocType
   *          Search only for doctype.
   * @param updateNext
   *          Save the new sequence in database.
   * @return String with the new document number.
   */
  public static String getDocumentNo(ConnectionProvider conn, VariablesSecureApp vars,
      String WindowNo, String TableName, String C_DocTypeTarget_ID, String C_DocType_ID,
      boolean onlyDocType, boolean updateNext) {
    if (TableName == null || TableName.length() == 0)
      throw new IllegalArgumentException("Utility.getDocumentNo - required parameter missing");
    final String AD_Client_ID = getContext(conn, vars, "AD_Client_ID", WindowNo);

    final String cDocTypeID = (C_DocTypeTarget_ID.equals("") ? C_DocType_ID : C_DocTypeTarget_ID);
    if (cDocTypeID.equals(""))
      return getDocumentNo(conn, AD_Client_ID, TableName, updateNext);

    if (AD_Client_ID.equals("0"))
      throw new UnsupportedOperationException("Utility.getDocumentNo - Cannot add System records");

    CSResponse cs = null;
    try {
      cs = DocumentNoData.nextDocType(conn, cDocTypeID, AD_Client_ID, (updateNext ? "Y" : "N"));
    } catch (final ServletException e) {
    }

    if (cs == null || cs.razon == null || cs.razon.equals("")) {
      if (!onlyDocType)
        return getDocumentNo(conn, AD_Client_ID, TableName, updateNext);
      else
        return "0";
    } else
      return cs.razon;
  }

  public static String getDocumentNo(Connection conn, ConnectionProvider con,
      VariablesSecureApp vars, String WindowNo, String TableName, String C_DocTypeTarget_ID,
      String C_DocType_ID, boolean onlyDocType, boolean updateNext) {
    if (TableName == null || TableName.length() == 0)
      throw new IllegalArgumentException("Utility.getDocumentNo - required parameter missing");
    final String AD_Client_ID = getContext(con, vars, "AD_Client_ID", WindowNo);

    final String cDocTypeID = (C_DocTypeTarget_ID.equals("") ? C_DocType_ID : C_DocTypeTarget_ID);
    if (cDocTypeID.equals(""))
      return getDocumentNo(con, AD_Client_ID, TableName, updateNext);

    if (AD_Client_ID.equals("0"))
      throw new UnsupportedOperationException("Utility.getDocumentNo - Cannot add System records");

    CSResponse cs = null;
    try {

      cs = DocumentNoData.nextDocTypeConnection(conn, con, cDocTypeID, AD_Client_ID,
          (updateNext ? "Y" : "N"));
    } catch (final ServletException e) {
    }

    if (cs == null || cs.razon == null || cs.razon.equals("")) {
      if (!onlyDocType)
        return getDocumentNoConnection(conn, con, AD_Client_ID, TableName, updateNext);
      else
        return "0";
    } else
      return cs.razon;
  }

  /**
   * Gets the document number from database.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param AD_Client_ID
   *          String with the client id.
   * @param TableName
   *          Table name.
   * @param updateNext
   *          Save the new sequence in database.
   * @return String with the new document number.
   */
  public static String getDocumentNo(ConnectionProvider conn, String AD_Client_ID,
      String TableName, boolean updateNext) {
    if (TableName == null || TableName.length() == 0)
      throw new IllegalArgumentException("Utility.getDocumentNo - required parameter missing");

    CSResponse cs = null;
    try {
      cs = DocumentNoData.nextDoc(conn, "DocumentNo_" + TableName, AD_Client_ID, (updateNext ? "Y"
          : "N"));
    } catch (final ServletException e) {
    }

    if (cs == null || cs.razon == null)
      return "";
    else
      return cs.razon;
  }

  /**
   * Gets the document number from database.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param AD_Client_ID
   *          String with the client id.
   * @param TableName
   *          Table name.
   * @param updateNext
   *          Save the new sequence in database.
   * @return String with the new document number.
   */
  public static String getDocumentNoConnection(Connection conn, ConnectionProvider con,
      String AD_Client_ID, String TableName, boolean updateNext) {
    if (TableName == null || TableName.length() == 0)
      throw new IllegalArgumentException("Utility.getDocumentNo - required parameter missing");

    CSResponse cs = null;
    try {
      cs = DocumentNoData.nextDocConnection(conn, con, "DocumentNo_" + TableName, AD_Client_ID,
          (updateNext ? "Y" : "N"));
    } catch (final ServletException e) {
    }

    if (cs == null || cs.razon == null)
      return "";
    else
      return cs.razon;
  }

  /**
   * Adds the system element to the given list.
   * 
   * @param list
   *          String with the list.
   * @return String with the modified list.
   */
  public static String addSystem(String list) {
    String retValue = "";

    final Hashtable<String, String> ht = new Hashtable<String, String>();
    ht.put("0", "0");

    final StringTokenizer st = new StringTokenizer(list, ",", false);
    while (st.hasMoreTokens())
      ht.put(st.nextToken(), "x");

    final Enumeration<String> e = ht.keys();
    while (e.hasMoreElements())
      retValue += e.nextElement() + ",";

    retValue = retValue.substring(0, retValue.length() - 1);
    return retValue;
  }

  /**
   * Checks if the user can make modifications in the window.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param AD_Client_ID
   *          Id of the client.
   * @param AD_Org_ID
   *          Id of the organization.
   * @param window
   *          Window id.
   * @return True if has permission, false if not.
   * @throws ServletException
   */
  public static boolean canUpdate(ConnectionProvider conn, VariablesSecureApp vars,
      String AD_Client_ID, String AD_Org_ID, String window) throws ServletException {
    final String User_Level = getContext(conn, vars, "#User_Level", window);

    if (User_Level.indexOf("S") != -1)
      return true;

    boolean retValue = true;
    String whatMissing = "";

    if (AD_Client_ID.equals("0") && AD_Org_ID.equals("0") && User_Level.indexOf("S") == -1) {
      retValue = false;
      whatMissing += "S";
    } else if (!AD_Client_ID.equals("0") && AD_Org_ID.equals("0") && User_Level.indexOf("C") == -1) {
      retValue = false;
      whatMissing += "C";
    } else if (!AD_Client_ID.equals("0") && !AD_Org_ID.equals("0") && User_Level.indexOf("O") == -1) {
      retValue = false;
      whatMissing += "O";
    }

    if (!WindowAccessData.hasWriteAccess(conn, window, vars.getRole()))
      retValue = false;

    return retValue;
  }

  /**
   * Parse the text searching @ parameters to translate.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param language
   *          String with the language to translate.
   * @param text
   *          String with the text to translate.
   * @return String translated.
   */
  public static String parseTranslation(ConnectionProvider conn, VariablesSecureApp vars,
      String language, String text) {
    return parseTranslation(conn, vars, null, language, text);
  }

  /**
   * Parse the text searching @ parameters to translate. If replaceMap is not null and contains a
   * replacement value for a token then it will be used, otherwise the return value of the translate
   * method will be used for the translation.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param replaceMap
   *          optional Map containing replacement values for the tokens
   * @param language
   *          String with the language to translate.
   * @param text
   *          String with the text to translate.
   * @return String translated.
   */
  public static String parseTranslation(ConnectionProvider conn, VariablesSecureApp vars,
      Map<String, String> replaceMap, String language, String text) {
    if (text == null || text.length() == 0)
      return text;

    String inStr = text;
    String token;
    final StringBuffer outStr = new StringBuffer();

    int i = inStr.indexOf("@");
    while (i != -1) {
      outStr.append(inStr.substring(0, i));
      inStr = inStr.substring(i + 1, inStr.length());

      final int j = inStr.indexOf("@");
      if (j < 0) {
        inStr = "@" + inStr;
        break;
      }

      token = inStr.substring(0, j);
      if (replaceMap != null && replaceMap.containsKey(token)) {
        outStr.append(replaceMap.get(token));
      } else {
        outStr.append(translate(conn, vars, token, language));
      }

      inStr = inStr.substring(j + 1, inStr.length());
      i = inStr.indexOf("@");
    }

    outStr.append(inStr);
    return outStr.toString();
  }

  /**
   * For each token found in the parseTranslation method, this method is called to find the correct
   * translation.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param token
   *          String with the token to translate.
   * @param language
   *          String with the language to translate.
   * @return String with the token translated.
   */
  public static String translate(ConnectionProvider conn, VariablesSecureApp vars, String token,
      String language) {
    String strTranslate = token;
    strTranslate = vars.getSessionValue(token);
    if (!strTranslate.equals(""))
      return strTranslate;
    strTranslate = messageBD(conn, token, language);
    if (strTranslate.equals(""))
      return token;
    return strTranslate;
  }

  /**
   * Checks if the value exists in the given array of FieldProviders.
   * 
   * @param data
   *          Array of FieldProviders.
   * @param fieldName
   *          Name of the field to search.
   * @param key
   *          The value to search.
   * @return True if exists or false if not.
   */
  public static boolean isInFieldProvider(FieldProvider[] data, String fieldName, String key) {
    if (data == null || data.length == 0)
      return false;
    else if (fieldName == null || fieldName.trim().equals(""))
      return false;
    else if (key == null || key.trim().equals(""))
      return false;
    String f = "";
    for (int i = 0; i < data.length; i++) {
      try {
        f = data[i].getField(fieldName);
      } catch (final Exception e) {
        log4j.error("Utility.isInFieldProvider - " + e);
        return false;
      }
      if (f != null && f.equalsIgnoreCase(key))
        return true;
    }
    return false;
  }

  /**
   * Gets the window id for a tab.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param strTabID
   *          Id of the tab.
   * @return String with the id of the window.
   * @throws ServletException
   */
  public static String getWindowID(ConnectionProvider conn, String strTabID)
      throws ServletException {
    return UtilityData.getWindowID(conn, strTabID);
  }

  /**
   * Saves the content into a fisical file.
   * 
   * @param strPath
   *          path for the file.
   * @param strFile
   *          name of the file.
   * @param data
   *          content of the file.
   * @return true if everything is ok or false if not.
   */
  public static boolean generateFile(String strPath, String strFile, String data) {
    try {
      final File fileData = new File(strPath, strFile);
      final FileWriter fileWriterData = new FileWriter(fileData);
      final PrintWriter printWriterData = new PrintWriter(fileWriterData);
      printWriterData.print(data);
      fileWriterData.close();
    } catch (final IOException e) {
      e.printStackTrace();
      log4j.error("Problem of IOExceptio in file: " + strPath + " - " + strFile);
      return false;
    }
    return true;
  }

  /*
   * public static String sha1Base64(String text) throws ServletException { if (text==null ||
   * text.trim().equals("")) return ""; String result = text; result =
   * CryptoSHA1BASE64.encriptar(text); return result; }
   * 
   * public static String encryptDecrypt(String text, boolean encrypt) throws ServletException { if
   * (text==null || text.trim().equals("")) return ""; String result = text; if (encrypt) result =
   * CryptoUtility.encrypt(text); else result = CryptoUtility.decrypt(text); return result; }
   */
  /**
   * Checks if the tab is declared as a tree tab.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param stradTabId
   *          Id of the tab.
   * @return True if is a tree tab or false if isn't.
   * @throws ServletException
   */
  public static boolean isTreeTab(ConnectionProvider conn, String stradTabId)
      throws ServletException {
    return UtilityData.isTreeTab(conn, stradTabId);
  }

  /**
   * Fill the parameters of the sql with the session values or FieldProvider values. Used in the
   * combo fields.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param data
   *          FieldProvider with the columns values.
   * @param cmb
   *          ComboTableData object.
   * @param window
   *          Window id.
   * @param actual_value
   *          actual value for the combo.
   * @throws ServletException
   * @see org.openbravo.erpCommon.utility.ComboTableData#fillParameters(FieldProvider, String,
   *      String)
   */
  public static void fillSQLParameters(ConnectionProvider conn, VariablesSecureApp vars,
      FieldProvider data, ComboTableData cmb, String window, String actual_value)
      throws ServletException {
    cmb.fillSQLParameters(conn, vars, data, "", window, actual_value, false);
  }

  /**
   * Fill the parameters of the sql with the session values or FieldProvider values. Used in the
   * combo relation's grids.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param data
   *          FieldProvider with the columns values.
   * @param cmb
   *          TableSQLData object.
   * @param window
   *          Window id.
   * @throws ServletException
   */
  public static void fillTableSQLParameters(ConnectionProvider conn, VariablesSecureApp vars,
      FieldProvider data, TableSQLData cmb, String window) throws ServletException {
    final Vector<String> vAux = cmb.getParameters();
    if (vAux != null && vAux.size() > 0) {
      if (log4j.isDebugEnabled())
        log4j.debug("Combo Parameters: " + vAux.size());
      for (int i = 0; i < vAux.size(); i++) {
        final String strAux = vAux.elementAt(i);
        try {
          final String value = parseParameterValue(conn, vars, data, strAux, "", window, "", false);
          if (log4j.isDebugEnabled())
            log4j.debug("Combo Parameter: " + strAux + " - Value: " + value);
          cmb.setParameter(strAux, value);
        } catch (final Exception ex) {
          throw new ServletException(ex);
        }
      }
    }
  }

  /**
   * Auxiliary method, used by fillSQLParameters and fillTableSQLParameters to get the values for
   * each parameter.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param data
   *          FieldProvider with the columns values.
   * @param name
   *          Name of the parameter.
   * @param window
   *          Window id.
   * @param actual_value
   *          Actual value.
   * @param fromSearch
   *          If the combo is used from the search popup (servlet). If true, then the pattern for
   *          obtaining the parameter values if changed to conform with the search popup naming.
   * @return String with the parsed parameter.
   * @throws Exception
   */
  static String parseParameterValue(ConnectionProvider conn, VariablesSecureApp vars,
      FieldProvider data, String name, String tab, String window, String actual_value,
      boolean fromSearch) throws Exception {
    String strAux = null;
    if (name.equalsIgnoreCase("@ACTUAL_VALUE@"))
      return actual_value;
    if (data != null)
      strAux = data.getField(name);
    if (strAux == null) {
      if (fromSearch) {
        // search popup has different incoming parameter name pattern
        // also preferences (getContext) should not be used for combos in the search popup,
        strAux = vars.getStringParameter("inpParam" + name);
        log4j.debug("parseParameterValues - getStringParameter(inpParam" + name + "): " + strAux);
        // but as search popup 'remembers' old values via the session the read from there needs
        // to be made here, as we disabled getContext (where it was before)
        if (strAux == null || strAux.equals(""))
          strAux = vars.getSessionValue(tab + "|param" + name);

        // Do not use context values for the fields that are in the search pop up
        String strAllFields = vars.getSessionValue("buscador.searchFilds");
        if (strAllFields == null) {
          strAllFields = "";
        }
        if ((strAux == null || strAux.equals("")) && !strAllFields.contains("|" + name + "|")) {
          strAux = Utility.getContext(conn, vars, name, window);
        }
      } else {
        strAux = vars.getStringParameter("inp" + Sqlc.TransformaNombreColumna(name));
        if (log4j.isDebugEnabled())
          log4j.debug("parseParameterValues - getStringParameter(inp"
              + Sqlc.TransformaNombreColumna(name) + "): " + strAux);
        if (strAux == null || strAux.equals(""))
          strAux = Utility.getContext(conn, vars, name, window);
      }
    }
    return strAux;
  }

  /**
   * Gets the Message for the instance of the processes.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param pinstanceData
   *          Array with the instance information.
   * @return Object with the message.
   * @throws ServletException
   */
  public static OBError getProcessInstanceMessage(ConnectionProvider conn, VariablesSecureApp vars,
      PInstanceProcessData[] pinstanceData) throws ServletException {
    OBError myMessage = new OBError();
    if (pinstanceData != null && pinstanceData.length > 0) {
      String message = "";
      String title = "Error";
      String type = "Error";
      if (!pinstanceData[0].errormsg.equals("")) {
        message = pinstanceData[0].errormsg;
      } else if (!pinstanceData[0].pMsg.equals("")) {
        message = pinstanceData[0].pMsg;
      }

      if (pinstanceData[0].result.equals("1")) {
        type = "Success";
        title = Utility.messageBD(conn, "Success", vars.getLanguage());
      } else if (pinstanceData[0].result.equals("0")) {
        type = "Error";
        title = Utility.messageBD(conn, "Error", vars.getLanguage());
      } else {
        type = "Warning";
        title = Utility.messageBD(conn, "Warning", vars.getLanguage());
      }

      final int errorPos = message.indexOf("@ERROR=");
      if (errorPos != -1) {
        myMessage = Utility.translateError(conn, vars, vars.getLanguage(),
            "@CODE=@" + message.substring(errorPos + 7));
        if (log4j.isDebugEnabled())
          log4j.debug("Error Message returned: " + myMessage.getMessage());
        if (message.substring(errorPos + 7).equals(myMessage.getMessage())) {
          myMessage.setMessage(parseTranslation(conn, vars, vars.getLanguage(),
              myMessage.getMessage()));
        }
        if (errorPos > 0)
          message = message.substring(0, errorPos);
        else
          message = "";
      }
      if (!message.equals("") && message.indexOf("@") != -1)
        message = Utility.parseTranslation(conn, vars, vars.getLanguage(), message);
      myMessage.setType(type);
      myMessage.setTitle(title);
      myMessage.setMessage(message + ((!message.equals("") && errorPos != -1) ? " <br> " : "")
          + myMessage.getMessage());
    }
    return myMessage;
  }

  /**
   * Translate the message, searching the @ parameters, and making use of the ErrorTextParser class
   * to get the appropiated message.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param strLanguage
   *          Language to translate.
   * @param message
   *          Strin with the message to translate.
   * @return Object with the message.
   */
  public static OBError translateError(ConnectionProvider conn, VariablesSecureApp vars,
      String strLanguage, String message) {
    final OBError myError = new OBError();
    myError.setType("Error");
    myError.setMessage(message);
    if (message != null && !message.equals("")) {
      String code = "";
      if (log4j.isDebugEnabled())
        log4j.debug("translateError - message: " + message);
      if (message.startsWith("@CODE=@"))
        message = message.substring(7);
      else if (message.startsWith("@CODE=")) {
        message = message.substring(6);
        final int pos = message.indexOf("@");
        if (pos == -1) {
          code = message;
          message = "";
        } else {
          code = message.substring(0, pos);
          message = message.substring(pos + 1);
        }
      }
      myError.setMessage(message);
      if (log4j.isDebugEnabled())
        log4j.debug("translateError - code: " + code + " - message: " + message);

      // BEGIN Checking if is a pool problem
      if (code != null && code.equals("NoConnectionAvailable")) {
        myError.setType("Error");
        myError.setTitle("Critical Error");
        myError.setConnectionAvailable(false);
        myError.setMessage("No database connection available");
        return myError;
      }
      // END Checking if is a pool problem

      // BEGIN Parsing message text
      if (message != null && !message.equals("")) {
        final String rdbms = conn.getRDBMS();
        ErrorTextParser myParser = null;
        try {
          final Class<?> c = Class.forName("org.openbravo.erpCommon.utility.ErrorTextParser"
              + rdbms.toUpperCase());
          myParser = (ErrorTextParser) c.newInstance();
        } catch (final ClassNotFoundException ex) {
          log4j.warn("Couldn´t find class: org.openbravo.erpCommon.utility.ErrorTextParser"
              + rdbms.toUpperCase());
          myParser = null;
        } catch (final Exception ex1) {
          log4j.warn("Couldn´t initialize class: org.openbravo.erpCommon.utility.ErrorTextParser"
              + rdbms.toUpperCase());
          myParser = null;
        }
        if (myParser != null) {
          myParser.setConnection(conn);
          myParser.setLanguage(strLanguage);
          myParser.setMessage(message);
          myParser.setVars(vars);
          try {
            final OBError myErrorAux = myParser.parse();
            if (myErrorAux != null
                && !myErrorAux.getMessage().equals("")
                && (code == null || code.equals("") || code.equals("0") || !myErrorAux.getMessage()
                    .equalsIgnoreCase(message)))
              return myErrorAux;
          } catch (final Exception ex) {
            log4j.error("Error while parsing text: " + ex);
          }
        }
      } else
        myError.setMessage(code);
      // END Parsing message text

      // BEGIN Looking for error code in AD_Message
      if (code != null && !code.equals("")) {
        final FieldProvider fldMessage = locateMessage(conn, code, strLanguage);
        if (fldMessage != null) {
          myError.setType((fldMessage.getField("msgtype").equals("E") ? "Error" : (fldMessage
              .getField("msgtype").equals("I") ? "Info" : (fldMessage.getField("msgtype").equals(
              "S") ? "Success" : "Warning"))));
          myError.setMessage(fldMessage.getField("msgtext"));
          return myError;
        }
      }
      // END Looking for error code in AD_Message
    }
    return myError;
  }

  /**
   * Search a message in the database.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param strCode
   *          Message to search.
   * @param strLanguage
   *          Language to translate.
   * @return FieldProvider with the message info.
   */
  public static FieldProvider locateMessage(ConnectionProvider conn, String strCode,
      String strLanguage) {
    FieldProvider[] fldMessage = null;

    try {
      if (log4j.isDebugEnabled())
        log4j.debug("Utility.messageBD - Message Code: " + strCode);
      fldMessage = MessageBDData.messageInfo(conn, strLanguage, strCode);
    } catch (final Exception ignore) {
    }
    if (fldMessage != null && fldMessage.length > 0)
      return fldMessage[0];
    else
      return null;
  }

  public String getServletInfo() {
    return "This servlet add some functions";
  }

  /**
   * Checks if an element is in a list. List is an string like "(e1, e2, e3,...)" where en are
   * elements. It is inteeded to be used for checking user client and organizations.
   * 
   * @param strList
   *          List to check in
   * @param strElement
   *          Element to check in the list
   * @return true in case the element is in the list
   */
  public static boolean isElementInList(String strList, String strElement) {
    strList = strList.replace("(", "").replace(")", "");
    final StringTokenizer st = new StringTokenizer(strList, ",", false);
    strElement = strElement.replaceAll("'", "");

    while (st.hasMoreTokens()) {
      final String token = st.nextToken().trim().replaceAll("'", "");
      if (token.equals(strElement))
        return true;
    }
    return false;
  }

  /**
   * Returns a JavaScript function to be used on selectors Depending on what element you want to
   * focus, you pass the id
   * 
   * @param id
   *          the html tag id to focus on
   * @return a String JavaScript function
   */
  public static String focusFieldJS(String id) {
    final String r = "\n function focusOnField() { \n" + " setWindowElementFocus('" + id
        + "', 'id'); \n" + " return true; \n" + "} \n";
    return r;
  }

  /**
   * Write the output to a file. It creates a file in the file location writing the content of the
   * outputstream.
   * 
   * @param fileLocation
   *          the file where you are going to write
   * @param outputstream
   *          the data source
   */
  public static void dumpFile(String fileLocation, OutputStream outputstream) {
    final byte dataPart[] = new byte[4096];
    try {
      final BufferedInputStream bufferedinputstream = new BufferedInputStream(new FileInputStream(
          new File(fileLocation)));
      int i;
      while ((i = bufferedinputstream.read(dataPart, 0, 4096)) != -1)
        outputstream.write(dataPart, 0, i);
      bufferedinputstream.close();
    } catch (final Exception exception) {
    }
  }

  /**
   * Returns a string list comma separated as SQL strings.
   * 
   * @param list
   * @return comma delimited quoted string
   */
  public static String stringList(String list) {
    String ret = "";
    final boolean hasBrackets = list.startsWith("(") && list.endsWith(")");
    if (hasBrackets)
      list = list.substring(1, list.length() - 1);
    final StringTokenizer st = new StringTokenizer(list, ",", false);
    while (st.hasMoreTokens()) {
      String token = st.nextToken().trim();
      if (!ret.equals(""))
        ret += ", ";
      if (!(token.startsWith("'") && token.endsWith("'")))
        token = "'" + token + "'";
      ret += token;
    }
    if (hasBrackets)
      ret = "(" + ret + ")";
    return ret;
  }

  /**
   * Determines if a string of characters is an Openbravo UUID (Universal Unique Identifier), i.e.,
   * if it is a 32 length hexadecimal string.
   * 
   * @param CharacterString
   *          A string of characters.
   * @return Returns true if this string of characters is an UUID.
   */
  public static boolean isUUIDString(String CharacterString) {
    if (CharacterString.length() == 32) {
      for (int i = 0; i < CharacterString.length(); i++) {
        if (!isHexStringChar(CharacterString.charAt(i)))
          return false;
      }
      return true;
    }
    return false;
  }

  /**
   * Returns true if the input argument character is A-F, a-f or 0-9.
   * 
   * @param c
   *          A single character.
   * @return Returns true if this character is hexadecimal.
   */
  public static final boolean isHexStringChar(char c) {
    return (("0123456789abcdefABCDEF".indexOf(c)) >= 0);
  }

  /**
   * Deletes recursively a (non-empty) array of directories
   * 
   * @param f
   * @return true in case the deletion has been successful
   */
  public static boolean deleteDir(File[] f) {
    for (int i = 0; i < f.length; i++) {
      if (!deleteDir(f[i]))
        return false;
    }
    return true;
  }

  /**
   * Deletes recursively a (non-empty) directory
   * 
   * @param f
   * @return true in case the deletion has been successful
   */
  public static boolean deleteDir(File f) {
    if (f.isDirectory()) {
      final File elements[] = f.listFiles();
      for (int i = 0; i < elements.length; i++) {
        if (!deleteDir(elements[i]))
          return false;
      }
    }
    return f.delete();
  }

  /**
   * Generates a String representing the file in a path
   * 
   * @param strPath
   * @return file to a String
   */
  public static String fileToString(String strPath) throws FileNotFoundException {
    StringBuffer strMyFile = new StringBuffer("");
    try {
      File f = new File(strPath);
      FileInputStream fis = new FileInputStream(f);
      InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

      final BufferedReader mybr = new BufferedReader(isr);

      String strTemp = mybr.readLine();
      strMyFile.append(strTemp);
      while (strTemp != null) {
        strTemp = mybr.readLine();
        if (strTemp != null)
          strMyFile.append("\n").append(strTemp);
        else {
          mybr.close();
          fis.close();
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return strMyFile.toString();
  }

  /**
   * Generates a String representing the wikified name from source
   * 
   * @param strSource
   * @return strTarget: wikified name
   */
  public static String wikifiedName(String strSource) throws FileNotFoundException {
    if (strSource == null || strSource.equals(""))
      return strSource;
    strSource = strSource.trim();
    if (strSource.equals(""))
      return strSource;
    final StringTokenizer source = new StringTokenizer(strSource, " ", false);
    String strTarget = "";
    String strTemp = "";
    int i = 0;
    while (source.hasMoreTokens()) {
      strTemp = source.nextToken();
      if (i != 0)
        strTarget = strTarget + "_" + strTemp;
      else {
        final String strFirstChar = strTemp.substring(0, 1);
        strTemp = strFirstChar.toUpperCase() + strTemp.substring(1, strTemp.length());
        strTarget = strTarget + strTemp;
      }
      i++;
    }
    return strTarget;
  }

  public static String getButtonName(ConnectionProvider conn, VariablesSecureApp vars,
      String reference, String currentValue, String buttonId,
      HashMap<String, String> usedButtonShortCuts, HashMap<String, String> reservedButtonShortCuts) {
    try {
      final UtilityData[] data = UtilityData.selectReference(conn, vars.getLanguage(), reference);
      String retVal = "";
      if (currentValue.equals("--"))
        currentValue = "CL";
      if (data == null)
        return retVal;
      for (int j = 0; j < data.length; j++) {
        int i = 0;
        final String name = data[j].name;
        while ((i < name.length())
            && (name.substring(i, i + 1).equals(" ") || reservedButtonShortCuts.containsKey(name
                .substring(i, i + 1).toUpperCase()))) {
          if (data[j].value.equals(currentValue))
            retVal += name.substring(i, i + 1);
          i++;
        }
        if ((i == name.length()) && (data[j].value.equals(currentValue))) {
          i = 1;
          while (i <= 10 && reservedButtonShortCuts.containsKey(new Integer(i).toString()))
            i++;
          if (i < 10) {
            if (data[j].value.equals(currentValue)) {
              retVal += "<span>(<u>" + i + "</u>)</span>";
              reservedButtonShortCuts.put(new Integer(i).toString(), "");
              usedButtonShortCuts.put(new Integer(i).toString(), "executeWindowButton('" + buttonId
                  + "');");
            }
          }
        } else {

          if (data[j].value.equals(currentValue)) {
            if (i < name.length())
              reservedButtonShortCuts.put(name.substring(i, i + 1).toUpperCase(), "");
            usedButtonShortCuts.put(name.substring(i, i + 1).toUpperCase(), "executeWindowButton('"
                + buttonId + "');");
            retVal += "<u>" + name.substring(i, i + 1) + "</u>" + name.substring(i + 1);
          }
        }
      }

      return retVal;
    } catch (final Exception e) {
      log4j.error(e.toString());
      return currentValue;
    }
  }

  public static String getButtonName(ConnectionProvider conn, VariablesSecureApp vars,
      String fieldId, String buttonId, HashMap<String, String> usedButtonShortCuts,
      HashMap<String, String> reservedButtonShortCuts) {
    try {
      final UtilityData data = UtilityData.selectFieldName(conn, vars.getLanguage(), fieldId);
      String retVal = "";
      if (data == null)
        return retVal;
      final String name = data.name;
      int i = 0;
      while ((i < name.length())
          && (name.substring(i, i + 1).equals(" ") || reservedButtonShortCuts.containsKey(name
              .substring(i, i + 1).toUpperCase()))) {
        retVal += name.substring(i, i + 1);
        i++;
      }

      if (i == name.length()) {
        i = 1;
        while (i <= 10 && reservedButtonShortCuts.containsKey(new Integer(i).toString()))
          i++;
        if (i < 10) {
          retVal += "<span>(<u>" + i + "</u>)</span>";
          reservedButtonShortCuts.put(new Integer(i).toString(), "");
          usedButtonShortCuts.put(new Integer(i).toString(), "executeWindowButton('" + buttonId
              + "');");
        }
      } else {
        if (i < name.length())
          reservedButtonShortCuts.put(name.substring(i, i + 1).toUpperCase(), "");
        usedButtonShortCuts.put(name.substring(i, i + 1).toUpperCase(), "executeWindowButton('"
            + buttonId + "');");
        retVal += "<u>" + name.substring(i, i + 1) + "</u>" + name.substring(i + 1);
      }

      return retVal;
    } catch (final Exception e) {
      log4j.error(e.toString());
      return "";
    }
  }

  /**
   * Returns the ID of the base currency of the given client
   * 
   * @param strClientId
   *          ID of client.
   * @return Returns String strBaseCurrencyId with the ID of the base currency.
   * @throws ServletException
   */
  public static String stringBaseCurrencyId(ConnectionProvider conn, String strClientId)
      throws ServletException {
    final String strBaseCurrencyId = UtilityData.getBaseCurrencyId(conn, strClientId);
    return strBaseCurrencyId;
  }

  /**
   * Build a JavaScript variable used for prompting a confirmation on changes
   * 
   * @param vars
   *          Helper to access the user context
   * @param windowId
   *          Identifier of the window
   * @return A string containing a JavaScript variable to be used by the checkForChanges function
   *         (utils.js)
   */
  public static String getJSConfirmOnChanges(VariablesSecureApp vars, String windowId) {
    String jsString = "var confirmOnChanges = ";
    String showConfirmation = getPreference(vars, "ShowConfirmation", windowId);

    if (showConfirmation == null || showConfirmation.equals(""))
      showConfirmation = vars.getSessionValue("#ShowConfirmation");
    jsString = jsString + (showConfirmation.equalsIgnoreCase("Y") ? "true" : "false") + ";";
    return jsString;
  }

  /**
   * Transforms an ArrayList to a String comma separated.
   * 
   * @param list
   * @return a comma separated String containing the contents of the array.
   */
  public static String arrayListToString(ArrayList<String> list, boolean addQuotes) {
    String rt = "";
    for (int i = 0; i < list.size(); i++) {
      rt += rt.equals("") ? "" : ", " + (addQuotes ? "'" : "") + list.get(i)
          + (addQuotes ? "'" : "");
    }
    return rt;
  }

  /**
   * Transforms a comma separated String into an ArrayList
   * 
   * @param list
   * @return the list representation of the comma delimited String
   */
  public static ArrayList<String> stringToArrayList(String list) {
    final ArrayList<String> rt = new ArrayList<String>();
    final StringTokenizer st = new StringTokenizer(list, ",");
    while (st.hasMoreTokens()) {
      final String token = st.nextToken().trim();
      rt.add(token);
    }
    return rt;
  }

  /**
   * Transforms a String[] into an ArrayList
   * 
   * @param list
   * @return the list representation of the array
   */
  public static ArrayList<String> stringToArrayList(String[] list) {
    final ArrayList<String> rt = new ArrayList<String>();
    if (list == null)
      return rt;
    for (int i = 0; i < list.length; i++)
      rt.add(list[i]);
    return rt;
  }

  /**
   * Returns the ISO code plus the symbol of the given currency in the form (ISO-SYM), e.g., (USD-$)
   * 
   * @param strCurrencyID
   *          ID of the currency.
   * @return Returns String strISOSymbol with the ISO code plus the symbol of the currency.
   * @throws ServletException
   */
  public static String stringISOSymbol(ConnectionProvider conn, String strCurrencyID)
      throws ServletException {
    final String strISOSymbol = UtilityData.getISOSymbol(conn, strCurrencyID);
    return strISOSymbol;
  }

  @Deprecated
  // in 2.50
  public static boolean hasAttachments(ConnectionProvider conn, String userClient, String userOrg,
      String tableId, String recordId) throws ServletException {
    if (tableId.equals("") || recordId.equals(""))
      return false;
    else
      return UtilityData.select(conn, userClient, userOrg, tableId, recordId);
  }

  /**
   * Determines the labor days between two dates
   * 
   * @param strDate1
   *          Date 1.
   * @param strDate2
   *          Date 2.
   * @param DateFormatter
   *          Format of the dates.
   * @return strLaborDays as the number of days between strDate1 and strDate2.
   */
  public static String calculateLaborDays(String strDate1, String strDate2, DateFormat DateFormatter)
      throws ParseException {
    String strLaborDays = "";
    if (strDate1 != null && !strDate1.equals("") && strDate2 != null && !strDate2.equals("")) {
      Integer LaborDays = 0;
      if (Utility.isBiggerDate(strDate1, strDate2, DateFormatter)) {
        do {
          strDate2 = Utility.addDaysToDate(strDate2, "1", DateFormatter); // Adds a day to the Date
          // 2 until it
          // reaches the Date 1
          if (!Utility.isWeekendDay(strDate2, DateFormatter))
            LaborDays++; // If it is not a weekend day, it adds a
          // day to the labor days
        } while (!strDate2.equals(strDate1));
      } else {
        do {
          strDate1 = Utility.addDaysToDate(strDate1, "1", DateFormatter); // Adds a day to the Date
          // 1 until it
          // reaches the Date 2
          if (!Utility.isWeekendDay(strDate1, DateFormatter))
            LaborDays++; // If it is not a weekend day, it adds a
          // day to the labor days
        } while (!strDate1.equals(strDate2));
      }
      strLaborDays = LaborDays.toString();
    }
    return strLaborDays;
  }

  /**
   * Adds an integer number of days to a given date
   * 
   * @param strDate
   *          Start date.
   * @param strDays
   *          Number of days to add.
   * @param DateFormatter
   *          Format of the date.
   * @return strFinalDate as the sum of strDate plus strDays.
   */
  public static String addDaysToDate(String strDate, String strDays, DateFormat DateFormatter)
      throws ParseException {
    String strFinalDate = "";
    if (strDate != null && !strDate.equals("") && strDays != null && !strDays.equals("")) {
      final Calendar FinalDate = Calendar.getInstance();
      FinalDate.setTime(DateFormatter.parse(strDate)); // FinalDate equals
      // to strDate
      FinalDate.add(Calendar.DATE, Integer.parseInt(strDays)); // FinalDate
      // equals
      // to
      // strDate
      // plus one
      // day
      strFinalDate = DateFormatter.format(FinalDate.getTime());
    }
    return strFinalDate;
  }

  /**
   * Determines the format of the date
   * 
   * @param vars
   *          Global variables.
   * @return DateFormatter as the format of the date.
   */
  public static DateFormat getDateFormatter(VariablesSecureApp vars) {
    String strFormat = vars.getSessionValue("#AD_SqlDateFormat").toString();
    strFormat = strFormat.replace('Y', 'y'); // Java accepts 'yy' for the
    // year
    strFormat = strFormat.replace('D', 'd'); // Java accepts 'dd' for the
    // day of the date
    final DateFormat DateFormatter = new SimpleDateFormat(strFormat);
    return DateFormatter;
  }

  /**
   * Determines if a day is a day of the weekend, i.e., Saturday or Sunday
   * 
   * @param strDay
   *          Given Date.
   * @param DateFormatter
   *          Format of the date.
   * @return true if the date is a Sunday or a Saturday.
   */
  public static boolean isWeekendDay(String strDay, DateFormat DateFormatter) throws ParseException {
    final Calendar Day = Calendar.getInstance();
    Day.setTime(DateFormatter.parse(strDay));
    final int weekday = Day.get(Calendar.DAY_OF_WEEK); // Gets the number of
    // the day of the
    // week: 1-Sunday,
    // 2-Monday,
    // 3-Tuesday,
    // 4-Wednesday,
    // 5-Thursday,
    // 6-Friday,
    // 7-Saturday
    if (weekday == 1 || weekday == 7)
      return true; // 1-Sunday, 7-Saturday
    return false;
  }

  /**
   * Determines if a date 1 is bigger than a date 2
   * 
   * @param strDate1
   *          Date 1.
   * @param strDate2
   *          Date 2.
   * @param DateFormatter
   *          Format of the dates.
   * @return true if strDate1 is bigger than strDate2.
   */
  public static boolean isBiggerDate(String strDate1, String strDate2, DateFormat DateFormatter)
      throws ParseException {
    final Calendar Date1 = Calendar.getInstance();
    Date1.setTime(DateFormatter.parse(strDate1));
    final long MillisDate1 = Date1.getTimeInMillis();
    final Calendar Date2 = Calendar.getInstance();
    Date2.setTime(DateFormatter.parse(strDate2));
    final long MillisDate2 = Date2.getTimeInMillis();
    if (MillisDate1 > MillisDate2)
      return true; // Date 1 is bigger than Date 2
    return false;
  }

  public static JasperReport getTranslatedJasperReport(ConnectionProvider conn, String reportName,
      String language, String baseDesignPath) throws JRException {

    log4j.debug("translate report: " + reportName + " for language: " + language);

    File reportFile = new File(reportName);

    InputStream reportInputStream = null;
    if (reportFile.exists()) {
      TranslationHandler handler = new TranslationHandler(conn);
      handler.prepareFile(reportName, language, reportFile, baseDesignPath);
      reportInputStream = handler.getInputStream();
    }
    JasperDesign jasperDesign;
    if (reportInputStream != null) {
      log4j.debug("Jasper report being created with inputStream.");
      jasperDesign = JRXmlLoader.load(reportInputStream);
    } else {
      log4j.debug("Jasper report being created with strReportName.");
      jasperDesign = JRXmlLoader.load(reportName);
    }

    JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

    return jasperReport;
  }

  /**
   * Returns the URL for a tab
   * 
   * @param tabId
   *          Id for the tab to obtain the url for
   * @param type
   *          "R" -> Relation, "E" -> Edition, "X" -> Excel
   * @param completeURL
   *          if true returns the complete ULR including server name and context, if not, it return
   *          URL relative to base context
   * @return the URL for a tab.
   */
  public static String getTabURL(String tabId, String type, boolean completeURL) {
    OBContext.setAdminMode();
    try {
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      if (tab == null) {
        log4j.error("Error trying to obtain URL for unknown tab:" + tabId);
        return "";
      }

      String url = (completeURL ? HttpBaseServlet.strDireccion : "") + "/";
      if (!"0".equals(tab.getWindow().getModule().getId())) {
        url += tab.getWindow().getModule().getJavaPackage();
      }
      url += mappingFormat(tab.getWindow().getName()) + "/" + mappingFormat(tab.getName());

      if (!"0".equals(tab.getModule().getId())) {
        url += tab.getId();
      }

      if ("R".equals(type)) {
        url += "_Relation.html";
      } else if ("X".equals(type)) {
        url += "_Excel.html";
      } else if ("none".equals(type)) {
        // do nothing
      } else {
        url += "_Edition.html";
      }

      return url;
    } catch (Exception e) {
      log4j.error(e.getMessage());
      return "";
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * @deprecated use {@link Utility#getTabURL(String, String, boolean)}
   */
  public static String getTabURL(ConnectionProvider conn, String tabId, String type) {
    return getTabURL(tabId, type, true);
  }

  /**
   * Utility method to generate mapping name. Copied from AD_Mapping_Format PL code. Keep in sync
   * with that one as it is used to generate 2.50 style menu
   * 
   */
  private static String mappingFormat(String map) {
    return map.replace(" ", "").replace("-", "").replace("/", "").replace("#", "").replace("&", "")
        .replace(",", "").replace("(", "").replace(")", "").replace("á", "a").replace("é", "e")
        .replace("í", "i").replace("ó", "o").replace("ú", "u").replace("Á", "A").replace("É", "")
        .replace("Í", "I").replace("Ó", "O").replace("Ú", "U");
  }

  /**
   * Determine if a String can be parsed into a BigDecimal.
   * 
   * @param str
   *          a String
   * @return true if the string can be parsed
   */
  public static boolean isBigDecimal(String str) {
    try {
      new BigDecimal(str.trim());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * When updating core it is necessary to update Openbravo properties maintaining already assigned
   * properties. Thus properties in original file are preserved but the new ones in
   * Openbravo.properties.template file are added with the default value.
   * 
   * @throws IOException
   * @throws FileNotFoundException
   * @return false in case no changes where needed, true in case the merge includes some changes and
   *         the original file is modified
   */
  public static boolean mergeOpenbravoProperties(String originalFile, String newFile)
      throws FileNotFoundException, IOException {
    Properties origOBProperties = new Properties();
    Properties newOBProperties = new Properties();
    boolean modified = false;

    // load both files
    origOBProperties.load(new FileInputStream(originalFile));
    newOBProperties.load(new FileInputStream(newFile));

    Enumeration<?> newProps = newOBProperties.propertyNames();
    while (newProps.hasMoreElements()) {
      String propName = (String) newProps.nextElement();
      String origValue = origOBProperties.getProperty(propName);

      // try to get original value for new property, if it does not exist add it to original
      // properties with its default value
      if (origValue == null) {
        String newValue = newOBProperties.getProperty(propName);
        origOBProperties.setProperty(propName, newValue);
        modified = true;
      }
    }

    // save original file only in case it has modifications
    if (modified) {
      origOBProperties
          .store(new FileOutputStream(originalFile), "Automatically updated properties");
    }
    return modified;
  }

  /**
   * Returns the name for a value in a list reference in the selected language.
   * 
   * @param ListName
   *          Name for the reference list to look in
   * @param value
   *          Value to look for
   * @param lang
   *          Language, if null the default language will be returned
   * @return Name for the value, in case the value is not found in the list the return is not the
   *         name but the passed value
   */
  public static String getListValueName(String ListName, String value, String lang) {
    // Try to obtain the translated value
    String hql = "  select rlt.name as name " + " from ADReference r, " + "      ADList rl,"
        + "      ADListTrl rlt" + " where rl.reference = r" + "  and rlt.listReference = rl"
        + "  and rlt.language.language = '" + lang + "'" + "  and r.name =  '" + ListName + "'"
        + "  and rl.searchKey = '" + value + "'";
    Query q = OBDal.getInstance().getSession().createQuery(hql);

    if (q.list().size() > 0) {
      return (String) q.list().get(0);
    }

    // No translated value obtained, get the standard one
    hql = "  select rl.name " + " from ADReference r, " + "      ADList rl"
        + " where rl.reference = r" + "  and r.name =  '" + ListName + "'"
        + "  and rl.searchKey = '" + value + "'";
    q = OBDal.getInstance().getSession().createQuery(hql);

    if (q.list().size() > 0) {
      return (String) q.list().get(0);
    } else {
      // Nothing found, return the value
      return value;
    }
  }

  /**
   * Constructs and returns a two dimensional array of the data passed. Array definition is
   * constructed according to Javascript syntax. Used to generate data storage of lists or trees
   * within some manual windows/reports.
   * 
   * @param strArrayName
   *          String with the name of the array to be defined.
   * @param data
   *          FieldProvider object with the data to be included in the array with the following
   *          three columns mandatory: padre | id | name.
   * @return String containing array definition according to Javascript syntax.
   */
  public static String arrayDobleEntrada(String strArrayName, FieldProvider[] data) {
    String strArray = "var " + strArrayName + " = ";
    if (data.length == 0) {
      strArray = strArray + "null";
      return strArray;
    }
    strArray = strArray + "new Array(";
    for (int i = 0; i < data.length; i++) {
      strArray = strArray + "\nnew Array(\"" + data[i].getField("padre") + "\", \""
          + data[i].getField("id") + "\", \"" + FormatUtilities.replaceJS(data[i].getField("name"))
          + "\")";
      if (i < data.length - 1)
        strArray = strArray + ", ";
    }
    strArray = strArray + ");";
    return strArray;
  }

  /**
   * Constructs and returns a two dimensional array of the data passed. Array definition is
   * constructed according to Javascript syntax. Used to generate data storage of lists or trees
   * within some manual windows/reports.
   * 
   * @param strArrayName
   *          String with the name of the array to be defined.
   * @param data
   *          FieldProvider object with the data to be included in the array with the following two
   *          columns mandatory: id | name.
   * @return String containing array definition according to Javascript syntax.
   */
  public static String arrayEntradaSimple(String strArrayName, FieldProvider[] data) {
    String strArray = "var " + strArrayName + " = ";
    if (data.length == 0) {
      strArray = strArray + "null";
      return strArray;
    }
    strArray = strArray + "new Array(";
    for (int i = 0; i < data.length; i++) {
      strArray = strArray + "\nnew Array(\"" + data[i].getField("id") + "\", \""
          + FormatUtilities.replaceJS(data[i].getField("name")) + "\")";
      if (i < data.length - 1)
        strArray = strArray + ", ";
    }
    strArray = strArray + ");";
    return strArray;
  }

  /**
   * Gets the reference list for a particular reference id
   * 
   * @param connectionProvider
   * @param language
   * @param referenceId
   * @return refValues string array containing reference values
   * @throws ServletException
   */
  public static String[] getReferenceValues(ConnectionProvider connectionProvider, String language,
      String referenceId) throws ServletException {
    String[] refValues = null;
    if (referenceId != null) {
      UtilityData[] datas = UtilityData.selectReference(connectionProvider, language, referenceId);
      if (datas != null) {
        int i = 0;
        refValues = new String[datas.length];
        for (UtilityData reference : datas) {
          refValues[i++] = reference.getField("value");
        }
      }
    }
    return refValues;
  }

  /**
   * Returns a DecimalFormat for the given formatting type contained in the Format.xml file
   */
  public static DecimalFormat getFormat(VariablesSecureApp vars, String typeName) {
    String format = vars.getSessionValue("#FormatOutput|" + typeName);
    String decimal = vars.getSessionValue("#DecimalSeparator|" + typeName);
    String group = vars.getSessionValue("#GroupSeparator|" + typeName);
    DecimalFormat numberFormatDecimal = null;
    if (format != null && !format.equals("") && decimal != null && !decimal.equals("")
        && group != null && !group.equals("")) {
      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      dfs.setDecimalSeparator(decimal.charAt(0));
      dfs.setGroupingSeparator(group.charAt(0));
      numberFormatDecimal = new DecimalFormat(format, dfs);
    }
    return numberFormatDecimal;
  }

  /**
   * Checks whether the current context is using new UI
   * 
   * @return true in case new UI is being used false if not
   */
  public static boolean isNewUI() {
    OBContext context = OBContext.getOBContext();
    return context != null && context.isNewUI();
  }

  private static byte[] getBlankImage() {

    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      new FileUtility(OBConfigFileProvider.getInstance().getServletContext().getRealPath("/"),
          getDefaultImageLogo("Empty"), false, true).dumpFile(bout);
      bout.close();
      return bout.toByteArray();
    } catch (IOException ex) {
      log4j.error("Could not load blank image.");
      return new byte[0];
    }
  }

  /**
   * Provides the image as a byte array. These images are stored in the table AD_IMAGES as a BLOB
   * field.
   * 
   * @param id
   *          The id of the image to display
   * @return The image requested
   * @see #getImage(String)
   */
  public static byte[] getImage(String id) {

    byte[] imageByte;
    try {
      Image img = getImageObject(id);
      if (img == null) {
        imageByte = getBlankImage();
      } else {
        OBContext.setAdminMode(true);
        try {
          imageByte = img.getBindaryData();
        } finally {
          OBContext.restorePreviousMode();
        }
      }
    } catch (Exception e) {
      log4j.error("Could not load image from database: " + id, e);
      imageByte = getBlankImage();
    }

    return imageByte;
  }

  /**
   * Provides the image as an image object. These images are stored in the table AD_IMAGES as a BLOB
   * field.
   * 
   * @param id
   *          The id of the image to display
   * @return The image requested
   * @see #getImage(String)
   */
  public static Image getImageObject(String id) {
    Image img = null;
    OBContext.setAdminMode();
    try {
      img = OBDal.getInstance().get(Image.class, id);
    } catch (Exception e) {
      log4j.error("Could not load image from database: " + id, e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return img;
  }

  /**
   * Provides the image as a BufferedImage object.
   * 
   * @param id
   *          The id of the image to display
   * @return The image requested
   * @throws IOException
   * @see #getImage(String)
   */
  public static BufferedImage showImage(String id) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(getImage(id)));
  }

  /**
   * Provides the image logo as a byte array for the indicated parameters.
   * 
   * @param logo
   *          The name of the logo to display This can be one of the following: yourcompanylogin,
   *          youritservicelogin, yourcompanymenu, yourcompanybig or yourcompanydoc
   * @param org
   *          The organization id used to get the logo In the case of requesting the yourcompanydoc
   *          logo you can indicate the organization used to request the logo.
   * @return The image requested
   */
  public static Image getImageLogoObject(String logo, String org) {
    Image img = null;
    OBContext.setAdminMode();
    try {

      if ("yourcompanylogin".equals(logo)) {
        img = OBDal.getInstance().get(SystemInformation.class, "0").getYourCompanyLoginImage();
      } else if ("youritservicelogin".equals(logo)) {
        img = OBDal.getInstance().get(SystemInformation.class, "0").getYourItServiceLoginImage();
      } else if ("yourcompanymenu".equals(logo)) {
        img = OBDal.getInstance()
            .get(ClientInformation.class, OBContext.getOBContext().getCurrentClient().getId())
            .getYourCompanyMenuImage();
        if (img == null) {
          img = OBDal.getInstance().get(SystemInformation.class, "0").getYourCompanyMenuImage();
        }
      } else if ("yourcompanybig".equals(logo)) {
        img = OBDal.getInstance()
            .get(ClientInformation.class, OBContext.getOBContext().getCurrentClient().getId())
            .getYourCompanyBigImage();
        if (img == null) {
          img = OBDal.getInstance().get(SystemInformation.class, "0").getYourCompanyBigImage();
        }
      } else if ("yourcompanydoc".equals(logo)) {
        if (org != null && !org.equals("")) {
          Organization organization = OBDal.getInstance().get(Organization.class, org);
          img = organization.getOrganizationInformationList().get(0).getYourCompanyDocumentImage();
        }
        if (img == null) {
          img = OBDal.getInstance().get(SystemInformation.class, "0").getYourCompanyDocumentImage();
        }
      } else if ("banner-production".equals(logo)) {
        img = OBDal.getInstance().get(SystemInformation.class, "0").getProductionBannerImage();
      } else if ("yourcompanylegal".equals(logo)) {
        if (org != null && !org.equals("")) {
          Organization organization = OBDal.getInstance().get(Organization.class, org);
          img = organization.getOrganizationInformationList().get(0).getYourCompanyDocumentImage();
        }
        if (img == null) {

          img = OBDal.getInstance()
              .get(ClientInformation.class, OBContext.getOBContext().getCurrentClient().getId())
              .getYourCompanyDocumentImage();

          if (img == null) {
            img = OBDal.getInstance().get(SystemInformation.class, "0")
                .getYourCompanyDocumentImage();
          }
        }
      } else {
        log4j.error("Logo key does not exist: " + logo);
      }
    } catch (Exception e) {
      log4j.error("Could not load logo from database: " + logo + ", " + org, e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return img;
  }

  /**
   * Provides the image logo as a byte array for the indicated parameters.
   * 
   * @param logo
   *          The name of the logo to display This can be one of the following: yourcompanylogin,
   *          youritservicelogin, yourcompanymenu, yourcompanybig or yourcompanydoc
   * @param org
   *          The organization id used to get the logo In the case of requesting the yourcompanydoc
   *          logo you can indicate the organization used to request the logo.
   * @return The image requested
   */
  public static byte[] getImageLogo(String logo, String org) {

    byte[] imageByte;

    try {
      Image img = getImageLogoObject(logo, org);
      if (img == null) {
        String path = getDefaultImageLogo(logo);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        new FileUtility(OBConfigFileProvider.getInstance().getServletContext().getRealPath("/"),
            path, false, true).dumpFile(bout);
        bout.close();
        imageByte = bout.toByteArray();
      } else {
        OBContext.setAdminMode(true);
        try {
          imageByte = img.getBindaryData();
        } finally {
          OBContext.restorePreviousMode();
        }
      }

    } catch (Exception e) {
      log4j.error("Could not load logo from database: " + logo + ", " + org, e);
      imageByte = getBlankImage();
    }
    return imageByte;
  }

  /**
   * Provides the image logo as a byte array for the indicated parameters.
   * 
   * @param logo
   *          The name of the logo to display This can be one of the following: yourcompanylogin,
   *          youritservicelogin, yourcompanymenu, yourcompanybig or yourcompanydoc
   * @param org
   *          The organization id used to get the logo In the case of requesting the yourcompanydoc
   *          logo you can indicate the organization used to request the logo.
   * @return The image requested
   */
  private static String getDefaultImageLogo(String logo) {

    String defaultImagePath = null;

    if (logo == null) {
      defaultImagePath = "web/images/blank.gif";
    } else if ("yourcompanylogin".equals(logo)) {
      defaultImagePath = "web/images/CompanyLogo_big.png";
    } else if ("youritservicelogin".equals(logo)) {
      defaultImagePath = "web/images/SupportLogo_big.png";
    } else if ("yourcompanymenu".equals(logo)) {
      defaultImagePath = "web/images/CompanyLogo_small.png";
    } else if ("yourcompanybig".equals(logo)) {
      defaultImagePath = "web/skins/ltr/Default/Login/initialOpenbravoLogo.png";
    } else if ("yourcompanydoc".equals(logo)) {
      defaultImagePath = "web/images/CompanyLogo_big.png";
    } else if ("banner-production".equals(logo)) {
      defaultImagePath = "web/images/blank.gif";
    } else if ("yourcompanylegal".equals(logo)) {
      defaultImagePath = "web/images/CompanyLogo_big.png";
    } else {
      defaultImagePath = "web/images/blank.gif";
    }

    return defaultImagePath;

  }

  /**
   * This method calculates the size of an image
   * 
   * @param bytea
   *          The contents of the image
   * @return An Long array with two elements (width, height)
   * @throws IOException
   */
  public static Long[] computeImageSize(byte[] bytea) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(bytea);
    BufferedImage rImage = ImageIO.read(bis);
    Long[] size = new Long[2];
    size[0] = new Long(rImage.getWidth());
    size[1] = new Long(rImage.getHeight());
    return size;
  }

  /**
   * Resize an image giving the image input as byte[]
   * 
   * @param bytea
   *          The contents of the image as a byte array
   * @param maxW
   *          Maximum width that the image will be resized.
   * @param maxH
   *          Maximum height that the image will be resized.
   * @param maintainAspectRatio
   *          If true, the image will be resized exactly to the maximum parameters. If false, the
   *          imagen will be resized closest to the maximum parameters keeping aspect ratio
   * @param canMakeLargerImage
   *          If true and the original image is smaller than maximum parameters, the resized image
   *          could be larger than the original one. If false, not.
   * @return The resized image
   */
  public static byte[] resizeImageByte(byte[] bytea, int maxW, int maxH,
      boolean maintainAspectRatio, boolean canMakeLargerImage) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(bytea);
    BufferedImage rImage = ImageIO.read(bis);
    int newW = maxW;
    int newH = maxH;
    int oldW = rImage.getWidth();
    int oldH = rImage.getHeight();
    if (newW == 0 && newH == 0) {
      return bytea;
    } else if (newW == 0) {
      if (maintainAspectRatio) {
        newW = 99999;
      } else {
        newW = oldW;
      }
    } else if (newH == 0) {
      if (maintainAspectRatio) {
        newH = 99999;
      } else {
        newH = oldH;
      }
    }
    if (oldW == newW && oldH == newH) {
      return bytea;
    }
    if (!canMakeLargerImage && newW > oldW && newH > oldH) {
      return bytea;
    }
    if (maintainAspectRatio) {
      float oldRatio = (float) oldW / (float) oldH;
      float newRatio = (float) newW / (float) newH;
      if (oldRatio < newRatio) {
        newW = (int) ((float) newH * oldRatio);
      } else if (oldRatio > newRatio) {
        newH = (int) ((float) newW / oldRatio);
      }
    }
    BufferedImage dimg = new BufferedImage(newW, newH, rImage.getType());
    Graphics2D g = dimg.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(rImage, 0, 0, newW, newH, 0, 0, oldW, oldH, null);
    g.dispose();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String mimeType = MimeTypeUtil.getInstance().getMimeTypeName(bytea);
    if (mimeType.contains("jpeg")) {
      mimeType = "jpeg";
    } else if (mimeType.contains("png")) {
      mimeType = "png";
    } else if (mimeType.contains("gif")) {
      mimeType = "gif";
    } else if (mimeType.contains("bmp")) {
      mimeType = "bmp";
    } else {
      return bytea;
    }
    ImageIO.write(dimg, mimeType, baos);
    byte[] bytesOut = baos.toByteArray();
    return bytesOut;
  }

  /**
   * Provides the image logo as a BufferedImage object.
   * 
   * @param logo
   *          The name of the logo to display
   * @return The image requested
   * @throws IOException
   * @see #getImageLogo(String,String)
   */
  public static BufferedImage showImageLogo(String logo) throws IOException {
    return showImageLogo(logo, null);
  }

  /**
   * Provides the image logo as a BufferedImage object.
   * 
   * @param logo
   *          The name of the logo to display
   * @param org
   *          The organization id used to get the logo
   * @return The image requested
   * @throws IOException
   * @see #getImageLogo(String,String)
   */
  public static BufferedImage showImageLogo(String logo, String org) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(getImageLogo(logo, org)));
  }

  /**
   * Returns whether the <code>Process</code> should be opened in modal or in poup mode.
   * 
   * @param process
   *          Process to check
   * @return <code>true</code> in case it should be opened in modal, <code>false</code> if not.
   */
  public static boolean isModalProcess(org.openbravo.model.ad.ui.Process process) {
    boolean modal = false;

    // Show in popup by default unless preference to prevent it is set
    String processModal = null;
    try {
      processModal = Preferences.getPreferenceValue("ModalProcess" + process.getId(), false,
          OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
              .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
              .getOBContext().getRole(), null);
    } catch (PropertyException e) {
      // Not found, keep processModal = null
    }

    if (processModal != null) {
      modal = "Y".equals(processModal);
    } else {
      try {
        modal = "Y".equals(Preferences.getPreferenceValue("ModalModule"
            + process.getModule().getJavaPackage(), false, OBContext.getOBContext()
            .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
            .getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
      } catch (PropertyException e1) {
        try {
          modal = "Y".equals(Preferences.getPreferenceValue("ModalModule"
              + process.getModule().getId(), false, OBContext.getOBContext().getCurrentClient(),
              OBContext.getOBContext().getCurrentOrganization(),
              OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
        } catch (PropertyException e) {
          modal = false;
        }
      }
    }
    return modal;
  }

  /**
   * Same as {@link Utility#isModalProcess(Process)} passing as parameter the process id.
   * 
   * @param processId
   *          to check
   * @return <code>true</code> in case it should be opened in modal, <code>false</code> if not.
   * @see Utility#isModalProcess(Process)
   */
  public static boolean isModalProcess(String processId) {
    OBContext.setAdminMode();
    try {
      Process process = OBDal.getInstance().get(org.openbravo.model.ad.ui.Process.class, processId);
      if (process == null) {
        return false;
      }
      return isModalProcess(process);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a field name in the specified language. If there is not translation for that language,
   * it returns the base name of the field.
   * 
   * @param fieldId
   *          ID of the field to look for.
   * @param language
   *          Langage to get the name in.
   * @return field name in the correct language.
   */
  public static String getFieldName(String fieldId, String language) {
    StringBuilder hql = new StringBuilder();
    hql.append("select (select t.name\n");
    hql.append("          from ADFieldTrl t\n");
    hql.append("         where t.field = f\n");
    hql.append("           and t.language.language=:lang),\n");
    hql.append("       f.name\n");
    hql.append("  from ADField f\n");
    hql.append(" where f.id =:fieldId\n");
    Query qName = OBDal.getInstance().getSession().createQuery(hql.toString());
    qName.setParameter("lang", language);
    qName.setParameter("fieldId", fieldId);

    if (qName.list().isEmpty()) {
      log4j.warn("Not found name for fieldId " + fieldId);
      return "";
    }

    Object[] names = (Object[]) qName.list().get(0);
    return names[0] != null ? (String) names[0] : (String) names[1];
  }
}
