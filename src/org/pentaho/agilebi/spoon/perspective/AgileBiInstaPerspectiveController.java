/*!
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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.agilebi.spoon.perspective;

import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulBrowser;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class AgileBiInstaPerspectiveController extends AbstractXulEventHandler {

  private BindingFactory bf = new DefaultBindingFactory();
  private boolean dirty = true;
  private XulBrowser browser;
  
  @Override
  public String getName() {
    return "instaPerspective"; //$NON-NLS-1$
  }

  @Override
  public void setXulDomContainer(XulDomContainer xulDomContainer) {
    super.setXulDomContainer(xulDomContainer);
    bf.setDocument(document);
    
  }
  
  public void setBrowser( XulBrowser browser ) {
	  this.browser = browser;
  }
  
  public void setPropVisible(boolean vis){
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }
  
  public void saveFile() {
		browser.execute("externalSaveFile()"); //$NON-NLS-1$
  }
  
  public void saveFileAs() {
		browser.execute("externalSaveFileAs()"); //$NON-NLS-1$
  }
  
  
}
