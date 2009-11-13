package org.pentaho.agilebi.pdi.visualizations.reportwizard;

import org.pentaho.agilebi.pdi.visualizations.IVisualization;

public class ReportWizardVisualization implements IVisualization {

	private String description;

	public String getTitle() {
		return description;
	}

	public void setTitle(String aDescription) {
		description = aDescription;
	}
	
	public String getUrl() {
		return null;
	}
	
	public void setUrl(String aUrl) {
	}

	public void openVisualizer(String aModelName, String aDatabaseName) {
	}
}
