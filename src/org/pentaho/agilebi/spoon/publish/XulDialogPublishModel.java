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
 * Copyright (c) 2010 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.spoon.publish;

import org.apache.commons.lang.StringUtils;
import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.TypesOfFileableObjects;
import org.pentaho.platform.util.client.BiPlatformRepositoryClient;
import org.pentaho.platform.util.client.BiPlatformRepositoryClientNavigationService;
import org.pentaho.ui.xul.XulEventSourceAdapter;

import java.util.List;

public class XulDialogPublishModel extends XulEventSourceAdapter {
  private BiServerConnection selectedConnection;

  private BiServerConnectionCollection serverCollection;

  private String filename;

  private SolutionObject solutions;

  private BiPlatformRepositoryClientNavigationService navigationService;

  private boolean publishXmi = true;
  private BiPlatformRepositoryClient client;
  private boolean isValid;
  private boolean connected;

  private String path;

  private String modelName;

  public XulDialogPublishModel(BiServerConfig config) {
    this.setServerCollection(config.getServerConnections());
  }

  public BiServerConnection getSelectedConnection() {
    return selectedConnection;
  }

  public void setSelectedConnection(BiServerConnection selectedConnection) {
    BiServerConnection prevVal = this.selectedConnection;
    this.selectedConnection = selectedConnection;
    if (prevVal != selectedConnection) {
      setConnected(false);
    }
    firePropertyChange("selectedConnection", prevVal, this.selectedConnection);
    calculateValidity();
  }

  public BiServerConnectionCollection getServerCollection() {
    return serverCollection;
  }

  public void setServerCollection(BiServerConnectionCollection serverCollection) {
    this.serverCollection = serverCollection;
    firePropertyChange("serverCollection", null, this.serverCollection);

    calculateValidity();
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    String prevVal = this.filename;
    this.filename = filename;
    firePropertyChange("filename", prevVal, this.filename);
    calculateValidity();
  }

  public SolutionObject getSolutions() {
    return solutions;
  }

  public void setSolutions(SolutionObject solutions) {
    this.solutions = solutions;
    firePropertyChange("solutions", null, this.solutions);
    calculateValidity();
  }

  public void createSolutionTree() throws PublishException {
    BiPlatformRepositoryClient biPlatformClient = new BiPlatformRepositoryClient();

    biPlatformClient.setServerUri(selectedConnection.getUrl());
    biPlatformClient.setUserId(selectedConnection.getUserId());
    biPlatformClient.setPassword(selectedConnection.getPassword());
    try {
      biPlatformClient.connect();
    } catch (Exception e) {
      throw new PublishException("Could not connect to the server", e);
    }
    try {
      navigationService = biPlatformClient.getNavigationService();
      //List<CmisObject> solutions = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", new TypesOfFileableObjects( TypesOfFileableObjects.FOLDERS ), 1, null, false, false);
      SolutionObject root = new SolutionObject();
     // for (CmisObject obj : solutions) {
      //  root.add(new SolutionObject(obj, navigationService, folderTreeDepth));
     // }

      setSolutions(root);
    } catch (Exception e) {
      throw new PublishException("Error building solution document", e);
    }
  }

  public BiPlatformRepositoryClientNavigationService getNavigationService() {
    return navigationService;
  }

  
  public boolean isPublishXmi() {
    return publishXmi;
  }

  public void setPublishXmi(boolean publish) {
    this.publishXmi = publish;
  }

  private void calculateValidity() {
    firePropertyChange("valid", null, isValid());
  }
  
  public boolean isValid(){
    return StringUtils.isNotEmpty(this.getFilename())     
      && this.selectedConnection != null;
  }

  public boolean isConnected() {
    return connected;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    String prevVal = this.path;
    this.path = path;
    firePropertyChange("path", prevVal, this.path);
    calculateValidity();
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    String prevVal = this.modelName;
    this.modelName = modelName;
    firePropertyChange("modelName", prevVal, this.modelName);
  }

}
