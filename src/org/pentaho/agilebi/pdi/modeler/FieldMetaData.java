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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.reporting.libraries.base.util.StringUtils;

/**
 * @author wseyler
 *
 */
public class FieldMetaData extends AbstractMetaDataModelNode implements Serializable {
  
  String name;
  String format = "NONE";
  String displayName;
  String fieldTypeDesc = "---";
  String levelTypeDesc = "---";
  String aggTypeDesc = "SUM";
  transient LogicalColumn logicalColumn;
  
  private List<String> aggTypes = new ArrayList<String>();
  {
    aggTypes.add("SUM");
    aggTypes.add("AVERAGE");
    aggTypes.add("MINIMUM");
    aggTypes.add("MAXIMUM");
  }
  
  public FieldMetaData(){
    
  }
  
  public FieldMetaData(String fieldName, String format, String displayName) {
    super();
    this.name = fieldName;
    this.format = format;
    this.displayName = displayName;
  }

  public String toString() {
    return name;
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

  public String getFormat() {
    if(StringUtils.isEmpty(format)){
      return "NONE";
    }
    return format;
  }

  public void setFormat(String format) {
    
    this.format = format;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getFieldTypeDesc() {
    return fieldTypeDesc;
  }

  public void setFieldTypeDesc(String fieldTypeDesc) {
    this.fieldTypeDesc = fieldTypeDesc;
  }

  public String getLevelTypeDesc() {
    return levelTypeDesc;
  }

  public void setLevelTypeDesc(String levelTypeDesc) {
    this.levelTypeDesc = levelTypeDesc;
  }

  public String getAggTypeDesc() {
    if(StringUtils.isEmpty(aggTypeDesc)){
      aggTypeDesc = aggTypes.get(0);
    }
    return aggTypeDesc;
  }

  public void setAggTypeDesc(String aggTypeDesc) {
    this.aggTypeDesc = aggTypeDesc;
  }
  
  // TODO: generate this based on field type
  public Vector getAggTypeDescValues() {
    return new Vector<String>(aggTypes);
  }
  
  public LogicalColumn getLogicalColumn(){
    return logicalColumn;
  }
  public void setLogicalColumn(LogicalColumn col){
    this.logicalColumn = col;
  }
  
  @Override
  public boolean equals(Object o) {
    if(o == null || o instanceof FieldMetaData == false){
      return false;
    }
    FieldMetaData f = (FieldMetaData) o;
    if(f.getLogicalColumn().getId().equals(this.getLogicalColumn().getId())){
      return true;
    }
    return false;
  }

  public List getChildren() {
    return null;
  }
  
  public void setUiExpanded() {
    
  }
  
  public boolean isUiExpanded() {
    return true;
  }

  @Override
  public String getValidImage() {
    return "images/sm_level_icon.png"; //$NON-NLS-1$
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
  }
  
  public boolean isEditingDisabled(){
    return false;
  }
}