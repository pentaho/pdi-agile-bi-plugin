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
package org.pentaho.agilebi.pdi.perspective;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.pentaho.agilebi.pdi.modeler.ModelerEngineMeta;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspaceUtil;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class AgileBiModelerPerspectiveController extends AbstractXulEventHandler {

  private Log logger = LogFactory.getLog(AgileBiModelerPerspective.class);
  private XulMenuitem modelPropItem;
  BindingFactory bf = new DefaultBindingFactory();
  
  @Override
  public String getName() {
    return "agileBiPerspective";
  }

  @Override
  public void setXulDomContainer(XulDomContainer xulDomContainer) {
    super.setXulDomContainer(xulDomContainer);
    modelPropItem = (XulMenuitem) document.getElementById("view-modeler-props");
    bf.setDocument(document);
    
  }
  
  public void setPropVisible(boolean vis){
    modelPropItem.setSelected(vis);
  }

  private Binding itemBinding;
  private ModelerEngineMeta meta;
  public void setSelectedModelerMeta(ModelerEngineMeta meta){
    this.meta = meta;
    if(itemBinding != null){
      itemBinding.destroyBindings();
    }
    if(meta != null){
      bf.setBindingType(Binding.Type.ONE_WAY);
      itemBinding = bf.createBinding(meta.getController(), "propVisible", this, "propVisible");
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
    meta.getController().togglePropertiesPanel();
  }
  
  public void exportSchema() {
    try {
      ModelerWorkspace model = this.meta.getController().getModel();
      if (model.isValid()) {
        ModelerWorkspaceUtil.populateDomain(model);
        LogicalModel lModel = model.getDomain().getLogicalModels().get(0);

        FileDialog fileDialog = new FileDialog(Spoon.getInstance().getShell(), SWT.SAVE);
        String[] theExtensions = { "*.xml" };
        fileDialog.setFilterExtensions(theExtensions);
        String theFile = fileDialog.open();
        if(theFile != null) {
          MondrianModelExporter exporter = new MondrianModelExporter(lModel, Locale.getDefault().toString());
          String mondrianSchema = exporter.createMondrianModelXML();
          logger.info(mondrianSchema);
  
          org.dom4j.Document schemaDoc = DocumentHelper.parseText(mondrianSchema);
          byte schemaBytes[] = schemaDoc.asXML().getBytes();
  
          File modelFile = new File(theFile);
          OutputStream out = new FileOutputStream(modelFile);
          out.write(schemaBytes);
          out.flush();
          out.close();
        }
      } else {
        StringBuffer validationErrors = new StringBuffer();
        for (String msg : model.getValidationMessages()) {
          validationErrors.append(msg);
          validationErrors.append("\n");
          logger.info(msg);
        }
        MessageDialog.openError(Spoon.getInstance().getShell(), "", validationErrors.toString());
      }
    } catch (Exception e) {
      logger.error(e);
      MessageDialog.openError(Spoon.getInstance().getShell(), "", e.getMessage());
    }
  }
}
