package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.metadata.model.LogicalColumn;

public interface ColumnBackedNode {
  void setLogicalColumn(LogicalColumn col);
  LogicalColumn getLogicalColumn();
}
