package org.pentaho.agilebi.pdi.spoon;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.agilebi.pdi.modeler.ModelerHelper;
import org.pentaho.agilebi.pdi.perspective.AgileBiPerspective;
import org.pentaho.agilebi.pdi.perspective.AgileBiVisualizationPerspective;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;

public class AgileBiSpoonVisualizationPlugin implements SpoonPlugin{

  public Map<String, XulEventHandler> getEventHandlers() {
    HashMap<String, XulEventHandler> hash = new HashMap<String, XulEventHandler>();
//    hash.put("spoon", ModelerHelper.getInstance());
//    hash.put("databaseDialog", ModelerHelper.getInstance());
    return hash;
  }

  public Map<String, XulOverlay> getOverlays() {
//    XulOverlay overlay = new DefaultXulOverlay("spoon.agilebi", "org/pentaho/agilebi/pdi/spoon/overlays.xul", null, null); //$NON-NLS-1$  //$NON-NLS-2$
    HashMap<String, XulOverlay> hash = new HashMap<String, XulOverlay>();
//    hash.put("spoon", overlay);
    return hash;
  }

  public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  public SpoonPerspective getPerspective() {
    return AgileBiVisualizationPerspective.getInstance();
  }
  
}
