package org.pentaho.agilebi.pdi.publish;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.TypesOfFileableObjects;
import org.pentaho.platform.util.client.BiPlatformRepositoryClient;
import org.pentaho.platform.util.client.BiPlatformRepositoryClientNavigationService;
import org.pentaho.platform.util.client.ServiceException;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class XulDialogPublishModel extends XulEventSourceAdapter{
  private BiServerConnection selectedConnection;
  private BiServerConnectionCollection serverCollection;
  private String filename;
  private SolutionObject solutions;
  private SolutionObject selectedFolder;
  private int folderTreeDepth = -1;
  private BiPlatformRepositoryClientNavigationService navigationService;
  private boolean publishXmi = true;
  private BiPlatformRepositoryClient client;
  private boolean isValid;
  private boolean connected;
  private String path;
  private String modelName;
  
  public XulDialogPublishModel(BiServerConfig config){
    this.setServerCollection(config.getServerConnections());
  }
  
  public BiServerConnection getSelectedConnection() {
    return selectedConnection;
  }

  public void setSelectedConnection(BiServerConnection selectedConnection) {
    BiServerConnection prevVal = this.selectedConnection;
    this.selectedConnection = selectedConnection;
    if(prevVal != selectedConnection){
      setConnected(false);
    }
    firePropertyChange("selectedConnection", prevVal, this.selectedConnection);

  }

  public BiServerConnectionCollection getServerCollection() {
    return serverCollection;
  }

  public void setServerCollection(BiServerConnectionCollection serverCollection) {
    this.serverCollection = serverCollection;
    firePropertyChange("serverCollection", null, this.serverCollection);
    if(selectedConnection == null && serverCollection != null && serverCollection.size() > 0){
      setSelectedConnection(serverCollection.get(0));
    }
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

  public void createSolutionTree() throws PublishException{
    BiPlatformRepositoryClient client = new BiPlatformRepositoryClient();
    
    client.setServerUri( selectedConnection.getUrl() );
    client.setUserId(selectedConnection.getUserId());
    client.setPassword( selectedConnection.getPassword() );
    try{
      client.connect();
    } catch(Exception e){
      throw new PublishException("Could not connect to the server", e);
    }
    try{
      navigationService = client.getNavigationService();
      List<CmisObject> solutions = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", new TypesOfFileableObjects( TypesOfFileableObjects.FOLDERS ), 1, null, false, false);
      SolutionObject root = new SolutionObject();
      for(CmisObject obj : solutions){
        root.add(new SolutionObject(obj, navigationService, folderTreeDepth));
      }
  
      setSolutions(root);
    } catch(Exception e){
      throw new PublishException("Error building solution document", e);
    }
  }
  
  public BiPlatformRepositoryClientNavigationService getNavigationService(){
    return navigationService;
  }

  public SolutionObject getSelectedFolder() {
    return selectedFolder;
  }

  public void setSelectedFolder(SolutionObject selectedFolder) {
    SolutionObject prevVal = this.selectedFolder;
    this.selectedFolder = selectedFolder;
    if(getNavigationService() != null && getSelectedFolder() != null){
      setPath(getNavigationService().getRepositoryPath(getSelectedFolder().getCmisObject()));
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

  public boolean isPublishXmi(){
    return publishXmi;
  }
  
  public void setPublishXmi(boolean publish){
    this.publishXmi = publish;
  }
  
  private void calculateValidity(){
    this.isValid = StringUtils.isNotEmpty(this.getFilename())
      && this.path != null
      && this.selectedConnection != null;
    firePropertyChange("valid", null, isValid);
  }
  
  public boolean isValid(){
    return isValid;
  }

  public boolean isConnected() {
    return connected;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }
  
  public String getPath(){
    return path;
  }
  public void setPath(String path){
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
