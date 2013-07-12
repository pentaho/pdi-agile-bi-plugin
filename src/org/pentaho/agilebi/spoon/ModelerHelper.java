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
package org.pentaho.agilebi.spoon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceUtil;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.agilebi.spoon.perspective.AgileBiModelerPerspective;
import org.pentaho.agilebi.spoon.visualizations.IVisualization;
import org.pentaho.agilebi.spoon.visualizations.VisualizationManager;
import org.pentaho.agilebi.spoon.wizard.EmbeddedWizard;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.ProvidesDatabaseConnectionInformation;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.ISpoonMenuController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.registry.IMetadataRegistry;
import org.pentaho.metadata.registry.OrderedFileRegistry;
import org.pentaho.metadata.registry.RegistryFactory;
import org.pentaho.metadata.registry.Type;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.WaitBoxRunnable;
import org.pentaho.ui.xul.components.XulWaitBox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.xul.swt.tab.TabItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelerHelper extends AbstractXulEventHandler implements ISpoonMenuController {

  private static final String MODELER_NAME = "Model Editor"; 
  private static final String TEMP_MODELS_FOLDER = "models";

  private static ModelerHelper instance = null;
    
  private static Logger logger = LoggerFactory.getLogger(ModelerHelper.class);

  static{

    try {
      ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
      
      RegistryFactory factory = RegistryFactory.getInstance();
      IMetadataRegistry registry = factory.getMetadataRegistry();
      if( registry == null ) {
          try {
        	  registry = new OrderedFileRegistry();
        	  ((OrderedFileRegistry) registry).setFilePath(org.pentaho.di.core.Const.getKettleDirectory() + File.separator + "registry.xml"); //$NON-NLS-1$
			factory.setMetadataRegistry(registry);
			registry.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      
    } catch (IllegalStateException e) {
      // someone else set this first, ignore this error
    }
  }
  private ModelerHelper() {
    File modelsDir = new File(TEMP_MODELS_FOLDER);
    if(modelsDir.exists()) {
      for(File file : modelsDir.listFiles()) {
        file.delete();
      }
      modelsDir.delete();
    }
  }
  
  public static synchronized ModelerHelper getInstance() {
    if( instance == null ) {
      instance = new ModelerHelper();
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      spoon.addSpoonMenuController(instance);
    }
    return instance;
  }

  /**
   * this method is used to see if a valid transformation step or job entry is selected in a graph before attempting to
   * model or quick vis
   *
   * @return true if valid
   */
  public static boolean isValidEntrySelected() {
    return getDatabaseConnectionInformationForCurrentActiveEntry() != null;
  }

  /**
   * Determines if we have valid connection information. "Valid" means we have enough information to build a model with.
   * This is, at a minimum, a non-null database meta and a non-empty table name.
   *
   * @param meta Database meta to check for validity
   * @param tableName Table name to check for validity
   * @return {@code true} if {@code meta} is not null and {@code tableName} is not empty.
   */
  public static boolean isValidConnectionInformation(DatabaseMeta meta, String tableName) {
    return meta != null && !Const.isEmpty(tableName);
  }

  /**
   * This uses {@link #isValidEntrySelected()} and shows an error dialog if not.
   * @return {@code true} if a valid entry is selected to model or quick vis
   */
  private static boolean isValidEntrySelectedWithLogging() {
    if (!isValidEntrySelected()) {
      showFriendlyModelerException(new ModelerException(BaseMessages.getString(ModelerHelper.class, "InvalidEntrySelected")));
      return false;
    }
    return true;
  }

  /**
   * Attempt to resolve the database connection information from the currently selected transformation or job.
   *
   * @return A {@link ProvidesDatabaseConnectionInformation} instance populated database connection information.
   */
  private static ProvidesDatabaseConnectionInformation getDatabaseConnectionInformationForCurrentActiveEntry() {
    Spoon spoon= (Spoon) SpoonFactory.getInstance();

    EngineMetaInterface metaInterface = spoon.getActiveMeta();
    ProvidesDatabaseConnectionInformation connectionInfo = null;
    if (metaInterface != null) {
      if (metaInterface instanceof TransMeta) {
        connectionInfo = getDatabaseConnectionInformationForCurrentTransStep(spoon);
      } else if (metaInterface instanceof JobMeta) {
        connectionInfo = getDatabaseConnectionInformationForJobEntry(spoon);
      }
    }
    return connectionInfo;
  }

  /**
   * Get the current step from the transformation graph if it is currently "active".
   *
   * @param spoon Spoon instance to get look up current step from
   * @return Current step if the transformation graph is active
   */
  protected static StepMeta getCurrentStepMeta(Spoon spoon) {
    if (spoon.getActiveTransGraph() == null) {
      return null;
    }
    return spoon.getActiveTransGraph().getCurrentStep();
  }

  /**
   * Get the current job entry from the job graph if it is currently "active".
   *
   * @param spoon Spoon instance to look up current job entry from
   * @return Current job entry if the job graph is active
   */
  protected static JobEntryCopy getCurrentJobEntry(Spoon spoon) {
    if (spoon.getActiveJobGraph() == null) {
      return null;
    }
    return spoon.getActiveJobGraph().getJobEntry();
  }

  /**
   * Get the database connection information for the active transformation.
   *
   * @param spoon Current instance of Spoon.
   * @return A valid {@link ProvidesDatabaseConnectionInformation} with database connection information from the currently selected
   *         transformation step
   */
  protected static ProvidesDatabaseConnectionInformation getDatabaseConnectionInformationForCurrentTransStep(Spoon spoon) {
    StepMeta stepMeta = getCurrentStepMeta(spoon);
    // see if we can get the objects we need
    return stepMeta == null ? null : getDatabaseConnectionInformation(stepMeta.getStepMetaInterface());
  }

  /**
   * Get the database connection information for the active job.
   *
   * @param spoon Current instance of Spoon.
   * @return A valid {@link ProvidesDatabaseConnectionInformation} with database connection information for the currently selected
   *         job entry
   */
  protected static ProvidesDatabaseConnectionInformation getDatabaseConnectionInformationForJobEntry(Spoon spoon) {
    JobEntryCopy entry = getCurrentJobEntry(spoon);
    // see if we can get the objects we need
    return entry == null ? null : getDatabaseConnectionInformation(entry.getEntry());
  }

  /**
   * Creates a modeler source that can build a model for the currently selected transformation step or job entry.
   *
   * @param connectionInfo Database Connection information
   * @param spoon Spoon instance to look up active step or entry from
   * @return A valid modeler source for the currently selected transformation step or job entry, or {@code null} if no valid active item could be found.
   */
  protected static KettleModelerSource createSourceForActiveSelection(ProvidesDatabaseConnectionInformation connectionInfo, Spoon spoon) {
    EngineMetaInterface metaInterface = spoon.getActiveMeta();
    KettleModelerSource source = null;
    if (metaInterface != null) {
      if (metaInterface instanceof TransMeta) {
        source = getModelerSourceForCurrentTransStep(connectionInfo, spoon);
      } else if (metaInterface instanceof JobMeta) {
        source = getModelerSourceForCurrentJobEntry(connectionInfo, spoon);
      }
    }
    return source;
  }

  /**
   * Create a modeler source for the current transformation step.
   *
   * @param connectionInfo Connection information
   * @param spoon Spoon instance to look for the currently active transformation step
   * @return A modeler source for the current transformation step so we can build a model for it
   */
  protected static KettleModelerSource getModelerSourceForCurrentTransStep(ProvidesDatabaseConnectionInformation connectionInfo, Spoon spoon) {
    StepMeta stepMeta = getCurrentStepMeta(spoon);
    if (stepMeta == null) {
      return null;
    }
    return getModelerSourceForStepMeta(connectionInfo, spoon.getActiveTransformation(), stepMeta);
  }

  /**
   * Create a modeler source for the current transformation step.
   *
   * @param connectionInfo Connection information
   * @param transMeta Tranformation the step belongs to
   * @param stepMeta Step to create modeler source for
   * @return A modeler source for the current transformation step so we can build a model for it
   */
  protected static KettleModelerSource getModelerSourceForStepMeta(ProvidesDatabaseConnectionInformation connectionInfo, TransMeta transMeta, StepMeta stepMeta) {
    Repository repository = transMeta.getRepository();
    String repositoryName = repository == null ? null : repository.getName();
    return new OutputStepModelerSource(connectionInfo.getDatabaseMeta(), connectionInfo.getTableName(), connectionInfo.getSchemaName(), transMeta.getName(), transMeta.getFilename(), repositoryName, stepMeta.getStepID());
  }

  /**
   * Create a modeler source for the current job entry.
   *
   * @param connectionInfo Connection information
   * @param spoon Spoon instance to look for the currently active job entry
   * @return A modeler source for the current job entry so we can build a model for it
   */
  protected static KettleModelerSource getModelerSourceForCurrentJobEntry(ProvidesDatabaseConnectionInformation connectionInfo, Spoon spoon) {
    JobEntryCopy entry = getCurrentJobEntry(spoon);
    if (entry == null) {
      return null;
    }
    return getModelerSourceForJobEntry(connectionInfo, spoon.getActiveJob(), entry);
  }

  /**
   * Create a modeler source for the job entry.
   *
   * @param connectionInfo Connection information
   * @param jobMeta Job the job entry belongs to
   * @param entry Job Entry to create modeler source for
   * @return A modeler source for the current job entry so we can build a model for it
   */
  protected static KettleModelerSource getModelerSourceForJobEntry(ProvidesDatabaseConnectionInformation connectionInfo, JobMeta jobMeta, JobEntryCopy entry) {
    Repository repository = jobMeta.getRepository();
    String repositoryName = repository == null ? null : repository.getName();
    return new KettleModelerSource(connectionInfo.getDatabaseMeta(), connectionInfo.getTableName(), connectionInfo.getSchemaName(), Type.TYPE_JOB, jobMeta.getName(), jobMeta.getFilename(), repositoryName, entry.getEntry().getPluginId());
  }

  /**
   * Get the database connection information from an object that implements {@link ProvidesDatabaseConnectionInformation}.
   *
   * @param o Object that implements {@link ProvidesDatabaseConnectionInformation} and has database connection information.
   * @return Database connection information from {@code o}.
   */
  protected static ProvidesDatabaseConnectionInformation getDatabaseConnectionInformation(Object o) {
	  if( o != null && ProvidesDatabaseConnectionInformation.class.isAssignableFrom(o.getClass()) ) {
      return ProvidesDatabaseConnectionInformation.class.cast(o);
	  }
    return null;
  }
  
  public static ModelerWorkspace populateModel(ModelerWorkspace model) throws ModelerException {

    if (!isValidEntrySelected()) {
      throw new ModelerException(BaseMessages.getString(ModelerHelper.class, "InvalidEntrySelected"));
    }

    ProvidesDatabaseConnectionInformation connectionInfo = getDatabaseConnectionInformationForCurrentActiveEntry();
		// we must have a database meta and a table name to continue, schema is optional
    if( !isValidConnectionInformation(connectionInfo.getDatabaseMeta(), connectionInfo.getTableName()) ) {
      String errorMsg = Const.NVL(connectionInfo.getMissingDatabaseConnectionInformationMessage(), BaseMessages.getString(ModelerHelper.class, "DatabaseConnectionInformationRequired"));
      throw new ModelerException(errorMsg);
    }

    KettleModelerSource source = createSourceForActiveSelection(connectionInfo, (Spoon) SpoonFactory.getInstance());

    if (source == null) {
      throw new ModelerException(BaseMessages.getString(ModelerHelper.class, "Error.NoModelerSource", connectionInfo.getDatabaseMeta().getName(), connectionInfo.getTableName()));
    }

    Domain d = source.generateDomain();

    model.setModelSource(source);
    model.setModelName(connectionInfo.getTableName());
    model.setDomain(d);

    RegistryFactory factory = RegistryFactory.getInstance();
    IMetadataRegistry registry = factory.getMetadataRegistry();

    source.registerLineageMetadata(registry);

    try {
		registry.commit();
	} catch (Exception e) {
		logger.error("Could not commit metadata registry", e);
	}
    
    return model;
  }


  public void createModelerTab() throws ModelerException {
    ModelerWorkspace model = createModelerWorkspace();
    
    populateModel(model);
    
    AgileBiModelerPerspective.getInstance().createTabForModel(model, MODELER_NAME);
    
  }
  
  public void createModelerTabFromSource( IModelerSource source ) throws ModelerException {

    Spoon spoon = ((Spoon)SpoonFactory.getInstance());

    ModelerWorkspace model = createModelerWorkspace();
    model.setModelSource(source);
    ModelerWorkspaceUtil.populateModelFromSource(model, source);
    
    // create unique name
    AgileBiModelerPerspective.getInstance().createTabForModel(model, getUniqueUntitledTabName(spoon, MODELER_NAME));
  }

  // TODO: replace this code after M1
  public Domain loadDomain(String fname){
    try{
      ModelerWorkspace model = createModelerWorkspace();
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
    if (!isValidEntrySelectedWithLogging()) {
      return;
    }

    try{
      ModelerHelper.getInstance().createModelerTab();
      SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiModelerPerspective.class);
    } catch (ModelerException e) {
      showFriendlyModelerException(e);
    } catch(Exception e){
      logger.error("Error opening modeler", e);
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating modeler", e); 
    }
  }

  public void quickVisualizeTable() {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    if( spoon.getSelectionObject() instanceof DatabaseMeta ) {
      final DatabaseMeta databaseMeta = (DatabaseMeta) spoon.getSelectionObject();
      
      DatabaseExplorerDialog std = new DatabaseExplorerDialog(spoon.getShell(), SWT.NONE, databaseMeta, new ArrayList<DatabaseMeta>());
      if (std.open()) {
          
        TableModelerSource source = new TableModelerSource( databaseMeta, std.getTableName(), std.getSchemaName() == null ? "" : std.getSchemaName() ); //$NON-NLS-1$
        if( source.getSchemaName() == null ) {
          source.setSchemaName(""); //$NON-NLS-1$
        }

        try{
          ModelerWorkspace model = createModelerWorkspace();
          ModelerWorkspaceUtil.populateModelFromSource(model, source);
          quickVisualize( model );
        } catch(Exception e){
          logger.error("Error opening visualization", e);
          new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error executing Quick Visualize", e);
        }
      }
    }
  }

  public void quickVisualizeTableOutputStep() {
    if (!isValidEntrySelectedWithLogging()) {
      return;
    }

    try{
      ModelerWorkspace model = createModelerWorkspace();
      populateModel(model);
      quickVisualize( model );
    } catch (ModelerException e) {
      showFriendlyModelerException(e);
    } catch(Exception e){
      logger.error("Error visualizing", e);
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e);
    }

  }

  /**
   * Displays a friendly error message box with the {@link ModelerException}'s message as the text.
   *
   * @param e Exception with message to display
   */
  private static void showFriendlyModelerException(ModelerException e) {
    logger.error(e.getMessage());
    SpoonFactory.getInstance().messageBox( e.getMessage() , MODELER_NAME, false, Const.ERROR);
  }

  public void reportWizard() {
    if (!isValidEntrySelectedWithLogging()) {
      return;
    }

    XulWaitBox box;
    try {
      box = (XulWaitBox) document.createElement("waitbox");
      box.setIndeterminate(true);
      box.setCanCancel(false);
      box.setTitle(BaseMessages.getString(ModelerWorkspace.class, "wait_dialog_title"));
      box.setMessage(BaseMessages.getString(ModelerWorkspace.class, "wait_dialog_message"));
      
      box.setCancelLabel(BaseMessages.getString(ModelerWorkspace.class, "wait_dialog_btn"));
      
      box.setDialogParent(((Spoon)SpoonFactory.getInstance()).getShell());
      box.setRunnable(new WaitBoxRunnable(box){
        boolean canceled = false;
        @Override
        public void run() {
          
          try {
            ModelerWorkspace model = createModelerWorkspace();
            populateModel(model);

            ObjectUtilities.setClassLoader(getClass().getClassLoader());
            ObjectUtilities.setClassLoaderSource(ObjectUtilities.CLASS_CONTEXT);
            
            if(ClassicEngineBoot.getInstance().isBootDone() == false){
              ClassicEngineBoot engineBoot = ClassicEngineBoot.getInstance();
              engineBoot.start();
            }
            createTemporaryModel(model, true, true);
            EmbeddedWizard wizard = new EmbeddedWizard(model, true);
            waitBox.stop();
            wizard.run(null);
          } catch (ModelerException e) {
            logger.error("Error booting reporting engine", e);
            showFriendlyModelerException(e);
          } catch (final Exception e) {
            logger.error("Error booting reporting engine", e);
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
      logger.error("error creating PRPT", e1);
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e1);
    }


  }
  
  public void quickVisualize( ModelerWorkspace model, String name, String fileName ) throws ModelerException {
	    createTemporaryModel(model, true, true);
	    VisualizationManager theManager = VisualizationManager.getInstance();
	    IVisualization theVisualization = theManager.getVisualization(theManager.getVisualizationNames().get(0));
	    if(theVisualization != null) {
	      if (model.getFileName() != null) {
	        // TODO: Find a better name for the cube, maybe just model name?
	        theVisualization.createVisualizationFromModel(model, true);
	        Spoon.getInstance().enableMenus();
	      } else {
	        throw new UnsupportedOperationException("TODO: prompt to save model before visualization");
	      }
	    }
	  }

  public void quickVisualize( ModelerWorkspace model ) throws ModelerException {
	    createTemporaryModel(model, true, true);
	    VisualizationManager theManager = VisualizationManager.getInstance();
	    IVisualization theVisualization = theManager.getVisualization(theManager.getVisualizationNames().get(0));
	    if(theVisualization != null) {
	      if (model.getFileName() != null) {
	        // TODO: Find a better name for the cube, maybe just model name?
	        theVisualization.createVisualizationFromModel(model, true);
	        Spoon.getInstance().enableMenus();
	      } else {
	        throw new UnsupportedOperationException("TODO: prompt to save model before visualization");
	      }
	    }
	  }

  public String createTemporaryModel(ModelerWorkspace model, boolean saveName, boolean autoModel) throws ModelerException {
    //give it a temporary name
    File modelsDir = new File(TEMP_MODELS_FOLDER); //$NON-NLS-1$
    modelsDir.mkdirs();
    int idx = 1;
    boolean looking = true;
    File modelFile;
    String fileName = ""; //$NON-NLS-1$
    String modelName = ""; //$NON-NLS-1$
    while( looking ) {
      modelName = "Model "+idx; //$NON-NLS-1$
      fileName = TEMP_MODELS_FOLDER + "/" + modelName+".xmi"; //$NON-NLS-1$ //$NON-NLS-2$
      modelFile = new File(fileName);
      if( !modelFile.exists() ) {
        looking = false;
      }
      idx++;
    }
    if(saveName){
      model.setFileName(fileName);
    }
    model.setModelName(modelName);
    if(autoModel){
      model.getWorkspaceHelper().autoModelFlat(model);
    }
    model.getWorkspaceHelper().populateDomain(model);
    ModelerWorkspaceUtil.saveWorkspace( model, fileName);
    
    /*
    // link the model to the transformation
    RegistryFactory factory = RegistryFactory.getInstance();
    IMetadataRegistry registry = factory.getMetadataRegistry();
    Entity modelEntity = new Entity(fileName, model.getModelName(), RegistryBase.TYPE_OLAP_MODEL.getId());
    Entity transEntity = new Entity(transFileName, name, RegistryBase.TYPE_TRANSFORMATION.getId());
    registry.addEntity(transEntity.getId(), transEntity);
    registry.addEntity(modelEntity.getId(), modelEntity);
//    Link link = new Link(modelEntity, null, transEntity); 
 */
    return fileName;
  }
  
  public ModelerWorkspace clone(ModelerWorkspace model) throws ModelerException{
    String fileName = createTemporaryModel(model, false, false);
    
    ModelerWorkspace newModel = createModelerWorkspace();
    
    newModel.setTemporary(true);
    newModel.setDirty(false);
    String xml;
    try {
      xml = new String(IOUtils.toByteArray(new FileInputStream(new File(fileName))), "UTF-8");
      ModelerWorkspaceUtil.loadWorkspace(fileName, xml, newModel);
    } catch (Exception e) {
      throw new ModelerException(e);
    } //$NON-NLS-1$
    return newModel;
  }
  
  public void databaseModelItem() {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    if( spoon.getSelectionObject() instanceof DatabaseMeta ) {
      final DatabaseMeta databaseMeta = (DatabaseMeta) spoon.getSelectionObject();
      
      DatabaseExplorerDialog std = new DatabaseExplorerDialog(spoon.getShell(), SWT.NONE, databaseMeta, new ArrayList<DatabaseMeta>());
      if (std.open()) {
          
        TableModelerSource source = new TableModelerSource( databaseMeta, std.getTableName(), std.getSchemaName());
        try{
          ModelerWorkspace model = createModelerWorkspace();
          ModelerWorkspaceUtil.populateModelFromSource(model, source);
          AgileBiModelerPerspective.getInstance().createTabForModel(model, getUniqueUntitledTabName(spoon, MODELER_NAME));
        } catch(Exception e){
          logger.error("Error creating model", e);
          new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating Modeler", e);
        }
      }
    }
  }  
  
  public void createEmptyModel() {
    try {
      ModelerWorkspace model = createModelerWorkspace();
      AgileBiModelerPerspective.getInstance().createTabForModel(model, MODELER_NAME);
      SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiModelerPerspective.class);
    } catch (Exception e) {
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e);
    }
  }

  private ModelerWorkspace createModelerWorkspace() {
    return new ModelerWorkspace(new SpoonModelerWorkspaceHelper(), SpoonModelerWorkspaceHelper.initGeoContext());
  }

  public void updateMenu(Document doc) {
	
//	  boolean isDisabled = !isValidEntrySelected();
//	  XulComponent menuItem = getXulDomContainer().getDocumentRoot().getElementById("trans-graph-entry-model");
//	  if (menuItem != null) {
//		  menuItem.setDisabled(isDisabled);
//	  }
//
//	  menuItem = getXulDomContainer().getDocumentRoot().getElementById("trans-graph-entry-visualize");
//	  if (menuItem != null) {
//		  menuItem.setDisabled(isDisabled);
//	  }
  }
}
