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
package org.pentaho.agilebi.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.ModelerController;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.spoon.SpoonModelerWorkspaceHelper;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlDataSource;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TargetTableType;
import org.pentaho.pms.messages.util.LocaleHelper;

public class WorkspaceTest {

  private Domain domain;
  private LogicalColumn logicalColumn1;
  private LogicalColumn logicalColumn2;
  
  @Before
  public void setup(){
    
    String locale = LocaleHelper.getLocale().toString();
    
    SqlPhysicalModel model = new SqlPhysicalModel();
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setDatabaseName("SampleData");
    model.setDatasource(dataSource);
    SqlPhysicalTable table = new SqlPhysicalTable(model);
    model.getPhysicalTables().add(table);
    table.setTargetTableType(TargetTableType.INLINE_SQL);
    table.setTargetTable("select * from customers");
    
    SqlPhysicalColumn column = new SqlPhysicalColumn(table);
    column.setTargetColumn("customername");
    column.setName(new LocalizedString(locale, "Customer Name"));
    column.setDescription(new LocalizedString(locale, "Customer Name Desc"));
    column.setDataType(DataType.STRING);
    
    table.getPhysicalColumns().add(column);
    
    LogicalModel logicalModel = new LogicalModel();
    model.setId("MODEL");
    model.setName(new LocalizedString(locale, "My Model"));
    model.setDescription(new LocalizedString(locale, "A Description of the Model"));
    
    LogicalTable logicalTable = new LogicalTable();
    logicalTable.setPhysicalTable(table);
    
    logicalModel.getLogicalTables().add(logicalTable);
    
    logicalColumn1 = new LogicalColumn();
    logicalColumn1.setId("LC_CUSTOMERNAME");
    logicalColumn1.setPhysicalColumn(column);
    logicalColumn1.setAggregationType(AggregationType.COUNT);

    logicalColumn2 = new LogicalColumn();
    logicalColumn2.setId("LC_CUSTOMERNUMBER");
    logicalColumn2.setAggregationType(AggregationType.COUNT);
    logicalColumn2.setPhysicalColumn(column);


    logicalTable.addLogicalColumn(logicalColumn1);
    logicalTable.addLogicalColumn(logicalColumn2);
    
    
    domain = new Domain();
    domain.addPhysicalModel(model);
    domain.addLogicalModel(logicalModel);
    
    
    
  }
  
  @Test
  public void testMoveAvailableToMeasures() throws ModelerException{
    ModelerWorkspace work = new ModelerWorkspace(new SpoonModelerWorkspaceHelper());
    work.setDomain(domain);
    AvailableField field = new AvailableField();
    field.setName("Test name");
    field.setLogicalColumn(logicalColumn1);
    work.getAvailableFields().add(field);
    
    work.addMeasure(work.createMeasureForNode(field));
    Assert.assertEquals(1, work.getModel().getMeasures().size());
    
    work.getWorkspaceHelper().populateDomain(work);
    
    
    // one logicalColumn in the category
    Assert.assertEquals(1, work.getDomain().getLogicalModels().get(0).getCategories().get(0).getLogicalColumns().size());
    LogicalColumn col = work.getDomain().getLogicalModels().get(0).getCategories().get(0).getLogicalColumns().get(0);
    
    Assert.assertEquals(col, logicalColumn1);
    
    
    
  }
  
  @Test
  public void testMeasuresValidation(){
    ModelerWorkspace work = new ModelerWorkspace(new SpoonModelerWorkspaceHelper());
    work.setDomain(domain);
    AvailableField field = new AvailableField();
    field.setName("Test name");
    field.setLogicalColumn(logicalColumn1);
    work.getAvailableFields().add(field);
    
    work.addMeasure(work.createMeasureForNode(field));
    Assert.assertTrue(work.getModel().getMeasures().isValid());
    
    work.getModel().getMeasures().get(0).setLogicalColumn(null);
    
    Assert.assertFalse(work.getModel().getMeasures().isValid());
  }
  

  @Test
  public void testDimensionPopulate(){
    ModelerWorkspace work = new ModelerWorkspace(new SpoonModelerWorkspaceHelper());
    work.setDomain(domain);
    AvailableField field = new AvailableField();
    field.setName("Test name");
    field.setLogicalColumn(logicalColumn1);
    work.getAvailableFields().add(field);
    
    work.addDimensionFromNode(field);
    
    DimensionMetaDataCollection dims = work.getModel().getDimensions();
    Assert.assertEquals(1, dims.size());
    //check that the auto-created level is pointing to the same column
    Assert.assertEquals(field.getLogicalColumn(), dims.get(0).get(0).get(0).getLogicalColumn());
    Assert.assertTrue(dims.isValid());
    
    dims.get(0).get(0).get(0).setLogicalColumn(null);
    
    Assert.assertFalse(dims.isValid());
    
    
  }
  
  @Test
  public void testControllerMoveToMeasures(){

    ModelerWorkspace work = new ModelerWorkspace(new SpoonModelerWorkspaceHelper());
    AvailableField availableField = new AvailableField();
    availableField.setName("Available Field");
    availableField.setLogicalColumn(logicalColumn2);
    work.getAvailableFields().add(availableField);
    ModelerController controller = new ModelerController(work);
    
    Object[] selectedFields = new Object[1];
    selectedFields[0] = availableField;
    controller.setSelectedFields(selectedFields);
    
    AvailableField dimensionTarget = new AvailableField();
    dimensionTarget.setName("Dimension Target");
    dimensionTarget.setLogicalColumn(logicalColumn1);
    work.getAvailableFields().add(dimensionTarget);
    work.addDimensionFromNode(dimensionTarget);
    
    DimensionMetaDataCollection dimensions = work.getModel().getDimensions();
    DimensionMetaData dimension = dimensions.get(0);
    controller.setDimTreeSelectionChanged(dimension);

    controller.addField();
    
    HierarchyMetaData theHierarchy = dimension.get(0);
    Assert.assertEquals("Dimension Target", theHierarchy.getName());
    
    LevelMetaData theLevel = theHierarchy.get(0);
    Assert.assertEquals(logicalColumn1, theLevel.getLogicalColumn());
  }
  


  @Test
  public void testDimensionValidation(){
    ModelerWorkspace work = new ModelerWorkspace(new SpoonModelerWorkspaceHelper());
    work.setDomain(domain);
    AvailableField field = new AvailableField();
    field.setName("Test name");
    field.setLogicalColumn(logicalColumn1);
    work.getAvailableFields().add(field);
    work.addDimensionFromNode(field);
    
    DimensionMetaDataCollection dims = work.getModel().getDimensions();
    
    Assert.assertTrue(dims.isValid());
    dims.get(0).get(0).get(0).setLogicalColumn(null);
    Assert.assertFalse(dims.isValid());
    
    
  }
  

  @Test
  public void testGetAvailableFields(){
    ModelerWorkspace work = new ModelerWorkspace(new SpoonModelerWorkspaceHelper());
    work.setDomain(domain);
    
    Assert.assertEquals(2, work.getAvailableFields().size());
    
  }
  
}
