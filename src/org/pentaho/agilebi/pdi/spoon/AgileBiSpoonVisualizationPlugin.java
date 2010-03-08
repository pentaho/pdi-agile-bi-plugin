package org.pentaho.agilebi.pdi.spoon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.agilebi.pdi.modeler.ModelerHelper;
import org.pentaho.agilebi.pdi.perspective.AgileBiVisualizationPerspective;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;

@SpoonPlugin(id = "AgileBiVis", image = "")
@SpoonPluginCategories({})
public class AgileBiSpoonVisualizationPlugin implements SpoonPluginInterface{

  public void applyToContainer(String category, XulDomContainer container) throws XulException {
     
  }


  public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  public SpoonPerspective getPerspective() {
    return AgileBiVisualizationPerspective.getInstance();
  }
  
}
