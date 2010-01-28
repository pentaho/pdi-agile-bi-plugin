/**
 * 
 */
package org.pentaho.agilebi.pdi.modeler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MeasuresCollection extends AbstractMetaDataModelNode<MeasureMetaData>  implements Serializable {
  private String name = Messages.getString("measures");
  
  public MeasuresCollection(){
    this.valid = false;
  }
  
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
    return "images/sm_folder_icon.png"; //$NON-NLS-1$
  }

  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();

    if (size() == 0) {
      validationMessages.add(Messages.getString("need_one_measure"));
      valid = false;
    }
    List<String> usedNames = new ArrayList<String>();
    for(MeasureMetaData measure : children){
      valid &= measure.isValid();
      validationMessages.addAll(measure.getValidationMessages());
      if(usedNames.contains(measure.getName())){
        valid = false;
        validationMessages.add(Messages.getString("duplicate_measure_name"));
      }
      
      usedNames.add(measure.getName());
      
    }
    this.firePropertyChange("valid", null, valid);
  }

  @Override
  public void onAdd(MeasureMetaData child) {
    child.setParent(this);
    child.addPropertyChangeListener("name", nameListener);
    child.addPropertyChangeListener("valid", validListener);
    child.addPropertyChangeListener("children", childrenListener);
    validateNode();
  }
  
  public void onRemove(MeasureMetaData child){
    child.removePropertyChangeListener(validListener);
    child.removePropertyChangeListener(nameListener);
    child.removePropertyChangeListener(childrenListener);
    validateNode();
  }
  

  
  public boolean isEditingDisabled(){
    return true;
  }
  
  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return GenericPropertiesForm.class;
  }
}