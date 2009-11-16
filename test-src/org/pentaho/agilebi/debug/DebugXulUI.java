package org.pentaho.agilebi.debug;

import org.pentaho.agilebi.pdi.modeler.ModelerController;
import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.XulUI;

public class DebugXulUI {

  public static void main(String[] args){
    try {
      new XulUI(null, new ModelerController(new ModelerWorkspace())).startDebugWindow();
    } catch (ModelerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
}
