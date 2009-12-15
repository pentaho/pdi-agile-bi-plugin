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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.List;

import org.pentaho.ui.xul.util.AbstractModelNode;

/**
 * Event aware node class that also listens to it's children's events and propagates them up.
 */
public class HierarchyMetaData extends AbstractModelNode<LevelMetaData> implements Serializable {
  private transient PropertyChangeListener listener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent evt) {
      fireCollectionChanged();
    }
  };

  String name;

  public HierarchyMetaData(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public List<LevelMetaData> getChildren() {
    return children;
  }
  
  
  public String toString() {
    return "Hierarchy Name: " + name;
  }
  
  public String getImage(){
    return "images/hieraracy.png";
  }
  
  
  @Override
  public void onAdd(LevelMetaData child) {
    child.addPropertyChangeListener("children", listener);
  }

  @Override
  public void onRemove(LevelMetaData child) {
    child.removePropertyChangeListener(listener);
  }
  
}