package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.metadata.model.Domain;

/**
 * Provides a ModelerModel with information needed to generate the UI and well
 * as the final Mondrian and Metadata models. All information from the context
 * in which the modeling is initiated should be contained within implementors of
 * this interface.
 * 
 * @author nbaker
 */
public interface IModelerSource {
	public Domain generateDomain() throws ModelerException;

	public String getDatabaseName();

	public void initialize() throws ModelerException;

	public void setFileName(String aFileName);

	public String getFileName();

	public void setRepositoryName(String aRepositoryName);

	public String getRepositoryName();
}
