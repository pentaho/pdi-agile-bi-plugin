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
import org.pentaho.agilebi.pdi.modeler.ModelerHelper;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspaceUtil;
import org.pentaho.agilebi.pdi.perspective.AgileBiVisualizationPerspective;
import org.pentaho.agilebi.pdi.perspective.AbstractPerspective.XulTabAndPanel;
import org.pentaho.agilebi.pdi.visualizations.AbstractVisualization;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.metadata.model.Domain;
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
    str = replaceField(str, "filename", pathAndFilename[1], true); //$NON-NLS-1$
    return str;
  }

  
	public String generateOpenUrl(String filename) {
	  // path, filename
	  String pathAndFilename[] = getPathAndFilename(filename);
	  String str = replaceField(openUrl, "path", pathAndFilename[0], true); //$NON-NLS-1$
	  str = replaceField(str, "filename", pathAndFilename[1], true); //$NON-NLS-1$
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
    return str;
	}
	
	public String generateRefreshDataJavascript(String fileLocation, String modelId) { 
    String str = replaceField(refreshDataJavascript, "modelLocation", fileLocation, true);
    str = replaceField(str, "modelId", modelId, true);
    return str;
	}
	
	public String generateRefreshModelJavascript(String fileLocation, String modelId) {
	  String str = replaceField(refreshModelJavascript, "modelLocation", fileLocation, true); //$NON-NLS-1$
    str = replaceField(str, "modelId", modelId, true); //$NON-NLS-1$
    str = replaceField(str, "basedir", new File("").getAbsolutePath(), true); //$NON-NLS-1$ //$NON-NLS-2$
    return str;
	}
	
	public void createVisualizationFromModel(ModelerWorkspace model) {
		
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    try {
    	SwtXulLoader theXulLoader = new SwtXulLoader();
      AnalyzerVisualizationController theController = new AnalyzerVisualizationController(spoon.tabfolder.getSwtTabset(), this, model.getFileName(), model.getModelName() + " Cube", null);
      theController.setModel(model);
    	XulDomContainer theXulContainer = theXulLoader.loadXul(WEB_VISUALIZATION);
			theXulContainer.addEventHandler(theController);
			Composite theMainBox = (Composite) theXulContainer.getDocumentRoot().getElementById("mainVBox").getManagedObject();
			SwtXulRunner theRunner = new SwtXulRunner();
			theRunner.addContainer(theXulContainer);
			theRunner.initialize();
      createTabForBrowser(theMainBox, theController, model);      
    } catch (Throwable e) {
      throw new RuntimeException(e);
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
      FileInputStream in = new FileInputStream( f );
      StringBuilder sb = new StringBuilder();
      byte b[] = new byte[2048];
      int n = in.read(b);
      while( n != -1 ) {
        sb.append( new String(b, 0, n) );
        n = in.read(b);
      }
      
      Document doc = DocumentHelper.parseText(sb.toString());
      Dom4jXPath xpath = new Dom4jXPath( "//@catalog");
      Dom4jXPath xpath2 = new Dom4jXPath( "//@cube");
      HashMap map = new HashMap();
      map.put( "pho", "http://www.pentaho.com");
      xpath.setNamespaceContext( new SimpleNamespaceContext( map));
      xpath2.setNamespaceContext( new SimpleNamespaceContext( map));
      org.dom4j.Node node = (org.dom4j.Node) xpath.selectSingleNode( doc);
      org.dom4j.Node node2 = (org.dom4j.Node) xpath2.selectSingleNode( doc);

      String modelFileName = node.getText();
      String modelId = node2.getText();            
      
      SwtXulLoader theXulLoader = new SwtXulLoader();
      AnalyzerVisualizationController theController = new AnalyzerVisualizationController(spoon.tabfolder.getSwtTabset(), this, modelFileName, modelId, null);
    	XulDomContainer theXulContainer = theXulLoader.loadXul(WEB_VISUALIZATION, new PDIMessages(IVisualization.class));
			theXulContainer.addEventHandler(theController);
			Composite theMainBox = (Composite) theXulContainer.getDocumentRoot().getElementById("mainVBox").getManagedObject();
			SwtXulRunner theRunner = new SwtXulRunner();
			theRunner.addContainer(theXulContainer);
			theRunner.initialize();
			
	  	Domain domain = ModelerHelper.getInstance().loadDomain(modelFileName);
			ModelerWorkspace model = new ModelerWorkspace();
			model.setDomain(domain);
			model.setModelName(modelId);
			model.setFileName(modelFileName);	
			theController.setModel(model);
      createTabForBrowser(theMainBox, theController, model);      
      
      String fullPath = f.getAbsolutePath();
      spoon.getProperties().addLastFile("Analyzer", fullPath, null, false, null);
      spoon.addMenuLast();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }    return true;
  }

  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    AnalyzerVisualizationMeta wvmeta = (AnalyzerVisualizationMeta)meta;
    wvmeta.save(fname);
    File f = new File(fname);
    String fullPath = f.getAbsolutePath();
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    spoon.getProperties().addLastFile("Model", fullPath, null, false, null);
    spoon.addMenuLast();
    AgileBiVisualizationPerspective.getInstance().setNameForTab(wvmeta.getTab(), fname);

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
    ResourceBundle bundle = ResourceBundle.getBundle("org/pentaho/agilebi/pdi/visualizations/analyzer/analyzer_visualization_browser", locale);
    return new String[]{bundle.getString("AnalyzerViz.fileTypeName")};
  }

  public String getRootNodeName() {
    return null;
  }

  public String[] getSupportedExtensions() {
    return new String[]{"xanalyzer"};
  }
  
}
