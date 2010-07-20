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
package org.pentaho.agilebi.modeler;

import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.propforms.AbstractModelerNodeForm;
import org.pentaho.agilebi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.FactoryBasedBindingProvider;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulEditpanel;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.dnd.DropEvent;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelNode;
import org.pentaho.ui.xul.util.XulDialogCallback;

import java.util.*;

/**
 * XUL Event Handler for the modeling interface. This class interacts with a ModelerModel to store state.
 *
 * @author nbaker
 */
public class ModelerController extends AbstractXulEventHandler {

  protected static final String FIELD_LIST_ID = "fieldList"; //$NON-NLS-1$

  protected static final String SOURCE_NAME_LABEL_ID = "source_name"; //$NON-NLS-1$

//  private static Log logger = LogFactory.getLog(ModelerController.class);

  protected ModelerWorkspace workspace;

  private XulTree dimensionTree;
  private XulDeck propDeck;
  private AvailableField[] selectedFields;


  protected BindingFactory bf;

  private Map<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm> propertiesForms = new HashMap<Class<? extends ModelerNodePropertiesForm>, ModelerNodePropertiesForm>();

  private ColResolverController colController;

  private XulEditpanel propPanel;

  private IModelerWorkspaceHelper workspaceHelper;

  private IModelerMessages messages;

  public ModelerController( ModelerWorkspace workspace ) {
    this.workspace = workspace;
  }

  public String getName() {
    return "modeler"; //$NON-NLS-1$
  }

  public void onFieldListDrag( DropEvent event ) {
    // nothing to do here
  }

  public void onDimensionTreeDrag( DropEvent event ) {
    // todo, disable dragging of Root elements once we've updated the tree UI
  }

  public void onDimensionTreeDrop( DropEvent event ) {


    boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true);
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
          newdata.add(
              workspace.createHierarchyForParentWithNode((DimensionMetaData) event.getDropParent(), availableField));
        } else if (event.getDropParent() instanceof HierarchyMetaData) {
          // hierarchy - add as a level
          newdata.add(
              workspace.createLevelForParentWithNode((HierarchyMetaData) event.getDropParent(), availableField));
        } else if (event.getDropParent() instanceof LevelMetaData) {
          // level - cannot drop into a level
          event.setAccepted(false);
          workspace.setModelIsChanging(prevChangeState, false);
          return;
        }
      } else if (obj instanceof LevelMetaData) {
        LevelMetaData level = (LevelMetaData) obj;
        if (event.getDropParent() instanceof HierarchyMetaData) {
          // rebind to workspace, including logical column and actual parent
          level.setParent((HierarchyMetaData) event.getDropParent());
          newdata.add(level);
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          // add as a new hierarchy
          HierarchyMetaData hier = workspace.createHierarchyForParentWithNode((DimensionMetaData) event.getDropParent(),
              level);
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
        HierarchyMetaData hierarchy = (HierarchyMetaData) obj;
        if (event.getDropParent() == null) {
          DimensionMetaData dim = new DimensionMetaData(hierarchy.getName());
          dim.add(hierarchy);
          hierarchy.setParent(dim);
          // TODO: this will also need to resolve the level LogicalColumns
          newdata.add(dim);
        } else if (event.getDropParent() instanceof DimensionMetaData) {
          DimensionMetaData dim = (DimensionMetaData) event.getDropParent();
          hierarchy.setParent(dim);
          // TODO: this will also need to resolve the level LogicalColumns
          newdata.add(hierarchy);
        }
      } else if (obj instanceof DimensionMetaData) {
        if (event.getDropParent() == null) {
          newdata.add((DimensionMetaData) obj);
          // TODO: this will also need to resolve level LogicalColumns
        }
      } else if (obj instanceof MeasureMetaData) {
        if (event.getDropParent() instanceof MeasuresCollection) {
          MeasureMetaData measure = (MeasureMetaData) obj;
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
    workspace.setModelIsChanging(prevChangeState, false);
  }

  @Bindable
  public void addField() {

    boolean prevChangeState = workspace.isModelChanging();
    workspace.setModelIsChanging(true);
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
          theNode = workspace.createHierarchyForParentWithNode((DimensionMetaData) selectedTreeItem, availableField);
          DimensionMetaData theDimension = (DimensionMetaData) selectedTreeItem;
          theDimension.add((HierarchyMetaData) theNode);
        } else if (selectedTreeItem instanceof HierarchyMetaData) {
          // hierarchy - add as a level
          theNode = workspace.createLevelForParentWithNode((HierarchyMetaData) selectedTreeItem, availableField);
          HierarchyMetaData theHierarchy = (HierarchyMetaData) selectedTreeItem;
          theHierarchy.add((LevelMetaData) theNode);
        }
        if (theNode != null) {
          theNode.setParent((AbstractMetaDataModelNode) selectedTreeItem);
        }
      }
    }
    workspace.setModelIsChanging(prevChangeState);
  }

  @Bindable
  public void init() throws ModelerException {

    bf.setDocument(document);

    dimensionTree = (XulTree) document.getElementById("dimensionTree"); //$NON-NLS-1$
    propDeck = (XulDeck) document.getElementById("propertiesdeck"); //$NON-NLS-1$
//    propPanel = (XulEditpanel) document.getElementById("propertiesPanel"); //$NON-NLS-1$

    XulLabel sourceLabel = (XulLabel) document.getElementById(SOURCE_NAME_LABEL_ID);
    String connectionName = ""; //$NON-NLS-1$
    String tableName = ""; //$NON-NLS-1$

    //TODO: migrate this code elsewhere or remove it entirely
//    if( workspace.getModelSource() != null && workspace.getModelSource() instanceof OutputStepModelerSource) {
//      // for now just list the first table in the first physical workspace
//      DatabaseMeta databaseMeta = workspace.getModelSource().getDatabaseMeta();
//      if( databaseMeta != null ) {
//        connectionName = databaseMeta.getName();
//      }
//      List<IPhysicalModel> physicalModels = workspace.getDomain().getPhysicalModels();
//      if( physicalModels != null && physicalModels.size() > 0 ) {
//        List<? extends IPhysicalTable> tables = physicalModels.get(0).getPhysicalTables();
//        if( tables != null && tables.size() > 0 ) {
//          // TODO where is the locale coming from? And why do we need one here?
//          tableName = tables.get(0).getName("en_US");
//        }
//      }
//    } else if (workspace.getModelSource() != null && workspace.getModelSource() instanceof TableModelerSource) {
//      tableName = workspace.getModelSource().getTableName();
//    }

    sourceLabel.setValue(tableName);
    bf.createBinding(workspace, "sourceName", sourceLabel, "value"); //$NON-NLS-1$//$NON-NLS-2$

    bf.setBindingType(Type.ONE_WAY);
    fieldListBinding = bf.createBinding(workspace, "availableFields", FIELD_LIST_ID,
        "elements"); //$NON-NLS-1$ //$NON-NLS-2$
    selectedFieldsBinding = bf.createBinding(FIELD_LIST_ID, "selectedItems", this,
        "selectedFields"); //$NON-NLS-1$//$NON-NLS-2$


    modelTreeBinding = bf.createBinding(workspace, "model", dimensionTree, "elements"); //$NON-NLS-1$//$NON-NLS-2$
    bf.createBinding(dimensionTree, "selectedItem", this, "dimTreeSelectionChanged"); //$NON-NLS-1$//$NON-NLS-2$

    bf.setBindingType(Type.BI_DIRECTIONAL);
    bf.createBinding(workspace, "selectedNode", dimensionTree, "selectedItems",
        new BindingConvertor<AbstractMetaDataModelNode, Collection>() { //$NON-NLS-1$//$NON-NLS-2$

          @Override
          public Collection sourceToTarget( AbstractMetaDataModelNode arg0 ) {
            return Collections.singletonList(arg0);
          }

          @Override
          public AbstractMetaDataModelNode targetToSource( Collection arg0 ) {
            return (AbstractMetaDataModelNode) ((arg0 == null || arg0.isEmpty()) ? null : arg0.iterator().next());
          }

        });
    bf.setBindingType(Type.ONE_WAY);

    bf.createBinding("fieldList", "selectedItem", "addField", "disabled",
        new BindingConvertor<Object, Boolean>() { //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$

          public Boolean sourceToTarget( Object value ) {
            return getSelectedFields().length == 0 || selectedTreeItem == null || selectedTreeItem instanceof LevelMetaData || selectedTreeItem instanceof MainModelNode;
          }

          public Object targetToSource( Boolean value ) {
            return null;
          }
        });

    bf.createBinding(dimensionTree, "selectedItem", "addField", "disabled",
        new BindingConvertor<Object, Boolean>() { //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

          public Boolean sourceToTarget( Object value ) {
            return getSelectedFields().length == 0 || selectedTreeItem == null || selectedTreeItem instanceof LevelMetaData || selectedTreeItem instanceof MainModelNode;
          }

          public Object targetToSource( Boolean value ) {
            return null;
          }
        });


    //TODO: move all this datasource stuff into models! use the existing property form validation to show messages.
    datasourceButtonBinding = bf.createBinding(sourceLabel, "value", "datasource_button", "visible",
        new BindingConvertor<Object, Boolean>() { //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

          public Boolean sourceToTarget( Object value ) {

            boolean isVisible = (value == null || "".equals(value.toString()));
            XulVbox messageBox = (XulVbox) document.getElementById("main_message"); //$NON-NLS-1$
            messageBox.setVisible(isVisible);

            XulComponent datsourceError = document.getElementById("datasource_message_label");
            datsourceError.setVisible(isVisible);

            XulComponent refreshButton = document.getElementById("refreshButton"); //$NON-NLS-1$
            //refreshButton.setDisabled(isVisible);

            XulComponent addFieldButton = document.getElementById("addField"); //$NON-NLS-1$
            addFieldButton.setDisabled(isVisible);

            XulComponent autoPopulateButton = document.getElementById("autoPopulateButton"); //$NON-NLS-1$
            //autoPopulateButton.setDisabled(isVisible);

            return isVisible;
          }

          public Object targetToSource( Boolean value ) {
            return null;
          }
        });


    bf.createBinding(dimensionTree, "selectedItem", "measureBtn", "disabled",
        new ButtonConvertor(MeasuresCollection.class)); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    bf.createBinding(dimensionTree, "selectedItem", "dimensionBtn", "disabled",
        new ButtonConvertor(DimensionMetaDataCollection.class)); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    bf.createBinding(dimensionTree, "selectedItem", "hierarchyBtn", "disabled",
        new ButtonConvertor(DimensionMetaData.class)); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    bf.createBinding(dimensionTree, "selectedItem", "levelBtn", "disabled",
        new ButtonConvertor(HierarchyMetaData.class)); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    bf.setBindingType(Type.BI_DIRECTIONAL);
    //modelNameBinding = bf.createBinding(workspace, "modelName", "modelname", "value"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    //bf.createBinding(this.propPanel, "visible", this, "propVisible"); //$NON-NLS-1$//$NON-NLS-2$

    fireBindings();

    dimensionTree.setSelectedItems(Collections.singletonList(workspace.getModel()));


  }

  @Bindable
  public void showAutopopulatePrompt() {
    try {
      MainModelNode model = workspace.getModel();
      if (model.getDimensions().isEmpty() && model.getMeasures().isEmpty()) {
        autoPopulate();
      } else {
        XulConfirmBox confirm = (XulConfirmBox) document.createElement("confirmbox"); //$NON-NLS-1$
        confirm.setTitle("auto_populate_title"); //$NON-NLS-1$
        confirm.setMessage("auto_populate_msg"); //$NON-NLS-1$
        confirm.setAcceptLabel("yes"); //$NON-NLS-1$
        confirm.setCancelLabel("no"); //$NON-NLS-1$

//
//          XulConfirmBox confirm = (XulConfirmBox) document.createElement("confirmbox"); //$NON-NLS-1$
//          confirm.setTitle(BaseMessages.getString(this.getClass(), "auto_populate_title")); //$NON-NLS-1$
//          confirm.setMessage(BaseMessages.getString(this.getClass(), "auto_populate_msg")); //$NON-NLS-1$
//          confirm.setAcceptLabel(BaseMessages.getString(this.getClass(), "yes")); //$NON-NLS-1$
//          confirm.setCancelLabel(BaseMessages.getString(this.getClass(), "no")); //$NON-NLS-1$

        confirm.addDialogCallback(new XulDialogCallback() {
          public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
            if (returnCode == Status.ACCEPT) {
              autoPopulate();
            }
          }

          public void onError( XulComponent sender, Throwable t ) {
          }
        });
        confirm.open();
      }
    } catch (XulException e) {
      e.printStackTrace();//logger.error(e);
    }
  }

  protected void fireBindings() throws ModelerException {
    try {
      datasourceButtonBinding.fireSourceChanged();
      fieldListBinding.fireSourceChanged();
      selectedFieldsBinding.fireSourceChanged();
      modelTreeBinding.fireSourceChanged();
      //modelNameBinding.fireSourceChanged();
      //visualizationsBinding.fireSourceChanged();
    } catch (Exception e) {
      e.printStackTrace();//logger.info("Error firing off initial bindings", e);
      throw new ModelerException(e);
    }
  }


  /**
   * Goes back to the source of the metadata and see if anything has changed.
   * Updates the UI accordingly
   */
  @Bindable
  public void refreshFields() throws ModelerException {
    workspace.refresh();
  }

  public void setFileName( String fileName ) {
    workspace.setFileName(fileName);
  }


  @Bindable
  public void showNewMeasureDialog() {
    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement("promptbox"); //$NON-NLS-1$
      prompt.setTitle(
          "NewMeasureTitle");//messages.getString(ModelerController.class, "ModelerController.NewMeasureTitle")); //$NON-NLS-1$
      prompt.setMessage(
          "NewMeasureText");//messages.getString(ModelerController.class, "ModelerController.NewMeasureText")); //$NON-NLS-1$
      prompt.addDialogCallback(new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if (returnCode == Status.ACCEPT) {
            MeasuresCollection theMesaures = (MeasuresCollection) selectedTreeItem;
            MeasureMetaData theMeasure = new MeasureMetaData("" + retVal, "",
                "" + retVal); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

            if (selectedFields.length > 0) {
              AvailableField f = selectedFields[0];
              theMeasure.setLogicalColumn(f.getLogicalColumn());
              workspace.setDirty(true);
            }

            theMeasure.setParent(theMesaures);
            theMeasure.validate();

            boolean prevChangeState = workspace.isModelChanging();
            workspace.setModelIsChanging(true);
            theMesaures.add(theMeasure);
            workspace.setModelIsChanging(prevChangeState);

          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();//logger.error(t);
        }

      });
      prompt.open();

    } catch (Exception e) {
      e.printStackTrace();//logger.error(e);
    }
  }

  @Bindable
  public void showNewHierarchyDialog() {
    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement("promptbox"); //$NON-NLS-1$
      prompt.setTitle(
          "NewHierarchyTitle");//messages.getString(ModelerController.class, "ModelerController.NewHierarchyTitle")); //$NON-NLS-1$
      prompt.setMessage(
          "NewHierarchyText");//messages.getString(ModelerController.class, "ModelerController.NewHierarchyText")); //$NON-NLS-1$
      prompt.addDialogCallback(new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if (returnCode == Status.ACCEPT) {
            DimensionMetaData theDimension = (DimensionMetaData) selectedTreeItem;
            HierarchyMetaData theHieararchy = new HierarchyMetaData("" + retVal);
            theHieararchy.setParent(theDimension);
            theHieararchy.setExpanded(true);

            boolean prevChangeState = workspace.isModelChanging();
            workspace.setModelIsChanging(true);
            theDimension.add(theHieararchy);
            workspace.setModelIsChanging(prevChangeState);
          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();//logger.error(t);
        }
      });
      prompt.open();
    } catch (XulException e) {
      e.printStackTrace();//logger.error(e);
    }
  }

  @Bindable
  public void showNewLevelDialog() {

    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement("promptbox"); //$NON-NLS-1$
      prompt.setTitle(
          "NewLevelTitle");//messages.getString(ModelerController.class, "ModelerController.NewLevelTitle")); //$NON-NLS-1$
      prompt.setMessage(
          "NewLevelTitle");//messages.getString(ModelerController.class, "ModelerController.NewLevelText")); //$NON-NLS-1$
      prompt.addDialogCallback(new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if (returnCode == Status.ACCEPT) {
            HierarchyMetaData theHierarchy = (HierarchyMetaData) selectedTreeItem;
            LevelMetaData theLevel = new LevelMetaData(theHierarchy, "" + retVal);

            if (selectedFields.length > 0) {
              AvailableField f = selectedFields[0];
              theLevel.setLogicalColumn(f.getLogicalColumn());
              workspace.setDirty(true);
            }

            theLevel.validate();
            boolean prevChangeState = workspace.isModelChanging();
            workspace.setModelIsChanging(true);
            theHierarchy.add(theLevel);
            workspace.setModelIsChanging(prevChangeState);

          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();//logger.error(t);
        }
      });
      prompt.open();

    } catch (Exception e) {
      e.printStackTrace();//logger.error(e);
    }

  }

  @Bindable
  public void showNewDimensionDialog() {

    try {
      XulPromptBox prompt = (XulPromptBox) document.createElement("promptbox"); //$NON-NLS-1$
      prompt.setTitle(
          "NewDimentionTitle");//messages.getString(ModelerController.class, "ModelerController.NewDimensionTitle")); //$NON-NLS-1$
      prompt.setMessage(
          "NewHierarchyText");//messages.getString(ModelerController.class, "ModelerController.NewDimensionText")); //$NON-NLS-1$
      prompt.addDialogCallback(new XulDialogCallback() {

        public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
          if (returnCode == Status.ACCEPT) {

            DimensionMetaData dimension = new DimensionMetaData("" + retVal); //$NON-NLS-1$
            dimension.setExpanded(true);
            HierarchyMetaData hierarchy = new HierarchyMetaData("" + retVal); //$NON-NLS-1$
            hierarchy.setExpanded(true);
            hierarchy.validate();
            hierarchy.setParent(dimension);
            dimension.add(hierarchy);
            workspace.addDimension(dimension);
          }
        }

        public void onError( XulComponent sender, Throwable t ) {
          t.printStackTrace();//logger.error(t);
        }

      });
      prompt.open();
    } catch (XulException e) {
      e.printStackTrace();//logger.error(e);
    }
  }

  @Bindable
  public void moveFieldUp() {
    if (selectedTreeItem == null) {
      return;
    }
    ((AbstractModelNode) selectedTreeItem).getParent().moveChildUp(selectedTreeItem);

  }

  @Bindable
  public void moveFieldDown() {
    if (selectedTreeItem == null) {
      return;
    }
    ((AbstractModelNode) selectedTreeItem).getParent().moveChildDown(selectedTreeItem);

  }

  Object selectedTreeItem;

  private Binding fieldListBinding;
  private Binding selectedFieldsBinding;

  private Binding visualizationsBinding;

  private Binding modelTreeBinding;
  private Binding datasourceButtonBinding;

  private Binding modelNameBinding;


  private ModelerNodePropertiesForm selectedForm;

  @Bindable
  public void setDimTreeSelectionChanged( Object selection ) {
    selectedTreeItem = selection;
    if (selection != null && selection instanceof AbstractMetaDataModelNode) {
      AbstractMetaDataModelNode node = (AbstractMetaDataModelNode) selection;
      ModelerNodePropertiesForm form = propertiesForms.get(node.getPropertiesForm());
      if (form != null) {
        if (selectedForm != null && selectedForm != form) {
          selectedForm.setObject(null);
        }
        form.activate((AbstractMetaDataModelNode) selection);
        selectedForm = form;
        return;
      }
    }
    if (this.propDeck != null) {
      this.propDeck.setSelectedIndex(0);
    }
  }


  @Bindable
  public void removeField() {
    if (selectedTreeItem instanceof DimensionMetaDataCollection
        || selectedTreeItem instanceof MeasuresCollection
        || selectedTreeItem instanceof MainModelNode
        || selectedTreeItem == null) {
      return;
    }
    ((AbstractModelNode) selectedTreeItem).getParent().remove(selectedTreeItem);
    setDimTreeSelectionChanged(null);
  }


  public ModelerWorkspace getModel() {
    return workspace;
  }

  public void setModel( ModelerWorkspace model ) throws ModelerException {
    this.workspace = model;
    fireBindings();
  }


  protected void showValidationMessages() {

    StringBuffer validationErrors = new StringBuffer(
        "Model contains erors");//messages.getString(ModelerController.class, "model_contains_errors")); //$NON-NLS-1$
    for (String msg : workspace.getValidationMessages()) {
      validationErrors.append(msg);
      validationErrors.append("\n"); //$NON-NLS-1$
      //logger.info(msg);
    }
    try {
      XulMessageBox msg = (XulMessageBox) document.createElement("messagebox"); //$NON-NLS-1$
      msg.setTitle("modelNotValid");//messages.getString(ModelerController.class, "model_not_valid")); //$NON-NLS-1$
      msg.setMessage(validationErrors.toString());
      msg.open();
    } catch (XulException e) {
      e.printStackTrace();//logger.error(e);
    }
  }

  public boolean saveWorkspace( String fileName ) throws ModelerException {
    workspace.getModel().validateTree();
    if (workspace.isValid() == false) {
      showValidationMessages();
      return false;
    }
    //TODO: GWT remove save ModelerWorkspaceUtil.saveWorkspace(workspace, fileName);
    workspace.setFileName(fileName);
    workspace.setDirty(false);
    workspace.setTemporary(false);
    return true;
  }

  @Bindable
  public void resolveMissingColumn() {
    if (selectedTreeItem instanceof ColumnBackedNode
        && ((AbstractMetaDataModelNode) selectedTreeItem).isValid() == false) {
      changeColumn();
    }
  }

  @Bindable
  public void changeColumn() {
    colController.show(this.workspace, (ColumnBackedNode) selectedTreeItem);
  }

  public void addPropertyForm( AbstractModelerNodeForm form ) {
    propertiesForms.put(form.getClass(), form);
  }

  public void setColResolver( ColResolverController controller ) {
    this.colController = controller;
  }

  public void autoPopulate() {
    try {
    // TODO: GWT-ify
    workspaceHelper.autoModelFlatInBackground(this.workspace);
    this.dimensionTree.expandAll();
    } catch (ModelerException e) {
      e.printStackTrace();//logger.error(e);
    }
  }

  public void togglePropertiesPanel() {
    setPropVisible(!isPropVisible());
  }


  private boolean propVisible = true;

  public boolean isPropVisible() {
    return propVisible;
  }

  public void setPropVisible( boolean vis ) {
    boolean prevVal = propVisible;
    this.propVisible = vis;
    this.firePropertyChange("propVisible", prevVal, vis); //$NON-NLS-1$
  }

  private static class ButtonConvertor extends BindingConvertor<Object, Boolean> {

    private Class type;

    public ButtonConvertor( Class aClass ) {
      type = aClass;
    }

    public Boolean sourceToTarget( Object value ) {
      return value == null || !(value.getClass() == type);
    }

    public Object targetToSource( Boolean value ) {
      return null;
    }
  }

  @Bindable
  public void setSelectedFields( Object[] aFields ) {
    AvailableField[] f = new AvailableField[aFields.length];
    for(int i=0; i<aFields.length; i++){
      f[i] = (AvailableField)aFields[i];
    }
    selectedFields = f;
  }

  @Bindable
  public Object[] getSelectedFields() {
    if (selectedFields == null) {
      selectedFields = new AvailableField[]{};
    }
    return selectedFields;
  }

  public void setBindingFactory(BindingFactory bf){
    this.bf = bf;
  }

  public IModelerWorkspaceHelper getWorkspaceHelper() {
    return workspaceHelper;
  }

  public void setWorkspaceHelper( IModelerWorkspaceHelper workspaceHelper ) {
    this.workspaceHelper = workspaceHelper;
  }

  public IModelerMessages getMessages() {
    return messages;
  }

  public void setMessages( IModelerMessages messages ) {
    this.messages = messages;
  }
}
