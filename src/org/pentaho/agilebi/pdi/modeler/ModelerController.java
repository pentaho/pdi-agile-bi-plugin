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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metadata.model.IPhysicalModel;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulEditpanel;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.dnd.DropEvent;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.AbstractModelNode;
import org.pentaho.ui.xul.util.XulDialogCallback;

/**
 * XUL Event Handler for the modeling interface. This class interacts with a ModelerModel to store state.
 * 
 * @author nbaker
 *
 */
public class ModelerController extends AbstractXulEventHandler{

  private static final String NEW_DIMENSION_NAME = "newDimensionName"; //$NON-NLS-1$

  private static final String NEW_DIMESION_DIALOG = "newDimesionDialog"; //$NON-NLS-1$


  private static final String MY_TAB_LIST_ID = "myTabList"; //$NON-NLS-1$

  private static final String FIELD_LIST_ID = "fieldList"; //$NON-NLS-1$

  private static final String IN_PLAY_TABLE_ID = "fieldTable"; //$NON-NLS-1$

  private static final String MODEL_NAME_FIELD_ID = "modelname"; //$NON-NLS-1$
  
  private static final String SOURCE_NAME_LABEL_ID = "source_name"; //$NON-NLS-1$

  private static final String MODEL_NAME_PROPERTY = "modelName"; //$NON-NLS-1$

  private static final String VALUE_PROPERTY = "value"; //$NON-NLS-1$

  private static final String ELEMENTS_PROPERTY = "elements"; //$NON-NLS-1$

  private static final String FIELD_NAMES_PROPERTY = "fieldNames"; //$NON-NLS-1$

  private static Log logger = LogFactory.getLog(ModelerController.class);
  
  private ModelerWorkspace workspace;
  
  private String fieldTypesDesc[];
  private Integer fieldTypesCode[];
  
  private XulDialog newDimensionDialog;
  private XulTextbox newDimensionName;
  private XulTree dimensionTree;
  private XulMenuList visualizationList;
  private XulDeck propDeck;
  private Object[] selectedFields;
  
  private BindingFactory bf = new DefaultBindingFactory();
 
  private List<String> visualizationNames;
  private Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> propertiesForms = new HashMap<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm>();
  
  private ColResolverController colController;
  
  private XulEditpanel propPanel;
  
  public ModelerController(){
    workspace = new ModelerWorkspace();
  }
  
  public ModelerController(ModelerWorkspace workspace){
    this.workspace = workspace;
  }
  
  public String getName(){
    return "modeler";
  }
  
  public void onFieldListDrag(DropEvent event) {
    // nothing to do here
  }

  public void onDimensionTreeDrag(DropEvent event) {
    // todo, disable dragging of Root elements once we've updated the tree UI
  }
  
  public void onDimensionTreeDrop(DropEvent event) {
    List<Object> data = event.getDataTransfer().getData();
    List<Object> newdata = new ArrayList<Object>();
    for (Object obj : data) {
      if (obj instanceof AvailableField) {
        AvailableField availableField = (AvailableField) obj;
        // depending on the parent
        if (event.getDropParent() == null) {
          // null - cannot add fields at this level
        } else if (event.getDropParent() instanceof MeasuresCollection) {
          // measure collection - add as a measure
          newdata.add(workspace.createMeasureForNode(availableField));
        } else if (event.getDropParent() instanceof DimensionMetaDataCollection) {
          // dimension collection - add as a dimension
          newdata.add(workspace.createDimensionFromNode(availableField));
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          // dimension - add as a hierarchy
          newdata.add(workspace.createHierarchyForParentWithNode((DimensionMetaData)event.getDropParent(), availableField));
        } else if (event.getDropParent() instanceof HierarchyMetaData) {
          // hierarchy - add as a level
          newdata.add(workspace.createLevelForParentWithNode((HierarchyMetaData)event.getDropParent(), availableField));
        } else if (event.getDropParent() instanceof LevelMetaData) {
          // level - cannot drop into a level
          event.setAccepted(false);
          return;
        }
      } else if (obj instanceof LevelMetaData) {
        LevelMetaData level = (LevelMetaData)obj;
        if (event.getDropParent() instanceof HierarchyMetaData) {
          // rebind to workspace, including logical column and actual parent
          level.setParent((HierarchyMetaData)event.getDropParent());
          newdata.add(level);
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          // add as a new hierarchy
          HierarchyMetaData hier = workspace.createHierarchyForParentWithNode((DimensionMetaData)event.getDropParent(), level);
          hier.setName(level.getName());
          hier.get(0).setName(level.getName());
          newdata.add(hier);
        } else if (event.getDropParent() == null) {
          DimensionMetaData dim = workspace.createDimensionWithName(level.getColumnName());
          dim.setName(level.getName());
          dim.get(0).setName(level.getName());
          dim.get(0).get(0).setName(level.getName());
          newdata.add(dim);
        }
      } else if (obj instanceof HierarchyMetaData) {
        HierarchyMetaData hierarchy = (HierarchyMetaData)obj;
        if (event.getDropParent() == null) {
          DimensionMetaData dim = new DimensionMetaData(hierarchy.getName());
          dim.add(hierarchy);
          hierarchy.setParent(dim);
          // TODO: this will also need to resolve the level LogicalColumns
          newdata.add(dim);
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          DimensionMetaData dim = (DimensionMetaData)event.getDropParent();
          hierarchy.setParent(dim);
          // TODO: this will also need to resolve the level LogicalColumns
          newdata.add(hierarchy);
        }
      } else if (obj instanceof DimensionMetaData) {
        if (event.getDropParent() == null) {
          newdata.add((DimensionMetaData)obj);
          // TODO: this will also need to resolve level LogicalColumns
        }
      } else if (obj instanceof MeasureMetaData) {
        if (event.getDropParent() instanceof MeasuresCollection) {
          MeasureMetaData measure = (MeasureMetaData)obj;
          LogicalColumn col = workspace.findLogicalColumn(obj.toString());
          measure.setLogicalColumn(col);
          newdata.add(measure);
        }
      }
      
    }
    if (newdata.size() == 0) {
      event.setAccepted(false);
    } else {
      event.getDataTransfer().setData(newdata);
    }
  }

  public void addField() {
  	AbstractMetaDataModelNode theNode = null;
    Object[] selectedItems = getSelectedFields();
    for (Object obj : selectedItems) {
      if (obj instanceof AvailableField) {
        AvailableField availableField = (AvailableField) obj;
        // depending on the parent
        if (selectedTreeItem == null) {
          // null - cannot add fields at this level
        } else if (selectedTreeItem instanceof MeasuresCollection) {
          // measure collection - add as a measure
        	MeasuresCollection theMesaures = (MeasuresCollection) selectedTreeItem;
        	theNode = workspace.createMeasureForNode(availableField);
        	theMesaures.add((MeasureMetaData) theNode);
        } else if (selectedTreeItem instanceof DimensionMetaDataCollection) {
          // dimension collection - add as a dimension
        	theNode = workspace.createDimensionFromNode(availableField);
        	DimensionMetaDataCollection theDimensions = (DimensionMetaDataCollection) selectedTreeItem;
        	theDimensions.add((DimensionMetaData) theNode);
        } else if (selectedTreeItem instanceof DimensionMetaData) {
          // dimension - add as a hierarchy
        	theNode = workspace.createHierarchyForParentWithNode((DimensionMetaData)selectedTreeItem, availableField);
        	DimensionMetaData theDimension = (DimensionMetaData) selectedTreeItem;
        	theDimension.add((HierarchyMetaData) theNode);
        } else if (selectedTreeItem instanceof HierarchyMetaData) {
          // hierarchy - add as a level
        	theNode = workspace.createLevelForParentWithNode((HierarchyMetaData)selectedTreeItem, availableField);
        	HierarchyMetaData theHierarchy = (HierarchyMetaData) selectedTreeItem;
        	theHierarchy.add((LevelMetaData) theNode);
        } 
        if(theNode != null) {
        	theNode.setParent((AbstractMetaDataModelNode) selectedTreeItem);
        } 
      } 
    }
  }
  
  public void init() throws ModelerException{

    bf.setDocument(document);
    
    newDimensionDialog = (XulDialog) document.getElementById(NEW_DIMESION_DIALOG);
    newDimensionName = (XulTextbox) document.getElementById(NEW_DIMENSION_NAME);
    dimensionTree = (XulTree) document.getElementById("dimensionTree");
    visualizationList = (XulMenuList)document.getElementById("visualizationlist");
    propDeck = (XulDeck) document.getElementById("propertiesdeck");
    propPanel = (XulEditpanel) document.getElementById("propertiesPanel");
    
    XulLabel sourceLabel = (XulLabel) document.getElementById(SOURCE_NAME_LABEL_ID);
    String connectionName = "";
    String tableName = "";
    
    //TODO: migrate this code elsewhere or remove it entirely
    if( workspace.getModelSource() != null && workspace.getModelSource() instanceof OutputStepModelerSource) {
      // for now just list the first table in the first physical workspace
      DatabaseMeta databaseMeta = workspace.getModelSource().getDatabaseMeta();
      if( databaseMeta != null ) {
        connectionName = databaseMeta.getName();
      }
      List<IPhysicalModel> physicalModels = workspace.getDomain().getPhysicalModels();
      if( physicalModels != null && physicalModels.size() > 0 ) {
        List<? extends IPhysicalTable> tables = physicalModels.get(0).getPhysicalTables();
        if( tables != null && tables.size() > 0 ) {
          // TODO where is the locale coming from? And why do we need one here?
          tableName = tables.get(0).getName("en_US");
        }
      }
    }
    sourceLabel.setValue( "Table : " + tableName );
    bf.createBinding(workspace, "sourceName", sourceLabel, "value");

    bf.setBindingType(Type.ONE_WAY);
    fieldListBinding = bf.createBinding(workspace, "availableFields", FIELD_LIST_ID, ELEMENTS_PROPERTY);
    selectedFieldsBinding = bf.createBinding(FIELD_LIST_ID, "selectedItems", this, "selectedFields");
    
    bf.createBinding(workspace, "selectedVisualization", visualizationList, "selectedItem");    
    visualizationsBinding = bf.createBinding(this, "visualizationNames", visualizationList, "elements");
    
    modelTreeBinding = bf.createBinding(workspace, "model", dimensionTree, "elements");
    bf.createBinding(dimensionTree, "selectedItem", this, "dimTreeSelectionChanged");    
    
    bf.createBinding("fieldList", "selectedItem", "addField", "disabled", new BindingConvertor<Object, Boolean>() {
    	public Boolean sourceToTarget(Object value) {
    		return getSelectedFields().length == 0 || selectedTreeItem == null || selectedTreeItem instanceof LevelMetaData || selectedTreeItem instanceof MainModelNode;
    	}

      public Object targetToSource(Boolean value) {
      	return null;
      }
    });
    
    bf.createBinding(dimensionTree, "selectedItem", "addField", "disabled", new BindingConvertor<Object, Boolean>() {
    	public Boolean sourceToTarget(Object value) {
    		return getSelectedFields().length == 0 || value == null || value instanceof LevelMetaData  || selectedTreeItem instanceof MainModelNode;
    	}

      public Object targetToSource(Boolean value) {
      	return null;
      }
    });      
    
    bf.createBinding(dimensionTree, "selectedItem", "measureBtn", "disabled", new ButtonConvertor(MeasuresCollection.class));    
    bf.createBinding(dimensionTree, "selectedItem", "dimensionBtn", "disabled", new ButtonConvertor(DimensionMetaDataCollection.class));
    bf.createBinding(dimensionTree, "selectedItem", "hierarchyBtn", "disabled", new ButtonConvertor(DimensionMetaData.class));
    bf.createBinding(dimensionTree, "selectedItem", "levelBtn", "disabled", new ButtonConvertor(HierarchyMetaData.class));
    
    bf.setBindingType(Type.BI_DIRECTIONAL);
    modelNameBinding = bf.createBinding(workspace, MODEL_NAME_PROPERTY, MODEL_NAME_FIELD_ID, VALUE_PROPERTY);
    
    bf.createBinding(this.propPanel, "visible", this, "propVisible");
    
    fireBindings();
    
    dimensionTree.expandAll();
    
    if(workspace.isShowAutoPopulatePrompt()){
      try{
        XulConfirmBox confirm = (XulConfirmBox) document.createElement("confirmbox");
        confirm.setTitle(Messages.getString("auto_populate_title"));
        confirm.setMessage(Messages.getString("auto_populate_msg"));
        confirm.setAcceptLabel(Messages.getString("yes"));
        confirm.setCancelLabel(Messages.getString("no"));
        confirm.addDialogCallback(new XulDialogCallback(){

          public void onClose(XulComponent sender, Status returnCode, Object retVal) {
            if(returnCode == Status.ACCEPT){
              autoPopulate();
            } 
          }

          public void onError(XulComponent sender, Throwable t) {}
          
        });
        confirm.open();
        
      } catch(XulException e){
        logger.error(e);
      }
    }
    
  }
  
  private void fireBindings() throws ModelerException{
    try {
      fieldListBinding.fireSourceChanged();
      selectedFieldsBinding.fireSourceChanged();
      modelTreeBinding.fireSourceChanged();
      modelNameBinding.fireSourceChanged();
      visualizationsBinding.fireSourceChanged();
    } catch (Exception e) {
      logger.info("Error firing off initial bindings", e);
      throw new ModelerException(e);
    }
  }
  
  public void setSelectedDims(List<Object> selectedDims) {
    List<Object> prevSelected = null; // this.selectedColumns;
    if (selectedDims != null) {
      System.out.println(selectedDims.get(0));
    }
    // this.selectedColumns = selectedColumns;
    // this.firePropertyChange("selectedColumns", prevSelected , selectedColumns);
  }
  
//  public void moveFieldIntoPlay() {
//    XulListbox fieldsList = (XulListbox) document.getElementById(FIELD_LIST_ID);
//    Object[] selectedItems = fieldsList.getSelectedItems();
//    int tabIndex = ((XulTabbox) document.getElementById(MY_TAB_LIST_ID)).getSelectedIndex();
//    if (tabIndex == 0) {
//      for (Object obj : selectedItems) {
//        workspace.addFieldIntoPlay(obj);
//      }
//    } else if (tabIndex == 1){
//      // if a dimension or hierarchy is selected, add the field as a level
//      // otherwise add a new dimension
//      for (Object obj : selectedItems) {
//        if (selectedTreeItem == null) {
//          workspace.addDimension(obj);
//        } else {
//          workspace.addToHeirarchy(selectedTreeItem, obj);
//        }
//      }
//    }
//  }

  
  public void visualize() throws ModelerException{
    try{
      openVisualizer();
    } catch(Exception e){
      logger.info(e);
      throw new ModelerException(e);
    }
  }
  
  public void publish() throws ModelerException{
    try{
      ModelerWorkspaceUtil.populateDomain(workspace);
      
      ModelServerPublish publisher = new ModelServerPublish();
      publisher.setModel( workspace );

      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      try {
      XulDialogPublish publishDialog = new XulDialogPublish( spoon.getShell() );
      publishDialog.setFolderTreeDepth(1);
      publishDialog.setComment( Messages.getString("ModelServerPublish.Publish.ModelPublishComment") ); //$NON-NLS-1$
      DatabaseMeta databaseMeta = workspace.getModelSource().getDatabaseMeta();
      publishDialog.setDatabaseMeta(databaseMeta);
      publishDialog.setFilename( workspace.getModelName() );
      publishDialog.setCheckDatasources( true );
      publishDialog.setShowLocation( true, true, false );
      String template = "{path}"+ //$NON-NLS-1$
      "resources"+ISolutionRepository.SEPARATOR+ //$NON-NLS-1$
      "metadata"+ISolutionRepository.SEPARATOR+ //$NON-NLS-1$
        "{file}.xmi"; //$NON-NLS-1$ 
      publishDialog.setPathTemplate( template );
      publishDialog.showDialog();
      if( publishDialog.isAccepted() ) {
        // now try to publish
        String path = publishDialog.getPath();
        // we always publish to {solution}/resources/metadata
        StringBuilder sb = new StringBuilder();
        BiServerConnection biServerConnection = publishDialog.getBiServerConnection();
        publisher.setBiServerConnection(biServerConnection);
        boolean publishDatasource = publishDialog.isPublishDataSource();
        sb.append( path )
        .append(ISolutionRepository.SEPARATOR)
        .append( "resources" ) //$NON-NLS-1$
        .append(ISolutionRepository.SEPARATOR)
        .append( "metadata" ); //$NON-NLS-1$
        String repositoryPath = sb.toString();
        String filename = publishDialog.getFilename();
        publisher.publishToServer( filename + ".mondrian.xml", workspace.getDatabaseName(), filename, repositoryPath, publishDatasource, true ); //$NON-NLS-1$
      }
      } catch (XulException e) {
        e.printStackTrace();
        SpoonFactory.getInstance().messageBox( "Could not create dialog: "+e.getLocalizedMessage(), "Dialog Error", false, Const.ERROR);
      }

//      BiServerConnection biServerConnection = BiServerConfig.getInstance().getServerByName( workspace.getSelectedServer() );
//      publisher.setBiServerConnection(biServerConnection);
      
      // create the publish dialog
      
//      publisher.publishToServer( workspace.getModelName() + ".mondrian.xml", workspace.getDatabaseName(), workspace.getModelName(), true );
    } catch(Exception e){
      throw new ModelerException(e);
    }

  }
  
  /**
   * Goes back to the source of the metadata and see if anything has changed.
   * Updates the UI accordingly
   */
  public void refreshFields() throws ModelerException {
    workspace.refresh();
  }
  
  public void setFileName(String fileName){
    workspace.setFileName(fileName);
  }
  

  
  public void showNewMeasureDialog() {
    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement("promptbox");
      prompt.setTitle("New Measure");
      prompt.setMessage("Enter new Measure name");
      prompt.addDialogCallback(new XulDialogCallback(){
  
        public void onClose(XulComponent sender, Status returnCode, Object retVal) {
          if(returnCode == Status.ACCEPT){
          	MeasuresCollection theMesaures = (MeasuresCollection) selectedTreeItem;
          	MeasureMetaData theMeasure = new MeasureMetaData("" + retVal, "", "" + retVal);
          	theMeasure.setParent(theMesaures);
          	theMesaures.add(theMeasure);
          }
        }
  
        public void onError(XulComponent sender, Throwable t) {
          logger.error(t);
        }
        
      });
      prompt.open();
    } catch (XulException e) {
      logger.error(e);
    }
  }
  
  public void showNewHierarchyDialog() {
  	 try {
       XulPromptBox prompt = (XulPromptBox) document.createElement("promptbox");
       prompt.setTitle("New Hierarchy");
       prompt.setMessage("Enter new Hierarcy name");
       prompt.addDialogCallback(new XulDialogCallback(){

      	 public void onClose(XulComponent sender, Status returnCode, Object retVal) {
           if(returnCode == Status.ACCEPT){
          	DimensionMetaData theDimension = (DimensionMetaData) selectedTreeItem;
          	HierarchyMetaData theHieararchy = new HierarchyMetaData("" + retVal);
          	theHieararchy.setParent(theDimension);
          	theDimension.add(theHieararchy);
           }
         }
   
         public void onError(XulComponent sender, Throwable t) {
           logger.error(t);
         }
       });
       prompt.open();
     } catch (XulException e) {
       logger.error(e);
     }
  }
  
  public void showNewLevelDialog() {
  	
  	try {
      XulPromptBox prompt = (XulPromptBox) document.createElement("promptbox");
      prompt.setTitle("New Level");
      prompt.setMessage("Enter new Level name");
      prompt.addDialogCallback(new XulDialogCallback(){

     	 public void onClose(XulComponent sender, Status returnCode, Object retVal) {
          if(returnCode == Status.ACCEPT){
          	HierarchyMetaData theHierarchy = (HierarchyMetaData) selectedTreeItem;
          	LevelMetaData theLevel = new LevelMetaData(theHierarchy, "" + retVal);
          	theHierarchy.add(theLevel);
          }
        }
  
        public void onError(XulComponent sender, Throwable t) {
          logger.error(t);
        }
      });
      prompt.open();
    } catch (XulException e) {
      logger.error(e);
    }
  	
  }
  
  public void showNewDimensionDialog(){
    
    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement("promptbox");
      //prompt.setModalParent(((Spoon) SpoonFactory.getInstance()).getShell());
      prompt.setTitle("New Dimension");
      prompt.setMessage("Enter new Dimension name");
      prompt.addDialogCallback(new XulDialogCallback(){
  
        public void onClose(XulComponent sender, Status returnCode, Object retVal) {
          if(returnCode == Status.ACCEPT){
  
            DimensionMetaData dimension = new DimensionMetaData(""+retVal);
            HierarchyMetaData hierarchy = new HierarchyMetaData(""+retVal);
            hierarchy.setParent(dimension);
            dimension.add(hierarchy);
            workspace.addDimension(dimension);
          }
        }
  
        public void onError(XulComponent sender, Throwable t) {
          logger.error(t);
        }
        
      });
      prompt.open();
    } catch (XulException e) {
      logger.error(e);
    }
  }
  
  public void moveFieldUp() {
    if(selectedTreeItem == null){
      return;
    }
    ((AbstractModelNode) selectedTreeItem).getParent().moveChildUp(selectedTreeItem);
    
  }
  
  public void moveFieldDown() {
    if(selectedTreeItem == null){
      return;
    }
    ((AbstractModelNode) selectedTreeItem).getParent().moveChildDown(selectedTreeItem);
    
  }

  Object selectedTreeItem;

  private Binding fieldListBinding;
  private Binding selectedFieldsBinding;

  private Binding visualizationsBinding;

  private Binding modelTreeBinding;

  private Binding modelNameBinding;
  
  
  public void setDimTreeSelectionChanged(Object selection){
    selectedTreeItem = selection;
    if(selection != null && selection instanceof AbstractMetaDataModelNode){
      AbstractMetaDataModelNode node = (AbstractMetaDataModelNode) selection;
      ModelerNodePropertiesForm form = propertiesForms.get(node.getPropertiesForm());
      if(form != null){
        form.activate((AbstractMetaDataModelNode) selection);
        return;
      }
    }
    if(this.propDeck != null) {
    this.propDeck.setSelectedIndex(0);
    }
  }
    
  
  public void removeField() {
    if(selectedTreeItem instanceof DimensionMetaDataCollection 
        || selectedTreeItem instanceof MeasuresCollection 
        || selectedTreeItem instanceof MainModelNode
        || selectedTreeItem == null){
      return;
    }
    ((AbstractModelNode) selectedTreeItem).getParent().remove(selectedTreeItem);
    setDimTreeSelectionChanged(null);
  }

  public List<String> getVisualizationNames() {
  	if(this.visualizationNames == null) {
  		VisualizationManager theManager = VisualizationManager.getInstance();
  		this.visualizationNames = theManager.getVisualizationNames();
  	}
  	return this.visualizationNames;
  }
  
  public ModelerWorkspace getModel() {
    return workspace;
  }

  public void setModel(ModelerWorkspace model) throws ModelerException{
    this.workspace = model;
    fireBindings();
  }
  

  public void openVisualizer() {
    if(workspace.isDirty()){
      try{
        XulMessageBox box = (XulMessageBox) document.createElement("messagebox");
        box.setTitle("Warning");
        box.setMessage("You must save your workspace before visualizing.");
        box.open();
      } catch(XulException e){
        e.printStackTrace();
        logger.error(e);
      }
      return;
    }
    workspace.getModel().validateTree();
    if(workspace.isValid() == false){
      showValidationMessages();
      return;
    }
    
  	VisualizationManager theManager = VisualizationManager.getInstance();
  	IVisualization theVisualization = theManager.getVisualization(visualizationList.getSelectedItem());
  	if(theVisualization != null) {
  	  if (workspace.getFileName() != null) {
  	    // TODO: Find a better name for the cube, maybe just workspace name?
  	    theVisualization.createVisualizationFromModel(workspace);
  	  } else {
  	    throw new UnsupportedOperationException("TODO: prompt to save workspace before visualization");
  	  }
  	}
  }
  
  private void showValidationMessages(){

    StringBuffer validationErrors = new StringBuffer(Messages.getString("model_contains_errors"));
    for (String msg : workspace.getValidationMessages()) {
      validationErrors.append(msg);
      validationErrors.append("\n");
      logger.info(msg);
    }
    try{
      XulMessageBox msg = (XulMessageBox) document.createElement("messagebox");
      msg.setTitle(Messages.getString("model_not_valid"));
      msg.setMessage(validationErrors.toString());
      msg.open();
    } catch(XulException e){
      logger.error(e);
    }
  }
  
  public boolean saveWorkspace(String fileName) throws ModelerException {
    workspace.getModel().validateTree();
    if(workspace.isValid() == false){
      showValidationMessages();
      return false;
    }
  	ModelerWorkspaceUtil.saveWorkspace(workspace, fileName);
    workspace.setFileName(fileName);
    workspace.setDirty(false);
    return true;
  }
  
  public void resolveMissingColumn() {
    if(selectedTreeItem instanceof ColumnBackedNode 
        && ((AbstractMetaDataModelNode) selectedTreeItem).isValid() == false){
      colController.show(this.workspace, (ColumnBackedNode) selectedTreeItem); 
    }
  }
  
  public void loadWorkspace() throws ModelerException {
  	
  	try {
	  	StringBuffer theStringBuffer = new StringBuffer();
	  	FileReader theReader = new FileReader(new File("my_metadata.xml"));
	  	BufferedReader theBuffer = new BufferedReader(theReader);
	  	String theLine = null;
	  	while((theLine = theBuffer.readLine()) != null) {
	  		theStringBuffer.append(theLine);
	  	}
	  	ModelerWorkspaceUtil.loadWorkspace("my_metadata.xml", theStringBuffer.toString(), getModel());
	  	
  	} catch(Exception e) {
  		logger.info(e.getLocalizedMessage());
  		throw new ModelerException(e);
  	}
  }
  
  public void loadPerspective(String id){
   // document.loadPerspective(id);
  }
  
  public void addPropertyForm(AbstractModelerNodeForm form){
    propertiesForms.put(form.getClass(), form);
  }

  public void setColResolver(ColResolverController controller){
    this.colController = controller;
  }
  
  public void autoPopulate(){

    try {
      ModelerWorkspaceUtil.autoModelFlat(this.workspace);
      this.dimensionTree.expandAll();
    } catch (ModelerException e) {
      logger.error(e.getLocalizedMessage());
    }
  }
  
  public void togglePropertiesPanel(){
    setPropVisible(! isPropVisible());
  }
  
  
  private boolean propVisible = true;
  public boolean isPropVisible(){
    return propVisible;
  }
  
  public void setPropVisible(boolean vis){
    boolean prevVal = propVisible;
    this.propVisible = vis;
    this.firePropertyChange("propVisible", prevVal, vis);
  }
  
  private static class ButtonConvertor extends BindingConvertor<Object, Boolean> {
  	
  	private Class type;
  	
  	public ButtonConvertor(Class aClass) {
  		type = aClass;
  	}
  	
  	public Boolean sourceToTarget(Object value) {
  		return value == null || !(value.getClass() == type);
  	}

    public Object targetToSource(Boolean value) {
    	return null;
    }
  }
  
  public void setSelectedFields(Object[] aFields) {
    selectedFields = aFields;
  }
  
  public Object[] getSelectedFields() {
    if(selectedFields == null) {
      selectedFields = new Object[]{};
    }
    return selectedFields;
  } 
}
