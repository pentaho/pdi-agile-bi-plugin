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
import org.pentaho.metadata.model.LogicalModel;

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
	private String stepId;
	public static final String SOURCE_TYPE = OutputStepModelerSource.class.getSimpleName();
	
  private static Log logger = LogFactory.getLog(OutputStepModelerSource.class);
  
  public OutputStepModelerSource(){
    
  }
	
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

	public void initialize(Domain domain) throws ModelerException {
	  LogicalModel lm = domain.getLogicalModels().get(0);
	  if(lm.getProperty("trans_file") != null){
	    setFileName(lm.getProperty("trans_file").toString());
	    setRepositoryName(lm.getProperty("trans_repo").toString());
      setStepId(lm.getProperty("trans_step").toString());
	  }
    
//		try {
//		 Spoon spoon = ((Spoon)SpoonFactory.getInstance());
//		 spoon.openFile(fileName, true);
//		 TransMeta transMeta = spoon.getActiveTransformation();
//		
//		 if(transMeta != null) {
//			
//		    StepMeta steps[] = transMeta.getSelectedSteps();
//		    StepMeta stepMeta = null;
//		    if(steps != null && steps.length > 0){
//		      stepMeta = steps[0];
//		      setStepId(stepMeta.getStepID());
//		    } else if(this.stepId != null){
//		      for(StepMeta meta : transMeta.getSteps()){
//		        if(meta.getStepID().equals(this.stepId)){
//		          stepMeta = meta;
//		          break;
//		        }
//		      }
//		    }
//		    if(stepMeta == null){
//		      throw new ModelerException("Could not find step to generate source with");
//		    }
//		    
//		    TableOutputMeta tableOutputMeta = (TableOutputMeta) stepMeta.getStepMetaInterface();
//		    DatabaseMeta databaseMeta = tableOutputMeta.getDatabaseMeta();
//
//		    RowMetaInterface rowMeta = null;
//	      rowMeta = transMeta.getStepFields(stepMeta);
//		    
//		    this.tableOutputMeta = tableOutputMeta;
//		    this.databaseMeta = databaseMeta;
//		    this.rowMeta = rowMeta;
//		 }
//		} catch(KettleStepException e) {
//			logger.info(e.getLocalizedMessage());
//			new ModelerException(e);
//		}
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
	  if(repositoryName != null && repositoryName.equals("")){
	    this.repositoryName = null;
	  }
	  this.repositoryName = repositoryName;
	}
	
	

  public String getStepId() {
    return stepId;
  }

  public void setStepId(String stepId) {
    this.stepId = stepId;
  }

  public void serializeIntoDomain(Domain d) {
    LogicalModel lm = d.getLogicalModels().get(0);
    lm.setProperty("trans_file", this.fileName);
    lm.setProperty("trans_repo", this.repositoryName != null ? this.repositoryName : "");
    lm.setProperty("source_type", this.SOURCE_TYPE);
    lm.setProperty("trans_step", this.stepId);
  }
	
}
