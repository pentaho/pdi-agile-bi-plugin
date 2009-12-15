/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.di.core.lifecycle.pdi;

import org.pentaho.agilebi.pdi.perspective.AgileBiPerspective;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
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

    ((Spoon) SpoonFactory.getInstance()).addFileListener(AgileBiPerspective.getInstance());
    
    for (IVisualization viz : VisualizationManager.getInstance().getVisualizations()) {
      ((Spoon) SpoonFactory.getInstance()).addFileListener(viz);
    }
    
  }
  
  public void onExit(LifeEventHandler arg0) throws LifecycleException {
  }
}
