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
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.platform.util.client.PublisherUtil;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulTextbox;

/**
 * A dialog for administering a connections to a BI server
 * @author jamesdixon
 *
 */
public class XulDialogBiServerConfig extends AbstractSwtXulDialogController {

  private static Log logger = LogFactory.getLog(XulDialogBiServerConfig.class);
  
  private BindingFactory bf = new DefaultBindingFactory();
    
  private BiServerConnection biServerConnection;
  
  private XulTextbox nameTextBox, urlTextBox, userIdTextBox, passwordTextBox, publishPasswordTextBox;

  private String serverName, url, userId, password, publishPassword;
  
  private Binding nameBinding, urlBinding, userIdBinding, passwordBinding, publishPasswordBinding;

  private XulButton okButton;
  
  private boolean accepted = false;
  
  /**
   * Creates a dialog for editing the settings to connect to the specified BI server
   * @param shell
   * @param biServerConnection
   * @throws XulException
   */
  public XulDialogBiServerConfig( Shell shell, BiServerConnection biServerConnection ) throws XulException {
    super( shell ); 
    
    setName("dialog"); //$NON-NLS-1$

    this.biServerConnection = biServerConnection;

    serverName = biServerConnection.getName();
    url = biServerConnection.getUrl();
    userId = biServerConnection.getUserId();
    password = biServerConnection.getPassword();
    publishPassword = biServerConnection.getPublishPassword();
    
    initDialogController( "org/pentaho/agilebi/pdi/modeler/biserverconfig.xul", "biserverconfig", null, null ); //$NON-NLS-1$ //$NON-NLS-2$
    
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
   * Initializes the dialog. Gets the text boxes and test button. Creates 
   * bindings to the settings. Makes sure the initial state of the Ok button
   * is correct.
   */
  public void init() {

    bf.setDocument(document);

    bf.setBindingType(Type.BI_DIRECTIONAL);

    nameTextBox = (XulTextbox)document.getElementById("name"); //$NON-NLS-1$
    urlTextBox = (XulTextbox)document.getElementById("url"); //$NON-NLS-1$
    userIdTextBox = (XulTextbox)document.getElementById("userid"); //$NON-NLS-1$
    passwordTextBox = (XulTextbox)document.getElementById("password"); //$NON-NLS-1$
    publishPasswordTextBox = (XulTextbox)document.getElementById("publishpassword"); //$NON-NLS-1$
    okButton = (XulButton) document.getElementById("biserverconfig_accept"); //$NON-NLS-1$
    
    nameBinding = bf.createBinding(this, "serverName", nameTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    urlBinding = bf.createBinding(this, "url", urlTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    userIdBinding = bf.createBinding(this, "userId", userIdTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    passwordBinding = bf.createBinding(this, "password", passwordTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    publishPasswordBinding = bf.createBinding(this, "publishPassword", publishPasswordTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$

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
    updateButtonStatus();

  }

  /**
   * Returns the name of the server connection
   * @return
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * Sets the name of the server connection
   * @param serverName
   */
  public void setServerName(String serverName) {
    this.serverName = serverName;
    updateButtonStatus();
  }

  /**
   * Returns the url of the server connection
   * @return
   */
  public String getUrl() {
    return url;
  }
  
  /**
   * Sets the url of the server connection
   * @param url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Returns the user id for the server connection
   * @return
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the user id of the server connection
   * @param userId
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Returns the password for the server connection
   * @return
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password of the server connection
   * @param password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Returns the publish password for the server connection
   * @return
   */
  public String getPublishPassword() {
    return publishPassword;
  }

  /**
   * Sets the publish password of the server connection
   * @param publishPassword
   */
  public void setPublishPassword(String publishPassword) {
    this.publishPassword = publishPassword;
  }  

  /**
   * Updates the BI server connection object with the current values from
   * the text boxes. Accepts the dialog result.
   */
  public void okClick() {
    accepted = true;
    biServerConnection.setName( serverName );
    biServerConnection.setUrl( url );
    biServerConnection.setUserId(userId);
    biServerConnection.setPassword(password);
    biServerConnection.setPublishPassword(publishPassword);
    onDialogAccept();
  }
  
  /**
   * Cancels the dialog. The BI server connection object provided in the
   * constructor is not modified.
   */
  public void cancelClick() {
    accepted = false;
    onDialogCancel();
  }

  /**
   * Returns true if the user clicked on the Ok button, false if they clicked
   * on the Cancel button
   * @return
   */
  public boolean isAccepted() {
    return accepted;
  }
  
  /**
   * Updates the button states based on the current values in the text boxes.
   * - Ok button is disabled if the connection name is empty.
   */
  public void updateButtonStatus() {
    okButton.setDisabled( StringUtils.isEmpty( serverName ) ); 
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
  
  /**
   * Tests a connection to the provided BI server connection object
   * @param serverConnection
   */
  public static void testServerConnection( BiServerConnection biServerConnection ) {
    ModelServerPublish publish = new ModelServerPublish();
    publish.setBiServerConnection(biServerConnection);
    try {
      // try to get a list of database connections
//      publish.listRemoteConnections();
      // now try to publish to the system solution
      File file = new File("plugins/spoon/agile-bi/testfile.txt"); //$NON-NLS-1$
      
      String DEFAULT_PUBLISH_URL = biServerConnection.getUrl()+"/RepositoryFilePublisher"; //$NON-NLS-1$
      File files[] = { file }; 
      int result = PublisherUtil.publish(DEFAULT_PUBLISH_URL, "system/tmp", files, biServerConnection.getPublishPassword(), biServerConnection.getUserId(), biServerConnection.getPassword(), true); //$NON-NLS-1$

      if( result == PublisherUtil.FILE_ADD_SUCCESSFUL ) {
        SpoonFactory.getInstance().messageBox( Messages.getInstance().getString( "XulDialogBiServerConfig.Test.Passed" ),  //$NON-NLS-1$
            Messages.getInstance().getString( "XulDialogBiServerConfig.Test.Title" ), false, Const.INFO);  //$NON-NLS-1$
      }
      else if( result == PublisherUtil.FILE_ADD_INVALID_USER_CREDENTIALS ) {
        SpoonFactory.getInstance().messageBox( Messages.getInstance().getString( "XulDialogBiServerConfig.Test.BadCredential" ),  //$NON-NLS-1$
            Messages.getInstance().getString( "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
      }
      else if( result == PublisherUtil.FILE_ADD_INVALID_PUBLISH_PASSWORD ) {
        SpoonFactory.getInstance().messageBox( Messages.getInstance().getString( "XulDialogBiServerConfig.Test.BadPublishPassword" ),  //$NON-NLS-1$
            Messages.getInstance().getString( "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
      }
      else if( result == PublisherUtil.FILE_ADD_FAILED ) {
        SpoonFactory.getInstance().messageBox( Messages.getInstance().getString( "XulDialogBiServerConfig.Test.UnknownFail" ),  //$NON-NLS-1$
            Messages.getInstance().getString( "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
      }
      
    } catch (Exception e) {
      SpoonFactory.getInstance().messageBox( e.getLocalizedMessage(), Messages.getInstance().getString( "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
    }
  }
  
}
