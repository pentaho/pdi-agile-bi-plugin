package org.pentaho.agilebi.spoon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.spoon.modeler.ISpoonModelerSource;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.automodel.AutoModeler;
import org.pentaho.metadata.automodel.PhysicalTableImporter;
import org.pentaho.metadata.automodel.SchemaTable;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TargetTableType;
import org.pentaho.metadata.util.ThinModelConverter;
import org.pentaho.metadata.util.Util;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.DatasourceServiceHelper;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.pms.core.exception.PentahoMetadataException;
import org.pentaho.reporting.libraries.base.util.StringUtils;

import java.util.Locale;

/**
 * User: nbaker
 * Date: Jul 16, 2010
 */
public class InlineSqlModelerSource implements ISpoonModelerSource {

  private DatabaseMeta databaseMeta;
  private String query, datasourceName;
  private static Log logger = LogFactory.getLog(InlineSqlModelerSource.class);
  private DatasourceServiceImpl datasourceImpl = new DatasourceServiceImpl();
  private String connectionName;

	public static final String SOURCE_TYPE = InlineSqlModelerSource.class.getSimpleName();

  public InlineSqlModelerSource( String connectionName, String query, String datasourceName){
    this.query = query;
    this.connectionName = connectionName;
    this.datasourceName = datasourceName;
  }

  public String getDatabaseName() {
    return databaseMeta.getName();
  }

  public Domain generateDomain() throws ModelerException {
    try{
      BusinessData bd =  datasourceImpl.generateLogicalModel(datasourceName, connectionName, query, "10");
      return bd.getDomain();
    } catch(DatasourceServiceException dce){
      throw new ModelerException(dce);
    }
  }

  public void initialize(Domain domain) throws ModelerException {
    SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get(0);
    SqlPhysicalTable table = model.getPhysicalTables().get(0);

    String targetTable = (String) table.getProperty("target_table"); //$NON-NLS-1$
    if(!StringUtils.isEmpty(targetTable)) {
      domain.setId(targetTable);
    }

    this.databaseMeta = ThinModelConverter.convertToLegacy(model.getId(), model.getDatasource());

  }

  public void serializeIntoDomain(Domain d) {
    LogicalModel lm = d.getLogicalModels().get(0);
    lm.setProperty("source_type", SOURCE_TYPE); //$NON-NLS-1$
  }

  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setDatabaseMeta(DatabaseMeta databaseMeta) {
    this.databaseMeta = databaseMeta;
  }

  public String getSchemaName() {
    return "";
  }

  public String getTableName() {
    return "INLINE_SQL_1";
  }
}
