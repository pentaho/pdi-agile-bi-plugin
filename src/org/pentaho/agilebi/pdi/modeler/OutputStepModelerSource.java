/*
 * This program is free software; you can redistribute it and/or modify it under the
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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;

/**
 * Provides information to the ModelerModel to support the User Interface. This
 * class also generates the final artifacts from the UI models.
 * 
 * @author nbaker
 * 
 */
public class OutputStepModelerSource extends TableModelerSource {

	private String fileName;
	private String repositoryName;
	private String stepId;
	public static final String OUTPUTSTEP_SOURCE_TYPE = OutputStepModelerSource.class.getSimpleName();
	
  public OutputStepModelerSource() {
    
  }
	
	public OutputStepModelerSource(TableOutputMeta tableOutputMeta, DatabaseMeta databaseMeta, RowMetaInterface rowMeta) {
	  super( databaseMeta, tableOutputMeta.getTablename(), tableOutputMeta.getSchemaName() );
	}

	public Domain generateDomain() throws ModelerException {
		return ModelerSourceUtil.generateDomain(getDatabaseMeta(), getSchemaName(), getTableName());
	}

	public void initialize(Domain domain) throws ModelerException {
	  super.initialize(domain);
	  LogicalModel lm = domain.getLogicalModels().get(0);
	  if(lm.getProperty("trans_file") != null) { //$NON-NLS-1$
	    setFileName(lm.getProperty("trans_file").toString()); //$NON-NLS-1$
	  }
	  if (lm.getProperty("trans_repo") != null) { //$NON-NLS-1$
	    setRepositoryName(lm.getProperty("trans_repo").toString()); //$NON-NLS-1$
	  }
	  if (lm.getProperty("trans_step") != null) { //$NON-NLS-1$
      setStepId(lm.getProperty("trans_step").toString()); //$NON-NLS-1$
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
	  if(repositoryName != null && repositoryName.equals("")){ //$NON-NLS-1$
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
    lm.setProperty("source_type", OutputStepModelerSource.OUTPUTSTEP_SOURCE_TYPE); //$NON-NLS-1$
    lm.setProperty("trans_file", this.fileName != null ? this.fileName : ""); //$NON-NLS-1$ //$NON-NLS-2$
    lm.setProperty("trans_repo", this.repositoryName != null ? this.repositoryName : ""); //$NON-NLS-1$ //$NON-NLS-2$
    lm.setProperty("trans_step", this.stepId != null ? this.stepId : ""); //$NON-NLS-1$ //$NON-NLS-2$
  }

}
