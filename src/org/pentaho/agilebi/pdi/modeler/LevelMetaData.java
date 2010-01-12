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

import org.apache.commons.lang.StringUtils;
import org.pentaho.metadata.model.LogicalColumn;

public class LevelMetaData extends AbstractMetaDataModelNode<Object> implements Serializable, ColumnBackedNode {

  private static final long serialVersionUID = -8026104295937064671L;
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
    if (!StringUtils.equals(name, this.name)) {
      String oldName = this.name;
      this.name = name;
      this.firePropertyChange("name", oldName, name); //$NON-NLS-1$
      validateNode();
    }
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

  @Override
  public String getValidImage() {
    return "images/sm_level_icon.png";
  }
  
  public LogicalColumn getLogicalColumn() {
    return logicalColumn;
  }

  public void setLogicalColumn(LogicalColumn logicalColumn) {
    this.logicalColumn = logicalColumn;
    validate();
  }
  
  public void setUniqueMembers(Boolean uniqueMembers) {
    this.uniqueMembers = uniqueMembers;
  }
  
  public Boolean isUniqueMembers(){
    return uniqueMembers;
  }

  @Override
  public void validate() {
    valid = true;
    validationMessages.clear();
    // check name
    if (StringUtils.isEmpty(name)) {
      validationMessages.add("Name is empty");
      valid = false;
    }
    if(logicalColumn == null){
      validationMessages.add("Column is missing");
      valid = false;
    }
  }
  
  public boolean isUiExpanded() {
    return true;
  }

  public boolean isEditingDisabled(){
    return false;
  }
  
  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return LevelsPropertiesForm.class;
  }
}