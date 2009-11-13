package org.pentaho.agilebi.pdi.visualizations;

public interface IVisualization {

	public String getTitle();

	public void setTitle(String aDescription);

	public void openVisualizer(String aModelName, String aDatabaseName);

}
