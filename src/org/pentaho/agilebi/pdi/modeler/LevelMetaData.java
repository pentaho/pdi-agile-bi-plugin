/**
 * 
 */
package org.pentaho.agilebi.pdi.modeler;

import java.util.List;

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class LevelMetaData extends XulEventSourceAdapter {
  String name;
  String columnName;
  HierarchyMetaData parent;
  LogicalColumn logicalColumn;
  
  public LevelMetaData(HierarchyMetaData parent, String name) {
    this.parent = parent;
    this.name = name;
    this.columnName = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getColumnName() {
    return columnName;
  }
  
  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }
  
  
  public HierarchyMetaData getParent() {
    return parent;
  }
  
  public String toString() {
    return "Level Name: " + name + "\nColumn Name: " + columnName;
  }

  public String getImage(){
    return "images/sm_level_icon.png";
  }

  public List getChildren(){
    return null;
  }

  public LogicalColumn getLogicalColumn() {
    return logicalColumn;
  }

  public void setLogicalColumn(LogicalColumn logicalColumn) {
    this.logicalColumn = logicalColumn;
  }
  
  public boolean isUniqueMembers(){
    // TODO: make real
    return true;
  }
  
  
}