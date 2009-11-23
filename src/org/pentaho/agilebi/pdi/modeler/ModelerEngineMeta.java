package org.pentaho.agilebi.pdi.modeler;

import java.util.Date;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;

public class ModelerEngineMeta implements EngineMetaInterface{
  
  private ModelerController controller;
  
  
  public ModelerEngineMeta(ModelerController controller){
    this.controller = controller;
    
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
    // TODO Auto-generated method stub
    return null;
  }

  public String getFileType() {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getFilterExtensions() {
    return new String[]{"xmi"};
  }

  public String[] getFilterNames() {
    return new String[]{"Metadata"};
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
    return controller.getModel().getModelName();
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

  public boolean saveSharedObjects() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setCreatedDate(Date date) {
    // TODO Auto-generated method stub
    
  }

  public void setCreatedUser(String createduser) {
    // TODO Auto-generated method stub
    
  }

  public void setFilename(String filename) {
    // TODO Auto-generated method stub
    
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
  
  
  

}
