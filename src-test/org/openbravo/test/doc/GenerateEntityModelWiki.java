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

package org.openbravo.test.doc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.test.base.BaseTest;

/**
 * Generates the entity model wiki page and its subpages:
 * 
 * http://wiki.openbravo.com/wiki/ERP/2.50/Developers_Guide/Reference/Entity_Model
 * 
 * It uses specific values for COOKIE and TOKEN which need to be obtained by login into the
 * openbravo wiki and then copy the wiki cookie values as one string to the COOKIE static. The TOKEN
 * is the value of the wpEditToken html hidden input which is present in the html of a wiki page in
 * edit mode.
 * 
 * Note this page generation uses two txt files located in the same package, these pages are used as
 * template like files.
 * 
 * @author mtaal
 */
public class GenerateEntityModelWiki extends BaseTest {
  private static final String WIKI_URL = "http://wiki.openbravo.com/wiki/index.php";
  private static final String ENTITY_MODEL_PATH = "ERP/2.50/Developers_Guide/Reference/Entity_Model";
  // TODO: obtain cookies and token in a proper way
  private static final String COOKIE = "__utma=233079192.658990777.1221427045.1241542285.1241594940.349; __utmz=233079192.1241076544.337.76.utmccn=(organic)|utmcsr=google|utmctr=openbravo+wiki+data+access+layer+performance|utmcmd=organic; __utma=112434266.233451159.1222677175.1236249553.1237214839.22; __utmz=112434266.1236249553.21.12.utmccn=(referral)|utmcsr=surveymonkey.com|utmcct=/s.aspx|utmcmd=referral; mediawikicas_mw_UserID=2084; mediawikicas_mw_UserName=Mtaal; __utmc=233079192; mediawikicas_mw__session=g4eur5j3egraer06kur7fveeu4; __utmb=233079192";
  // note that the \ at the end needs to be escaped (so there should be two: \)
  private static final String TOKEN = "f89d091d50dbd8816cb80d2a56e486e3+\\";

  // private static final String OUTPUT = "/tmp";

  private final Map<String, String> cachedTemplates = new HashMap<String, String>();

  public void _testPrint() {
    // ERP/2.50/Developers_Guide/Database_Model/org.openbravo.model.ad.datamodel/AD_Table
    final List<Entity> entities = new ArrayList<Entity>(ModelProvider.getInstance().getModel());
    Collections.sort(entities, new EntityComparator());
    for (Entity entity : entities) {
      System.err.println("[[ERP/2.50/Developers_Guide/Database_Model/" + entity.getPackageName()
          + "/" + entity.getTableName() + " |" + entity.getTableName() + "]]");
    }
  }

  /**
   * Generates the entity model wiki pages and uploads them to the openbravo wiki.
   * 
   * @throws Exception
   */
  public void testGenerateWiki() throws Exception {
    super.setTestAdminContext();
    if (true) {
      final String entityContent = generateModelContent();
      final Map<String, String> content = new HashMap<String, String>();
      content.put("CONTENT", entityContent);
      final String entityPage = readApplyTemplate("entity_model_wiki.txt", content);
      writeToWiki(ENTITY_MODEL_PATH, entityPage);
      // writeFile(OUTPUT, "entity_page.txt", entityPage);
    }

    // write the entities
    int cnt = 0;
    for (Entity entity : ModelProvider.getInstance().getModel()) {
      final Map<String, String> content = new HashMap<String, String>();
      final Table table = getTable(entity.getTableName());
      content.put("CONTENT", generateEntityPage(table, entity));
      content.put("ENTITY_NAME", entity.getName());
      content.put("ENTITY_HELP", (table.getHelpComment() != null ? table.getHelpComment() : ""));
      content.put("SOURCE", getJavaSourceCode(entity.getClassName()));
      content.put("BACK_TO_ENTITY_MODEL", "ERP/2.50/Developers_Guide/Reference/Entity_Model#"
          + entity.getName());
      content.put(
          "TABLE_LINK",
          getLink("ERP/2.50/Developers_Guide/Database_Model/" + entity.getPackageName() + "/"
              + entity.getTableName(), "To the database table (" + entity.getTableName()
              + ") of this entity."));

      final String result = readApplyTemplate("entity_wiki.txt", content);
      writeToWiki(ENTITY_MODEL_PATH + "/" + entity.getName(), result);
      System.err.println(cnt++);
      // writeFile(OUTPUT, entity.getName(), result);
    }
  }

  private String generateEntityPage(Table table, Entity entity) {
    final StringBuilder sb = new StringBuilder();
    for (Property property : entity.getProperties()) {
      final Column column;
      if (property.getColumnName() != null) {
        column = getColumn(table, property.getColumnName());
      } else {
        column = null;
      }

      if (sb.length() > 0) {
        sb.append("\n\n");
      }
      sb.append("|-");
      sb.append("\n|");
      // property name
      sb.append(property.getName() + (property.isId() ? "<sup>*</sup>" : "")
          + (property.isIdentifier() ? "<sup>#</sup>" : ""));
      if (property.isInactive()) {
        sb.append(" '''(inactive)'''");
      }

      sb.append(" || ");

      // table name
      if (column != null) {
        sb.append(getLink("ERP/2.50/Developers_Guide/Database_Model/" + entity.getPackageName()
            + "/" + entity.getTableName() + "#" + column.getName(), column.getDBColumnName()));
      } else {
        sb.append("");
      }

      sb.append(" || ");

      // mandatory
      {
        final StringBuilder constraints = new StringBuilder();
        if (property.isMandatory()) {
          if (constraints.length() > 0) {
            constraints.append("<br/>");
          }
          constraints.append("Mandatory");
        }
        if (property.getMinValue() != null) {
          if (constraints.length() > 0) {
            constraints.append("<br/>");
          }
          constraints.append("Min: " + property.getMinValue());
        }
        if (property.getMaxValue() != null) {
          if (constraints.length() > 0) {
            constraints.append("<br/>");
          }
          System.err.println("Property " + property);
          constraints.append("Max: " + property.getMaxValue());
        }
        if (property.getFieldLength() > 0 && property.getPrimitiveType() == String.class) {
          if (constraints.length() > 0) {
            constraints.append("<br/>");
          }
          constraints.append("Max Length: " + property.getFieldLength());
        }
        sb.append(constraints.toString());
      }
      sb.append(" || ");

      // the type
      if (property.isPrimitive()) {
        sb.append(property.getTypeName());
      } else {
        if (property.isOneToMany()) {
          sb.append("List of ");
        }
        sb.append(getLink("ERP/2.50/Developers_Guide/Reference/Entity_Model/"
            + property.getTargetEntity().getName(), property.getTargetEntity().getName()));
      }

      sb.append(" || ");

      if (column != null && column.getHelpComment() != null) {
        sb.append(column.getHelpComment());
      }
    }
    return sb.toString();
  }

  private String generateModelContent() {
    final StringBuilder sb = new StringBuilder();

    final List<Entity> entities = new ArrayList<Entity>(ModelProvider.getInstance().getModel());
    Collections.sort(entities, new EntityComparator());
    for (Entity entity : entities) {
      if (sb.length() > 0) {
        sb.append("\n\n");
      }
      sb.append("|-");
      sb.append("\n|");
      // entity name
      sb.append("<span id=\""
          + entity.getName()
          + "\"></span>"
          + getLink("ERP/2.50/Developers_Guide/Reference/Entity_Model/" + entity.getName(),
              entity.getName()));

      sb.append(" || ");

      // table name
      sb.append(getLink("ERP/2.50/Developers_Guide/Database_Model/" + entity.getPackageName() + "/"
          + entity.getTableName(), entity.getTableName()));

      sb.append(" || ");

      // java code
      sb.append(getLink("ERP/2.50/Developers_Guide/Reference/Entity_Model/" + entity.getName()
          + "#Java_Entity_Class", entity.getClassName()));
      //
      // sb.append(" || ");
      //
      // final Table table = getTable(entity.getTableName());
      // if (table.getHelpComment() != null) {
      // sb.append(table.getHelpComment());
      // }
    }
    return sb.toString();
  }

  private Table getTable(String tableName) {
    final OBCriteria<Table> tables = OBDal.getInstance().createCriteria(Table.class);
    tables.add(Restrictions.eq(Table.PROPERTY_DBTABLENAME, tableName));
    return tables.list().get(0);
  }

  private Column getColumn(Table table, String columnName) {
    final OBCriteria<Column> cs = OBDal.getInstance().createCriteria(Column.class);
    cs.add(Restrictions.and(Restrictions.eq(Column.PROPERTY_TABLE, table),
        Restrictions.eq(Column.PROPERTY_DBCOLUMNNAME, columnName)));

    if (cs.list().size() == 0) {
      return null;
    }
    return cs.list().get(0);
  }

  private String getLink(String path, String name) {
    return "[[" + path + " | " + name + "]]";
  }

  private String readApplyTemplate(String template, Map<String, String> content) throws Exception {
    String fileContent = cachedTemplates.get(template);
    if (fileContent == null) {
      final File file = new File(this.getClass().getResource(template).toURI());
      fileContent = readFile(file);
      cachedTemplates.put(template, fileContent);
    }

    for (String key : content.keySet()) {
      fileContent = fileContent.replace(key, content.get(key));
    }
    return fileContent;
  }

  private String readFile(File file) throws Exception {
    final FileReader fileReader = new FileReader(file);
    final BufferedReader reader = new BufferedReader(fileReader);
    String line;
    final StringBuilder sb = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(line);
    }
    reader.close();
    fileReader.close();
    return sb.toString();
  }

  // private void writeFile(String directory, String fileName, String content) throws Exception {
  // final File file = new File(directory, fileName);
  // if (file.exists()) {
  // file.delete();
  // }
  // file.createNewFile();
  // final FileWriter fw = new FileWriter(file);
  // fw.write(content);
  // fw.close();
  // }

  private class EntityComparator implements Comparator<Entity> {

    @Override
    public int compare(Entity o1, Entity o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }

  // private class EntityTableNameComparator implements Comparator<Entity> {
  //
  // @Override
  // public int compare(Entity o1, Entity o2) {
  // return o1.getTableName().compareTo(o2.getTableName());
  // }
  // }

  private String getJavaSourceCode(String className) throws Exception {
    final String srcPath = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .get("source.path");
    final File sourceFile = new File(srcPath, "src-gen/" + className.replaceAll("\\.", "/")
        + ".java");
    return readFile(sourceFile);
  }

  private void writeToWiki(String page, String content) throws Exception {
    String contentStr = "wpStarttime=20090504082750&wpEdittime=20090504082750&wpEditToken="
        + URLEncoder.encode(TOKEN, "utf-8");
    contentStr += "&wpTextbox1=" + content;

    URL url = new URL(WIKI_URL + "?action=submit&title=" + page);
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
    urlConn.setRequestProperty("Cookie", COOKIE);

    urlConn.setRequestProperty("Keep-Alive", "300");
    urlConn.setRequestProperty("Connection", "keep-alive");
    urlConn.setRequestMethod("POST");
    urlConn.setDoInput(true);
    urlConn.setDoOutput(true);
    urlConn.setUseCaches(false);
    urlConn.setAllowUserInteraction(false);

    urlConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
    urlConn.setRequestProperty("Content-length", "" + contentStr.length());

    DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());

    System.out.println("\n" + "Writing: " + page);
    out.writeBytes(contentStr);
    out.flush();
    out.close();
    // get input connection
    BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
    // String line;
    // while ((line = in.readLine()) != null) {
    // // System.err.println(line);
    // }
    in.close();
  }
}
