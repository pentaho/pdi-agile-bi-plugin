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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.lang.ObjectUtils.Null;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.pentaho.agilebi.pdi.perspective.AgileBiModelerPerspective;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
import org.pentaho.agilebi.platform.JettyServer;
import org.pentaho.agilebi.vfs.MetadataToMondrianVfs;
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.core.plugins.PluginClassTypeMapping;
import org.pentaho.di.ui.spoon.Spoon;

@LifecyclePlugin(id="AgileBiPlugin")
@PluginClassTypeMapping(classTypes = { GUIOption.class }, implementationClass = {Null.class})
public class AgileBILifecycleListener implements LifecycleListener, GUIOption{
	public static int consolePort;
  public void onStart(LifeEventHandler arg0) throws LifecycleException {
    try {
      
      // because we're outside of the default classpath,
      // META-INF/providers.xml is not loaded, so instead,
      // we register our VFS provider programmatically
      ((DefaultFileSystemManager)VFS.getManager()).addProvider("mtm", new MetadataToMondrianVfs());
      
      int port = 9999;
      File consoleProp = new File("plugins/spoon/agile-bi/console.properties");
      FileInputStream in = new FileInputStream(consoleProp);
      try{
	      if(consoleProp.exists()){
	    	  Properties prop = new Properties();
	    	  prop.load(in);
	    	  String val = prop.getProperty("jetty.port");
	    	  if(val != null){
	    		  port = Integer.parseInt(val);
	    	  }
	      }
      } finally {
    	  in.close();
      }
      AgileBILifecycleListener.consolePort = port;
      JettyServer server = new JettyServer("localhost", port); //$NON-NLS-1$
      server.startServer();
    } catch (Exception e) {
      e.printStackTrace();
    }

    ((Spoon) SpoonFactory.getInstance()).addFileListener(AgileBiModelerPerspective.getInstance());
    
    for (IVisualization viz : VisualizationManager.getInstance().getVisualizations()) {
      ((Spoon) SpoonFactory.getInstance()).addFileListener(viz);
    }
  }
  
  public void onExit(LifeEventHandler arg0) throws LifecycleException {
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
