package org.pentaho.agilebi.test;

import org.junit.Test;
import org.pentaho.agilebi.pdi.visualizations.xul.PrptViewerTag;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class TestPrptComponent {
  @Test
  public void testPrptComponent() throws Exception{

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
}
