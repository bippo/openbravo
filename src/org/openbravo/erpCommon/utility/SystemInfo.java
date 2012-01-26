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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.ad.access.SessionUsageAudit;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DalConnectionProvider;

public class SystemInfo {

  private static final Logger log4j = Logger.getLogger(SystemInfo.class);
  private static Map<Item, String> systemInfo;
  private static Date firstLogin;
  private static Date lastLogin;
  private static Long numberOfLogins;
  private static SimpleDateFormat sd;
  private static int numberOfLoginsThisMonth = 0;
  private static int numberOfRejectedLoginsDueConcUsersThisMonth = 0;
  private static BigDecimal avgUsers = BigDecimal.ZERO;
  private static BigDecimal usagePercentageTime = BigDecimal.ZERO;
  private static int maxUsers = 0;
  private static String systemIdentifier;
  private static String macAddress;
  private static String databaseIdentifier;

  private static long maxDayWsLogins;
  private static BigDecimal avgWsLogins = BigDecimal.ZERO;

  private static long maxDayWsCLogins;
  private static BigDecimal avgWsCLogins = BigDecimal.ZERO;
  private static long maxDayRejectedWsLogins;
  private static BigDecimal avgRejectedWsLogins;

  static {
    systemInfo = new HashMap<Item, String>();
    sd = new SimpleDateFormat("dd-MM-yyyy");
  }

  /**
   * Loads system information but ID
   */
  public static void load(ConnectionProvider conn) throws ServletException {
    try {
      OBContext.setAdminMode();
      loadSessionInfo();
      for (Item i : Item.values()) {
        if (!i.isIdInfo()) {
          load(i, conn);
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Loads ID information
   */
  public static void loadId(ConnectionProvider conn) throws ServletException {
    for (Item i : Item.values()) {
      if (i.isIdInfo()) {
        load(i, conn);
      }
    }
  }

  private static void load(Item i, ConnectionProvider conn) throws ServletException {

    OBContext.setAdminMode();
    try {

      SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
      switch (i) {
      case SYSTEM_IDENTIFIER:
        systemInfo.put(i, getSystemIdentifier(conn));
        break;
      case MAC_IDENTIFIER:
        systemInfo.put(i, calculateMacIdentifier());
        break;
      case DB_IDENTIFIER:
        systemInfo.put(i, getDBIdentifier(conn));
        break;
      case DATABASE:
        systemInfo.put(i, conn.getRDBMS());
        break;
      case DATABASE_VERSION:
        systemInfo.put(i, getDatabaseVersion(conn));
        break;
      case WEBSERVER:
        systemInfo.put(i, getWebserver()[0]);
        break;
      case WEBSERVER_VERSION:
        systemInfo.put(i, getWebserver()[1]);
        break;
      case SERVLET_CONTAINER:
        systemInfo.put(i, SystemInfoData.selectServletContainer(conn));
        break;
      case SERVLET_CONTAINER_VERSION:
        systemInfo.put(i, SystemInfoData.selectServletContainerVersion(conn));
        break;
      case ANT_VERSION:
        systemInfo.put(i, getVersion(SystemInfoData.selectAntVersion(conn)));
        break;
      case OB_VERSION:
        OBVersion version = OBVersion.getInstance();
        systemInfo.put(i, version.getVersionNumber() + version.getMP());
        break;
      case OB_INSTALL_MODE:
        systemInfo.put(i, SystemInfoData.selectObInstallMode(conn));
        break;
      case CODE_REVISION:
        systemInfo.put(i, SystemInfoData.selectCodeRevision(conn));
        break;
      case NUM_REGISTERED_USERS:
        systemInfo.put(i, SystemInfoData.selectNumRegisteredUsers(conn));
        break;
      case ISHEARTBEATACTIVE:
        systemInfo.put(i, SystemInfoData.selectIsheartbeatactive(conn));
        break;
      case ISPROXYREQUIRED:
        systemInfo.put(i, SystemInfoData.selectIsproxyrequired(conn));
        break;
      case PROXY_SERVER:
        systemInfo.put(i, SystemInfoData.selectProxyServer(conn));
        break;
      case PROXY_PORT:
        systemInfo.put(i, SystemInfoData.selectProxyPort(conn));
        break;
      case OPERATING_SYSTEM:
        String os = System.getProperty("os.name");
        if (os.length() > 60) {
          os = os.substring(0, 57) + "...";
        }
        systemInfo.put(i, os);
        break;
      case OPERATING_SYSTEM_VERSION:
        systemInfo.put(i, System.getProperty("os.version"));
        break;
      case JAVA_VERSION:
        systemInfo.put(i, System.getProperty("java.version"));
        break;
      case MODULES:
        systemInfo.put(i, getModules());
        break;
      case OBPS_INSTANCE:
        systemInfo.put(i, getOBPSInstance());
        break;
      case INSTANCE_NUMBER:
        systemInfo.put(i, getOBPSIntanceNumber());
        break;
      case FIRST_LOGIN:
        systemInfo.put(i, sd.format(firstLogin));
        break;
      case LAST_LOGIN:
        systemInfo.put(i, sd.format(lastLogin));
        break;
      case TOTAL_LOGINS:
        systemInfo.put(i, numberOfLogins.toString());
        break;
      case AVG_CONCURRENT_USERS:
        systemInfo.put(i, avgUsers.toString());
        break;
      case MAX_CONCURRENT_USERS:
        systemInfo.put(i, Integer.toString(maxUsers));
        break;
      case PERC_TIME_USAGE:
        systemInfo.put(i, usagePercentageTime.toString());
        break;
      case TOTAL_LOGINS_LAST_MOTH:
        systemInfo.put(i, Integer.toString(numberOfLoginsThisMonth));
        break;
      case WS_CALLS_MAX:
        systemInfo.put(i, Long.toString(maxDayWsLogins));
        break;
      case WS_CALLS_AVG:
        systemInfo.put(i, avgWsLogins.toString());
        break;
      case WSR_CALLS_MAX:
        systemInfo.put(i, Long.toString(maxDayRejectedWsLogins));
        break;
      case WSR_CALLS_AVG:
        systemInfo.put(i, avgRejectedWsLogins.toString());
        break;
      case WSC_CALLS_MAX:
        systemInfo.put(i, Long.toString(maxDayWsCLogins));
        break;
      case WSC_CALLS_AVG:
        systemInfo.put(i, avgWsCLogins.toString());
        break;
      case NUMBER_OF_CLIENTS:
        systemInfo.put(i, getNumberOfClients());
        break;
      case NUMBER_OF_ORGS:
        systemInfo.put(i, getNumberOfOrgs());
        break;
      case USAGE_AUDIT:
        systemInfo.put(i, isUsageAuditEnabled() ? "true" : "false");
        break;
      case INSTANCE_PURPOSE:
        String instancePurpose = sysInfo.getInstancePurpose();
        systemInfo.put(i, instancePurpose == null ? "U" : instancePurpose);
        break;
      case REJECTED_LOGINS_DUE_CONC_USERS:
        systemInfo.put(i, Integer.toString(numberOfRejectedLoginsDueConcUsersThisMonth));
        break;
      case CUSTOM_QUERY_ENABLED:
        systemInfo.put(i, sysInfo.isEnableCustomQueries() ? "Y" : "N");
        break;
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public final static synchronized String getSystemIdentifier() throws ServletException {
    if (systemIdentifier == null)
      systemIdentifier = getSystemIdentifier(new DalConnectionProvider());
    return systemIdentifier;
  }

  public final static synchronized String getDBIdentifier() {
    if (databaseIdentifier == null)
      databaseIdentifier = getDBIdentifier(new DalConnectionProvider());
    return databaseIdentifier;
  }

  public final static synchronized String getMacAddress() {
    if (macAddress == null)
      macAddress = calculateMacIdentifier();
    return macAddress;
  }

  private final static String getSystemIdentifier(ConnectionProvider conn) throws ServletException {
    validateConnection(conn);
    String strSystemIdentifier = SystemInfoData.selectSystemIdentifier(conn);
    if (strSystemIdentifier == null || strSystemIdentifier.equals("")) {
      strSystemIdentifier = UUID.randomUUID().toString();
      SystemInfoData.updateSystemIdentifier(conn, strSystemIdentifier);
    }
    return strSystemIdentifier;
  }

  /**
   * Obtains mac address a CRC of the byte[] array for the obtained mac address.
   * 
   * In case multiple interfaces are present, it is taken the first one with mac address of the list
   * sorted in this way: loopbacks are sorted at the end, the rest of interfaces are sorted by name.
   */
  private final static String calculateMacIdentifier() {
    List<NetworkInterface> interfaces = new ArrayList<NetworkInterface>();
    try {
      interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

      Collections.sort(interfaces, new Comparator<NetworkInterface>() {
        @Override
        public int compare(NetworkInterface o1, NetworkInterface o2) {
          try {
            if (o1.isLoopback() && !o2.isLoopback()) {
              return 1;
            }
            if (!o1.isLoopback() && o2.isLoopback()) {
              return -1;
            }
          } catch (SocketException e) {
            log4j.error("Error sorting network interfaces", e);
            return 0;
          }
          return o1.getName().compareTo(o2.getName());
        }
      });

      for (NetworkInterface iface : interfaces) {
        if (iface.getHardwareAddress() != null) {
          // get the first not null hw address and CRC it
          CRC32 crc = new CRC32();
          crc.update(iface.getHardwareAddress());
          return Long.toHexString(crc.getValue());
        }
      }

      if (interfaces.isEmpty()) {
        log4j.warn("Not found mac adress");
      }
      return "";
    } catch (SocketException e) {
      log4j.error("Error getting mac address", e);
      return "";
    }
  }

  /**
   * Obtains a unique identifier for database
   */
  private final static String getDBIdentifier(ConnectionProvider conn) {
    Properties obProps = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    if ("ORACLE".equals(conn.getRDBMS())) {
      Connection con = null;
      Statement st = null;
      try {
        // Obtain a direct jdbc connection instead of using DAL nor ConnectionProvider. This query
        // is needed to be executed with DBA privileges, which standard user might not have.

        con = DriverManager.getConnection(obProps.getProperty("bbdd.url"),
            obProps.getProperty("bbdd.systemUser"), obProps.getProperty("bbdd.systemPassword"));
        st = con.createStatement();
        st.execute("select dbid from v$database");
        st.getResultSet().next();

        String id = st.getResultSet().getString(1);
        // Add schema name to id, it's possible to have multiple schemas in the same db
        id += obProps.getProperty("bbdd.user");

        CRC32 crc = new CRC32();
        crc.update(id.getBytes());
        return Long.toHexString(crc.getValue());
      } catch (SQLException e) {
        log4j.debug("Error obtaining Oracle's DB identifier", e);
        return "0";
      } finally {
        try {
          if (st != null) {
            if (st.getResultSet() != null) {
              st.getResultSet().close();
            }
            st.close();
          }
          if (con != null) {
            con.close();
          }
        } catch (SQLException e) {
          // ignore
        }
      }
    } else { // PG
      Vector<String> param = new Vector<String>();
      param.add(obProps.getProperty("bbdd.sid"));
      try {
        // Executing query in this way instead of with sqlc not to have to create pg_stat_database
        // view in Oracle
        ExecuteQuery q = new ExecuteQuery(conn,
            "select datid from pg_stat_database where datname=?", param);
        FieldProvider[] results = q.select();
        if (results.length != 0) {
          String id = results[0].getField("datid");
          CRC32 crc = new CRC32();
          crc.update(id.getBytes());
          return Long.toHexString(crc.getValue());
        }
      } catch (Exception e) {
        log4j.error("Error getting PG DB ID", e);
      }
      log4j.warn("Not found DB id");
      return "";
    }
  }

  private final static String getDatabaseVersion(ConnectionProvider conn) throws ServletException {
    validateConnection(conn);
    if (systemInfo.get(Item.DATABASE) == null) {
      load(Item.DATABASE, conn);
    }
    String database = systemInfo.get(Item.DATABASE);
    String databaseVersion = null;
    if ("ORACLE".equals(database)) {
      databaseVersion = getVersion(SystemInfoData.selectOracleVersion(conn));
    } else {
      databaseVersion = SystemInfoData.selectPostregresVersion(conn);
    }
    return databaseVersion;
  }

  /**
   * Runs a native command to try and locate the user's web server version. Tests all combinations
   * of paths + commands.
   * 
   * Currently only checks for Apache.
   */
  private final static String[] getWebserver() {
    List<String> commands = new ArrayList<String>();
    String[] paths = { "/usr/local/sbin", "/usr/local/bin", "/usr/sbin", "/usr/bin", "/sbin",
        "/bin" };
    String[] execs = { "httpd", "apache", "apache2" };
    for (String path : paths) {
      for (String exec : execs) {
        commands.add(path + "/" + exec);
      }
    }
    commands.addAll(Arrays.asList(execs));
    for (String command : commands) {
      try {
        Process process = new ProcessBuilder(command, "-v").start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
          sb.append(line);
        }
        Pattern pattern = Pattern.compile("Apache/((\\d+\\.)+)\\d+");
        Matcher matcher = pattern.matcher(sb.toString());
        if (matcher.find()) {
          String s = matcher.group();
          return s.split("/");
        }
      } catch (IOException e) {
        // OK. We'll probably get a lot of these.
      }
    }
    return new String[] { "", "" };
  }

  /**
   * Obtain the total number of clients in the system
   */
  private final static String getNumberOfClients() {
    try {
      OBContext.setAdminMode();
      OBCriteria<Client> qClients = OBDal.getInstance().createCriteria(Client.class);
      qClients.setFilterOnReadableOrganization(false);
      qClients.setFilterOnReadableClients(false);
      return Integer.toString(qClients.count());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Obtain the total number of organizations in the system
   */
  private final static String getNumberOfOrgs() {
    try {
      OBContext.setAdminMode();
      OBCriteria<Organization> qOrgs = OBDal.getInstance().createCriteria(Organization.class);
      qOrgs.setFilterOnReadableOrganization(false);
      qOrgs.setFilterOnReadableClients(false);
      return Integer.toString(qOrgs.count());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Obtain all the modules installed in the instance.
   */
  private final static String getModules() {
    try {
      OBContext.setAdminMode();
      OBCriteria<Module> qMods = OBDal.getInstance().createCriteria(Module.class);
      qMods.addOrder(Order.asc(Module.PROPERTY_JAVAPACKAGE));
      JSONArray mods = new JSONArray();
      Date startOfPeriod = getStartOfPeriod().getTime();
      boolean usageAuditEnabled = isUsageAuditEnabled();
      for (Module mod : qMods.list()) {
        ArrayList<String> modInfo = new ArrayList<String>();
        modInfo.add(mod.getId());
        modInfo.add(mod.getVersion());
        modInfo.add(mod.isEnabled() ? "Y" : "N");
        modInfo.add(mod.getName());

        if (usageAuditEnabled) {
          OBCriteria<SessionUsageAudit> qUsage = OBDal.getInstance().createCriteria(
              SessionUsageAudit.class);
          qUsage.setFilterOnReadableClients(false);
          qUsage.setFilterOnReadableOrganization(false);
          qUsage.add(Restrictions.eq(SessionUsageAudit.PROPERTY_MODULE, mod));
          qUsage.add(Restrictions.ge(SessionUsageAudit.PROPERTY_CREATIONDATE, startOfPeriod));
          modInfo.add(Integer.toString(qUsage.count()));
        }
        mods.put(modInfo);
      }
      return mods.toString();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static boolean isUsageAuditEnabled() {
    SystemInformation sys = OBDal.getInstance().get(SystemInformation.class, "0");
    return sys.isUsageauditenabled();
  }

  private static boolean validateConnection(ConnectionProvider conn) throws ServletException {
    if (conn == null) {
      throw new ServletException("Invalid database connection provided.");
    }
    return true;
  }

  /**
   * @return the all systemInfo properties
   */
  public static Properties getSystemInfo() {
    Properties props = new Properties();
    if (systemInfo == null) {
      return props;
    }
    for (Map.Entry<Item, String> entry : systemInfo.entrySet()) {
      String key = entry.getKey().getLabel();
      String value = entry.getValue();
      props.setProperty(key, value);
    }
    return props;
  }

  /**
   * Returns the string representation of a numerical version from a longer string. For example,
   * given the string: 'Apache Ant version 1.7.0 compiled on August 29 2007' getVersion() will
   * return '1.7.0'
   * 
   * @param str
   * @return the string representation of a numerical version from a longer string.
   */
  private static String getVersion(String str) {
    String version = "";
    if (str == null)
      return "";
    Pattern pattern = Pattern.compile("((\\d+\\.)+)\\d+");
    Matcher matcher = pattern.matcher(str);
    if (matcher.find()) {
      version = matcher.group();
    }
    return version;
  }

  /**
   * In case it is an OBPS instance, it returns the CRC of the activation key
   */
  public static String getOBPSInstance() {
    if (ActivationKey.getInstance().isOPSInstance()) {
      return ActivationKey.getInstance().getOpsLogId();
    } else {
      return "";
    }
  }

  /**
   * In case it is an OBPS instance, it returns the number of instance
   */
  public static String getOBPSIntanceNumber() {
    if (ActivationKey.getInstance().isOPSInstance()) {
      return ActivationKey.getInstance().getProperty("instanceno");
    } else {
      return "";
    }
  }

  /**
   * Reads all information about session:
   * <ul>
   * <li>First and last login in the instance
   * <li>Number o total of logins in the instance
   * <li>Maximum concurrent users during last month
   * <li>Average concurrent users during last month
   * <li>Percentage of usage time during last month
   * <ul>
   */
  private static void loadSessionInfo() {
    // Obtain login counts
    StringBuilder hql = new StringBuilder();
    hql.append("select min(s.creationDate) as firstLogin, ");
    hql.append("       max(s.creationDate) as lastLogin, ");
    hql.append("       count(*) as totalLogins");
    hql.append("  from ADSession s");
    Query q = OBDal.getInstance().getSession().createQuery(hql.toString());
    if (q.list().size() != 0) {
      Object[] logInfo = (Object[]) q.list().get(0);
      firstLogin = (Date) logInfo[0];
      lastLogin = (Date) logInfo[1];
      numberOfLogins = (Long) logInfo[2];
    }

    // Calculate statistics

    // Obtain all sessions that have been alive during last 30 days
    try {
      long computationTime = System.currentTimeMillis();
      Calendar now = Calendar.getInstance();
      Calendar startOfPeriod = getStartOfPeriod();

      OBCriteria<Session> qSession = OBDal.getInstance().createCriteria(Session.class);
      qSession.add(Restrictions.isNotNull(Session.PROPERTY_LASTPING));
      qSession.add(Restrictions.ge(Session.PROPERTY_LASTPING, startOfPeriod.getTime()));
      qSession.addOrder(Order.asc(Session.PROPERTY_CREATIONDATE));

      // Prepare a list of events based on logins and logouts.
      List<Event> events = new ArrayList<Event>();
      List<Session> sessions = qSession.list();

      numberOfLoginsThisMonth = sessions.size();
      for (Session session : sessions) {
        Event newSession = new Event();
        newSession.eventDate = session.getCreationDate();
        newSession.sessionCount = 1;

        Event closeSession = new Event();
        closeSession.eventDate = session.getLastPing();
        closeSession.sessionCount = -1;

        events.add(newSession);
        events.add(closeSession);
      }
      Collections.sort(events);

      // At this point we have all events of last month, let's compute them to obtain summarized
      // information. For each login sum 1 to number of concurrent users at this time, for each
      // logout subtract 1.
      maxUsers = 0;
      int concurrentUsers = 0;
      BigDecimal totalUsageTime = BigDecimal.ZERO;
      BigDecimal usersPeriod = BigDecimal.ZERO;
      for (int i = 0; i < events.size() - 1; i++) {
        Event event = events.get(i);

        concurrentUsers += event.sessionCount;

        if (log4j.isDebugEnabled()) {
          log4j.debug("Period " + event.eventDate + " - " + events.get(i + 1).eventDate + " u:"
              + concurrentUsers + " t:"
              + ((events.get(i + 1).eventDate.getTime() - event.eventDate.getTime()) / 60000));
        }

        if (concurrentUsers > 0) {
          if (concurrentUsers > maxUsers) {
            maxUsers = concurrentUsers;
          }
          // If there is at least one user, the system is in use. Sum it up.
          BigDecimal periodTime = new BigDecimal(events.get(i + 1).eventDate.getTime())
              .subtract(new BigDecimal(event.eventDate.getTime()));
          totalUsageTime = totalUsageTime.add(periodTime);
          usersPeriod = usersPeriod.add(periodTime.multiply(new BigDecimal(concurrentUsers)));
        }
      }
      calculateNumberOfRejectedLoginsDueConcurrentUsersLastMonth(startOfPeriod);

      BigDecimal totalTime = new BigDecimal(now.getTimeInMillis() - startOfPeriod.getTimeInMillis());
      if (totalUsageTime.compareTo(BigDecimal.ZERO) != 0) {
        avgUsers = usersPeriod.divide(totalUsageTime, 3, RoundingMode.HALF_DOWN);
        usagePercentageTime = totalUsageTime.divide(totalTime, 5, RoundingMode.HALF_DOWN).multiply(
            new BigDecimal(100));
      }
      log4j.debug("max:" + maxUsers + " total:" + totalUsageTime + " "
          + usagePercentageTime.toString() + "% avg usr:" + avgUsers.toString());

      // WS calls
      maxDayWsLogins = 0L;
      long totalWsLogins = 0L;
      for (Long dayWsLogins : getWsLogins("WS", startOfPeriod.getTime())) {
        totalWsLogins += dayWsLogins;
        if (dayWsLogins > maxDayWsLogins) {
          maxDayWsLogins = dayWsLogins;
        }
      }
      avgWsLogins = BigDecimal.valueOf(totalWsLogins).divide(BigDecimal.valueOf(30), 3,
          RoundingMode.HALF_DOWN);
      log4j.debug("WS Calls: total:" + totalWsLogins + " - max:" + maxDayWsLogins + " - avg:"
          + avgWsLogins.toString());

      // Rejected WS calls
      maxDayRejectedWsLogins = 0L;
      long totalRejectedWsLogins = 0L;
      for (Long dayRWsLogins : getWsLogins("WSR", startOfPeriod.getTime())) {
        totalRejectedWsLogins += dayRWsLogins;
        if (dayRWsLogins > maxDayRejectedWsLogins) {
          maxDayRejectedWsLogins = dayRWsLogins;
        }
      }
      avgRejectedWsLogins = BigDecimal.valueOf(totalRejectedWsLogins).divide(
          BigDecimal.valueOf(30), 3, RoundingMode.HALF_DOWN);
      log4j.debug("WS Rejected Calls: total:" + totalRejectedWsLogins + " - max:"
          + maxDayRejectedWsLogins + " - avg:" + avgRejectedWsLogins.toString());

      // Connector calls
      maxDayWsCLogins = 0L;
      long totalWsCLogins = 0L;
      for (Long dayWsCLogins : getWsLogins("WSC", startOfPeriod.getTime())) {
        totalWsCLogins += dayWsCLogins;
        if (dayWsCLogins > maxDayWsCLogins) {
          maxDayWsCLogins = dayWsCLogins;
        }
      }
      avgWsCLogins = BigDecimal.valueOf(totalWsCLogins).divide(BigDecimal.valueOf(30), 3,
          RoundingMode.HALF_DOWN);
      log4j.debug("WSC Calls: total:" + totalWsCLogins + " - max:" + maxDayWsCLogins + " - avg:"
          + avgWsCLogins.toString());

      log4j
          .debug("Total time computing sessions:" + (System.currentTimeMillis() - computationTime));
    } catch (Exception e) {
      log4j.error("Error calculating login information", e);
    }

  }

  @SuppressWarnings("unchecked")
  private static List<Long> getWsLogins(String type, Date fromDate) {
    StringBuilder hql = new StringBuilder();
    hql.append("select count(*)\n");
    hql.append("  from ADSession\n");
    hql.append(" where loginStatus = :type\n");
    hql.append("   and creationDate > :firstDay\n");
    hql.append(" group by day(creationDate), month(creationDate), year(creationDate)\n");
    Query qWs = OBDal.getInstance().getSession().createQuery(hql.toString());
    qWs.setParameter("firstDay", fromDate);
    qWs.setParameter("type", type);
    return qWs.list();
  }

  private static void calculateNumberOfRejectedLoginsDueConcurrentUsersLastMonth(
      Calendar startOfPeriod) {
    OBCriteria<Session> qSession = OBDal.getInstance().createCriteria(Session.class);
    qSession.add(Restrictions.ge(Session.PROPERTY_CREATIONDATE, startOfPeriod.getTime()));
    qSession.add(Restrictions.eq(Session.PROPERTY_LOGINSTATUS, "CUR"));

    numberOfRejectedLoginsDueConcUsersThisMonth = qSession.count();
  }

  /**
   * Returns the date to start the computation data period which is 30 days before now.
   * 
   * @return Starting date
   */
  private static Calendar getStartOfPeriod() {
    Calendar startOfPeriod = Calendar.getInstance();
    startOfPeriod.add(Calendar.DAY_OF_MONTH, -30);
    return startOfPeriod;
  }

  /**
   * Auxiliary class to keep track of session events. It contains the time when the event occurred
   * and which kind of event is (in sessionCount field) +1 in case it is login, -1 for logout, so
   * then it is possible to compute number of users taking into account all the events.
   * 
   */
  private static class Event implements Comparable<Event> {
    Date eventDate;
    int sessionCount;

    /**
     * Sort by event date
     */
    @Override
    public int compareTo(Event o) {
      return this.eventDate.compareTo(o.eventDate);
    }
  }

  /**
   * @param item
   * @return the systemInfo of the passed item
   */
  public static String get(Item item) {
    return systemInfo.get(item);
  }

  public enum Item {
    SYSTEM_IDENTIFIER("systemIdentifier", true), MAC_IDENTIFIER("macId", true), DB_IDENTIFIER(
        "dbIdentifier", true), OPERATING_SYSTEM("os", false), OPERATING_SYSTEM_VERSION("osVersion",
        false), DATABASE("db", false), DATABASE_VERSION("dbVersion", false), WEBSERVER("webserver",
        false), WEBSERVER_VERSION("webserverVersion", false), SERVLET_CONTAINER("servletContainer",
        false), SERVLET_CONTAINER_VERSION("servletContainerVersion", false), ANT_VERSION(
        "antVersion", false), OB_VERSION("obVersion", false), OB_INSTALL_MODE("obInstallMode",
        false), CODE_REVISION("codeRevision", false), NUM_REGISTERED_USERS("numRegisteredUsers",
        false), ISHEARTBEATACTIVE("isheartbeatactive", true), ISPROXYREQUIRED("isproxyrequired",
        false), PROXY_SERVER("proxyServer", false), PROXY_PORT("proxyPort", false), JAVA_VERSION(
        "javaVersion", false), MODULES("modules", false), OBPS_INSTANCE("obpsId", false), FIRST_LOGIN(
        "firstLogin", false), LAST_LOGIN("lastLogin", false), TOTAL_LOGINS("totalLogins", false), TOTAL_LOGINS_LAST_MOTH(
        "loginsMoth", false), MAX_CONCURRENT_USERS("maxUsers", false), AVG_CONCURRENT_USERS(
        "avgUsers", false), PERC_TIME_USAGE("timeUsage", false), NUMBER_OF_CLIENTS("clientNum",
        false), NUMBER_OF_ORGS("orgNum", false), USAGE_AUDIT("usageAudit", false), INSTANCE_PURPOSE(
        "instancePurpose", false), REJECTED_LOGINS_DUE_CONC_USERS("rejectedLoginsDueConcUsers",
        false), INSTANCE_NUMBER("instanceNo", false), CUSTOM_QUERY_ENABLED("enabledCustomQuery",
        false), WS_CALLS_MAX("wsCallsMax", false), WS_CALLS_AVG("wsCallsAvg", false), WSC_CALLS_MAX(
        "wscCallsMax", false), WSC_CALLS_AVG("wscCallsAvg", false), WSR_CALLS_MAX(
        "wsRejectedCallsMax", false), WSR_CALLS_AVG("wsRejectedCallsAvg", false);

    private String label;
    private boolean isIdInfo;

    private Item(String label, boolean isIdInfo) {
      this.label = label;
      this.isIdInfo = isIdInfo;
    }

    public String getLabel() {
      return label;
    }

    public boolean isIdInfo() {
      return isIdInfo;
    }
  }

  /**
   * Parses a date represented by a String with the format used for the date properties into a Date
   */
  public static Date parseDate(String date) throws ParseException {
    return sd.parse(date);
  }

}
