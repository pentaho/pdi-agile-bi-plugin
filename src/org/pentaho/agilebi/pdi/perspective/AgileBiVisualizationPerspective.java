package org.pentaho.agilebi.pdi.perspective;

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
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.agilebi.pdi.modeler.ModelerEngineMeta;
import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspaceUtil;
import org.pentaho.agilebi.pdi.modeler.XulUI;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.DefaultBinding;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.w3c.dom.Node;

public class AgileBiVisualizationPerspective extends AbstractPerspective {

  private Log logger = LogFactory.getLog(AgileBiPerspective.class);
  private static final AgileBiVisualizationPerspective INSTANCE = new AgileBiVisualizationPerspective();
  private ResourceBundle messages = ResourceBundle.getBundle("org/pentaho/agilebi/pdi/perspective/perspective"); //$NON-NLS-1$
  protected List<ModelerWorkspace> models = new ArrayList<ModelerWorkspace>();
  private Map<ModelerWorkspace, EngineMetaInterface> metas = new HashMap<ModelerWorkspace, EngineMetaInterface>();
  
  private AgileBiVisualizationPerspective(){
    super("org/pentaho/agilebi/pdi/perspective/perspective.xul");
  }
  
  public static AgileBiVisualizationPerspective getInstance(){
    return INSTANCE;
  }
  
  public void setModel(ModelerWorkspace aModel) {
  	model = aModel;
  }
  
  public String getDisplayName(Locale l) {
    ResourceBundle messages = ResourceBundle.getBundle("org/pentaho/agilebi/pdi/perspective/perspective", l); //$NON-NLS-1$
    return messages.getString("visualizationPerspectiveName");
  }

  public InputStream getPerspectiveIcon() {
    ClassLoader loader = getClass().getClassLoader();
    return loader.getResourceAsStream("org/pentaho/agilebi/pdi/perspective/analyzer.png");
  }

  public String getId() {
    return "agilebiVisualization";
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
      XulTabAndPanel tabAndPanel = createTab();
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      XulUI xul = new XulUI(spoon.getShell(), model);
      metas.put(model, xul.getMeta());
  
      Composite parentComposite = (Composite) tabAndPanel.panel.getManagedObject();
      xul.getMainPanel().setParent(parentComposite);
      parentComposite.layout(true);
      
      Binding bind = new DefaultBinding(model, "shortFileName", tabAndPanel.tab, "label"); //$NON-NLS-1$ //$NON-NLS-2$
      bind.setConversion(new NameBindingConvertor(this, tabAndPanel.tab));
      bind.setBindingType(Binding.Type.ONE_WAY);
      document.addBinding(bind);
      models.add(model);
      
      SpoonPerspectiveManager.getInstance().activatePerspective(getClass());
    } catch (KettleException e) {
      logger.error(e);
    } catch (ModelerException e) {
      logger.error(e);
    }
  }
  
  public boolean open(Node transNode, String fname, boolean importfile) {
    try {
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      ModelerWorkspace model = new ModelerWorkspace();
      createTabForModel(model,AgileBiPerspective.createShortName(fname));

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
    return Collections.singletonList((XulEventHandler)this);
  }

  public List<XulOverlay> getOverlays() {
    XulOverlay overlay = new XulOverlay(){

      public String getId() {
        return "agileBi"; //$NON-NLS-1$
      }

      public String getOverlayUri() {
        return "org/pentaho/agilebi/pdi/perspective/visualization_perspective_overlay.xul"; //$NON-NLS-1$
        
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
    return "agileBiVisPerspective"; //$NON-NLS-1$
  }
  
  public boolean onTabClose(final int pos) throws XulException{
    return true;
  }
}
