/**
 * 
 */
package org.pentaho.agilebi.pdi.modeler;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class MainModelNode extends AbstractMetaDataModelNode<AbstractMetaDataModelNode<?>> implements Serializable {

  private static final long serialVersionUID = 2399128598598210134L;

  String name = "Model";
  
  private MeasuresCollection measures = new MeasuresCollection();
  private DimensionMetaDataCollection dimensions = new DimensionMetaDataCollection();
  
  public MainModelNode(){
    add(measures);
    add(dimensions);
    setExpanded(true);
    measures.setExpanded(true);
    dimensions.setExpanded(true);
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    // noop
  }

  public String getImage() {
    return "images/sm_model_icon.png"; //$NON-NLS-1$
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
    this.changeSupport.firePropertyChange("children", null, this); //$NON-NLS-1$
  }

  @Override
  public void onAdd(AbstractMetaDataModelNode<?> child) {
    child.addPropertyChangeListener("children", listener); //$NON-NLS-1$
    child.addPropertyChangeListener("valid",validListener); //$NON-NLS-1$
  }

  @Override
  public void onRemove(AbstractMetaDataModelNode<?> child) {
    child.removePropertyChangeListener(listener);
    child.removePropertyChangeListener(validListener);
  }
  
  public DimensionMetaDataCollection getDimensions(){
    return dimensions;
  }

  public MeasuresCollection getMeasures(){
    return measures;
  }
  
  public boolean isEditingDisabled(){
    return true;
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return MainModelerNodePropertiesForm.class;
  }

  @Override
  public String getValidImage() {
    return getImage();
  }

  @Override
  public void validate() {
    valid = true;
    this.validationMessages.clear();
    if(this.children.size() != 2){
      valid = false;
      this.validationMessages.add(BaseMessages.getString(this.getClass(), "model_structure_invalid"));
    }
    for(AbstractMetaDataModelNode child : children){
      valid &= child.isValid();
      this.validationMessages.addAll(child.getValidationMessages());
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((dimensions == null) ? 0 : dimensions.hashCode());
    result = prime * result + ((listener == null) ? 0 : listener.hashCode());
    result = prime * result + ((measures == null) ? 0 : measures.hashCode());
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
    MainModelNode other = (MainModelNode) obj;
    if (dimensions == null) {
      if (other.dimensions != null)
        return false;
    } else if (!dimensions.equals(other.dimensions))
      return false;
    if (listener == null) {
      if (other.listener != null)
        return false;
    } else if (!listener.equals(other.listener))
      return false;
    if (measures == null) {
      if (other.measures != null)
        return false;
    } else if (!measures.equals(other.measures))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
  
  
  
}