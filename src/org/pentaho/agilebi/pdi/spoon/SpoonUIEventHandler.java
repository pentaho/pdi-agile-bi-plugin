package org.pentaho.agilebi.pdi.spoon;

import java.lang.reflect.Method;

import org.pentaho.agilebi.pdi.modeler.ModelerCanvas;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class SpoonUIEventHandler extends AbstractXulEventHandler{

  public String getName(){
    return "agileBi";
  }
  

  public void openModeler() {
    
    try{
      ModelerCanvas.getInstance().createModelerTabFromOutputStep();
    } catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public void quickVisualize() {
    System.out.println("In 'quickVisualize()'");
  }
}
