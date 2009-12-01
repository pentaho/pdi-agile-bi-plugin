package org.pentaho.agilebi.pdi.visualizations.web;

import java.io.File;
import java.util.Date;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;

public class WebVisualizationMeta implements EngineMetaInterface {

  WebVisualizationBrowser browser;
  
  public WebVisualizationMeta(WebVisualizationBrowser browser) {
    this.browser = browser;
  }
  
  public void save(String filename) {
    this.browser.save(filename);
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
    return browser.getVisualization().getExtension();
  }

  public RepositoryDirectory getDirectory() {
 // TODO: evaluate when moving to Repo support
    return new RepositoryDirectory();
  }

  public String getFileType() {
    return getDefaultExtension();
  }

  public String getFilename() {
    return browser.getVisFileLocation();
  }

  public String[] getFilterExtensions() {
    return new String[]{getDefaultExtension()};
  }

  public String[] getFilterNames() {
    return new String[]{browser.getVisualization().getTitle()};
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
    if (browser.getVisFileLocation() == null) {
      return "Untitled";
    } else {
      File f = new File(browser.getVisFileLocation());
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

  public boolean saveSharedObjects() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setCreatedDate(Date arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setCreatedUser(String arg0) {
    // TODO Auto-generated method stub
    
  }

  public void setFilename(String arg0) {
    // TODO Auto-generated method stub
    
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

}
