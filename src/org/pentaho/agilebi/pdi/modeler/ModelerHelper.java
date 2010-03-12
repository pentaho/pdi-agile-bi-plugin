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
import org.eclipse.swt.widgets.Display;
import org.pentaho.agilebi.pdi.PDIMessages;
import org.pentaho.agilebi.pdi.perspective.AgileBiModelerPerspective;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
import org.pentaho.agilebi.pdi.wizard.EmbeddedWizard;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.metadata.model.Domain;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.WaitBoxRunnable;
import org.pentaho.ui.xul.components.XulWaitBox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.xul.swt.tab.TabItem;

public class ModelerHelper extends AbstractXulEventHandler {

  private static final String MODELER_NAME = "Modeler"; 

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
    
    AgileBiModelerPerspective.getInstance().createTabForModel(model, MODELER_NAME);
    
  }
  
  public void createModelerTabFromSource( IModelerSource source ) throws ModelerException {

    Spoon spoon = ((Spoon)SpoonFactory.getInstance());

    ModelerWorkspace model = new ModelerWorkspace();
    model.setModelSource(source);
    ModelerWorkspaceUtil.populateModelFromSource(model, source);
    
    // create unique name
    AgileBiModelerPerspective.getInstance().createTabForModel(model, getUniqueUntitledTabName(spoon, MODELER_NAME));
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
    TabItem tabItem = spoon.delegates.tabs.findTabMapEntry(tabName, TabMapEntry.ObjectType.BROWSER).getTabItem();
    while (tabItem != null) {
      tabName = title + " " + (++num); //$NON-NLS-1$
      // TODO: Add new plugin object type to spoon
      tabItem = spoon.delegates.tabs.findTabMapEntry(tabName, TabMapEntry.ObjectType.BROWSER).getTabItem();
    }
    return tabName;
  }

  public String getName(){
    return "agileBi"; //$NON-NLS-1$
  }
  
  public void openModeler() {

    try{
      ModelerHelper.getInstance().createModelerTabFromOutputStep();
      SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiModelerPerspective.class);
      
    } catch(Exception e){
      logger.error(e);
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating modeler", e); 
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
          logger.error(e);
          new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error executing Quick Visualize", e);
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
      logger.error(e);
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e);
    }

  }
  
  public void reportWizard() {
    XulWaitBox box;
    try {
      box = (XulWaitBox) document.createElement("waitbox");
      box.setIndeterminate(true);
      box.setMaximum(10);
      box.setCanCancel(false);
      box.setTitle(BaseMessages.getString(XulUI.class, "wait_dialog_title"));
      box.setMessage(BaseMessages.getString(XulUI.class, "wait_dialog_message"));
      
      box.setCancelLabel(BaseMessages.getString(XulUI.class, "wait_dialog_btn"));
      
      box.setDialogParent(((Spoon)SpoonFactory.getInstance()).getShell());
      box.setRunnable(new WaitBoxRunnable(box){
        boolean canceled = false;
        @Override
        public void run() {
          
          try {
            ModelerWorkspace model = new ModelerWorkspace();
            ModelerWorkspaceUtil.populateModelFromOutputStep(model);

            ObjectUtilities.setClassLoaderSource(ObjectUtilities.CLASS_CONTEXT);
            if(ClassicEngineBoot.getInstance().isBootDone() == false){
              ClassicEngineBoot engineBoot = ClassicEngineBoot.getInstance();
              engineBoot.start();
            }
            EmbeddedWizard wizard = new EmbeddedWizard(model);
            waitBox.stop();
            wizard.run(null);
          } catch (final Exception e) {
            logger.error(e);
            Display.getDefault().asyncExec(new Runnable(){
              public void run() {
                new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e);
              }
            });
          
          }
          waitBox.stop();
        }

        @Override
        public void cancel() {
          canceled =true;
        }
        
        
      });
      box.start();
    } catch (XulException e1) {
      logger.error(e1);
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e1);
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
    ModelerWorkspaceUtil.populateDomain(model);
    ModelerWorkspaceUtil.saveWorkspace( model, fileName);
    VisualizationManager theManager = VisualizationManager.getInstance();
    IVisualization theVisualization = theManager.getVisualization(theManager.getVisualizationNames().get(0));
    if(theVisualization != null) {
      if (model.getFileName() != null) {
        // TODO: Find a better name for the cube, maybe just model name?
        theVisualization.createVisualizationFromModel(model);
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
          AgileBiModelerPerspective.getInstance().createTabForModel(model, getUniqueUntitledTabName(spoon, MODELER_NAME));
        } catch(Exception e){
          logger.error(e);
          new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating Modeler", e);
        }
      }
    }
  }  
    
  public void editBiServerConfig() {
    
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    try {
    XulDialogBiServerList biServerConfigDialog = new XulDialogBiServerList( spoon.getShell() );
    biServerConfigDialog.showDialog();
    } catch (XulException e) {
      logger.error(e);
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Could not create dialog", e);
    }
  }
  
  public void createEmptyModel() {
    try {
      ModelerWorkspace model = new ModelerWorkspace();
      AgileBiModelerPerspective.getInstance().createTabForModel(model, MODELER_NAME);
      SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiModelerPerspective.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
}
