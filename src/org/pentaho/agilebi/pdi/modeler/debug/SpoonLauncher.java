package org.pentaho.agilebi.pdi.modeler.debug;

import org.pentaho.di.ui.spoon.Spoon;

public class SpoonLauncher {
  public static void main(String[] args){
    try{
      Spoon.main(new String[]{});
    } catch(Exception e){
      e.printStackTrace();
    }
  }
}
