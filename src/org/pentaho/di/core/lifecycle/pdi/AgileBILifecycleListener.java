package org.pentaho.di.core.lifecycle.pdi;

import org.pentaho.agilebi.pdi.modeler.ModelerCanvas;
import org.pentaho.agilebi.platform.JettyServer;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.ui.spoon.Spoon;

public class AgileBILifecycleListener implements LifecycleListener {

  public void onStart(LifeEventHandler arg0) throws LifecycleException {
    // start up embedded server
    try {
      JettyServer server = new JettyServer("localhost", 9999); //$NON-NLS-1$
      server.startServer();
    } catch (Exception e) {
      throw new LifecycleException("error starting embedded bi server", e, true);
    }

    ((Spoon) SpoonFactory.getInstance()).addFileListener(ModelerCanvas.getInstance(), "xmi", "Models");
  }
  
  public void onExit(LifeEventHandler arg0) throws LifecycleException {
  }
}
