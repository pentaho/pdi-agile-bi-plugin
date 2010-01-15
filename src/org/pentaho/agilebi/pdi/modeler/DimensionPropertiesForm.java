package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;

public class DimensionPropertiesForm extends AbstractModelerNodeForm<DimensionMetaData>{

  private XulTextbox name;
  private DimensionMetaData dim;
  private XulVbox messageBox;
  private XulLabel messageLabel;
  private PropertyChangeListener nameListener = new PropertyChangeListener(){
    public void propertyChange(PropertyChangeEvent arg0) {
      setName(dim.getName());
    }
  };
  
  public DimensionPropertiesForm(){
    super("dimensionprops");
  }
  
  public void setObject(DimensionMetaData dim) {
    if(this.dim != null){
      this.dim.removePropertyChangeListener(nameListener);
    }
    this.dim = dim;
    this.dim.addPropertyChangeListener("name", nameListener);
    name.setValue(dim.getName());
    messageLabel.setValue(dim.getValidationMessagesString());
    messageBox.setVisible(dim.getValidationMessages().size() > 0);
  }

  public void init() {
    super.init();
    name = (XulTextbox) document.getElementById("dimension_name");
    messageBox = (XulVbox) document.getElementById("dimension_message");
    messageLabel = (XulLabel) document.getElementById("dimension_message_label");
    bf.createBinding(this, "name", name, "value");
    
  }
  
  public void setName(String name){
    if(dim != null){
      dim.setName(name);
    }
    this.name.setValue(name);
  }
  
  public String getName(){
    if(dim == null){
      return null;
    }
    return dim.getName();
  }
  
  
}
