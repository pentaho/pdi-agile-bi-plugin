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
package org.pentaho.agilebi.pdi.visualizations.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom4j.Dom4jXPath;
import org.pentaho.agilebi.pdi.PDIMessages;
import org.pentaho.agilebi.pdi.modeler.IModelerSource;
import org.pentaho.agilebi.pdi.modeler.ModelerSourceFactory;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.perspective.AgileBiVisualizationPerspective;
import org.pentaho.agilebi.pdi.perspective.AbstractPerspective.XulTabAndPanel;
import org.pentaho.agilebi.pdi.visualizations.AbstractVisualization;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.lifecycle.pdi.AgileBILifecycleListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.w3c.dom.Node;

public class AnalyzerVisualization extends AbstractVisualization {
    
    public static final String WEB_VISUALIZATION = "org/pentaho/agilebi/pdi/visualizations/analyzer/analyzer_visualization_browser.xul";

    private String newUrl;
    private String openUrl;
    private String saveJavascript;
    
    private String refreshDataJavascript;
    private String refreshModelJavascript;
    
  private String getStateJavascript;
  private String setStateJavascript;
  private String reportName;
    
    public String getNewUrl() {
        return newUrl;
    }

    public void setNewUrl(String aUrl) {
    newUrl = aUrl;
  }
    
    public String getOpenUrl() {
      return openUrl;
    }
    
    public void setOpenUrl(String openUrl) {
      this.openUrl = openUrl;
    }
    

  public void setSaveJavascript(String saveJavascript) {
    this.saveJavascript = saveJavascript;
  }

  public String getSaveJavascript() {
    return saveJavascript;
  }
  
  public void setRefreshDataJavascript(String refreshDataJavascript) {
    this.refreshDataJavascript = refreshDataJavascript;
  }
  
  public String getRefreshDataJavascript() {
    return refreshDataJavascript;
  }
  
  public void setRefreshModelJavascript(String refreshModelJavascript) {
    this.refreshModelJavascript = refreshModelJavascript;
  }
  
  public String getRefreshModelJavascript() {
    return this.refreshModelJavascript;
  }
  
  public String generateSaveJavascript(String filename) {
    // path, filename
    String pathAndFilename[] = getPathAndFilename(filename);
    String str = replaceField(saveJavascript, "path", pathAndFilename[0], true); //$NON-NLS-1$
    str = replaceField(str, "filename", pathAndFilename[1], false); //$NON-NLS-1$
    str = replaceField(str, "port", ""+AgileBILifecycleListener.consolePort, false); //$NON-NLS-1$
    return str;
  }

  
    public String generateOpenUrl(String filename) {
      // path, filename
      String pathAndFilename[] = getPathAndFilename(filename);
      String str = replaceField(openUrl, "path", pathAndFilename[0], true); //$NON-NLS-1$
      str = replaceField(str, "filename", pathAndFilename[1], true); //$NON-NLS-1$
      str = replaceField(str, "port", ""+AgileBILifecycleListener.consolePort, false); //$NON-NLS-1$
      return str;
    }
    
    private String replaceField(String str, String fieldName, String value, boolean urlEncode) {
      if (urlEncode) {
        value = URLEncoder.encode(value);
      }
      return str.replaceAll("\\$\\{"+fieldName+"\\}", value); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public String generateNewUrl(String fileLocation, String modelId) {
    String str = replaceField(newUrl, "modelLocation", fileLocation, true); //$NON-NLS-1$
    str = replaceField(str, "modelId", modelId, true); //$NON-NLS-1$
      str = replaceField(str, "port", ""+AgileBILifecycleListener.consolePort, false); //$NON-NLS-1$
    return str;
    }
    
    public String generateRefreshDataJavascript(String fileLocation, String modelId) { 
    String str = replaceField(refreshDataJavascript, "modelLocation", fileLocation, true);
    str = replaceField(str, "modelId", modelId, true);
      str = replaceField(str, "port", ""+AgileBILifecycleListener.consolePort, false); //$NON-NLS-1$
    return str;
    }
    
    public String generateRefreshModelJavascript(String fileLocation, String modelId) {
      String str = replaceField(refreshModelJavascript, "modelLocation", fileLocation, true); //$NON-NLS-1$
      str = str.replaceAll("tmpview", reportName); //$NON-NLS-1$
      str = replaceField(str, "modelId", modelId, true); //$NON-NLS-1$
      str = replaceField(str, "port", ""+AgileBILifecycleListener.consolePort, false); //$NON-NLS-1$
    str = replaceField(str, "basedir", new File("").getAbsolutePath(), true); //$NON-NLS-1$ //$NON-NLS-2$
    return str;
    }
    
    public void createVisualizationFromModel(ModelerWorkspace model) {
        
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    try {
        SwtXulLoader theXulLoader = new SwtXulLoader();

      theXulLoader.registerClassLoader(getClass().getClassLoader());
      
      String theFileName = model.getFileName();
      if(theFileName == null) {
        theFileName = "models/" + model.getModelName() + ".xmi";
      }

      // flush the cache before creating an analyzer visualization
      flushAnalyzerCache();
      
      AnalyzerVisualizationController theController = new AnalyzerVisualizationController(spoon.tabfolder.getSwtTabset(), this, theFileName, model.getModelName(), null, null);
      theController.setModel(model);
        XulDomContainer theXulContainer = theXulLoader.loadXul(WEB_VISUALIZATION, new PDIMessages(IVisualization.class));
            theXulContainer.addEventHandler(theController);
            Composite theMainBox = (Composite) theXulContainer.getDocumentRoot().getElementById("mainVBox").getManagedObject();
            SwtXulRunner theRunner = new SwtXulRunner();
            theRunner.addContainer(theXulContainer);
            theRunner.initialize();
      createTabForBrowser(theMainBox, theController, model);   
      reportName = "Unsaved Report"; //$NON-NLS-1$
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
    
    private void flushAnalyzerCache() {
    ICacheManager cacheMgr = PentahoSystem.getCacheManager(null);
    if (cacheMgr != null) {
      cacheMgr.clearRegionCache("mondrian-catalog-cache"); //$NON-NLS-1$
    }
    }

    private void createTabForBrowser(Composite composite, AnalyzerVisualizationController controller, ModelerWorkspace model) throws KettleException {

    SpoonPerspectiveManager.getInstance().activatePerspective(AgileBiVisualizationPerspective.class);
    XulTabAndPanel tabAndPanel = AgileBiVisualizationPerspective.getInstance().createTab();

    GridData layoutData = new GridData();

    layoutData.verticalAlignment = SWT.FILL;
    layoutData.grabExcessVerticalSpace = true;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.grabExcessHorizontalSpace = true;
    
    composite.setLayoutData(layoutData);
    
    Composite parentComposite = (Composite) tabAndPanel.panel.getManagedObject();
    composite.setParent(parentComposite);
    parentComposite.layout(true);
    controller.getMeta().setTab(tabAndPanel.tab);
    AgileBiVisualizationPerspective.getInstance().setNameForTab(tabAndPanel.tab, controller.getMeta().getName());
    AgileBiVisualizationPerspective.getInstance().setMetaForTab(tabAndPanel.tab, controller.getMeta());
    
    AgileBiVisualizationPerspective.getInstance().setModel(model);    
    AgileBiVisualizationPerspective.getInstance().setSelectedMeta(controller.getMeta());
    Spoon.getInstance().enableMenus();
    }
    
    public static Document getXAnalyzerDocument( File file ) throws Exception {
      
    FileInputStream in = new FileInputStream( file );
    StringBuilder sb = new StringBuilder();
    byte b[] = new byte[2048];
    int n = in.read(b);
    while( n != -1 ) {
      sb.append( new String(b, 0, n) );
      n = in.read(b);
    }
    
    Document doc = DocumentHelper.parseText(sb.toString());

    return doc;
    }
    
    public static String getDocumentText( Document doc, String xPath ) throws JaxenException {
    Dom4jXPath xpath = new Dom4jXPath( xPath );
    HashMap<String, String> map = new HashMap<String, String>();
    map.put( "pho", "http://www.pentaho.com");  //$NON-NLS-1$//$NON-NLS-2$
    xpath.setNamespaceContext( new SimpleNamespaceContext( map));
    org.dom4j.Node node = (org.dom4j.Node) xpath.selectSingleNode( doc);
    return node.getText();
    }

     public static void setDocumentText( Document doc, String xPath, String value ) throws JaxenException {
        Dom4jXPath xpath = new Dom4jXPath( xPath );
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( "pho", "http://www.pentaho.com");  //$NON-NLS-1$//$NON-NLS-2$
        xpath.setNamespaceContext( new SimpleNamespaceContext( map));
        org.dom4j.Node node = (org.dom4j.Node) xpath.selectSingleNode( doc);
        node.setText( value );
      }

  public boolean open(Node transNode, String fname, boolean importfile) {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    try {
      File f = new File( fname );
      reportName = f.getName();
      reportName = reportName.substring(0, reportName.indexOf(".xanalyzer")); //$NON-NLS-1$
      FileInputStream in = new FileInputStream( f );
      StringBuilder sb = new StringBuilder();
      byte b[] = new byte[2048];
      int n = in.read(b);
      while( n != -1 ) {
        sb.append( new String(b, 0, n) );
        n = in.read(b);
      }
      
      Document doc = DocumentHelper.parseText(sb.toString());
      Dom4jXPath xpath = new Dom4jXPath( "//@catalog"); //$NON-NLS-1$
      Dom4jXPath xpath2 = new Dom4jXPath( "//@cube"); //$NON-NLS-1$
      HashMap map = new HashMap();
      map.put( "pho", "http://www.pentaho.com"); //$NON-NLS-1$ //$NON-NLS-2$
      xpath.setNamespaceContext( new SimpleNamespaceContext( map));
      xpath2.setNamespaceContext( new SimpleNamespaceContext( map));
      org.dom4j.Node node = (org.dom4j.Node) xpath.selectSingleNode( doc);
      org.dom4j.Node node2 = (org.dom4j.Node) xpath2.selectSingleNode( doc);

      String modelFileName = node.getText();
      String modelId = node2.getText();            
      
      SwtXulLoader theXulLoader = new SwtXulLoader();
      theXulLoader.registerClassLoader(getClass().getClassLoader());
      AnalyzerVisualizationController theController = new AnalyzerVisualizationController(spoon.tabfolder.getSwtTabset(), this, modelFileName, modelId, f.toString(), f.getName());
        XulDomContainer theXulContainer = theXulLoader.loadXul(WEB_VISUALIZATION, new PDIMessages(IVisualization.class));
            theXulContainer.addEventHandler(theController);
            Composite theMainBox = (Composite) theXulContainer.getDocumentRoot().getElementById("mainVBox").getManagedObject(); //$NON-NLS-1$
            SwtXulRunner theRunner = new SwtXulRunner();
            theRunner.addContainer(theXulContainer);
            theRunner.initialize();
            
            ModelerWorkspace model = new ModelerWorkspace();
        XmiParser parser = new XmiParser();
        FileInputStream inputStream = new FileInputStream(new File(modelFileName));
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
        model.setFileName(modelFileName); 
        model.setTemporary(false);
        theController.setModel(model);
        theXulContainer.addEventHandler(theController);
            
      createTabForBrowser(theMainBox, theController, model);

      // flush the cache before opening an analyzer visualization
      flushAnalyzerCache();
            
      theController.openReport(fname);
      
      String fullPath = f.getAbsolutePath();
      spoon.getProperties().addLastFile("Analyzer", fullPath, null, false, null); //$NON-NLS-1$
      spoon.addMenuLast();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }    return true;
  }

  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    AnalyzerVisualizationMeta wvmeta = (AnalyzerVisualizationMeta)meta;
    wvmeta.save(fname);
    File f = new File(fname);
    reportName = f.getName();
    reportName = reportName.substring(0, reportName.indexOf(".xanalyzer")); //$NON-NLS-1$
    String fullPath = f.getAbsolutePath();
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    spoon.getProperties().addLastFile("Model", fullPath, null, false, null);
    spoon.addMenuLast();
    wvmeta.setFilename(f.getAbsolutePath());
    AgileBiVisualizationPerspective.getInstance().setNameForTab(wvmeta.getTab(), getPathAndFilename(fname)[1].replace("."+this.getExtension(), ""));

    return true;
  }

  public void syncMetaName(EngineMetaInterface arg0, String arg1) {
    // TODO Auto-generated method stub
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

  public String getGetStateJavascript() {
    return getStateJavascript;
  }

  public void setGetStateJavascript(String getStateJavascript) {
    this.getStateJavascript = getStateJavascript;
  }

  public String getSetStateJavascript() {
    return setStateJavascript;
  }

  public void setSetStateJavascript(String setStateJavascript) {
    this.setStateJavascript = setStateJavascript;
  }


  public boolean accepts(String fileName) {
    if(fileName == null || fileName.indexOf('.') == -1){
      return false;
    }
    String extension = fileName.substring(fileName.lastIndexOf('.')+1);
    return extension.equals("xanalyzer");
  }

  public boolean acceptsXml(String nodeName) {
    return nodeName.equals("reportRecord");
  }

  public String[] getFileTypeDisplayNames(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("org/pentaho/agilebi/pdi/visualizations/analyzer/analyzer_visualization_browser", locale, getClass().getClassLoader());
    return new String[]{bundle.getString("AnalyzerViz.fileTypeName")};
  }

  public String getRootNodeName() {
    return null;
  }

  public String[] getSupportedExtensions() {
    return new String[]{"xanalyzer"};
  }
  
}
