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

package org.openbravo.dal.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.hibernate.type.YesNoType;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.session.DalUUIDGenerator;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.util.Check;

/**
 * This class is responsible for generating the Hibernate mapping for the tables and entities within
 * OpenBravo. It uses the runtime model provided by {@link ModelProvider ModelProvider}.
 * 
 * @author mtaal
 */

public class DalMappingGenerator implements OBSingleton {
  private static final Logger log = Logger.getLogger(DalMappingGenerator.class);

  private final static String HIBERNATE_FILE_PROPERTY = "hibernate.hbm.file";

  private final static String TEMPLATE_FILE = "template.hbm.xml";
  private final static String MAIN_TEMPLATE_FILE = "template_main.hbm.xml";
  // private final static char TAB = '\t';
  private final static String TAB2 = "\t\t";
  private final static String TAB3 = "\t\t\t";
  private final static char NL = '\n';

  private static DalMappingGenerator instance = new DalMappingGenerator();

  public static synchronized DalMappingGenerator getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(DalMappingGenerator.class);
    }
    return instance;
  }

  public static synchronized void setInstance(DalMappingGenerator dalMappingGenerator) {
    instance = dalMappingGenerator;
  }

  private String templateContents;

  /**
   * Generates the Hibernate mapping for {@link Entity Entities} in the system. The generated
   * Hibernate mapping is returned as a String.
   * 
   * @return the generated Hibernate mapping (corresponds to what is found in a hbm.xml file)
   */
  public String generateMapping() {
    final ModelProvider mp = ModelProvider.getInstance();
    final StringBuilder sb = new StringBuilder();
    for (final Entity e : mp.getModel()) {
      final String entityMapping = generateMapping(e);
      sb.append(entityMapping);
    }
    final String mainTemplate = readFile(MAIN_TEMPLATE_FILE);
    final String result = mainTemplate.replace("contentPlaceholder", sb.toString());

    if (log.isDebugEnabled()) {
      log.debug(result);
    }

    final String hibernateFileLocation = OBPropertiesProvider.getInstance()
        .getOpenbravoProperties().getProperty(HIBERNATE_FILE_PROPERTY);

    if (hibernateFileLocation != null) {
      try {
        final File f = new File(hibernateFileLocation);
        if (f.exists()) {
          f.delete();
        }
        f.createNewFile();
        final FileWriter fw = new FileWriter(f);
        fw.write(result);
        fw.close();
      } catch (final Exception e) {
        // ignoring exception for the rest
        log.error("Exception when saving hibernate mapping in " + hibernateFileLocation, e);
      }
    }
    return result;
  }

  private String generateMapping(Entity entity) {
    String hbm = getClassTemplateContents();
    hbm = hbm.replaceAll("mappingName", entity.getName());
    hbm = hbm.replaceAll("tableName", entity.getTableName());
    hbm = hbm.replaceAll("ismutable", entity.isMutable() + "");

    if (entity.getMappingClass() != null) {
      hbm = hbm.replaceAll("<class", "<class name=\"" + entity.getClassName() + "\" ");
    }

    // create the content by first getting the id
    final StringBuilder content = new StringBuilder();

    if (entity.getMappingClass() == null) {
      content.append("<tuplizer entity-mode=\"dynamic-map\" "
          + "class=\"org.openbravo.dal.core.OBDynamicTuplizer\"/>\n\n");
    } else {
      content.append("<tuplizer entity-mode=\"pojo\" "
          + "class=\"org.openbravo.dal.core.OBTuplizer\"/>\n\n");
    }

    if (entity.hasCompositeId()) {
      content.append(generateCompositeID(entity));
    } else {
      content.append(generateStandardID(entity));
    }
    content.append(NL);

    // now handle the standard columns
    for (final Property p : entity.getProperties()) {
      if (p.isId()) { // && p.isPrimitive()) { // handled separately
        continue;
      }

      if (p.isPartOfCompositeId()) {
        continue;
      }

      if (p.isOneToMany()) {
        content.append(generateOneToMany(p));
      } else {
        if (p.isPrimitive()) {
          content.append(generatePrimitiveMapping(p));
        } else {
          content.append(generateReferenceMapping(p));
        }
      }
    }

    if (entity.isActiveEnabled()) {
      content.append(getActiveFilter());
    }

    hbm = hbm.replace("content", content.toString());
    return hbm;
  }

  private String getActiveFilter() {
    return "<filter name=\"activeFilter\" condition=\":activeParam = isActive\"/>\n";
  }

  private String generatePrimitiveMapping(Property p) {
    if (p.getHibernateType() == Object.class) {
      return "";
    }
    final StringBuffer sb = new StringBuffer();
    sb.append(TAB2 + "<property name=\"" + p.getName() + "\"");
    sb.append(getAccessorAttribute());
    String type;
    if (p.getHibernateType().isArray()) {
      type = p.getHibernateType().getComponentType().getName() + "[]";
    } else {
      type = p.getHibernateType().getName();
    }
    if (p.isBoolean()) {
      type = YesNoType.class.getName(); // "yes_no";
    }
    sb.append(" type=\"" + type + "\"");

    sb.append(" column=\"" + p.getColumnName() + "\"");

    if (p.isMandatory()) {
      sb.append(" not-null=\"true\"");
    }

    // ignoring isUpdatable for now as this is primarily used
    // for ui and not for background processes
    // if (!p.isUpdatable() || p.isInactive()) {

    if (p.isInactive() || p.getEntity().isView()) {
      sb.append(" update=\"false\"");
    }
    if (p.isInactive() || p.getEntity().isView()) {
      sb.append(" insert=\"false\"");
    }

    sb.append("/>" + NL);
    return sb.toString();
  }

  private String generateReferenceMapping(Property p) {
    if (p.getTargetEntity() == null) {
      return "<!-- Unsupported reference type " + p.getName() + " of entity "
          + p.getEntity().getName() + "-->" + NL;
    }
    final StringBuffer sb = new StringBuffer();
    if (p.isOneToOne()) {
      final String name = p.getSimpleTypeName().substring(0, 1).toLowerCase()
          + p.getSimpleTypeName().substring(1);
      sb.append(TAB2 + "<one-to-one name=\"" + name + "\"");
      sb.append(" constrained=\"true\"");
    } else {
      sb.append(TAB2 + "<many-to-one name=\"" + p.getName() + "\" column=\"" + p.getColumnName()
          + "\""); // cascade=\
      // "save-update\"
      if (p.isMandatory()) {
        sb.append(" not-null=\"true\"");
      }

      // language is always loaded explicitly by Hibernate because it is a non-pk
      // association, eager fetch with the parent then..
      // disabled for now for later study
      // if (p.getTargetEntity().getName().equals("ADLanguage")) {
      // sb.append(" fetch=\"join\" ");
      // }

      if (p.isInactive() || p.getEntity().isView()) {
        sb.append(" update=\"false\"");
      }
      if (p.isInactive() || p.getEntity().isView()) {
        sb.append(" insert=\"false\"");
      }
    }
    // sb.append(" cascade=\"save-update\"");

    // to prevent cascade errors that the parent is saved after the child
    // this is handled by the DataImportService.insertObjectGraph
    // but other specific code needs it
    if (p.isParent() && p.isMandatory()) {
      sb.append(" cascade=\"persist\"");
    }

    sb.append(" entity-name=\"" + p.getTargetEntity().getName() + "\"");

    sb.append(getAccessorAttribute());

    if (p.getReferencedProperty() != null && !p.getReferencedProperty().isId()) {
      sb.append(" property-ref=\"" + p.getReferencedProperty().getName() + "\"");
    }

    sb.append("/>" + NL);
    return sb.toString();
  }

  private String generateOneToMany(Property p) {
    final StringBuffer sb = new StringBuffer();
    StringBuffer order = new StringBuffer();
    if (p.isOneToMany()) {
      if (p.getTargetEntity().getOrderByProperties().size() > 0) {
        order.append("order-by=\"");
        for (final Property po : p.getTargetEntity().getOrderByProperties()) {
          order.append(po.getColumnName() + " ASC,");
        }
        order = order.replace(order.length() - 1, order.length(), "");
        order.append("\"");
      }

      String mutable = "";
      String cascade = "";
      if (p.isChild()) {
        cascade = " cascade=\"all,delete-orphan\" ";
      }
      if (p.getEntity().isView() || p.getTargetEntity().isView()) {
        mutable = " mutable=\"false\" ";
        cascade = "";
      }

      sb.append(TAB2 + "<bag name=\"" + p.getName() + "\" " + cascade + order
          + getAccessorAttribute() + mutable + " inverse=\"true\">" + NL);
      sb.append(TAB3 + "<key column=\"" + p.getReferencedProperty().getColumnName() + "\""
          + (p.getReferencedProperty().isMandatory() ? " not-null=\"true\"" : "") + "/>" + NL);
      sb.append(TAB3 + "<one-to-many entity-name=\"" + p.getTargetEntity().getName() + "\"/>" + NL);

      if (p.getTargetEntity().isActiveEnabled()) {
        sb.append(getActiveFilter());
      }
      sb.append(TAB2 + "</bag>" + NL);

    }
    return sb.toString();
  }

  // assumes one primary key column
  private String generateStandardID(Entity entity) {
    Check.isTrue(entity.getIdProperties().size() == 1,
        "Method can only handle primary keys with one column");
    final Property p = entity.getIdProperties().get(0);
    final StringBuffer sb = new StringBuffer();
    sb.append(TAB2 + "<id name=\"" + p.getName() + "\" type=\"string\" " + getAccessorAttribute()
        + " column=\"" + p.getColumnName() + "\" unsaved-value=\"null\">" + NL);
    if (p.getIdBasedOnProperty() != null) {
      sb.append(TAB3 + "<generator class=\"foreign\">" + NL);
      sb.append(TAB2 + TAB2 + "<param name=\"property\">" + p.getIdBasedOnProperty().getName()
          + "</param>" + NL);
      sb.append(TAB3 + "</generator>" + NL);
    } else if (p.isUuid()) {
      sb.append(TAB3 + "<generator class=\"" + DalUUIDGenerator.class.getName() + "\"/>" + NL);
    }
    sb.append(TAB2 + "</id>" + NL);
    return sb.toString();
  }

  private String getAccessorAttribute() {
    return " access=\"" + OBDynamicPropertyHandler.class.getName() + "\"";
  }

  private String generateCompositeID(Entity e) {
    Check.isTrue(e.hasCompositeId(),
        "Method can only handle primary keys with more than one column");
    final StringBuffer sb = new StringBuffer();
    sb.append(TAB2 + "<composite-id name=\"id\" class=\"" + e.getClassName() + "$Id\""
        + getAccessorAttribute() + ">" + NL);
    final Property compId = e.getIdProperties().get(0);
    Check
        .isTrue(compId.isCompositeId(), "Property " + compId + " is expected to be a composite Id");
    for (final Property p : compId.getIdParts()) {
      if (p.isPrimitive()) {
        String type = p.getHibernateType().getName();
        if (boolean.class.isAssignableFrom(p.getHibernateType().getClass())
            || Boolean.class == p.getHibernateType()) {
          type = "yes_no";
        }
        sb.append(TAB3 + "<key-property name=\"" + p.getName() + "\" column=\"" + p.getColumnName()
            + "\" type=\"" + type + "\"/>" + NL);
      } else {
        sb.append(TAB3 + "<key-many-to-one name=\"" + p.getName() + "\" column=\""
            + p.getColumnName() + "\"");
        sb.append(" entity-name=\"" + p.getTargetEntity().getName() + "\"");
        sb.append("/>" + NL);
      }
    }
    sb.append(TAB2 + "</composite-id>" + NL);
    return sb.toString();
  }

  private String getClassTemplateContents() {
    if (templateContents == null) {
      templateContents = readFile(TEMPLATE_FILE);
    }
    return templateContents;
  }

  private String readFile(String fileName) {
    try {
      final InputStreamReader fr = new InputStreamReader(getClass().getResourceAsStream(fileName));
      final BufferedReader br = new BufferedReader(fr);
      try {
        String line;
        final StringBuffer sb = new StringBuffer();
        while ((line = br.readLine()) != null) {
          sb.append(line + "\n");
        }
        return sb.toString();
      } finally {
        br.close();
        fr.close();
      }
    } catch (final IOException e) {
      throw new OBException(e);
    }
  }
}
