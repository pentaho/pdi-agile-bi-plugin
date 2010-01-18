package org.pentaho.agilebi.pdi.visualizations.prpt;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.agilebi.pdi.modeler.Messages;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.perspective.AgileBiVisualizationPerspective;
import org.pentaho.agilebi.pdi.perspective.AbstractPerspective.XulTabAndPanel;
import org.pentaho.agilebi.pdi.visualizations.AbstractVisualization;
import org.pentaho.agilebi.pdi.visualizations.xul.PrptViewerTag;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.libraries.fonts.LibFontBoot;
import org.pentaho.reporting.libraries.resourceloader.LibLoaderBoot;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.w3c.dom.Node;

public class PRPTVisualization extends AbstractVisualization {

  static{
    LibLoaderBoot.getInstance().start();
    LibFontBoot.getInstance().start();
    ClassicEngineBoot.getInstance().start();
  }
  
  public PRPTVisualization(){
    super();
    System.setProperty("org.jpedal.suppressViewerPopups", "true");
  }
  
  private static Log logger = LogFactory.getLog(PRPTVisualization.class);
  
  public void createVisualizationFromModel(String modelFileLocation, String modelId) {
    // TODO Auto-generated method stub
    
  }

  public boolean accepts(String fileName) {
    return fileName.endsWith(".prpt");
  }

  public boolean acceptsXml(String nodeName) {
    return false;
  }

  public String[] getFileTypeDisplayNames(Locale locale) {
    return new String[]{Messages.getInstance().getString("prpt_file_type_name")};
  }

  public String getRootNodeName() {
    return null;
  }

  public String[] getSupportedExtensions() {
    return new String[]{"prpt"};
  }

  public boolean open(Node transNode, String fname, boolean importfile) {
    try{
      
      XulTabAndPanel tabAndPanel = AgileBiVisualizationPerspective.getInstance().createTab();
      
      AgileBiVisualizationPerspective.getInstance().setNameForTab(tabAndPanel.tab, fname);
      AgileBiVisualizationPerspective.getInstance().setMetaForTab(tabAndPanel.tab, null);
      
  
      try {
        SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiVisualizationPerspective.class);
      } catch (KettleException e) {
        logger.error(e);
        return false;
      }
      SwtXulLoader theXulLoader = new SwtXulLoader();
      theXulLoader.register("PRPT", "org.pentaho.agilebi.pdi.visualizations.xul.PrptViewerTag");
      XulDomContainer theXulContainer = theXulLoader.loadXul("org/pentaho/agilebi/pdi/visualizations/prpt/prptVisualization.xul");
      Composite theMainBox = (Composite) theXulContainer.getDocumentRoot().getElementById("prptViewer").getManagedObject();
      SwtXulRunner theRunner = new SwtXulRunner();
      theRunner.addContainer(theXulContainer);
      theRunner.initialize();
      theMainBox.setParent((Composite) tabAndPanel.panel.getManagedObject());
      
      ((PrptViewerTag) theXulContainer.getDocumentRoot().getElementById("prptViewer")).setSrc(fname);
      ((Composite) tabAndPanel.panel.getManagedObject()).layout(true);
      return true;
    } catch(Exception e){
      e.printStackTrace();
    }
    return false;
  }

  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    return false;
  }

  public void syncMetaName(EngineMetaInterface meta, String name) {
  }

  public void createVisualizationFromModel(ModelerWorkspace model) {
    // TODO Auto-generated method stub
    
  }
  
  
	
}
