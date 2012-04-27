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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.domaintype.BaseDomainType;
import org.openbravo.base.model.domaintype.ForeignKeyDomainType;
import org.openbravo.base.model.domaintype.OneToManyDomainType;
import org.openbravo.base.model.domaintype.StringDomainType;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.session.UniqueConstraintColumn;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.CheckException;
import org.openbravo.database.ConnectionProviderImpl;

/**
 * Builds the Runtime model base on the data model (application dictionary: table, column,
 * reference, etc). Makes the runtime model (Entity and Property) available to the rest of the
 * system.
 * 
 * @see Entity
 * @see Property
 * @see Table
 * @see Column
 * 
 * @author iperdomo
 * @author mtaal
 */

public class ModelProvider implements OBSingleton {
  private static final Logger log = Logger.getLogger(ModelProvider.class);

  private static ModelProvider instance;
  private List<Entity> model = null;
  private List<Table> tables = null;
  private HashMap<String, Table> tablesByTableName = null;
  private Map<String, RefTable> refTableMap = new HashMap<String, RefTable>();
  private Map<String, RefSearch> refSearchMap = new HashMap<String, RefSearch>();
  private HashMap<String, Entity> entitiesByName = null;
  private HashMap<String, Entity> entitiesByClassName = null;
  private HashMap<String, Entity> entitiesByTableName = null;
  private HashMap<String, Entity> entitiesByTableId = null;
  private HashMap<String, Reference> referencesById = null;
  // a list because for small numbers a list is faster than a hashmap
  private List<Entity> entitiesWithTreeType = null;
  private List<Module> modules;
  private Session initsession;

  /**
   * Returns the singleton instance providing the ModelProvider functionality.
   * 
   * @return the ModelProvider instance
   */
  public static synchronized ModelProvider getInstance() {
    // set in a localInstance to prevent threading issues when
    // reseting it in setInstance()
    ModelProvider localInstance = instance;
    if (localInstance == null) {
      localInstance = OBProvider.getInstance().get(ModelProvider.class);
      instance = localInstance;
    }
    return localInstance;
  }

  /**
   * Makes it possible to override the default ModelProvider with a custom implementation.
   * 
   * @param instance
   *          the custom ModelProvider
   */
  public static synchronized void setInstance(ModelProvider instance) {
    ModelProvider.instance = instance;
  }

  /**
   * Creates a new ModelProvider, initializes it and sets it in the instance here.
   */
  public static void refresh() {
    try {
      OBProvider.getInstance().removeInstance(ModelProvider.class);
      final ModelProvider localProvider = OBProvider.getInstance().get(ModelProvider.class);
      setInstance(localProvider);
      // initialize it
      localProvider.getModel();
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * The list of Entities created on the basis of the Application Dictionary. The main entry point
   * for retrieving the in-memory model. This method will initialize the in-memory model when it is
   * called for the first time.
   * 
   * @return the list Entities
   */
  public List<Entity> getModel() {
    if (model == null) {
      initialize();
    }

    return model;
  }

  private void initialize() {
    log.info("Building runtime model");
    // Caching model (tables, table-references, search-references,
    // list-references)
    // Changed to use the SessionHandler directly because the dal
    // layer uses the ModelProvider, so otherwise there will be a
    // cyclic relation.
    final ModelSessionFactoryController sessionFactoryController = new ModelSessionFactoryController();
    initializeReferenceClasses(sessionFactoryController);
    initsession = sessionFactoryController.getSessionFactory().openSession();
    final Transaction tx = initsession.beginTransaction();
    try {
      log.debug("Read model from db");

      tables = list(initsession, Table.class);
      Collections.sort(tables, new Comparator<Table>() {
        public int compare(Table t1, Table t2) {
          return t1.getName().compareTo(t2.getName());
        }
      });

      referencesById = new HashMap<String, Reference>();
      final List<Reference> references = list(initsession, Reference.class);
      for (Reference reference : references) {
        reference.getDomainType().setModelProvider(this);
        reference.getDomainType().initialize();
        referencesById.put(reference.getId(), reference);
      }
      // read the columns in one query and assign them to the table
      final List<Column> cols = readColumns(initsession);
      assignColumnsToTable(cols);

      // reading will automatically link the reftable, refsearch and reflist
      // to the reference
      final List<RefTable> refTables = list(initsession, RefTable.class);
      final List<RefSearch> refSearches = list(initsession, RefSearch.class);
      list(initsession, RefList.class);
      modules = retrieveModules(initsession);
      tables = removeInvalidTables(tables);

      // maintained for api support of the
      // getColumnByReference method
      for (final RefTable rt : refTables) {
        refTableMap.put(rt.getId(), rt);
      }
      for (final RefSearch rs : refSearches) {
        // note mapped by reference id
        refSearchMap.put(rs.getReference(), rs);
      }
      // see remark above

      // this map stores the mapped tables
      tablesByTableName = new HashMap<String, Table>();
      for (final Table t : tables) {
        // tables are stored case insensitive!
        tablesByTableName.put(t.getTableName().toLowerCase(), t);
      }

      log.debug("Setting referencetypes for columns");
      for (final Table t : tablesByTableName.values()) {
        t.setReferenceTypes(ModelProvider.instance);
      }
      //
      // log.debug("Setting List Values for columns");
      // for (final RefList rl : refList) {
      // rl.setAllowedValue();
      // }

      model = new ArrayList<Entity>();
      entitiesByName = new HashMap<String, Entity>();
      entitiesByClassName = new HashMap<String, Entity>();
      entitiesByTableName = new HashMap<String, Entity>();
      entitiesByTableId = new HashMap<String, Entity>();
      entitiesWithTreeType = new ArrayList<Entity>();
      for (final Table t : tables) {
        log.debug("Building model for table " + t.getTableName());
        final Entity e = new Entity();
        e.initialize(t);
        model.add(e);
        entitiesByClassName.put(e.getClassName(), e);
        entitiesByName.put(e.getName(), e);
        entitiesByTableName.put(t.getTableName().toUpperCase(), e);
        entitiesByTableId.put(t.getId(), e);
        if (e.getTreeType() != null) {
          entitiesWithTreeType.add(e);
        }
      }

      // in the second pass set all the referenceProperties
      // and targetEntities
      // uses global member tablesByTableName.
      // Obtains list of columns candidate to be translated, to be handled after setting properties
      // in parent entities.
      List<Column> translatableColumns = setReferenceProperties();

      // add virtual property for the case that the
      // id property is also a reference (a foreign key)
      // In this case hibernate requires two mappings
      // one for the id (a string) and for the reference
      // in addition the id generation strategy should be set
      // to foreign.
      log.debug("Setting virtual property for many-to-one id's");
      setVirtualPropertiesForReferenceId();

      buildUniqueConstraints(initsession, sessionFactoryController);

      final Map<String, Boolean> colMandatories = getColumnMandatories(initsession,
          sessionFactoryController);

      // initialize the name and also set the mandatory value on the basis
      // of the real not-null in the database!
      for (final Entity e : model) {
        for (final Property p : e.getProperties()) {
          if (!p.isOneToMany()) {
            p.initializeName();
            // don't do mandatory value setting for views
            if (!e.isView() && p.getColumnName() != null) {
              final Boolean mandatory = colMandatories.get(createColumnMandatoryKey(
                  e.getTableName(), p.getColumnName()));
              if (mandatory != null) {
                p.setMandatory(mandatory);
              } else {
                log.warn("Column " + p + " mandatory setting not found in the database metadata. "
                    + "A cause can be that the column does not exist in the database schema");
              }
            }
          }
        }
        // dumpPropertyNames(e);
      }
      for (final Entity e : model) {
        // add virtual property in the parent table based on
        // isParent columns
        createPropertyInParentEntity(e);
      }

      for (final Entity e : model) {
        for (final Property p : e.getProperties()) {
          if (p.isOneToMany()) {
            p.initializeName();
          }
        }
      }

      setTranslatableColumns(translatableColumns);

    } finally {
      log.debug("Closing session and sessionfactory used during model read");
      tx.commit();
      initsession.close();
      sessionFactoryController.getSessionFactory().close();
    }
    clearLists();
  }

  private void setTranslatableColumns(List<Column> translatableColumns) {
    for (Column c : translatableColumns) {
      final Entity translationEntity = getEntityByTableName(c.getTable().getTableName() + "_Trl");

      Property translationProperty = null;
      if (translationEntity != null) {
        translationProperty = translationEntity.getPropertyByColumnName(c.getColumnName());
      }
      final Property thisProp = c.getProperty();
      thisProp.setTranslatable(translationProperty);
    }
  }

  /**
   * This method uses a normal JDBC connection to retrieve the classes of the references. These
   * classes will be instantiated and if they implement the correct interface, they will be added to
   * the SessionFactoryController
   */
  private void initializeReferenceClasses(ModelSessionFactoryController sessionFactoryController) {
    ConnectionProviderImpl con = null;
    Connection connection = null;
    try {
      con = new ConnectionProviderImpl(OBPropertiesProvider.getInstance().getOpenbravoProperties());
      connection = con.getConnection();
      PreparedStatement ps = connection
          .prepareStatement("select distinct model_impl from ad_reference where model_impl is not null");
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        String classname = rs.getString(1);
        Class<?> myClass = Class.forName(classname);
        if (org.openbravo.base.model.domaintype.BaseDomainType.class.isAssignableFrom(myClass)) {
          BaseDomainType classInstance = (BaseDomainType) myClass.newInstance();
          for (Class<?> aClass : classInstance.getClasses()) {
            sessionFactoryController.addAdditionalClasses(aClass);
          }
        }
      }
    } catch (Exception e) {
      throw new OBException("Failed to load reference classes", e);
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        // do nothing
      }
      try {
        if (con != null) {
          con.destroy();
        }
      } catch (Exception e) {
        // do nothing
      }
    }

  }

  /**
   * Returns list of tables known in the dal in memory model.
   * 
   * This excludes i.e. tables which do not have any column defined with iskey='Y'
   * 
   * @return list of tables known by dal in no particular stable order
   */
  public List<Table> getTables() {
    return new ArrayList<Table>(tablesByTableName.values());
  }

  /**
   * @return the last time that one of the relevant Application Dictionary objects was modified.
   *         Relevant AD objects are: Table, Column, Reference, RefList, RefSearch, RefTable,
   *         Module, Package.
   */
  public long computeLastUpdateModelTime() {
    final SessionFactoryController sessionFactoryController = new ModelSessionFactoryController();
    final Session session = sessionFactoryController.getSessionFactory().openSession();
    final Transaction tx = session.beginTransaction();
    try {
      // compute the last updated time
      long currentLastTimeUpdated = 0;
      currentLastTimeUpdated = getLastUpdated(Table.class, currentLastTimeUpdated, session);
      currentLastTimeUpdated = getLastUpdated(Column.class, currentLastTimeUpdated, session);
      currentLastTimeUpdated = getLastUpdated(RefTable.class, currentLastTimeUpdated, session);
      currentLastTimeUpdated = getLastUpdated(RefSearch.class, currentLastTimeUpdated, session);
      currentLastTimeUpdated = getLastUpdated(RefList.class, currentLastTimeUpdated, session);
      currentLastTimeUpdated = getLastUpdated(Module.class, currentLastTimeUpdated, session);
      currentLastTimeUpdated = getLastUpdated(Package.class, currentLastTimeUpdated, session);
      currentLastTimeUpdated = getLastUpdated(Reference.class, currentLastTimeUpdated, session);
      return currentLastTimeUpdated;
    } finally {
      tx.commit();
      session.close();
      sessionFactoryController.getSessionFactory().close();
    }
  }

  private <T extends ModelObject> long getLastUpdated(Class<T> clz, long currentLastTime,
      Session session) {
    final ModelObject mo = queryLastUpdateObject(session, clz);
    if (mo.getUpdated().getTime() > currentLastTime) {
      return mo.getUpdated().getTime();
    }
    return currentLastTime;
  }

  private <T extends ModelObject> T queryLastUpdateObject(Session session, Class<T> clazz) {
    final Criteria c = session.createCriteria(clazz);
    c.addOrder(Order.desc("updated"));
    c.setMaxResults(1);
    @SuppressWarnings("unchecked")
    final List<T> list = c.list();
    if (list.size() == 0) {
      throw new OBException("No instances of " + clazz.getName()
          + " in the database, has the database been created and filled with data?");
    }
    return (T) list.get(0);

  }

  // clears some in-memory lists to save memory
  private void clearLists() {
    tables = null;
  }

  @SuppressWarnings("unchecked")
  private List<Column> readColumns(Session session) {
    final Criteria c = session.createCriteria(Column.class);
    c.addOrder(Order.asc("position"));
    return c.list();
  }

  private void assignColumnsToTable(List<Column> cols) {
    for (final Column column : cols) {
      final Table table = column.getTable();
      table.getColumns().add(column);
    }
  }

  private void setVirtualPropertiesForReferenceId() {

    for (final Entity e : entitiesByName.values()) {
      if (e.getIdProperties().size() == 1 && !e.getIdProperties().get(0).isPrimitive()) {
        createIdReferenceProperty(e);
      } else if (e.getIdProperties().size() > 1) {
        createCompositeId(e);
      }
    }
  }

  private List<Column> setReferenceProperties() {
    log.debug("Setting reference property");
    // uses global member tablesByTableName

    List<Column> translatableColumns = new ArrayList<Column>();
    for (final Table t : tablesByTableName.values()) {
      for (final Column c : t.getColumns()) {
        if (!c.isPrimitiveType()) {
          final Property thisProp = c.getProperty();
          log.debug("Setting targetEntity and reference Property for " + thisProp);
          final Column thatColumn = c.getReferenceType();
          if (thatColumn == null) {
            if (!OBPropertiesProvider.isFriendlyWarnings()) {
              log.error("Property "
                  + thisProp
                  + " is mapped incorrectly, there is no referenced column for it, removing from the mapping");
            }
            thisProp.getEntity().getProperties().remove(thisProp);
            if (thisProp.getEntity().getIdProperties().remove(thisProp)) {
              Check.fail("Incorrect mapping for property " + thisProp
                  + " which is an id, mapping fails, stopping here");
            }
            thisProp.getEntity().getIdentifierProperties().remove(thisProp);
            continue;
          }

          // can occur if the column is read and returned through a
          // module provided Domain Type
          if (thatColumn.getProperty() == null) {
            final Entity entity = getEntityByTableName(thatColumn.getTable().getTableName());
            Check.isNotNull(entity, "No entity found using tablename "
                + thatColumn.getTable().getTableName() + " for column " + thatColumn);
            final Property property = entity.getPropertyByColumnName(thatColumn.getColumnName());
            thatColumn.setProperty(property);
          }

          // targetentity is set within setReferencedProperty
          final Property thatProperty = thatColumn.getProperty();
          thisProp.setReferencedProperty(thatProperty);
        }

        if (c.isTranslatable()) {
          translatableColumns.add(c);
        }
      }
    }

    return translatableColumns;

  }

  private List<Table> removeInvalidTables(List<Table> allTables) {
    final List<Table> toRemove = new ArrayList<Table>();
    final List<Table> localTables = allTables;
    for (final Table t : localTables) {
      // taking into account inactive tables for now...

      // if (false && !t.isActive()) {
      // log.debug("Table " + t.getName() + " is not active ignoring it");
      // toRemove.add(t);
      // continue;
      // }

      if (t.getPrimaryKeyColumns().size() == 0) {
        // note, after this issue is solved:
        // https://issues.openbravo.com/view.php?id=14696
        // then also log.warn for views
        if (!t.isView()) {
          log.warn("Ignoring table/view " + t.getName() + " because it has no primary key columns");
        }
        toRemove.add(t);
        continue;
      }
    }
    allTables.removeAll(toRemove);
    return tables;
  }

  private Map<String, Boolean> getColumnMandatories(Session session,
      SessionFactoryController sfController) {
    final String columnQry = sfController.getColumnMetadataQuery();

    final Map<String, Boolean> result = new HashMap<String, Boolean>();
    final SQLQuery sqlQuery = session.createSQLQuery(columnQry);
    for (final Object row : sqlQuery.list()) {
      final Object[] vals = (Object[]) row;
      final String key = createColumnMandatoryKey(vals[0], vals[1]);
      if (vals[2] instanceof String) {
        // note the string contains Y or N
        result.put(key, ((String) vals[2]).toUpperCase().equals("N"));
      } else {
        result.put(key, (Boolean) vals[2]);
      }
    }
    return result;
  }

  private String createColumnMandatoryKey(Object tableName, Object columnName) {
    return tableName.toString().toUpperCase() + ";" + columnName.toString().toUpperCase();
  }

  // Build unique constraints
  private void buildUniqueConstraints(Session session,
      SessionFactoryController sessionFactoryController) {
    final List<UniqueConstraintColumn> uniqueConstraintColumns = getUniqueConstraintColumns(
        session, sessionFactoryController);
    Entity entity = null;
    UniqueConstraint uniqueConstraint = null;
    for (final UniqueConstraintColumn uniqueConstraintColumn : uniqueConstraintColumns) {
      // get the entity
      if (entity == null
          || !entity.getTableName().equalsIgnoreCase(uniqueConstraintColumn.getTableName())) {
        entity = getEntityByTableName(uniqueConstraintColumn.getTableName());
        uniqueConstraint = null;
      }
      if (entity == null) {
        continue;
      }

      // the uniqueconstraint
      if (uniqueConstraint == null
          || !uniqueConstraint.getName().equalsIgnoreCase(
              uniqueConstraintColumn.getUniqueConstraintName())) {
        // note uniqueconstraint should be set to null, because the
        // for loop my not find another one
        uniqueConstraint = null;
        // get a new one, walk through all of them of the entity
        for (final UniqueConstraint uc : entity.getUniqueConstraints()) {
          if (uc.getName().equalsIgnoreCase(uniqueConstraintColumn.getUniqueConstraintName())) {
            uniqueConstraint = uc;
            break;
          }
        }
      }
      if (uniqueConstraint == null) {
        uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setEntity(entity);
        uniqueConstraint.setName(uniqueConstraintColumn.getUniqueConstraintName());
        entity.getUniqueConstraints().add(uniqueConstraint);
      }
      uniqueConstraint.addPropertyForColumn(uniqueConstraintColumn.getColumnName());
    }

    // dumpUniqueConstraints();
  }

  // returns a list of uniqueconstraint columns containing all
  // uniqueconstraints from the database
  private List<UniqueConstraintColumn> getUniqueConstraintColumns(Session session,
      SessionFactoryController sessionFactoryController) {
    final List<UniqueConstraintColumn> result = new ArrayList<UniqueConstraintColumn>();
    final SQLQuery sqlQuery = session.createSQLQuery(sessionFactoryController
        .getUniqueConstraintQuery());
    for (final Object row : sqlQuery.list()) {
      // cast to an array of strings!
      // 0: tablename
      // 1: columnname
      // 2: uniqueconstraintname
      final Object[] values = (Object[]) row;
      Check.isTrue(values.length == 3,
          "Unexpected value length for constraint query, should be 3, but is " + values.length);
      final UniqueConstraintColumn uniqueConstraintColumn = new UniqueConstraintColumn();
      uniqueConstraintColumn.setTableName((String) values[0]);
      uniqueConstraintColumn.setColumnName((String) values[1]);
      uniqueConstraintColumn.setUniqueConstraintName((String) values[2]);
      result.add(uniqueConstraintColumn);
    }
    return result;
  }

  // expects that there is only one property
  private void createIdReferenceProperty(Entity e) {
    Check.isTrue(e.getIdProperties().size() == 1 && !e.getIdProperties().get(0).isPrimitive(),
        "Expect one id property for the entity and it should be a reference type");
    final Property idProperty = e.getIdProperties().get(0);
    log.debug("Handling many-to-one reference for " + idProperty);
    Check.isTrue(e.getIdProperties().size() == 1,
        "Foreign-key id-properties are only handled if there is one in an entity " + e.getName());
    // create a reference property
    final Property newProp = new Property();
    newProp.setEntity(e);
    newProp.setId(false);
    newProp.setIdentifier(idProperty.isIdentifier());
    newProp.setMandatory(true);
    newProp.setDomainType(idProperty.getDomainType());
    newProp.setColumnName(idProperty.getColumnName());
    newProp.setColumnId(idProperty.getColumnId());
    newProp.setParent(idProperty.isParent());
    newProp.setTargetEntity(idProperty.getTargetEntity());
    newProp.setReferencedProperty(idProperty.getTargetEntity().getIdProperties().get(0));
    newProp.setOneToOne(true);

    // the name is the name of the class of the target without
    // the package part and with the first character lowercased
    final String propName = idProperty.getSimpleTypeName().substring(0, 1).toLowerCase()
        + idProperty.getSimpleTypeName().substring(1);
    newProp.setName(propName);
    e.addProperty(newProp);

    // and change the old id property to a primitive one
    // this assumes that the column in the target entity is itself
    // not a foreign key!
    final Property targetIdProp = idProperty.getTargetEntity().getIdProperties().get(0);
    Check
        .isTrue(
            targetIdProp.isPrimitive(),
            "Entity "
                + e
                + ", The ID property of the referenced class should be primitive, an other case is not supported");
    idProperty.setDomainType(targetIdProp.getDomainType());
    idProperty.setIdBasedOnProperty(newProp);
    idProperty.setIdentifier(false);
    idProperty.setParent(false);
    idProperty.setTargetEntity(null);
  }

  private void createCompositeId(Entity e) {
    Check.isTrue(e.getIdProperties().size() > 1, "Expect that entity " + e
        + " has more than one id property ");
    final Property compId = new Property();
    compId.setEntity(e);
    compId.setId(true);
    compId.setIdentifier(false);
    compId.setMandatory(true);
    final StringDomainType domainType = new StringDomainType();
    domainType.setModelProvider(this);
    compId.setDomainType(domainType);
    compId.setCompositeId(true);
    compId.setName("id");
    // compId is added to the entity below

    final List<Property> toRemove = new ArrayList<Property>();
    for (final Property p : e.getIdProperties()) {
      compId.getIdParts().add(p);
      p.setPartOfCompositeId(true);
      p.setId(false);
      toRemove.add(p);
    }
    e.getIdProperties().removeAll(toRemove);
    Check.isTrue(e.getIdProperties().size() == 0, "There should not be any id properties (entity "
        + e + ") at this point");

    // and now add the id property again
    e.addProperty(compId);
  }

  private void createPropertyInParentEntity(Entity e) {
    try {
      List<Property> props = new ArrayList<Property>(e.getProperties());
      for (final Property p : props) {
        if (!p.isParent()
            && (p.isOneToMany()
                || p.isId()
                || p.getColumnName().equalsIgnoreCase("createdby")
                || p.getColumnName().equalsIgnoreCase("updatedby")
                || p.getReferencedProperty() == null
                || entitiesByClassName.get("org.openbravo.model.ad.system.Client").equals(
                    p.getReferencedProperty().getEntity())
                || entitiesByClassName.get("org.openbravo.model.common.enterprise.Organization")
                    .equals(p.getReferencedProperty().getEntity())
                || entitiesByClassName.get("org.openbravo.model.ad.module.Module").equals(
                    p.getReferencedProperty().getEntity()) || entitiesByClassName.get(
                "org.openbravo.model.ad.system.Language").equals(
                p.getReferencedProperty().getEntity()))) {
          continue;
        }

        if (p.getReferencedProperty() == null) {
          // Log message in case referenced property is null, this will cause a NPE, which is not
          // solved but at least relevant info is shown to fix it in AD
          log.error("Referenced property is null for " + e.getName() + "." + p.getName());
        }

        final Entity parent = p.getReferencedProperty().getEntity();
        createChildProperty(parent, p);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void createChildProperty(Entity parentEntity, Property childProperty) {
    final Property newProp = new Property();
    newProp.setEntity(parentEntity);
    newProp.setId(false);
    newProp.setIdentifier(false);
    newProp.setMandatory(false);
    final OneToManyDomainType domainType = new OneToManyDomainType();
    domainType.setModelProvider(this);
    newProp.setDomainType(domainType);
    newProp.setTargetEntity(childProperty.getEntity());
    newProp.setReferencedProperty(childProperty);
    newProp.setOneToOne(false);
    newProp.setOneToMany(true);
    newProp.setChild(childProperty.isParent());
    parentEntity.addProperty(newProp);
  }

  @SuppressWarnings("unchecked")
  public <T extends Object> List<T> list(Session s, Class<T> clazz) {
    final Criteria c = s.createCriteria(clazz);
    return c.list();
  }

  public List<Module> getModules() {
    return modules;
  }

  @SuppressWarnings("unchecked")
  private List<Module> retrieveModules(Session s) {
    final Criteria c = s.createCriteria(Module.class);
    c.addOrder(Order.asc("seqno"));
    c.add(Restrictions.eq("active", true));
    return c.list();
  }

  /**
   * Return the table using the tableName. If not found then a CheckException is thrown.
   * 
   * @param tableName
   * @return the Table object
   * @throws CheckException
   */
  public Table getTable(String tableName) throws CheckException {
    if (tablesByTableName == null)
      getModel();
    // search case insensitive!
    final Table table = tablesByTableName.get(tableName.toLowerCase());
    if (table == null) {
      if (OBPropertiesProvider.isFriendlyWarnings()) {
        // this error won't be logged...
        throw new IllegalArgumentException("Table: " + tableName
            + " not found in runtime model, is it maybe inactive?");
      } else {
        Check.fail("Table: " + tableName + " not found in runtime model, is it maybe inactive?");
      }
    }
    return table;
  }

  /**
   * Retrieves an Entity using the entityName. If not found then a CheckException is thrown.
   * 
   * @param entityName
   *          the name used for searching the Entity.
   * @return the Entity object
   * @throws CheckException
   */
  public Entity getEntity(String entityName) throws CheckException {
    if (model == null)
      getModel();
    final Entity entity = entitiesByName.get(entityName);
    if (entity == null)
      Check.fail("Mapping name: " + entityName + " not found in runtime model");
    return entity;
  }

  /**
   * Returns an Entity using the table name of the table belonging to the Entity. If no Entity is
   * found then null is returned, no Exception is thrown.
   * 
   * Note: the AD_Table.tablename should be used here, not the AD_Table.name!
   * 
   * @param tableName
   *          the name used to search for the Entity
   * @return the Entity or null if not found
   */
  public Entity getEntityByTableName(String tableName) {
    if (model == null) {
      getModel();
    }
    final Entity entity = entitiesByTableName.get(tableName.toUpperCase());
    // is null for views
    // if (entity == null) {
    // log.warn("Table name: " + tableName + " not found in runtime model");
    // }
    return entity;
  }

  /**
   * Returns an Entity based on the ID of the table belonging to the Entity. If no Entity is found
   * then null is returned, no Exception is thrown.
   * 
   * @param tableId
   *          the ID of the table belonging to the table
   * @return the Entity or null if not found
   */
  public Entity getEntityByTableId(String tableId) {
    if (model == null) {
      getModel();
    }

    final Entity entity = entitiesByTableId.get(tableId);

    if (entity == null) {
      log.warn("Entity not found in runtime model for table id: " + tableId);
    }

    return entity;
  }

  /**
   * Searches for an Entity using the business object class implementing the Entity in the business
   * code. Throws a CheckException if the Entity can not be found.
   * 
   * @param clz
   *          the java class used for the Entity
   * @return the Entity
   * @throws CheckException
   */
  public Entity getEntity(Class<?> clz) throws CheckException {
    if (model == null)
      getModel();
    // TODO: handle subclasses, so if not found then try to find superclass!
    final Entity entity = entitiesByClassName.get(clz.getName());
    if (entity == null)
      Check.fail("Class name: " + clz.getName() + " not found in runtime model");
    return entity;
  }

  /**
   * @deprecated use {@link ForeignKeyDomainType#getForeignKeyColumn(String)}
   */
  protected Column getColumnByReference(String reference, String referenceValue,
      char validationType, String columnName) throws CheckException {
    Column c = null;

    if (tablesByTableName == null)
      getModel();

    if (reference.equals(Reference.TABLEDIR)
        || (reference.equals(Reference.SEARCH) && referenceValue.equals(Reference.NO_REFERENCE))
        || reference.equals(Reference.IMAGE) || reference.equals(Reference.PRODUCT_ATTRIBUTE)
        || reference.equals(Reference.RESOURCE_ASSIGNMENT)) {

      // Removing _ID from tableName based on Openbravo's naming
      // convention
      String sTable = columnName.substring(0, columnName.length() - 3);

      // TODO: solve references in the application dictionary
      // Special Cases
      if (sTable.equals("Ref_OrderLine"))
        sTable = "C_OrderLine";

      if (columnName.equals("C_Settlement_Cancel_ID")
          || columnName.equals("C_Settlement_Generate_ID"))
        sTable = "C_Settlement";

      if (columnName.equals("Fact_Acct_Ref_ID"))
        sTable = "Fact_Acct";

      if (columnName.equals("Account_ID"))
        sTable = "C_ElementValue";

      if (columnName.equalsIgnoreCase("CreatedBy") || columnName.equalsIgnoreCase("UpdatedBy"))
        sTable = "AD_User";

      if (reference.equals(Reference.PRODUCT_ATTRIBUTE))
        sTable = "M_AttributeSetInstance";

      try {
        c = getTable(sTable).getPrimaryKeyColumns().get(0);
      } catch (final Exception e) {
        e.printStackTrace();
        Check.fail("Reference column for " + columnName + " not found in runtime model [ref: "
            + reference + ", refval: " + referenceValue + "], encountered exception "
            + e.getMessage());
      }

    } else if (reference.equals(Reference.TABLE)) {
      if (validationType == Reference.TABLE_VALIDATION) {
        final RefTable rt = refTableMap.get(referenceValue);
        if (rt != null) {
          c = rt.getColumn();
        }
      }
    } else if (reference.equals(Reference.SEARCH) && !referenceValue.equals(Reference.NO_REFERENCE)) {
      if (validationType == Reference.SEARCH_VALIDATION) {
        final RefSearch rs = refSearchMap.get(referenceValue);
        if (rs != null) {
          c = rs.getColumn();
        }
      }
    } else if (reference.equals(Reference.IMAGE_BLOB)) {
      List<Column> columns = tablesByTableName.get("ad_image").getColumns();
      for (Column acolumn : columns) {
        if (acolumn.getColumnName().equalsIgnoreCase("AD_Image_Id")) {
          c = acolumn;
          break;
        }
      }
    }
    if (c == null) {
      Check.fail("Reference column for " + columnName + " not found in runtime model [ref: "
          + reference + ", refval: " + referenceValue + "]");
    }
    return c;
  }

  /**
   * Returns a reference instance from the org.openbravo.base.model package.
   * 
   * @param referenceId
   * @return the reference identified by the referenceId, if not found then null is returned
   */
  public Reference getReference(String referenceId) {
    return referencesById.get(referenceId);
  }

  /**
   * Returns all reference (instance from the org.openbravo.base.model package).
   * 
   * @return the references
   */
  public Collection<Reference> getAllReferences() {
    return referencesById.values();
  }

  /**
   * Returns the entity for a specific tree type. The tree type is used to link an entity to a tree
   * (see the AD_Tree table).
   * 
   * @param treeType
   *          the tree type
   * @return Entity or null if none found
   */
  public Entity getEntityFromTreeType(String treeType) {
    for (Entity entity : entitiesWithTreeType) {
      if (entity.getTreeType().equals(treeType)) {
        return entity;
      }
    }
    // prevent the warning in this case
    // note from email:
    // Martin, tree type II was used for a project we did 3 years ago to automate
    // functional testing, but it was not finished and deprecated. So you can just
    // ignore that entry. Stefan will remove it in the clean up project.
    //
    // Ismael
    if (treeType != null && !treeType.equals("II")) {
      log.warn("No entity for tree type " + treeType);
    }
    return null;
  }

  /**
   * This method can be used to get the session of the ModelProvider. This method is intended to be
   * used by DomainType classes during initialization phase, to do queries in the database
   */
  public Session getSession() {
    return initsession;
  }
}
