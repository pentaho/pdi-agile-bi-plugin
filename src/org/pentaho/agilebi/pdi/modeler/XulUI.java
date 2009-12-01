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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class XulUI {

  private XulDomContainer container;

  XulRunner runner;
  BindingFactory bf;

  private static Log logger = LogFactory.getLog(XulUI.class);
  
  public XulUI( Shell shell, XulEventHandler... handlers ) throws ModelerException{
    try{
      SwtXulLoader loader = new SwtXulLoader();
      loader.setOuterContext(shell);
      container = loader.loadXul("org/pentaho/agilebi/pdi/modeler/panel.xul");
      bf = new DefaultBindingFactory();
      bf.setDocument(container.getDocumentRoot());
  
      if(handlers != null){
        for(XulEventHandler h : handlers){
          container.addEventHandler(h);
        }
      }
      
      runner = new SwtXulRunner();
      runner.addContainer(container);
      runner.initialize();
    } catch(Exception e){
      logger.info(e);
      throw new ModelerException(e);
    }
  }
  
  public Composite getMainPanel(){
    return (Composite) container.getDocumentRoot().getRootElement().getElementById("mainVBox").getManagedObject();
  }
  
  public BindingFactory getBindingFactory() {
    return bf;
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
  
}
