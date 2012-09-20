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

import java.net.Socket;

import org.apache.commons.lang.ObjectUtils.Null;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.pentaho.agilebi.modeler.util.ModelerSourceFactory;
import org.pentaho.agilebi.platform.JettyServer;
import org.pentaho.agilebi.spoon.KettleModelerSource;
import org.pentaho.agilebi.spoon.OutputStepModelerSource;
import org.pentaho.agilebi.spoon.perspective.AgileBiInstaPerspective;
import org.pentaho.agilebi.spoon.perspective.AgileBiModelerPerspective;
import org.pentaho.agilebi.spoon.perspective.AgileBiSpoonInstaPlugin;
import org.pentaho.agilebi.spoon.visualizations.IVisualization;
import org.pentaho.agilebi.spoon.visualizations.VisualizationManager;
import org.pentaho.agilebi.vfs.MetadataToMondrianVfs;
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.core.plugins.PluginClassTypeMapping;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

@LifecyclePlugin(id="AgileBiPlugin")
@PluginClassTypeMapping(classTypes = { GUIOption.class }, implementationClass = {Null.class})
public class AgileBILifecycleListener implements LifecycleListener, GUIOption{
	public static int consolePort;
  private JettyServer server = null;

  public void onStart(LifeEventHandler arg0) throws LifecycleException {
    try {

      // because we're outside of the default classpath,
      // META-INF/providers.xml is not loaded, so instead,
      // we register our VFS provider programmatically
      ((DefaultFileSystemManager)VFS.getManager()).addProvider("mtm", new MetadataToMondrianVfs());

      int port = 9999;
      boolean portFound = false;
      int tries = 100;
      while(portFound == false && tries > 0){
        port++;
        tries--;
        Socket sock = null;
        try {
          sock = new Socket("localhost", port);
        } catch (Exception e) {
          portFound = true;
        } finally {
          if(sock != null){
            sock.close();
          }
        }
      }
      if(!portFound){
        throw new IllegalStateException("Could not find an open port to start the Agile-BI server on");
      }

      AgileBILifecycleListener.consolePort = port;
      server = new JettyServer("localhost", port); //$NON-NLS-1$
      server.startServer();
  
      // Only initialize the Instaview perspective if the Instaview plugin is registered
      if (AgileBiSpoonInstaPlugin.isInstaviewRegistered(PentahoSystem.get(IPluginManager.class))) {
        AgileBiInstaPerspective.getInstance().onStart();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    ModelerSourceFactory.registerSourceType(OutputStepModelerSource.OUTPUTSTEP_SOURCE_TYPE, OutputStepModelerSource.class);
    ModelerSourceFactory.registerSourceType(KettleModelerSource.SOURCE_TYPE, KettleModelerSource.class);
    if( SpoonFactory.getInstance() != null ) { // condition if for unit testing
	    ((Spoon) SpoonFactory.getInstance()).addFileListener(AgileBiModelerPerspective.getInstance());
	
	    for (IVisualization viz : VisualizationManager.getInstance().getVisualizations()) {
	      ((Spoon) SpoonFactory.getInstance()).addFileListener(viz);
	    }
    }
  }

  public void onExit(LifeEventHandler arg0) throws LifecycleException {
	    server.stopServer();
		AgileBiInstaPerspective.getInstance().shutdown();
  }

  public String getLabelText() {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getLastValue() {
    // TODO Auto-generated method stub
    return null;
  }

  public DisplayType getType() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setValue(Object value) {
    // TODO Auto-generated method stub

  }


}
