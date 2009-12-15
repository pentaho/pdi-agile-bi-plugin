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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.metadata.model.IPhysicalModel;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulTabbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.dnd.DropEvent;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

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
  
  private ModelerWorkspace model;
  
  private String fieldTypesDesc[];
  private Integer fieldTypesCode[];
  
  private XulDialog newDimensionDialog;
  private XulTextbox newDimensionName;
  private XulTree dimensionTree;
  private XulLabel detailsLabel;
  private XulLabel detailsLabel2;
  private XulLabel detailsLabel3;
  private XulMenuList serverList;
  private XulMenuList visualizationList;
  private XulCheckbox uniqueMemberCB;
  
  private BindingFactory bf = new DefaultBindingFactory();
 
  private List<String> serverNames;
  private List<String> visualizationNames;
  
  public ModelerController(){
    model = new ModelerWorkspace();
  }
  
  public ModelerController(ModelerWorkspace model){
    this.model = model;
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
      if (obj instanceof FieldMetaData) {
        // depending on the parent
        if (event.getDropParent() == null) {
          // null - add as a dimension
          newdata.add(model.createDimension(obj));
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          // dimension - add as a hierarchy
          newdata.add(model.createHierarchy(model.findDimension((DimensionMetaData)event.getDropParent()), obj));
        } else if (event.getDropParent() instanceof HierarchyMetaData) {
          // hierarchy - add as a level
          newdata.add(model.createLevel(model.findHierarchy((HierarchyMetaData)event.getDropParent()), obj));
        } else if (event.getDropParent() instanceof LevelMetaData) {
          // level - cannot drop into a level
          event.setAccepted(false);
          return;
        }
      } else if (obj instanceof LevelMetaData) {
        LevelMetaData level = (LevelMetaData)obj;
        if (event.getDropParent() instanceof HierarchyMetaData) {
          // rebind to model, including logical column and actual parent
          LogicalColumn col = model.findLogicalColumn(obj.toString());
          level.setLogicalColumn(col);
          level.setParent(model.findHierarchy((HierarchyMetaData)event.getDropParent()));
          newdata.add(level);
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          // add as a new hierarchy
          HierarchyMetaData hier = model.createHierarchy(model.findDimension((DimensionMetaData)event.getDropParent()), level.getColumnName());
          hier.setName(level.getName());
          hier.getChildren().get(0).setName(level.getName());
          newdata.add(hier);
        } else if (event.getDropParent() == null) {
          DimensionMetaData dim = model.createDimension(level.getColumnName());
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
          hierarchy.setParent(model.findDimension(dim));
          // TODO: this will also need to resolve the level LogicalColumns
          newdata.add(hierarchy);
        }
      } else if (obj instanceof DimensionMetaData) {
        if (event.getDropParent() == null) {
          newdata.add((DimensionMetaData)obj);
          // TODO: this will also need to resolve level LogicalColumns
        }
      }
      
    }
    if (newdata.size() == 0) {
      event.setAccepted(false);
    } else {
      event.getDataTransfer().setData(newdata);
    }
  }

  public void init() throws ModelerException{

    createServerList();
    
    bf.setDocument(document);
    
    newDimensionDialog = (XulDialog) document.getElementById(NEW_DIMESION_DIALOG);
    newDimensionName = (XulTextbox) document.getElementById(NEW_DIMENSION_NAME);
    dimensionTree = (XulTree) document.getElementById("dimensionTree");
    detailsLabel = (XulLabel)document.getElementById("details");
    detailsLabel2 = (XulLabel)document.getElementById("details2");
    detailsLabel3 = (XulLabel)document.getElementById("details3");
//    uniqueMemberCB = (XulCheckbox)document.getElementById("uniquecb");
    serverList = (XulMenuList)document.getElementById("serverlist");
    visualizationList = (XulMenuList)document.getElementById("visualizationlist");

    XulLabel sourceLabel = (XulLabel) document.getElementById(SOURCE_NAME_LABEL_ID);
    String connectionName = "";
    String tableName = "";
    if( model.getModelSource() != null ) {
      // for now just list the first table in the first physical model
      DatabaseMeta databaseMeta = model.getModelSource().getDatabaseMeta();
      if( databaseMeta != null ) {
        connectionName = databaseMeta.getName();
      }
      List<IPhysicalModel> physicalModels = model.getDomain().getPhysicalModels();
      if( physicalModels != null && physicalModels.size() > 0 ) {
        List<? extends IPhysicalTable> tables = physicalModels.get(0).getPhysicalTables();
        if( tables != null && tables.size() > 0 ) {
          // TODO where is the locale coming from? And why do we need one here?
          tableName = tables.get(0).getName("en_US");
        }
      }
    }
    sourceLabel.setValue( "Connection : "+connectionName + ", Table : " + tableName );
    bf.createBinding(model, "sourceName", sourceLabel, "value");

    bf.setBindingType(Type.ONE_WAY);
    fieldListBinding = bf.createBinding(model, "availableFields", FIELD_LIST_ID, ELEMENTS_PROPERTY);
    
    // dimensionTable

    bf.createBinding(model, "selectedServer", serverList, "selectedItem");    
    serversBinding = bf.createBinding(this, "serverNames", serverList, "elements");
    
    bf.createBinding(model, "selectedVisualization", visualizationList, "selectedItem");    
    visualizationsBinding = bf.createBinding(this, "visualizationNames", visualizationList, "elements");
    
    dimensionTreeBinding = bf.createBinding(model, "dimensions", dimensionTree, "elements");
    bf.createBinding(dimensionTree, "selectedItem", this, "dimTreeSelectionChanged");
    
    bf.setBindingType(Type.BI_DIRECTIONAL);
    inPlayTableBinding = bf.createBinding(model, "fields" , "fieldTable", "elements");
    modelNameBinding = bf.createBinding(model, MODEL_NAME_PROPERTY, MODEL_NAME_FIELD_ID, VALUE_PROPERTY);
    
    fireBindings();
  }
  
  private void fireBindings() throws ModelerException{
    try {
      fieldListBinding.fireSourceChanged();
      dimensionTreeBinding.fireSourceChanged();
      inPlayTableBinding.fireSourceChanged();
      modelNameBinding.fireSourceChanged();
      serversBinding.fireSourceChanged();
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
  
  
  public void moveFieldIntoPlay() {
    XulListbox fieldsList = (XulListbox) document.getElementById(FIELD_LIST_ID);
    Object[] selectedItems = fieldsList.getSelectedItems();
    int tabIndex = ((XulTabbox) document.getElementById(MY_TAB_LIST_ID)).getSelectedIndex();
    if (tabIndex == 0) {
      for (Object obj : selectedItems) {
        model.addFieldIntoPlay(obj);
      }
    } else if (tabIndex == 1){
      // if a dimension or hierarchy is selected, add the field as a level
      // otherwise add a new dimension
      for (Object obj : selectedItems) {
        if (selectedTreeItem == null) {
          model.addDimension(obj);
        } else {
          model.addToHeirarchy(selectedTreeItem, obj);
        }
      }
    }
  }

  public void moveFieldOutOfPlay() {
    XulTree tree = (XulTree) document.getElementById(IN_PLAY_TABLE_ID);
    int[] selectedIndexes = tree.getSelectedRows();
    int tabIndex = ((XulTabbox) document.getElementById(MY_TAB_LIST_ID)).getSelectedIndex();
    if (tabIndex == 0) {
      for (int index : selectedIndexes) {
        FieldMetaData fieldMeta = model.getFields().get(index);
        model.removeFieldFromPlay(fieldMeta);
      }
    } else if (tabIndex == 1){
      if (selectedTreeItem instanceof LevelMetaData) {
        removeDimension();
      }
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
    try{
      ModelerWorkspaceUtil.populateDomain(model);
    
      ModelServerPublish publisher = new ModelServerPublish();
      publisher.setModel( model );

      BiServerConnection biServerConnection = BiServerConfig.getInstance().getServerByName( model.getSelectedServer() );
      publisher.setBiServerConnection(biServerConnection);
      publisher.publishToServer( model.getModelName() + ".mondrian.xml", model.getDatabaseName(), model.getModelName(), true );
    } catch(Exception e){
      logger.info(e);
      SpoonFactory.getInstance().messageBox( "Publish Failed: "+ e.getLocalizedMessage(), "Publish To Server: "+model.getSelectedServer(), false, Const.ERROR);

      throw new ModelerException(e);
    }

  }
  
  /**
   * Goes back to the source of the metadata and see if anything has changed.
   * Updates the UI accordingly
   */
  public void refreshFields() throws ModelerException {

    model.refresh();
  }
  
  public void setFileName(String fileName){
    model.setFileName(fileName);
  }
  

  public void showNewDimensionDialog(){
    this.newDimensionDialog.show();
  }
  
  public void hideNewDimensionDialog(){
    this.newDimensionDialog.hide();
  }
  
  public void addNewDimension(){
    String dimName = this.newDimensionName.getValue();

    DimensionMetaData dimension = new DimensionMetaData(dimName);
    HierarchyMetaData hierarchy = new HierarchyMetaData(dimName);
    hierarchy.setParent(dimension);
    dimension.add(hierarchy);
    model.addDimension(dimension);
    
    hideNewDimensionDialog();
  }
  
  public void moveDimensionUp() {

    if (selectedTreeItem instanceof DimensionMetaData) {
      if(model.getDimensions().indexOf(selectedTreeItem) > 0){
        model.getDimensions().moveChildUp((DimensionMetaData) selectedTreeItem);
        setDimTreeSelectionChanged(null);
      }
    } else if (selectedTreeItem instanceof HierarchyMetaData) {
      DimensionMetaData dim = (DimensionMetaData) ((HierarchyMetaData)selectedTreeItem).getParent();
      if(dim.indexOf(selectedTreeItem) > 0){
        dim.moveChildUp((HierarchyMetaData)selectedTreeItem);
        setDimTreeSelectionChanged(null);
      }
    } else if (selectedTreeItem instanceof LevelMetaData) {
      HierarchyMetaData dim = (HierarchyMetaData) ((LevelMetaData)selectedTreeItem).getParent();
      if(dim.indexOf(selectedTreeItem) > 0){
        dim.moveChildUp((LevelMetaData)selectedTreeItem);
        setDimTreeSelectionChanged(null);
      }
    }
  }
  
  public void moveDimensionDown() {

    if (selectedTreeItem instanceof DimensionMetaData) {
      if(model.getDimensions().size() > model.getDimensions().indexOf(selectedTreeItem) + 1){
        model.getDimensions().moveChildDown((DimensionMetaData) selectedTreeItem);
        setDimTreeSelectionChanged(null);
      }
    } else if (selectedTreeItem instanceof HierarchyMetaData) {
      DimensionMetaData dim = (DimensionMetaData) ((HierarchyMetaData)selectedTreeItem).getParent();
      if(dim.size() > dim.indexOf(selectedTreeItem) +1){
        dim.moveChildDown((HierarchyMetaData)selectedTreeItem);
        setDimTreeSelectionChanged(null);
      }
    } else if (selectedTreeItem instanceof LevelMetaData) {
      HierarchyMetaData dim = (HierarchyMetaData) ((LevelMetaData)selectedTreeItem).getParent();
      if(dim.getChildren().size() > dim.getChildren().indexOf(selectedTreeItem) +1){
        dim.moveChildDown((LevelMetaData)selectedTreeItem);
        setDimTreeSelectionChanged(null);
      }
    }
  }

  Object selectedTreeItem;

  private Binding fieldListBinding;

  private Binding serversBinding;

  private Binding visualizationsBinding;

  private Binding dimensionTreeBinding;

  private Binding modelNameBinding;

  private Binding inPlayTableBinding;
  
  public void setDimTreeSelectionChanged(Object selection){
    selectedTreeItem = selection;
    detailsLabel.setValue("");
    detailsLabel2.setValue("");
    detailsLabel3.setValue("");
    if (selectedTreeItem != null) {
      String data[] = selectedTreeItem.toString().split("\n");
      detailsLabel.setValue(data[0]);
      if (data.length > 1) {
        detailsLabel2.setValue(data[1]);
        if (data.length > 2) {
          detailsLabel3.setValue(data[2]);
        }
      }
    }
//    if (selectedTreeItem instanceof LevelMetaData) {
//      uniqueMemberCB.setChecked(((LevelMetaData)selectedTreeItem).isUniqueMembers());
//      uniqueMemberCB.setVisible(true);
//    } else {
//      uniqueMemberCB.setVisible(false);
//    }
  }
  
  public void updateUniqueMember() {
    LevelMetaData levelMeta = (LevelMetaData)selectedTreeItem;
    levelMeta.setUniqueMembers(uniqueMemberCB.isChecked());
  }
  
  public void removeDimension() {
    if (selectedTreeItem instanceof DimensionMetaData) {
      model.getDimensions().remove(selectedTreeItem);
    } else if (selectedTreeItem instanceof HierarchyMetaData) {
      ((HierarchyMetaData)selectedTreeItem).getParent().remove(selectedTreeItem);
    } else if (selectedTreeItem instanceof LevelMetaData) {
      ((LevelMetaData)selectedTreeItem).getParent().remove(selectedTreeItem);
    }
    setDimTreeSelectionChanged(null);
  }

  public List<String> getServerNames() {
    return serverNames;
  }
  
  public List<String> getVisualizationNames() {
  	if(this.visualizationNames == null) {
  		VisualizationManager theManager = VisualizationManager.getInstance();
  		this.visualizationNames = theManager.getVisualizationNames();
  	}
  	return this.visualizationNames;
  }
  
  private void createServerList() {
    BiServerConfig biServerConfig = BiServerConfig.getInstance();
    serverNames = biServerConfig.getServerNames();
  }

  public ModelerWorkspace getModel() {
    return model;
  }

  public void setModel(ModelerWorkspace model) throws ModelerException{
    this.model = model;
    fireBindings();
  }
  

  public void openVisualizer() {
    if(model.isDirty()){
      try{
        XulMessageBox box = (XulMessageBox) document.createElement("messagebox");
        box.setTitle("Warning");
        box.setMessage("You must save your model before visualizing.");
        box.open();
      } catch(XulException e){
        e.printStackTrace();
        logger.error(e);
      }
      return;
    }
  	VisualizationManager theManager = VisualizationManager.getInstance();
  	IVisualization theVisualization = theManager.getVisualization(visualizationList.getSelectedItem());
  	if(theVisualization != null) {
  	  if (model.getFileName() != null) {
  	    // TODO: Find a better name for the cube, maybe just model name?
  	    theVisualization.createVisualizationFromModel(model.getFileName(), model.getModelName() + " Cube");
  	  } else {
  	    throw new UnsupportedOperationException("TODO: prompt to save model before visualization");
  	  }
  	}
  }
  
  public void saveWorkspace(String fileName) throws ModelerException {
  	ModelerWorkspaceUtil.saveWorkspace(model, fileName);
    model.setFileName(fileName);
    model.setDirty(false);
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
  		new ModelerException(e);
  	}
  }
  
  public void setSelectedServer(String server){
    model.setSelectedServer(server);
  }
  
  public void loadPerspective(String id){
   // document.loadPerspective(id);
  }
}
