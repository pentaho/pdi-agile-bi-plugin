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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.row.RowMetaInterface;
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
import org.pentaho.metadata.util.MondrianModelExporter;
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
      SpoonFactory.getInstance().messageBox( Messages.getString( "ModelerWorkspaceUtil.FromOutputStep.TransNotOpen" ), MODELER_NAME, false, Const.ERROR); //$NON-NLS-1$
      throw new IllegalStateException(Messages.getString( "ModelerWorkspaceUtil.FromOutputStep.TransNotOpen")); //$NON-NLS-1$
    }
    StepMeta steps[] = transMeta.getSelectedSteps();
    if( steps == null || steps.length > 1 ) {
      SpoonFactory.getInstance().messageBox( Messages.getString( "ModelerWorkspaceUtil.FromOutputStep.OneStepNeeded"), MODELER_NAME, false, Const.ERROR); //$NON-NLS-1$
      throw new IllegalStateException(Messages.getString( "ModelerWorkspaceUtil.FromOutputStep.OneStepNeeded")); //$NON-NLS-1$
    }
    
    // assume only one selected 
    StepMeta stepMeta = steps[0];
    if( !(stepMeta.getStepMetaInterface() instanceof TableOutputMeta) ) {
      SpoonFactory.getInstance().messageBox( Messages.getString( "ModelerWorkspaceUtil.FromOutputStep.OutputStepNeeded"), MODELER_NAME, false, Const.ERROR); //$NON-NLS-1$
      throw new IllegalStateException(Messages.getString( "ModelerWorkspaceUtil.FromOutputStep.OutputStepNeeded")); //$NON-NLS-1$
    }
    
    TableOutputMeta tableOutputMeta = (TableOutputMeta) stepMeta.getStepMetaInterface();
    DatabaseMeta databaseMeta = tableOutputMeta.getDatabaseMeta();

    RowMetaInterface rowMeta = null;
    try {
      rowMeta = transMeta.getStepFields(stepMeta);
    } catch (KettleException e) {
    	logger.info(e);
      throw new ModelerException(Messages.getString( "ModelerWorkspaceUtil.FromOutputStep.NoStepMeta"), e); //$NON-NLS-1$
    }
    if(rowMeta == null){
   	 throw new ModelerException(Messages.getString( "ModelerWorkspaceUtil.FromOutputStep.NoStepMeta")); //$NON-NLS-1$
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
    workspace.getModel().getMeasures().clear();
    workspace.getModel().getDimensions().clear();
    List<AvailableField> fields = workspace.getAvailableFields();
    for( AvailableField field : fields ) {
      DataType dataType = field.getLogicalColumn().getDataType();
      if( dataType == DataType.NUMERIC) {
        // create a measure
        workspace.addFieldIntoPlay(field);
      }
      // create a dimension
      workspace.addDimension(field);
    }
    
    /*
    // 
    List<ColumnProfile> profileList = new ArrayList<ColumnProfile>();
    List<ValueMetaInterface> valueMetaList = rowMeta.getValueMetaList();
    
    for( ValueMetaInterface valueMeta : valueMetaList ) {
      // create one or more profiles for each value meta
      ColumnProfile profile = new ColumnProfile();
      int dataType = valueMeta.getType();
      if( dataType ==  ValueMetaInterface.TYPE_BIGNUMBER || 
          dataType == ValueMetaInterface.TYPE_INTEGER ||
          dataType == ValueMetaInterface.TYPE_NUMBER ) {
        // create a measure
        profile.setFieldType( ColumnProfile.FIELD_FACT );
        profile.setType( valueMeta.getType() );
        profile.setId( valueMeta.getName()+" (m)" );
        profile.setColumnName( valueMeta.getName() );
        profile.setTitle( valueMeta.getName()+"(m)" );
        profile.setUsed(true);
        profileList.add( profile );
        profile.setFormat("#");
      }
      profile = new ColumnProfile();
      profile.setFieldType( ColumnProfile.FIELD_DIMENSION );
      profile.setType( valueMeta.getType() );
      profile.setId( valueMeta.getName() );
      profile.setColumnName( valueMeta.getName() );
      profile.setDimensionTitle( valueMeta.getName() );
      profile.setTitle( valueMeta.getName() );
      profile.setUsed(true);
      profileList.add( profile );
    }
    ColumnProfile profiles[] = profileList.toArray( new ColumnProfile[profileList.size()] );
    generateModels( modelId, databaseMeta, locale, user, tableName, profiles );
    */
  }
  
  public static void populateDomain(ModelerWorkspace model) throws ModelerException {
    
    model.getDomain().setId( model.getModelName() );
    
    List<Category> cats = model.getDomain().getLogicalModels().get(0).getCategories();
    LogicalTable logicalTable = model.getDomain().getLogicalModels().get(0).getLogicalTables().get(0);

    if (model.getModelSource() != null) {
      model.getModelSource().serializeIntoDomain(model.getDomain());
    }
    
    LogicalModel logicalModel = model.getDomain().getLogicalModels().get(0);
    logicalModel.setName( new LocalizedString( Locale.getDefault().toString(), model.getModelName() ) );
    
    Category cat;
    // Find existing category or create new one

    if (cats.size() > 0) {
      cat = cats.get(0);
    } else {
      cat = new Category();
      model.getDomain().getLogicalModels().get(0).addCategory(cat);
    }
    cat.setId(model.getModelName());
    cat.getLogicalColumns().clear();

    // Add all measures
    for (MeasureMetaData f : model.getFields()) {
      LogicalColumn lCol = f.getLogicalColumn();
      lCol.setName(new LocalizedString(Locale.getDefault().toString(), f.getDisplayName()));
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
        logger.info(Messages.getString("ModelerWorkspaceUtil.Populate.BadAggType", f.getAggTypeDesc() ), e); //$NON-NLS-1$
        throw new ModelerException(e);
      }
      lCol.setAggregationType(selectedAgg);
      cat.addLogicalColumn(lCol);
    }

    // Add levels
    for (DimensionMetaData dim : model.getModel().getDimensions()) {
      for (HierarchyMetaData hier : dim) {
        for (int j = 0; j < hier.getChildren().size(); j++) {
          LevelMetaData level = hier.getChildren().get(j);
          LogicalColumn lCol = level.getLogicalColumn();
          lCol.setName(new LocalizedString(Locale.getDefault().toString(), level.getName()));
          if (cat.findLogicalColumn(lCol.getId()) == null) {
            cat.addLogicalColumn(lCol);
          }
        }
      }
    }
//
//    XmiParser parser = new XmiParser();
//    String xmi = parser.generateXmi(model.getDomain());
//  
//    // write the XMI to a tmp file
//    // models was created earlier.
//    try{
//      File dir = new File( "models"); //$NON-NLS-1$
//      dir.mkdirs();
//      File file = new File("models/" + model.getModelName() + ".xmi"); //$NON-NLS-1$ //$NON-NLS-2$
//      PrintWriter pw = new PrintWriter(new FileWriter(file));
//      pw.print(xmi);
//      pw.close();
//    } catch(IOException e){
//      logger.info(Messages.getString("ModelerWorkspaceUtil.Populate.BadWrite"),e); //$NON-NLS-1$
//      throw new ModelerException(Messages.getString("ModelerWorkspaceUtil.Populate.BadWrite"),e); //$NON-NLS-1$
//    }
//  

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

          for (LevelMetaData lvl : hier.getChildren()) {
            // System.out.println( "level "+child.getId() );
            OlapHierarchyLevel level = new OlapHierarchyLevel(hierarchy);
            level.setName(lvl.getName());
            level.setReferenceColumn(lvl.getLogicalColumn());
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
      cube.setName( Messages.getString("ModelerWorkspaceUtil.Populate.CubeName", model.getModelName() ) ); //$NON-NLS-1$
      cube.setOlapDimensionUsages(usages);

      for (MeasureMetaData f : model.getFields()) {

        OlapMeasure measure = new OlapMeasure();
        String n = f.getDisplayName() != null ? f.getDisplayName() : f.getName();
        measure.setName(n);
        f.getLogicalColumn().setAggregationType(AggregationType.valueOf(f.getAggTypeDesc()));
        measure.setLogicalColumn(f.getLogicalColumn());
        measures.add(measure);
      }

      cube.setOlapMeasures(measures);

      LogicalModel lModel = model.getDomain().getLogicalModels().get(0);

      if (olapDimensions.size() > 0) { // Metadata OLAP generator doesn't like empty lists.
        lModel.setProperty("olap_dimensions", olapDimensions); //$NON-NLS-1$
      }
      List<OlapCube> cubes = new ArrayList<OlapCube>();
      cubes.add(cube);
      lModel.setProperty("olap_cubes", cubes); //$NON-NLS-1$

//      try{
//        MondrianModelExporter exporter = new MondrianModelExporter(lModel, Locale.getDefault().toString());
//        String mondrianSchema = exporter.createMondrianModelXML();
//  
//        logger.info(mondrianSchema);
//  
//        // run it thru the parser to be safe and get a doc type node
//        Document schemaDoc = DocumentHelper.parseText(mondrianSchema);
//        byte schemaBytes[] = schemaDoc.asXML().getBytes();
//        // write out the file
//        File modelFile = new File("models"); //$NON-NLS-1$
//        modelFile.mkdirs();
//        modelFile = new File("models/" + model.getModelName() + ".mondrian.xml"); //$NON-NLS-1$ //$NON-NLS-2$
//        OutputStream out = new FileOutputStream(modelFile);
//        out.write(schemaBytes);
//      } catch(Exception e){
//        throw new ModelerException(Messages.getString("ModelerWorkspaceUtil.Populate.BadGenerateOLAP"),e); //$NON-NLS-1$
//      }

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
	      logger.info(Messages.getString("ModelerWorkspaceUtil.Populate.BadGenerateMetadata"),e); //$NON-NLS-1$
	      throw new ModelerException(Messages.getString("ModelerWorkspaceUtil.Populate.BadGenerateMetadata"),e); //$NON-NLS-1$
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
      throw new ModelerException(Messages.getString("ModelerWorkspaceUtil.LoadWorkspace.Failed"),e); //$NON-NLS-1$
    }
  }  
}
