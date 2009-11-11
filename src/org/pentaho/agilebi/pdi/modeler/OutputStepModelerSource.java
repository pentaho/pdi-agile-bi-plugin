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
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;

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
  
  public OutputStepModelerSource( TableOutputMeta tableOutputMeta, DatabaseMeta databaseMeta, RowMetaInterface rowMeta){

    this.tableOutputMeta = tableOutputMeta;
    this.databaseMeta = databaseMeta;
    this.rowMeta = rowMeta;
  }

  public List<String> getFieldNames() {
    if (rowMeta == null) {
      return null;
    }
    List<String> result = new ArrayList<String>();
    for (String fieldName : rowMeta.getFieldNames()) {
      result.add(fieldName);
    }
    Collections.sort(result);
    return result;
  }
  
  public String getDatabaseName(){
    return databaseMeta.getDatabaseName();
  }
  
}
