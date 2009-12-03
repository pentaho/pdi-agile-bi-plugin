package org.pentaho.agilebi.pdi.visualizations.web;

import java.io.File;
import java.net.URLEncoder;

import org.pentaho.agilebi.pdi.visualizations.AbstractVisualization;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.w3c.dom.Node;

public class WebVisualization extends AbstractVisualization {

	private String newUrl;
	private String openUrl;
	private String saveJavascript;
	
	private String refreshDataJavascript;
	private String refreshModelJavascript;
	
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
  
  public void setRefreshDataJavascript(String refreshDataJavascript) {
    this.refreshDataJavascript = refreshDataJavascript;
  }
  
  public String getRefreshDataJavascript() {
    return refreshDataJavascript;
  }
  
  public void setRefreshModelJavascript(String refreshModelJavascript) {
    this.refreshModelJavascript = refreshModelJavascript;
  }
  
  public String getRefreshModelJavascript() {
    return this.refreshModelJavascript;
  }
  
  public String generateSaveJavascript(String filename) {
    // path, filename
    String pathAndFilename[] = getPathAndFilename(filename);
    String str = replaceField(saveJavascript, "path", pathAndFilename[0], true); //$NON-NLS-1$
    str = replaceField(str, "filename", pathAndFilename[1], true); //$NON-NLS-1$
    return str;
  }

  
	public String generateOpenUrl(String filename) {
	  // path, filename
	  String pathAndFilename[] = getPathAndFilename(filename);
	  String str = replaceField(openUrl, "path", pathAndFilename[0], true); //$NON-NLS-1$
	  str = replaceField(str, "filename", pathAndFilename[1], true); //$NON-NLS-1$
	  return str;
	}
	
	private String replaceField(String str, String fieldName, String value, boolean urlEncode) {
	  if (urlEncode) {
	    value = URLEncoder.encode(value);
	  }
	  return str.replaceAll("\\$\\{"+fieldName+"\\}", value); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String generateNewUrl(String fileLocation, String modelId) {
    String str = replaceField(newUrl, "modelLocation", fileLocation, true); //$NON-NLS-1$
    str = replaceField(str, "modelId", modelId, true); //$NON-NLS-1$
    return str;
	}


	
	public String generateRefreshDataJavascript(String fileLocation, String modelId) { 
    String str = replaceField(refreshDataJavascript, "modelLocation", fileLocation, true);
    str = replaceField(str, "modelId", modelId, true);
    return str;
	}
	
	public String getRefreshModelJavascript(String fileLocation, String modelId) {
    String str = replaceField(refreshModelJavascript, "modelLocation", fileLocation, true); //$NON-NLS-1$
    str = replaceField(str, "modelId", modelId, true); //$NON-NLS-1$
    return str;
	}

	public void createVisualizationFromModel(String fileLocation, String modelId) {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    try {
      WebVisualizationBrowser browser = new WebVisualizationBrowser(spoon.tabfolder.getSwtTabset(), spoon, this, fileLocation, modelId);
      addAndSelectTab(spoon, browser, browser.getComposite(), getUniqueUntitledTabName(spoon));
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
		
  public boolean open(Node transNode, String fname, boolean importfile) {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    try {
      WebVisualizationBrowser browser = new WebVisualizationBrowser(spoon.tabfolder.getSwtTabset(), spoon, this, fname);
      addAndSelectTab(spoon, browser, browser.getComposite(), browser.getMeta().getName());
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }    return true;
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
    String path = ""; //$NON-NLS-1$
    String fname = ""; //$NON-NLS-1$
    if (loc == -1) {
      fname = filename;
    } else {
      path = filename.substring(0, loc);
      fname = filename.substring(loc + 1);
    }
    return new String[]{path, fname};
  }


}
