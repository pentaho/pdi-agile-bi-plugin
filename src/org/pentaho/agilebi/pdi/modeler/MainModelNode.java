/**
 * 
 */
package org.pentaho.agilebi.pdi.modeler;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class MainModelNode extends AbstractModelNode<XulEventSourceAdapter> implements Serializable {
  String name = "Model";
  
  private MeasuresCollection measures = new MeasuresCollection();
  private DimensionMetaDataCollection dimensions = new DimensionMetaDataCollection();
  
  public MainModelNode(){
    add(measures);
    add(dimensions);
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    // noop
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
  public void onAdd(XulEventSourceAdapter child) {
    child.addPropertyChangeListener("children", listener);
  }

  @Override
  public void onRemove(XulEventSourceAdapter child) {
    child.removePropertyChangeListener(listener);
  }
  
  public DimensionMetaDataCollection getDimensions(){
    return dimensions;
  }

  public MeasuresCollection getMeasures(){
    return measures;
  }
}