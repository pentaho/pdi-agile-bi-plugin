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
package org.pentaho.agilebi.pdi.modeler.propforms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import org.pentaho.agilebi.pdi.modeler.nodes.MeasureMetaData;
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
    if(t == null){
      return;
    }
    
    t.addPropertyChangeListener("valid", propListener);
    t.addPropertyChangeListener("logicalColumn", propListener);
    
    setDisplayName(t.getName());
    setFormat(t.getFormat());
    setAggTypes(t.getAggTypeDescValues());
    setAggTypeDesc(t.getAggTypeDesc());
    setValidMessages(t.getValidationMessagesString());
    setNotValid(!t.isValid());
    setColumnName(t.getLogicalColumn());
    this.fieldMeta = t;
  }
  
  public void setColumnName(LogicalColumn col){
    String prevName = this.colName;
    this.colName = (col != null && col.getPhysicalColumn() != null) ? col.getPhysicalColumn().getName(LanguageChoice.getInstance().getDefaultLocale().toString()) : ""; //$NON-NLS-1$
    this.firePropertyChange("columnName", prevName, this.colName); //$NON-NLS-1$
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
    if(fieldMeta != null){
      fieldMeta.setName(displayName);
    }
    this.firePropertyChange("displayName", null, displayName);

  }

  public String getFormat() {
    if (fieldMeta == null) {
      return null;
    }
    return fieldMeta.getFormat();
  }

  public void setFormat(String format) {

    if(fieldMeta != null){
      fieldMeta.setFormat(format);
    }
    this.firePropertyChange("format", null, format);
  }

  public String getAggTypeDesc() {
    if (fieldMeta == null) {
      return null;
    }
    return fieldMeta.getAggTypeDesc();
  }

  public void setAggTypeDesc(String aggTypeDesc) {
    String prevVal = null;

    if(fieldMeta != null){
      fieldMeta.getAggTypeDesc();
      fieldMeta.setAggTypeDesc(aggTypeDesc);
    }
    this.firePropertyChange("aggTypeDesc", prevVal, aggTypeDesc);
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
