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

import org.pentaho.agilebi.pdi.modeler.nodes.HierarchyMetaData;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulVbox;

public class HierarchyPropertiesForm extends AbstractModelerNodeForm<HierarchyMetaData>{

  private XulTextbox name;
  private HierarchyMetaData dim;
  private XulVbox messageBox;
  private XulLabel messageLabel;
  
  private PropertyChangeListener nameListener = new PropertyChangeListener(){
    public void propertyChange(PropertyChangeEvent arg0) {
      setName(dim.getName());
    }
  };
  
  public HierarchyPropertiesForm(){
    super("hierarchyprops");
  }
  
  public void setObject(HierarchyMetaData dim) {
    if(this.dim != null){
      this.dim.removePropertyChangeListener(nameListener);
    }
    this.dim = dim;
    if(dim == null){
      return;
    }
    this.dim.addPropertyChangeListener("name", nameListener);
    name.setValue(dim.getName());
    messageLabel.setValue(dim.getValidationMessagesString());
    messageBox.setVisible(dim.getValidationMessages().size() > 0);
  }

  public void init() {
    super.init();
    name = (XulTextbox) document.getElementById("hierarchy_name");
    messageBox = (XulVbox) document.getElementById("hierarchy_message");
    messageLabel = (XulLabel) document.getElementById("hierarchy_message_label");
    bf.createBinding(this, "name", name, "value");
    
  }
  
  public void setName(String name){
    if(dim != null){
      dim.setName(name);
    }
    this.name.setValue(name);
  }
  
  public String getName(){
    if(dim == null){
      return null;
    }
    return dim.getName();
  }
  
  
}
