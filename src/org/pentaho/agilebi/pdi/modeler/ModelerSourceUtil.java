package org.pentaho.agilebi.pdi.modeler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.automodel.SchemaTable;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.pms.core.exception.PentahoMetadataException;

public class ModelerSourceUtil {

  public static final String DEFAULT_USER_NAME = "joe";
  private static ModelGenerator generator = new ModelGenerator();

  public static Domain generateDomain(DatabaseMeta databaseMeta, String schemaName, String tableName)
      throws PentahoMetadataException {

    // modelName, databaseMeta, , "joe", tableOutputMeta.getTablename(), profiles);
    String locale = Locale.getDefault().toString();
    generator.setLocale(locale);
    generator.setDatabaseMeta(databaseMeta);
    generator.setModelName(tableName);

    SchemaTable tableNames[] = new SchemaTable[1];
    // TODO: support schema names.
    tableNames[0] = new SchemaTable(schemaName, tableName);
    generator.setTableNames(tableNames);
    Domain domain = generator.generateDomain();
    domain.setId(tableName); // replaced with user specified name later

    LogicalModel businessModel = domain.getLogicalModels().get(0); // schemaMeta.getActiveModel();

    // TODO do this with messages
    businessModel.setName(new LocalizedString(locale, tableName));
    businessModel.setDescription(new LocalizedString(locale, "This is the data model for "
        + businessModel.getName(locale)));

    LogicalTable businessTable = businessModel.getLogicalTables().get(0);
    businessTable.setName(new LocalizedString(locale, "Available Columns"));

    // is this necessary
    // businessTable.getsetTargetTable( tableName );
    SecurityOwner owner = new SecurityOwner(SecurityOwner.OwnerType.USER, DEFAULT_USER_NAME);

    Metadata meta = new Metadata();

    // TODO: investigate and replace this magic number with named constant?
    int rights = 31;
    meta.setUserAccess(DEFAULT_USER_NAME, rights, businessModel);
    meta.setUserAccess(DEFAULT_USER_NAME, rights, businessTable);

    return domain;
  }

  
}
