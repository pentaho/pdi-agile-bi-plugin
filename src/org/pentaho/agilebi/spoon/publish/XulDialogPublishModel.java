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
import org.pentaho.platform.util.client.BiPlatformRepositoryClient;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class XulDialogPublishModel extends XulEventSourceAdapter {
  private BiServerConnection selectedConnection;

  private BiServerConnectionCollection serverCollection;

  private String filename;

  private SolutionObject solutions;

  private SolutionObject selectedFolder;

  private int folderTreeDepth = -1;

  //private BiPlatformRepositoryClientNavigationService navigationService;

  private boolean publishXmi = true;

  private BiPlatformRepositoryClient client;

  private boolean isValid;

  private boolean connected;

  private String path;

  private String modelName;
  
  private boolean groupBoxFolderVisible = false;

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

  public SolutionObject getSelectedFolder() {
    return selectedFolder;
  }

  public void setSelectedFolder(SolutionObject selectedFolder) {
    SolutionObject prevVal = this.selectedFolder;
    this.selectedFolder = selectedFolder;
    if (getSelectedFolder() != null) {
      setPath(getSelectedFolder().getName());
    }
    firePropertyChange("selectedFolder", prevVal, this.selectedFolder);
    calculateValidity();
  }

  public int getFolderTreeDepth() {
    return folderTreeDepth;
  }

  public void setFolderTreeDepth(int folderTreeDepth) {
    this.folderTreeDepth = folderTreeDepth;
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

  public boolean isValid() {
    boolean valid = true;
    if(groupBoxFolderVisible){
      valid = StringUtils.isNotEmpty(this.getFilename()) && this.path != null;
    }
    valid = valid && this.selectedConnection != null;
    return valid; 
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

  public boolean isGroupBoxFolderVisible() {
    return groupBoxFolderVisible;
  }

  public void setGroupBoxFolderVisible(boolean visible) {
    this.groupBoxFolderVisible = visible;
    firePropertyChange("groupBoxFolderVisible", null, this.groupBoxFolderVisible);
  }

  
}
