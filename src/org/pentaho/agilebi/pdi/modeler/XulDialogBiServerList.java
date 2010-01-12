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

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulListbox;

/**
 * A dialog for administering a list of connections to BI servers
 * @author jamesdixon
 *
 */
public class XulDialogBiServerList extends AbstractSwtXulDialogController {

  private static Log logger = LogFactory.getLog(XulDialogBiServerList.class);
  
  private BindingFactory bf = new DefaultBindingFactory();
  
  private Binding serverNamesBinding;
  
  private XulListbox serverListBox;
  
  private XulButton testButton, removeButton, saveButton;

  private XulTextbox nameTextBox, urlTextBox, userIdTextBox, passwordTextBox, publishPasswordTextBox;

  private String serverName, url, userId, password, publishPassword;
  
  private Binding nameBinding, urlBinding, userIdBinding, passwordBinding, publishPasswordBinding;

  private BiServerConnectionCollection biServerConnections;
  
  private BiServerConfig biServerConfig;
  
  /**
   * Creates a new dialog. Gets the current list of BI server connections from the properties file
   * @param shell
   * @throws XulException
   */
  public XulDialogBiServerList( Shell shell ) throws XulException {
    super( shell ); 
    
    setName("dialog"); //$NON-NLS-1$

    // create the 'biserverlist' dialog
    initDialogController( "org/pentaho/agilebi/pdi/modeler/res/biserverconfig.xul", "biserverlist", null, null ); //$NON-NLS-1$ //$NON-NLS-2$
    
  }

  @Override
  protected Object getDialogResult() {
    return null;
  }

  @Override
  public Log getLogger() {
    return logger;
  }
  
  /**
   * Initializes the dialog. Finds the buttons so we can dis/enable them as 
   * needed. Displays the list of current connections. Sets the initial
   * buttons status.
   */
  public void init() {

    biServerConfig = BiServerConfig.getInstance();
    biServerConnections = biServerConfig.getServerConnections();

    bf.setDocument(document);

    bf.setBindingType(Type.ONE_WAY);

    serverListBox = (XulListbox)document.getElementById("serverListBox"); //$NON-NLS-1$

    nameTextBox = (XulTextbox)document.getElementById("name"); //$NON-NLS-1$
    urlTextBox = (XulTextbox)document.getElementById("url"); //$NON-NLS-1$
    userIdTextBox = (XulTextbox)document.getElementById("userid"); //$NON-NLS-1$
    passwordTextBox = (XulTextbox)document.getElementById("password"); //$NON-NLS-1$
    publishPasswordTextBox = (XulTextbox)document.getElementById("publishpassword"); //$NON-NLS-1$
    saveButton = (XulButton)document.getElementById("saveButton"); //$NON-NLS-1$
    testButton = (XulButton)document.getElementById("testButton"); //$NON-NLS-1$
    removeButton = (XulButton)document.getElementById("removeButton"); //$NON-NLS-1$
    
    serverNamesBinding = bf.createBinding(this, "BiServerConnections", serverListBox, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
    bf.setBindingType(Type.BI_DIRECTIONAL);
    nameBinding = bf.createBinding(this, "serverName", nameTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    urlBinding = bf.createBinding(this, "url", urlTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    userIdBinding = bf.createBinding(this, "userId", userIdTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    passwordBinding = bf.createBinding(this, "password", passwordTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    publishPasswordBinding = bf.createBinding(this, "publishPassword", publishPasswordTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    
    updateConnectionList();
    updateButtonStatus();
    
  }
  
  /**
   * Updates the server list to reflect the current state
   */
  private void updateConnectionList() {
    try {
      serverNamesBinding.fireSourceChanged();
    } catch (IllegalArgumentException e1) {
      e1.printStackTrace();
    } catch (XulException e1) {
      e1.printStackTrace();
    } catch (InvocationTargetException e1) {
      e1.printStackTrace();
    }
  }
  
  /**
   * Returns the list of BI server connections
   * @return
   */
  public BiServerConnectionCollection getBiServerConnections() {
    return biServerConnections;
  }

  /**
   * Adds a BI server connection. Opens the XulDialogBiServerConfig dialog.
   * If the user creates a connection it is added to the list, and the
   * listbox and buttons are refreshed. The list is flushed to the properties
   * file.
   */
  public void addBiServer() {

    serverListBox.setSelectedIndex(-1);
    serverName = "untitled";
    url = "http://server:port/pentaho";
    userId = ""; //$NON-NLS-1$
    password = ""; //$NON-NLS-1$
    publishPassword = ""; //$NON-NLS-1$
    updateSettings();
    updateButtonStatus();
    
  }

  /**
   * Removes a BI server connection. The listbox and buttons are refreshed. 
   * The list is flushed to the properties file.
   */  public void removeBiServer() {
    int idx = serverListBox.getSelectedIndex();
    if( idx == -1 ) {
      return;
    }
    biServerConnections.remove(idx);
    serverName = ""; //$NON-NLS-1$
    url = ""; //$NON-NLS-1$
    userId = ""; //$NON-NLS-1$
    password = ""; //$NON-NLS-1$
    publishPassword = ""; //$NON-NLS-1$
    updateConnectionList();
    updateSettings();
    updateButtonStatus();
  }
  
   /**
    * Saves a BI server connection. The current settings are copied to the
    * current BI server connection object.
    */
  public void saveServerConnection() {
    int idx = serverListBox.getSelectedIndex();
    BiServerConnection biServerConnection = null;
    if( idx == -1 ) {
      // we are doing an add
      biServerConnection = new BiServerConnection();
      biServerConnections.add(biServerConnection);
      idx = biServerConnections.size()-1;
    } else {
      biServerConnection = biServerConnections.get( idx );
      if( biServerConnection == null ) {
        return;
      }
    }
    
    biServerConnection.setName( serverName );
    biServerConnection.setUrl( url );
    biServerConnection.setUserId(userId);
    biServerConnection.setPassword(password);
    biServerConnection.setPublishPassword(publishPassword);

    updateConnectionList();
    serverListBox.setSelectedIndex( idx );
    updateButtonStatus();

  }
  
  /**
   * Creates a temporary BI server connection object and tests it
   */
  public void testServerConnection() {
    
    BiServerConnection tmpServerConnection = new BiServerConnection();
    tmpServerConnection.setName( serverName );
    tmpServerConnection.setUrl( url );
    tmpServerConnection.setUserId(userId);
    tmpServerConnection.setPassword(password);
    tmpServerConnection.setPublishPassword(publishPassword);

    XulDialogBiServerConfig.testServerConnection(tmpServerConnection);
    
  }

  public void serverClick() {
    
    int idx = serverListBox.getSelectedIndex();
    if( idx == -1 ) {
      return;
    }
    BiServerConnection connection = biServerConnections.get( idx );
    if( connection == null ) {
      return;
    }

    serverName = connection.getName();
    url = connection.getUrl();
    userId = connection.getUserId();
    password = connection.getPassword();
    publishPassword = connection.getPublishPassword();
    updateSettings();
    updateButtonStatus();
  }
  
  private void updateSettings() {
    try {
      nameBinding.fireSourceChanged();
      urlBinding.fireSourceChanged();
      userIdBinding.fireSourceChanged();
      passwordBinding.fireSourceChanged();
      publishPasswordBinding.fireSourceChanged();
    } catch (IllegalArgumentException e1) {
      e1.printStackTrace();
    } catch (XulException e1) {
      e1.printStackTrace();
    } catch (InvocationTargetException e1) {
      e1.printStackTrace();
    }
  }
  
  /**
   * Updates the status of the buttons. The remove, edit, and test buttons
   * are only enabled if a BI server connection is selected in the list box
   */
  public void updateButtonStatus() {
    int idx = serverListBox.getSelectedIndex();
    removeButton.setDisabled( idx == -1 );
    testButton.setDisabled( StringUtil.isEmpty( url ) );
    saveButton.setDisabled( StringUtil.isEmpty( url ) );
    if( idx == -1 ) {
      removeButton.setImage( "images/remove_disabled.png" ); //$NON-NLS-1$
    } else {
      removeButton.setImage( "images/remove.png" ); //$NON-NLS-1$
    }
  }
  
  public void okClick() {
    
    biServerConfig.save();
    onDialogAccept();
  }

  public void cancelClick() {
    // perform an undo by reloading from the properties file
    biServerConfig.refreshServerList();
    onDialogCancel();
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
    updateButtonStatus();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPublishPassword() {
    return publishPassword;
  }

  public void setPublishPassword(String publishPassword) {
    this.publishPassword = publishPassword;
  }

}
