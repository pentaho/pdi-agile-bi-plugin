package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.pentaho.ui.xul.util.AbstractModelNode;

/**
 * Event aware node class that also listens to it's children's events and propagates them up.
 */
public class HierarchyMetaData extends AbstractModelNode<LevelMetaData>{
  private PropertyChangeListener listener = new PropertyChangeListener(){
    public void propertyChange(PropertyChangeEvent evt) {
      fireCollectionChanged();
    }
  };
  
  

  String name;

  public HierarchyMetaData(String name) {
    this.parent = parent;
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public List<LevelMetaData> getChildren() {
    return children;
  }
  
  
  public String toString() {
    return "Hierarchy Name: " + name;
  }
  
  public String getImage(){
    return "images/hieraracy.png";
  }
  
  
  @Override
  public void onAdd(LevelMetaData child) {
    child.addPropertyChangeListener("children", listener);
  }

  @Override
  public void onRemove(LevelMetaData child) {
    child.removePropertyChangeListener(listener);
  }
  
}