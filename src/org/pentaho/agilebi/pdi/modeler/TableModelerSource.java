/*
OutputStepModelerSource.java * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.util.ThinModelConverter;
import org.pentaho.reporting.libraries.base.util.StringUtils;

/**
 * Provides information to the ModelerModel to support the User Interface. This
 * class also generates the final artifacts from the UI models.
 * 
 * @author jdixon
 * 
 */
public class TableModelerSource implements IModelerSource {

	private String tableName;
	private transient DatabaseMeta databaseMeta;
	private String schemaName;
	public static final String SOURCE_TYPE = TableModelerSource.class.getSimpleName();
	
  public TableModelerSource(){
    
  }
	
	public TableModelerSource(DatabaseMeta databaseMeta, String tableName, String schemaName ) {
		this.tableName = tableName;
		this.databaseMeta = databaseMeta;
		this.schemaName = schemaName;
		if( schemaName == null ) {
		  this.schemaName = ""; //$NON-NLS-1$
		}
	}

	public String getDatabaseName() {
		return databaseMeta.getName();
	}

	public Domain generateDomain() throws ModelerException {
		return ModelerSourceUtil.generateDomain(databaseMeta, schemaName, tableName);
	}

	public void initialize(Domain domain) throws ModelerException {
	  SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get(0);
	  SqlPhysicalTable table = model.getPhysicalTables().get(0);
	  
	  String targetTable = (String) table.getProperty("target_table"); //$NON-NLS-1$
	  if(!StringUtils.isEmpty(targetTable)) {
	    domain.setId(targetTable);
	  }
	  
	  this.databaseMeta = ThinModelConverter.convertToLegacy(model.getId(), model.getDatasource());
    this.tableName = table.getTargetTable();
	  this.schemaName = table.getTargetSchema();
	  
    if( schemaName == null ) {
      schemaName = ""; //$NON-NLS-1$
    }
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

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getSchemaName() {
    return schemaName == null ? "" : schemaName; //$NON-NLS-1$
  }

  public void setSchemaName(String schemaName) {
    if( schemaName == null ) {
      schemaName = ""; //$NON-NLS-1$
    }
    this.schemaName = schemaName;
  }

}
