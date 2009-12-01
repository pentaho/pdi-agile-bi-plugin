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
  
  public static ModelerWorkspace createModelFromOutputStep() throws ModelerException {

    return createModelFromOutputStep(new ModelerWorkspace());
  }
  
  public static ModelerWorkspace createModelFromOutputStep(ModelerWorkspace model) throws ModelerException {
    
    String MODELER_NAME = "OutputStepModeler"; //$NON-NLS-1$

    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    TransMeta transMeta = spoon.getActiveTransformation();
    if( transMeta == null ) {
      SpoonFactory.getInstance().messageBox( "This must be open from a transformation", MODELER_NAME, false, Const.ERROR); //$NON-NLS-1$
      throw new IllegalStateException("Could not get transformation");
    }
    StepMeta steps[] = transMeta.getSelectedSteps();
    if( steps == null || steps.length > 1 ) {
      SpoonFactory.getInstance().messageBox( "One (and only one) step must be selected", MODELER_NAME, false, Const.ERROR); //$NON-NLS-1$
      throw new IllegalStateException("One (and only one) step must be selected");
    }
    
    // assume only one selected 
    StepMeta stepMeta = steps[0];
    if( !(stepMeta.getStepMetaInterface() instanceof TableOutputMeta) ) {
      SpoonFactory.getInstance().messageBox( "A Table Output step must be selected", MODELER_NAME, false, Const.ERROR); //$NON-NLS-1$
      throw new IllegalStateException("A Table Output step must be selected");
    }
    
    TableOutputMeta tableOutputMeta = (TableOutputMeta) stepMeta.getStepMetaInterface();
    DatabaseMeta databaseMeta = tableOutputMeta.getDatabaseMeta();
    String tableName = tableOutputMeta.getTablename();

    RowMetaInterface rowMeta = null;
    try {
      rowMeta = transMeta.getStepFields(stepMeta);
    } catch (KettleException e) {
    	logger.info(e);
      throw new ModelerException("Could not get transformation step metadata", e);
    }
    if(rowMeta == null){
   	 throw new ModelerException("Could not get transformation step metadata");
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
  
  public static void populateDomain(ModelerWorkspace model) throws ModelerException {
    List<Category> cats = model.getDomain().getLogicalModels().get(0).getCategories();
    LogicalTable logicalTable = model.getDomain().getLogicalModels().get(0).getLogicalTables().get(0);

    model.getModelSource().serializeIntoDomain(model.getDomain());
    
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
    for (FieldMetaData f : model.getFields()) {
      LogicalColumn lCol = f.getLogicalColumn();
      if (f.getFormat() != null) {
        // TODO: set mask
        // lCol.setFormat( f.getFormat());
      }
      lCol.setName(new LocalizedString(Locale.getDefault().toString(), f.getDisplayName()));
      AggregationType type = AggregationType.valueOf(f.getAggTypeDesc());
      if (type != AggregationType.NONE) {
        lCol.setAggregationType(type);
      }
      lCol.setAggregationList(DEFAULT_AGGREGATION_LIST);
      AggregationType selectedAgg = AggregationType.NONE; 
      try{
        selectedAgg = AggregationType.valueOf(f.getAggTypeDesc());
      } catch(IllegalArgumentException e){
        logger.info("Could not parse Aggregation string to type: "+f.getAggTypeDesc(), e);
        throw new ModelerException(e);
      }
      lCol.setAggregationType(selectedAgg);
      cat.addLogicalColumn(lCol);
    }

    // Add levels
    for (DimensionMetaData dim : model.getDimensions()) {
      for (HierarchyMetaData hier : dim) {
        for (int j = 0; j < hier.getChildren().size(); j++) {
          LevelMetaData level = hier.getChildren().get(j);
          String format = "#";
          
          LogicalColumn lCol = level.getLogicalColumn();

          // TODO: handle custom formating
          if (format != null) {
            lCol.setProperty("mask", format);
          }
          lCol.setName(new LocalizedString(Locale.getDefault().toString(), level.getName()));
          if (cat.findLogicalColumn(lCol.getId()) == null) {
            cat.addLogicalColumn(lCol);
          }
        }
      }
    }

    String domainName = model.getDomain().getId();
    XmiParser parser = new XmiParser();
    String xmi = parser.generateXmi(model.getDomain());
  
    // write the XMI to a tmp file
    // models was created earlier.
    try{
      File dir = new File("models/" + model.getModelName());
      dir.mkdirs();
      File file = new File("models/" + model.getModelName() + "/metadata.xmi");
      PrintWriter pw = new PrintWriter(new FileWriter(file));
      pw.print(xmi);
      pw.close();
    } catch(IOException e){
      logger.info("Error writing metadata model",e);
      throw new ModelerException("Error writing metadata model",e);
    }
  

    // =========================== OLAP ===================================== //

    
      List<OlapDimensionUsage> usages = new ArrayList<OlapDimensionUsage>();
      List<OlapDimension> olapDimensions = new ArrayList<OlapDimension>();
      List<OlapMeasure> measures = new ArrayList<OlapMeasure>();

      for (DimensionMetaData dim : model.getDimensions()) {

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
      cube.setName(model.getModelName() + " Cube");
      cube.setOlapDimensionUsages(usages);

      for (FieldMetaData f : model.getFields()) {

        OlapMeasure measure = new OlapMeasure();
        String n = f.getDisplayName() != null ? f.getDisplayName() : f.getFieldName();
        measure.setName(n);
        f.getLogicalColumn().setAggregationType(AggregationType.valueOf(f.getAggTypeDesc()));
        measure.setLogicalColumn(f.getLogicalColumn());
        measures.add(measure);
      }

      cube.setOlapMeasures(measures);

      LogicalModel lModel = model.getDomain().getLogicalModels().get(0);

      if (olapDimensions.size() > 0) { // Metadata OLAP generator doesn't like empty lists.
        lModel.setProperty("olap_dimensions", olapDimensions);
      }
      List<OlapCube> cubes = new ArrayList<OlapCube>();
      cubes.add(cube);
      lModel.setProperty("olap_cubes", cubes);

      try{
        MondrianModelExporter exporter = new MondrianModelExporter(lModel, Locale.getDefault().toString());
        String mondrianSchema = exporter.createMondrianModelXML();
  
        logger.info(mondrianSchema);
  
        // run it thru the parser to be safe and get a doc type node
        Document schemaDoc = DocumentHelper.parseText(mondrianSchema);
        byte schemaBytes[] = schemaDoc.asXML().getBytes();
        String schemaFileName = model.getModelName() + ".mondrian.xml"; //$NON-NLS-1$
        // write out the file
        File modelFile = new File("models");
        modelFile.mkdirs();
        modelFile = new File("models/" + model.getModelName() + ".mondrian.xml");
        OutputStream out = new FileOutputStream(modelFile);
        out.write(schemaBytes);
      } catch(Exception e){
        throw new ModelerException("Could not generate Mondrian model",e);
      }
      // now add to the schema catalog
      /*
       * String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl(); boolean enableXmla = true; String
       * schemaSolutionPath = modelId+"/"+schemaFileName; //$NON-NLS-1$ MondrianCatalogHelper.addToCatalog(baseUrl,
       * enableXmla, schemaSolutionPath, session, jndi, true);
       * AggregationManager.instance().getCacheControl(null).flushSchemaCache();
       */

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
	      logger.info("Error writing metadata model",e);
	      throw new ModelerException("Error writing metadata model",e);
	    }

  	} catch (Exception e) {
  		logger.info(e.getLocalizedMessage());
  		new ModelerException(e);
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
        	
    	IModelerSource theSource = ModelerSourceFactory.generateSource(domain.getLogicalModels().get(0).getProperty("source_type").toString());
    	
    	theSource.initialize(domain);  	
  
    	aModel.setDomain(domain);
    	aModel.setModelSource(theSource);
    	aModel.setFileName(fileName);
    } catch (Exception e){
      logger.info(e);
      e.printStackTrace();
      throw new ModelerException("Error loading workspace",e);
    }
  }  
}
