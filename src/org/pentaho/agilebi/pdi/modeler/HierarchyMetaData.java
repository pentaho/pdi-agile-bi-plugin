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

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Event aware node class that also listens to it's children's events and propagates them up.
 */
public class HierarchyMetaData extends AbstractMetaDataModelNode<LevelMetaData> implements Serializable {

  private static final long serialVersionUID = 7063031303948537101L;

  String name;

  public HierarchyMetaData(String name) {
    this.name = name;
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
  
  
  @Override
  public void validate() {
    System.out.println("Validating HierarchyMetaData");
    valid = true;
    validationMessages.clear();
    // check name
    if (StringUtils.isEmpty(name)) {
      validationMessages.add("Name is empty");
      valid = false;
    }
    if (size() == 0) {
      validationMessages.add("Hierarchy must have at least one level");
      valid = false;
    }
    List<String> usedNames = new ArrayList<String>();
    
    if(children.size() == 0){
      valid = false;
      validationMessages.add(BaseMessages.getString(this.getClass(), "missing_level_from_heirarchy"));
    }
    for(LevelMetaData level: children){
      valid &= level.isValid();
      validationMessages.addAll(level.getValidationMessages());
      if(usedNames.contains(level.getName())){
        valid = false;
        validationMessages.add(BaseMessages.getString(this.getClass(), "duplicate_level_names"));
      }
      usedNames.add(level.getName());
    }
  }
  
  public String toString() {
    return "Hierarchy Name: " + name;
  }
  
  @Override
  public String getValidImage() {
    return "images/sm_hierarchy_icon.png"; //$NON-NLS-1$
  }
  
  public boolean isUiExpanded(){
    return true;
  }
  
  public boolean isEditingDisabled(){
    return false;
  }

  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return HierarchyPropertiesForm.class;
  }
  
  @Override
  public void onAdd(LevelMetaData child) {
    child.addPropertyChangeListener("name", nameListener);
    child.addPropertyChangeListener("valid", validListener);
    child.addPropertyChangeListener("children", childrenListener);
    validateNode();
  }

  @Override
  public void onRemove(LevelMetaData child) {
    child.removePropertyChangeListener(validListener);
    child.removePropertyChangeListener(nameListener);
    child.removePropertyChangeListener(childrenListener);
    validateNode();
  }
}