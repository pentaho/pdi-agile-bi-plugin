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
package org.pentaho.agilebi.pdi.modeler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.pentaho.metadata.util.XmiParser;

/** 
 * Utility class for generating ModelerModels for the User Interface.
 * 
 * @author nbaker
 *
 */
public class ModelerWorkspaceUtil {

  private static final List<AggregationType> DEFAULT_AGGREGATION_LIST = new ArrayList<AggregationType>();
  
  private static Log logger = LogFactory.getLog(ModelerWorkspaceUtil.class);
  
  static {
    DEFAULT_AGGREGATION_LIST.add(AggregationType.NONE);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.SUM);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.AVERAGE);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.MINIMUM);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.MAXIMUM);
  }
  
  public static ModelerWorkspace populateModelFromSource( ModelerWorkspace model, IModelerSource source ) throws ModelerException {
    Domain d = source.generateDomain();
    
    model.setModelSource(source);
    model.setModelName(source.getTableName());
    model.setDomain(d);
    
    return model;  
  }
  
  public static ModelerWorkspace populateModelFromOutputStep(ModelerWorkspace model) throws ModelerException {
    
    String MODELER_NAME = "OutputStepModeler"; //$NON-NLS-1$

    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    TransMeta transMeta = spoon.getActiveTransformation();
    if( transMeta == null ) {
      SpoonFactory.getInstance().messageBox( BaseMessages.getString(ModelerWorkspaceUtil.class,  "ModelerWorkspaceUtil.FromOutputStep.TransNotOpen" ), MODELER_NAME, false, Const.ERROR); //$NON-NLS-1$
      throw new IllegalStateException(BaseMessages.getString(ModelerWorkspaceUtil.class,  "ModelerWorkspaceUtil.FromOutputStep.TransNotOpen")); //$NON-NLS-1$
    }
    List<StepMeta> steps = transMeta.getSelectedSteps();
    if( steps == null || steps.size() > 1 ) {
      SpoonFactory.getInstance().messageBox( BaseMessages.getString(ModelerWorkspaceUtil.class,  "ModelerWorkspaceUtil.FromOutputStep.OneStepNeeded"), MODELER_NAME, false, Const.ERROR); //$NON-NLS-1$
      throw new IllegalStateException(BaseMessages.getString(ModelerWorkspaceUtil.class,  "ModelerWorkspaceUtil.FromOutputStep.OneStepNeeded")); //$NON-NLS-1$
    }
    
    // assume only one selected 
    StepMeta stepMeta = steps.get(0);
    if( !(stepMeta.getStepMetaInterface() instanceof TableOutputMeta) ) {
      SpoonFactory.getInstance().messageBox( BaseMessages.getString(ModelerWorkspaceUtil.class,  "ModelerWorkspaceUtil.FromOutputStep.OutputStepNeeded"), MODELER_NAME, false, Const.ERROR); //$NON-NLS-1$
      throw new IllegalStateException(BaseMessages.getString(ModelerWorkspaceUtil.class,  "ModelerWorkspaceUtil.FromOutputStep.OutputStepNeeded")); //$NON-NLS-1$
    }
    
    TableOutputMeta tableOutputMeta = (TableOutputMeta) stepMeta.getStepMetaInterface();
    DatabaseMeta databaseMeta = tableOutputMeta.getDatabaseMeta();

    RowMetaInterface rowMeta = null;
    try {
      rowMeta = transMeta.getStepFields(stepMeta);
    } catch (KettleException e) {
    	logger.info(e);
    	Throwable e1 = e;
    	while (e1.getCause() != null) {
    	  e1 = e1.getCause();
    	}
      throw new ModelerException(BaseMessages.getString(ModelerWorkspaceUtil.class,  "ModelerWorkspaceUtil.FromOutputStep.NoStepMeta1", e1.getCause().getMessage()), e); //$NON-NLS-1$
    }
    if(rowMeta == null){
   	 throw new ModelerException(BaseMessages.getString(ModelerWorkspaceUtil.class,  "ModelerWorkspaceUtil.FromOutputStep.NoStepMeta")); //$NON-NLS-1$
    }
    
    
    OutputStepModelerSource source = new OutputStepModelerSource(tableOutputMeta, databaseMeta, rowMeta);
    source.setFileName(transMeta.getFilename());
    source.setStepId(stepMeta.getStepID());
    Repository repository = transMeta.getRepository();
    if(repository != null) {
    	source.setRepositoryName(repository.getName());	
    }    
    Domain d = source.generateDomain();
    
    
    model.setModelSource(source);
    model.setModelName(tableOutputMeta.getTablename());
    model.setDomain(d);
    
    return model;
  }
  
  
  /**
   * Builds an OLAP model that is attribute based.
   * @param modelId
   * @param rowMeta
   * @param locale
   * @param user
   * @param tableName
   */
  public static void autoModelFlat( ModelerWorkspace workspace ) throws ModelerException {
    if(workspace.isAutoModel()) {
      workspace.setModel(new MainModelNode());
      workspace.setModelIsChanging(true);
  
      List<AvailableField> fields = workspace.getAvailableFields();
      for( AvailableField field : fields ) {
        DataType dataType = field.getLogicalColumn().getDataType();
        if( dataType == DataType.NUMERIC) {
          // create a measure
          MeasureMetaData measure = workspace.createMeasureForNode(field);
          workspace.getModel().getMeasures().add(measure);
        }
        // create a dimension
        workspace.addDimensionFromNode(field);
      }
      workspace.setModelIsChanging(false);
    }
  }
  
  
  /**
   * Builds an OLAP model that is attribute based.
   * @param modelId
   * @param rowMeta
   * @param locale
   * @param user
   * @param tableName
   */
  public static void autoModelFlatInBackground( final ModelerWorkspace workspace ) throws ModelerException {
    if(workspace.isAutoModel()) {
      final Display display = Display.findDisplay(Thread.currentThread());
      Runnable worker = new Runnable(){

        public void run() {
          workspace.setModel(new MainModelNode());
          final boolean prevChangeState = workspace.isModelChanging();
          workspace.setModelIsChanging(true);
      
          List<AvailableField> fields = workspace.getAvailableFields();
          for( AvailableField field : fields ) {
            DataType dataType = field.getLogicalColumn().getDataType();
            if( dataType == DataType.NUMERIC) {
              // create a measure
              MeasureMetaData measure = workspace.createMeasureForNode(field);
              workspace.getModel().getMeasures().add(measure);
            }
            // create a dimension
            workspace.addDimensionFromNode(field);
          }
          display.syncExec(new Runnable(){

            public void run() {

              workspace.setModelIsChanging(prevChangeState); 
            }
          });
        }
      };
      new Thread(worker).start();
      
      
    }
  }
  
  public static void populateDomain(ModelerWorkspace model) throws ModelerException {
    
    Domain domain = model.getDomain();
    domain.setId( model.getModelName() );
    
    List<Category> cats = domain.getLogicalModels().get(0).getCategories();
    LogicalTable logicalTable = domain.getLogicalModels().get(0).getLogicalTables().get(0);

    if (model.getModelSource() != null) {
      model.getModelSource().serializeIntoDomain(domain);
    }
    
    LogicalModel logicalModel = domain.getLogicalModels().get(0);
    logicalModel.setName( new LocalizedString( Locale.getDefault().toString(), model.getModelName() ) );
    
    Category cat;
    // Find existing category or create new one

    if (cats.size() > 0) {
      cat = cats.get(0);
    } else {
      cat = new Category();
      logicalModel.addCategory(cat);
    }
    cat.setId(model.getModelName());
    cat.getLogicalColumns().clear();

    // Add all measures
    for (MeasureMetaData f : model.getModel().getMeasures()) {
      LogicalColumn lCol = logicalModel.findLogicalColumn(f.getLogicalColumn().getId());
      lCol.setName(new LocalizedString(Locale.getDefault().toString(), f.getName()));
      AggregationType type = AggregationType.valueOf(f.getAggTypeDesc());
      if (type != AggregationType.NONE) {
        lCol.setAggregationType(type);
      }
      
      // set the format mask
      
      String formatMask = f.getFormat();
      if( MeasureMetaData.FORMAT_NONE.equals(formatMask) || StringUtils.isBlank(formatMask)) {
        formatMask = null;
      }
      if (formatMask != null) {
        lCol.setProperty("mask", formatMask); //$NON-NLS-1$
      } else {
        // remove old mask that might have been set
        if (lCol.getChildProperty("mask") != null) { //$NON-NLS-1$
          lCol.removeChildProperty("mask"); //$NON-NLS-1$
        }
      }
      
      lCol.setAggregationList(DEFAULT_AGGREGATION_LIST);
      AggregationType selectedAgg = AggregationType.NONE; 
      try{
        selectedAgg = AggregationType.valueOf(f.getAggTypeDesc());
      } catch(IllegalArgumentException e){
        logger.info(BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.BadAggType", f.getAggTypeDesc() ), e); //$NON-NLS-1$
        throw new ModelerException(e);
      }
      lCol.setAggregationType(selectedAgg);
      cat.addLogicalColumn(lCol);
    }

    // Add levels
    for (DimensionMetaData dim : model.getModel().getDimensions()) {
      for (HierarchyMetaData hier : dim) {
        for (int j = 0; j < hier.size(); j++) {
          LevelMetaData level = hier.get(j);
          LogicalColumn lCol = logicalModel.findLogicalColumn(level.getLogicalColumn().getId());
          lCol.setName(new LocalizedString(Locale.getDefault().toString(), level.getName()));
          if (cat.findLogicalColumn(lCol.getId()) == null) {
            cat.addLogicalColumn(lCol);
          }
        }
      }
    }

    // =========================== OLAP ===================================== //

    
      List<OlapDimensionUsage> usages = new ArrayList<OlapDimensionUsage>();
      List<OlapDimension> olapDimensions = new ArrayList<OlapDimension>();
      List<OlapMeasure> measures = new ArrayList<OlapMeasure>();

      for (DimensionMetaData dim : model.getModel().getDimensions()) {

        OlapDimension dimension = new OlapDimension();
        String dimTitle = dim.getName();

        dimension.setName(dimTitle);
        dimension.setTimeDimension(dim.isTime());

        List<OlapHierarchy> hierarchies = new ArrayList<OlapHierarchy>();

        for (HierarchyMetaData hier : dim) {
          OlapHierarchy hierarchy = new OlapHierarchy(dimension);
          hierarchy.setName(hier.getName());
          hierarchy.setLogicalTable(logicalTable);
          List<OlapHierarchyLevel> levels = new ArrayList<OlapHierarchyLevel>();

          for (LevelMetaData lvl : hier) {
            OlapHierarchyLevel level = new OlapHierarchyLevel(hierarchy);
            level.setName(lvl.getName());
            LogicalColumn lvlColumn = logicalModel.findLogicalColumn(lvl.getLogicalColumn().getId());
            level.setReferenceColumn(lvlColumn);
            level.setHavingUniqueMembers(lvl.isUniqueMembers());
            levels.add(level);
          }

          hierarchy.setHierarchyLevels(levels);
          hierarchies.add(hierarchy);
        }
        
        if(hierarchies.isEmpty()) {
          // create a default hierarchy
          OlapHierarchy defaultHierarchy = new OlapHierarchy(dimension);
          defaultHierarchy.setLogicalTable(logicalTable);
          hierarchies.add(defaultHierarchy);	
        }

        dimension.setHierarchies(hierarchies);

        olapDimensions.add(dimension);
        OlapDimensionUsage usage = new OlapDimensionUsage(dimension.getName(), dimension);
        usages.add(usage);

      }

      OlapCube cube = new OlapCube();
      cube.setLogicalTable(logicalTable);
      // TODO find a better way to generate default names
      cube.setName( BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.CubeName", model.getModelName() ) ); //$NON-NLS-1$
      cube.setOlapDimensionUsages(usages);

      for (MeasureMetaData f : model.getModel().getMeasures()) {

        OlapMeasure measure = new OlapMeasure();
        String n = f.getName();
        measure.setName(n);
        f.getLogicalColumn().setAggregationType(AggregationType.valueOf(f.getAggTypeDesc()));
        measure.setLogicalColumn(f.getLogicalColumn());
        measures.add(measure);
      }

      cube.setOlapMeasures(measures);

      LogicalModel lModel = domain.getLogicalModels().get(0);

      if (olapDimensions.size() > 0) { // Metadata OLAP generator doesn't like empty lists.
        lModel.setProperty("olap_dimensions", olapDimensions); //$NON-NLS-1$
      }
      List<OlapCube> cubes = new ArrayList<OlapCube>();
      cubes.add(cube);
      lModel.setProperty("olap_cubes", cubes); //$NON-NLS-1$
  }
  
  public static void saveWorkspace(ModelerWorkspace aModel, String fileName) throws ModelerException {
  	try {
  	  
	    String xmi = getMetadataXML(aModel);
	  
	    // write the XMI to a tmp file
	    // models was created earlier.
	    try{

	      File file = new File(fileName);
	      PrintWriter pw = new PrintWriter(new FileWriter(file));
	      pw.print(xmi);
	      pw.close();
	      
	    } catch(IOException e){
	      logger.info(BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.BadGenerateMetadata"),e); //$NON-NLS-1$
	      throw new ModelerException(BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.BadGenerateMetadata"),e); //$NON-NLS-1$
	    }

  	} catch (Exception e) {
  		logger.info(e.getLocalizedMessage());
  		throw new ModelerException(e);
  	}
  }
  
  public static String getMetadataXML(ModelerWorkspace aModel) throws ModelerException {
    populateDomain(aModel);
    XmiParser parser = new XmiParser();
    return parser.generateXmi(aModel.getDomain());
  
  }
  
  public static void loadWorkspace(String fileName, String aXml, ModelerWorkspace aModel) throws ModelerException {

    try{
      XmiParser parser = new XmiParser();
      Domain domain = parser.parseXmi(new ByteArrayInputStream(aXml.getBytes()));
        	
      // re-hydrate the source
      LogicalModel logical = domain.getLogicalModels().get(0);
      Object property = logical.getProperty("source_type"); //$NON-NLS-1$
      if( property != null ) {
        IModelerSource theSource = ModelerSourceFactory.generateSource(property.toString());
        theSource.initialize(domain);   
        aModel.setModelSource(theSource);
      }
  
    	aModel.setDomain(domain);
    	aModel.setFileName(fileName);
    	aModel.resolveConnectionFromDomain();
    	aModel.refresh();
    	aModel.setDirty(false);
    } catch (Exception e){
      logger.info(e);
      e.printStackTrace();
      throw new ModelerException(BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.LoadWorkspace.Failed"),e); //$NON-NLS-1$
    }
  }  
}
