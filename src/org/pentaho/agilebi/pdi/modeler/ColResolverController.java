package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;


/**
 * Serves as a Controller and Form Model for the Missing Column Resolver Dialog. 
 * Condensed here for succinctness.
 * 
 * @author nbaker
 *
 */
public class ColResolverController extends AbstractXulEventHandler{

  private ModelerWorkspace workspace;
  private XulDialog dialog;
  private ColumnBackedNode node;
  private XulListbox availableCols;
  
  public ColResolverController(){
    
  }
  
  public void show(ModelerWorkspace workspace, ColumnBackedNode node){
    this.workspace = workspace;
    this.node = node;
    availableCols.setElements(workspace.getAvailableFields());
    dialog.show();
  }
  
  public void init(){
    this.dialog = (XulDialog) document.getElementById("resolveColumnsDialog");
    availableCols = (XulListbox) document.getElementById("resolveColumnsList");
    
  }
  
  public void done(){
    int idx = this.availableCols.getSelectedIndex();
    if(idx > -1){
      LogicalColumn lCol = workspace.getAvailableFields().get(idx).getLogicalColumn();
      node.setLogicalColumn(lCol);
      workspace.setDirty(true);
    }
    dialog.hide();
  }
  
  public void cancel(){
    dialog.hide();
  }
  
  public String getName(){
    return "colResolver";
  }
}
