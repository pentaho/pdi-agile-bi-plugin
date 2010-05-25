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
