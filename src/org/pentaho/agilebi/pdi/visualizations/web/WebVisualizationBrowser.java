package org.pentaho.agilebi.pdi.visualizations.web;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.agilebi.pdi.modeler.ModelServerPublish;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.XulHelper;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonBrowser;
import org.pentaho.di.ui.spoon.XulMessages;
import org.w3c.dom.Node;

public class WebVisualizationBrowser extends SpoonBrowser implements FileListener {

  private static final String XUL_FILE_ANALYZER_BROWSER_TOOLBAR = "plugins/spoon/agile-bi/ui/analyzer-toolbar.xul";
  public static final String XUL_FILE_ANALYZER_TOOLBAR_PROPERTIES = "plugins/spoon/agile-bi/ui/analyzer-toolbar.properties";

  private String xmiFileLocation = null;
  private String modelId = null;
  // private String databaseName;
  private String url;
  private WebVisualization visualization;
  private WebVisualizationMeta meta;
  private String visFileLocation = null;
  
  public WebVisualizationBrowser(Composite parent, final Spoon spoon, final WebVisualization visualization, String visFileLocation) throws SWTError {
    super(parent, spoon, visualization.getOpenUrl(visFileLocation), true, true);
    this.visualization = visualization;
    this.visFileLocation = visFileLocation;
    this.meta = new WebVisualizationMeta(this);
  }
  
  public WebVisualizationBrowser(Composite parent, final Spoon spoon, final WebVisualization visualization, String xmiFileLocation, String modelId) throws SWTError {
    super(parent, spoon, visualization.getNewUrl(xmiFileLocation, modelId), true, true );
    this.visualization = visualization;
    this.xmiFileLocation = xmiFileLocation;
    this.modelId = modelId;
    this.meta = new WebVisualizationMeta(this);
  }
  
  public WebVisualization getVisualization() {
    return visualization;
  }
  
  protected Browser createBrowser() {
    return new Browser(composite, SWT.MOZILLA);
  }
  
  protected void addToolBar() {
    try {
      // TODO: won't this all go away with the new xul?
      toolbar = XulHelper.createToolbar(XUL_FILE_ANALYZER_BROWSER_TOOLBAR, composite, this, new XulMessages());
      
      // Add a few default key listeners
      //
      // This is currently turned off in Spoon
//      ToolBar toolBar = (ToolBar) toolbar.getNativeObject();
//      toolBar.addKeyListener(spoon.defKeys);
      
      addToolBarListeners();
    } catch (Throwable t ) {
      log.logError(toString(), Const.getStackTracker(t));
      new ErrorDialog(shell, Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_ANALYZER_BROWSER_TOOLBAR), new Exception(t));
    }
  }

  public void addToolBarListeners() {
    try {
      // first get the XML document
      URL url = XulHelper.getAndValidate(XUL_FILE_ANALYZER_TOOLBAR_PROPERTIES);
      Properties props = new Properties();
      props.load(url.openStream());
      String ids[] = toolbar.getMenuItemIds();
      for (int i = 0; i < ids.length; i++) {
        String methodName = (String) props.get(ids[i]);
        if (methodName != null) {
          toolbar.addMenuListener(ids[i], this, methodName);
        }
      }
    } catch (Throwable t ) {
      t.printStackTrace();
      new ErrorDialog(shell, Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), 
          Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_BROWSER_TOOLBAR_PROPERTIES), new Exception(t));
    }
  }

  public void save(String filename) {
    visFileLocation = filename;
    browser.execute(visualization.getSaveJavascript(filename));
  }
  
  public void save() {
    if (visFileLocation == null) {
      saveAs();
    } else {
      save(visFileLocation);
    }
  }
  
  public void saveAs() {
    //
    System.out.println("SAVE AS CALLED");

    String path = "/home/gorman/";
    String filename = "analyzer_report";
    
    // TODO: encode strings
    
    browser.execute("gCtrlr.repositoryBrowserController.remoteSave('" + filename + "', '', '"+path+"', null, true);");

    
    //
    System.out.println("SAVE AS CALLED");

    // present a dialog, then call save JS
  }
  
  public void refreshData() {
    // first clear the server cache
    
    // TODO: bring ModelServerPublish code over
    if (true) throw new UnsupportedOperationException();
    
    ModelServerPublish modelServerPublish = new ModelServerPublish();
    try {
      modelServerPublish.refreshOlapCaches(modelId);
      browser.execute(visualization.getRefreshDataJavascript());
      //  "cv.getActiveReport().refreshReport()" )
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void refreshModel() {
    // first save the view
    if (true) throw new UnsupportedOperationException();
    // TODO: can we do this without requiring a "remote save"? 
    browser.execute( visualization.getRefreshModelJavascript());
    // "gCtrlr.repositoryBrowserController.remoteSave('"+modelId+"','tmp', '', 'xanalyzer', true)" 
        
    // now reload it
    browser.setUrl(visualization.getNewUrl());
    
  }

  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }
  
  public EngineMetaInterface getMeta() {
    return meta;
  }

  public boolean canHandleSave() {
    return true;
  }

  public boolean setFocus() {
    return true;
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
}
