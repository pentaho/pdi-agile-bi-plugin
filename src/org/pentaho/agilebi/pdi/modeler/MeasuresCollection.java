/**
 * 
 */
package org.pentaho.agilebi.pdi.modeler;

import java.awt.Image;
import java.io.Serializable;

import org.pentaho.ui.xul.util.AbstractModelNode;

public class MeasuresCollection extends AbstractModelNode<FieldMetaData>  implements Serializable {
  private String name = "Measures";
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public Image getImage() {
    return null;
  }
  
  public boolean isUiExpanded(){
    return true;
  }

  @Override
  public void onAdd(FieldMetaData field) {
    field.setParent(this);
  }
}