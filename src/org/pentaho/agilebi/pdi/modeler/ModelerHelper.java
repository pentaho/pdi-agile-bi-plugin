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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.xul.swt.tab.TabItem;
import org.w3c.dom.Node;

public class ModelerHelper implements FileListener {

  private static final String MODELER_NAME = "Modeler"; //$NON-NLS-1$

  private static ModelerHelper instance = null;
    
  private static Log logger = LogFactory.getLog(ModelerHelper.class);
  
  private ModelerHelper() {
  }
  
  public static ModelerHelper getInstance() {
    if( instance == null ) {
      instance = new ModelerHelper();
    }
    return instance;
  }

  
  public void createModelerTabFromOutputStep() throws ModelerException {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    ModelerController controller = new ModelerController();
    ModelerEngineMeta meta = new ModelerEngineMeta(controller);
    ModelerWorkspaceUtil.createModelFromOutputStep(controller.getModel());
    XulUI xul = new XulUI(spoon.getShell(), meta, controller);

    // create unique name
    createModelerTab(spoon, xul, getUniqueUntitledTabName(spoon, MODELER_NAME));
  }
  

  public boolean open(Node transNode, String fname, boolean importfile) {
    try{
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      ModelerController controller = new ModelerController();
      ModelerEngineMeta meta = new ModelerEngineMeta(controller);
      XulUI xul = new XulUI(spoon.getShell(), meta, controller);
      TabItem tabItem = createModelerTab(spoon, xul, createShortName(fname));
      String xml = new String(IOUtils.toByteArray(new FileInputStream(new File(fname))), "UTF-8"); //$NON-NLS-1$
      ModelerWorkspaceUtil.loadWorkspace(fname, xml, controller.getModel());
      tabItem.setText(createShortName(fname));
    } catch(ModelerException e){
      e.printStackTrace();
    } catch(IOException e){
      e.printStackTrace();
    }
    
    return true;
  }
  
  private String getUniqueUntitledTabName(Spoon spoon, String title) {
    int num = 1;
    String tabName = title + " " + num; //$NON-NLS-1$
    // TODO: Add new plugin object type to spoon
    TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_BROWSER);
    while (tabItem != null) {
      tabName = title + " " + (++num); //$NON-NLS-1$
      // TODO: Add new plugin object type to spoon
      tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_BROWSER);
    }
    return tabName;
  }
  
  private TabItem createModelerTab(Spoon spoon, XulUI xul, String tabName) throws ModelerException {
    CTabFolder cTabFolder = spoon.tabfolder.getSwtTabset();
    TabItem tabItem = new TabItem(spoon.tabfolder, tabName, tabName);
    Image modelTabImage = ImageUtil.getImageAsResource(spoon.getDisplay(), "plugins/spoon/agile-bi/ui/images/modeler.png"); //$NON-NLS-1$
    tabItem.setImage(modelTabImage);
    
    Composite comp = xul.getMainPanel();
    comp.setParent(cTabFolder);
    tabItem.setControl(comp);
    
    // TODO: Add new plugin object type to spoon
    TabMapEntry entry = new TabMapEntry(tabItem, tabName, xul, TabMapEntry.OBJECT_TYPE_BROWSER);
    
    spoon.delegates.tabs.addTab(entry);
    spoon.getTabSet().addTab(tabItem);

    int idx = spoon.tabfolder.indexOf(tabItem);

    // keep the focus on the graph
    spoon.tabfolder.setSelected(idx);
    
    return tabItem;
  } 


  private String createShortName( String filename ) {
    if( filename == null ) {
      return null;
    }
    int extensionPos = filename.lastIndexOf('.');
    if( extensionPos == -1 ) {
      extensionPos = filename.length();
    }
    int sepPos = filename.replace('\\', '/').lastIndexOf('/');
    if( sepPos == -1 ) {
      sepPos = 0;
    } else {
      sepPos++;
    }
    return filename.substring(sepPos, extensionPos);    
  }
  
  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    try {
      ((ModelerEngineMeta)meta).getController().saveWorkspace(fname);
      return true;
    } catch (ModelerException e) {
      e.printStackTrace();
    } 
    return false;
  }

  public void syncMetaName(EngineMetaInterface meta, String name) {
    // TODO Auto-generated method stub
    
  }

    
}
