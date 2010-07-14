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
package org.pentaho.agilebi.spoon;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;
import org.pentaho.agilebi.modeler.*;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.spoon.ModelerSourceFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.*;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.*;
import org.pentaho.metadata.util.Util;
import org.pentaho.metadata.util.XmiParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 
 * Utility class for generating ModelerModels for the User Interface.
 * 
 * @author nbaker
 *
 */
public class ModelerWorkspaceUtil {

  private static final List<AggregationType> DEFAULT_AGGREGATION_LIST = new ArrayList<AggregationType>();
  private static final List<AggregationType> DEFAULT_NON_NUMERIC_AGGREGATION_LIST = new ArrayList<AggregationType>();

  
  private static Log logger = LogFactory.getLog(ModelerWorkspaceUtil.class);
  
  static {
    DEFAULT_AGGREGATION_LIST.add(AggregationType.NONE);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.SUM);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.AVERAGE);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.MINIMUM);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.MAXIMUM);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.COUNT);
    DEFAULT_AGGREGATION_LIST.add(AggregationType.COUNT_DISTINCT);
    
    DEFAULT_NON_NUMERIC_AGGREGATION_LIST.add(AggregationType.NONE);
    DEFAULT_NON_NUMERIC_AGGREGATION_LIST.add(AggregationType.COUNT);
    DEFAULT_NON_NUMERIC_AGGREGATION_LIST.add(AggregationType.COUNT_DISTINCT);

  }
  
  public static ModelerWorkspace populateModelFromSource( ModelerWorkspace model, IModelerSource source) throws
                                                                                                         ModelerException {
    Domain d = source.generateDomain();
    
    model.setModelSource(source);
    model.setModelName(source.getTableName());
    model.setDomain(d);
    
    return model;  
  }
  
  private static String MODELER_NAME = "OutputStepModeler"; //$NON-NLS-1$

  
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
      if (f.getLogicalColumn() == null) {
        continue;
      }
      LogicalColumn lCol = logicalModel.findLogicalColumn(f.getLogicalColumn().getId());
      if (cat.getLogicalColumns().contains(lCol)) {
        // clone the logical column
        // all measures must have a unique logical column
        // because of different names and aggregates
        lCol = (LogicalColumn)lCol.clone();
        lCol.setId(Util.uniquify(lCol.getId(), logicalModel.getLogicalTables().get(0).getLogicalColumns()));
        logicalModel.getLogicalTables().get(0).addLogicalColumn(lCol);
        f.setLogicalColumn(lCol);
      }
      
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
      
      // All Measures get a list of aggs to choose from within metadata
      // eventually this will be customizable
      
      if (lCol.getDataType() != DataType.NUMERIC) {
        lCol.setAggregationList(DEFAULT_NON_NUMERIC_AGGREGATION_LIST);
      } else {
        lCol.setAggregationList(DEFAULT_AGGREGATION_LIST);
      }
      cat.addLogicalColumn(lCol);
    }

    // Add levels
    for (DimensionMetaData dim : model.getModel().getDimensions()) {
      for (HierarchyMetaData hier : dim) {
        for (int j = 0; j < hier.size(); j++) {
          LevelMetaData level = hier.get(j);
          if (level.getLogicalColumn() == null) {
            continue;
          }
          LogicalColumn lCol = logicalModel.findLogicalColumn(level.getLogicalColumn().getId());
          if(cat.getLogicalColumns().contains(lCol)){
            continue;
          }
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
            if (lvl.getLogicalColumn() != null) {
              LogicalColumn lvlColumn = logicalModel.findLogicalColumn(lvl.getLogicalColumn().getId());
              level.setReferenceColumn(lvlColumn);
            }
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
      //cube.setName( BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.CubeName", model.getModelName() ) ); //$NON-NLS-1$
      cube.setName( model.getModelName() ); //$NON-NLS-1$
      cube.setOlapDimensionUsages(usages);

      for (MeasureMetaData f : model.getModel().getMeasures()) {

        OlapMeasure measure = new OlapMeasure();
        if (f.getAggTypeDesc() != null) {
          f.getLogicalColumn().setAggregationType(AggregationType.valueOf(f.getAggTypeDesc()));
        }
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
	      OutputStream out = new FileOutputStream(file);
	      out.write(xmi.getBytes("UTF-8"));
	      out.flush();
	      out.close();
	      
	    } catch(IOException e){
	      logger.info(BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.BadGenerateMetadata"),e); //$NON-NLS-1$
	      throw new ModelerException(BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.BadGenerateMetadata"),e); //$NON-NLS-1$
	    }

  	} catch (Exception e) {
  		logger.error("error", e);
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
      Domain domain = parser.parseXmi(new ByteArrayInputStream(aXml.getBytes("UTF-8")));

      LogicalModel logical = domain.getLogicalModels().get(0);
      
      Object agileBiProp = logical.getProperty("AGILE_BI_GENERATED_SCHEMA");
      if(agileBiProp == null || "FALSE".equals(agileBiProp)){
        throw new IncompatibleModelerException();
      }
        	
      // re-hydrate the source
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
        ModelerWorkspaceUtil.populateDomain(aModel);
    	aModel.setDirty(false);
    } catch (Exception e){
      logger.error("error", e);
      if(e instanceof ModelerException){
        throw (ModelerException) e;
      }
      throw new ModelerException(BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.LoadWorkspace.Failed"),e); //$NON-NLS-1$
    }
  }  
}
