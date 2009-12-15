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
import org.pentaho.ui.xul.XulEventSourceAdapter;

/**
 * @author wseyler
 *
 */
public class FieldMetaData extends XulEventSourceAdapter implements Serializable {
  
	String rowNum;
  String fieldName;
  String format;
  String displayName;
  String fieldTypeDesc = "---";
  String levelTypeDesc = "---";
  String aggTypeDesc;
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
  
  public FieldMetaData(String rowNum, String fieldName, String format, String displayName) {
    super();
    this.rowNum = rowNum;
    this.fieldName = fieldName;
    this.format = format;
    this.displayName = displayName;
  }

  public String getRowNum() {
    return rowNum;
  }

  public void setRowNum(String rowNum) {
    this.rowNum = rowNum;
  }

  public String toString() {
    return fieldName;
  }
  
  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFormat() {
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
    if(aggTypeDesc == null){
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
  public boolean equals(Object o){
    if(o == null || o instanceof FieldMetaData == false){
      return false;
    }
    FieldMetaData f = (FieldMetaData) o;
    if(f.getLogicalColumn().getId().equals(this.getLogicalColumn().getId())){
      return true;
    }
    return false;
  }

  public boolean isUiExpanded(){
    return true;
  }
}