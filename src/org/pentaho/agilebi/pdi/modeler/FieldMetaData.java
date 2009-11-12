package org.pentaho.agilebi.pdi.modeler;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.ui.xul.XulEventSourceAdapter;

/**
 * @author wseyler
 *
 */
public class FieldMetaData extends XulEventSourceAdapter {
  String rowNum;
  String fieldName;
  String format;
  String displayName;
  String fieldTypeDesc = "---";
  String levelTypeDesc = "---";
  String aggTypeDesc;
  LogicalColumn logicalColumn;
  private List<String> aggTypes = new ArrayList<String>();
  {
    aggTypes.add("NONE");
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
  
}