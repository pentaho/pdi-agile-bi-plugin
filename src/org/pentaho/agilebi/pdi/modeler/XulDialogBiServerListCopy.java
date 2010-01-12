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
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulListbox;

/**
 * A dialog for administering a list of connections to BI servers
 * @author jamesdixon
 *
 */
public class XulDialogBiServerListCopy extends AbstractSwtXulDialogController {

  private static Log logger = LogFactory.getLog(XulDialogBiServerListCopy.class);
  
  private BindingFactory bf = new DefaultBindingFactory();
  
  private Binding serverNamesBinding;
  
  private XulListbox serverListBox;
  
  private XulButton testButton, removeButton, editButton;
  
  private BiServerConnectionCollection biServerConnections;
  
  private BiServerConfig biServerConfig;
  
  /**
   * Creates a new dialog. Gets the current list of BI server connections from the properties file
   * @param shell
   * @throws XulException
   */
  public XulDialogBiServerListCopy( Shell shell ) throws XulException {
    super( shell ); 
    
    setName("dialog"); //$NON-NLS-1$

    biServerConfig = BiServerConfig.getInstance();
    
    biServerConnections = biServerConfig.getServerConnections();
    
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

    bf.setDocument(document);

    bf.setBindingType(Type.ONE_WAY);

    serverListBox = (XulListbox)document.getElementById("serverListBox"); //$NON-NLS-1$

    testButton = (XulButton)document.getElementById("testButton"); //$NON-NLS-1$
    removeButton = (XulButton)document.getElementById("removeButton"); //$NON-NLS-1$
    editButton = (XulButton)document.getElementById("editButton"); //$NON-NLS-1$
    
    serverNamesBinding = bf.createBinding(this, "BiServerConnections", serverListBox, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
    
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

    BiServerConnection connection = new BiServerConnection();
    
    try {
      XulDialogBiServerConfig serverConfigDialog = new XulDialogBiServerConfig( getShell(), connection );
      serverConfigDialog.showDialog();
      if(serverConfigDialog.isAccepted()) {
        biServerConnections.add( connection );
        updateConnectionList();
        serverListBox.setSelectedIndex( biServerConnections.size()-1 );
        updateButtonStatus();
        biServerConfig.save();
      }
    } catch (XulException e) {
      e.printStackTrace();
    }
    
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
    updateConnectionList();
    updateButtonStatus();
    biServerConfig.save();
  }
  
   /**
    * Edits a BI server connection. Opens the XulDialogBiServerConfig dialog.
    * If the user clicks ok the listbox and buttons are refreshed. The list 
    * is flushed to the properties file.
    */
  public void editBiServer() {
    int idx = serverListBox.getSelectedIndex();
    if( idx == -1 ) {
      return;
    }
    BiServerConnection connection = biServerConnections.get( idx );
    if( connection == null ) {
      return;
    }
    try {
      XulDialogBiServerConfig serverConfigDialog = new XulDialogBiServerConfig( getShell(), connection );
      serverConfigDialog.showDialog();
      if(serverConfigDialog.isAccepted()) {
        // maybe the name changed
        updateConnectionList();
        serverListBox.setSelectedIndex( idx );
        updateButtonStatus();
        biServerConfig.save();
      }
    } catch (XulException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Tests a selected connection.
   */
  public void testServerConnection() {
    
    int idx = serverListBox.getSelectedIndex();
    if( idx == -1 ) {
      return;
    }
    BiServerConnection connection = biServerConnections.get( idx );
    if( connection == null ) {
      return;
    }

    XulDialogBiServerConfig.testServerConnection(connection);
    
  }

  public void serverClick() {
    updateButtonStatus();
  }
  
  /**
   * Updates the status of the buttons. The remove, edit, and test buttons
   * are only enabled if a BI server connection is selected in the list box
   */
  public void updateButtonStatus() {
    int idx = serverListBox.getSelectedIndex();
    editButton.setDisabled( idx == -1 );
    removeButton.setDisabled( idx == -1 );
    testButton.setDisabled( idx == -1 );
    if( idx == -1 ) {
      editButton.setImage( "images/edit_disabled.png" ); //$NON-NLS-1$
      removeButton.setImage( "images/remove_disabled.png" ); //$NON-NLS-1$
    } else {
      editButton.setImage( "images/edit.png" ); //$NON-NLS-1$
      removeButton.setImage( "images/remove.png" ); //$NON-NLS-1$
    }
  }
  
}
