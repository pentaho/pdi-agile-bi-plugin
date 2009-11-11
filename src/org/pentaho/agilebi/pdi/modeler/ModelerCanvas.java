package org.pentaho.agilebi.pdi.modeler;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.pms.core.exception.PentahoMetadataException;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulTabbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulMenu;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;


public class ModelerCanvas implements TabItemInterface {

  private static final String NEW_DIMENSION_NAME = "newDimensionName"; //$NON-NLS-1$

  private static final String NEW_DIMESION_DIALOG = "newDimesionDialog"; //$NON-NLS-1$

  private static final String MODELER_NAME = "modeler"; //$NON-NLS-1$

  private static final String MY_TAB_LIST_ID = "myTabList"; //$NON-NLS-1$

  private static final String FIELD_LIST_ID = "fieldList"; //$NON-NLS-1$

  private static final String IN_PLAY_TABLE_ID = "fieldTable"; //$NON-NLS-1$

  private static final String MODEL_NAME_FIELD_ID = "modelname"; //$NON-NLS-1$
  
  private static final String SOURCE_NAME_LABEL_ID = "source_name"; //$NON-NLS-1$

  private static final String MODEL_NAME_PROPERTY = "modelName"; //$NON-NLS-1$

  private static final String VALUE_PROPERTY = "value"; //$NON-NLS-1$

  private static final String IN_PLAY_FIELDS_PROPERTY = "inPlayFields"; //$NON-NLS-1$

  private static final String ELEMENTS_PROPERTY = "elements"; //$NON-NLS-1$

  private static final String FIELD_NAMES_PROPERTY = "fieldNames"; //$NON-NLS-1$

  private static ModelerCanvas instance = null;
  
  
  private String tableName;
  
  private XulUI xul;
  /**
   * returns the name identifier for this event handler that's used by the xul framework
   */
  public String getName(){
    return MODELER_NAME;
  }
  
  /** 
   * Called when the Xul Panel is loaded. grab references to xul elements here.
   */
  public void init(){
  }
  
  public static ModelerCanvas getInstance() {
    if( instance == null ) {
      instance = new ModelerCanvas();
    }
    return instance;
  }
  
  public static void openModeler() {
    // find the source step...
   
    
    ModelerCanvas modelerCanvas = ModelerCanvas.getInstance();
    
    modelerCanvas.createModelerTab();
  }
  
  
  public void createModelerTab() {

    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    TabSet tabSet = spoon.getTabSet();
    TabSet tabfolder = spoon.tabfolder;
    CTabFolder cTabFolder = tabfolder.getSwtTabset();
    TabItem tabItem = new TabItem(tabfolder, MODELER_NAME, MODELER_NAME);
    Image modelTabImage = 
      ImageUtil.getImageAsResource(spoon.getDisplay(),
          "ui/images/modeler.png");

    tabItem.setImage(modelTabImage);

    ModelerWorkspace model = null;
    try{
      model = ModelerWorkspaceUtil.createModelFromOutputStep();
    } catch (PentahoMetadataException e){
      e.printStackTrace();
      return;
    }
    
    ModelerController controller = new ModelerController(model);
    
    xul = new XulUI(spoon.getShell(), controller);
    Composite comp = xul.getMainPanel();
    comp.setParent(cTabFolder);
    tabItem.setControl(comp);
    
    TabMapEntry entry = new TabMapEntry(tabItem, MODELER_NAME, this, TabMapEntry.OBJECT_TYPE_BROWSER);
    
    spoon.delegates.tabs.addTab(entry);
    tabSet.addTab(tabItem);

    int idx = tabfolder.indexOf(tabItem);
    // keep the focus on the graph
    tabfolder.setSelected(idx);
    

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
  
  
}
