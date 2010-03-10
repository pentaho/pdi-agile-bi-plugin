package org.pentaho.agilebi.pdi.visualizations.prpt;

import java.io.File;
import java.util.Date;

import org.pentaho.agilebi.pdi.HasXulController;
import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryLock;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.ui.xul.components.XulTab;
import org.pentaho.ui.xul.impl.XulEventHandler;

public class PRPTMeta implements EngineMetaInterface, HasXulController {

  PRPTVisualizationController controller;
  XulTab tab;
  
  public PRPTMeta(){
    
  }
  
  public boolean canSave() {
    return true;
  }  
  
  public PRPTMeta(PRPTVisualizationController controller) {
    this.controller = controller;
  }
  
  public void setController(PRPTVisualizationController controller){
    this.controller = controller;
  }

  public void save(String filename) throws ModelerException{
    controller.save(filename);
    
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
    return "prpt";
  }

  public RepositoryDirectory getDirectory() {
    // TODO: evaluate when moving to Repo support
    return new RepositoryDirectory();
  }

  public String getFileType() {
    return getDefaultExtension();
  }

  public String getFilename() {
    return controller.getFileName();
  }

  public String[] getFilterExtensions() {
    return new String[] { getDefaultExtension() };
  }

  public String[] getFilterNames() {
    return new String[] { getDefaultExtension() };
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
    // this uses the file name to determine the tab name
    if (controller.getFileName() == null) {
      return "Untitled";
    } else {
      File f = new File(controller.getFileName());
      String n = f.getName();
      // strip the file name of its extension
      if (n.length() > getDefaultExtension().length() + 1) {
        n = n.substring(0, n.length() - (getDefaultExtension().length() + 1));
      }
      return n;
    }
  }

  public String getXML() throws KettleException {
    // this is handled by the embedded browser
    throw new UnsupportedOperationException();
  }

  public void nameFromFilename() {
    // TODO Auto-generated method stub

  }

  public void saveRep(Repository arg0, ProgressMonitorListener arg1) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void saveSharedObjects() {
    // TODO Auto-generated method stub
  }

  public void setCreatedDate(Date arg0) {
    // TODO Auto-generated method stub

  }

  public void setCreatedUser(String arg0) {
    // TODO Auto-generated method stub

  }

  public void setFilename(String arg0) {
    controller.setFileName(arg0);
  }

  public void setID(long arg0) {
    // TODO Auto-generated method stub

  }

  public void setInternalKettleVariables() {
    // TODO Auto-generated method stub

  }

  public void setModifiedDate(Date arg0) {
    // TODO Auto-generated method stub

  }

  public void setModifiedUser(String arg0) {
    // TODO Auto-generated method stub

  }

  public boolean showReplaceWarning(Repository arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  public XulTab getTab() {
    return tab;
  }

  public void setTab(XulTab tab) {
    this.tab = tab;
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

  public RepositoryDirectory getRepositoryDirectory() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setObjectId(ObjectId id) {
    // TODO Auto-generated method stub
    
  }

  public RepositoryObjectType getRepositoryElementType() {
    // TODO Auto-generated method stub
    return null;
  }

  public XulEventHandler getController() {
    return controller;
  }

  
  
}
