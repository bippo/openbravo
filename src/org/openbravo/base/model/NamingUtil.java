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

package org.openbravo.base.model;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;

/**
 * The NamingUtil class is used to create names for the {@link Property properties} of the
 * {@link Entity entities}.
 * 
 * @author iperdomo
 * @author mtaal
 */

public class NamingUtil {
  private static final Logger log = org.apache.log4j.Logger.getLogger(NamingUtil.class);

  public static final char[] ILLEGAL_ENTITY_NAME_CHARS = new char[] { '.', ',', ' ', '/', '\\' };

  public static final String ENTITY_NAME_CONSTANT = "ENTITY_NAME";
  public static final String PROPERTY_CONSTANT_PREFIX = "PROPERTY_";

  private static HashMap<String, String> reservedNames = new HashMap<String, String>();

  static {
    reservedNames.put("case", "cse");
    reservedNames.put("char", "chr");
    reservedNames.put("abstract", "abstrct");
    reservedNames.put("continue", "cntinue");
    reservedNames.put("for", "fr");
    reservedNames.put("new", "nw");
    reservedNames.put("switch", "swtch");
    reservedNames.put("assert", "assrt");
    reservedNames.put("default", "deflt");
    reservedNames.put("package", "pkg");
    reservedNames.put("synchronized", "synchrnized");
    reservedNames.put("boolean", "bolean");
    reservedNames.put("do", "d");
    reservedNames.put("if", "i");
    reservedNames.put("private", "prvate");
    reservedNames.put("this", "ths");
    reservedNames.put("break", "brk");
    reservedNames.put("double", "dble");
    reservedNames.put("implements", "implments");
    reservedNames.put("protected", "prtected");
    reservedNames.put("throw", "thrw");
    reservedNames.put("byte", "bte");
    reservedNames.put("else", "els");
    reservedNames.put("import", "imprt");
    reservedNames.put("public", "pblic");
    reservedNames.put("throws", "thrws");
    reservedNames.put("case", "cse");
    reservedNames.put("enum", "enm");
    reservedNames.put("instanceof", "instnceof");
    reservedNames.put("return", "rturn");
    reservedNames.put("transient", "trnsnt");
    reservedNames.put("catch", "ctch");
    reservedNames.put("extends", "xtends");
    reservedNames.put("int", "nt");
    reservedNames.put("short", "shrt");
    reservedNames.put("try", "tr");
    reservedNames.put("char", "chr");
    reservedNames.put("final", "fnl");
    reservedNames.put("interface", "interfce");
    reservedNames.put("static", "statc");
    reservedNames.put("void", "vod");
    reservedNames.put("class", "clss");
    reservedNames.put("finally", "finlly");
    reservedNames.put("long", "lng");
    reservedNames.put("strictfp", "strctfp");
    reservedNames.put("volatile", "volatle");
    reservedNames.put("float", "flt");
    reservedNames.put("native", "natve");
    reservedNames.put("super", "spr");
    reservedNames.put("while", "whle");
  }

  /**
   * Checks if a name of a table contains illegal chars.
   * 
   * @param name
   *          the name to check
   * @return true if the name contains a char from the {@link #ILLEGAL_ENTITY_NAME_CHARS}).
   */
  public static boolean doesNameHaveIllegalChars(String name) {
    final char[] chars = name.trim().toCharArray();
    for (char c : chars) {
      for (char illegalChar : ILLEGAL_ENTITY_NAME_CHARS) {
        if (c == illegalChar) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks a name for non-normal characters. Non-normal is everything from a to z, A to Z, 0 to 9
   * and _.
   * 
   * @param name
   *          the name to check
   * @return true if the name contains illegal chars, false otherwise
   */
  public static boolean doesNameContainNonNormalCharacters(String name) {
    final char[] chars = name.trim().toCharArray();
    for (char c : chars) {
      final boolean normalChar = ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9')
          || ('a' <= c && c <= 'z') || c == '_';
      if (!normalChar) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the value of the ENTITY_NAME constant in the passed class.
   * 
   * @param clz
   *          the entity class
   * @return the ENTITY_NAME constant
   */
  public static String getEntityName(Class<?> clz) {
    try {
      if (clz == null) {
        return null;
      }
      final Field fld = clz.getField(ENTITY_NAME_CONSTANT);
      return (String) fld.get(null);
    } catch (final Exception e) {
      throw new OBException("Exception when getting ENTITY_NAME constant from  " + clz.getName(), e);
    }
  }

  /**
   * Generated entity classes have String constants to denote their property name. It has a
   * performance benefit if the String constant is used because then HashMap get operations are
   * faster (as equals also tests on object equality). This method tries to retrieve the String
   * constant from the passed class. The clz parameter maybe null (happens for dynamic entities). In
   * this case the passed propertyName is returned.
   * 
   * @param clz
   *          the entity class, maybe null, in which case propertyName is returned
   * @param propertyName
   *          the propertyName to search for in the class
   * @return the String constant or the passed propertyName
   */
  public static String getStaticPropertyName(Class<?> clz, String propertyName) {
    try {
      if (clz == null) {
        return propertyName;
      }
      final Field fld = clz.getField(PROPERTY_CONSTANT_PREFIX + propertyName.toUpperCase());
      final String fldPropertyValue = (String) fld.get(null);
      // see issue:
      // 14619: Generate entities do not notice that a field name has changed one letter to upper
      if (fldPropertyValue != null && propertyName.equals(fldPropertyValue)) {
        return fldPropertyValue;
      }
      return propertyName;
    } catch (final NoSuchFieldException e) {
      // ignoring on purpose, exception can occur when a new column is
      // added and its business object class has not yet been generated
      return propertyName;
    } catch (final IllegalAccessException e) {
      // ignoring on purpose, exception can occur when a new column is
      // added and its business object class has not yet been generated
      return propertyName;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  static String getSafeJavaName(String name) {
    if (reservedNames.get(name) != null) {
      return reservedNames.get(name);
    }
    return name;
  }

  static String getPropertyMappingName(Property property) {
    String mappingName = getPropName(property);
    if (property.isPrimitive()) {
      if (property.isId() && property.getEntity().getIdProperties().size() == 1) {
        mappingName = "id";
      }

      // if (property.isBoolean()
      // && mappingName.toLowerCase().startsWith("is")) {
      // String tmp = mappingName.substring(2);
      // boolean duplicated = false;
      // for (Property p : property.getEntity().getProperties()) {
      // if (tmp.equals(p.getColumnName())) {
      // duplicated = true;
      // break;
      // }
      // }
      // if (!duplicated) {
      // mappingName = tmp;
      // }
      // }
    } else {
      if (property.getTargetEntity() == null && mappingName != null) {
        log.error("Property " + property + " does not have a target entity");
      } else if (property.isOneToMany()) {
        if (columnNameSameAsKeyColumn(property.getReferencedProperty())) {
          mappingName = property.getTargetEntity().getName() + "List";
        } else {
          // for a one-to-many the referenced property is the
          // property which models the real foreign key column
          mappingName = property.getTargetEntity().getName() + "_"
              + getPropName(property.getReferencedProperty()) + "List";
        }
      } else if (columnNameSameAsKeyColumn(property)) {
        mappingName = getPropName(property); // property.getTargetEntity().getSimpleClassName();
      } else {
        mappingName = getPropName(property);
      }
    }
    // only strip for core module
    final boolean coreModuleProp = property.getModule() != null
        && !property.getModule().getId().equals("0");
    final boolean coreModuleEntity = property.getEntity().getModule().getId().equals("0");

    if (coreModuleProp || coreModuleEntity) {
      mappingName = stripPrefix(mappingName);
    }
    mappingName = camelCaseIt(mappingName, "_");
    mappingName = camelCaseIt(mappingName, " ");
    mappingName = stripIllegalCharacters(mappingName);
    mappingName = lowerCaseFirst(mappingName);

    // check for doublures and be robust....
    for (Property p : property.getEntity().getProperties()) {
      if (p.getName() != null && p.getName().equalsIgnoreCase(mappingName)) {
        log.error("ERROR: Property name computation fails for property "
            + property
            + " using new name "
            + mappingName
            + " there is more then one property with the same name, being robust and "
            + "renaming property automatically. If this error appears during update.database then "
            + "this is possibly solved automatically. Otherwise this should be repaired manually by "
            + "changing the AD_Column.name of the column " + property.getEntity().getTableName()
            + "." + property.getColumnName());

        mappingName += property.getIndexInEntity();
        return mappingName;
      }
    }

    return mappingName;
  }

  // get rid of illegal characters
  private static String stripIllegalCharacters(String value) {
    // TODO: probably it is faster to use a char array
    // instead of the stringbuilder
    final StringBuilder result = new StringBuilder();

    boolean first = true;
    for (char ch : value.toCharArray()) {

      if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
          || (!first && ch >= '0' && ch <= '9')) {
        result.append(ch);
        first = false;
      } else {
        result.append("");
      }
    }
    return result.toString();
  }

  private static String getPropName(Property prop) {
    return prop.getNameOfColumn();
  }

  // checks if the columname is the same as the pk column name of the
  // target entity
  private static boolean columnNameSameAsKeyColumn(Property p) {
    final Entity targetEntity = p.getTargetEntity();
    if (p.getColumnName() == null) {
      return false;
    }
    if (targetEntity == null) {
      return true;
    }
    // more than one pk column, can not handle that here
    if (targetEntity.getIdProperties().size() != 1) {
      return false;
    }
    if (p.getReferencedProperty() == null || p.getReferencedProperty().getColumnName() == null) {
      return false;
    }
    return p.getColumnName().toLowerCase()
        .equals(p.getReferencedProperty().getColumnName().toLowerCase());
  }

  private static String stripPrefix(String mappingName) {
    String localMappingName = mappingName;
    if (localMappingName.toLowerCase().endsWith("_id")) {
      localMappingName = mappingName.substring(0, mappingName.length() - 3);
    }
    final int index = localMappingName.indexOf("_");
    if (index == 1) {
      return localMappingName.substring(2);
    } else if (index == 2) {
      return localMappingName.substring(3);
    }
    return localMappingName;
  }

  private static String camelCaseIt(String mappingName, String separator) {
    String localMappingName = mappingName;
    // strip _ at the end
    while (localMappingName.endsWith(separator)) {
      localMappingName = localMappingName.substring(0, localMappingName.length() - 1);
    }
    // strip _ at the beginning
    while (localMappingName.startsWith(separator)) {
      localMappingName = localMappingName.substring(1);
    }

    // "CamelCasing"
    int pos = localMappingName.indexOf(separator);
    while (pos != -1) {
      final String leftPart = localMappingName.substring(0, pos);
      final String camelLetter = String.valueOf(localMappingName.charAt(pos + 1)).toUpperCase();
      final String rightPart = localMappingName.substring(pos + 2);
      localMappingName = leftPart + camelLetter + rightPart;
      pos = localMappingName.indexOf(separator);
    }
    return localMappingName;
  }

  private static String lowerCaseFirst(String value) {
    if (value.length() > 1) {
      return value.substring(0, 1).toLowerCase() + value.substring(1);
    }
    return value;
  }
}
