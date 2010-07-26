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
package org.pentaho.agilebi.spoon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.agilebi.spoon.PDIMessages;
import org.pentaho.agilebi.modeler.*;
import org.pentaho.agilebi.modeler.propforms.*;
import org.pentaho.agilebi.spoon.modeler.SpoonModelerController;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.ui.spoon.ChangedWarningInterface;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class XulUI implements TabItemInterface {

  private XulDomContainer container;

  private XulRunner runner;
  ModelerController controller;
  EngineMetaInterface meta;

  private static Logger logger = LoggerFactory.getLogger(XulUI.class);
  
  
  public ChangedWarningInterface getChangedWarning() {
    return null;
  }
  
  public XulUI( Shell shell,  ModelerWorkspace model) throws ModelerException {
    try{
      SwtXulLoader loader = new SwtXulLoader();
      loader.registerClassLoader(getClass().getClassLoader());
      loader.setOuterContext(shell);
      container = loader.loadXul("org/pentaho/agilebi/modeler/res/panel.xul", new PDIMessages(ModelerWorkspace.class)); //$NON-NLS-1$

      
      controller = new SpoonModelerController(model);
      this.meta = new ModelerEngineMeta(controller);
      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument(container.getDocumentRoot());
      container.addEventHandler(controller);
      controller.setBindingFactory(bf);
      controller.setWorkspaceHelper(new SpoonModelerWorkspaceHelper());
      
      AbstractModelerNodeForm propController = new MeasuresPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();
      
      propController = new DimensionPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();
      
      propController = new LevelsPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();
      

      propController = new HierarchyPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();
      
      propController = new MainModelerNodePropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();
      

      propController = new GenericPropertiesForm();
      container.addEventHandler(propController);
      controller.addPropertyForm(propController);
      propController.setBindingFactory(bf);
      propController.init();
      
      ColResolverController colController = new ColResolverController();
      container.addEventHandler(colController);
      controller.setColResolver(colController);
      
      runner = new SwtXulRunner();
      runner.addContainer(container);
      runner.initialize();
      container.loadPerspective("ov1");
    } catch(Exception e){
      logger.info("error initializing", e);
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

  public void setControlStates() {
    // TODO Auto-generated method stub
    
  }
  
  
}
