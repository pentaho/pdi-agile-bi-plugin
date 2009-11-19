package org.pentaho.agilebi.pdi.visualizations;

public interface IVisualization {

	public String getTitle();

	public void setTitle(String aDescription);
	
	/**
	 * The order is used to determine the order of visualizations in the GUI.
	 * @param order
	 */
	public void setOrder(int order);
	
	public int getOrder();
	

	public void openVisualizer(String fileLocation, String modelId);

}
