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

import java.io.Serializable;
import java.util.List;

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class LevelMetaData extends AbstractModelNode implements Serializable {
  String name;
  String columnName;
  HierarchyMetaData parent;
  transient LogicalColumn logicalColumn;
  Boolean uniqueMembers = true;
  
  public LevelMetaData(HierarchyMetaData parent, String name) {
    this.parent = parent;
    this.name = name;
    this.columnName = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getColumnName() {
    return columnName;
  }
  
  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }
  
  
  public HierarchyMetaData getParent() {
    return parent;
  }
  
  public void setParent(HierarchyMetaData md){
    this.parent = md;
  }
  
  public String toString() {
    return "Level Name: " + name + "\nColumn Name: " + columnName;
  }

  public String getImage(){
    return "images/sm_level_icon.png";
  }

  public LogicalColumn getLogicalColumn() {
    return logicalColumn;
  }

  public void setLogicalColumn(LogicalColumn logicalColumn) {
    this.logicalColumn = logicalColumn;
  }
  
  public void setUniqueMembers(Boolean uniqueMembers) {
    this.uniqueMembers = uniqueMembers;
  }
  
  public Boolean isUniqueMembers(){
    return uniqueMembers;
  }

  public boolean isUiExpanded(){
    return true;
  }
}