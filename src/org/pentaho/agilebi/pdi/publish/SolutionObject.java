package org.pentaho.agilebi.pdi.publish;

import java.util.List;

import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.PropertiesBase;
import org.pentaho.commons.util.repository.type.TypesOfFileableObjects;
import org.pentaho.platform.util.client.BiPlatformRepositoryClient;
import org.pentaho.platform.util.client.BiPlatformRepositoryClientNavigationService;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class SolutionObject extends AbstractModelNode<SolutionObject>{

  private String name;
  
  private CmisObject cmisObject;
  
  private BiPlatformRepositoryClientNavigationService navigationService;
  
  public SolutionObject(){
    
  }
  
  public SolutionObject( CmisObject cmisObject, BiPlatformRepositoryClientNavigationService navigationService, int maxDepth  ) {
    this.cmisObject = cmisObject;
    this.name = cmisObject.findStringProperty( CmisObject.LOCALIZEDNAME, null );
    this.navigationService = navigationService;
    
    try {
      List<CmisObject> children = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, cmisObject.findIdProperty( PropertiesBase.OBJECTID, null ), new TypesOfFileableObjects( TypesOfFileableObjects.FOLDERS ), 1, null, false, false);
      if(maxDepth == -1 ||calculateDepth() < maxDepth){
        for(CmisObject obj : children){
          add(new SolutionObject(obj, navigationService, maxDepth));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  
  
  @Override
  public boolean add(SolutionObject child) {
    child.setParent(this);
    return super.add(child);
  }

  private int calculateDepth(){
    int d = 0;
    SolutionObject parent = this;
    while(parent != null){
      d++;
      parent = (SolutionObject) this.getParent();
    }
    return d;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName( String name ) {
    this.name = name;
  }

  public CmisObject getCmisObject() {
    return cmisObject;
  }

  public void setCmisObject(CmisObject cmisObject) {
    this.cmisObject = cmisObject;
  }
  
  public String getImage() {
    if(cmisObject == null){
      return null;
    }
    String type = cmisObject.findStringProperty( PropertiesBase.OBJECTTYPEID, null );
    if( CmisObject.OBJECT_TYPE_FOLDER.equals( type ) ) {
      return "images/sm_folder_icon.png"; //$NON-NLS-1$
    } else {
      return null;
    }
  }
}
