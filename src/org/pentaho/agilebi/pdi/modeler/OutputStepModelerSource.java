package org.pentaho.agilebi.pdi.modeler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.pms.core.exception.PentahoMetadataException;

/**
 * Provides information to the ModelerModel to support the User Interface. This class also generates the 
 * final artifacts from the UI models.
 * 
 * @author nbaker
 *
 */
public class OutputStepModelerSource implements IModelerSource{
  private TableOutputMeta tableOutputMeta;
  private DatabaseMeta databaseMeta;
  private RowMetaInterface rowMeta;
  private String schemaName;
  
  public OutputStepModelerSource( TableOutputMeta tableOutputMeta, DatabaseMeta databaseMeta, RowMetaInterface rowMeta){

    this.tableOutputMeta = tableOutputMeta;
    this.databaseMeta = databaseMeta;
    this.rowMeta = rowMeta;
    this.schemaName = tableOutputMeta.getSchemaName() != null ? tableOutputMeta.getSchemaName() : "";
  }
  
  public String getDatabaseName(){
    return databaseMeta.getDatabaseName();
  }

  public Domain generateDomain() throws PentahoMetadataException {
    return ModelerSourceUtil.generateDomain(databaseMeta, schemaName, tableOutputMeta.getTablename());
  }
  
  
  
}
