package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class PropertiesForm extends AbstractXulEventHandler{

  private XulDeck propertiesDeck;
  private BindingFactory bf;
  private FieldMetaData fieldMeta;
  
  
  public void init(){
    bf = new DefaultBindingFactory();
    bf.setDocument(document);
    
    propertiesDeck = (XulDeck) document.getElementById("propertiesdeck");
    
    bf.createBinding(this, "displayName", "displayname", "value");
    bf.createBinding(this, "aggTypeDesc", "aggregationtype", "selectedItem");
    bf.createBinding(this, "format", "formatstring", "selectedItem", new FormatStringConverter());
  }
  
  
  public void setSelectedItem(Object selection){

    if (selection != null && selection instanceof FieldMetaData) {
      propertiesDeck.setSelectedIndex(1);
      setFieldMetaData((FieldMetaData) selection);
    } else {
      propertiesDeck.setSelectedIndex(0);
    }
  }
  
  private void setFieldMetaData(FieldMetaData meta){
    this.fieldMeta = meta;
    setDisplayName(fieldMeta.getDisplayName());
    setFormat(fieldMeta.getFormat());
    setAggTypeDesc(fieldMeta.getAggTypeDesc());
  }
  
  @Override
  public String getName() {
    return "propertiesForm";
  }

  
  
  public String getDisplayName() {
    if(fieldMeta == null){
      return null;
    }
    return fieldMeta.getDisplayName();
  }


  public void setDisplayName(String displayName) {
    String prevVal = fieldMeta.getDisplayName();
    fieldMeta.setDisplayName(displayName);
    this.firePropertyChange("displayName", prevVal, displayName);
    
  }


  public String getFormat() {
    if(fieldMeta == null){
      return null;
    }
    return fieldMeta.getFormat();
  }


  public void setFormat(String format) {
    String prevVal = fieldMeta.getFormat();
    fieldMeta.setFormat(format);
    this.firePropertyChange("format", prevVal, format);
  }


  public String getAggTypeDesc() {
    if(fieldMeta == null){
      return null;
    }
    return fieldMeta.getAggTypeDesc();
  }


  public void setAggTypeDesc(String aggTypeDesc) {
    String prevVal = fieldMeta.getAggTypeDesc();
    fieldMeta.setAggTypeDesc(aggTypeDesc);
    this.firePropertyChange("aggTypeDesc", prevVal, aggTypeDesc);    
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
