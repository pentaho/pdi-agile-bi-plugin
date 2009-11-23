package org.pentaho.agilebi.pdi.spoon;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.ui.xul.impl.XulEventHandler;

public class AgileBISpoonPlugin implements SpoonPlugin{

  public List<? extends XulEventHandler> getEventHandlers() {
    return Collections.singletonList(new SpoonUIEventHandler());
  }

  public List<String> getOverlays() {
    return Collections.singletonList("org/pentaho/agilebi/pdi/spoon/overlays.xul");
  }

}
