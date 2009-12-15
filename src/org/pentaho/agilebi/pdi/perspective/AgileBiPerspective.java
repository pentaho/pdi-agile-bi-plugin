package org.pentaho.agilebi.pdi.perspective;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveListener;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.DefaultBinding;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulTab;
import org.pentaho.ui.xul.components.XulTabpanel;
import org.pentaho.ui.xul.containers.XulTabbox;
import org.pentaho.ui.xul.containers.XulTabpanels;
import org.pentaho.ui.xul.containers.XulTabs;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.pentaho.ui.xul.swt.tags.SwtTab;
import org.pentaho.ui.xul.util.XulDialogCallback;
import org.w3c.dom.Node;

import edu.emory.mathcs.backport.java.util.Collections;

public class AgileBiPerspective extends AbstractXulEventHandler implements SpoonPerspective, FileListener{

  private XulDomContainer container;
  private XulRunner runner;
  private Log logger = LogFactory.getLog(AgileBiPerspective.class);
  private Document document;
  private XulTabs tabs;
  private XulTabpanels panels;
  private XulTabbox tabbox;
  private static final AgileBiPerspective INSTANCE = new AgileBiPerspective();
  private List<SpoonPerspectiveListener> listeners = new ArrayList<SpoonPerspectiveListener>();
  private List<ModelerWorkspace> models = new ArrayList<ModelerWorkspace>();
  private ResourceBundle messages = ResourceBundle.getBundle("org/pentaho/agilebi/pdi/perspective/perspective"); //$NON-NLS-1$
  
  private Map<ModelerWorkspace, EngineMetaInterface> metas = new HashMap<ModelerWorkspace, EngineMetaInterface>();
  
  private AgileBiPerspective(){
    try{
      SwtXulLoader loader = new SwtXulLoader();
      container = loader.loadXul("org/pentaho/agilebi/pdi/perspective/perspective.xul"); //$NON-NLS-1$
      
      runner = new SwtXulRunner();
      runner.addContainer(container);
      runner.initialize();
      
      document = container.getDocumentRoot();
      container.addEventHandler(this);
      tabs = (XulTabs) document.getElementById("tabs");
      panels = (XulTabpanels) document.getElementById("tabpanels");
      tabbox = (XulTabbox) tabs.getParent();
    } catch(Exception e){
      logger.error(e);
    }
  }
  
  public static AgileBiPerspective getInstance(){
    return INSTANCE;
  }
  

  public String getDisplayName(Locale l) {
    return "Agile BI";
  }

  public InputStream getPerspectiveIcon() {
    ClassLoader loader = getClass().getClassLoader();
    return loader.getResourceAsStream("org/pentaho/agilebi/pdi/perspective/blueprint.png");
  }

  public void setActive(boolean active) {
    for(SpoonPerspectiveListener l : listeners){
      if(active){
        l.onActivation();
      } else {
        l.onDeactication();
      }
    }
  }

  public String getId() {
    return "agilebi";
  }

  public Composite getUI() {
    return (Composite) container.getDocumentRoot().getRootElement().getFirstChild().getManagedObject();
  }

  // ======== File Listener ====== //

  public boolean accepts(String fileName) {
    if(fileName == null || fileName.indexOf('.') == -1){
      return false;
    }
    String extension = fileName.substring(fileName.lastIndexOf('.')+1);
    return extension.equals("xmi");
  }
  

  public boolean acceptsXml(String nodeName) {
    return false;
  }

  public String[] getFileTypeDisplayNames(Locale locale) {
    return new String[]{"Models"};
  }

  public String getRootNodeName() {
    return null;
  }

  public String[] getSupportedExtensions() {
    return new String[]{"xmi"};
  }

  public void createTabForModel(final ModelerWorkspace model, String name){

    try{
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      XulUI xul = new XulUI(spoon.getShell(), model);
      metas.put(model, xul.getMeta());
      
      XulTab tab = (XulTab) document.createElement("tab");
      tab.setLabel(name);
      XulTabpanel panel = (XulTabpanel) document.createElement("tabpanel"); //$NON-NLS-1$
      xul.getMainPanel().setParent((Composite) panel.getManagedObject());
      
      Binding bind = new DefaultBinding(model, "shortFileName", tab, "label"); //$NON-NLS-1$ //$NON-NLS-2$
      bind.setConversion(new BindingConvertor<String, String>(){

        @Override
        public String sourceToTarget(String value) {
          String tabName = value;
          for(ModelerWorkspace m : models){
            if(m != model && m.getShortFileName().equals(value)){
              List<String> usedNames = new ArrayList<String>();
              for(XulComponent c : tabs.getChildNodes()){
                usedNames.add(((SwtTab) c).getLabel());
              }
              int num = 2;
              while(true){
                tabName = value+" ("+num+")";
                if(usedNames.contains(tabName) == false){
                  break;
                }
                num++;
              }
              break;
            }
          }
          return tabName;
        }
        @Override
        public String targetToSource(String value) {return value;}
        
      });
      bind.setBindingType(Binding.Type.ONE_WAY);
      document.addBinding(bind);
      
      tabs.addChild(tab);
      panels.addChild(panel);
      tabbox.setSelectedIndex(panels.getChildNodes().indexOf(panel));
      
      SpoonPerspectiveManager.getInstance().activatePerspective(getClass());

      models.add(model);
      
    } catch(ModelerException e){
      e.printStackTrace();
    } catch (XulException e) {
      e.printStackTrace();
    } catch (KettleException e) {
      e.printStackTrace();
    }
    
  }
  
  public boolean open(Node transNode, String fname, boolean importfile) {
    try{

      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      ModelerWorkspace model = new ModelerWorkspace();
      createTabForModel(model,createShortName(fname));

      String xml = new String(IOUtils.toByteArray(new FileInputStream(new File(fname))), "UTF-8"); //$NON-NLS-1$
      ModelerWorkspaceUtil.loadWorkspace(fname, xml, model);

      File f = new File(fname);
      String fullPath = f.getAbsolutePath();
      spoon.getProperties().addLastFile("Model", fullPath, null, false, null);
      spoon.addMenuLast();
      
    } catch(ModelerException e){
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

  public List<XulEventHandler> getEventHandlers() {
    return Collections.singletonList(this);
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


  public void addPerspectiveListener(SpoonPerspectiveListener listener) {
    if(listeners.contains(listener) == false){
      listeners.add(listener);
    }
  }
  
  public EngineMetaInterface getActiveMeta() {
    return metas.get(models.get(tabbox.getSelectedIndex()));
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
        metas.remove(pos);
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
 
  private static class CloseConfirmXulDialogCallback implements XulDialogCallback<Object>{
    public boolean closeIt = false;
    public void onClose(XulComponent sender, Status returnCode, Object retVal) {
      if(returnCode == Status.ACCEPT){
        closeIt = true;
      }
    }
    public void onError(XulComponent sender, Throwable t) {}
  }
}