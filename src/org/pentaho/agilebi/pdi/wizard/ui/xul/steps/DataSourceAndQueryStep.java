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
import java.io.FileInputStream;
import java.io.InputStream;

import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspaceUtil;
import org.pentaho.commons.metadata.mqleditor.editor.SwtMqlEditor;
import org.pentaho.metadata.repository.FileBasedMetadataDomainRepository;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.CompoundDataFactory;
import org.pentaho.reporting.engine.classic.core.DataFactory;
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
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.AbstractModelNode;

/**
 * TODO: Document Me
 *
 * @author William Seyler
 */
public class DataSourceAndQueryStep extends AbstractWizardStep
{
  private enum DATASOURCE_TYPE
  {
    ROOT, DATAFACTORY, CONNECTION, QUERY
  }

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
      FileBasedMetadataDomainRepository repo = new FileBasedMetadataDomainRepository();
      repo.setDomainFolder(modelFile.getParent());
      XmiParser parser = new XmiParser();
      try {
        InputStream inStream = new FileInputStream(modelFile);
        if (inStream != null) {
          org.pentaho.metadata.model.Domain d = parser.parseXmi(inStream);
          d.setId(modelFile.getName());
          repo.storeDomain(d, true);
          repo.reloadDomains();
        }
      } catch (Exception e) {
        
      }
      SwtMqlEditor editor = new SwtMqlEditor(repo);
      editor.show();
      System.out.println("In 'doCreateQuery()'");
    }
    
    public void doEditQuery() {
      System.out.println("In 'doEditQuery()'");
    }
  }


  protected class DatasourceModelNode extends AbstractModelNode<DatasourceModelNode>
  {
    private DATASOURCE_TYPE type;

    private String value;
    private Object userObject;

    public DatasourceModelNode(String value, Object userObject, DATASOURCE_TYPE type)
    {
      this.value = value;
      this.userObject = userObject;
      this.type = type;
    }

    public String getValue()
    {
      return value;
    }

    public void setValue(String value)
    {
      String oldValue = this.value;
      this.value = value;

      this.firePropertyChange(VALUE_PROPERTY_NAME, oldValue, value);
    }

    public DATASOURCE_TYPE getType()
    {
      return type;
    }

    public void setType(DATASOURCE_TYPE type)
    {
      this.type = type;
    }

    public Object getUserObject()
    {
      return userObject;
    }

    public void setUserObject(Object userObject)
    {
      this.userObject = userObject;
    }
  }

  private static final String DATASOURCE_AND_QUERY_STEP_OVERLAY = "org/pentaho/agilebi/pdi/wizard/ui/xul/res/datasource_and_query_step_Overlay.xul"; //$NON-NLS-1$
  private static final String HANDLER_NAME = "datasource_and_query_step_handler"; //$NON-NLS-1$

  private static final String CURRENT_QUERY_PROPERTY_NAME = "currentQuery"; //$NON-NLS-1$
  private static final String VALUE_PROPERTY_NAME = "value"; //$NON-NLS-1$
  private static final String DATA_SOURCE_NAME_LABEL_ID = "data_source_name_label";  //$NON-NLS-1$

  private DatasourceModelNode dataSourcesRoot;
  private CompoundDataFactory cdf;
  private ModelerWorkspace model;
  private File modelFile;

  public DataSourceAndQueryStep()
  {
    super();
  }

  public void setBindings()
  {
  }

  public void editQuery(String queryName)
  {
    DataFactory dataFactory = getOwnerDataFactory(queryName);
  }


  private DataFactory getOwnerDataFactory(String queryName)
  {
    return cdf.getDataFactoryForQuery(queryName);
  }

  public void stepActivating()
  {
    super.stepActivating();
    cdf = (CompoundDataFactory) getEditorModel().getReportDefinition().getDataFactory();
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

      PmdDataFactory dataFactory = new PmdDataFactory();
      dataFactory.setConnectionProvider(new PmdConnectionProvider());
      dataFactory.setXmiFile(fileName);
      dataFactory.setDomainId(fileName);
      
      try {
        cdf.add(dataFactory);
      } catch (ReportDataFactoryException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    DataFactory df = null;
    try {
      df = cdf.get(0);
      XulLabel datasourceLabel = (XulLabel) getDocument().getElementById(DATA_SOURCE_NAME_LABEL_ID);
      datasourceLabel.setValue(df.toString());
    } catch (ReportDataFactoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    setValid(validateStep());
  }

  public boolean stepDeactivating()
  {
    getEditorModel().getReportDefinition().setDataFactory(cdf);
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
    if (cdf.isQueryExecutable(queryName, new StaticDataRow()) == false)
    {
      return false;
    }

    try
    {
      final AbstractReportDefinition abstractReportDefinition =
          (AbstractReportDefinition) reportDefinition.derive();
      abstractReportDefinition.setDataFactory(cdf.derive());
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
      this.setValid(validateStep());
    }
  }

  public DatasourceModelNode getDataSourcesRoot()
  {
    return dataSourcesRoot;
  }

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
