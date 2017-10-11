/*!
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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.agilebi.spoon.publish;

import org.pentaho.di.core.encryption.Encr;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A class for loading, saving and manipulating a list of connections to BI servers.
 * @author jamesdixon
 *
 */
public class BiServerConfig {

  private static BiServerConfig instance = null;
  
  private static final String CONFIG_FILE_LOCATION = "plugins/spoon/agile-bi/biservers.properties"; //$NON-NLS-1$
    
  private BiServerConnectionCollection servers;
  
  /**
   * Creates a BiServerConfig instance
   * @return
   */
  public static BiServerConfig getInstance() {
    if( instance == null ) {
      instance = new BiServerConfig();
    }
    return instance;
  }
  
  protected BiServerConfig() {
    servers = new BiServerConnectionCollection();
    refreshServerList();
  }
  
  /**
   * Reads the list of BI server connections from plugins/spoon/agile-bi/biservers.properties.
   */
  public void refreshServerList() {
    try {
      servers.clear();
      
      File file = new File( CONFIG_FILE_LOCATION ); 
      if(file.exists() == false){
        return;
      }
      InputStream in = new FileInputStream( file );
      Properties props = new Properties();
      props.load( in );
      int idx = 1;
      boolean running = true;
      servers.clear();
      while( running ) {
        String serverId = "biserver"+Integer.toString(idx); //$NON-NLS-1$
        String serverName = props.getProperty(serverId+"/name"); //$NON-NLS-1$
        String url = props.getProperty(serverId+"/url"); //$NON-NLS-1$
        String userId = props.getProperty(serverId+"/user"); //$NON-NLS-1$
        String password = Encr.decryptPassword( props.getProperty(serverId+"/password") );    //$NON-NLS-1$
        String defaultFolder = props.getProperty(serverId+"/defaultfolder"); //$NON-NLS-1$
        boolean defaultDatasourcePublish = "true".equalsIgnoreCase(props.getProperty(serverId+"/defaultdatasourcepublish"));  //$NON-NLS-1$//$NON-NLS-2$
        if( serverName != null ) {
          BiServerConnection server = new BiServerConnection();
          server.setName( serverName );
          server.setPassword(password);
          server.setUrl(url);
          server.setUserId(userId);
          server.setDefaultDatasourcePublish(defaultDatasourcePublish);
          server.setDefaultFolder(defaultFolder);
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
  
  /**
   * Returns a list of strings holding the names of the BI server connections
   * @return
   */
  public List<String> getServerNames() {
    List<String> serverNames = new ArrayList<String>();
    for( BiServerConnection server : servers ) {
      serverNames.add( server.getName() );
    }
    return serverNames;
  }
  
  /**
   * Returns a BiServerConnectionCollection holding the current BI server 
   * connections. If this list is modified, save() can be called to flush
   * the changes back to the properties file.
   * @return
   */
  public BiServerConnectionCollection getServerConnections() {
    return servers;
  }
  
  /**
   * Deletes a server connection by its name
   * @param serverName
   * @return
   */
  public boolean deleteServerByName( String serverName ) {
    for( BiServerConnection server : servers ) {
      if( server.getName().equals( serverName ) ) {
        servers.remove( server );
        return true;
      }
    }
    return false;
  }
  
  /**
   * Adds a server connection to the current list
   * @param server
   */
  public void addServer( BiServerConnection server ) {
    servers.add( server );
  }
  
  /**
   * Returns the server connection with the specified name
   * @param serverName
   * @return
   */
  public BiServerConnection getServerByName( String serverName ) {
    for( BiServerConnection server : servers ) {
      if( server.getName().equals( serverName ) ) {
        return server;
      }
    }
    return null;
  }
  
  /**
   * SaVes the current server connection list to the properties file.
   * The password and publish password are encrypted
   */
  public void save() {
    PrintWriter writer = null;
    try {
      File file = new File( CONFIG_FILE_LOCATION ); 
      Properties props = new Properties();

      int idx = 1;
      for( BiServerConnection server : servers ) {
        String serverId = "biserver"+Integer.toString(idx); //$NON-NLS-1$
        if( server.getName() != null ) {
          props.put(serverId+"/name", server.getName() ); //$NON-NLS-1$
        }
        if( server.getUrl() != null ) {
          props.put(serverId+"/url", server.getUrl() ); //$NON-NLS-1$
        }
        if( server.getUserId() != null ) {
          props.put(serverId+"/user", server.getUserId() ); //$NON-NLS-1$
        }
        if( server.getPassword() != null ) {
          props.put(serverId+"/password", Encr.encryptPassword( server.getPassword() ) ); //$NON-NLS-1$
       }        
        if( server.getDefaultFolder() != null ) {
          props.put(serverId+"/defaultfolder", server.getDefaultFolder()); //$NON-NLS-1$
        }
        props.put(serverId+"/defaultdatasourcepublish", server.getDefaultDatasourcePublish() ? "true" : "false" );  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        idx++;
      }
      writer = new PrintWriter( file );
      props.list( writer );

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if( writer != null ) {
        writer.close();
      }
    }    
  }
  
}
