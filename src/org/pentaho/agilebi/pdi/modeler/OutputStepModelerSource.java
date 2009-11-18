package org.pentaho.agilebi.pdi.modeler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metadata.model.Domain;

/**
 * Provides information to the ModelerModel to support the User Interface. This
 * class also generates the final artifacts from the UI models.
 * 
 * @author nbaker
 * 
 */
public class OutputStepModelerSource implements IModelerSource {

	private transient TableOutputMeta tableOutputMeta;
	private transient DatabaseMeta databaseMeta;
	private transient RowMetaInterface rowMeta;
	private String schemaName;
	private String fileName;
	private String repositoryName;
	
  private static Log logger = LogFactory.getLog(OutputStepModelerSource.class);
	
	public OutputStepModelerSource(TableOutputMeta tableOutputMeta, DatabaseMeta databaseMeta, RowMetaInterface rowMeta) {

		this.tableOutputMeta = tableOutputMeta;
		this.databaseMeta = databaseMeta;
		this.rowMeta = rowMeta;
		this.schemaName = tableOutputMeta.getSchemaName() != null ? tableOutputMeta.getSchemaName() : "";
	}

	public String getDatabaseName() {
		return databaseMeta.getDatabaseName();
	}

	public Domain generateDomain() throws ModelerException {
		return ModelerSourceUtil.generateDomain(databaseMeta, schemaName, tableOutputMeta.getTablename());
	}

	public void initialize() throws ModelerException {
		try {
		 Spoon spoon = ((Spoon)SpoonFactory.getInstance());
		 spoon.openFile(fileName, true);
		 TransMeta transMeta = spoon.getActiveTransformation();
		
		 if(transMeta != null) {
			
		    StepMeta steps[] = transMeta.getSelectedSteps();
		    StepMeta stepMeta = steps[0];
		    
		    TableOutputMeta tableOutputMeta = (TableOutputMeta) stepMeta.getStepMetaInterface();
		    DatabaseMeta databaseMeta = tableOutputMeta.getDatabaseMeta();

		    RowMetaInterface rowMeta = null;
	      rowMeta = transMeta.getStepFields(stepMeta);
		    
		    this.tableOutputMeta = tableOutputMeta;
		    this.databaseMeta = databaseMeta;
		    this.rowMeta = rowMeta;
		 }
		} catch(KettleStepException e) {
			logger.info(e.getLocalizedMessage());
			new ModelerException(e);
		}
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

}
