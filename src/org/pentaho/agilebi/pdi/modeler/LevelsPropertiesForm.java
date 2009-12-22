package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;

public class LevelsPropertiesForm extends AbstractModelerNodeForm<LevelMetaData> {

  private XulTextbox name;
  private XulLabel sourceLabel;
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
    }
    this.dim = dim;
    this.dim.addPropertyChangeListener("name", nameListener);
    sourceLabel.setValue(dim.getLogicalColumn().getName(Locale.getDefault().toString()));
  }

  public void init() {
    super.init();
    name = (XulTextbox) document.getElementById("level_name");
    sourceLabel = (XulLabel) document.getElementById("level_source_col");
    
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

}
