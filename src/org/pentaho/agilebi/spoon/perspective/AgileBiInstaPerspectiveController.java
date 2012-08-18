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
package org.pentaho.agilebi.spoon.perspective;

import org.pentaho.agilebi.spoon.HasXulController;
import org.pentaho.agilebi.spoon.visualizations.PropertyPanelController;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class AgileBiInstaPerspectiveController extends AbstractXulEventHandler {

  private XulMenuitem modelPropItem;
  private BindingFactory bf = new DefaultBindingFactory();
  private Binding itemBinding;
  private HasXulController meta;
  private boolean dirty = true;
  
  @Override
  public String getName() {
    return "agileBiVisPerspective"; //$NON-NLS-1$
  }

  @Override
  public void setXulDomContainer(XulDomContainer xulDomContainer) {
    super.setXulDomContainer(xulDomContainer);
    modelPropItem = (XulMenuitem) document.getElementById("view-vis-props"); //$NON-NLS-1$
    bf.setDocument(document);
    
  }
  
  public void setPropVisible(boolean vis){
    modelPropItem.setSelected(vis);
  }

  public void setSelectedModelerMeta(EngineMetaInterface meta){
    this.meta = (HasXulController) meta;
    if(itemBinding != null){
      itemBinding.destroyBindings();
    }
    if(meta != null){
      bf.setBindingType(Binding.Type.ONE_WAY);
      itemBinding = bf.createBinding(this.meta.getController(), "propVisible", this, "propVisible"); //$NON-NLS-1$ //$NON-NLS-2$
      try {
        itemBinding.fireSourceChanged();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
  }

  public void toggleProperties(){
    if(meta == null){
      return;
    }
    ((PropertyPanelController) meta.getController()).togglePropertiesPanel();
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }
  
  
  
  
}
