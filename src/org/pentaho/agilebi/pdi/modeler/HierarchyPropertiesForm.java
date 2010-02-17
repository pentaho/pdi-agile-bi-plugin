package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;

public class HierarchyPropertiesForm extends AbstractModelerNodeForm<HierarchyMetaData>{

  private XulTextbox name;
  private HierarchyMetaData dim;
  private XulVbox messageBox;
  private XulLabel messageLabel;
  
  private PropertyChangeListener nameListener = new PropertyChangeListener(){
    public void propertyChange(PropertyChangeEvent arg0) {
      setName(dim.getName());
    }
  };
  
  public HierarchyPropertiesForm(){
    super("hierarchyprops");
  }
  
  public void setObject(HierarchyMetaData dim) {
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
    name = (XulTextbox) document.getElementById("hierarchy_name");
    messageBox = (XulVbox) document.getElementById("hierarchy_message");
    messageLabel = (XulLabel) document.getElementById("hierarchy_message_label");
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
