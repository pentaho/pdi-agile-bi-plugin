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
import java.util.Locale;

import org.pentaho.agilebi.pdi.modeler.nodes.LevelMetaData;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulHbox;
import org.pentaho.ui.xul.containers.XulVbox;

public class LevelsPropertiesForm extends AbstractModelerNodeForm<LevelMetaData> {

  private XulTextbox name;
  private XulLabel sourceLabel;
  private XulLabel level_message_label;
  private XulVbox messageBox;
  private String colName;
  

  private PropertyChangeListener validListener = new PropertyChangeListener(){

    public void propertyChange(PropertyChangeEvent arg0) {
      setObject(dim);
    }
  };
  
  private LevelMetaData dim;
  private PropertyChangeListener nameListener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent arg0) {
      setName(dim.getName());
    }
  };

  public LevelsPropertiesForm() {
    super("levelprops");
  }

  public void setObject(LevelMetaData dim) {
    if (this.dim != null) {
      this.dim.removePropertyChangeListener(nameListener);
      this.dim.removePropertyChangeListener(validListener);
    }
    
    this.dim = dim;
    if(dim == null){
      return;
    }
    this.dim.addPropertyChangeListener("valid", validListener);
    this.dim.addPropertyChangeListener("logicalColumn", validListener);
    
    this.dim.addPropertyChangeListener("name", nameListener);

    
    name.setValue(dim.getName());
    messageBox.setVisible(dim.getValidationMessages().size() > 0);
    setColumnName(dim.getLogicalColumn());
    
    level_message_label.setValue(dim.getValidationMessagesString());
    setNotValid(!dim.isValid());
  }

  public void init() {
    super.init();
    bf.createBinding(this, "notValid", "level_message", "visible");
    name = (XulTextbox) document.getElementById("level_name");
    sourceLabel = (XulLabel) document.getElementById("level_source_col");
    level_message_label = (XulLabel) document.getElementById("level_message_label");
    messageBox = (XulVbox) document.getElementById("level_message");
    bf.createBinding(this, "notValid", "fixLevelColumnsBtn", "visible");
    
    bf.createBinding(this, "columnName", sourceLabel, "value");
    
    
    
    bf.createBinding(this, "name", name, "value");

  }

  public void setColumnName(LogicalColumn col){
    String prevName = this.colName;
    this.colName = (col != null && col.getPhysicalColumn() != null) ? col.getPhysicalColumn().getName(LanguageChoice.getInstance().getDefaultLocale().toString()) : ""; //$NON-NLS-1$
    this.firePropertyChange("columnName", prevName, this.colName); //$NON-NLS-1$
  }
  
  public String getColumnName(){
    return colName;
  }
  
  public void setName(String name) {
    if (dim != null) {
      dim.setName(name);
    }
    this.name.setValue(name);
  }

  public String getName() {
    if (dim == null) {
      return null;
    }
    return dim.getName();
  }

  public boolean isNotValid() {
    if (dim != null) {
      return !dim.isValid();
    } else {
      return false;
    }
  }

  public void setNotValid(boolean notValid) {
    this.firePropertyChange("notValid", null, notValid);
  }

}
