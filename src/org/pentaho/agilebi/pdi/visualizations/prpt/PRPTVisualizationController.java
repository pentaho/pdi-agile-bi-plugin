package org.pentaho.agilebi.pdi.visualizations.prpt;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.pdi.modeler.Messages;
import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.modeler.ModelerHelper;
import org.pentaho.agilebi.pdi.perspective.AgileBiPerspective;
import org.pentaho.agilebi.pdi.visualizations.PropertyPanelController;
import org.pentaho.agilebi.pdi.visualizations.xul.PrptViewerTag;
import org.pentaho.agilebi.pdi.wizard.EmbeddedWizard;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalModel;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.containers.XulEditpanel;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class PRPTVisualizationController extends AbstractXulEventHandler implements PropertyPanelController{

  private Spoon spoon;
  private EngineMetaInterface meta;
  private MasterReport report;
  private String fileName;
  private Binding modelNameBinding;
  private Binding factTableNameBinding;
  private BindingFactory bf;
  private static Log logger = LogFactory.getLog(PRPTVisualizationController.class);
  private String factTableName, modelId;
  private PrptViewerTag viewer;
  private XulEditpanel propPanel;
  
  public PRPTVisualizationController(PRPTMeta meta, MasterReport rpt){
    spoon = (Spoon) SpoonFactory.getInstance();
    meta.setController(this);
    this.meta = meta;
    this.report = rpt;
  }
  
  @Override
  public String getName() {
    return "prpt";
  }
  
  public void init(){
    this.bf = new DefaultBindingFactory();
    bf.setDocument(document);
    this.propPanel = (XulEditpanel) document.getElementById("propPanel");

    
    // try to find the model name
    String xmiFileLocation = getXmiFileLocation();
    if(xmiFileLocation != null){

      Domain theDomain = ModelerHelper.getInstance().loadDomain(xmiFileLocation);
      modelId = theDomain.getLogicalModels().get(0).getName(Locale.getDefault().toString());
    } else {
      modelId = Messages.getString("unavailable");
    }
    bf.setBindingType(Binding.Type.ONE_WAY);
    this.modelNameBinding = this.bf.createBinding(this, "modelId", "modelName", "value");
    this.factTableNameBinding = this.bf.createBinding(this, "factTableName", "factTableName", "value");

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(this.propPanel, "visible", this, "propVisible");

    viewer = (PrptViewerTag) document.getElementById("prptViewer");
    loadReport();
    fireBindings();
  }

  private void fireBindings() {
    try {
      this.modelNameBinding.fireSourceChanged();
      this.factTableNameBinding.fireSourceChanged();
    } catch (Exception e) {
      logger.info(e);
    }
  }

  
  
  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }
  
  public void setFactTableName(String name){
    this.factTableName = name;
  }

  public String getFactTableName() {
    if (this.factTableName == null) {
      this.factTableName = processFactTableName();
    }
    return this.factTableName;
  }

  private String processFactTableName() {
    String theName = null;
    String xmiFileLocation = getXmiFileLocation();
    if(xmiFileLocation == null){
      return Messages.getString("unavailable");
    }
    Domain theDomain = ModelerHelper.getInstance().loadDomain(xmiFileLocation);
    List<IPhysicalModel> theModels = theDomain.getPhysicalModels();
    if (theModels != null && theModels.size() > 0) {
      IPhysicalModel theModel = theModels.get(0);
      List theTables = theModel.getPhysicalTables();
      if (theTables != null && theTables.size() > 0) {
        IPhysicalTable theTable = (IPhysicalTable) theTables.get(0);
        theName = theTable.getName(Locale.getDefault().toString());
      }
    }
    return theName;
  }
  
  public void save(String fileName) throws ModelerException{
    this.fileName = fileName;
    
    try {
      BundleWriter.writeReportToZipFile(report, new File(fileName));
    } catch (Exception e) {
      throw new ModelerException(e);
    }
    
  }

  public void save(){
    try {
      spoon.saveToFile(meta);
    } catch (KettleException e) {
      logger.error(e);
      showErrorDialog(Messages.getString("error_saving"));
    }
    
  }
  
  public void saveAs(){
    try {
      spoon.saveFileAs(meta);
    } catch (KettleException e) {
      logger.error(e);
      showErrorDialog(Messages.getString("error_saving"));
    }
  }
  
  private void showErrorDialog(String msg){
    XulMessageBox dlg;
    try {
      dlg = (XulMessageBox) document.createElement("messagebox");
      dlg.setTitle(Messages.getString("error_title"));
      dlg.setMessage(msg);
      dlg.open();
    } catch (XulException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public void editReport(){
    String xmiFileLocation = getXmiFileLocation();
    
    if(xmiFileLocation != null){
      EmbeddedWizard wiz = new EmbeddedWizard();
      try {
        wiz.run(report);
      } catch (ReportProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else {

      XulMessageBox box;
      try {
        box = (XulMessageBox) document.createElement("messagebox");
        box.setTitle(Messages.getString("error_title"));
        box.setMessage(Messages.getString("prpt_no_model"));
        box.open();
      } catch (XulException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public void editModel(){
    String xmiFileLocation = getXmiFileLocation();
    
    if(xmiFileLocation != null){
      AgileBiPerspective.getInstance().open(null, xmiFileLocation, false);
    } else {

      XulMessageBox box;
      try {
        box = (XulMessageBox) document.createElement("messagebox");
        box.setTitle(Messages.getString("error_title"));
        box.setMessage(Messages.getString("prpt_no_model"));
        box.open();
      } catch (XulException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
  }
  
  public String getXmiFileLocation(){

    if(this.report.getDataFactory() instanceof PmdDataFactory == false){
      return null;
    }
    return ((PmdDataFactory) this.report.getDataFactory()).getXmiFile();
    
  }
  
  public void publish(){
    
  }
  
  public String getFileName(){
    return fileName;
  }
  
  public void setFileName(String fileName){
    this.fileName = fileName;
    
  }
  
  public void loadReport(){
    viewer.setMasterReport(report);
  }
  
  public void refresh(){
    loadReport();
  }
  

  public void togglePropertiesPanel(){
    setPropVisible(! isPropVisible());
  }
  
  
  private boolean propVisible = true;
  public boolean isPropVisible(){
    return propVisible;
  }
  
  public void setPropVisible(boolean vis){
    boolean prevVal = propVisible;
    this.propVisible = vis;
    this.firePropertyChange("propVisible", prevVal, vis);
  }
}
