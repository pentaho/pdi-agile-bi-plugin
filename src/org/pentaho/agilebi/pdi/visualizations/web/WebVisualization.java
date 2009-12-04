package org.pentaho.agilebi.pdi.visualizations.web;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom4j.Dom4jXPath;
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
	
  private String getStateJavascript;
  private String setStateJavascript;
	
	private String modelFileName;
	
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
	
	public String generateRefreshModelJavascript(String fileLocation, String modelId) {
	  String str = replaceField(refreshModelJavascript, "modelLocation", fileLocation, true); //$NON-NLS-1$
    str = replaceField(str, "modelId", modelId, true); //$NON-NLS-1$
    str = replaceField(str, "basedir", new File("").getAbsolutePath(), false); //$NON-NLS-1$ //$NON-NLS-2$
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
      File f = new File( fname );
      FileInputStream in = new FileInputStream( f );
      StringBuilder sb = new StringBuilder();
      byte b[] = new byte[2048];
      int n = in.read(b);
      while( n != -1 ) {
        sb.append( new String(b, 0, n) );
        n = in.read(b);
      }
      
      Document doc = DocumentHelper.parseText(sb.toString());
      Dom4jXPath xpath = new Dom4jXPath( "//@catalog");
      HashMap map = new HashMap();
      map.put( "pho", "http://www.pentaho.com");
      xpath.setNamespaceContext( new SimpleNamespaceContext( map));
      org.dom4j.Node node = (org.dom4j.Node) xpath.selectSingleNode( doc);

      modelFileName = node.getText();

      WebVisualizationBrowser browser = new WebVisualizationBrowser(spoon.tabfolder.getSwtTabset(), spoon, this, fname);
      browser.setXmiFileLocation(modelFileName);
      addAndSelectTab(spoon, browser, browser.getComposite(), browser.getMeta().getName());
      String fullPath = f.getAbsolutePath();
      spoon.getProperties().addLastFile("Analyzer", fullPath, null, false, null);
      spoon.addMenuLast();
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

  public String getModelFileName() {
    return modelFileName;
  }

  public String getGetStateJavascript() {
    return getStateJavascript;
  }

  public void setGetStateJavascript(String getStateJavascript) {
    this.getStateJavascript = getStateJavascript;
  }

  public String getSetStateJavascript() {
    return setStateJavascript;
  }

  public void setSetStateJavascript(String setStateJavascript) {
    this.setStateJavascript = setStateJavascript;
  }


}
