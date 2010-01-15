package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulHbox;
import org.pentaho.ui.xul.containers.XulVbox;

public class LevelsPropertiesForm extends AbstractModelerNodeForm<LevelMetaData> {

  private XulTextbox name;
  private XulLabel sourceLabel;
  private XulLabel level_message_label;
  private XulVbox messageBox;
  

  private PropertyChangeListener validListener = new PropertyChangeListener(){

    public void propertyChange(PropertyChangeEvent arg0) {
      setObject(dim);
    }
  };
  
  private LevelMetaData dim;
  private PropertyChangeListener nameListener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent arg0) {
      setName(dim.getName());
    }
  };

  public LevelsPropertiesForm() {
    super("levelprops");
  }

  public void setObject(LevelMetaData dim) {
    if (this.dim != null) {
      this.dim.removePropertyChangeListener(nameListener);
      this.dim.removePropertyChangeListener(validListener);
    }
    
    this.dim = dim;
    this.dim.addPropertyChangeListener("valid", validListener);
    
    this.dim.addPropertyChangeListener("name", nameListener);
    if(dim.getLogicalColumn() != null){
      sourceLabel.setValue(dim.getLogicalColumn().getName(Locale.getDefault().toString()));
    } else {
      sourceLabel.setValue("");
    }
    
    name.setValue(dim.getName());
    messageBox.setVisible(dim.getValidationMessages().size() > 0);
    
    level_message_label.setValue(dim.getValidationMessagesString());
    setNotValid(!dim.isValid());
  }

  public void init() {
    super.init();
    bf.createBinding(this, "notValid", "level_message", "visible");
    name = (XulTextbox) document.getElementById("level_name");
    sourceLabel = (XulLabel) document.getElementById("level_source_col");
    level_message_label = (XulLabel) document.getElementById("level_message_label");
    messageBox = (XulVbox) document.getElementById("level_message");
    bf.createBinding(this, "notValid", "fixLevelColumnsBtn", "visible");
    
    
    bf.createBinding(this, "name", name, "value");

  }

  public void setName(String name) {
    if (dim != null) {
      dim.setName(name);
    }
    this.name.setValue(name);
  }

  public String getName() {
    if (dim == null) {
      return null;
    }
    return dim.getName();
  }

  public boolean isNotValid() {
    if (dim != null) {
      return !dim.isValid();
    } else {
      return false;
    }
  }

  public void setNotValid(boolean notValid) {
    this.firePropertyChange("notValid", null, notValid);
  }

}
