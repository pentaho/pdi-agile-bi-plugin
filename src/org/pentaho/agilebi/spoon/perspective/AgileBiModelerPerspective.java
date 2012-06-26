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

import org.apache.commons.io.IOUtils;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.DocumentHelper;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.pentaho.agilebi.modeler.*;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceUtil;
import org.pentaho.agilebi.spoon.ModelerEngineMeta;
import org.pentaho.agilebi.spoon.SpoonModelerMessages;
import org.pentaho.agilebi.spoon.SpoonModelerWorkspaceHelper;
import org.pentaho.agilebi.spoon.XulUI;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBinding;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTab;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.w3c.dom.Node;

import java.io.*;
import java.util.*;


public class AgileBiModelerPerspective extends AbstractPerspective implements SpoonPerspective, FileListener{

	public static final String PERSPECTIVE_ID = "010-agilebi"; //$NON-NLS-1$

  private Logger logger = LoggerFactory.getLogger(AgileBiModelerPerspective.class);
  private static final AgileBiModelerPerspective INSTANCE = new AgileBiModelerPerspective();
  protected List<ModelerWorkspace> models = new ArrayList<ModelerWorkspace>();
  private XulMenuitem modelPropItem;
  
  private AgileBiModelerPerspectiveController perspectiveController = new AgileBiModelerPerspectiveController();

  BindingFactory bf = new DefaultBindingFactory();
  
  private AgileBiModelerPerspective() {
    super("org/pentaho/agilebi/spoon/perspective/perspective.xul");
    setDefaultExtension("xmi");
    bf.setDocument(document);
    try {
      ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
    } catch (IllegalStateException e) {
      // someone else set this first, ignore the error
    }
  }
  
  public static AgileBiModelerPerspective getInstance() {
    return INSTANCE;
  }
  
  public String getDisplayName(Locale l) {
    return BaseMessages.getString(this.getClass(), "Modeler.Perspective.perspectiveName");
  }

  public InputStream getPerspectiveIcon() {
    ClassLoader loader = getClass().getClassLoader();
    return loader.getResourceAsStream("org/pentaho/agilebi/spoon/perspective/blueprint.png");
  }

  public String getId() {
    return PERSPECTIVE_ID;
  }

  public boolean acceptsXml(String nodeName) {
    return false;
  }

  public String[] getFileTypeDisplayNames(Locale locale) {
    return new String[]{"Models"};
  }

  public String[] getSupportedExtensions() {
    return new String[]{"xmi"};
  }

  public void createTabForModel(final ModelerWorkspace aModel, String name) throws ModelerException {

    try {
      SpoonPerspectiveManager.getInstance().activatePerspective(getClass());
      
      XulTabAndPanel tabAndPanel = createTab();
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      XulUI xul = new XulUI(spoon.getShell(), aModel);
      metas.put(tabAndPanel.tab, xul.getMeta());
      if(selectedMeta != xul.getMeta()){
        setSelectedMeta(xul.getMeta());
      }
  
      Composite parentComposite = (Composite) tabAndPanel.panel.getManagedObject();
      xul.getMainPanel().setParent(parentComposite);
      parentComposite.layout(true);
      
      setNameForTab(tabAndPanel.tab, name);
      
      Binding bind = new DefaultBinding(aModel, "shortFileName", tabAndPanel.tab, "label"); //$NON-NLS-1$ //$NON-NLS-2$
      bind.setConversion(new NameBindingConvertor(this, tabAndPanel.tab));
      bind.setBindingType(Binding.Type.ONE_WAY);
      document.addBinding(bind);
      models.add(aModel);
    	model = aModel;
    	setSelectedMeta(xul.getMeta());
    	Spoon.getInstance().enableMenus();
      
   } catch(KettleException e){
      throw new ModelerException(e);
    }
  }
  
  public boolean open(Node transNode, String fname, boolean importfile) {

    try {
      // files may be a mix of absolute and relative. Constructing File objects to test equality
      File incomingFile = new File(fname);
      for(Map.Entry<XulTab, EngineMetaInterface> m : this.metas.entrySet()){
        if(m == null){
          continue;
        }
        String fileName = ((ModelerEngineMeta) m.getValue()).getController().getModel().getFileName();
        
        if(fileName != null && new File(fileName).getAbsoluteFile().equals(incomingFile.getAbsoluteFile())){
          int idx = this.tabbox.getTabs().getChildNodes().indexOf(m.getKey());
          if(idx > -1){
            SpoonPerspectiveManager.getInstance().activatePerspective(getClass());
            this.tabbox.setSelectedIndex(idx);
            return true;
          }
        }
      }
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      ModelerWorkspace theModel = new ModelerWorkspace(new SpoonModelerWorkspaceHelper(), SpoonModelerWorkspaceHelper.initGeoContext());
      theModel.setTemporary(false);
      theModel.setDirty(false);
      String xml = new String(IOUtils.toByteArray(new FileInputStream(new File(fname))), "UTF-8"); //$NON-NLS-1$
      ModelerWorkspaceUtil.loadWorkspace(fname, xml, theModel);
      
      createTabForModel(theModel, createShortName(fname));

      File f = new File(fname);
      String fullPath = f.getAbsolutePath();
      spoon.getProperties().addLastFile("Model", fullPath, null, false, null); //$NON-NLS-1$
      spoon.addMenuLast();
      
      return true;  
    } catch(IncompatibleModelerException e){
      try {
        XulMessageBox box = (XulMessageBox) document.createElement("messagebox");
        box.setTitle(BaseMessages.getString(AgileBiModelerPerspective.class, "incompatible_model_title"));
        box.setMessage(BaseMessages.getString(AgileBiModelerPerspective.class, "incompatible_model_msg"));
        box.setModalParent(((Spoon)SpoonFactory.getInstance()).getShell());
        box.open();
      } catch (XulException e1) {
        e1.printStackTrace();
      }
      
    } catch(ModelerException e){
      logger.error("Error opening file", e);
    } catch(IOException e){
      logger.error("Error opening file", e);
    } catch (KettleException e) {
      logger.error("Error opening file", e);
    } 
    
    return false;
  }

  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    try {
      return ((ModelerEngineMeta) meta).getController().saveWorkspace(fname);
    } catch (ModelerException e) {
      e.printStackTrace();
    } 
    return false;
  }

  public void syncMetaName(EngineMetaInterface meta, String name) {
  }

  public List<XulEventHandler> getEventHandlers() {
    return Collections.singletonList( (XulEventHandler) perspectiveController);
  }

  public List<XulOverlay> getOverlays() {
    XulOverlay overlay = new XulOverlay(){

      public String getId() {
        return "agileBi"; //$NON-NLS-1$
      }

      public String getOverlayUri() {
        return "org/pentaho/agilebi/spoon/perspective/modeler_perspective_overlay.xul"; //$NON-NLS-1$
        
      }

      public String getOverlayXml() {
        return null;
      }

      public String getResourceBundleUri() {
        return null;
      }

      public String getSource() {
        return null;
      }
      
    };
    return Collections.singletonList(overlay);
  }


  @Override
  public String getName() {
    return "agileBiPerspective"; //$NON-NLS-1$
  }

  public boolean onTabClose(final int pos) throws XulException{
  	String contentId = PERSPECTIVE_ID+"\t"+models.get(pos).getFileName(); //$NON-NLS-1$
    if(models.get(pos).isDirty()){
      XulConfirmBox confirm = (XulConfirmBox) document.createElement("confirmbox"); //$NON-NLS-1$
      confirm.setTitle(BaseMessages.getString(this.getClass(), "Modeler.Perspective.unsavedChanges")); //$NON-NLS-1$
      confirm.setMessage(BaseMessages.getString(this.getClass(), "Modeler.Perspective.unsavedChangesMessage")); //$NON-NLS-1$
      
      CloseConfirmXulDialogCallback callback = new CloseConfirmXulDialogCallback();
      confirm.addDialogCallback(callback);
      confirm.open();
      if(callback.closeIt){
        models.remove(pos);
        metas.remove(tabbox.getTabs().getChildNodes().get(pos));
        switchToCaller(contentId);
        return true;
      } else {
        return false;
      }
      
    } else {
        switchToCaller(contentId);
      models.remove(pos);
      metas.remove(pos);
    }
    return true;
  }
  
  public void exportSchema() {
    try {
      if (this.model.isValid()) {
        this.model.getWorkspaceHelper().populateDomain(this.model);
        LogicalModel lModel = this.model.getLogicalModel(ModelerPerspective.ANALYSIS);

        FileDialog fileDialog = new FileDialog(Spoon.getInstance().getShell(), SWT.SAVE);
        String[] theExtensions = { "*.xml" };
        fileDialog.setFilterExtensions(theExtensions);
        String theFile = fileDialog.open();
        if(theFile != null) {
          MondrianModelExporter exporter = new MondrianModelExporter(lModel, LocalizedString.DEFAULT_LOCALE);
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
        for (String msg : this.model.getValidationMessages()) {
          validationErrors.append(msg);
          validationErrors.append("\n");
          logger.info(msg);
        }
        MessageDialog.openError(Spoon.getInstance().getShell(), "", validationErrors.toString());
      }
    } catch (Exception e) {
      logger.error("Error exporting Schema", e);
      MessageDialog.openError(Spoon.getInstance().getShell(), "", e.getMessage());
    }
  }

  @Override
  public void setSelectedMeta(EngineMetaInterface meta) {
    super.setSelectedMeta(meta);
    perspectiveController.setSelectedModelerMeta((ModelerEngineMeta) meta);
  }
  
  
 
  
}