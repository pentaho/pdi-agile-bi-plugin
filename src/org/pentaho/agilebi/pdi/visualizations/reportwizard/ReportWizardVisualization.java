package org.pentaho.agilebi.pdi.visualizations.reportwizard;

import org.pentaho.agilebi.pdi.visualizations.IVisualization;

public class ReportWizardVisualization implements IVisualization {

	private String description;
	private int order;
	
	public String getTitle() {
		return description;
	}

	public void setTitle(String aDescription) {
		description = aDescription;
	}
	
  public void setOrder(int order) {
    this.order = order;
  }
  
  public int getOrder() {
    return order;
  }

	public void openVisualizer(String aModelName, String aDatabaseName) {
	}
}
