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

import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;
import org.pentaho.ui.xul.impl.XulPerspectiveImpl;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class XulUI implements TabItemInterface {

  private XulDomContainer container;

  private XulRunner runner;
  ModelerController controller;
  EngineMetaInterface meta;

  private static Log logger = LogFactory.getLog(XulUI.class);
  
  public XulUI( Shell shell,  ModelerWorkspace model) throws ModelerException{
    try{
      SwtXulLoader loader = new SwtXulLoader();
      loader.setOuterContext(shell);
      container = loader.loadXul("org/pentaho/agilebi/pdi/modeler/res/panel.xul", new PDIMessages()); //$NON-NLS-1$

      
      controller = new ModelerController(model);
      this.meta = new ModelerEngineMeta(controller);
      
      container.addEventHandler(controller);
      
      AbstractModelerNodeForm propController = new MeasuresPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.init();
      
      propController = new DimensionPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.init();
      
      propController = new LevelsPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.init();
      
      propController = new MainModelerNodePropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.init();
      
      ColResolverController colController = new ColResolverController();
      container.addEventHandler(colController);
      controller.setColResolver(colController);
      
      runner = new SwtXulRunner();
      runner.addContainer(container);
      runner.initialize();
      container.loadPerspective("ov1");
    } catch(Exception e){
      logger.info(e);
      throw new ModelerException(e);
    }
  }
  
  public Composite getMainPanel(){
    return (Composite) container.getDocumentRoot().getRootElement().getElementById("mainVBox").getManagedObject(); //$NON-NLS-1$
  }
  
  public ModelerController getController(){
    return controller;
  }
  
  public void startDebugWindow(){
    try {
      runner.start();
    } catch (XulException e) {
      e.printStackTrace();
    }
  }
  
  public XulDomContainer getContainer() {
    return container;
  }
  
  public Menu getMenuBar(){
    return (Menu) container.getDocumentRoot().getRootElement().getElementById("mainMenu").getManagedObject();
  }

  public boolean canBeClosed() {
    return true;
  }

  public boolean canHandleSave() {
    return true;
  }
  
  public EngineMetaInterface getMeta() {
    return meta;
  }
  
  public boolean hasContentChanged() {
    return true;
  }
  
  public boolean applyChanges() {
    // TODO Auto-generated method stub
    return false;
  }
  
  public Object getManagedObject() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean setFocus() {
    // TODO Auto-generated method stub
    return false;
  }

  public int showChangedWarning() {
    // TODO Auto-generated method stub
    return 0;
  }
  
}
