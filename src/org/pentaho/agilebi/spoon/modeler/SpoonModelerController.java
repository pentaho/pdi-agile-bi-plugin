package org.pentaho.agilebi.spoon.modeler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.agilebi.modeler.*;
import org.pentaho.agilebi.spoon.ModelerWorkspaceUtil;
import org.pentaho.agilebi.spoon.TableModelerSource;
import org.pentaho.agilebi.spoon.publish.PublisherHelper;
import org.pentaho.agilebi.spoon.visualizations.IVisualization;
import org.pentaho.agilebi.spoon.visualizations.VisualizationManager;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.MainSpoonPerspective;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.spoon.delegates.SpoonDBDelegate;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jun 14, 2010
 * Time: 1:14:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpoonModelerController extends ModelerController {
  private ModelerControllerDBRegistry databaseInterface = new ModelerControllerDBRegistry();
  private static Log logger = LogFactory.getLog(SpoonModelerController.class);
  private XulMenuList visualizationList;

  private List<String> visualizationNames;
  private Binding visualizationsBinding;

  public void init() throws ModelerException{

    visualizationList = (XulMenuList)document.getElementById("visualizationlist"); //$NON-NLS-1$
    bf.createBinding(workspace, "selectedVisualization", visualizationList, "selectedItem"); //$NON-NLS-1$//$NON-NLS-2$
       visualizationsBinding = bf.createBinding(this, "visualizationNames", visualizationList, "elements"); //$NON-NLS-1$//$NON-NLS-2$

    super.init();
  }

  public void editDataSource() {
    try {
      Spoon theSpoon = Spoon.getInstance();
      Repository theRepository = theSpoon.getRepository();

      List<DatabaseMeta> theDatabases = new ArrayList<DatabaseMeta>();

      EngineMetaInterface theMeta = null;
      HasDatabasesInterface theDatabasesInterface = null;
      List<SpoonPerspective> thePerspectives = SpoonPerspectiveManager.getInstance().getPerspectives();
      for (SpoonPerspective thePerspective : thePerspectives) {
        if(thePerspective instanceof MainSpoonPerspective) {
           theMeta = thePerspective.getActiveMeta();
           break;
        }
      }
      if(theMeta != null) {
        theDatabasesInterface = (HasDatabasesInterface) theMeta;
      } else {
        theDatabasesInterface = this.databaseInterface;
      }

      if(theRepository != null) {
        TransMeta theTransMeta = new TransMeta();
        theRepository.readTransSharedObjects(theTransMeta);
        theDatabases.addAll(theTransMeta.getDatabases());
        theDatabasesInterface.setDatabases(theDatabases);
      } else {
        theDatabases.addAll(theDatabasesInterface.getDatabases());
      }

      String theSelectedTable = null;
      IModelerSource theModelerSource = this.workspace.getModelSource();
      if(theModelerSource != null) {
        theSelectedTable = theModelerSource.getDatabaseName();
      }
      int[] theSelectedIndexes = new int[1];
      String[] theNames = new String[theDatabases.size()];
      for (int i = 0; i < theDatabases.size(); i++) {
        theNames[i] = theDatabases.get(i).getName();
        if(theSelectedTable != null && theNames[i].equals(theSelectedTable)) {
          theSelectedIndexes[0] = i;
        }
      }

      EnterSelectionDialog theDialog = new EnterSelectionDialog(theSpoon.getShell(), theNames,
          BaseMessages.getString(Spoon.class ,"Spoon.ExploreDB.SelectDB.Title"), //$NON-NLS-1$
          BaseMessages.getString(Spoon.class, "Spoon.ExploreDB.SelectDB.Message"), theDatabasesInterface); //$NON-NLS-1$
      theDialog.setSelectedNrs(theSelectedIndexes);
      String theDBName = theDialog.open();

      if (theDBName != null) {
        SpoonDBDelegate theDelegate = new SpoonDBDelegate(theSpoon);
        DatabaseMeta theDBMeta = DatabaseMeta.findDatabase(theDatabasesInterface.getDatabases(), theDBName);
        String theTable = theDelegate.exploreDB(theDBMeta, false);

        if (StringUtils.isEmpty(theTable)) {
          MessageBox theMessageBox = new MessageBox(theSpoon.getShell(), SWT.ICON_ERROR | SWT.OK);
          theMessageBox.setText(BaseMessages.getString(Spoon.class, "Spoon.Message.Warning.Warning")); //$NON-NLS-1$
          theMessageBox.setMessage(BaseMessages.getString(ModelerController.class, "Spoon.Message.Model.EmptyTable")); //$NON-NLS-1$
          theMessageBox.open();
          return;
        }

        boolean refresh = this.workspace.getAvailableFields().isEmpty();
        if(!StringUtils.isEmpty(theTable) && !this.workspace.getAvailableFields().isEmpty()) {

          MessageBox theMessageBox = new MessageBox(theSpoon.getShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
          theMessageBox.setText(BaseMessages.getString(Spoon.class, "Spoon.Message.Warning.Warning")); //$NON-NLS-1$
          theMessageBox.setMessage(BaseMessages.getString(ModelerController.class, "Spoon.Message.Model.Warning")); //$NON-NLS-1$

          int theVal = theMessageBox.open();
          if(theVal == SWT.OK) {
            refresh = true;
          } else {
            refresh = false;
          }
        }
        if(refresh) {
          TableModelerSource theSource = new TableModelerSource(theDBMeta, theTable, null);
          ModelerWorkspaceUtil.populateModelFromSource(this.workspace, theSource);
          XulLabel sourceLabel = (XulLabel) document.getElementById(this.SOURCE_NAME_LABEL_ID);
          sourceLabel.setValue(theTable);
          fireBindings();
        }
      }
    } catch (Exception e) {
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e); //$NON-NLS-1$
    }
  }


  public void visualize() throws ModelerException{
    try{
      openVisualizer();
    } catch(Exception e){
      logger.info(e);
      throw new ModelerException(e);
    }
  }


  public void publish() throws ModelerException{
    String publishingFile = workspace.getFileName();
    int treeDepth = 0;
    DatabaseMeta databaseMeta = ((ISpoonModelerSource) workspace.getModelSource()).getDatabaseMeta();
    boolean checkDatasources = true;
    boolean showServerSelection = true;
    boolean showFolders = true;
    boolean showCurrentFolder = false;
    String serverPathTemplate = "{path}" + ISolutionRepository.SEPARATOR + //$NON-NLS-1$
      "resources" + ISolutionRepository.SEPARATOR + "metadata"; //$NON-NLS-1$ //$NON-NLS-2$
    String databaseName = workspace.getDatabaseName();
    String extension = ".xmi"; //$NON-NLS-1$
    String filename = workspace.getModelName();
    ModelerWorkspaceUtil.populateDomain(workspace);
    String fileName = PublisherHelper.publish(workspace, publishingFile, treeDepth, databaseMeta, filename, checkDatasources,
        false, showFolders, showCurrentFolder, serverPathTemplate, extension, databaseName);
    workspace.getModel().setName(fileName);
    workspace.setDirty(true);
  }

  public void openVisualizer() {

    workspace.getModel().validateTree();
    if (workspace.isValid() == false) {
      showValidationMessages();
      return;
    }

    try {
      VisualizationManager theManager = VisualizationManager.getInstance();
      IVisualization theVisualization = theManager.getVisualization(visualizationList.getSelectedItem());
      if (workspace.getFileName() == null) { //temp model
        theVisualization.createVisualizationFromModel(workspace, true);
      } else {
        theVisualization.createVisualizationFromModel(workspace, false);
      }
      Spoon.getInstance().enableMenus();
      if (theVisualization != null) {
        // TODO: Find a better name for the cube, maybe just workspace name?
      }
    } catch (Exception e) {
      logger.error(e);
    }
  }



  public List<String> getVisualizationNames() {
  	if(this.visualizationNames == null) {
  		VisualizationManager theManager = VisualizationManager.getInstance();
  		this.visualizationNames = theManager.getVisualizationNames();
  	}
  	return this.visualizationNames;
  }

}
