package org.pentaho.agilebi.pdi.visualizations.web;

import java.util.List;
import java.util.Locale;

import mondrian.rolap.agg.AggregationManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.agilebi.pdi.modeler.ModelerHelper;
import org.pentaho.agilebi.pdi.perspective.AgileBiPerspective;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalModel;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulBrowser;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.w3c.dom.Node;

public class WebVisualizationController extends AbstractXulEventHandler implements FileListener {

	public static final String XUL_FILE_ANALYZER_TOOLBAR_PROPERTIES = "plugins/spoon/agile-bi/ui/analyzer-toolbar.properties"; //$NON-NLS-1$

	private String xmiFileLocation = null;
	private String modelId = null;
	private WebVisualization visualization;
	private WebVisualizationMeta meta;
	private String visFileLocation = null;
	private XulBrowser browser;
	private Spoon spoon;
	private String location;
	private BindingFactory bf;
	private Binding modelNameBinding;
	private Binding factTableNameBinding;
	private String factTableName;

	private static Log logger = LogFactory.getLog(WebVisualizationController.class);

	public WebVisualizationController(Composite parent, final WebVisualization visualization, String xmiFileLocation, String modelId, String aVisFileLocaiton) throws SWTError {
		this.visualization = visualization;
		this.xmiFileLocation = xmiFileLocation;
		this.modelId = modelId;
		this.visFileLocation = aVisFileLocaiton;
		this.meta = new WebVisualizationMeta(this);
		this.spoon = ((Spoon) SpoonFactory.getInstance());
		this.location = visualization.generateNewUrl(xmiFileLocation, modelId);
		this.bf = new DefaultBindingFactory();
	}

	public void init() {
		this.browser = (XulBrowser) this.document.getElementById("web_visualization_browser");
		this.browser.setSrc(this.location);

		this.bf.setDocument(super.document);
		this.bf.setBindingType(Type.ONE_WAY);

		this.modelNameBinding = this.bf.createBinding(this, "modelId", "modelName", "value");
		this.factTableNameBinding = this.bf.createBinding(this, "factTableName", "factTableName", "value");
		fireBindings();
	}

	private String processFactTableName() {
		String theName = null;
		Domain theDomain = ModelerHelper.getInstance().loadDomain(this.xmiFileLocation);
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

	private void fireBindings() {
		try {
			this.modelNameBinding.fireSourceChanged();
			this.factTableNameBinding.fireSourceChanged();
		} catch (Exception e) {
			logger.info(e);
		}
	}

	public String getFactTableName() {
		if (this.factTableName == null) {
			this.factTableName = processFactTableName();
		}
		return this.factTableName;
	}

	public void setFactTableName(String aFactTableName) {
		this.factTableName = aFactTableName;
	}

	public WebVisualization getVisualization() {
		return visualization;
	}

	public String getVisFileLocation() {
		return visFileLocation;
	}

	public void save(String filename) {
		visFileLocation = filename;
		browser.execute(visualization.generateSaveJavascript(filename));
	}

	public void save() {
		spoon.saveToFile(meta);
	}

	public void saveAs() {
		spoon.saveFileAs(meta);
	}

	public void editModel() {
		AgileBiPerspective.getInstance().open(null, xmiFileLocation, false);

	}

	public void refreshData() {
		// first clear the server cache

		AggregationManager.instance().getCacheControl(null).flushSchemaCache();
		browser.execute(visualization.generateRefreshDataJavascript(xmiFileLocation, modelId));
	}

	public void refreshModel() {
		// first save the view
		// if (true) throw new UnsupportedOperationException();
		// TODO: can we do this without requiring a "remote save"?
		AggregationManager.instance().getCacheControl(null).flushSchemaCache();

		browser.execute(visualization.generateRefreshModelJavascript(xmiFileLocation, modelId));
		// "gCtrlr.repositoryBrowserController.remoteSave('"+modelId+"','tmp', '', 'xanalyzer', true)"

	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public EngineMetaInterface getMeta() {
		return meta;
	}

	public boolean canHandleSave() {
		return true;
	}

	public boolean setFocus() {
		return true;
	}

	public boolean open(Node arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean save(EngineMetaInterface arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public void syncMetaName(EngineMetaInterface arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void setXmiFileLocation(String xmiFileLocation) {
		this.xmiFileLocation = xmiFileLocation;
	}

	public void setVisFileLocation(String visFileLocation) {
		this.visFileLocation = visFileLocation;
	}

	// FileListener methods
	public boolean accepts(String fileName) {
		if (fileName == null || fileName.indexOf('.') == -1) {
			return false;
		}
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		return extension.equals("xanalyzer");
	}

	public boolean acceptsXml(String nodeName) {
		return nodeName.equals("reportRecord");
	}

	public String[] getFileTypeDisplayNames(Locale locale) {
		return new String[] { "Models" };
	}

	public String getRootNodeName() {
		return null;
	}

	public String[] getSupportedExtensions() {
		return new String[] { "xmi" };
	}

	public String getName() {
		return "web_visualiztion";
	}
}
