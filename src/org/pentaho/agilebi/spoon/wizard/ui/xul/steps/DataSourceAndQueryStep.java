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

package org.pentaho.agilebi.spoon.wizard.ui.xul.steps;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.spoon.wizard.EmbeddedWizard;
import org.pentaho.commons.metadata.mqleditor.MqlQuery;
import org.pentaho.commons.metadata.mqleditor.editor.MQLEditorService;
import org.pentaho.commons.metadata.mqleditor.editor.SwtMqlEditor;
import org.pentaho.commons.metadata.mqleditor.editor.service.MQLEditorServiceImpl;
import org.pentaho.commons.metadata.mqleditor.editor.service.util.MQLEditorServiceDelegate;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.query.model.util.QueryXmlHelper;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.reporting.engine.classic.core.*;
import org.pentaho.reporting.engine.classic.core.states.datarow.StaticDataRow;
import org.pentaho.reporting.engine.classic.core.wizard.DataAttributes;
import org.pentaho.reporting.engine.classic.core.wizard.DataSchema;
import org.pentaho.reporting.engine.classic.core.wizard.DataSchemaModel;
import org.pentaho.reporting.engine.classic.core.wizard.DefaultDataAttributeContext;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.IPmdConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdConnectionProvider;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.engine.classic.wizard.ui.xul.WizardEditorModel;
import org.pentaho.reporting.engine.classic.wizard.ui.xul.components.AbstractWizardStep;
import org.pentaho.reporting.libraries.base.util.DebugLog;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.ui.datasources.pmd.PmdPreviewWorker;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;
import org.pentaho.ui.xul.swt.SwtXulLoader;

import javax.swing.table.TableModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * TODO: Document Me
 *
 * @author William Seyler
 */
public class DataSourceAndQueryStep extends AbstractWizardStep
{

  private static final String DATASOURCE_AND_QUERY_STEP_OVERLAY = "org/pentaho/agilebi/pdi/wizard/ui/xul/res/datasource_and_query_step_Overlay.xul"; //$NON-NLS-1$
  private static final String HANDLER_NAME = "datasource_and_query_step_handler"; //$NON-NLS-1$

  private static final String CURRENT_QUERY_PROPERTY_NAME = "currentQuery"; //$NON-NLS-1$
  private static final String DATA_SOURCE_NAME_LABEL_ID = "data_source_name_label";  //$NON-NLS-1$
  private static final String AVAILABLE_COLUMNS_PROPERTY_NAME = "availableColumns"; //$NON-NLS-1$
  private static final String ELEMENTS_PROPERTY_NAME = "elements"; //$NON-NLS-1$
  private static final String QUERY_RESULT_LIST_ID = "query_result_list"; //$NON-NLS-1$
  private static final String NEXT_BTN_ID = "next_btn"; //$NON-NLS-1$

  private static final String DEFAULT = "default"; //$NON-NLS-1$

  private PmdDataFactory df;
  private ModelerWorkspace model;
  private File modelFile;
  private List<String> availableColumns;

  /**
   * @author wseyler
   * DatasourceAndQueryStepHandler
   * A concrete implementation of AbstractXulEventHandler that defines a name for
   * itself and contains methods that correspond to onClick and onCommand markups
   * in the corresponding *.xul file.
   */
  protected class DatasourceAndQueryStepHandler extends AbstractXulEventHandler
  {
    public DatasourceAndQueryStepHandler()
    {
      
      
    }

    public String getName()
    {
      return HANDLER_NAME;
    }

    private IMetadataDomainRepository getDomainRepo() throws ReportDataFactoryException {

      IPmdConnectionProvider connectionProvider = ((PmdDataFactory) getEditorModel().getReportDefinition().getDataFactory()).getConnectionProvider();
      IMetadataDomainRepository repo = connectionProvider.getMetadataDomainRepository(DEFAULT, getEditorModel().getReportDefinition().getResourceManager(), getEditorModel().getReportDefinition().getContentBase(), df.getXmiFile());
      
      return repo;
    }
    
    
    private MQLEditorServiceDelegate getMqlServiceDelegate() throws ReportDataFactoryException{

      MQLEditorServiceDelegate delegate = new MQLEditorServiceDelegate(getDomainRepo()) {
        @Override
        public String[][] getPreviewData(MqlQuery query, int page, int limit) {
          org.pentaho.metadata.query.model.Query mqlQuery = convertQueryModel(query);
          String mqlString = new QueryXmlHelper().toXML(mqlQuery);
          

          PmdDataFactory df = (PmdDataFactory) getEditorModel().getReportDefinition().getDataFactory();
          df.setQuery("default", mqlString);
          
          PmdPreviewWorker worker = new PmdPreviewWorker(df, "default", 0,  limit);
          worker.run();
          if(worker.getException() != null){
            worker.getException().printStackTrace();
          }
          TableModel model = worker.getResultTableModel();
          int colCount = model.getColumnCount();
          int rowCount = model.getRowCount();
          String[][] results = new String[rowCount][colCount];
          for(int y = 0; y < rowCount; y++ ){
            for(int x=0; x < colCount; x++){
              results[y][x] = ""+model.getValueAt(y, x);
            }
          }
          return results;
        }
      };
      return delegate;
    }
    
    private MQLEditorService getMqlService(MQLEditorServiceDelegate delegate){

      MQLEditorServiceImpl mqlService = new MQLEditorServiceImpl(delegate) {
        @Override
        public void getPreviewData(MqlQuery query, int page, int limit, XulServiceCallback<String[][]> callback) {
          callback.success(delegate.getPreviewData(query, page, limit));
        }
      };
      return mqlService;
    }
    
    /**
     * doEditQuery()
     * Updates (or creates) a query using the PME data source.
     */
    public void doEditQuery() {
      try {

        IMetadataDomainRepository repo = getDomainRepo();
        
        
        MQLEditorServiceDelegate delegate = getMqlServiceDelegate();
        
        SwtMqlEditor editor = new SwtMqlEditor(repo, getMqlService(delegate), delegate){

          @Override
          protected XulLoader getLoader() {
            SwtXulLoader loader;
            try {
              loader = new SwtXulLoader();
              loader.registerClassLoader(getClass().getClassLoader());
              return loader;
            } catch (XulException e) {
              e.printStackTrace();
            }
            return null;
          }
                    
        };
        String queryString = null;
        if (df != null && df.getQuery(DEFAULT) != null) {
          queryString = df.getQuery(DEFAULT);
          editor.setQuery(queryString);
        }
        editor.addOverlay(new DefaultXulOverlay("org/pentaho/agilebi/pdi/wizard/ui/xul/res/mqleditor-overlay.xul"));
        editor.show();
        if (editor.getOkClicked()) {
          queryString = editor.getQuery();
          df.setQuery(DEFAULT, queryString);
          setCurrentQuery(DEFAULT);
        }
      } catch (Exception e) {
        getDesignTimeContext().userError(e);
      }
    }
  }

  public DataSourceAndQueryStep()
  {
    super();
  }

  
  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.AbstractWizardStep#stepActivating()
   * 
   * stepActivating()
   * When this step activates we check to see if we've already been here and if we haven't then we
   * creates the model 'n'.xmi file from the model.
   * 
   * If we're coming back in then we just get the current data source and manipulate that.
   */
  public void stepActivating()
  {
    super.stepActivating();
    
    if (model != null && df == null) {

      if (getEditorModel().getReportDefinition().getDataFactory() != null && getEditorModel().getReportDefinition().getDataFactory()
          instanceof CompoundDataFactory) {
        CompoundDataFactory cdf = (CompoundDataFactory) getEditorModel().getReportDefinition().getDataFactory();
        for (int i=0; i<cdf.size(); i++) {
          cdf.remove(i);
        }
      }

      df = new PmdDataFactory();
      PmdConnectionProvider connectionProvider = new PmdConnectionProvider();
      df.setConnectionProvider(connectionProvider);
      df.setXmiFile(model.getFileName());
      df.setDomainId(DEFAULT);
      getEditorModel().getReportDefinition().setDataFactory(df);

    } else { // editing existing
      DataFactory savedDf = getEditorModel().getReportDefinition().getDataFactory();
      if (savedDf instanceof PmdDataFactory) {
        df = (PmdDataFactory) savedDf;
      } else {
        df = (PmdDataFactory)((CompoundDataFactory)savedDf).getDataFactoryForQuery(DEFAULT);
      }
      
      // Couldn't find anything to edit so we gotta create something new.
      if (df == null) {
        df = new PmdDataFactory();
        PmdConnectionProvider connectionProvider = new PmdConnectionProvider();
        df.setConnectionProvider(connectionProvider);
        if (model != null) {
          df.setXmiFile(model.getFileName());
        }
        df.setDomainId(DEFAULT);
      }
      if (getEditorModel() != null) {
        getEditorModel().getReportDefinition().setDataFactory(df);
      }
    }

    updateGui();

    setValid(validateStep());
  }


  
  /**
   * updateGui()
   * 
   * Updates the data source name label and populates the available columns list box.
   */
  private void updateGui() {
    // Set the data source name
    XulLabel datasourceLabel = (XulLabel) getDocument().getElementById(DATA_SOURCE_NAME_LABEL_ID);
    if(datasourceLabel != null && modelFile != null){
      datasourceLabel.setValue(modelFile.getName().substring(0, modelFile.getName().lastIndexOf('.')));
    }
    createColumnsList();
  }
  
  /**
   * createColumnsList()
   * 
   * Get all the columns current defined by the query and creates a list of their friendly
   * names for display in the available columns list box.
   * 
   * Additionally it removes any names whose source is not the query and then it sorts the
   * final list.
   */
  private void createColumnsList() {
    // Set the available query fields;
    final DataSchemaModel dataSchemaModel = getEditorModel().getDataSchema();
    final DataSchema dataSchema = dataSchemaModel.getDataSchema();
    final String[] names = dataSchema.getNames();
    Arrays.sort(names);
    
    ArrayList<String> items = new ArrayList<String>();
    if (names != null) {
    final DefaultDataAttributeContext dataAttributeContext = new DefaultDataAttributeContext();

      for ( String name : names ) {
        final DataAttributes attributes = dataSchema.getAttributes(name);
        final String source = (String) attributes.getMetaAttribute(MetaAttributeNames.Core.NAMESPACE, MetaAttributeNames.Core.SOURCE, String.class, dataAttributeContext);
        if ( !source.equals("environment") && !source.equals("parameter") ) {
          String displayStr = (String) attributes.getMetaAttribute
          (MetaAttributeNames.Formatting.NAMESPACE, MetaAttributeNames.Formatting.LABEL,
              String.class, dataAttributeContext);
          if (displayStr != null && displayStr.length() > 0) {
            items.add(displayStr);
          }
        }
      }
      
    }
    if (items.size() < 1) {
      items.add(BaseMessages.getString(EmbeddedWizard.class,"DataSourceAndQueryStep.no_defined_fields")); //$NON-NLS-1$
    }
    setAvailableColumns(items);  
  }
  
  /**
   * @return true if the query can be executed.
   */
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

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.AbstractWizardStep#createPresentationComponent(org.pentaho.ui.xul.XulDomContainer)
   * 
   * Loads the overlay for this step and hooks up the event handler
   */
  public void createPresentationComponent(XulDomContainer mainWizardContainer) throws XulException
  {
    super.createPresentationComponent(mainWizardContainer);

    mainWizardContainer.loadOverlay(DATASOURCE_AND_QUERY_STEP_OVERLAY);
    mainWizardContainer.addEventHandler(new DatasourceAndQueryStepHandler());
  }

  /**
   * @return the currently defined query
   */
  public String getCurrentQuery()
  {
    return getEditorModel().getReportDefinition().getQuery();
  }

  /**
   * @param currentQuery set the current query to the argument 'currentQuery' and fires
   * a property change event for objects that have registered.
   */
  public void setCurrentQuery(String currentQuery)
  {
    String oldQuery = getCurrentQuery();
    getEditorModel().updateQuery(df, DEFAULT);
    this.firePropertyChange(CURRENT_QUERY_PROPERTY_NAME, oldQuery, currentQuery);
    this.setValid(validateStep());
    updateGui();
  }
  
  /**
   * @param availableColumns the availableColumns to set once set it fires and property
   * change event.
   */
  public void setAvailableColumns(List<String> newValue) {
    List<String> oldValue = this.availableColumns;
    this.availableColumns = newValue;
    this.firePropertyChange(AVAILABLE_COLUMNS_PROPERTY_NAME, oldValue, newValue);
  }

  /**
   * @return the availableColumns
   */
  public List<String> getAvailableColumns() {
    return availableColumns;
  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.AbstractWizardStep#setValid(boolean)
   * 
   * sets the validity of this step.  If this is set to true the 'next' and preview button will
   * be available.
   */
  protected void setValid(final boolean valid) {
    XulButton nextButton = (XulButton) getDocument().getElementById(NEXT_BTN_ID);
    nextButton.setDisabled(!valid);
  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardStep#getStepName()
   * 
   * returns the internationalized step name that appears in the step list.
   */
  public String getStepName()
  {
    return BaseMessages.getString(EmbeddedWizard.class,"DataSourceAndQueryStep.name"); //$NON-NLS-1$
  }

  public void setModel(ModelerWorkspace model) {
    this.model = model;
  }

  /* (non-Javadoc)
   * @see org.pentaho.reporting.engine.classic.wizard.ui.xul.components.WizardStep#setBindings()
   * 
   * Binds the available columns property to the query result list.
   */
  public void setBindings() {
    getBindingFactory().createBinding(this, AVAILABLE_COLUMNS_PROPERTY_NAME, QUERY_RESULT_LIST_ID, ELEMENTS_PROPERTY_NAME);
  }

}
