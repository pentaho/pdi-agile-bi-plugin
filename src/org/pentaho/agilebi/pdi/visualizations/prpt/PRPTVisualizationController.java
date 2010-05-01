/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2010 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.pdi.visualizations.prpt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.modeler.ModelerHelper;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.XulUI;
import org.pentaho.agilebi.pdi.perspective.AgileBiModelerPerspective;
import org.pentaho.agilebi.pdi.perspective.PublisherHelper;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.PropertyPanelController;
import org.pentaho.agilebi.pdi.visualizations.xul.PrptViewerTag;
import org.pentaho.agilebi.pdi.wizard.EmbeddedWizard;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalModel;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMenuList;
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
  private XulMenuList zoomList;
  private ModelerWorkspace model;
  

  private TreeMap<Double, String> zoomMap = new TreeMap<Double, String>();
  {
    zoomMap.put(0.5, "50%");
    zoomMap.put(0.75, "75%");
    zoomMap.put(1.0, "100%");
    zoomMap.put(1.25, "125%");
    zoomMap.put(1.5, "150%");
    zoomMap.put(2.0, "200%");
  }
  
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
    this.zoomList = (XulMenuList) document.getElementById("zoomlist");

    
    // try to find the model name
    String xmiFileLocation = getXmiFileLocation();
    if(xmiFileLocation != null){

      Domain theDomain = ModelerHelper.getInstance().loadDomain(xmiFileLocation);
      modelId = theDomain.getLogicalModels().get(0).getName(Locale.getDefault().toString());
    } else {
      modelId = BaseMessages.getString(IVisualization.class,"unavailable");
    }
    bf.setBindingType(Binding.Type.ONE_WAY);
    this.modelNameBinding = this.bf.createBinding(this, "modelId", "modelName", "value");
    this.factTableNameBinding = this.bf.createBinding(this, "factTableName", "factTableName", "value");

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(this.propPanel, "visible", this, "propVisible");

    viewer = (PrptViewerTag) document.getElementById("prptViewer");
    bf.createBinding(viewer, "zoom", zoomList, "selectedIndex", new BindingConvertor<Double, Integer>(){

      @Override
      public Integer sourceToTarget(Double value) {
        return new ArrayList(zoomMap.keySet()).indexOf(viewer.getZoom());
      }

      @Override
      public Double targetToSource(Integer value) {
        switch(value){
          case 5:       // 200%
            return 2.0;
          default:      // Others are increments of 25
            return 0.5 + (0.25 *  value);
        }
      }
     
    });

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
      return BaseMessages.getString(IVisualization.class,"unavailable");
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
      showErrorDialog(BaseMessages.getString(IVisualization.class,"error_saving"));
    }
    
  }
  
  public void saveAs(){
    try {
      spoon.saveFileAs(meta);
    } catch (KettleException e) {
      logger.error(e);
      showErrorDialog(BaseMessages.getString(IVisualization.class,"error_saving"));
    }
  }
  
  private void showErrorDialog(String msg){
    XulMessageBox dlg;
    try {
      dlg = (XulMessageBox) document.createElement("messagebox");
      dlg.setTitle(BaseMessages.getString(IVisualization.class,"error_title"));
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
        box.setTitle(BaseMessages.getString(IVisualization.class,"error_title"));
        box.setMessage(BaseMessages.getString(IVisualization.class,"prpt_no_model"));
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
      AgileBiModelerPerspective.getInstance().open(null, xmiFileLocation, false);
    } else {

      XulMessageBox box;
      try {
        box = (XulMessageBox) document.createElement("messagebox");
        box.setTitle(BaseMessages.getString(IVisualization.class,"error_title"));
        box.setMessage(BaseMessages.getString(IVisualization.class,"prpt_no_model"));
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
  
  public void publish() throws ModelerException {
    
    String theXmiFile = model.getFileName();
    String comment = BaseMessages.getString(XulUI.class, "ModelServerPublish.Publish.ModelPublishComment"); //$NON-NLS-1$
    int treeDepth = 100;
    DatabaseMeta databaseMeta = model.getModelSource().getDatabaseMeta();
    boolean checkDatasources = true;
    boolean showServerSelection = true;
    boolean showFolders = true;
    boolean showCurrentFolder = false;
    String serverPathTemplate = "{path}" + ISolutionRepository.SEPARATOR + //$NON-NLS-1$
      "resources" + ISolutionRepository.SEPARATOR + "metadata"; //$NON-NLS-1$ //$NON-NLS-2$
    String databaseName = model.getDatabaseName();
    String modelName = model.getModelName();
    
    PmdDataFactory thePmdDataFactory = (PmdDataFactory) this.report.getDataFactory();
    String theOldDomainId = thePmdDataFactory.getDomainId();
    
    String thePrpt = getFileName();
    PublisherHelper.publishPrpt(this.report, model, theXmiFile, thePrpt, comment, treeDepth, databaseMeta, modelName, checkDatasources, 
        showServerSelection, showFolders, showCurrentFolder, serverPathTemplate, databaseName);
    
    thePmdDataFactory.setDomainId(theOldDomainId);
    save(thePrpt);
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
  
  public void start(){
    viewer.start();
  }
  
  public void previous(){
    viewer.previous();
  }
  
  public void next(){
    viewer.next();
  }
  
  public void last(){
    viewer.last();
  }
  
  public void zoomOut(){
    viewer.zoomOut();
  }
  
  public void zoomIn(){
    viewer.zoomIn();
  }
  
  public void setModel(ModelerWorkspace model) {
    this.model = model; 
  }
  
  public ModelerWorkspace getModel() {
    return this.model;
  }
}
