/**
 * 
 */
package org.pentaho.agilebi.pdi.modeler;

import java.awt.Image;
import java.io.Serializable;

import org.pentaho.ui.xul.util.AbstractModelNode;

public class MeasuresCollection extends AbstractMetaDataModelNode<FieldMetaData>  implements Serializable {
  private String name = "Measures";
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public boolean isUiExpanded(){
    return true;
  }

  @Override
  public String getValidImage() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void validate() {
    valid = true;
    if (size() == 0) {
      validationMessages.add("Model requires at least one Measure");
      valid = false;
    }
    
  }

  @Override
  public void onAdd(FieldMetaData field) {
    field.setParent(this);
  }
  
  public boolean isEditingDisabled(){
    return true;
  }
}