package org.pentaho.agilebi.pdi.modeler;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.automodel.SchemaTable;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.pms.core.exception.PentahoMetadataException;

public class ModelerSourceUtil {

  public static final String DEFAULT_USER_NAME = "joe";
  private static ModelGenerator generator = new ModelGenerator();
  private static Log logger = LogFactory.getLog(ModelerSourceUtil.class);

  public static Domain generateDomain(DatabaseMeta databaseMeta, String schemaName, String tableName)
      throws ModelerException {
  	Domain domain = null;
  	try {
	    // modelName, databaseMeta, , "joe", tableOutputMeta.getTablename(), profiles);
	    String locale = Locale.getDefault().toString();
	    generator.setLocale(locale);
	    generator.setDatabaseMeta(databaseMeta);
	    generator.setModelName(tableName);
	
	    SchemaTable tableNames[] = new SchemaTable[1];
	    // TODO: support schema names.
	    tableNames[0] = new SchemaTable(schemaName, tableName);
	    generator.setTableNames(tableNames);
	    domain = generator.generateDomain();
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
  	} catch (PentahoMetadataException e) {
  		logger.info(e.getLocalizedMessage());
  		throw new ModelerException(e);
  	}
    return domain;
  }
}
