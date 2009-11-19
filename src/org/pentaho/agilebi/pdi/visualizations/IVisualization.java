package org.pentaho.agilebi.pdi.visualizations;

public interface IVisualization {

	public String getTitle();

	public void setTitle(String aDescription);

	public void setUrl(String aUrl);

	public String getUrl();

	public void openVisualizer(String fileLocation, String modelId);

}
