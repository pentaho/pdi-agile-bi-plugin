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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.agilebi.spoon.perspective;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceUtil;
import org.pentaho.agilebi.spoon.ModelerEngineMeta;
import org.pentaho.agilebi.spoon.SpoonModelerWorkspaceHelper;
import org.pentaho.agilebi.spoon.XulUI;
import org.pentaho.agilebi.spoon.visualizations.SaveAwareMeta;
import org.pentaho.agilebi.spoon.visualizations.analyzer.AnalyzerVisualizationController;
import org.pentaho.agilebi.spoon.visualizations.analyzer.AnalyzerVisualizationMeta;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.DefaultBinding;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.w3c.dom.Node;

public class AgileBiVisualizationPerspective extends AbstractPerspective {

	public static final String PERSPECTIVE_ID = "020-agilebiVisualization"; //$NON-NLS-1$
//  private Logger logger = LoggerFactory.getLogger(AgileBiModelerPerspective.class);
  private static final AgileBiVisualizationPerspective INSTANCE = new AgileBiVisualizationPerspective();
  protected List<ModelerWorkspace> models = new ArrayList<ModelerWorkspace>();
  private Map<ModelerWorkspace, EngineMetaInterface> metas = new HashMap<ModelerWorkspace, EngineMetaInterface>();
  private AgileBiVisualizationPerspectiveController controller = new AgileBiVisualizationPerspectiveController();
  
  private AgileBiVisualizationPerspective(){
    super("org/pentaho/agilebi/spoon/perspective/vizperspective.xul");
  }
  
  public static AgileBiVisualizationPerspective getInstance(){
    return INSTANCE;
  }
  
  public void setModel(ModelerWorkspace aModel) {
  	model = aModel;
  }
  
  public String getDisplayName(Locale l) {
  
    return BaseMessages.getString(this.getClass(), "Visualization.Perspective.perspectiveName");
  }

  public InputStream getPerspectiveIcon() {
    ClassLoader loader = getClass().getClassLoader();
    return loader.getResourceAsStream("org/pentaho/agilebi/spoon/perspective/analyzer.png");
  }

  public String getId() {
    return PERSPECTIVE_ID;
  }

  public boolean acceptsXml(String nodeName) {
    return false;
  }

  public String[] getFileTypeDisplayNames(Locale locale) {
    return new String[]{"Visualizations"};
  }

  public String[] getSupportedExtensions() {
    return new String[]{"xanalyzer"};
  }

  public void createTabForModel(final ModelerWorkspace model, String name){

    try {
      SpoonPerspectiveManager.getInstance().activatePerspective(getClass());
      XulTabAndPanel tabAndPanel = createTab();
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      XulUI xul = new XulUI(spoon.getShell(), model);
      metas.put(model, xul.getMeta());
  
      Composite parentComposite = (Composite) tabAndPanel.panel.getManagedObject();
      xul.getMainPanel().setParent(parentComposite);
      parentComposite.layout(true);
      if(selectedMeta != xul.getMeta()){
        setSelectedMeta(xul.getMeta());
      }
      
      Binding bind = new DefaultBinding(model, "shortFileName", tabAndPanel.tab, "label"); //$NON-NLS-1$ //$NON-NLS-2$
      bind.setConversion(new NameBindingConvertor(this, tabAndPanel.tab));
      bind.setBindingType(Binding.Type.ONE_WAY);
      document.addBinding(bind);
      models.add(model);
      setSelectedMeta(xul.getMeta());
      
    } catch (KettleException e) {
      e.printStackTrace();//logger.error(e);
    } catch (ModelerException e) {
      e.printStackTrace();//logger.error(e);
    }
  }
  
  public boolean open(Node transNode, String fname, boolean importfile) {
    try {
      
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      ModelerWorkspace model = new ModelerWorkspace(new SpoonModelerWorkspaceHelper(), SpoonModelerWorkspaceHelper.initGeoContext());
      createTabForModel(model, createShortName(fname));

      String xml = new String(IOUtils.toByteArray(new FileInputStream(new File(fname))), "UTF-8"); //$NON-NLS-1$
      ModelerWorkspaceUtil.loadWorkspace(fname, xml, model);

      File f = new File(fname);
      String fullPath = f.getAbsolutePath();
      spoon.getProperties().addLastFile("Visualization", fullPath, null, false, null);
      spoon.addMenuLast();
      
    } catch(ModelerException e) {
      e.printStackTrace();
    } catch(IOException e){
      e.printStackTrace();
    } 
    return true;
  }

  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    try {
      ((ModelerEngineMeta) meta).getController().saveWorkspace(fname);
      return true;
    } catch (ModelerException e) {
      e.printStackTrace();
    } 
    return false;
  }

  public void syncMetaName(EngineMetaInterface meta, String name) {
  }

  public List<XulEventHandler> getEventHandlers() {
    return Collections.singletonList( (XulEventHandler) controller);
  }

  public List<XulOverlay> getOverlays() {
    XulOverlay overlay = new XulOverlay(){

      public String getId() {
        return "agileBi"; //$NON-NLS-1$
      }

      public String getOverlayUri() {
        return "org/pentaho/agilebi/spoon/perspective/visualization_perspective_overlay.xul"; //$NON-NLS-1$
        
      }

      @Override
      public int getPriority() {
        return 0;
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
    return "agileBiVizPerspective"; //$NON-NLS-1$
  }
  
  public boolean onTabClose(final int pos) throws XulException{
	  AnalyzerVisualizationController controller = (AnalyzerVisualizationController) ((AnalyzerVisualizationMeta)selectedMeta).getController();
	  String contentId = PERSPECTIVE_ID+"\t"+controller.getVisFileLocation(); //$NON-NLS-1$
	  
// TODO - JD - enable this in Spoon
/*
	  String caller = Spoon.getInstance().getCaller(contentId);
	  if( caller == null ) {
		  contentId = PERSPECTIVE_ID+"\t"+controller.toString(); //$NON-NLS-1$
	  }
*/	  
    if(((SaveAwareMeta) this.getActiveMeta()).isDirty()){
      XulConfirmBox confirm = (XulConfirmBox) document.createElement("confirmbox"); //$NON-NLS-1$
      confirm.setTitle(BaseMessages.getString(this.getClass(), "Modeler.Perspective.unsavedChanges")); //$NON-NLS-1$
      confirm.setMessage(BaseMessages.getString(this.getClass(), "Visualization.Perspective.unsavedChangesMessage")); //$NON-NLS-1$
      
      CloseConfirmXulDialogCallback callback = new CloseConfirmXulDialogCallback();
      confirm.addDialogCallback(callback);
      confirm.open();
      if(callback.closeIt){
          switchToCaller(contentId);
        return true;
      } else {
        return false;
      }
      
    }
    switchToCaller(contentId);
    return true;
  }
  
  

  @Override
  public void setSelectedMeta(EngineMetaInterface meta) {
    super.setSelectedMeta(meta);
    controller.setSelectedModelerMeta(meta);
  }
  
}
