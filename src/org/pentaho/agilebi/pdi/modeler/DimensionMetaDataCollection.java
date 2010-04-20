/**
 * 
 */
package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.i18n.BaseMessages;

public class DimensionMetaDataCollection extends AbstractMetaDataModelNode<DimensionMetaData> implements Serializable {

  private static final long serialVersionUID = -6327799582519270107L;
  
  private String name = "Dimensions";

  public DimensionMetaDataCollection(){
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

  private transient PropertyChangeListener listener = new PropertyChangeListener(){
    public void propertyChange(PropertyChangeEvent evt) {
      fireCollectionChanged();
    }
  };

  //TODO: investigate using "this" form of notification in super-class
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange("children", null, this); //$NON-NLS-1$
  }

  @Override
  public void onAdd(DimensionMetaData child) {
    child.setParent(this);
    child.addPropertyChangeListener("name", nameListener);
    child.addPropertyChangeListener("valid", validListener);
    child.addPropertyChangeListener("children", childrenListener);
    validateNode();
  }

  @Override
  public void onRemove(DimensionMetaData child) {
    child.removePropertyChangeListener(validListener);
    child.removePropertyChangeListener(nameListener);
    child.removePropertyChangeListener(childrenListener);
    validateNode();
  }
  
  @Override
  public String getValidImage() {
    return "images/sm_folder_icon.png"; //$NON-NLS-1$
  }

  @Override
  public void validate() {
    boolean prevValid = valid;
    valid = true;
    validationMessages.clear();
    if (size() == 0) {
      validationMessages.add("Model requires at least one Dimension");
      valid = false;
    }
    List<String> usedNames = new ArrayList<String>();
    
    for(DimensionMetaData dim: children){
      valid &= dim.isValid();
      validationMessages.addAll(dim.getValidationMessages());
      if(usedNames.contains(dim.getName())){
        valid = false;
        validationMessages.add(BaseMessages.getString(this.getClass(), "duplicate_dimension_names"));
      }
      usedNames.add(dim.getName());
    }
    if(this.suppressEvents == false){
      this.firePropertyChange("valid", prevValid, valid);
    }
  }
  
  public boolean isEditingDisabled(){
    return true;
  }
  
  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return GenericPropertiesForm.class;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DimensionMetaDataCollection other = (DimensionMetaDataCollection) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
  
  
  
}