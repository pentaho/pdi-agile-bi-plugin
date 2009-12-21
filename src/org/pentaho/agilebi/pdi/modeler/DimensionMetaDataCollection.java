/**
 * 
 */
package org.pentaho.agilebi.pdi.modeler;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import org.pentaho.ui.xul.util.AbstractModelNode;

public class DimensionMetaDataCollection extends AbstractModelNode<DimensionMetaData> implements Serializable {
  private String name = "Dimensions";
  
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

  private PropertyChangeListener listener = new PropertyChangeListener(){
    public void propertyChange(PropertyChangeEvent evt) {
      fireCollectionChanged();
    }
  };

  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange("children", null, this);
  }

  @Override
  public void onAdd(DimensionMetaData child) {
    child.addPropertyChangeListener("children", listener);
  }

  @Override
  public void onRemove(DimensionMetaData child) {
    child.removePropertyChangeListener(listener);
  }
  
}