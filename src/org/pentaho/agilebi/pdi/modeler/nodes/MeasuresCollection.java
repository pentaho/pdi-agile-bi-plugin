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
package org.pentaho.agilebi.pdi.modeler.nodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.propforms.GenericPropertiesForm;
import org.pentaho.agilebi.pdi.modeler.propforms.ModelerNodePropertiesForm;
import org.pentaho.di.i18n.BaseMessages;

public class MeasuresCollection extends AbstractMetaDataModelNode<MeasureMetaData>  implements Serializable {
  private String name = BaseMessages.getString(ModelerWorkspace.class, "measures");
  
  public MeasuresCollection(){
    this.valid = false;
  }
  
  public String getName() {
    return name;
  }
  
  public String getDisplayName(){
    return getName();
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public boolean isUiExpanded(){
    return true;
  }

  @Override
  public String getValidImage() {
    return "images/sm_folder_icon.png"; //$NON-NLS-1$
  }

  @Override
  public void validate() {
    boolean prevVal = valid;
    valid = true;
    validationMessages.clear();

    if (size() == 0) {
      validationMessages.add(BaseMessages.getString(ModelerWorkspace.class, "need_one_measure"));
      valid = false;
    }
    List<String> usedNames = new ArrayList<String>();
    for(MeasureMetaData measure : children){
      valid &= measure.isValid();
      validationMessages.addAll(measure.getValidationMessages());
      if(usedNames.contains(measure.getName())){
        valid = false;
        validationMessages.add(BaseMessages.getString(ModelerWorkspace.class, "duplicate_measure_name"));
      }
      
      usedNames.add(measure.getName());
      
    }
    this.firePropertyChange("valid", prevVal, valid);
  }

  @Override
  public void onAdd(MeasureMetaData child) {
    child.setParent(this);
    child.addPropertyChangeListener("name", nameListener);
    child.addPropertyChangeListener("valid", validListener);
    child.addPropertyChangeListener("children", childrenListener);
    validateNode();
  }
  
  public void onRemove(MeasureMetaData child){
    child.removePropertyChangeListener(validListener);
    child.removePropertyChangeListener(nameListener);
    child.removePropertyChangeListener(childrenListener);
    validateNode();
  }
  

  
  public boolean isEditingDisabled(){
    return true;
  }
  
  @Override
  public Class<? extends ModelerNodePropertiesForm> getPropertiesForm() {
    return GenericPropertiesForm.class;
  }
}