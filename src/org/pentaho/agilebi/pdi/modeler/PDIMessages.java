package org.pentaho.agilebi.pdi.modeler;

import java.util.Enumeration;
import java.util.ResourceBundle;


public class PDIMessages extends ResourceBundle{

  private static ResourceBundle lafBundle;
  
  @Override
  public Enumeration<String> getKeys() {
    return null;
  }

  @Override
  protected Object handleGetObject(String key) {
    String result = Messages.getString(key);
    return result;
  }
  
}