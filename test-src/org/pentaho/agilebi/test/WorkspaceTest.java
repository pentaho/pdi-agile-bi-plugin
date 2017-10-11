/*!
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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.agilebi.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerController;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.agilebi.modeler.strategy.SimpleAutoModelStrategy;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.agilebi.spoon.SpoonModelerWorkspaceHelper;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalColumn;
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
  private IPhysicalColumn physicalColumn;
  private IPhysicalColumn physicalColumn2;

  @BeforeClass
  public static void init() throws Exception {
    //    System.setProperty("org.osjava.sj.root", "test-res/solution1/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
    ModelerMessagesHolder.setMessages( new SpoonModelerMessages() );
    //    KettleEnvironment.init();
    //    Props.init(Props.TYPE_PROPERTIES_EMPTY);
  }


  @Before
  public void setup() {

    String locale = LocaleHelper.getLocale().toString();

    SqlPhysicalModel model = new SqlPhysicalModel();
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setDatabaseName( "SampleData" );
    model.setDatasource( dataSource );
    SqlPhysicalTable table = new SqlPhysicalTable( model );
    model.getPhysicalTables().add( table );
    table.setTargetTableType( TargetTableType.INLINE_SQL );
    table.setTargetTable( "select * from customers" );
    table.setId( "customers" );

    SqlPhysicalColumn column = new SqlPhysicalColumn( table );
    column.setTargetColumn( "customername" );
    column.setName( new LocalizedString( locale, "Customer Name" ) );
    column.setDescription( new LocalizedString( locale, "Customer Name Desc" ) );
    column.setDataType( DataType.STRING );
    column.setId( "cutomer_customername" );

    table.getPhysicalColumns().add( column );

    LogicalModel logicalModel = new LogicalModel();
    model.setId( "MODEL" );
    model.setName( new LocalizedString( locale, "My Model" ) );
    model.setDescription( new LocalizedString( locale, "A Description of the Model" ) );

    LogicalTable logicalTable = new LogicalTable();
    logicalTable.setId( "BT_CUSTOMERS" );
    logicalTable.setPhysicalTable( table );

    logicalModel.getLogicalTables().add( logicalTable );
    logicalModel.setName( new LocalizedString( locale, "My Model" ) );

    logicalColumn1 = new LogicalColumn();
    logicalColumn1.setId( "LC_CUSTOMERNAME" );
    logicalColumn1.setPhysicalColumn( column );
    logicalColumn1.setAggregationType( AggregationType.COUNT );
    logicalColumn1.setLogicalTable( logicalTable );
    logicalColumn1.setDataType( DataType.STRING );

    logicalColumn2 = new LogicalColumn();
    logicalColumn2.setId( "LC_CUSTOMERNUMBER" );
    logicalColumn2.setAggregationType( AggregationType.COUNT );
    logicalColumn2.setPhysicalColumn( column );
    logicalColumn2.setLogicalTable( logicalTable );
    logicalColumn2.setDataType( DataType.NUMERIC );


    logicalTable.addLogicalColumn( logicalColumn1 );
    logicalTable.addLogicalColumn( logicalColumn2 );


    domain = new Domain();
    domain.addPhysicalModel( model );
    domain.addLogicalModel( logicalModel );

    physicalColumn = column;

    SqlPhysicalColumn column2 = new SqlPhysicalColumn( table );
    column2.setTargetColumn( "customername" );
    column2.setName( new LocalizedString( locale, "Customer Number" ) );
    column2.setDescription( new LocalizedString( locale, "Customer Number" ) );
    column2.setDataType( DataType.NUMERIC );
    column2.setId( "customer_customernumber" );

    physicalColumn2 = column2;

    table.getPhysicalColumns().add( physicalColumn2 );
  }


  @Test
  public void testMoveAvailableToMeasures() throws ModelerException {
    ModelerWorkspace work = new ModelerWorkspace( new SpoonModelerWorkspaceHelper() );
    work.setDomain( domain );
    AvailableField field = new AvailableField();
    field.setName( "Test name" );
    field.setPhysicalColumn( physicalColumn );

    work.addMeasure( work.createMeasureForNode( field ) );
    Assert.assertEquals( 1, work.getModel().getMeasures().size() );

  }

  @Test
  public void testMeasuresValidation() {
    ModelerWorkspace work = new ModelerWorkspace( new SpoonModelerWorkspaceHelper() );
    work.setDomain( domain );
    AvailableField field = new AvailableField();
    field.setName( "Test name" );
    field.setPhysicalColumn( physicalColumn );

    work.addMeasure( work.createMeasureForNode( field ) );
    Assert.assertTrue( work.getModel().getMeasures().isValid() );

    work.getModel().getMeasures().get( 0 ).setLogicalColumn( null );

    Assert.assertFalse( work.getModel().getMeasures().isValid() );
  }


  @Test
  public void testDimensionPopulate() {
    ModelerWorkspace work = new ModelerWorkspace( new SpoonModelerWorkspaceHelper() );
    work.setDomain( domain );
    AvailableField field = new AvailableField();
    field.setName( "Test name" );
    field.setPhysicalColumn( physicalColumn );

    work.addDimensionFromNode( work.createColumnBackedNode( field, ModelerPerspective.ANALYSIS ) );

    DimensionMetaDataCollection dims = work.getModel().getDimensions();
    Assert.assertEquals( 1, dims.size() );
    //check that the auto-created level is pointing to the same column
    Assert.assertEquals( field.getPhysicalColumn(),
        dims.get( 0 ).get( 0 ).get( 0 ).getLogicalColumn().getPhysicalColumn() );
    Assert.assertTrue( dims.isValid() );

    dims.get( 0 ).get( 0 ).get( 0 ).setLogicalColumn( null );

    Assert.assertFalse( dims.isValid() );


  }

  @Test
  public void testControllerMoveToMeasures() throws ModelerException {

    ModelerWorkspace work = new ModelerWorkspace( new SpoonModelerWorkspaceHelper() );
    work.setDomain( domain );
    AvailableField availableField = new AvailableField();
    availableField.setName( "Available Field" );
    availableField.setPhysicalColumn( physicalColumn2 );

    ModelerController controller = new ModelerController( work );
    controller.setWorkspaceHelper( work.getWorkspaceHelper() );

    Object[] selectedFields = new Object[ 1 ];
    selectedFields[ 0 ] = availableField;
    controller.setSelectedFields( selectedFields );

    AvailableField dimensionTarget = new AvailableField();
    dimensionTarget.setName( "Dimension Target" );

    dimensionTarget.setPhysicalColumn( physicalColumn );

    work.addDimensionFromNode( work.createColumnBackedNode( dimensionTarget, ModelerPerspective.ANALYSIS ) );

    DimensionMetaDataCollection dimensions = work.getModel().getDimensions();
    DimensionMetaData dimension = dimensions.get( 0 );
    controller.getDimTreeHelper().setTreeSelectionChanged( dimension );

    controller.addField();

    HierarchyMetaData theHierarchy = dimension.get( 0 );
    Assert.assertEquals( "Dimension Target", theHierarchy.getName() );

    LevelMetaData theLevel = theHierarchy.get( 0 );
    Assert.assertEquals( physicalColumn, theLevel.getLogicalColumn().getPhysicalColumn() );
  }


  @Test
  public void testDimensionValidation() {
    ModelerWorkspace work = new ModelerWorkspace( new SpoonModelerWorkspaceHelper() );
    work.setDomain( domain );
    AvailableField field = new AvailableField();
    field.setName( "Test name" );
    field.setPhysicalColumn( physicalColumn );

    work.addDimensionFromNode( work.createColumnBackedNode( field, ModelerPerspective.ANALYSIS ) );

    DimensionMetaDataCollection dims = work.getModel().getDimensions();

    Assert.assertTrue( dims.isValid() );
    dims.get( 0 ).get( 0 ).get( 0 ).setLogicalColumn( null );
    Assert.assertFalse( dims.isValid() );


  }


  @Test
  public void testGetAvailableFields() {
    ModelerWorkspace work = new ModelerWorkspace( new SpoonModelerWorkspaceHelper() );
    work.setDomain( domain );

    Assert.assertEquals( 2, work.getAvailableTables().getChildren().size() );

  }
  @Test
  public void testGeoContext() throws ModelerException {
    GeoContext geoRoles = new GeoContext();
    SpoonModelerWorkspaceHelper helper = new SpoonModelerWorkspaceHelper( geoRoles );
    ModelerWorkspace work = new ModelerWorkspace( helper );
    SimpleAutoModelStrategy autoModelStrategy = doNothingAutoModelStrategy();
    helper.setAutoModelStrategy( autoModelStrategy );
    helper.autoModelFlat( work );
    Assert.assertSame( geoRoles, autoModelStrategy.getGeoContext() );
    Assert.assertSame( geoRoles, work.getGeoContext() );
  }

  private SimpleAutoModelStrategy doNothingAutoModelStrategy() {
    return new SimpleAutoModelStrategy( null ) {
      @Override public void autoModelOlap( final ModelerWorkspace workspace, final MainModelNode mainModel )
          throws ModelerException {
      }

      @Override
      public void autoModelRelational( final ModelerWorkspace workspace, final RelationalModelNode relationalModelNode )
          throws ModelerException {
      }
    };
  }


}
