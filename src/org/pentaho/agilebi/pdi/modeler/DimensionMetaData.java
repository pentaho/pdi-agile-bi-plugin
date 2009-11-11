package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.ui.xul.util.AbstractModelNode;

/**
 * Event aware node class that also listens to it's children's events and propagates them up.
 */
public class DimensionMetaData extends AbstractModelNode<HierarchyMetaData>{
  
  private PropertyChangeListener listener = new PropertyChangeListener(){
    public void propertyChange(PropertyChangeEvent evt) {
      fireCollectionChanged();
    }
  };
  

  String name;
  public DimensionMetaData(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  
  public String toString() {
    return "Dimension Name: " + name;
  }
  
  public String getImage(){
    return "images/sm_dim_icon.png";
  }
  
  
  @Override
  public void onAdd(HierarchyMetaData child) {
    child.addPropertyChangeListener("children", listener);
  }

  @Override
  public void onRemove(HierarchyMetaData child) {
    child.removePropertyChangeListener(listener);
  }
  
  public boolean isTime(){
    // TODO: make time dimension real
    return false;
  }
  
}