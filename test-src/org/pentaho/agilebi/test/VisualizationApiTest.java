package org.pentaho.agilebi.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;

@SuppressWarnings("nls")
public class VisualizationApiTest {
  
  @Test
  public void testAnalyzerPlugin() {
    TestVisualizationManager mgr = new TestVisualizationManager();
    mgr.loadVisualizationFile(new File("package-res/visualizations/analyzer/plugin.xml"));
    
    Assert.assertEquals(2, mgr.getVisualizations().size());
    IVisualization vis = mgr.getVisualizations().get(0);
    Assert.assertEquals(0, vis.getOrder());
    Assert.assertEquals("Analyzer", vis.getTitle());
  }
  
  static class TestVisualizationManager extends VisualizationManager {
    
    public TestVisualizationManager() {
      super();
    }
    
    public TestVisualizationManager(String dirloc) {
      super(dirloc);
    }
    // make this method public for testing
    public void loadVisualizationFile(File file) {
      super.loadVisualizationFile(file);
    }
  }
  
  @Test
  public void testVizApi() {
    VisualizationManager mgr = new VisualizationManager("package-res/visualizations");
    Assert.assertEquals(1, mgr.getVisualizations().size());
  }
}
