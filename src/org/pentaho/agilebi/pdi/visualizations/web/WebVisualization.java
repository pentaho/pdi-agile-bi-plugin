package org.pentaho.agilebi.pdi.visualizations.web;

import java.io.File;
import java.net.URLEncoder;

import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizeCanvas;
import org.pentaho.di.core.EngineMetaInterface;
import org.w3c.dom.Node;

public class WebVisualization implements IVisualization {

  private int order;
  
	private String newUrl;
	private String openUrl;
	private String saveJavascript;
	
	private String refreshDataJavascript;
	private String refreshModelJavascript;
	private String title;
	private String extension;
	
	public String getExtension() {
	  return extension;
	}
	
	public void setExtension(String extension) {
	  this.extension = extension;
	}
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String aDescription) {
		title = aDescription;
	}
	
	public void setOrder(int order) {
	  this.order = order;
	}
	
	public int getOrder() {
	  return order;
	}
	
	public String getNewUrl() {
		return newUrl;
	}

	public void setNewUrl(String aUrl) {
    newUrl = aUrl;
  }
	
	public String getOpenUrl() {
	  return openUrl;
	}
	
	public void setOpenUrl(String openUrl) {
	  this.openUrl = openUrl;
	}
	

  public void setSaveJavascript(String saveJavascript) {
    this.saveJavascript = saveJavascript;
  }

  public String getSaveJavascript() {
    return saveJavascript;
  }

  public String generateSaveJavascript(String filename) {
    // path, filename
    String pathAndFilename[] = getPathAndFilename(filename);
    String str = replaceField(saveJavascript, "path", pathAndFilename[0], true);
    str = replaceField(str, "filename", pathAndFilename[1], true);
    return str;
  }

  
	public String generateOpenUrl(String filename) {
	  // path, filename
	  String pathAndFilename[] = getPathAndFilename(filename);
	  String str = replaceField(openUrl, "path", pathAndFilename[0], true);
	  str = replaceField(str, "filename", pathAndFilename[1], true);
	  return str;
	}
	
	private String replaceField(String str, String fieldName, String value, boolean urlEncode) {
	  if (urlEncode) {
	    value = URLEncoder.encode(value);
	  }
	  return str.replaceAll("\\$\\{"+fieldName+"\\}", value); //$NON-NLS-1$
	}
	
	public String generateNewUrl(String fileLocation, String modelId) {
    String str = replaceField(newUrl, "modelLocation", fileLocation, true);
    str = replaceField(str, "modelId", modelId, true);
    return str;
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
	
	public String generateRefreshDataJavascript(String fileLocation, String modelId) { 
    String str = replaceField(refreshDataJavascript, "modelLocation", fileLocation, true);
    str = replaceField(str, "modelId", modelId, true);
    return str;
	}
	
	public void setRefreshModelJavascript(String refreshModelJavascript) {
	  this.refreshModelJavascript = refreshModelJavascript;
	}
	
	public String getRefreshModelJavascript() {
	  return this.refreshModelJavascript;
	}
	
	public String getRefreshModelJavascript(String fileLocation, String modelId) {
    String str = replaceField(refreshModelJavascript, "modelLocation", fileLocation, true);
    str = replaceField(str, "modelId", modelId, true);
    return str;
	}

  public boolean open(Node transNode, String fname, boolean importfile) {
    VisualizeCanvas canvas = new VisualizeCanvas(this, fname);
    canvas.openExistingVisualization();
    return true;
  }

  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    WebVisualizationMeta wvmeta = (WebVisualizationMeta)meta;
    wvmeta.save(fname);
    return true;
  }

  public void syncMetaName(EngineMetaInterface arg0, String arg1) {
    // TODO Auto-generated method stub
  }
  
  public String[] getPathAndFilename(String filename) {
    int loc = filename.lastIndexOf(File.separator);
    String path = "";
    String fname = "";
    if (loc == -1) {
      fname = filename;
    } else {
      path = filename.substring(0, loc);
      fname = filename.substring(loc + 1);
    }
    return new String[]{path, fname};
  }


}
