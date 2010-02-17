package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.binding.BindingConvertor;

public class MeasuresPropertiesForm extends AbstractModelerNodeForm<MeasureMetaData> {

  private MeasureMetaData fieldMeta;
  private Vector aggTypes;
  private String colName;

  private PropertyChangeListener propListener = new PropertyChangeListener(){

    public void propertyChange(PropertyChangeEvent arg0) {
      setObject(fieldMeta);
    }
  };
  
  public MeasuresPropertiesForm() {
    super("measuresprops");
  }


  public void init() {
    super.init();
    
    bf.createBinding(this, "notValid", "messages2", "visible");
    bf.createBinding(this, "validMessages", "messages2label", "value");
    bf.createBinding(this, "displayName", "displayname", "value");
    bf.createBinding(this, "aggTypes", "aggregationtype", "elements");
    bf.createBinding(this, "aggTypeDesc", "aggregationtype", "selectedItem");
    bf.createBinding(this, "format", "formatstring", "selectedItem", new FormatStringConverter());
    bf.createBinding(this, "notValid", "fixMeasuresColumnsBtn", "visible");
    bf.createBinding(this, "columnName", "measure_column_name", "value");

  }

  public void setObject(MeasureMetaData t) {
    if(fieldMeta != null){
      fieldMeta.removePropertyChangeListener(propListener);
    }
    this.fieldMeta = t;
    t.addPropertyChangeListener("valid", propListener);
    t.addPropertyChangeListener("logicalColumn", propListener);
    
    setDisplayName(fieldMeta.getName());
    setFormat(fieldMeta.getFormat());
    setAggTypeDesc(fieldMeta.getAggTypeDesc());
    setAggTypes(fieldMeta.getAggTypeDescValues());
    setValidMessages(fieldMeta.getValidationMessagesString());
    setNotValid(!fieldMeta.isValid());
    setColumnName(fieldMeta.getLogicalColumn());
  }
  
  public void setColumnName(LogicalColumn col){
    String prevName = this.colName;
    this.colName = col != null ? col.getName(LanguageChoice.getInstance().getDefaultLocale().getDisplayLanguage()) : "";
    this.firePropertyChange("columnName", prevName, colName);
  }
  
  public String getColumnName(){
    return colName;
  }

  public boolean isNotValid() {
    if (fieldMeta != null) {
      return !fieldMeta.isValid();
    } else {
      return false;
    }
  }

  public void setNotValid(boolean notValid) {
    this.firePropertyChange("notValid", null, notValid);
  }

  public void setValidMessages(String validMessages) {
    this.firePropertyChange("validMessages", null, validMessages);
  }

  public String getValidMessages() {
    if (fieldMeta != null) {
      return fieldMeta.getValidationMessagesString();
    } else {
      return null;
    }
  }

  @Override
  public String getName() {
    return "propertiesForm";
  }

  public String getDisplayName() {
    if (fieldMeta == null) {
      return null;
    }
    return fieldMeta.getName();
  }

  public void setDisplayName(String displayName) {
    fieldMeta.setName(displayName);
    this.firePropertyChange("displayName", null, displayName);

  }

  public String getFormat() {
    if (fieldMeta == null) {
      return null;
    }
    return fieldMeta.getFormat();
  }

  public void setFormat(String format) {
    fieldMeta.setFormat(format);
    this.firePropertyChange("format", null, format);
  }

  public String getAggTypeDesc() {
    if (fieldMeta == null) {
      return null;
    }
    return fieldMeta.getAggTypeDesc();
  }

  public void setAggTypeDesc(String aggTypeDesc) {
    fieldMeta.setAggTypeDesc(aggTypeDesc);
    this.firePropertyChange("aggTypeDesc", null, aggTypeDesc);
  }
  
  

  public Vector getAggTypes() {
    return aggTypes;
  }


  public void setAggTypes(Vector aggTypes) {
    this.aggTypes = aggTypes;
    this.firePropertyChange("aggTypes", null, aggTypes);
  }



  /**
   * @author wseyler
   */
  private static class FormatStringConverter extends BindingConvertor<String, String> {

    @Override
    public String sourceToTarget(String value) {
      if (value == null) {
        return "NONE";
      } else {
        return value;
      }
    }

    @Override
    public String targetToSource(String value) {
      if (value.equalsIgnoreCase("NONE")) {
        return null;
      } else {
        return value;
      }
    }

  }
}
