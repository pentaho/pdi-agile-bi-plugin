package org.pentaho.agilebi.pdi.visualizations;

import org.pentaho.di.ui.spoon.FileListener;

public interface IVisualization extends FileListener {

	public String getTitle();

	public void setTitle(String aDescription);
	
	/**
	 * The order is used to determine the order of visualizations in the GUI.
	 * @param order
	 */
	public void setOrder(int order);
	
	public int getOrder();

	// should change this to "new"
	public void openVisualizer(String modelFileLocation, String modelId);

	public String getExtension();
	
}
