package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class DimensionPropertiesForm extends AbstractModelerNodeForm<DimensionMetaData>{

  private XulTextbox name;
  private DimensionMetaData dim;
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
  }

  public void init() {
    super.init();
    name = (XulTextbox) document.getElementById("dimension_name");
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
