package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class MeasuresPropertiesForm extends AbstractModelerNodeForm<MeasureMetaData> {

  private MeasureMetaData fieldMeta;

  public MeasuresPropertiesForm() {
    super("measuresprops");
  }


  public void init() {
    super.init();
    
    bf.createBinding(this, "notValid", "messages2", "visible");
    bf.createBinding(this, "validMessages", "messages2label", "value");
    bf.createBinding(this, "displayName", "displayname", "value");
    bf.createBinding(this, "aggTypeDesc", "aggregationtype", "selectedItem");
    bf.createBinding(this, "format", "formatstring", "selectedItem", new FormatStringConverter());

  }

  public void setObject(MeasureMetaData t) {
    this.fieldMeta = t;
    setDisplayName(fieldMeta.getDisplayName());
    setFormat(fieldMeta.getFormat());
    setAggTypeDesc(fieldMeta.getAggTypeDesc());
    setNotValid(!fieldMeta.isValid());
    setValidMessages(fieldMeta.getValidationMessagesString());
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
    return fieldMeta.getDisplayName();
  }

  public void setDisplayName(String displayName) {
    fieldMeta.setDisplayName(displayName);
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
