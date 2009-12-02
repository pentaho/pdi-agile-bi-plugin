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
package org.pentaho.agilebi.pdi.modeler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BiServerConfig {

  private static BiServerConfig instance = null;
  
  private static final String CONFIG_FILE_LOCATION = "plugins/spoon/agile-bi/biservers.properties"; //$NON-NLS-1$
  
  private Properties props;
  
  private List<BiServerConnection> servers;
  
  public static BiServerConfig getInstance() {
    if( instance == null ) {
      instance = new BiServerConfig();
    }
    return instance;
  }
  
  protected BiServerConfig() {
    props = new Properties();
    servers = new ArrayList<BiServerConnection>();
    refreshServerList();
  }
  
  public void refreshServerList() {
    try {
      File file = new File( CONFIG_FILE_LOCATION ); 
      InputStream in = new FileInputStream( file );
      props.load( in );
      int idx = 1;
      boolean running = true;
      while( running ) {
        String serverId = "biserver"+Integer.toString(idx); //$NON-NLS-1$
        String serverName = props.getProperty(serverId+"/name"); //$NON-NLS-1$
        String url = props.getProperty(serverId+"/url"); //$NON-NLS-1$
        String userId = props.getProperty(serverId+"/user"); //$NON-NLS-1$
        String password = props.getProperty(serverId+"/password");    //$NON-NLS-1$
        String publishPassword = props.getProperty(serverId+"/publishpassword"); //$NON-NLS-1$
        if( serverName != null ) {
          BiServerConnection server = new BiServerConnection();
          server.setName( serverName );
          server.setPassword(password);
          server.setPublishPassword(publishPassword);
          server.setUrl(url);
          server.setUserId(userId);
          servers.add( server );
          idx++;
        } else {
          running = false;
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
  
  public List<String> getServerNames() {
    List<String> serverNames = new ArrayList<String>();
    for( BiServerConnection server : servers ) {
      serverNames.add( server.getName() );
    }
    return serverNames;
  }
  
  public List<BiServerConnection> getServerConnections() {
    return servers;
  }
  
  public boolean deleteServerByName( String serverName ) {
    for( BiServerConnection server : servers ) {
      if( server.getName().equals( serverName ) ) {
        servers.remove( server );
        return true;
      }
    }
    return false;
  }
  
  public void addServer( BiServerConnection server ) {
    servers.add( server );
  }
  
  public BiServerConnection getServerByName( String serverName ) {
    for( BiServerConnection server : servers ) {
      if( server.getName().equals( serverName ) ) {
        return server;
      }
    }
    return null;
  }
  
  public void save() {
    
  }
  
}
