/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.pdi.modeler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.ui.xul.components.XulFileDialog.RETURN_CODE;
import org.pentaho.ui.xul.components.XulFileDialog.SEL_TYPE;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;
import org.w3c.dom.Node;


public class ModelerCanvas implements TabItemInterface, FileListener {

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
  
  private ModelerEngineMeta meta;
  
  private String tableName;
  
  private XulUI xul;

  private String fileName;
  
  private ModelerController controller;
  
  private TabItem tabItem;
  
  private static Log logger = LogFactory.getLog(ModelerCanvas.class);
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
  
  private ModelerCanvas(){
  }
  
  public static ModelerCanvas getInstance() {
    if( instance == null ) {
      instance = new ModelerCanvas();
    }
    return instance;
  }
  
  public static void openModeler() throws ModelerException {
    // find the source step...
   
    
    ModelerCanvas modelerCanvas = ModelerCanvas.getInstance();
    
    modelerCanvas.createModelerTab();
  }
  
  public void createModelerTabFromOutputStep() throws ModelerException {

    controller = new ModelerController();
    ModelerWorkspace  model = ModelerWorkspaceUtil.createModelFromOutputStep(controller.getModel());
    createModelerTab();
  }
  
  public void createModelerTab() throws ModelerException {

    
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    
    TabSet tabSet = spoon.getTabSet();
    TabSet tabfolder = spoon.tabfolder;
    CTabFolder cTabFolder = tabfolder.getSwtTabset();
    tabItem = new TabItem(tabfolder, MODELER_NAME, MODELER_NAME);
    Image modelTabImage = 
      ImageUtil.getImageAsResource(spoon.getDisplay(),
          "ui/images/modeler.png");

    tabItem.setImage(modelTabImage);

    if(controller == null){

      controller = new ModelerController();
    }
    
    try{
      xul = new XulUI(spoon.getShell(), controller);
    } catch(ModelerException e){
      Spoon.getInstance().getLog().logError("AGILE BI Modeler", e.getLocalizedMessage(), e);
    }
    Composite comp = xul.getMainPanel();
    comp.setParent(cTabFolder);
    tabItem.setControl(comp);
    
    TabMapEntry entry = new TabMapEntry(tabItem, MODELER_NAME, this, TabMapEntry.OBJECT_TYPE_BROWSER);
    
    spoon.delegates.tabs.addTab(entry);
    tabSet.addTab(tabItem);

    int idx = tabfolder.indexOf(tabItem);
    meta = new ModelerEngineMeta(controller, this);
    // keep the focus on the graph
    tabfolder.setSelected(idx);
  }
  
  public boolean canBeClosed() {
    // TODO Auto-generated method stub
    return true;
  }
  

  public boolean canHandleSave() {
    return true;
  }
  
  public String getFileName(){
    return fileName;
  }
  
  public void setFileName(String name){
    this.fileName = name;
    this.tabItem.setText(createShortName(name));
  }
    

  public boolean open(Node transNode, String fname, boolean importfile) {
    try{
      createModelerTab();
      String xml = new String(IOUtils.toByteArray(new FileInputStream(new File(fname))), "UTF-8");
      ModelerWorkspaceUtil.loadWorkspace(fname, xml, controller.getModel());
      this.tabItem.setText(createShortName(fname));
    } catch(ModelerException e){
      e.printStackTrace();
    } catch(IOException e){
      e.printStackTrace();
    }
    
    return true;
  }

  private String createShortName( String filename ) {
    if( filename == null ) {
      return null;
    }
    int extensionPos = filename.lastIndexOf('.');
    if( extensionPos == -1 ) {
      extensionPos = filename.length();
    }
    int sepPos = filename.replace('\\', '/').lastIndexOf('/');
    if( sepPos == -1 ) {
      sepPos = 0;
    } else {
      sepPos++;
    }
    return filename.substring(sepPos, extensionPos);    
  }
  
  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    setFileName(fname);
    try {
      controller.saveWorkspace(fname);
      return true;
    } catch (ModelerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    return false;
  }

  public void syncMetaName(EngineMetaInterface meta, String name) {
    // TODO Auto-generated method stub
    
  }

  public Object getManagedObject() {
    // TODO Auto-generated method stub
    return null;
  }

  public EngineMetaInterface getMeta() {
    return meta;
  }

  public boolean hasContentChanged() {
    return true;
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
  
  public boolean setFocus() {
    // TODO Auto-generated method stub
    return false;
  }
  
}
