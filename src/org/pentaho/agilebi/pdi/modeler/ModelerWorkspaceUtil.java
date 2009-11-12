package org.pentaho.agilebi.pdi.modeler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metadata.automodel.SchemaTable;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
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
import org.pentaho.pms.core.exception.PentahoMetadataException;

/** 
 * Utility class for generating ModelerModels for the User Interface.
 * 
 * @author nbaker
 *
 */
public class ModelerWorkspaceUtil {
  public static final String DEFAULT_USER_NAME = "joe";
  private static ModelGenerator generator = new ModelGenerator();
  
  public static ModelerWorkspace createModelFromOutputStep() throws PentahoMetadataException{
    
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
      e.printStackTrace();
      SpoonFactory.getInstance().messageBox( "Could not get transformation step metadata", MODELER_NAME, false, Const.ERROR); //$NON-NLS-1$
      throw new IllegalStateException("Could not get transformation step metadata");
    }
    if(rowMeta == null){
      throw new IllegalStateException("Could not get transformation step metadata");
    }
    
    String schemaName = tableOutputMeta.getSchemaName() != null ? tableOutputMeta.getSchemaName() : "";
    Domain d = generateDomain(databaseMeta, schemaName, tableOutputMeta.getTablename());
    
    OutputStepModelerSource source = new OutputStepModelerSource(tableOutputMeta, databaseMeta, rowMeta);
    ModelerWorkspace model = new ModelerWorkspace();
    model.setModelSource(source);
    model.setModelName(tableOutputMeta.getTablename());
    model.setDomain(d);
    
    return model;
  }
  
  public static Domain generateDomain(DatabaseMeta databaseMeta, String schemaName, String tableName) throws PentahoMetadataException{

    
    //modelName, databaseMeta, , "joe", tableOutputMeta.getTablename(), profiles);
    String locale = Locale.getDefault().toString();
    generator.setLocale(locale);
    generator.setDatabaseMeta(databaseMeta);
    generator.setModelName(tableName);

    SchemaTable tableNames[] = new SchemaTable[1];
    // TODO: support schema names.
    tableNames[0] = new SchemaTable(schemaName, tableName);
    generator.setTableNames(tableNames);
    Domain domain = generator.generateDomain();
    domain.setId(tableName); //replaced with user specified name later

    LogicalModel businessModel = domain.getLogicalModels().get(0); // schemaMeta.getActiveModel();

    // TODO do this with messages
    businessModel.setName(new LocalizedString(locale, tableName));
    businessModel.setDescription(new LocalizedString(locale, "This is the data model for "+businessModel.getName(locale)));
    
    LogicalTable businessTable = businessModel.getLogicalTables().get(0);
    businessTable.setName(new LocalizedString( locale , "Available Columns" ));
    
    
    // is this necessary
    // businessTable.getsetTargetTable( tableName );
    SecurityOwner owner = new SecurityOwner(SecurityOwner.OwnerType.USER, DEFAULT_USER_NAME );

    Metadata meta = new Metadata();

    // TODO: investigate and replace this magic number with named constant?
    int rights = 31; 
    meta.setUserAccess(DEFAULT_USER_NAME, rights, businessModel );
    meta.setUserAccess(DEFAULT_USER_NAME, rights, businessTable );
    
    return domain;
  }
  
  public static void populateDomain(ModelerWorkspace model){
    List<Category> cats = model.getDomain().getLogicalModels().get(0).getCategories();
    LogicalTable logicalTable = model.getDomain().getLogicalModels().get(0).getLogicalTables().get(0);
    
    Category cat;
    // Find existing category or create new one
    if(cats.size() > 0){
      cat = cats.get(0);
      cat.setId(model.getModelName());
    } else {
      cat = new Category();
      cat.setId(model.getModelName());
      model.getDomain().getLogicalModels().get(0).addCategory(cat);
    }
    cat.getLogicalColumns().clear();
    
    // Add all measures
    for(FieldMetaData f : model.getFields()){
      LogicalColumn lCol = f.getLogicalColumn();
      if( f.getFormat() != null ) {
        // TODO: set mask
        //lCol.setFormat( f.getFormat());
      }
      lCol.setName(new LocalizedString(Locale.getDefault().toString(), f.getDisplayName()));
      lCol.setAggregationType(AggregationType.valueOf(f.getAggTypeDesc()));
      cat.addLogicalColumn(lCol);
    }
    
    
    // Add levels
    for (DimensionMetaData dim : model.getDimensions()) {
      for (HierarchyMetaData hier : dim.getChildren()) {
        for (int j = 0; j < hier.getChildren().size(); j++) {
          LevelMetaData level = hier.getChildren().get(j);
          String format = "#";

          LogicalColumn businessColumn = null;
          String columnId1 = level.getColumnName();
          for( int bizCol=0; bizCol < logicalTable.getLogicalColumns().size(); bizCol++ ) {
            LogicalColumn column = logicalTable.getLogicalColumns().get(bizCol);
            String bizColumnId = ((SqlPhysicalTable)logicalTable.getPhysicalTable()).getTargetTable() + "." + ((SqlPhysicalColumn)column.getPhysicalColumn()).getTargetColumn(); //$NON-NLS-1$
            String columnId2 = ((SqlPhysicalTable)logicalTable.getPhysicalTable()).getTargetTable() + "." + level.getColumnName(); //$NON-NLS-1$
            if( columnId1.equalsIgnoreCase( bizColumnId ) || columnId2.equalsIgnoreCase( bizColumnId ) ) {
              businessColumn = column;
              break;
            }
          }
          
          if( businessColumn != null  ) {
            // TODO: handle custom formating
            if( format != null ) {
              businessColumn.setProperty("mask", format );
            }
            businessColumn.setName(new LocalizedString(Locale.getDefault().toString(), level.getName()));
            if( cat.findLogicalColumn(businessColumn.getId()) == null ) {
              cat.addLogicalColumn(businessColumn);
            }
          }
        }
      }
    }
    
    
    // =========================== OLAP ===================================== //

    try{
      List<OlapDimensionUsage> usages = new ArrayList<OlapDimensionUsage>();
      List<OlapDimension> olapDimensions = new ArrayList<OlapDimension>();
      List<OlapMeasure> measures = new ArrayList<OlapMeasure>();
      
      for (DimensionMetaData dim : model.getDimensions()) {

        OlapDimension dimension = new OlapDimension();
        String dimTitle = dim.getName();
        
        dimension.setName( dimTitle );
        dimension.setTimeDimension( dim.isTime() );

        List<OlapHierarchy> hierarchies = new ArrayList<OlapHierarchy>();
        
        // create a default hierarchy
        OlapHierarchy defaultHierarchy = new OlapHierarchy( dimension );
        defaultHierarchy.setLogicalTable( logicalTable );
        hierarchies.add( defaultHierarchy );
        
        
        for (HierarchyMetaData hier : dim.getChildren()) {
          OlapHierarchy hierarchy = new OlapHierarchy( dimension );
          hierarchy.setLogicalTable( logicalTable );
          List<OlapHierarchyLevel> levels = new ArrayList<OlapHierarchyLevel>();
          
          for (LevelMetaData lvl : hier.getChildren()) {
//          System.out.println( "level "+child.getId() );
            OlapHierarchyLevel level = new OlapHierarchyLevel( hierarchy );
            level.setName( lvl.getName() );
            level.setReferenceColumn( lvl.getLogicalColumn() );
            level.setHavingUniqueMembers( lvl.isUniqueMembers() );
            levels.add( level );
          }

          hierarchy.setHierarchyLevels( levels );
          hierarchies.add(hierarchy);
        }
        
        dimension.setHierarchies( hierarchies );
        
        olapDimensions.add( dimension );
        OlapDimensionUsage usage = new OlapDimensionUsage( dimension.getName(), dimension );
        usages.add( usage );
        
         
      }
      
      OlapCube cube = new OlapCube();
      cube.setLogicalTable( logicalTable );
      // TODO find a better way to generate default names
      cube.setName( model.getModelName() + " Cube" );
      cube.setOlapDimensionUsages(usages);

      for(FieldMetaData f : model.getFields()){

        OlapMeasure measure = new OlapMeasure();
        measure.setName( f.getFieldName() );
        measure.setLogicalColumn( f.getLogicalColumn() );
        measures.add( measure );
      }
      
      cube.setOlapMeasures( measures );
      
      LogicalModel lModel = model.getDomain().getLogicalModels().get(0); 
      
      lModel.setProperty("olap_dimensions", olapDimensions );
      List<OlapCube> cubes = new ArrayList<OlapCube>();
      cubes.add(cube);
      lModel.setProperty("olap_cubes", cubes );
      
      MondrianModelExporter exporter = new MondrianModelExporter(lModel, Locale.getDefault().toString());
      String mondrianSchema = exporter.createMondrianModelXML();
      System.out.println(mondrianSchema);
      
      
      
      // run it thru the parser to be safe and get a doc type node
      Document schemaDoc = DocumentHelper.parseText(mondrianSchema);
      byte schemaBytes[] = schemaDoc.asXML().getBytes();
      String schemaFileName = model.getModelName()+".mondrian.xml"; //$NON-NLS-1$
      // write out the file
      File file = new File( "models" );
      file.mkdirs();
      file = new File( "models/"+model.getModelName()+".mondrian.xml" );
      OutputStream out = new FileOutputStream( file );
      out.write( schemaBytes );
      // now add to the schema catalog
      /*
      String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
      boolean enableXmla = true;
      String schemaSolutionPath = modelId+"/"+schemaFileName; //$NON-NLS-1$
      MondrianCatalogHelper.addToCatalog(baseUrl, enableXmla, schemaSolutionPath, session, jndi, true);
      AggregationManager.instance().getCacheControl(null).flushSchemaCache();
      */
    } catch (Throwable e) {
      // TODO log this
      e.printStackTrace();
    }
          
    String domainName = model.getDomain().getId();
    XmiParser parser = new XmiParser();
    String xmi = parser.generateXmi(model.getDomain());
    try {
      // write the XMI to a tmp file
      // models was created earlier.
      File dir = new File( "models/" + model.getModelName());
      dir.mkdirs();
      File file = new File( "models/" + model.getModelName() + "/metadata.xmi" );
      PrintWriter pw = new PrintWriter(new FileWriter(file));
      pw.print(xmi);
      pw.close();
    } catch (Throwable e) {
      e.printStackTrace();
    }

  }
  
  
}
