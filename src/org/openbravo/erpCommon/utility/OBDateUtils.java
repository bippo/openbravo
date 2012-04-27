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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;

/**
 * Utilities to manage dates.
 */
public class OBDateUtils {

  /**
   * Returns an String with the date in the <i>dateFormat.java</i> format defined in
   * Openbravo.properties
   * 
   * @see OBDateUtils#formatDate(Date, String)
   * 
   * @param date
   *          Date to be formatted.
   * @param pattern
   *          Format expected for the output.
   * @return String formatted.
   */
  public static String formatDate(Date date) {
    final String pattern = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
    return dateFormatter.format(date);
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
   * Parses the string to a date using the dateFormat.java property.
   * 
   * @param strDate
   *          String containing the date
   * @return the date
   * @throws ParseException
   */
  public static Date getDate(String strDate) throws ParseException {
    if (strDate.equals("")) {
      return null;
    }
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
    return outputFormat.parse(strDate);
  }

  /**
   * Parses the string to a date with time using the dateTimeFormat defined in Openbravo.properties.
   * If the string parameter does not have time include it will add the current hours, minutes and
   * seconds.
   * 
   * @param strDate
   *          String date.
   * @return the date with time.
   * @throws ParseException
   */
  public static Date getDateTime(String strDate) throws ParseException {
    String dateTime = strDate;
    Calendar cal = Calendar.getInstance();
    if ("".equals(strDate) || strDate == null) {
      return null;
    }
    if (!strDate.contains(":")) {
      dateTime = strDate + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)
          + ":" + cal.get(Calendar.SECOND);
    }
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateTimeFormat.java");
    SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
    return outputFormat.parse(dateTime);
  }

  /**
   * Determines the labor days between two dates
   * 
   * @param strDate1
   *          Date 1.
   * @param strDate2
   *          Date 2.
   * @param dateFormatter
   *          Format of the dates.
   * @return strLaborDays as the number of days between strDate1 and strDate2.
   */
  public static String calculateLaborDays(String _strDate1, String _strDate2,
      DateFormat dateFormatter) throws ParseException {
    String strLaborDays = "";
    String strDate1 = _strDate1;
    String strDate2 = _strDate2;
    if (strDate1 != null && !strDate1.equals("") && strDate2 != null && !strDate2.equals("")) {
      Integer laborDays = 0;
      if (isBiggerDate(strDate1, strDate2, dateFormatter)) {
        do {
          // Adds a day to the Date 2 until it reaches the Date 1
          strDate2 = addDaysToDate(strDate2, 1, dateFormatter);
          if (!isWeekendDay(strDate2, dateFormatter)) {
            // If it is not a weekend day, it adds a day to the labor days
            laborDays++;
          }
        } while (!strDate2.equals(strDate1));
      } else {
        do {
          // Adds a day to the Date 1 until it reaches the Date 2
          strDate1 = addDaysToDate(strDate1, 1, dateFormatter);
          if (!isWeekendDay(strDate1, dateFormatter)) {
            // If it is not a weekend day, it adds a day to the labor days
            laborDays++;
          }
        } while (!strDate1.equals(strDate2));
      }
      strLaborDays = laborDays.toString();
    }
    return strLaborDays;
  }

  /**
   * Adds an integer number of days to a given date
   * 
   * @param strDate
   *          Start date.
   * @param days
   *          Number of days to add.
   * @param dateFormatter
   *          Format of the date.
   * @return strFinalDate as the sum of strDate plus strDays.
   * @throws ParseException
   */
  public static String addDaysToDate(String strDate, int days, DateFormat dateFormatter)
      throws ParseException {
    if (strDate == null || "".equals(strDate)) {
      return "";
    }
    Date date = dateFormatter.parse(strDate);
    Date finalDate = DateUtils.addDays(date, days);
    return dateFormatter.format(finalDate);
  }

  /**
   * Determines the format of the date
   * 
   * @param vars
   *          Global variables.
   * @return DateFormatter as the format of the date.
   */
  public static DateFormat getDateFormatter(VariablesSecureApp vars) {
    String strFormat = vars.getJavaDateFormat();
    final DateFormat DateFormatter = new SimpleDateFormat(strFormat);
    return DateFormatter;
  }

  /**
   * Determines if a day is a day of the weekend, i.e., Saturday or Sunday
   * 
   * @param strDay
   *          Given Date.
   * @param dateFormatter
   *          Format of the date.
   * @return true if the date is a Sunday or a Saturday.
   */
  public static boolean isWeekendDay(String strDay, DateFormat dateFormatter) throws ParseException {
    final Calendar Day = Calendar.getInstance();
    Day.setTime(dateFormatter.parse(strDay));
    final int weekday = Day.get(Calendar.DAY_OF_WEEK);
    // Gets the number of the day of the week: 1-Sunday, 2-Monday, 3-Tuesday, 4-Wednesday,
    // 5-Thursday, 6-Friday, 7-Saturday
    return weekday == 1 || weekday == 7;
  }

  /**
   * Determines if a date 1 is bigger than a date 2
   * 
   * @param strDate1
   *          Date 1.
   * @param strDate2
   *          Date 2.
   * @param dateFormatter
   *          Format of the dates.
   * @return true if strDate1 is bigger than strDate2.
   */
  public static boolean isBiggerDate(String strDate1, String strDate2, DateFormat dateFormatter)
      throws ParseException {
    final Date date1 = dateFormatter.parse(strDate1);
    final Date date2 = dateFormatter.parse(strDate2);
    return date1.after(date2);
  }

}
