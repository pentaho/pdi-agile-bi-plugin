package org.pentaho.agilebi.test;

import java.io.File;

import org.junit.Test;
import org.pentaho.agilebi.pdi.visualizations.xul.PrptViewerTag;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.swt.SwtXulLoader;

/**
 * Not much we can test. The two cases mainly ensures there's no excpetions thrown by the component.
 * @author nbaker
 *
 */
public class TestPrptComponent {
  @Test
  public void testPrptSrc() throws Exception{

    if(ClassicEngineBoot.getInstance().isBootDone() == false){
      ClassicEngineBoot engineBoot = ClassicEngineBoot.getInstance();
      engineBoot.start();
    }
    
    SwtXulLoader loader = new SwtXulLoader();
    loader.register("PRPT", PrptViewerTag.class.getCanonicalName());
    XulDomContainer container = loader.loadXul("org/pentaho/agilebi/test/res/prpt.xul");
    
    PrptViewerTag prpt = (PrptViewerTag) container.getDocumentRoot().getElementById("prptViewer");
    
    prpt.setSrc("test-res/test.prpt");
    
  }
  
  @Test
  public void testPrptSetMasterReport() throws Exception{

    if(ClassicEngineBoot.getInstance().isBootDone() == false){
      ClassicEngineBoot engineBoot = ClassicEngineBoot.getInstance();
      engineBoot.start();
    }
    
    SwtXulLoader loader = new SwtXulLoader();
    loader.register("PRPT", PrptViewerTag.class.getCanonicalName());
    XulDomContainer container = loader.loadXul("org/pentaho/agilebi/test/res/prpt.xul");
    
    PrptViewerTag prpt = (PrptViewerTag) container.getDocumentRoot().getElementById("prptViewer");
    
    ResourceManager theResourceManager = new ResourceManager();
    theResourceManager.registerDefaults();
    File theReportFile = new File("test-res/test.prpt");
    Resource theResource = theResourceManager.createDirectly(theReportFile, MasterReport.class);
    MasterReport masterReport = (MasterReport) theResource.getResource();
    
    prpt.setMasterReport(masterReport);
    
  }
}
