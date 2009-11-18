package org.pentaho.agilebi.pdi.modeler;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.XulHelper;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonBrowser;
import org.pentaho.di.ui.spoon.XulMessages;

public class AnalyzerBrowser extends SpoonBrowser {

  private static final String XUL_FILE_ANALYZER_BROWSER_TOOLBAR = "ui/analyzer-toolbar.xul";
  public static final String XUL_FILE_ANALYZER_TOOLBAR_PROPERTIES = "ui/analyzer-toolbar.properties";

  private String modelId = null;
  private String databaseName;
  private String url;
  
  public AnalyzerBrowser(Composite parent, final Spoon spoon, final String stringUrl,boolean isURL, String aUrl) throws SWTError {
    this( parent, spoon, stringUrl, isURL, true, aUrl );
  }

  public AnalyzerBrowser(Composite parent, final Spoon spoon, final String stringUrl,boolean isURL, boolean showControls, String aUrl) throws SWTError {
    super(parent, spoon, stringUrl, isURL, showControls);
    this.url = aUrl;
  }
  
  protected Browser createBrowser() {
    return new Browser(composite, SWT.MOZILLA);
  }
  
  protected void addToolBar()
  {

    try {
      toolbar = XulHelper.createToolbar(XUL_FILE_ANALYZER_BROWSER_TOOLBAR, composite, this, new XulMessages());
      
      // Add a few default key listeners
      //
      ToolBar toolBar = (ToolBar) toolbar.getNativeObject();
//      toolBar.addKeyListener(spoon.defKeys);
      
      addToolBarListeners();
    } catch (Throwable t ) {
      log.logError(toString(), Const.getStackTracker(t));
      new ErrorDialog(shell, Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_ANALYZER_BROWSER_TOOLBAR), new Exception(t));
    }
  }

  public void addToolBarListeners()
  {
    try
    {
      // first get the XML document
      URL url = XulHelper.getAndValidate(XUL_FILE_ANALYZER_TOOLBAR_PROPERTIES);
      Properties props = new Properties();
      props.load(url.openStream());
      String ids[] = toolbar.getMenuItemIds();
      for (int i = 0; i < ids.length; i++)
      {
        String methodName = (String) props.get(ids[i]);
        if (methodName != null)
        {
          toolbar.addMenuListener(ids[i], this, methodName);

        }
      }

    } catch (Throwable t ) {
      t.printStackTrace();
      new ErrorDialog(shell, Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), 
          Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_BROWSER_TOOLBAR_PROPERTIES), new Exception(t));
    }
  }

  public void refreshData() {
    // first clear the server caches
    ModelServerPublish modelServerPublish = new ModelServerPublish();
    try {
      modelServerPublish.refreshOlapCaches(modelId);
      browser.execute( "cv.getActiveReport().refreshReport()" );
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void refreshModel() {
    System.out.println("refreshModel");

    // first save the view
    executeJavascript( "gCtrlr.repositoryBrowserController.remoteSave('"+modelId+"','tmp', '', 'xanalyzer', true)" );
    
    // republish the model
    try {
      ModelServerPublish publisher = new ModelServerPublish();
      publisher.publishToServer( modelId+".mondrian.xml", databaseName, modelId, false );

    } catch (Exception e) {
      e.printStackTrace();
    }
    
    // now reload it
    browser.setUrl(this.url + "?command=open&solution=tmp&path=&action="+modelId+".xanalyzer&showFieldList=true");
    
  }

  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }
  
  public void executeJavascript( String script ) {
    browser.execute( script );
  }
  
  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

}
