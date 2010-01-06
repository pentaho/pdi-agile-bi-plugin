package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.PropertiesBase;

public class NamedCmisObject extends NamedObject {
  
  private String name;
  
  private CmisObject cmisObject;
  
  public NamedCmisObject( CmisObject cmisObject ) {
    this.cmisObject = cmisObject;
    this.name = cmisObject.findStringProperty( CmisObject.LOCALIZEDNAME );
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
    
    String type = cmisObject.findStringProperty( PropertiesBase.OBJECTTYPEID );
    if( CmisObject.OBJECT_TYPE_FOLDER.equals( type ) ) {
      return "images/sm_folder_icon.png"; //$NON-NLS-1$
    } else {
      return null;
    }
    
  }
  
}