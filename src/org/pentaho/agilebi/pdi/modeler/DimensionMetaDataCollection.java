/**
 * 
 */
package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

public class DimensionMetaDataCollection extends AbstractMetaDataModelNode<DimensionMetaData> implements Serializable {

  private static final long serialVersionUID = -6327799582519270107L;
  
  private String name = "Dimensions";
  
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

  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange("children", null, this); //$NON-NLS-1$
  }

  @Override
  public void onAdd(DimensionMetaData child) {
    child.setParent(this);
    child.addPropertyChangeListener("children", listener); //$NON-NLS-1$
  }

  @Override
  public void onRemove(DimensionMetaData child) {
    child.removePropertyChangeListener(listener);
  }

  @Override
  public String getValidImage() {
    return null;
  }

  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();
    if (size() == 0) {
      validationMessages.add("Model requires at least one Dimension");
      valid = false;
    }
  }
  
  public boolean isEditingDisabled(){
    return true;
  }
  
}