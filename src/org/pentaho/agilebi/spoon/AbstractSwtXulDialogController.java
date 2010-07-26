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
package org.pentaho.agilebi.spoon;

import org.slf4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

/**
 * An abstract XUL dialog controller. This can be subclassed to create instances of XUL dialogs
 * @author jamesdixon
 *
 */
public abstract class AbstractSwtXulDialogController extends AbstractXulDialogController {

  private XulRunner runner;
  
  private XulDomContainer container;

  private XulDialog xulDialog;
  
  private Composite swtDialog;
  
  private Shell shell;
  
  protected AbstractSwtXulDialogController( Shell shell ) {
    this.shell = shell;
  }
  
  /**
   * Initializes a dialog using the specified xul document and dialog id. If 
   * an event handler is not provided the dialog class will be added as an
   * event handler
   * @param xulPath Path to the XUL document
   * @param dialogId Id of the dialog to create
   * @param eventHandler Optional event handler to register
   * @param perspective Optional perspective to load
   * @throws XulException
   */
  public void initDialogController( String xulPath, String dialogId, XulEventHandler eventHandler, String perspective ) throws XulException {
    SwtXulLoader loader = new SwtXulLoader();
    loader.registerClassLoader(getClass().getClassLoader());
    loader.setSettingsManager(XulSpoonSettingsManager.getInstance());
    loader.setOuterContext(shell);
    container = loader.loadXul( xulPath ); 

    if( eventHandler != null ) {
      container.addEventHandler(eventHandler);
    } else {
      container.addEventHandler(this);
    }
    
    runner = new SwtXulRunner();
    runner.addContainer(container);

    // try and get the dialog 
    xulDialog = (XulDialog) container.getDocumentRoot().getRootElement().getElementById( dialogId ); 
    swtDialog = (Composite) container.getDocumentRoot().getRootElement().getElementById( dialogId ).getManagedObject(); 
    
    runner.initialize();
    if( perspective != null ) {
      container.loadPerspective( perspective );
    }
    
  }
  
  /**
   * Returns the XUL dialog
   */
  public XulDialog getDialog(){
    return xulDialog;
  }
  
  /**
   * Returns the SWT composite for the dialog
   * @return
   */
  public Composite getComposite() {
    return swtDialog;
  }
  
  public abstract Logger getLogger();

  /**
   * Returns the XulDomContainer for the XUL document
   * @return
   */
  public XulDomContainer getContainer() {
    return container;
  }
  
  protected abstract void init();

  public Shell getShell() {
    return shell;
  }

  public void setShell(Shell shell) {
    this.shell = shell;
  }
  
}
