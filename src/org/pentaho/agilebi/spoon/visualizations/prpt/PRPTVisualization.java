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
package org.pentaho.agilebi.spoon.visualizations.prpt;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.util.ModelerSourceFactory;
import org.pentaho.agilebi.spoon.ModelerHelper;
import org.pentaho.agilebi.spoon.PDIMessages;
import org.pentaho.agilebi.spoon.SpoonModelerWorkspaceHelper;
import org.pentaho.agilebi.spoon.perspective.AbstractPerspective.XulTabAndPanel;
import org.pentaho.agilebi.spoon.perspective.AgileBiVisualizationPerspective;
import org.pentaho.agilebi.spoon.visualizations.AbstractVisualization;
import org.pentaho.agilebi.spoon.visualizations.IVisualization;
import org.pentaho.agilebi.spoon.wizard.EmbeddedWizard;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.registry.Entity;
import org.pentaho.metadata.registry.IMetadataRegistry;
import org.pentaho.metadata.registry.Link;
import org.pentaho.metadata.registry.RegistryFactory;
import org.pentaho.metadata.registry.Type;
import org.pentaho.metadata.registry.Verb;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.fonts.LibFontBoot;
import org.pentaho.reporting.libraries.resourceloader.LibLoaderBoot;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.components.WaitBoxRunnable;
import org.pentaho.ui.xul.components.XulWaitBox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class PRPTVisualization extends AbstractVisualization {

  public PRPTVisualization(){
    super();
    System.setProperty("org.jpedal.suppressViewerPopups", "true");
  }
  
  private static Logger logger = LoggerFactory.getLogger(PRPTVisualization.class);
  
  private String callerId =null;
  
  public void createVisualizationFromModel(String modelFileLocation, String modelId) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getId() {
	  return "REPORTING";
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
    try {
      
      if(ClassicEngineBoot.getInstance().isBootDone() == false){

        ObjectUtilities.setClassLoader(getClass().getClassLoader());
        ObjectUtilities.setClassLoaderSource(ObjectUtilities.CLASS_CONTEXT);
        
        LibLoaderBoot.getInstance().start();
        LibFontBoot.getInstance().start();
        ClassicEngineBoot.getInstance().start();
      }      
      
      ResourceManager theResourceManager = new ResourceManager();
      theResourceManager.registerDefaults();
      File theReportFile = new File(fname);
      Resource theResource = theResourceManager.createDirectly(theReportFile, MasterReport.class);
      MasterReport masterReport = (MasterReport) theResource.getResource();
      
      
      XulTabAndPanel tabAndPanel = AgileBiVisualizationPerspective.getInstance().createTab();
      AgileBiVisualizationPerspective.getInstance().setNameForTab(tabAndPanel.tab, getPathAndFilename(fname)[1].replace(".prpt", "")); //$NON-NLS-1$ //$NON-NLS-2$
  
      SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiVisualizationPerspective.class);
      SwtXulLoader theXulLoader = new SwtXulLoader();
      theXulLoader.registerClassLoader(getClass().getClassLoader());
      theXulLoader.register("PRPT", "org.pentaho.agilebi.spoon.visualizations.xul.PrptViewerTag"); //$NON-NLS-1$ //$NON-NLS-2$
      XulDomContainer theXulContainer = theXulLoader.loadXul("org/pentaho/agilebi/spoon/visualizations/prpt/prptVisualization.xul", new PDIMessages(IVisualization.class)); //$NON-NLS-1$
      
      PRPTMeta meta = new PRPTMeta();
      meta.setTab(tabAndPanel.tab);
      AgileBiVisualizationPerspective.getInstance().setMetaForTab(tabAndPanel.tab, meta);
      
      PRPTVisualizationController controller = new PRPTVisualizationController(meta, masterReport);
      controller.setFileName(fname);
      controller.setDirty(false);
      PmdDataFactory theDataFactory = (PmdDataFactory) masterReport.getDataFactory();
      String theXmiFile = theDataFactory.getXmiFile();
      
      ModelerWorkspace model = new ModelerWorkspace(new SpoonModelerWorkspaceHelper(), SpoonModelerWorkspaceHelper.initGeoContext());
      XmiParser parser = new XmiParser();
      FileInputStream inputStream = new FileInputStream(new File(theXmiFile));
      Domain domain = parser.parseXmi(inputStream);
      inputStream.close();

      LogicalModel logical = domain.getLogicalModels().get(0);
      Object property = logical.getProperty("source_type"); //$NON-NLS-1$
      if( property != null ) {
        IModelerSource theSource = ModelerSourceFactory.generateSource(property.toString());
        theSource.initialize(domain);   
        model.setModelSource(theSource);
      }
  
      model.setDomain(domain);
      model.setModelName(domain.getId());
      model.setFileName(theXmiFile); 
      model.setTemporary(false);
      controller.setModel(model);
      theXulContainer.addEventHandler(controller);
      
      Composite theMainBox = (Composite) theXulContainer.getDocumentRoot().getElementById("mainContainer").getManagedObject(); //$NON-NLS-1$
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
      prptMeta.controller.setDirty(false);

      if(fname.endsWith(".prpt") == false){
        fname +=".prpt";
      }
      File f = new File(fname);
      String fullPath = f.getAbsolutePath();
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      spoon.getProperties().addLastFile("Model", fullPath, null, false, null);
      String name = getPathAndFilename(fname)[1].replace(".prpt", "");
      AgileBiVisualizationPerspective.getInstance().setNameForTab(prptMeta.getTab(), fname);
      AgileBiVisualizationPerspective.getInstance().setNameForTab(prptMeta.getTab(), name);
      
      // register this in the metadata registry
      RegistryFactory factory = RegistryFactory.getInstance();
      IMetadataRegistry registry = factory.getMetadataRegistry();
      Entity vizEntity = new Entity(fname, name, Type.TYPE_REPORT.getId());
      registry.addEntity(vizEntity);
      
      String modelId = prptMeta.controller.getModel().getFileName();
      Entity modelEntity = registry.getEntity(modelId, Type.TYPE_OLAP_MODEL.getId());
      if( modelEntity != null ) {
          Link link = new Link( vizEntity, Verb.VERB_USES, modelEntity );
          registry.addLink(link);
      }
      
      try {
  		registry.commit();
  	} catch (Exception e) {
  		logger.error("Could not commit metadata registry", e);
  	}								
      
      
      spoon.addMenuLast();
      return true;
    } catch(ModelerException e){
      logger.error("error saving", e);
      return false;
    }
  }

  public void syncMetaName(EngineMetaInterface meta, String name) {
  }

  public void createVisualizationFromModel( ModelerWorkspace model, final boolean tempModel) {


    
    XulWaitBox box;
    try {

      if(tempModel){
        ModelerHelper theHelper = ModelerHelper.getInstance();
        model = theHelper.clone(model);
      }
      final ModelerWorkspace fModel = model;
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      Document document = spoon.getMainSpoonContainer().getDocumentRoot();
      
      box = (XulWaitBox) document.createElement("waitbox");
      box.setIndeterminate(true);
      box.setMaximum(10);
      box.setCanCancel(false);
      box.setTitle(BaseMessages.getString(ModelerWorkspace.class, "wait_dialog_title"));
      box.setMessage(BaseMessages.getString(ModelerWorkspace.class, "wait_dialog_message"));
      
      box.setCancelLabel(BaseMessages.getString(ModelerWorkspace.class, "wait_dialog_btn"));
      
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
            EmbeddedWizard wizard = new EmbeddedWizard(fModel, tempModel);
            waitBox.stop();
            wizard.run(null);
          } catch (final Exception e) {
            logger.error("Error initializing reporting", e);
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
    } catch (Exception e1) {
      logger.error("Error creating visualization", e1);
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e1);
    }
    
  }
  
  public void createVisualizationFromMasterReport(MasterReport rpt, boolean tempModel){
    try{
      
      if(ClassicEngineBoot.getInstance().isBootDone() == false){
        LibLoaderBoot.getInstance().start();
        LibFontBoot.getInstance().start();
        ClassicEngineBoot.getInstance().start();
      }
      
      XulTabAndPanel tabAndPanel = AgileBiVisualizationPerspective.getInstance().createTab();      
      AgileBiVisualizationPerspective.getInstance().setNameForTab(tabAndPanel.tab, "Untitled Report"); //$NON-NLS-1$
     
      SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiVisualizationPerspective.class);
      SwtXulLoader theXulLoader = new SwtXulLoader();
      theXulLoader.registerClassLoader(getClass().getClassLoader());
      theXulLoader.register("PRPT", "org.pentaho.agilebi.spoon.visualizations.xul.PrptViewerTag"); //$NON-NLS-1$ //$NON-NLS-2$
      XulDomContainer theXulContainer = theXulLoader.loadXul("org/pentaho/agilebi/spoon/visualizations/prpt/prptVisualization.xul", new PDIMessages(IVisualization.class)); //$NON-NLS-1$
      Composite theMainBox = (Composite) theXulContainer.getDocumentRoot().getElementById("mainContainer").getManagedObject(); //$NON-NLS-1$
      
      PRPTMeta meta = new PRPTMeta();
      AgileBiVisualizationPerspective.getInstance().setMetaForTab(tabAndPanel.tab, meta);
      meta.setTab(tabAndPanel.tab);
      
      PRPTVisualizationController controller = new PRPTVisualizationController(meta, rpt);  
      
      PmdDataFactory theDataFactory = (PmdDataFactory) rpt.getDataFactory();
      String theXmiFile = theDataFactory.getXmiFile();
      
      XmiParser parser = new XmiParser();
      FileInputStream inputStream = new FileInputStream(new File(theXmiFile));
      Domain domain = parser.parseXmi(inputStream);
      inputStream.close();      
      
      ModelerWorkspace model = new ModelerWorkspace(new SpoonModelerWorkspaceHelper(), SpoonModelerWorkspaceHelper.initGeoContext());
      LogicalModel logical = domain.getLogicalModels().get(0);
      Object property = logical.getProperty("source_type"); //$NON-NLS-1$
      if( property != null ) {
        IModelerSource theSource = ModelerSourceFactory.generateSource(property.toString());
        theSource.initialize(domain);   
        model.setModelSource(theSource);
      }
      
      model.setDomain(domain);
      model.setModelName(domain.getLogicalModels().get(0).getName(LocalizedString.DEFAULT_LOCALE));
      model.setFileName(theXmiFile); 
      model.setTemporary(tempModel);
      controller.setModel(model);
      theXulContainer.addEventHandler(controller);
      
      SwtXulRunner theRunner = new SwtXulRunner();
      theRunner.addContainer(theXulContainer);
      theRunner.initialize();
      theMainBox.setParent((Composite) tabAndPanel.panel.getManagedObject());
      
      ((Composite) tabAndPanel.panel.getManagedObject()).layout(true);
      AgileBiVisualizationPerspective.getInstance().setSelectedMeta(meta);
      Spoon.getInstance().enableMenus();
      
    } catch(Exception e){
      logger.error("Error creating visualization", e);
    }
  }
  public String[] getPathAndFilename(String filename) {
    int loc = filename.lastIndexOf(File.separator);
    String path = ""; //$NON-NLS-1$
    String fname = ""; //$NON-NLS-1$
    if (loc == -1) {
      fname = filename;
    } else {
      path = filename.substring(0, loc);
      fname = filename.substring(loc + 1);
    }
    return new String[]{path, fname};
  }
  
  @Override
  public void setCaller( String callerId ) {
	  this.callerId = callerId;	  
  }
  
}
