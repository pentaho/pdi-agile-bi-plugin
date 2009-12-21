package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.metadata.model.LogicalColumn;

public class AvailableField {
  private LogicalColumn logicalColumn;
  private String name, displayName, aggTypeDesc;

  public LogicalColumn getLogicalColumn() {
    return logicalColumn;
  }

  public void setLogicalColumn(LogicalColumn logicalColumn) {
    this.logicalColumn = logicalColumn;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getAggTypeDesc() {
    return aggTypeDesc;
  }

  public void setAggTypeDesc(String aggTypeDesc) {
    this.aggTypeDesc = aggTypeDesc;
  }
  
  
  
}
