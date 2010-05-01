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
package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.PropertiesBase;

public class NamedCmisObject extends NamedObject {
  
  private String name;
  
  private CmisObject cmisObject;
  
  public NamedCmisObject( CmisObject cmisObject ) {
    this.cmisObject = cmisObject;
    this.name = cmisObject.findStringProperty( CmisObject.LOCALIZEDNAME, null );
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
    
    String type = cmisObject.findStringProperty( PropertiesBase.OBJECTTYPEID, null );
    if( CmisObject.OBJECT_TYPE_FOLDER.equals( type ) ) {
      return "images/sm_folder_icon.png"; //$NON-NLS-1$
    } else {
      return null;
    }
    
  }
  
}