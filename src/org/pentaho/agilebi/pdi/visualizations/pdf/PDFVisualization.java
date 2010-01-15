package org.pentaho.agilebi.pdi.visualizations.pdf;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.Locale;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.pentaho.agilebi.pdi.modeler.Messages;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.perspective.AgileBiVisualizationPerspective;
import org.pentaho.agilebi.pdi.perspective.AbstractPerspective.XulTabAndPanel;
import org.pentaho.agilebi.pdi.visualizations.AbstractVisualization;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.w3c.dom.Node;

public class PDFVisualization extends AbstractVisualization {

  public PDFVisualization(){
    super();
    System.setProperty("org.jpedal.suppressViewerPopups", "true");
  }
  
  private static Log logger = LogFactory.getLog(PDFVisualization.class);
  
  public void createVisualizationFromModel(String modelFileLocation, String modelId) {
    // TODO Auto-generated method stub
    
  }

  public boolean accepts(String fileName) {
    return fileName.endsWith(".pdf");
  }

  public boolean acceptsXml(String nodeName) {
    return false;
  }

  public String[] getFileTypeDisplayNames(Locale locale) {
    return new String[]{Messages.getInstance().getString("pdf_file_type_name")};
  }

  public String getRootNodeName() {
    return null;
  }

  public String[] getSupportedExtensions() {
    return new String[]{"pdf"};
  }

  public boolean open(Node transNode, String fname, boolean importfile) {
    
    XulTabAndPanel tabAndPanel = AgileBiVisualizationPerspective.getInstance().createTab();

    GridData gData = new GridData(GridData.FILL_BOTH);

    Composite parentComposite = (Composite) tabAndPanel.panel.getManagedObject();

    Composite swingComposite = new Composite(parentComposite, SWT.EMBEDDED);
    swingComposite .setLayoutData(gData);

    Frame swingFrame = SWT_AWT.new_Frame(swingComposite);

    JPanel browserPanel = new JPanel();
    browserPanel.setLayout(new BorderLayout());
    
    swingFrame.add(browserPanel);
    
    SimpleViewer viewer = new SimpleViewer(browserPanel, "plugins/spoon/agile-bi/visualizations/pdf/jPedalPrefs.xml");
    
    parentComposite.layout(true);
    AgileBiVisualizationPerspective.getInstance().setNameForTab(tabAndPanel.tab, fname);
    AgileBiVisualizationPerspective.getInstance().setMetaForTab(tabAndPanel.tab, null);
    

    try {
      SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiVisualizationPerspective.class);
    } catch (KettleException e) {
      logger.error(e);
      return false;
    }

    viewer.setupViewer();
    
    viewer.openDefaultFile(fname);
    return true;
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
