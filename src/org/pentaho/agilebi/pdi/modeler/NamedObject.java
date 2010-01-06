package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeListener;

import org.pentaho.ui.xul.XulEventSource;

public class NamedObject implements XulEventSource {
  
  private String name;
  
  public NamedObject( ) {
  }
  
  public NamedObject( String name ) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName( String name ) {
    this.name = name;
  }

  public void addPropertyChangeListener(PropertyChangeListener arg0) {
  }

  public void removePropertyChangeListener(PropertyChangeListener arg0) {
  }
}