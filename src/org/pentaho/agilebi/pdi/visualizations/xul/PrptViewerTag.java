package org.pentaho.agilebi.pdi.visualizations.xul;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewPane;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.swt.SwtElement;

public class PrptViewerTag extends SwtElement{

  private String src;
  
  private PreviewPane viewer;
  
  private MasterReport masterReport;
  
  private static Log log = LogFactory.getLog(PrptViewerTag.class);

  
  public PrptViewerTag(Element self, XulComponent parent, XulDomContainer container, String tagName) {
    super("prpt");
 
    Composite parentComposite = (Composite) parent.getManagedObject();

    Composite swingComposite = new Composite(parentComposite, SWT.EMBEDDED);
    GridData gData = new GridData(GridData.FILL_BOTH);
    swingComposite .setLayoutData(gData);
    Frame swingFrame = SWT_AWT.new_Frame(swingComposite);

    JPanel browserPanel = new JPanel();
    browserPanel.setLayout(new BorderLayout());
    
    swingFrame.add(browserPanel);
    
    this.viewer = new PreviewPane();
    browserPanel.add(viewer, BorderLayout.CENTER);
    
    parentComposite.layout(true);
    setManagedObject(swingComposite);
    
  }
  
  public void layout(){

    this.initialized = true;
    if(src != null){
      loadPRPT();
    }
  }
  
  public String getSrc() {
    return src;
  }

  public void setSrc(String src) {
    this.src = src;
    if(this.initialized){
      loadPRPT();
    }
  }
  
  public void setMasterReport(MasterReport aMasterRerport) {
    this.masterReport = aMasterRerport;
    if(this.initialized){
      loadPRPT();
    }
  }
  
  
  private void loadPRPT(){
    try {
    	if (this.masterReport == null) {
	      ResourceManager theResourceManager = new ResourceManager();
	      theResourceManager.registerDefaults();
	      File theReportFile = new File(src);
	      Resource theResource = theResourceManager.createDirectly(theReportFile, MasterReport.class);
	      this.masterReport = (MasterReport) theResource.getResource();
    	}
      viewer.setReportJob(this.masterReport);
    } catch(Exception e){
      log.error(e);
    }
  }
  
  
}
