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

package org.openbravo.erpCommon.obps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.CRC32;

import javax.crypto.Cipher;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.DalContextListener;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.obps.DisabledModules.Artifacts;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SystemInfo;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Session;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.HeartbeatLog;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalConnectionProvider;

public class ActivationKey {
  private final static String OB_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCPwCM5RfisLvWhujHajnLEjEpLC7DOXLySuJmHBqcQ8AQ63yZjlcv3JMkHMsPqvoHF3s2ztxRcxBRLc9C2T3uXQg0PTH5IAxsV4tv05S+tNXMIajwTeYh1LCoQyeidiid7FwuhtQNQST9/FqffK1oVFBnWUfgZKLMO2ZSHoEAORwIDAQAB";
  private final static String OB_PUBLIC_KEY2 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCeivfuzeE+hdv7mXEyOWTpGglsT1J+UHcp9RrHydgLgccPdQ5EjqtKVSc/jzzJV5g+9XaSxz9pK5TuzzdN4fJHPCnuO0EiwWI2dxS/t1Boo+gGageGZyFRMhMsULU4902gzmw1qugEskUSKONJcR65H06HYRn2fTgVbGvEhFMASwIDAQAB";

  private static final String HEARTBEAT_URL = "https://butler.openbravo.com:443/heartbeat-server/heartbeat";

  private boolean isActive = false;
  private boolean hasActivationKey = false;
  private String errorMessage = "";
  private String messageType = "Error";
  private Properties instanceProperties;
  private static final Logger log = Logger.getLogger(ActivationKey.class);
  private String strPublicKey;
  private static boolean opsLog = false;
  private static String opsLogId;
  private Long pendingTime;
  private boolean hasExpired = false;
  private boolean subscriptionConvertedProperty = false;
  private boolean subscriptionActuallyConverted = false;
  private LicenseClass licenseClass;
  private List<String> tier1Artifacts;
  private List<String> tier2Artifacts;
  private List<String> goldenExcludedArtifacts;
  private Date lastRefreshTime;
  private boolean trial = false;
  private boolean golden = false;
  private Date startDate;
  private Date endDate;
  private boolean limitedWsAccess = true;

  private boolean notActiveYet = false;
  private boolean inconsistentInstance = false;

  private long maxWsCalls;
  private long wsDayCounter;
  private Date initWsCountTime;
  private List<Date> exceededInLastDays;

  private static final Logger log4j = Logger.getLogger(ActivationKey.class);

  private static final String TIER_1_PREMIUM_FEATURE = "T1P";
  private static final String TIER_2_PREMIUM_FEATURE = "T2P";
  private static final String GOLDEN_EXCLUDED = "GOLDENEXCLUDED";
  private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Number of minutes since last license refresh to wait before doing it again
   */
  private static final int REFRESH_MIN_TIME = 60;

  public enum LicenseRestriction {
    NO_RESTRICTION, OPS_INSTANCE_NOT_ACTIVE, NUMBER_OF_SOFT_USERS_REACHED, NUMBER_OF_CONCURRENT_USERS_REACHED, MODULE_EXPIRED, NOT_MATCHED_INSTANCE, HB_NOT_ACTIVE, EXPIRED_GOLDEN
  }

  public enum CommercialModuleStatus {
    NO_SUBSCRIBED, ACTIVE, EXPIRED, NO_ACTIVE_YET, CONVERTED_SUBSCRIPTION, DISABLED
  }

  public enum FeatureRestriction {
    NO_RESTRICTION(""), DISABLED_MODULE_RESTRICTION("FeatureInDisabledModule"), TIER1_RESTRICTION(
        "FEATURE_OBPS_ONLY"), TIER2_RESTRICTION("FEATURE_OBPS_ONLY"), UNKNOWN_RESTRICTION(""), GOLDEN_RESTRICTION(
        "RESTRICTED_TO_GOLDEN");

    private String msg;

    private FeatureRestriction(String msg) {
      this.msg = msg;
    }

    @Override
    public String toString() {
      return msg;
    }
  }

  public enum WSRestriction {
    NO_RESTRICTION, EXCEEDED_MAX_WS_CALLS, EXCEEDED_WARN_WS_CALLS, EXPIRED;
  }

  public enum LicenseClass {
    COMMUNITY("C"), BASIC("B"), STD("STD");
    private String code;

    private LicenseClass(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  public enum SubscriptionStatus {
    COMMUNITY("COM"), ACTIVE("ACT"), CANCEL("CAN"), EXPIRED("EXP"), NO_ACTIVE_YET("NAY");
    private String code;

    private SubscriptionStatus(String code) {
      this.code = code;
    }

    /**
     * Returns the name of the current status in the given language.
     */
    public String getStatusName(String language) {
      return Utility.getListValueName("OBPSLicenseStatus", code, language);
    }

  }

  private static final int MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
  private static final int PING_TIMEOUT_SECS = 120;
  private static final Long EXPIRATION_BASIC_DAYS = 30L;
  private static final Long EXPIRATION_PROF_DAYS = 30L;

  private final static int WS_DAYS_EXCEEDING_ALLOWED = 5;
  private final static long WS_DAYS_EXCEEDING_ALLOWED_PERIOD = 30L;
  private final static long WS_MS_EXCEEDING_ALLOWED_PERIOD = MILLSECS_PER_DAY
      * WS_DAYS_EXCEEDING_ALLOWED_PERIOD;

  private static ActivationKey instance = new ActivationKey();

  /**
   * @see ActivationKey#getInstance(boolean)
   * 
   */
  public static synchronized ActivationKey getInstance() {
    return getInstance(false);
  }

  /**
   * Obtains the ActivationKey instance. Instances should be get in this way, rather than creating a
   * new one.
   * 
   * If refreshIfNeeded parameter is true, license is tried to be refreshed if it is needed to.
   * 
   * @param refreshIfNeeded
   *          refresh license if needed to
   * 
   */
  public static synchronized ActivationKey getInstance(boolean refreshIfNeeded) {
    if (refreshIfNeeded) {
      instance.refreshIfNeeded();
    }

    if (instance.startDate != null) {
      // check dates in case there is a license with dates
      instance.checkDates();
    }
    return instance;
  }

  public static synchronized void setInstance(ActivationKey ak) {
    instance = ak;
    ak.setRefreshTime(new Date());
  }

  private void setRefreshTime(Date time) {
    lastRefreshTime = time;
  }

  /**
   * Reloads ActivationKey instance from information in DB.
   */
  public static synchronized ActivationKey reload() {
    ActivationKey ak = getInstance();
    org.openbravo.model.ad.system.System sys = OBDal.getInstance().get(
        org.openbravo.model.ad.system.System.class, "0");
    ak.loadInfo(sys.getActivationKey());
    ak.loadRestrictions();
    return ak;
  }

  /**
   * ActivationKey constructor, this should not be used. ActivationKey should be treated as
   * Singleton, so the {@link ActivationKey#getInstance()} method should be used instead.
   * <p/>
   * This constructor is public to maintain backwards compatibility.
   * 
   * @deprecated
   */
  public ActivationKey() {
    OBContext.setAdminMode();
    try {
      org.openbravo.model.ad.system.System sys = OBDal.getInstance().get(
          org.openbravo.model.ad.system.System.class, "0");
      strPublicKey = sys.getInstanceKey();
      String activationKey = sys.getActivationKey();
      loadInfo(activationKey);
      loadRestrictions();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public ActivationKey(String publicKey, String activationKey) {
    strPublicKey = publicKey;
    loadInfo(activationKey);
    loadRestrictions();
  }

  private void loadInfo(String activationKey) {
    // Reset
    isActive = false;
    hasActivationKey = false;
    errorMessage = "";
    messageType = "Error";
    instanceProperties = null;
    opsLog = false;
    hasExpired = false;
    subscriptionConvertedProperty = false;
    subscriptionActuallyConverted = false;
    tier1Artifacts = null;
    tier2Artifacts = null;
    goldenExcludedArtifacts = null;
    trial = false;
    golden = false;
    licenseClass = LicenseClass.COMMUNITY;
    startDate = null;
    endDate = null;
    pendingTime = null;
    limitedWsAccess = false;

    if (strPublicKey == null || activationKey == null || strPublicKey.equals("")
        || activationKey.equals("")) {
      hasActivationKey = false;
      setLogger();
      return;
    }

    PublicKey pk = getPublicKey(strPublicKey);
    if (pk == null) {
      hasActivationKey = true;
      errorMessage = "@NotAValidKey@";
      setLogger();
      return;
    }
    hasActivationKey = true;
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      boolean signed = decrypt(activationKey.getBytes(), pk, bos, OB_PUBLIC_KEY);

      if (!signed) {
        // Basic license is only supported from 2.50mp21, they are signed with second key. So in
        // case first key does not work, try to use the second one.
        bos = new ByteArrayOutputStream();
        signed = decrypt(activationKey.getBytes(), pk, bos, OB_PUBLIC_KEY2);
      }

      if (signed) {
        byte[] props = bos.toByteArray();
        ByteArrayInputStream isProps = new ByteArrayInputStream(props);
        InputStreamReader reader = new InputStreamReader(isProps, "UTF-8");
        instanceProperties = new Properties();

        instanceProperties.load(reader);
        reader.close();
      } else {
        isActive = false;
        errorMessage = "@NotSigned@";
        setLogger();
        return;
      }

      String sysId = getProperty("sysId");
      String dbId = getProperty("dbId");
      String macId = getProperty("macId");

      SystemInfo.loadId(new DalConnectionProvider(false));
      if ((sysId != null && !sysId.isEmpty() && !sysId.equals(SystemInfo.getSystemIdentifier()))
          || (dbId != null && !dbId.isEmpty() && !dbId.equals(SystemInfo.getDBIdentifier()))
          || (macId != null && !macId.isEmpty() && !macId.equals(SystemInfo.getMacAddress()))) {
        isActive = false;
        inconsistentInstance = true;
        errorMessage = "@IncorrectLicenseInstance@";
        setLogger();
        return;
      }
    } catch (Exception e) {
      isActive = false;
      errorMessage = "@NotAValidKey@";
      e.printStackTrace();
      setLogger();
      return;
    }

    // Get license class, old Activation Keys do not have this info, so treat them as Standard
    // Edition instances
    String pLicenseClass = getProperty("licenseedition");
    if (pLicenseClass == null || pLicenseClass.isEmpty() || pLicenseClass.equals("STD")) {
      licenseClass = LicenseClass.STD;
    } else if (pLicenseClass.equals("B")) {
      licenseClass = LicenseClass.BASIC;
    } else {
      log4j.warn("Unknown license class:" + pLicenseClass + ". Using Basic!.");
      licenseClass = LicenseClass.BASIC;
    }

    // Check for dates to know if the instance is active
    subscriptionConvertedProperty = "true".equals(getProperty("subscriptionConverted"));

    trial = "true".equals(getProperty("trial"));
    golden = "true".equals(getProperty("golden"));

    String strUnlimitedWsAccess = getProperty("unlimitedWsAccess");

    if (StringUtils.isEmpty(strUnlimitedWsAccess)) {
      // old license, setting defaults
      if (trial || golden) {
        limitedWsAccess = true;
        maxWsCalls = 500L;
        instanceProperties.put("wsPacks", "1");
        instanceProperties.put("wsUnitsPerUnit", "500");
        initializeWsCounter();
      } else {
        limitedWsAccess = false;
      }
    } else {
      limitedWsAccess = "false".equals(getProperty("unlimitedWsAccess"));
      if (limitedWsAccess) {
        String packs = getProperty("wsPacks");
        String unitsPack = getProperty("wsUnitsPerUnit");

        if (StringUtils.isEmpty(packs) || StringUtils.isEmpty(unitsPack)) {
          log.warn("Couldn't determine ws call limitation, setting unlimited.");
          limitedWsAccess = false;
        } else {
          try {
            Integer nPacks = Integer.parseInt(packs);
            Integer nUnitsPack = Integer.parseInt(unitsPack);
            maxWsCalls = nPacks * nUnitsPack;
            log.debug("Maximum ws calls: " + maxWsCalls);
            initializeWsCounter();
          } catch (Exception e) {
            log.error("Error setting ws call limitation, setting unlimited.", e);
            limitedWsAccess = false;
          }
        }
      }
    }

    try {
      startDate = sDateFormat.parse(getProperty("startdate"));

      if (getProperty("enddate") != null) {
        endDate = sDateFormat.parse(getProperty("enddate"));
      }
    } catch (Exception e) {
      errorMessage = "@ErrorReadingDates@";
      isActive = false;
      log.error(e.getMessage(), e);
      setLogger();
      return;
    }

    checkDates();
  }

  private void checkDates() {
    // Check for dates to know if the instance is active
    Date now = new Date();
    if (startDate == null || now.before(startDate)) {
      isActive = false;
      notActiveYet = true;
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      errorMessage = "@OPSNotActiveTill@ " + outputFormat.format(startDate);
      messageType = "Warning";
      setLogger();
      return;
    }
    if (endDate != null) {
      pendingTime = ((endDate.getTime() - now.getTime()) / MILLSECS_PER_DAY) + 1;
      if (pendingTime <= 0) {
        if (subscriptionConvertedProperty) {
          // A bought out instance is actually converted when the license has expired.
          subscriptionActuallyConverted = true;
        } else {
          isActive = false;
          hasExpired = true;
          String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
              .getProperty("dateFormat.java");
          SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
          errorMessage = "@OPSActivationExpired@ " + outputFormat.format(endDate);
          setLogger();
          return;
        }
      }
    }
    isActive = true;

    setLogger();
  }

  private boolean decrypt(byte[] bytes, PublicKey pk, ByteArrayOutputStream bos,
      String strOBPublicKey) throws Exception {
    PublicKey obPk = getPublicKey(strOBPublicKey); // get OB public key to check signature
    Signature signer = Signature.getInstance("MD5withRSA");
    signer.initVerify(obPk);

    Cipher cipher = Cipher.getInstance("RSA");

    ByteArrayInputStream bis = new ByteArrayInputStream(
        org.apache.commons.codec.binary.Base64.decodeBase64(bytes));

    // Encryptation only accepts 128B size, it must be chuncked
    final byte[] buf = new byte[128];
    final byte[] signature = new byte[128];

    // read the signature
    if (!(bis.read(signature) > 0)) {
      return false;
    }

    // decrypt
    while ((bis.read(buf)) > 0) {
      cipher.init(Cipher.DECRYPT_MODE, pk);
      bos.write(cipher.doFinal(buf));
    }

    // verify signature
    signer.update(bos.toByteArray());
    boolean signed = signer.verify(signature);
    log.debug("signature length:" + buf.length);
    log.debug("singature:" + (new BigInteger(signature).toString(16).toUpperCase()));
    log.debug("signed:" + signed);
    if (!signed) {
      isActive = false;
      errorMessage = "@NotSigned@";
      setLogger();
      return false;
    }
    return true;
  }

  /**
   * Loads information about the restricted artifacts due to license (Premium and Advance features).
   */
  @SuppressWarnings("unchecked")
  private void loadRestrictions() {
    DisabledModules.reload();
    tier1Artifacts = new ArrayList<String>();
    tier2Artifacts = new ArrayList<String>();
    goldenExcludedArtifacts = new ArrayList<String>();
    if (isActive() && licenseClass == LicenseClass.STD && !golden) {
      // Don't read restrictions for Standard instances
      return;
    }

    try {
      // read restriction file from context directory
      String restrictionsFilePath = DalContextListener.getServletContext().getRealPath(
          "src-loc/design/org/openbravo/erpCommon/obps/licenseRestrictions");
      File restrictionsFile = new File(restrictionsFilePath);
      log4j.debug("Restrictions file: " + restrictionsFile.getAbsolutePath());

      FileInputStream fis = new FileInputStream(restrictionsFile);
      byte fileContent[] = new byte[(int) restrictionsFile.length()];
      fis.read(fileContent);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      decrypt(fileContent, getPublicKey(OB_PUBLIC_KEY), bos, OB_PUBLIC_KEY);
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
      HashMap<String, ArrayList<String>> m1 = (HashMap<String, ArrayList<String>>) ois.readObject();
      ois.close();

      if (!isActive()) {
        // community instance, restrict both tiers
        tier1Artifacts.addAll(m1.get(TIER_1_PREMIUM_FEATURE));
        tier2Artifacts.addAll(m1.get(TIER_2_PREMIUM_FEATURE));
      } else if (licenseClass == LicenseClass.BASIC) {
        // basic, restrict tier 2
        tier2Artifacts.addAll(m1.get(TIER_2_PREMIUM_FEATURE));
      }

      if (isGolden()) {
        goldenExcludedArtifacts.addAll(m1.get(GOLDEN_EXCLUDED));
      }
    } catch (Exception e) {
      log4j.error("Error reading license restriction file", e);
      tier1Artifacts = null;
      tier2Artifacts = null;
      goldenExcludedArtifacts = null;
    }
  }

  public LicenseClass getLicenseClass() {
    return licenseClass == null ? LicenseClass.COMMUNITY : licenseClass;
  }

  @SuppressWarnings({ "static-access", "unchecked" })
  private void setLogger() {
    if (isActive() && !opsLog) {
      // add instance id to logger
      Enumeration<Appender> appenders = log.getRootLogger().getAllAppenders();
      while (appenders.hasMoreElements()) {
        Appender appender = appenders.nextElement();
        if (appender.getLayout() instanceof PatternLayout) {
          PatternLayout l = (PatternLayout) appender.getLayout();
          opsLogId = getOpsLogId() + " ";
          String conversionPattern = l.getConversionPattern();

          // do not set checksum in case it is already set
          if (conversionPattern == null || !conversionPattern.startsWith(opsLogId)) {
            l.setConversionPattern(opsLogId + conversionPattern);
          }
        }
      }
      opsLog = true;
    }

    if (!isActive() && opsLog) {

      // remove instance id from logger
      Enumeration<Appender> appenders = log.getRootLogger().getAllAppenders();
      while (appenders.hasMoreElements()) {
        Appender appender = appenders.nextElement();
        if (appender.getLayout() instanceof PatternLayout) {
          PatternLayout l = (PatternLayout) appender.getLayout();
          String pattern = l.getConversionPattern();
          if (pattern.startsWith(opsLogId)) {
            l.setConversionPattern(l.getConversionPattern().substring(opsLogId.length()));
          }
        }
      }
      opsLog = false;
    }

  }

  public String getOpsLogId() {
    CRC32 crc = new CRC32();
    crc.update(getPublicKey().getBytes());
    return Long.toHexString(crc.getValue());
  }

  private PublicKey getPublicKey(String strPublickey) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      byte[] rawPublicKey = org.apache.commons.codec.binary.Base64.decodeBase64(strPublickey
          .getBytes());

      X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(rawPublicKey);
      return keyFactory.generatePublic(publicKeySpec);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  public String getPublicKey() {
    return strPublicKey;
  }

  /**
   * Returns true when the instance currently is OBPS active, this is when it has a valid activation
   * key.
   */
  public boolean isActive() {
    return isActive;
  }

  /**
   * Returns true when the instance currently is OBPS active. It is similar as
   * {@link ActivationKey#isActive}, but it is static and is initialized whenever the ActivationKey
   * class is instantiated.
   */
  public static boolean isActiveInstance() {
    return opsLog;
  }

  /**
   * Returns true when the instance has a activation key and the activation file has been loaded. It
   * doesn't verify is still valid.
   */
  public boolean isOPSInstance() {
    return instanceProperties != null;
  }

  /**
   * Returns true in case the instance has activation key though it might not be valid or activated
   */
  public boolean hasActivationKey() {
    return hasActivationKey;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getMessageType() {
    return messageType;
  }

  /**
   * Deprecated, use instead {@link ActivationKey#checkOPSLimitations(String)}
   * 
   */
  @Deprecated
  public LicenseRestriction checkOPSLimitations() {
    return checkOPSLimitations("");
  }

  /**
   * Checks the current activation key
   * 
   * @param currentSession
   *          Current session, not to be taken into account
   * 
   * @return {@link LicenseRestriction} with the status of the restrictions
   */
  public LicenseRestriction checkOPSLimitations(String currentSession) {
    LicenseRestriction result = LicenseRestriction.NO_RESTRICTION;
    if (!isOPSInstance()) {
      return LicenseRestriction.NO_RESTRICTION;
    }

    if (inconsistentInstance) {
      return LicenseRestriction.NOT_MATCHED_INSTANCE;
    }

    if (trial && !isHeartbeatActive()) {
      return LicenseRestriction.HB_NOT_ACTIVE;
    }

    if (!isActive && golden) {
      return LicenseRestriction.EXPIRED_GOLDEN;
    }

    if (!isActive) {
      return LicenseRestriction.OPS_INSTANCE_NOT_ACTIVE;
    }

    if (getProperty("lincensetype").equals("USR")) {
      Long softUsers = null;
      if (getProperty("limituserswarn") != null) {
        softUsers = new Long(getProperty("limituserswarn"));
      }

      Long maxUsers = new Long(getProperty("limitusers"));

      // maxUsers==0 is unlimited concurrent users
      if (maxUsers != 0) {
        OBContext.setAdminMode();
        int activeSessions = 0;
        try {
          activeSessions = getActiveSessions(currentSession);
          log4j.debug("Active sessions: " + activeSessions);
          if (activeSessions >= maxUsers || (softUsers != null && activeSessions >= softUsers)) {
            // Before raising concurrent users error, clean the session with ping timeout and try it
            // again
            if (deactivateTimeOutSessions(currentSession)) {
              activeSessions = getActiveSessions(currentSession);
              log4j.debug("Active sessions after timeout cleanup: " + activeSessions);
            }
          }
        } catch (Exception e) {
          log4j.error("Error checking sessions", e);
        } finally {
          OBContext.restorePreviousMode();
        }
        if (activeSessions >= maxUsers) {
          return LicenseRestriction.NUMBER_OF_CONCURRENT_USERS_REACHED;
        }

        if (softUsers != null && activeSessions >= softUsers) {
          result = LicenseRestriction.NUMBER_OF_SOFT_USERS_REACHED;
        }
      }
    }

    if (getExpiredInstalledModules().size() > 0) {
      result = LicenseRestriction.MODULE_EXPIRED;
    }

    return result;
  }

  /**
   * Checks if heartbeat is active and a beat has been sent during last days.
   * 
   * @return
   */
  public boolean isHeartbeatActive() {
    OBContext.setAdminMode();
    try {
      Boolean active = OBDal.getInstance().get(SystemInformation.class, "0").isEnableHeartbeat();
      if (active == null || !active) {
        return false;
      }
      OBCriteria<HeartbeatLog> hbLog = OBDal.getInstance().createCriteria(HeartbeatLog.class);
      Calendar lastDays = Calendar.getInstance();
      lastDays.add(Calendar.DAY_OF_MONTH, -9);
      hbLog.add(Restrictions.ge(HeartbeatLog.PROPERTY_CREATIONDATE,
          new Date(lastDays.getTimeInMillis())));
      return hbLog.count() > 0;

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Looks for all active sessions that have not had activity during last
   * {@link ActivationKey#PING_TIMEOUT_SECS} seconds and deactivates them. Activity is tracked by
   * the requests the browser sends to look for alerts (see
   * {@link org.openbravo.erpCommon.utility.VerticalMenu}).
   * <p/>
   * PING_TIMEOUT_SECS is hardcoded to 120s, pings are hardcoded in front-end to 50s.
   */
  private boolean deactivateTimeOutSessions(String currentSessionId) {
    // Last valid ping time is current time substract timeout seconds
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.SECOND, (-1) * PING_TIMEOUT_SECS);
    Date lastValidPingTime = new Date(cal.getTimeInMillis());

    OBCriteria<Session> obCriteria = OBDal.getInstance().createCriteria(Session.class);

    // sesion_active='Y' and (lastPing is null or lastPing<lastValidPing)
    obCriteria.add(Restrictions.and(
        Restrictions.eq(Session.PROPERTY_SESSIONACTIVE, true),
        Restrictions.or(Restrictions.isNull(Session.PROPERTY_LASTPING),
            Restrictions.lt(Session.PROPERTY_LASTPING, lastValidPingTime))));
    obCriteria.add(Restrictions.ne(Session.PROPERTY_ID, currentSessionId));

    boolean sessionDeactivated = false;
    for (Session expiredSession : obCriteria.list()) {
      expiredSession.setSessionActive(false);
      sessionDeactivated = true;
      log4j.info("Deactivated session: " + expiredSession.getId()
          + " beacuse of ping time out. Last ping: " + expiredSession.getLastPing()
          + ". Last valid ping time: " + lastValidPingTime);
    }
    if (sessionDeactivated) {
      OBDal.getInstance().flush();
    } else {
      log4j.debug("No ping timeout sessions");
    }
    return sessionDeactivated;
  }

  /**
   * Returns the number of current active sessions
   */
  private int getActiveSessions(String currentSession) {
    OBCriteria<Session> obCriteria = OBDal.getInstance().createCriteria(Session.class);
    obCriteria.add(Restrictions.eq(Session.PROPERTY_SESSIONACTIVE, true));
    if (currentSession != null && !currentSession.equals("")) {
      obCriteria.add(Restrictions.ne(Session.PROPERTY_ID, currentSession));
    }
    return obCriteria.count();
  }

  public String toString(ConnectionProvider conn, String lang) {
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);

    StringBuilder sb = new StringBuilder();
    if (instanceProperties != null) {
      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSCustomer", lang))
          .append("</td><td>").append(getProperty("customer")).append("</td></tr>");

      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSLicenseEdition", lang))
          .append("</td><td>")
          .append(Utility.getListValueName("OBPSLicenseEdition", licenseClass.getCode(), lang))
          .append("</td></tr>");
      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSLicenseType", lang))
          .append("</td><td>")
          .append(Utility.getListValueName("OPSLicenseType", getProperty("lincensetype"), lang));
      if (trial) {
        sb.append(" (" + Utility.messageBD(conn, "OPSTrialLicense", lang) + ")");
      }
      sb.append("</td></tr>");
      if (startDate != null) {
        sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSStartDate", lang))
            .append("</td><td>").append(outputFormat.format(startDate)).append("</td></tr>");
      }

      if (endDate != null) {
        sb.append("<tr><td>")
            .append(Utility.messageBD(conn, "OPSEndDate", lang))
            .append("</td><td>")
            .append(
                (getProperty("enddate") == null ? Utility.messageBD(conn, "OPSNoEndDate", lang)
                    : outputFormat.format(endDate))).append("</td></tr>");
      }

      sb.append("<tr><td>")
          .append(Utility.messageBD(conn, "OPSConcurrentUsers", lang))
          .append("</td><td>")
          .append(
              (getProperty("limitusers") == null || getProperty("limitusers").equals("0")) ? Utility
                  .messageBD(conn, "OPSUnlimitedUsers", lang) : getProperty("limitusers"))
          .append("</td></tr>");
      if (getProperty("limituserswarn") != null) {
        sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSConcurrentUsersWarn", lang))
            .append("</td><td>").append(getProperty("limituserswarn")).append("</td></tr>");
      }

      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSInstanceNo", lang))
          .append("</td><td>").append(getProperty("instanceno")).append("\n");

      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSInstancePurpose", lang))
          .append("</td><td>")
          .append(Utility.getListValueName("InstancePurpose", getProperty("purpose"), lang))
          .append("</td></tr>");

      sb.append("<tr><td>").append(Utility.messageBD(conn, "OPSWSLimitation", lang))
          .append("</td><td>");
      sb.append(getWSExplanation(conn, lang));
      sb.append("</td></tr>");

    } else {
      sb.append(Utility.messageBD(conn, "OPSNonActiveInstance", lang));
    }
    return sb.toString();
  }

  public String getPurpose(String lang) {
    return Utility.getListValueName("InstancePurpose", getProperty("purpose"), lang);
  }

  public String getLicenseExplanation(ConnectionProvider conn, String lang) {
    if (getProperty("lincensetype").equals("USR")) {
      return getProperty("limitusers") + " " + Utility.messageBD(conn, "OPSConcurrentUsers", lang);

    } else {
      return Utility.getListValueName("OPSLicenseType", getProperty("lincensetype"), lang);
    }
  }

  /**
   * Returns a message explaining WS call limitations
   */
  public String getWSExplanation(ConnectionProvider conn, String lang) {
    if (!limitedWsAccess) {
      return Utility.messageBD(conn, "OPSWSUnlimited", lang);
    } else {
      String packs = getProperty("wsPacks");
      String unitsPack = getProperty("wsUnitsPerUnit");
      return Utility.messageBD(conn, "OPWSLimited", lang).replace("@packs@", packs)
          .replace("@unitsPerPack@", unitsPack);
    }
  }

  public boolean hasExpirationDate() {
    return isOPSInstance() && (getProperty("enddate") != null);
  }

  public String getProperty(String propName) {
    return instanceProperties.getProperty(propName);
  }

  public Long getPendingDays() {
    return pendingTime;
  }

  public boolean isSubscriptionConverted() {
    return subscriptionConvertedProperty;
  }

  public boolean hasExpired() {
    return hasExpired;
  }

  public boolean isNotActiveYet() {
    return notActiveYet;
  }

  /**
   * Obtains a List of all the modules that are installed in the instance which license has expired.
   * 
   * @return List of the expired modules
   */
  public ArrayList<Module> getExpiredInstalledModules() {
    ArrayList<Module> result = new ArrayList<Module>();
    HashMap<String, CommercialModuleStatus> subscribedModules = getSubscribedModules();
    Iterator<String> iterator = subscribedModules.keySet().iterator();
    while (iterator.hasNext()) {
      String moduleId = iterator.next();
      if (subscribedModules.get(moduleId) == CommercialModuleStatus.EXPIRED) {
        Module module = OBDal.getInstance().get(Module.class, moduleId);
        if (module != null && module.getStatus().equals("A")) {
          result.add(module);
        }
      }
    }
    return result;
  }

  /**
   * Obtains a list for modules ID the instance is subscribed to and their statuses
   * 
   * @return HashMap<String, CommercialModuleStatus> containing the subscribed modules
   */
  public HashMap<String, CommercialModuleStatus> getSubscribedModules() {
    return getSubscribedModules(true);
  }

  /**
   * Same as {@link ActivationKey#getSubscribedModules()} with the includeDisabled parameter. When
   * this parameter is true, disabled modules are returned with the DISABLED status, other way they
   * are returned with the status they would have if they were not disabled.
   * 
   * @param includeDisabled
   * @return
   */
  private HashMap<String, CommercialModuleStatus> getSubscribedModules(boolean includeDisabled) {
    HashMap<String, CommercialModuleStatus> moduleList = new HashMap<String, CommercialModuleStatus>();
    if (instanceProperties == null) {
      return moduleList;
    }

    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

    String allModules = getProperty("modules");
    if (allModules == null || allModules.equals(""))
      return moduleList;
    String modulesInfo[] = allModules.split(",");
    Date now = new Date();
    for (String moduleInfo : modulesInfo) {
      String moduleData[] = moduleInfo.split("\\|");

      Date validFrom = null;
      Date validTo = null;
      try {
        validFrom = sd.parse(moduleData[1]);
        if (moduleData.length > 2) {
          validTo = sd.parse(moduleData[2]);
        }
        if (includeDisabled && !DisabledModules.isEnabled(Artifacts.MODULE, moduleData[0])) {
          moduleList.put(moduleData[0], CommercialModuleStatus.DISABLED);
        } else if (subscriptionActuallyConverted) {
          moduleList.put(moduleData[0], CommercialModuleStatus.CONVERTED_SUBSCRIPTION);
        } else if (validFrom.before(now) && (validTo == null || validTo.after(now))) {
          moduleList.put(moduleData[0], CommercialModuleStatus.ACTIVE);
        } else if (validFrom.after(now)) {
          moduleList.put(moduleData[0], CommercialModuleStatus.NO_ACTIVE_YET);
        } else if (validTo != null && validTo.before(now)) {
          if (subscriptionConvertedProperty) {
            moduleList.put(moduleData[0], CommercialModuleStatus.CONVERTED_SUBSCRIPTION);
          } else {
            moduleList.put(moduleData[0], CommercialModuleStatus.EXPIRED);
          }
        }
      } catch (Exception e) {
        log.error("Error reading module's dates module:" + moduleData[0], e);
      }

    }
    return moduleList;
  }

  /**
   * Checks whether a disabled module can be enabled again. A commercial module cannot be enabled in
   * case its license has expired or the instance is not commercial.
   * 
   * @param module
   * @return true in case the module can be enabled
   */
  public boolean isModuleEnableable(Module module) {
    if (!module.isCommercial()) {
      return true;
    }
    if (!isActive()) {
      return false;
    }

    HashMap<String, CommercialModuleStatus> moduleList = getSubscribedModules(false);

    if (!moduleList.containsKey(module.getId())) {
      return false;
    }

    CommercialModuleStatus status = moduleList.get(module.getId());
    return status == CommercialModuleStatus.ACTIVE
        || status == CommercialModuleStatus.CONVERTED_SUBSCRIPTION;
  }

  /**
   * Returns the status for the commercial module passed as parameter. Note that module tier is not
   * checked here, this should be correctly handled in the license itself.
   * 
   * @param moduleId
   * @return the status for the commercial module passed as parameter
   */
  public CommercialModuleStatus isModuleSubscribed(String moduleId) {
    return isModuleSubscribed(moduleId, true);
  }

  private CommercialModuleStatus isModuleSubscribed(String moduleId, boolean refreshIfNeeded) {
    HashMap<String, CommercialModuleStatus> moduleList = getSubscribedModules();

    if (!moduleList.containsKey(moduleId)) {
      log4j.debug("Module " + moduleId + " is not in the list of subscribed modules");

      if (!refreshIfNeeded) {
        return CommercialModuleStatus.NO_SUBSCRIBED;
      }

      boolean refreshed = refreshLicense(REFRESH_MIN_TIME);

      if (refreshed) {
        return ActivationKey.instance.isModuleSubscribed(moduleId);
      } else {
        return CommercialModuleStatus.NO_SUBSCRIBED;
      }
    }

    return moduleList.get(moduleId);
  }

  /**
   * Refreshes license online in case of:
   * <ul>
   * <li>It expired
   * <li>Maximum number of WS calls has been reached during last 30 days
   * <li>Maximum number of concurrent users has been reached
   * </ul>
   */
  private void refreshIfNeeded() {
    if (hasActivationKey
        && !subscriptionConvertedProperty
        && !golden
        && !trial
        && (hasExpired || checkNewWSCall(false) != WSRestriction.NO_RESTRICTION || checkOPSLimitations(null) == LicenseRestriction.NUMBER_OF_CONCURRENT_USERS_REACHED)) {
      refreshLicense(24 * 60);
    }
  }

  private synchronized boolean refreshLicense(int minutesToRefresh) {
    Date timeToRefresh = null;
    if (lastRefreshTime != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(lastRefreshTime);
      calendar.add(Calendar.MINUTE, minutesToRefresh);
      timeToRefresh = calendar.getTime();
    }

    if (timeToRefresh == null || new Date().after(timeToRefresh)) {
      log4j.debug("Trying to refresh license, last refresh "
          + (lastRefreshTime == null ? "never" : lastRefreshTime.toString()));

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("publicKey", strPublicKey);
      params.put("purpose", getProperty("purpose"));
      params.put("instanceNo", getProperty("instanceno"));
      params.put("activate", true);
      ProcessBundle pb = new ProcessBundle(null, new VariablesSecureApp("0", "0", "0"));
      pb.setParams(params);

      boolean refreshed = false;
      OBContext.setAdminMode();
      try {
        new ActiveInstanceProcess().execute(pb);
        OBError msg = (OBError) pb.getResult();
        refreshed = msg.getType().equals("Success");
        if (refreshed) {
          OBDal.getInstance().flush();
          log4j.debug("Instance refreshed");
        } else {
          log4j.info("Problem refreshing instance " + msg.getMessage());
        }
      } catch (Exception e) {
        log4j.error("Error refreshing instance", e);
        refreshed = false;
      } finally {
        OBContext.restorePreviousMode();
      }

      if (!refreshed) {
        // Even license couldn't be refreshed, set lastRefreshTime not to try to refresh in the
        // following period of time
        lastRefreshTime = new Date();
      }
      return refreshed;
    } else {
      log4j.debug("Not refreshing, last refresh was " + lastRefreshTime.toString()
          + ". Next time to refresh " + timeToRefresh.toString());
      return false;
    }
  }

  /**
   * Checks whether there is access to an artifact because of license restrictions (checking core
   * advance and premium features).
   * 
   * @param type
   *          Type of artifact (Window, Report, Process...)
   * @param id
   *          Id of the Artifact
   * @return true in case it has access, false if not
   */
  public FeatureRestriction hasLicenseAccess(String type, String id) {
    String actualType = type;

    if (actualType == null || actualType.isEmpty() || id == null || id.isEmpty()) {
      return FeatureRestriction.NO_RESTRICTION;
    }
    log4j.debug("Type:" + actualType + " id:" + id);
    if (tier1Artifacts == null || tier2Artifacts == null || goldenExcludedArtifacts == null) {
      log4j.error("No restrictions set, do not allow access");

      throw new OBException(Utility.messageBD(new DalConnectionProvider(false),
          "NoRestrictionsFile", OBContext.getOBContext().getLanguage().getLanguage()));
    }

    String artifactId = id;
    if ("W".equals(actualType)) {
      // Access is granted to window, but permissions is checked for tabs
      OBContext.setAdminMode();
      try {
        Tab tab = OBDal.getInstance().get(Tab.class, id);
        if (tab == null) {
          log4j.error("Could't find tab " + id + " to check access. Access not allowed");
          return FeatureRestriction.UNKNOWN_RESTRICTION;
        }
        artifactId = tab.getWindow().getId();

        // For windows check whether the window's module is disabled, and later whether the tab is
        // disabled
        if (!DisabledModules.isEnabled(Artifacts.MODULE, tab.getWindow().getModule().getId())) {
          return FeatureRestriction.DISABLED_MODULE_RESTRICTION;
        }
      } finally {
        OBContext.restorePreviousMode();
      }
    } else if ("MW".equals(actualType)) {
      // Menu window, it receives window instead of tab
      actualType = "W";
    } else if ("R".equals(actualType)) {
      actualType = "P";
    }

    // Check disabled modules restrictions
    Artifacts artifactType;
    if ("MW".equals(actualType)) {
      artifactType = Artifacts.WINDOW;
    } else if ("W".equals(actualType)) {
      artifactType = Artifacts.TAB;
    } else if ("X".equals(actualType)) {
      artifactType = Artifacts.FORM;
    } else {
      artifactType = Artifacts.PROCESS;
    }
    // Use id instead of artifactId to keep tabs' ids
    if (!DisabledModules.isEnabled(artifactType, id)) {
      return FeatureRestriction.DISABLED_MODULE_RESTRICTION;
    }

    // Check core premium features restrictions
    if (licenseClass == LicenseClass.STD && !golden) {
      return FeatureRestriction.NO_RESTRICTION;
    }

    if (tier1Artifacts.contains(actualType + artifactId)) {
      return FeatureRestriction.TIER1_RESTRICTION;
    }
    if (tier2Artifacts.contains(actualType + artifactId)) {
      return FeatureRestriction.TIER2_RESTRICTION;
    }
    if (goldenExcludedArtifacts.contains(actualType + artifactId)) {
      return FeatureRestriction.GOLDEN_RESTRICTION;
    }

    if ("W".equals(actualType)) {
      // For windows, check also tab restrictions
      return hasLicencesTabAccess(id);
    }

    return FeatureRestriction.NO_RESTRICTION;
  }

  public FeatureRestriction hasLicencesTabAccess(String tabId) {
    if (tier1Artifacts.contains("T" + tabId)) {
      return FeatureRestriction.TIER1_RESTRICTION;
    }
    if (tier2Artifacts.contains("T" + tabId)) {
      return FeatureRestriction.TIER2_RESTRICTION;
    }
    return FeatureRestriction.NO_RESTRICTION;
  }

  /**
   * Verifies all the commercial installed modules are allowed to the instance.
   * 
   * @return List of non allowed modules
   */
  public String verifyInstalledModules() {
    return verifyInstalledModules(true);
  }

  String verifyInstalledModules(boolean refreshIfneeded) {
    String rt = "";

    OBContext.setAdminMode();
    try {
      OBCriteria<Module> mods = OBDal.getInstance().createCriteria(Module.class);
      mods.add(Restrictions.eq(Module.PROPERTY_COMMERCIAL, true));
      mods.add(Restrictions.eq(Module.PROPERTY_ENABLED, true));
      // Allow development of commercial modules which are not in the license.
      mods.add(Restrictions.eq(Module.PROPERTY_INDEVELOPMENT, false));
      mods.addOrder(Order.asc(Module.PROPERTY_NAME));
      for (Module mod : mods.list()) {
        if (isModuleSubscribed(mod.getId(), refreshIfneeded) == CommercialModuleStatus.NO_SUBSCRIBED) {
          rt += (rt.isEmpty() ? "" : ", ") + mod.getName();
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return rt;
  }

  /**
   * Returns current subscription status
   */
  public SubscriptionStatus getSubscriptionStatus() {
    if (!isOPSInstance() || inconsistentInstance) {
      return SubscriptionStatus.COMMUNITY;
    } else if (isSubscriptionConverted()) {
      return SubscriptionStatus.CANCEL;
    } else if (hasExpired()) {
      return SubscriptionStatus.EXPIRED;
    } else if (isNotActiveYet()) {
      return SubscriptionStatus.NO_ACTIVE_YET;
    } else {
      return SubscriptionStatus.ACTIVE;
    }
  }

  public boolean isTrial() {
    return trial;
  }

  public boolean isGolden() {
    return golden;
  }

  /**
   * Returns a JSONObject with a message warning about near expiration or already expired instance.
   * 
   */
  public JSONObject getExpirationMessage(String lang) {
    JSONObject result = new JSONObject();
    try {
      // Community or professional without expiration
      if (pendingTime == null || subscriptionActuallyConverted) {
        return result;
      }

      if (!hasExpired) {
        String msg;
        Long daysToExpireMsg = getProperty("daysWarn") == null ? null : Long
            .parseLong(getProperty("daysWarn"));
        if (golden) {
          msg = "OBPS_TO_EXPIRE_GOLDEN";
          if (daysToExpireMsg == null) {
            daysToExpireMsg = 999L; // show always
          }
        } else if (trial) {
          msg = "OBPS_TO_EXPIRE_TRIAL";
          if (daysToExpireMsg == null) {
            daysToExpireMsg = 999L; // show always
          }
        } else if (licenseClass == LicenseClass.BASIC) {
          msg = "OBPS_TO_EXPIRE_BASIC";
          if (daysToExpireMsg == null) {
            daysToExpireMsg = EXPIRATION_BASIC_DAYS;
          }
        } else {
          msg = "OBPS_TO_EXPIRE_PROF";
          if (daysToExpireMsg == null) {
            daysToExpireMsg = EXPIRATION_PROF_DAYS;
          }
        }

        if (pendingTime <= daysToExpireMsg) {
          result.put("type", "Error");
          result.put("text", Utility.messageBD(new DalConnectionProvider(false), msg, lang, false)
              .replace("@days@", pendingTime.toString()));
        }
      } else {
        String msg;
        if (golden) {
          msg = "OBPS_EXPIRED_GOLDEN";
          result.put("disableLogin", true);
        } else if (trial) {
          msg = "OBPS_EXPIRED_TRIAL";
        } else if (licenseClass == LicenseClass.BASIC) {
          msg = "OBPS_EXPIRED_BASIC";
        } else {
          msg = "OBPS_EXPIRED_PROF";
        }

        result.put("type", "Error");
        result.put("text", Utility.messageBD(new DalConnectionProvider(false), msg, lang, false));
      }
    } catch (JSONException e) {
      log4j.error("Error calculating expiration message", e);
    }
    return result;
  }

  /**
   * This method checks web service can be called. If <code>updateCounter</code> parameter is
   * <code>true</code> number of daily calls is increased by one.
   * 
   * @param updateCounter
   *          daily calls should be updated
   */
  public synchronized WSRestriction checkNewWSCall(boolean updateCounter) {
    if (!limitedWsAccess) {
      return WSRestriction.NO_RESTRICTION;
    }

    if (hasExpired) {
      return WSRestriction.EXPIRED;
    }
    Date today = getDayAt0(new Date());

    if (initWsCountTime == null || today.getTime() != initWsCountTime.getTime()) {
      initializeWsDayCounter();
    }

    long checkCalls = maxWsCalls;
    if (updateCounter) {
      wsDayCounter += 1;
      // Adding 1 to maxWsCalls because session is already saved in DB
      checkCalls += 1;
    }

    if (wsDayCounter > checkCalls) {
      // clean up old days
      while (!exceededInLastDays.isEmpty()
          && exceededInLastDays.get(0).getTime() < today.getTime() - WS_MS_EXCEEDING_ALLOWED_PERIOD) {
        Date removed = exceededInLastDays.remove(0);
        log.info("Removed date from exceeded days " + removed);
      }

      if (!exceededInLastDays.contains(today)) {
        exceededInLastDays.add(today);

        // Adding a new failing day, send a new beat to butler
        Runnable sendBeatProcess = new Runnable() {
          @Override
          public void run() {
            try {
              String content = "beatType=CWSR";
              content += "&systemIdentifier="
                  + URLEncoder.encode(SystemInfo.getSystemIdentifier(), "utf-8");
              content += "&dbIdentifier="
                  + URLEncoder.encode(SystemInfo.getDBIdentifier(), "utf-8");
              content += "&macId=" + URLEncoder.encode(SystemInfo.getMacAddress(), "utf-8");
              content += "&obpsId=" + URLEncoder.encode(SystemInfo.getOBPSInstance(), "utf-8");
              content += "&instanceNo="
                  + URLEncoder.encode(SystemInfo.getOBPSIntanceNumber(), "utf-8");

              URL url = new URL(HEARTBEAT_URL);
              HttpsUtils.sendSecure(url, content);
              log.info("Sending CWSR beat");
            } catch (Exception e) {
              log.error("Error connecting server", e);
            }

          }
        };
        Thread sendBeat = new Thread(sendBeatProcess);
        sendBeat.start();
      }

      if (exceededInLastDays.size() > WS_DAYS_EXCEEDING_ALLOWED) {
        return WSRestriction.EXCEEDED_MAX_WS_CALLS;
      } else {
        return WSRestriction.EXCEEDED_WARN_WS_CALLS;
      }
    }
    return WSRestriction.NO_RESTRICTION;
  }

  private Date getDayAt0(Date date) {
    try {
      return sDateFormat.parse(sDateFormat.format(date));
    } catch (ParseException e) {
      log.error("Error getting day " + date + " at 0:00", e);
      return new Date();
    }
  }

  private void initializeWsDayCounter() {
    initWsCountTime = getDayAt0(new Date());
    OBContext.setAdminMode();
    try {
      OBCriteria<Session> qLogins = OBDal.getInstance().createCriteria(Session.class);
      qLogins.add(Restrictions.eq(Session.PROPERTY_LOGINSTATUS, "WS"));
      qLogins.add(Restrictions.ge(Session.PROPERTY_CREATIONDATE, initWsCountTime));
      wsDayCounter = qLogins.count();
      log.info("Initialized ws count to " + wsDayCounter + " from " + initWsCountTime);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void initializeWsCounter() {
    StringBuilder hql = new StringBuilder();
    hql.append("select min(creationDate)\n");
    hql.append("  from ADSession\n");
    hql.append(" where loginStatus = 'WS'\n");
    hql.append("   and creationDate > :firstDay\n");
    hql.append(" group by day(creationDate), month(creationDate), year(creationDate)\n");
    hql.append("having count(*) > :maxWsPerDay\n");
    hql.append(" order by 1\n");

    Query qExceededDays = OBDal.getInstance().getSession().createQuery(hql.toString());
    qExceededDays.setParameter("firstDay", new Date(getDayAt0(new Date()).getTime()
        - WS_MS_EXCEEDING_ALLOWED_PERIOD));
    qExceededDays.setParameter("maxWsPerDay", maxWsCalls);

    exceededInLastDays = new ArrayList<Date>();

    for (Object d : qExceededDays.list()) {
      Date day = getDayAt0((Date) d);
      exceededInLastDays.add(day);
      log.info("Addind exceeded ws calls day " + day);
    }
    initializeWsDayCounter();
  }

  /**
   * Returns the number of days during last 30 days exceeding the maximum allowed number of calls
   */
  public int getWsCallsExceededDays() {
    if (exceededInLastDays == null) {
      return 0;
    }
    return exceededInLastDays.size();
  }

  /**
   * Returns the number of days that can exceed the maximum number of ws calls taking into account
   * the ones that exceeded it during last 30 days.
   */
  public int getExtraWsExceededDaysAllowed() {
    return WS_DAYS_EXCEEDING_ALLOWED - getWsCallsExceededDays();
  }

  /**
   * Returns the number of days pending till the end of ws calls verification period.
   */
  public int getNumberOfDaysLeftInPeriod() {
    if (exceededInLastDays == null || exceededInLastDays.size() == 0) {
      return (int) WS_DAYS_EXCEEDING_ALLOWED_PERIOD;
    }

    Date today = getDayAt0(new Date());
    Date firstDayOfPeriod = exceededInLastDays.get(0);

    long lastDayOfPeriod;
    if (today.getTime() + (getExtraWsExceededDaysAllowed() * MILLSECS_PER_DAY) < firstDayOfPeriod
        .getTime() + WS_MS_EXCEEDING_ALLOWED_PERIOD) {
      lastDayOfPeriod = firstDayOfPeriod.getTime() + WS_MS_EXCEEDING_ALLOWED_PERIOD;
    } else {
      lastDayOfPeriod = today.getTime() + WS_MS_EXCEEDING_ALLOWED_PERIOD;
    }
    new Date(lastDayOfPeriod);
    long pendingMs = lastDayOfPeriod - today.getTime()
        - (exceededInLastDays.size() * MILLSECS_PER_DAY);
    return (int) (pendingMs / MILLSECS_PER_DAY);
  }
}
