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
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.xul.swt.tab.TabItem;
import org.w3c.dom.Node;

public class ModelerHelper extends AbstractXulEventHandler implements FileListener {

  private static final String MODELER_NAME = "Modeler"; //$NON-NLS-1$

  private static ModelerHelper instance = null;
    
  private static Log logger = LogFactory.getLog(ModelerHelper.class);
  
  private ModelerHelper() {
  }
  
  public static ModelerHelper getInstance() {
    if( instance == null ) {
      instance = new ModelerHelper();
    }
    return instance;
  }

  
  public void createModelerTabFromOutputStep() throws ModelerException {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());

    ModelerWorkspace model = new ModelerWorkspace();
    
    ModelerWorkspaceUtil.populateModelFromOutputStep(model);
    
    XulUI xul = new XulUI(spoon.getShell(), model);

    // create unique name
    createModelerTab(spoon, xul, getUniqueUntitledTabName(spoon, MODELER_NAME));
  }
  
  public void createModelerTabFromSource( IModelerSource source ) throws ModelerException {

    Spoon spoon = ((Spoon)SpoonFactory.getInstance());

    ModelerWorkspace model = new ModelerWorkspace();
    model.setModelSource(source);
    ModelerWorkspaceUtil.populateModelFromSource(model, source);
    XulUI xul = new XulUI(spoon.getShell(), model);

    // create unique name
    createModelerTab(spoon, xul, getUniqueUntitledTabName(spoon, MODELER_NAME));
  }

  public boolean open(Node transNode, String fname, boolean importfile) {
    try{
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());

      ModelerWorkspace model = new ModelerWorkspace();
      
      XulUI xul = new XulUI(spoon.getShell(), model);
      TabItem tabItem = createModelerTab(spoon, xul, createShortName(fname));
      String xml = new String(IOUtils.toByteArray(new FileInputStream(new File(fname))), "UTF-8"); //$NON-NLS-1$
      ModelerWorkspaceUtil.loadWorkspace(fname, xml, model);
      tabItem.setText(createShortName(fname));
      File f = new File(fname);
      String fullPath = f.getAbsolutePath();
      spoon.getProperties().addLastFile("Model", fullPath, null, false, null);
      spoon.addMenuLast();

    } catch(ModelerException e){
      e.printStackTrace();
    } catch(IOException e){
      e.printStackTrace();
    }
    
    return true;
  }
  
  private String getUniqueUntitledTabName(Spoon spoon, String title) {
    int num = 1;
    String tabName = title + " " + num; //$NON-NLS-1$
    // TODO: Add new plugin object type to spoon
    TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_BROWSER);
    while (tabItem != null) {
      tabName = title + " " + (++num); //$NON-NLS-1$
      // TODO: Add new plugin object type to spoon
      tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_BROWSER);
    }
    return tabName;
  }
  
  private TabItem createModelerTab(Spoon spoon, XulUI xul, String tabName) throws ModelerException {
    CTabFolder cTabFolder = spoon.tabfolder.getSwtTabset();
    TabItem tabItem = new TabItem(spoon.tabfolder, tabName, tabName);
    Image modelTabImage = ImageUtil.getImageAsResource(spoon.getDisplay(), "plugins/spoon/agile-bi/ui/images/modeler.png"); //$NON-NLS-1$
    tabItem.setImage(modelTabImage);
    
    Composite comp = xul.getMainPanel();
    comp.setParent(cTabFolder);
    tabItem.setControl(comp);
    
    // TODO: Add new plugin object type to spoon
    TabMapEntry entry = new TabMapEntry(tabItem, tabName, xul, TabMapEntry.OBJECT_TYPE_BROWSER);
    
    spoon.delegates.tabs.addTab(entry);
    spoon.getTabSet().addTab(tabItem);

    int idx = spoon.tabfolder.indexOf(tabItem);

    // keep the focus on the graph
    spoon.tabfolder.setSelected(idx);
    
    return tabItem;
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
    try {
      ((ModelerEngineMeta)meta).getController().saveWorkspace(fname);
      return true;
    } catch (ModelerException e) {
      e.printStackTrace();
    } 
    return false;
  }

  public void syncMetaName(EngineMetaInterface meta, String name) {
    // TODO Auto-generated method stub
    
  }

  public String getName(){
    return "agileBi"; //$NON-NLS-1$
  }
  
  public void openModeler() {
    
    try{
      ModelerHelper.getInstance().createModelerTabFromOutputStep();
    } catch(Exception e){
      e.printStackTrace();
      SpoonFactory.getInstance().messageBox( "Could not create a modeler: "+e.getLocalizedMessage(), "Modeler Error", false, Const.ERROR);
    }
  }
  
  public void quickVisualizeTable() {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    if( spoon.getSelectionObject() instanceof DatabaseMeta ) {
      final DatabaseMeta databaseMeta = (DatabaseMeta) spoon.getSelectionObject();
      
      DatabaseExplorerDialog std = new DatabaseExplorerDialog(spoon.getShell(), SWT.NONE, databaseMeta, new ArrayList<DatabaseMeta>());
      std.setSplitSchemaAndTable(true);
      if (std.open() != null) {
          
        TableModelerSource source = new TableModelerSource( databaseMeta, std.getTableName(), std.getSchemaName() == null ? "" : std.getSchemaName() ); //$NON-NLS-1$
        if( source.getSchemaName() == null ) {
          source.setSchemaName(""); //$NON-NLS-1$
        }

        try{
          ModelerWorkspace model = new ModelerWorkspace();
          ModelerWorkspaceUtil.populateModelFromSource(model, source);
          quickVisualize( model );
        } catch(Exception e){
          e.printStackTrace();
          SpoonFactory.getInstance().messageBox( "Could not create a modeler: "+e.getLocalizedMessage(), "Modeler Error", false, Const.ERROR);
        }
      }
    }
  }

  public void quickVisualizeTableOutputStep() {
    try{
      ModelerWorkspace model = new ModelerWorkspace();
      ModelerWorkspaceUtil.populateModelFromOutputStep(model);
      quickVisualize( model );
    } catch(Exception e){
      e.printStackTrace();
      SpoonFactory.getInstance().messageBox( "Could not create a vizualizer: "+e.getLocalizedMessage(), "Vizualizer Error", false, Const.ERROR);
    }

  }
  
  public void quickVisualize( ModelerWorkspace model ) throws ModelerException {


    // give it a temporary name
    File modelsDir = new File("models"); //$NON-NLS-1$
    modelsDir.mkdirs();
    int idx = 1;
    boolean looking = true;
    File modelFile;
    String fileName = ""; //$NON-NLS-1$
    String modelName = ""; //$NON-NLS-1$
    while( looking ) {
      modelName = "Model "+idx; //$NON-NLS-1$
      fileName = "models/"+modelName+".xmi"; //$NON-NLS-1$ //$NON-NLS-2$
      modelFile = new File(fileName);
      if( !modelFile.exists() ) {
        looking = false;
      }
      idx++;
    }
    model.setFileName(fileName);
    model.setModelName(modelName);
    ModelerWorkspaceUtil.autoModelFlat(model);
    ModelerWorkspaceUtil.saveWorkspace( model, fileName);
    VisualizationManager theManager = VisualizationManager.getInstance();
    IVisualization theVisualization = theManager.getVisualization(theManager.getVisualizationNames().get(0));
    if(theVisualization != null) {
      if (model.getFileName() != null) {
        // TODO: Find a better name for the cube, maybe just model name?
        theVisualization.createVisualizationFromModel(model.getFileName(), model.getModelName() + " Cube");
      } else {
        throw new UnsupportedOperationException("TODO: prompt to save model before visualization");
      }
    }

  }
  
  public void databaseModelItem() {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    if( spoon.getSelectionObject() instanceof DatabaseMeta ) {
      final DatabaseMeta databaseMeta = (DatabaseMeta) spoon.getSelectionObject();
      
      DatabaseExplorerDialog std = new DatabaseExplorerDialog(spoon.getShell(), SWT.NONE, databaseMeta, new ArrayList<DatabaseMeta>());
      std.setSplitSchemaAndTable(true);
      if (std.open() != null) {
          
        TableModelerSource source = new TableModelerSource( databaseMeta, std.getTableName(), std.getSchemaName());
        try{
          ModelerWorkspace model = new ModelerWorkspace();
          ModelerWorkspaceUtil.populateModelFromSource(model, source);
          XulUI xul = new XulUI(spoon.getShell(), model);

          // create unique name
          createModelerTab(spoon, xul, getUniqueUntitledTabName(spoon, source.getTableName()));
        } catch(Exception e){
          e.printStackTrace();
          SpoonFactory.getInstance().messageBox( "Could not create a modeler: "+e.getLocalizedMessage(), "Modeler Error", false, Const.ERROR);
        }
      }
    }
  }
    
}
