package org.pentaho.agilebi.pdi.wizard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
import org.pentaho.agilebi.pdi.visualizations.prpt.PRPTVisualization;
import org.pentaho.agilebi.pdi.visualizations.xul.PrptViewerTag;
import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.wizard.WizardProcessorUtil;
import org.pentaho.reporting.engine.classic.wizard.model.WizardSpecification;
import org.pentaho.reporting.engine.classic.wizard.ui.xul.WizardEditorModel;
import org.pentaho.reporting.engine.classic.wizard.ui.xul.components.LinearWizardController;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class PreviewWizardController extends LinearWizardController {

	private static Log logger = LogFactory.getLog(PreviewWizardController.class);

	public PreviewWizardController(WizardEditorModel aModel, DefaultBindingFactory aFactory) {
		super(aModel, aFactory);
	}

	public void preview() {
		try {
			AbstractReportDefinition reportDefinition = getEditorModel().getReportDefinition();
			AbstractReportDefinition element = (AbstractReportDefinition) reportDefinition.derive();
			final WizardSpecification spec = getEditorModel().getReportSpec();
			element.setAttribute(AttributeNames.Wizard.NAMESPACE, "enable", Boolean.TRUE);
			WizardProcessorUtil.applyWizardSpec(element, (WizardSpecification) spec.clone());
			WizardProcessorUtil.ensureWizardProcessorIsAdded(element, null);

			SwtXulLoader theXulLoader = new SwtXulLoader();
			theXulLoader.register("PRPT", "org.pentaho.agilebi.pdi.visualizations.xul.PrptViewerTag");
			XulDomContainer theXulContainer = theXulLoader.loadXul("org/pentaho/agilebi/pdi/wizard/prptPreview.xul");

			SwtXulRunner theRunner = new SwtXulRunner();
			theRunner.addContainer(theXulContainer);
			theRunner.initialize();

			PrptViewerTag thePrptViewerTag = (PrptViewerTag) theXulContainer.getDocumentRoot().getElementById("prptViewer");
			thePrptViewerTag.setMasterReport((MasterReport) element);

			XulDialog theDialog = (XulDialog) theXulContainer.getDocumentRoot().getElementById("prpt-preview");
			theDialog.show();

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

  @Override
  public void finish() {
    try {
      AbstractReportDefinition reportDefinition = getEditorModel().getReportDefinition();
      MasterReport element = (MasterReport) reportDefinition.derive();
      final WizardSpecification spec = getEditorModel().getReportSpec();
      element.setAttribute(AttributeNames.Wizard.NAMESPACE, "enable", Boolean.TRUE);
      WizardProcessorUtil.applyWizardSpec(element, (WizardSpecification) spec.clone());
      WizardProcessorUtil.ensureWizardProcessorIsAdded(element, null);
      
      VisualizationManager theManager = VisualizationManager.getInstance();
      PRPTVisualization theVisualization = (PRPTVisualization) theManager.getVisualization("PRPT Viewer");
      if(theVisualization != null) {
        theVisualization.createVisualizationFromMasterReport(element);
        
      }
      
      ((XulDialog) document.getElementById("main_wizard_window")).hide();
      
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      logger.error(e);
    }
    
  }
	
	
}
