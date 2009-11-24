package org.pentaho.agilebi.pdi.visualizations.reportwizard;

import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.di.core.EngineMetaInterface;
import org.w3c.dom.Node;

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

  public boolean open(Node arg0, String arg1, boolean arg2) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean save(EngineMetaInterface arg0, String arg1, boolean arg2) {
    // TODO Auto-generated method stub
    return false;
  }

  public void syncMetaName(EngineMetaInterface arg0, String arg1) {
    // TODO Auto-generated method stub
    
  }

  public String getExtension() {
    return "prpt";
  }
}
