package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;

public class GenericPropertiesForm extends AbstractModelerNodeForm<AbstractMetaDataModelNode>{

  private XulTextbox name;
  private XulVbox messageBox;
  private XulLabel messageLabel;
  private AbstractMetaDataModelNode node;
  private PropertyChangeListener validListener = new PropertyChangeListener(){
    public void propertyChange(PropertyChangeEvent arg0) {
      setObject(node);
    }
  };
  
  public GenericPropertiesForm(){
    super("genericProps");
  }
  
  public void setObject(AbstractMetaDataModelNode node) {
    if(this.node != null){
      this.node.removePropertyChangeListener(validListener);
    }
    this.node = node;
    this.node.addPropertyChangeListener("valid", validListener);
    messageLabel.setValue(node.getValidationMessagesString());
    messageBox.setVisible(node.getValidationMessages().size() > 0);
  }

  public void init() {
    super.init();
    messageBox = (XulVbox) document.getElementById("generic_message");
    messageLabel = (XulLabel) document.getElementById("generic_message_label");
    
  }
  
}

