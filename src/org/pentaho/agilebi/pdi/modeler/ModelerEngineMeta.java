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

import java.util.Date;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryLock;
import org.pentaho.di.repository.RepositoryObjectType;

public class ModelerEngineMeta implements EngineMetaInterface {
  
  private ModelerController controller;
  
  public ModelerEngineMeta(ModelerController controller) {
    this.controller = controller;
  }

  public ModelerController getController() {
    return controller;
  }
  
  public void clearChanged() {
    // TODO Auto-generated method stub
    
  }

  public Date getCreatedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getCreatedUser() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getDefaultExtension() {
    return "xmi";
  }

  public RepositoryDirectory getDirectory() {
    // TODO: evaluate when moving to Repo support
    return new RepositoryDirectory();
  }

  public String getFilename() {
    return controller.getModel().getFileName();
  }

  public String getFileType() {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getFilterExtensions() {
    return new String[]{"xmi"};
  }

  public String[] getFilterNames() {
    return new String[]{"Model"};
  }

  public Date getModifiedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getModifiedUser() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getName() {
    return controller.getModel().getFileName() != null  
        ? controller.getModel().getFileName()
        : controller.getModel().getModelName();
  }

  public String getXML() throws KettleException {
    try{
      return ModelerWorkspaceUtil.getMetadataXML(controller.getModel());
    } catch(ModelerException e){
      throw new KettleException(e);
    }
  }

  public void nameFromFilename() {
    // TODO Auto-generated method stub
    
  }

  public void saveRep(Repository rep, ProgressMonitorListener monitor) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  public void saveSharedObjects() {
  }

  public void setCreatedDate(Date date) {
    // TODO Auto-generated method stub
    
  }

  public void setCreatedUser(String createduser) {
    // TODO Auto-generated method stub
    
  }

  public void setFilename(String filename) {
    controller.setFileName(filename);
  }

  public void setID(long id) {
    // TODO Auto-generated method stub
    
  }

  public void setInternalKettleVariables() {
    // TODO Auto-generated method stub
    
  }

  public void setModifiedDate(Date date) {
    // TODO Auto-generated method stub
    
  }

  public void setModifiedUser(String user) {
    // TODO Auto-generated method stub
    
  }

  public boolean showReplaceWarning(Repository rep) {
    // TODO Auto-generated method stub
    return false;
  }

  public RepositoryDirectory getRepositoryDirectory() {
    // TODO Auto-generated method stub
    return null;
  }

  public RepositoryObjectType getRepositoryElementType() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setObjectId(ObjectId id) {
    // TODO Auto-generated method stub
    
  }

  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  public ObjectId getObjectId() {
    // TODO Auto-generated method stub
    return null;
  }

  public ObjectRevision getObjectRevision() {
    // TODO Auto-generated method stub
    return null;
  }

  public RepositoryLock getRepositoryLock() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setDescription(String description) {
    // TODO Auto-generated method stub
    
  }

  public void setName(String name) {
    // TODO Auto-generated method stub
    
  }

  public void setObjectRevision(ObjectRevision objectRevision) {
    // TODO Auto-generated method stub
    
  }

  public void setRepositoryDirectory(RepositoryDirectory repositoryDirectory) {
    // TODO Auto-generated method stub
    
  }
  

}
