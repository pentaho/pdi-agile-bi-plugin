package org.pentaho.agilebi.pdi.perspective;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.DefaultBinding;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.w3c.dom.Node;


public class AgileBiPerspective extends AbstractPerspective implements SpoonPerspective, FileListener{

  private Log logger = LogFactory.getLog(AgileBiPerspective.class);
  private static final AgileBiPerspective INSTANCE = new AgileBiPerspective();
  private ResourceBundle messages = ResourceBundle.getBundle("org/pentaho/agilebi/pdi/perspective/perspective"); //$NON-NLS-1$
  protected List<ModelerWorkspace> models = new ArrayList<ModelerWorkspace>();
    
  private AgileBiPerspective() {
    super("org/pentaho/agilebi/pdi/perspective/agileBiPerspective.xul");
    setDefaultExtension("xmi");
  }
  
  public static AgileBiPerspective getInstance() {
    return INSTANCE;
  }
  
  public String getDisplayName(Locale l) {
    ResourceBundle messages = ResourceBundle.getBundle("org/pentaho/agilebi/pdi/perspective/perspective", l); //$NON-NLS-1$
    return messages.getString("perspectiveName");
  }

  public InputStream getPerspectiveIcon() {
    ClassLoader loader = getClass().getClassLoader();
    return loader.getResourceAsStream("org/pentaho/agilebi/pdi/perspective/blueprint.png");
  }

  public String getId() {
    return "agilebi";
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
      XulTabAndPanel tabAndPanel = createTab();
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      XulUI xul = new XulUI(spoon.getShell(), aModel);
      metas.put(tabAndPanel.tab, xul.getMeta());
  
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
      
      SpoonPerspectiveManager.getInstance().activatePerspective(getClass());
   } catch(KettleException e){
      throw new ModelerException(e);
    }
  }
  
  public boolean open(Node transNode, String fname, boolean importfile) {
    try {
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      ModelerWorkspace model = new ModelerWorkspace();
      String xml = new String(IOUtils.toByteArray(new FileInputStream(new File(fname))), "UTF-8"); //$NON-NLS-1$
      ModelerWorkspaceUtil.loadWorkspace(fname, xml, model);
      
      createTabForModel(model,AgileBiPerspective.createShortName(fname));

      File f = new File(fname);
      String fullPath = f.getAbsolutePath();
      spoon.getProperties().addLastFile("Model", fullPath, null, false, null);
      spoon.addMenuLast();
      
      return true;  
    } catch(ModelerException e){
      logger.error(e);
    } catch(IOException e){
      logger.error(e);
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
    return Collections.singletonList( (XulEventHandler) this);
  }

  public List<XulOverlay> getOverlays() {
    XulOverlay overlay = new XulOverlay(){

      public String getId() {
        return "agileBi"; //$NON-NLS-1$
      }

      public String getOverlayUri() {
        return "org/pentaho/agilebi/pdi/perspective/perspective_overlay.xul"; //$NON-NLS-1$
        
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
    if(models.get(0).isDirty()){
      XulConfirmBox confirm = (XulConfirmBox) document.createElement("confirmbox"); //$NON-NLS-1$
      confirm.setTitle(messages.getString("unsavedChanges")); //$NON-NLS-1$
      confirm.setMessage(messages.getString("unsavedChangesMessage")); //$NON-NLS-1$
      
      CloseConfirmXulDialogCallback callback = new CloseConfirmXulDialogCallback();
      confirm.addDialogCallback(callback);
      confirm.open();
      if(callback.closeIt){
        models.remove(pos);
        metas.remove(tabbox.getTabs().getChildNodes().get(pos));
        return true;
      } else {
        return false;
      }
      
    } else {
      models.remove(pos);
      metas.remove(pos);
    }
    return true;
  }
 
}