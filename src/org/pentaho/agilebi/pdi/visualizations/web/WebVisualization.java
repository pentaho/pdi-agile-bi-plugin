package org.pentaho.agilebi.pdi.visualizations.web;

import java.net.URLEncoder;

import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizeCanvas;

public class WebVisualization implements IVisualization {

	private String url;
	private String refreshDataJavascript;
	private String refreshModelJavascript;
	private String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String aDescription) {
		title = aDescription;
	}
	
	public String getUrl() {
		return url;
	}
	
	private String replaceFields(String str, String fileLocation, String modelId, boolean urlEncode) {
	  String fl = null;
	  String mid = null;
	  if (urlEncode) {
	    fl = URLEncoder.encode(fileLocation);
	    mid = URLEncoder.encode(modelId);
	  } else {
	    fl = fileLocation;
	    mid = modelId;
	  }
    String newstr = str.replaceAll("\\$\\{fileLocation\\}", fl);
    newstr = newstr.replaceAll("\\$\\{modelId\\}", mid);
    return newstr;
	  
	}
	
	public String getUrl(String fileLocation, String modelId) {
	  return replaceFields(url, fileLocation, modelId, true);
	}
	
	public void setUrl(String aUrl) {
		url = aUrl;
	}

	public void openVisualizer(String fileLocation, String modelId) {
	  VisualizeCanvas canvas = new VisualizeCanvas(this, fileLocation, modelId);
	  canvas.openVisualization();
	}
	
	public void setRefreshDataJavascript(String refreshDataJavascript) {
	  this.refreshDataJavascript = refreshDataJavascript;
	}
	
	public String getRefreshDataJavascript() {
	  return refreshDataJavascript;
	}
	
	public String getRefreshDataJavascript(String fileLocation, String modelId) { 
	  return replaceFields(refreshDataJavascript, fileLocation, modelId, false);
	}
	
	public void setRefreshModelJavascript(String refreshModelJavascript) {
	  this.refreshModelJavascript = refreshModelJavascript;
	}
	
	public String getRefreshModelJavascript() {
	  return this.refreshModelJavascript;
	}
	
	public String getRefreshModelJavascript(String fileLocation, String modelId) {
	  return replaceFields(refreshModelJavascript, fileLocation, modelId, false);
	}
}
