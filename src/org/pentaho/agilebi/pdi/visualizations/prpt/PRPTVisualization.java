package org.pentaho.agilebi.pdi.visualizations.prpt;

import java.io.File;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.pentaho.agilebi.pdi.PDIMessages;
import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspaceUtil;
import org.pentaho.agilebi.pdi.modeler.XulUI;
import org.pentaho.agilebi.pdi.perspective.AgileBiVisualizationPerspective;
import org.pentaho.agilebi.pdi.perspective.AbstractPerspective.XulTabAndPanel;
import org.pentaho.agilebi.pdi.visualizations.AbstractVisualization;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.wizard.EmbeddedWizard;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.fonts.LibFontBoot;
import org.pentaho.reporting.libraries.resourceloader.LibLoaderBoot;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.WaitBoxRunnable;
import org.pentaho.ui.xul.components.XulWaitBox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.w3c.dom.Node;

public class PRPTVisualization extends AbstractVisualization {
  
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
    return new String[]{BaseMessages.getString(IVisualization.class, "prpt_file_type_name")};
  }

  public String getRootNodeName() {
    return null;
  }

  public String[] getSupportedExtensions() {
    return new String[]{"prpt"};
  }

  public boolean open(Node transNode, String fname, boolean importfile) {
    try{
      
      if(ClassicEngineBoot.getInstance().isBootDone() == false){

        ObjectUtilities.setClassLoader(getClass().getClassLoader());
        ObjectUtilities.setClassLoaderSource(ObjectUtilities.CLASS_CONTEXT);
        
        LibLoaderBoot.getInstance().start();
        LibFontBoot.getInstance().start();
        ClassicEngineBoot.getInstance().start();
      }
      
      MasterReport masterReport = null;
      try {
        ResourceManager theResourceManager = new ResourceManager();
        theResourceManager.registerDefaults();
        File theReportFile = new File(fname);
        Resource theResource = theResourceManager.createDirectly(theReportFile, MasterReport.class);
        masterReport = (MasterReport) theResource.getResource();
      } catch(Exception e){
        logger.error(e);
      }
      
      
      XulTabAndPanel tabAndPanel = AgileBiVisualizationPerspective.getInstance().createTab();
      
      AgileBiVisualizationPerspective.getInstance().setNameForTab(tabAndPanel.tab, fname);
  
      try {
        SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiVisualizationPerspective.class);
      } catch (KettleException e) {
        logger.error(e);
        return false;
      }
      SwtXulLoader theXulLoader = new SwtXulLoader();
      theXulLoader.registerClassLoader(getClass().getClassLoader());
      theXulLoader.register("PRPT", "org.pentaho.agilebi.pdi.visualizations.xul.PrptViewerTag");
      XulDomContainer theXulContainer = theXulLoader.loadXul("org/pentaho/agilebi/pdi/visualizations/prpt/prptVisualization.xul", new PDIMessages(IVisualization.class));
      
      PRPTMeta meta = new PRPTMeta();
      meta.setTab(tabAndPanel.tab);
      AgileBiVisualizationPerspective.getInstance().setMetaForTab(tabAndPanel.tab, meta);
      
      PRPTVisualizationController controller = new PRPTVisualizationController(meta, masterReport);
      
      theXulContainer.addEventHandler(controller);
      
      Composite theMainBox = (Composite) theXulContainer.getDocumentRoot().getElementById("mainContainer").getManagedObject();
      SwtXulRunner theRunner = new SwtXulRunner();
      theRunner.addContainer(theXulContainer);
      theRunner.initialize();
      theMainBox.setParent((Composite) tabAndPanel.panel.getManagedObject());

      ((Composite) tabAndPanel.panel.getManagedObject()).layout(true);

      AgileBiVisualizationPerspective.getInstance().setSelectedMeta(meta);
      return true;
    } catch(Exception e){
      e.printStackTrace();
    }
    return false;
  }

  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    try{
      PRPTMeta prptMeta = (PRPTMeta) meta;
      prptMeta.save(fname);

      if(fname.endsWith(".prpt") == false){
        fname +=".prpt";
      }
      File f = new File(fname);
      String fullPath = f.getAbsolutePath();
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      spoon.getProperties().addLastFile("Model", fullPath, null, false, null);
      AgileBiVisualizationPerspective.getInstance().setNameForTab(prptMeta.getTab(), fname);
      spoon.addMenuLast();
      return true;
    } catch(ModelerException e){
      logger.error(e);
      return false;
    }
  }

  public void syncMetaName(EngineMetaInterface meta, String name) {
  }

  public void createVisualizationFromModel(final ModelerWorkspace model) {


    
    XulWaitBox box;
    try {
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      Document document = spoon.getMainSpoonContainer().getDocumentRoot();
      
      box = (XulWaitBox) document.createElement("waitbox");
      box.setIndeterminate(true);
      box.setMaximum(10);
      box.setCanCancel(false);
      box.setTitle(BaseMessages.getString(XulUI.class, "wait_dialog_title"));
      box.setMessage(BaseMessages.getString(XulUI.class, "wait_dialog_message"));
      
      box.setCancelLabel(BaseMessages.getString(XulUI.class, "wait_dialog_btn"));
      
      box.setDialogParent(((Spoon)SpoonFactory.getInstance()).getShell());
      box.setRunnable(new WaitBoxRunnable(box){
        boolean canceled = false;
        @Override
        public void run() {
          
          try {

            ObjectUtilities.setClassLoader(getClass().getClassLoader());
            ObjectUtilities.setClassLoaderSource(ObjectUtilities.CLASS_CONTEXT);
            
            if(ClassicEngineBoot.getInstance().isBootDone() == false){
              ClassicEngineBoot engineBoot = ClassicEngineBoot.getInstance();
              engineBoot.start();
            }
            model.setAutoModel(false);
            EmbeddedWizard wizard = new EmbeddedWizard(model);
            waitBox.stop();
            wizard.run(null);
          } catch (final Exception e) {
            logger.error(e);
            Display.getDefault().asyncExec(new Runnable(){
              public void run() {
                new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e);
              }
            });
          
          }
          waitBox.stop();
        }

        @Override
        public void cancel() {
          canceled =true;
        }
        
        
      });
      box.start();
    } catch (XulException e1) {
      logger.error(e1);
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e1);
    }
    
  }
  
  public void createVisualizationFromMasterReport(MasterReport rpt){
try{
      
      if(ClassicEngineBoot.getInstance().isBootDone() == false){
        LibLoaderBoot.getInstance().start();
        LibFontBoot.getInstance().start();
        ClassicEngineBoot.getInstance().start();
      }
      
      XulTabAndPanel tabAndPanel = AgileBiVisualizationPerspective.getInstance().createTab();
      
      AgileBiVisualizationPerspective.getInstance().setNameForTab(tabAndPanel.tab, "Untitled Report");
      
  
      try {
        SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiVisualizationPerspective.class);
      } catch (KettleException e) {
        logger.error(e);
        return;
      }
      SwtXulLoader theXulLoader = new SwtXulLoader();
      theXulLoader.registerClassLoader(getClass().getClassLoader());
      theXulLoader.register("PRPT", "org.pentaho.agilebi.pdi.visualizations.xul.PrptViewerTag");
      XulDomContainer theXulContainer = theXulLoader.loadXul("org/pentaho/agilebi/pdi/visualizations/prpt/prptVisualization.xul", new PDIMessages(IVisualization.class));
      Composite theMainBox = (Composite) theXulContainer.getDocumentRoot().getElementById("mainContainer").getManagedObject();
      
      PRPTMeta meta = new PRPTMeta();

      AgileBiVisualizationPerspective.getInstance().setMetaForTab(tabAndPanel.tab, meta);
      meta.setTab(tabAndPanel.tab);
      
      PRPTVisualizationController controller = new PRPTVisualizationController(meta, rpt);
      theXulContainer.addEventHandler(controller);
      
      SwtXulRunner theRunner = new SwtXulRunner();
      theRunner.addContainer(theXulContainer);
      theRunner.initialize();
      theMainBox.setParent((Composite) tabAndPanel.panel.getManagedObject());
      
      ((Composite) tabAndPanel.panel.getManagedObject()).layout(true);
      AgileBiVisualizationPerspective.getInstance().setSelectedMeta(meta);

    } catch(Exception e){
      e.printStackTrace();
    }
    
  }
	
}
