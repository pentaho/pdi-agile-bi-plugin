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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.agilebi.pdi.wizard.ui.xul.steps;

import java.io.File;
import java.io.IOException;

import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspaceUtil;
import org.pentaho.agilebi.pdi.wizard.ui.xul.res.Messages;
import org.pentaho.commons.metadata.mqleditor.editor.SwtMqlEditor;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.pms.core.exception.PentahoMetadataException;
import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.CompoundDataFactory;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.states.datarow.StaticDataRow;
import org.pentaho.reporting.engine.classic.core.wizard.DataSchemaModel;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.IPmdConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.engine.classic.wizard.model.DetailFieldDefinition;
import org.pentaho.reporting.engine.classic.wizard.model.GroupDefinition;
import org.pentaho.reporting.engine.classic.wizard.ui.xul.WizardEditorModel;
import org.pentaho.reporting.engine.classic.wizard.ui.xul.components.AbstractWizardStep;
import org.pentaho.reporting.libraries.base.util.DebugLog;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

/**
 * TODO: Document Me
 *
 * @author William Seyler
 */
public class DataSourceAndQueryStep extends AbstractWizardStep
{

  protected class DatasourceAndQueryStepHandler extends AbstractXulEventHandler
  {
    /**
     * 
     */

    public DatasourceAndQueryStepHandler()
    {
    }

    public String getName()
    {
      return HANDLER_NAME;
    }

    private IMetadataDomainRepository getDomainRepo() throws ReportDataFactoryException, IOException{

      IPmdConnectionProvider connectionProvider = ((PmdDataFactory) getEditorModel().getReportDefinition().getDataFactory()).getConnectionProvider();
      IMetadataDomainRepository repo = connectionProvider.getMetadataDomainRepository(DEFAULT, getEditorModel().getReportDefinition().getResourceManager(), getEditorModel().getReportDefinition().getContentBase(), df.getXmiFile());
      
      return repo;
    }
    
    public void doEditQuery() {
      try {

        IMetadataDomainRepository repo = getDomainRepo();
        
        SwtMqlEditor editor = new SwtMqlEditor(repo);
        String queryString = null;
        if (df != null && df.getQuery(DEFAULT) != null) {
          queryString = df.getQuery(DEFAULT);
          editor.setQuery(queryString);
        }
        editor.show();
        if (editor.getOkClicked()) {
          queryString = editor.getQuery();
          df.setQuery(DEFAULT, queryString);
          setCurrentQuery(DEFAULT);
        }
      } catch (ReportDataFactoryException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (PentahoMetadataException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private static final String DATASOURCE_AND_QUERY_STEP_OVERLAY = "org/pentaho/agilebi/pdi/wizard/ui/xul/res/datasource_and_query_step_Overlay.xul"; //$NON-NLS-1$
  private static final String HANDLER_NAME = "datasource_and_query_step_handler"; //$NON-NLS-1$

  private static final String CURRENT_QUERY_PROPERTY_NAME = "currentQuery"; //$NON-NLS-1$
  private static final String DATA_SOURCE_NAME_LABEL_ID = "data_source_name_label";  //$NON-NLS-1$
//  private static final String CREATE_QUERY_BTN_ID = "create_query_btn"; //$NON-NLS-1$
  private static final String EDIT_QUERY_BTN_ID = "edit_query_btn"; //$NON-NLS-1$

  private static final String DEFAULT = "default"; //$NON-NLS-1$

//  private DatasourceModelNode dataSourcesRoot;
//  private CompoundDataFactory cdf;
  PmdDataFactory df;
  private ModelerWorkspace model;
  private File modelFile;

  public DataSourceAndQueryStep()
  {
    super();
  }

  public void stepActivating()
  {
    super.stepActivating();
    if (model != null && df == null) {
      
      // Populate a PmdDataFactoryClass for the report definition to use
      File modelsDir = new File("models"); //$NON-NLS-1$
      modelsDir.mkdirs();
      int idx = 1;
      boolean looking = true;
      String fileName = ""; //$NON-NLS-1$
      String modelName = ""; //$NON-NLS-1$
      while( looking ) {
        modelName = "Model "+idx; //$NON-NLS-1$
        fileName = "models/"+modelName+".xmi"; //$NON-NLS-1$ //$NON-NLS-2$
        modelFile = new File(fileName);
        if( !modelFile.exists() ) {
          looking = false;
        }
        idx++;
      }
      model.setFileName(fileName);
      model.setModelName(modelName);
      
      try {
        ModelerWorkspaceUtil.autoModelFlat(model);
        ModelerWorkspaceUtil.saveWorkspace( model, fileName);
      } catch (ModelerException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      
      if (getEditorModel().getReportDefinition().getDataFactory() != null && getEditorModel().getReportDefinition().getDataFactory() instanceof CompoundDataFactory) {
        CompoundDataFactory cdf = (CompoundDataFactory) getEditorModel().getReportDefinition().getDataFactory();
        for (int i=0; i<cdf.size(); i++) {
          cdf.remove(i);
        }
      }

      df = new PmdDataFactory();
      PmdConnectionProvider connectionProvider = new PmdConnectionProvider();
      df.setConnectionProvider(connectionProvider);
      try {
        df.setXmiFile(modelFile.getCanonicalPath());
      } catch (IOException e) {
        e.printStackTrace();
      }
      df.setDomainId(DEFAULT);
      getEditorModel().getReportDefinition().setDataFactory(df);
      
    } else { // editing existing
      df = (PmdDataFactory) getEditorModel().getReportDefinition().getDataFactory();
    }
    
    updateGui();
    
    setValid(validateStep());
  }

  private void updateGui() {
    XulLabel datasourceLabel = (XulLabel) getDocument().getElementById(DATA_SOURCE_NAME_LABEL_ID);
    datasourceLabel.setValue(modelFile.getName().substring(0, modelFile.getName().lastIndexOf('.')));
  }
  
  protected boolean validateStep()
  {
    // If we have no createdDataFactory and we don't have anything in the model then we can't continue
    final AbstractReportDefinition reportDefinition = getEditorModel().getReportDefinition();
    if (reportDefinition.getDataFactory() == null ||
        StringUtils.isEmpty(reportDefinition.getQuery()))
    {
      DebugLog.log("Have no query or no datafactory " + //$NON-NLS-1$
          reportDefinition.getDataFactory() + " " + reportDefinition.getQuery()); //$NON-NLS-1$
      return false;
    }

    // if we have a DataFactory and a query make sure that they are contained in cdf.
    final String queryName = reportDefinition.getQuery();
    if (df == null || df.isQueryExecutable(queryName, new StaticDataRow()) == false)
    {
      return false;
    }

    try
    {
      final AbstractReportDefinition abstractReportDefinition =
          (AbstractReportDefinition) reportDefinition.derive();
      abstractReportDefinition.setDataFactory(df);
      final DataSchemaModel schemaModel = WizardEditorModel.compileDataSchemaModel(abstractReportDefinition);
      return schemaModel.isValid();
    }
    catch (Exception ee)
    {
      getDesignTimeContext().userError(ee);
      return false;
    }
  }

  public void createPresentationComponent(XulDomContainer mainWizardContainer) throws XulException
  {
    super.createPresentationComponent(mainWizardContainer);

    mainWizardContainer.loadOverlay(DATASOURCE_AND_QUERY_STEP_OVERLAY);
    mainWizardContainer.addEventHandler(new DatasourceAndQueryStepHandler());
  }

  public String getCurrentQuery()
  {
    return getEditorModel().getReportDefinition().getQuery();
  }

  public void setCurrentQuery(String currentQuery)
  {
    String oldQuery = getCurrentQuery();
    getEditorModel().getReportDefinition().setQuery(currentQuery);
    clearGroupsAndFields();
    this.firePropertyChange(CURRENT_QUERY_PROPERTY_NAME, oldQuery, currentQuery);
    updateGui();
    this.setValid(validateStep());
  }
  
  private void clearGroupsAndFields() {
    getEditorModel().getReportSpec().setGroupDefinitions(new GroupDefinition[0]);
    getEditorModel().getReportSpec().setDetailFieldDefinitions(new DetailFieldDefinition[0]);
  }

  protected void setValid(final boolean valid) {
    XulButton nextButton = (XulButton) getDocument().getElementById("next_btn"); //$NON-NLS-1$
    nextButton.setDisabled(!valid);
  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardStep#getStepName()
   */
  public String getStepName()
  {
    return Messages.getString("DataSourceAndQueryStep.name"); //$NON-NLS-1$
  }

  public void setModel(ModelerWorkspace model) {
    this.model = model;
  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardStep#setBindings()
   */
  public void setBindings() {
    // TODO Auto-generated method stub
    
  }
}
