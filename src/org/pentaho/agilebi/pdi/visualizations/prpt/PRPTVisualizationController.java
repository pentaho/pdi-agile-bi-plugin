package org.pentaho.agilebi.pdi.visualizations.prpt;

import org.pentaho.agilebi.pdi.perspective.AgileBiPerspective;
import org.pentaho.agilebi.pdi.wizard.EmbeddedWizard;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class PRPTVisualizationController extends AbstractXulEventHandler{

  private Spoon spoon;
  private EngineMetaInterface meta;
  private MasterReport report;
  private String fileName;
  
  public PRPTVisualizationController(EngineMetaInterface meta, MasterReport rpt){
    spoon = (Spoon) SpoonFactory.getInstance();
    this.meta = meta;
    this.report = rpt;
  }
  
  @Override
  public String getName() {
    return "prpt";
  }  
  
  public void save(String fileName){
    this.fileName = fileName;
  }

  public void save(){
    spoon.saveToFile(meta);
    
  }
  
  public void saveAs(){

    spoon.saveFileAs(meta);
  }
  
  public void editReport(){
    EmbeddedWizard wiz = new EmbeddedWizard();
    try {
      wiz.run(report);
    } catch (ReportProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public void editModel(){
    String xmiFile = ((PmdDataFactory) this.report.getDataFactory()).getXmiFile();
    
    AgileBiPerspective.getInstance().open(null, xmiFile, false);
    
  }
  
  public void publish(){
    
  }
  
  public String getFileName(){
    return fileName;
  }
  
  public void setFileName(String fileName){
    this.fileName = fileName;
    
  }
}
