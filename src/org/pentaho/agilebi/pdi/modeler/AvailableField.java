package org.pentaho.agilebi.pdi.modeler;

import java.io.Serializable;

import org.pentaho.metadata.model.LogicalColumn;

public class AvailableField implements Serializable {

  private static final long serialVersionUID = -4430951279551589688L;
  
  private transient LogicalColumn logicalColumn;
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

  public String toString() {
    return name;
  }
  
}
