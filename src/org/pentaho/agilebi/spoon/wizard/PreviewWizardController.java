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
package org.pentaho.agilebi.spoon.wizard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.spoon.visualizations.VisualizationManager;
import org.pentaho.agilebi.spoon.visualizations.prpt.PRPTVisualization;
import org.pentaho.agilebi.spoon.visualizations.xul.PrptViewerTag;
import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.wizard.WizardProcessor;
import org.pentaho.reporting.engine.classic.wizard.WizardProcessorUtil;
import org.pentaho.reporting.engine.classic.wizard.model.WizardSpecification;
import org.pentaho.reporting.engine.classic.wizard.ui.xul.WizardEditorModel;
import org.pentaho.reporting.engine.classic.wizard.ui.xul.components.LinearWizardController;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulRoot;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class PreviewWizardController extends LinearWizardController {

	private static Log logger = LogFactory.getLog(PreviewWizardController.class);
	private XulDomContainer theXulContainer;
	private boolean tempModel;

	public PreviewWizardController(WizardEditorModel aModel, DefaultBindingFactory aFactory, boolean tempModel) {
		super(aModel, aFactory);
		this.tempModel = tempModel;
	}

	
	public void preview() {
		try {
			AbstractReportDefinition element = processWizardSpecification();
			SwtXulLoader theXulLoader = new SwtXulLoader();
      theXulLoader.registerClassLoader(getClass().getClassLoader());
      theXulLoader.setOuterContext(((XulRoot) document.getRootElement()).getRootObject());
			theXulLoader.register("PRPT", "org.pentaho.agilebi.spoon.visualizations.xul.PrptViewerTag");
			theXulContainer = theXulLoader.loadXul("org/pentaho/agilebi/pdi/wizard/prptPreview.xul");
			theXulContainer.addEventHandler(new PreviewCloseHandler());
			
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
	
	public void finish() {
		try {

			MasterReport element = (MasterReport) processWizardSpecification();
			element = WizardProcessorUtil.materialize(element, new WizardProcessor());
			VisualizationManager theManager = VisualizationManager.getInstance();
			PRPTVisualization theVisualization = (PRPTVisualization) theManager.getVisualization("Report Wizard");
			if (theVisualization != null) {
				theVisualization.createVisualizationFromMasterReport(element, tempModel);
			}
			((XulDialog) document.getElementById("main_wizard_window")).hide();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	private AbstractReportDefinition processWizardSpecification() throws Exception {

		AbstractReportDefinition reportDefinition = getEditorModel().getReportDefinition();
		AbstractReportDefinition element = (AbstractReportDefinition) reportDefinition.derive();
		final WizardSpecification spec = getEditorModel().getReportSpec();
		element.setAttribute(AttributeNames.Wizard.NAMESPACE, "enable", Boolean.TRUE);
		WizardProcessorUtil.applyWizardSpec(element, (WizardSpecification) spec.clone());
		WizardProcessorUtil.ensureWizardProcessorIsAdded(element, null);
		return element;
	}

	public static class PreviewCloseHandler extends AbstractXulEventHandler{
    public String getName(){
      return "controller";
    }

    public void close(){
      ((XulDialog) document.getElementById("prpt-preview")).hide();
    }

  }

}
