package org.pentaho.agilebi.pdi.modeler;

import java.net.URLEncoder;
import java.util.Locale;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabListener;
import org.pentaho.xul.swt.tab.TabSet;

public class VisualizeCanvas implements TabItemInterface, ModifyListener, TabListener {

  private String modelName;
  
  private String databaseName;
  
  private TabItem tabItem = null;
  
  boolean isTabSelected = false;
  
  private AnalyzerBrowser browser;
  
  private String url;
  
  public static void openVisualizer( String modelName, String databaseName, String url) {
    
    VisualizeCanvas canvas = new VisualizeCanvas();
    
    canvas.setUrl(url);
    canvas.setModelName( modelName );
    canvas.setDatabaseName(databaseName);
    
    canvas.createTab();
    
  }
  
  public void createTab() {
    /*
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    TabSet tabSet = spoon.getTabSet();
    TabSet tabfolder = spoon.tabfolder;
    String name = "Visualize "+modelName;
    CTabFolder cTabFolder = tabfolder.getSwtTabset();
    TabItem tabItem = new TabItem(tabfolder, name, name);
    tabItem.setImage(GUIResource.getInstance().getImageLogoSmall());
    final Canvas canvas = new Canvas(cTabFolder, SWT.NO_BACKGROUND);
    tabItem.setControl( canvas );

    TabMapEntry entry = new TabMapEntry(tabItem, name, this, TabMapEntry.OBJECT_TYPE_BROWSER);
    
    spoon.delegates.tabs.addTab(entry);
    tabSet.addTab(tabItem);

    int idx = tabfolder.indexOf(tabItem);
    // keep the focus on the graph
    tabfolder.setSelected(idx);
*/
    String encodedModelName = URLEncoder.encode( modelName);
    String urlString = this.url + "?command=new&catalog="+encodedModelName+"%20Model&cube="+encodedModelName+"%20Cube&userid=joe&password=password";

    String tabName = modelName+" ";
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    addAnalyzerBrowser(tabName, modelName, urlString, true);
    
  }
  
  public boolean addAnalyzerBrowser(String tabName, String modelName, String urlString,boolean isURL)
  {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    TabSet tabfolder = spoon.tabfolder;

    try
    {
      // OK, now we have the HTML, create a new browset tab.

      // See if there already is a tab for this browser
      // If no, add it
      // If yes, select that tab
      //
      tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_BROWSER);
      if (tabItem == null)
      {
        CTabFolder cTabFolder = tabfolder.getSwtTabset();
        browser = new AnalyzerBrowser(cTabFolder, spoon, urlString,isURL, true, url);
        browser.setModelId(modelName);
        browser.setDatabaseName(databaseName);
        tabItem = new TabItem(tabfolder, tabName, tabName);
        Image visualizeTabImage = 
          ImageUtil.getImageAsResource(spoon.getDisplay(),
              "ui/images/visualizer.png"); // , "ui/images/kettle_logo_small.png"
        tabItem.setImage(visualizeTabImage);
        tabItem.setControl(browser.getComposite());
        tabItem.addListener( this );
        tabfolder.addListener(this);

        spoon.delegates.tabs.addTab(new TabMapEntry(tabItem, tabName, browser, TabMapEntry.OBJECT_TYPE_BROWSER));
      }
      int idx = tabfolder.indexOf(tabItem);

      // keep the focus on the graph
      tabfolder.setSelected(idx);
      return true;
    } 
    catch (Throwable e)
    {
      e.printStackTrace();
    }
    return false;
  }
  public boolean canBeClosed() {
    // TODO Auto-generated method stub
    return true;
  }

  public Object getManagedObject() {
    // TODO Auto-generated method stub
    return null;
  }

  public EngineMetaInterface getMeta() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasContentChanged() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setControlStates() {
    // TODO Auto-generated method stub
    
  }

  public int showChangedWarning() {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean applyChanges() {
    // TODO Auto-generated method stub
    return false;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public void modifyText(ModifyEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  public static void quickVisualize( DatabaseMeta databaseMeta, String modelName, String tableName, String sql ) {
    
    Database db = new Database( databaseMeta );
    RowMetaInterface rowMeta = null;
    try
    {
          db.connect();
          rowMeta = db.getQueryFields(sql, false);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if( rowMeta != null ) {
      try {
        // create a flat model
        ModelGenerator modelGenerator = new ModelGenerator();
        //modelGenerator.generateFlatModel(modelName, rowMeta, databaseMeta, "en_US", "joe", tableName);
        
        // visualize
        ModelServerPublish publisher = new ModelServerPublish();
        publisher.publishToServer( modelName+".mondrian.xml", databaseMeta.getDatabaseName(), modelName, false );
        VisualizeCanvas.openVisualizer(modelName, databaseMeta.getDatabaseName(), null);
        
      } catch (Exception e) {
        e.printStackTrace();
      }
      
    }
    
  }
  
  public static void quickVisualize( TransMeta transMeta, StepMeta stepMeta ) {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
//    TransMeta transMeta = spoon.getActiveTransformation();
    if( transMeta == null ) {
      SpoonFactory.getInstance().messageBox( "This must be open from a transformation", "Modeler", false, Const.ERROR);
      return;
    }
    StepMeta steps[] = transMeta.getSelectedSteps();
    if( steps == null || steps.length > 1 ) {
      SpoonFactory.getInstance().messageBox( "One (and only one) step must be selected", "Modeler", false, Const.ERROR);
      return;
    }
    
    // assume only one selected 
//    StepMeta stepMeta = steps[0];
    if( !(stepMeta.getStepMetaInterface() instanceof TableOutputMeta) ) {
      SpoonFactory.getInstance().messageBox( "A Table Output step must be selected", "Modeler", false, Const.ERROR);
      return;
    }
    RowMetaInterface rowMeta = null;
    try {
      rowMeta = transMeta.getStepFields(stepMeta);
    } catch (KettleException e) {
      e.printStackTrace();
      SpoonFactory.getInstance().messageBox( "Could not get transformation step metadata", "Modeler", false, Const.ERROR);
      return;
    }

    TableOutputMeta tableOutputMeta = (TableOutputMeta) stepMeta.getStepMetaInterface();
    DatabaseMeta databaseMeta = tableOutputMeta.getDatabaseMeta();
    String tableName = tableOutputMeta.getTablename();
    String modelName = stepMeta.getName();

    try {
      // create a flat model
      ModelGenerator modelGenerator = new ModelGenerator();
//      modelGenerator.generateFlatModel(modelName, rowMeta, databaseMeta, "en_US", "joe", tableName);
      
      // visualize
      ModelServerPublish publisher = new ModelServerPublish();
      publisher.publishToServer( modelName+".mondrian.xml", databaseMeta.getDatabaseName(), modelName, false );
      VisualizeCanvas.openVisualizer(modelName, databaseMeta.getDatabaseName(), null) ;
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  public boolean tabClose(TabItem item) {
    // TODO Auto-generated method stub
    return true;
  }

  public void tabDeselected(TabItem item) {
    System.out.println("tabDeselected");
    if( item == this.tabItem ) {
      
    }
  }

  public void tabSelected(TabItem item) {
    // TODO Auto-generated method stub
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }
  
  public void setUrl(String aUrl) {
  	this.url = aUrl;
  }
  
}
