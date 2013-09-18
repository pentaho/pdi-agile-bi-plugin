/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.agilebi.spoon.publish;

import java.util.List;

import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class SolutionObject extends AbstractModelNode<SolutionObject> {

  private static final long serialVersionUID = -2910390314706330209L;

  private String name;

  private RepositoryFileTreeDto reposFileTree;

  //private BiPlatformRepositoryClientNavigationService navigationService;

  public SolutionObject() {

  }

  public SolutionObject(RepositoryFileTreeDto reposFileTree, int maxDepth) {
    this.reposFileTree = reposFileTree;
    this.name = reposFileTree.getFile().getName();
    //.findStringProperty( CmisObject.LOCALIZEDNAME, null );
    //this.navigationService = navigationService;

    try {
      List<RepositoryFileTreeDto> children = reposFileTree.getChildren();
      if (children != null) {
        //navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, reposFileTree.findIdProperty( PropertiesBase.OBJECTID, null ), new TypesOfFileableObjects( TypesOfFileableObjects.FOLDERS ), 1, null, false, false);
        if (maxDepth == -1 || calculateDepth() < maxDepth) {
          for (RepositoryFileTreeDto obj : children) {
            if (obj.getFile().isFolder())
              add(new SolutionObject(obj, maxDepth));
          }
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

  private int calculateDepth() {
    int d = 0;
    SolutionObject parent = this;
    while (parent != null) {
      d++;
      parent = (SolutionObject) this.getParent();
    }
    return d;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public RepositoryFileTreeDto getReposFileTree() {
    return reposFileTree;
  }

  public void setReposFileTree(RepositoryFileTreeDto reposFileTree) {
    this.reposFileTree = reposFileTree;
  }

  public String getImage() {
    if (reposFileTree == null) {
      return null;
    }
    //String type =;
    //.findStringProperty( PropertiesBase.OBJECTTYPEID, null );
    if (reposFileTree.getFile().isFolder()) {
      return "images/sm_folder_icon.png"; //$NON-NLS-1$
    } else {
      return null;
    }
  }

  /**
   * helper method to hide internal structure of object
   * @return
   */
  public String getPath() {
    String ans = null;
    if(reposFileTree != null && reposFileTree.getFile() != null)
      ans = reposFileTree.getFile().getPath();
    return ans;
  }
}
