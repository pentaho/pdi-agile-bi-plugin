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
import org.pentaho.commons.metadata.mqleditor.editor.SwtMqlEditor;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.CompoundDataFactory;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.states.datarow.StaticDataRow;
import org.pentaho.reporting.engine.classic.core.wizard.DataSchemaModel;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
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
//  private enum DATASOURCE_TYPE
//  {
//    ROOT, DATAFACTORY, CONNECTION, QUERY
//  }

  protected class DatasourceAndQueryStepHandler extends AbstractXulEventHandler
  {
    public DatasourceAndQueryStepHandler()
    {
    }

    public String getName()
    {
      return HANDLER_NAME;
    }

    public void doCreateQuery() {
      try {
        if (getEditorModel().getReportDefinition().getDataFactory() != null && getEditorModel().getReportDefinition().getDataFactory() instanceof CompoundDataFactory) {
          CompoundDataFactory cdf = (CompoundDataFactory) getEditorModel().getReportDefinition().getDataFactory();
          for (int i=0; i<cdf.size(); i++) {
            cdf.remove(i);
          }
        }
        df = new PmdDataFactory();
        PmdConnectionProvider connectionProvider = new PmdConnectionProvider();
        IMetadataDomainRepository repo = connectionProvider.getMetadataDomainRepository("default", getEditorModel().getReportDefinition().getResourceManager(), getEditorModel().getReportDefinition().getContentBase(), modelFile.getCanonicalPath());
        SwtMqlEditor editor = new SwtMqlEditor(repo);
        editor.show();
        String queryString = editor.getQuery();
        df.setConnectionProvider(connectionProvider);
        df.setXmiFile(modelFile.getCanonicalPath());
        df.setDomainId("default");
        df.setQuery("default", queryString);
        getEditorModel().getReportDefinition().setDataFactory(df);
        setCurrentQuery("default");
      } catch (ReportDataFactoryException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    public void doEditQuery() {
      System.out.println("In 'doEditQuery()'");
    }
  }


//  protected class DatasourceModelNode extends AbstractModelNode<DatasourceModelNode>
//  {
//    private DATASOURCE_TYPE type;
//
//    private String value;
//    private Object userObject;
//
//    public DatasourceModelNode(String value, Object userObject, DATASOURCE_TYPE type)
//    {
//      this.value = value;
//      this.userObject = userObject;
//      this.type = type;
//    }
//
//    public String getValue()
//    {
//      return value;
//    }
//
//    public void setValue(String value)
//    {
//      String oldValue = this.value;
//      this.value = value;
//
//      this.firePropertyChange(VALUE_PROPERTY_NAME, oldValue, value);
//    }
//
//    public DATASOURCE_TYPE getType()
//    {
//      return type;
//    }
//
//    public void setType(DATASOURCE_TYPE type)
//    {
//      this.type = type;
//    }
//
//    public Object getUserObject()
//    {
//      return userObject;
//    }
//
//    public void setUserObject(Object userObject)
//    {
//      this.userObject = userObject;
//    }
//  }

  private static final String DATASOURCE_AND_QUERY_STEP_OVERLAY = "org/pentaho/agilebi/pdi/wizard/ui/xul/res/datasource_and_query_step_Overlay.xul"; //$NON-NLS-1$
  private static final String HANDLER_NAME = "datasource_and_query_step_handler"; //$NON-NLS-1$

  private static final String CURRENT_QUERY_PROPERTY_NAME = "currentQuery"; //$NON-NLS-1$
  private static final String VALUE_PROPERTY_NAME = "value"; //$NON-NLS-1$
  private static final String DATA_SOURCE_NAME_LABEL_ID = "data_source_name_label";  //$NON-NLS-1$

//  private DatasourceModelNode dataSourcesRoot;
//  private CompoundDataFactory cdf;
  PmdDataFactory df;
  private ModelerWorkspace model;
  private File modelFile;

  public DataSourceAndQueryStep()
  {
    super();
  }

  public void setBindings()
  {
  }

  public void stepActivating()
  {
    super.stepActivating();
    if (model != null) {
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
    }
    
    XulLabel datasourceLabel = (XulLabel) getDocument().getElementById(DATA_SOURCE_NAME_LABEL_ID);
    datasourceLabel.setValue("default");

    setValid(validateStep());
  }

  public boolean stepDeactivating()
  {
//    getEditorModel().getReportDefinition().setDataFactory(cdf);
    return super.stepDeactivating();
  }

  protected boolean validateStep()
  {
    // If we have no createdDataFactory and we don't have anything in the model then we can't continue
    final AbstractReportDefinition reportDefinition = getEditorModel().getReportDefinition();
    if (reportDefinition.getDataFactory() == null ||
        StringUtils.isEmpty(reportDefinition.getQuery()))
    {
      DebugLog.log("Have no query or no datafactory " +
          reportDefinition.getDataFactory() + " " + reportDefinition.getQuery());
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
    if (!(currentQuery != null && currentQuery.equals(oldQuery))) {
      getEditorModel().getReportDefinition().setQuery(currentQuery);
      this.firePropertyChange(CURRENT_QUERY_PROPERTY_NAME, oldQuery, currentQuery);
    }
    this.setValid(validateStep());
  }

  protected void setValid(final boolean valid) {
    XulButton nextButton = (XulButton) getDocument().getElementById("next_btn");
    nextButton.setDisabled(!valid);
  }

//  public DatasourceModelNode getDataSourcesRoot()
//  {
//    return dataSourcesRoot;
//  }
//
  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardStep#getStepName()
   */
  public String getStepName()
  {
    return messages.getString("DATASOURCE_AND_QUERY_STEP.Step_Name"); //$NON-NLS-1$
  }

  public void setModel(ModelerWorkspace model) {
    this.model = model;
  }
}
