package org.pentaho.agilebi.pdi.modeler;

public class ModelerException extends Exception{
  
  public ModelerException(String msg){
    super(msg);
  }
  
  public ModelerException(String msg, Throwable t){
    super(msg, t);
  }
  
  public ModelerException(Throwable t){
    super(t);
  }
}
