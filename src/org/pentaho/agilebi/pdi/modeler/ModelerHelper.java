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
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.agilebi.pdi.perspective.AgileBiPerspective;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.xul.swt.tab.TabItem;
import org.w3c.dom.Node;

public class ModelerHelper extends AbstractXulEventHandler {

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
    
    AgileBiPerspective.getInstance().createTabForModel(model, getUniqueUntitledTabName(spoon, MODELER_NAME));
  }
  
  public void createModelerTabFromSource( IModelerSource source ) throws ModelerException {

    Spoon spoon = ((Spoon)SpoonFactory.getInstance());

    ModelerWorkspace model = new ModelerWorkspace();
    model.setModelSource(source);
    ModelerWorkspaceUtil.populateModelFromSource(model, source);
    
    // create unique name
    AgileBiPerspective.getInstance().createTabForModel(model, getUniqueUntitledTabName(spoon, MODELER_NAME));
  }

  // TODO: replace this code after M1
  public Domain loadDomain(String fname){
    try{
      ModelerWorkspace model = new ModelerWorkspace();
      String xml = new String(IOUtils.toByteArray(new FileInputStream(new File(fname))), "UTF-8"); //$NON-NLS-1$
      ModelerWorkspaceUtil.loadWorkspace(fname, xml, model);
      return model.getDomain();
    } catch(ModelerException e){
      e.printStackTrace();
    } catch(IOException e){
      e.printStackTrace();
    }
    return null;
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

  public String getName(){
    return "agileBi"; //$NON-NLS-1$
  }
  
  public void openModeler() {

    try{
      ModelerHelper.getInstance().createModelerTabFromOutputStep();
      SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiPerspective.class);
      
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

          AgileBiPerspective.getInstance().createTabForModel(model, getUniqueUntitledTabName(spoon, MODELER_NAME));
          
        } catch(Exception e){
          e.printStackTrace();
          SpoonFactory.getInstance().messageBox( "Could not create a modeler: "+e.getLocalizedMessage(), "Modeler Error", false, Const.ERROR);
        }
      }
    }
  }  
    
}
