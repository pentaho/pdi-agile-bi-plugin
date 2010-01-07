package org.pentaho.agilebi.pdi.visualizations.web;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Locale;

import mondrian.rolap.agg.AggregationManager;

import org.dom4j.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.agilebi.pdi.modeler.BiServerConnection;
import org.pentaho.agilebi.pdi.modeler.Messages;
import org.pentaho.agilebi.pdi.modeler.ModelServerPublish;
import org.pentaho.agilebi.pdi.modeler.XulDialogPublish;
import org.pentaho.agilebi.pdi.perspective.AbstractPerspective;
import org.pentaho.agilebi.pdi.perspective.AgileBiPerspective;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonBrowser;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.w3c.dom.Node;

public class WebVisualizationBrowser extends SpoonBrowser implements FileListener {

  private static final String XUL_FILE_ANALYZER_BROWSER_TOOLBAR = "plugins/spoon/agile-bi/ui/analyzer-toolbar.xul"; //$NON-NLS-1$
  public static final String XUL_FILE_ANALYZER_TOOLBAR_PROPERTIES = "plugins/spoon/agile-bi/ui/analyzer-toolbar.properties"; //$NON-NLS-1$

  private String xmiFileLocation = null;
  private String modelId = null;
  // private String databaseName;
  private WebVisualization visualization;
  private WebVisualizationMeta meta;
  private String visFileLocation = null;

  public WebVisualizationBrowser(Composite parent, final Spoon spoon, final WebVisualization visualization, String xmiFileLocation, String modelId, String visFileLocation, String url) throws SWTError {
    super(parent, spoon, url, true, true );
    this.visualization = visualization;
    this.xmiFileLocation = xmiFileLocation;
    this.modelId = modelId;
    this.visFileLocation = visFileLocation;
    this.meta = new WebVisualizationMeta(this);
  }
  
  public WebVisualization getVisualization() {
    return visualization;
  }

  protected Browser createBrowser() {
    Browser newBrowser;
    if (System.getProperty("os.name") != null && System.getProperty("os.name").indexOf("Mac") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      newBrowser = new Browser(composite, SWT.MOZILLA);
    } else {
      newBrowser = super.createBrowser();
    }
    return newBrowser;

  }
  
  protected void addToolBar() {
    try {
      XulLoader loader = new SwtXulLoader();
      loader.setOuterContext(composite);
      XulDomContainer xulDomContainer = loader.loadXul(XUL_FILE_ANALYZER_BROWSER_TOOLBAR);
      xulDomContainer.addEventHandler(this);
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById("nav-toolbar"); //$NON-NLS-1$

      addToolBarListeners();
    } catch (Throwable t ) {
      log.logError(toString(), Const.getStackTracker(t));
      new ErrorDialog(shell, org.pentaho.di.ui.spoon.Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), org.pentaho.di.ui.spoon.Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_ANALYZER_BROWSER_TOOLBAR), new Exception(t)); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public void addToolBarListeners() {
    /*
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
      new ErrorDialog(shell, Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"),  //$NON-NLS-1$
          Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_BROWSER_TOOLBAR_PROPERTIES), new Exception(t)); //$NON-NLS-1$
    }
    */
  }

  public String getVisFileLocation() {
    return visFileLocation;
  }
  
  public void save(String filename) {
    visFileLocation = filename;
    browser.execute(visualization.generateSaveJavascript(filename));
  }
  
  public void save() {
    spoon.saveToFile(meta);
  }
  
  public void saveAs() {
    spoon.saveFileAs(meta);
  }
  
  public void editModel() {
    AgileBiPerspective.getInstance().open(null, xmiFileLocation, false); 
    
  }
  
  public void publishView() {

    // save the file locally
    File tmp = new File("tmp"); //$NON-NLS-1$
    if( !tmp.exists() ) {
      tmp.mkdirs();
    }
    String origName = meta.getName();
    String origVisFileLocation = visFileLocation;
    
    // now throw up the publish dialog

    try {
    XulDialogPublish publishDialog = new XulDialogPublish( spoon.getShell() );
    publishDialog.setFolderTreeDepth(99);
    publishDialog.setComment( Messages.getInstance().getString("ModelServerPublish.Publish.ViewPublishComment") ); //$NON-NLS-1$
    publishDialog.setFilename( origName );
    publishDialog.setCheckDatasources( false );
    publishDialog.setShowLocation( true, true, true );
    publishDialog.setShowDatasourceStatus( false );
    String template = "{path}{file}.xanalyzer"; //$NON-NLS-1$ 
    publishDialog.setPathTemplate( template );
    publishDialog.showDialog();
    if( publishDialog.isAccepted() ) {
      
      String newName = publishDialog.getFilename();
      
      String tmpFileName = "tmp/"+newName+".xanalyzer"; //$NON-NLS-1$ //$NON-NLS-2$
      File file = new File(tmpFileName);
      tmpFileName = file.getAbsolutePath();
      meta.setFilename(tmpFileName); 
      if( file.exists() ) {
        file.delete();
      }
      spoon.saveToFile(meta);

      PublishThread publish = new PublishThread();
      publish.setBiServerConnection(publishDialog.getBiServerConnection());
      publish.setRepositoryPath(publishDialog.getPath());
      
      publish.setFile(file);
      
      Thread thread = new Thread( publish );
      thread.start();

    }
    } catch (XulException e) {
      e.printStackTrace();
      SpoonFactory.getInstance().messageBox( "Could not create dialog: "+e.getLocalizedMessage(), "Dialog Error", false, Const.ERROR);
    } finally {
      visFileLocation = origVisFileLocation;
    }
  }
  
  public void refreshData() {
    // first clear the server cache
    
    AggregationManager.instance().getCacheControl(null).flushSchemaCache();
    browser.execute(visualization.generateRefreshDataJavascript( xmiFileLocation, modelId ));
  }
  
  public void refreshModel() {
    // first save the view
//    if (true) throw new UnsupportedOperationException();
    // TODO: can we do this without requiring a "remote save"? 
    AggregationManager.instance().getCacheControl(null).flushSchemaCache();
    
    browser.execute( visualization.generateRefreshModelJavascript( xmiFileLocation, modelId ));
    // "gCtrlr.repositoryBrowserController.remoteSave('"+modelId+"','tmp', '', 'xanalyzer', true)" 
    
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

  public void setXmiFileLocation(String xmiFileLocation) {
    this.xmiFileLocation = xmiFileLocation;
  }

  public void setVisFileLocation(String visFileLocation) {
    this.visFileLocation = visFileLocation;
  }

  // FileListener methods
  public boolean accepts(String fileName) {
    if(fileName == null || fileName.indexOf('.') == -1){
      return false;
    }
    String extension = fileName.substring(fileName.lastIndexOf('.')+1);
    return extension.equals("xanalyzer");
  }

  public boolean acceptsXml(String nodeName) {
    return nodeName.equals("reportRecord");
  }

  public String[] getFileTypeDisplayNames(Locale locale) {
    return new String[]{"Models"};
  }

  public String getRootNodeName() {
    return null;
  }

  public String[] getSupportedExtensions() {
    return new String[]{"xmi"};
  }
  
  private class PublishThread implements Runnable {

    private File file;
    
    private String repositoryPath;
    
    private BiServerConnection biServerConnection;
    
    public void run() {
      // wait for the file to appear
      long time = (new Date()).getTime();
      long target = time + 10 * 1000;
      while( !file.exists() && (new Date()).getTime() < target ) {
        Thread.yield();
        try {
          Thread.sleep( 250 );
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      if( file.exists() ) {
        
        // first modify the catalog
        try {
          Document doc = WebVisualization.getXAnalyzerDocument( file );

          // tidy up the catalog
          String catalogFileName = WebVisualization.getDocumentText(doc, "//@catalog"); //$NON-NLS-1$
          String catalog = AbstractPerspective.createShortName(catalogFileName);

          WebVisualization.setDocumentText(doc, "//@catalog", catalog ); //$NON-NLS-1$
          // now write the document back
          FileOutputStream out = new FileOutputStream( file );
          String xml = doc.asXML();
          out.write( xml.getBytes() );
          out.close();
          
          File files[] = {file};
          ModelServerPublish publisher = new ModelServerPublish();
          publisher.setBiServerConnection(biServerConnection);
          publisher.publishFile(repositoryPath, files, true);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        
      } else {
        // we timed out...
        SpoonFactory.getInstance().messageBox( "The publish timed-out", "Dialog Error", false, Const.ERROR);
      }
    }

    public File getFile() {
      return file;
    }

    public void setFile(File file) {
      this.file = file;
    }

    public String getRepositoryPath() {
      return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
      this.repositoryPath = repositoryPath;
    }

    public BiServerConnection getBiServerConnection() {
      return biServerConnection;
    }

    public void setBiServerConnection(BiServerConnection biServerConnection) {
      this.biServerConnection = biServerConnection;
    }
    
  }
  
  
}
