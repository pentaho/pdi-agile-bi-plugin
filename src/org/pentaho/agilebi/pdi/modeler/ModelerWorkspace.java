package org.pentaho.agilebi.pdi.modeler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalTable;
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
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelList;


/**
 * UI model behind the XUL-based interface. This class contains a reference from the context in
 * which the modeling was initiated through an IModelerSource which also provides model generation.
 * 
 * @author nbaker
 *
 */
public class ModelerWorkspace extends XulEventSourceAdapter{


  private FieldsCollection inPlayFields = new FieldsCollection();
  
  private DimensionMetaDataCollection dimensions = new DimensionMetaDataCollection();
  
  private List<FieldMetaData> availableFields = new ArrayList<FieldMetaData>();
  
  private String sourceName;
  
  private String modelName;
  
  private IModelerSource source;

  private String selectedServer;
  
  private String selectedVisualization;
  
  private String schemaName;
  
  private Domain domain;
  
  public ModelerWorkspace(){
    
    inPlayFields.addPropertyChangeListener(new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent arg0) {
        fireFieldsChanged();
      }
    });
    dimensions.addPropertyChangeListener(new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent arg0) {
        fireDimensionsChanged();
      }
    });
  }
  
  //transMeta.getFilename()
  public String getSourceName(){
    return sourceName;
  }
  
  public void setSourceName(String sourceName){
    this.sourceName = sourceName;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public int getNumberLevels() {
    int v = 0;
    for (DimensionMetaData dim : dimensions) {
      for (HierarchyMetaData hier : dim.getChildren()) {
        for (LevelMetaData lvl : hier.getChildren()) {
          v++;
        }
      }
    }
    return v;
  }
  
  
  public List<FieldMetaData> getAvailableFields() {
    return availableFields;
  }
  
  
  public void setSelectedServer(String server){
    this.selectedServer = server;
  }
  
  public String getSelectedServer(){
    return selectedServer;
  }
  

  public void setSelectedVisualization(String aVisualization) {
  	this.selectedVisualization = aVisualization;
  }  
  
  public String getSelectedVisualization() {
  	return this.selectedVisualization;
  }


  
  public void publishToServer() throws Exception {
    publishOlapSchemaToServer( this.modelName+".mondrian.xml" );
  }
  
  public void addDimension(Object obj){

    DimensionMetaData dimension = new DimensionMetaData(obj.toString());
    HierarchyMetaData hierarchy = new HierarchyMetaData(obj.toString());
    hierarchy.setParent(dimension);
    LevelMetaData level = new LevelMetaData(hierarchy, obj.toString());

    // TODO: remove lookup
    LogicalColumn col = findLogicalColumn(obj.toString());
    level.setLogicalColumn(col);
    
    dimension.getChildren().add(hierarchy);
    hierarchy.getChildren().add(level);

    addDimension(dimension);
  }
  public void addDimension(DimensionMetaData dim){
    this.dimensions.add(dim);
  }
  
  public void addToHeirarchy(Object selectedItem, Object newItem){
    if (selectedItem instanceof LevelMetaData) {
      LevelMetaData sib = (LevelMetaData)selectedItem;
      LevelMetaData level = new LevelMetaData(sib.getParent(), newItem.toString());

      // TODO: remove lookup
      LogicalColumn col = findLogicalColumn(newItem.toString());
      level.setLogicalColumn(col);
      
      sib.getParent().getChildren().add(level);
      this.firePropertyChange("dimensions", null , dimensions);
    } else if (selectedItem instanceof HierarchyMetaData) {
      HierarchyMetaData hier = (HierarchyMetaData) selectedItem;
      LevelMetaData level = new LevelMetaData(hier, newItem.toString());

      // TODO: remove lookup
      LogicalColumn col = findLogicalColumn(newItem.toString());
      level.setLogicalColumn(col);
      
      hier.getChildren().add(level);
      this.firePropertyChange("dimensions", null , dimensions);
    } else if (selectedItem instanceof DimensionMetaData) {
      DimensionMetaData dim = (DimensionMetaData)selectedItem;
      HierarchyMetaData hier = null;

      if (dim.getChildren().size() > 0) {
        hier = dim.getChildren().get(0);
      } else {
        hier = new HierarchyMetaData(newItem.toString());
        hier.setParent(dim);
        dim.getChildren().add(hier);
      }
      LevelMetaData level = new LevelMetaData(hier, newItem.toString());
      hier.getChildren().add(level);
      // TODO: remove lookup
      LogicalColumn col = findLogicalColumn(newItem.toString());
      level.setLogicalColumn(col);
      this.firePropertyChange("dimensions", null , dimensions);
    }
  }

  private void fireFieldsChanged(){
    firePropertyChange("fields", null, inPlayFields);
  }
  
  private void fireDimensionsChanged(){
    firePropertyChange("dimensions", null, dimensions);
  }
  
  // Restore once SetListBox is returning bound objects instead of Strings
  //public void addFieldIntoPlay(Object selectedField){
  //  
  //  FieldMetaData selected = (FieldMetaData) selectedField;
  //  selected.setRowNum(Integer.toString(inPlayFields.size()+1));
  //  
  //  this.inPlayFields.add(selected); //$NON-NLS-1$
  //  
  //  this.firePropertyChange("fields", null, inPlayFields);
  //}
  
  public void addFieldIntoPlay(Object selectedField){
    FieldMetaData meta = new FieldMetaData(Integer.toString(inPlayFields.size()+1), selectedField.toString(), "", selectedField.toString());
    
    // TODO: replace this terrible resolution with better model code.
    LogicalColumn col = findLogicalColumn(selectedField.toString());
    meta.setLogicalColumn(col);
    this.inPlayFields.add(meta); //$NON-NLS-1$
    
    
    this.firePropertyChange("fields", null, inPlayFields);
  }
  
  private LogicalColumn findLogicalColumn(String id){
    LogicalColumn col = null;
    for(LogicalColumn c : domain.getLogicalModels().get(0).getLogicalTables().get(0).getLogicalColumns()){
      
      if(c.getName(Locale.getDefault().toString()).equals(id)){
        col = c;
        break;
      }
    }
    return col;
  }
  
  public void removeFieldFromPlay(FieldMetaData field){
    this.inPlayFields.remove(field);
    this.firePropertyChange("fields", null, inPlayFields);
  }
  
  public void publishOlapSchemaToServer( String schemaFilePath ) throws Exception {
    
    String publishPath = "models";
    File publishFile = new File( "models/"+schemaFilePath );
    String jndiName = source.getDatabaseName(); // assume JNDI for now
    boolean enableXmla = false;
    
    ModelServerPublish publisher = new ModelServerPublish();
    publisher.setBiServerId(selectedServer);
    int result = publisher.publish(publishPath, publishFile, jndiName, schemaName, enableXmla);
    String serverName = publisher.getServerName();
    switch (result) {
      case ModelServerPublish.PUBLISH_CATALOG_EXISTS: {
        SpoonFactory.getInstance().messageBox( "Catalog exists already", "Publish To Server: "+serverName, false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_DATASOURCE_PROBLEM: {
        SpoonFactory.getInstance().messageBox( "Datasource problem", "Publish To Server: "+serverName, false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_FAILED: {
        SpoonFactory.getInstance().messageBox( "Publish failed", "Publish To Server: "+serverName, false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_FILE_EXISTS: {
        SpoonFactory.getInstance().messageBox( "File exists already", "Publish To Server: "+serverName, false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_INVALID_PASSWORD: {
        SpoonFactory.getInstance().messageBox( "Invalid pssword", "Publish To Server: "+serverName, false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_INVALID_USER_OR_PASSWORD: {
        SpoonFactory.getInstance().messageBox( "Invalid user id or password", "Publish To Server: "+serverName, false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_SUCCESS: {
        SpoonFactory.getInstance().messageBox( "Publish was successful", "Publish To Server: "+serverName, false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_UNKNOWN_PROBLEM: {
        SpoonFactory.getInstance().messageBox( "Unknown problem encountered while publishing", "Publish To Server: "+serverName, false, Const.ERROR);
        break;
      }
    }
  }
  

  public void setModelSource(IModelerSource source) {
    this.source = source;
  }

  public IModelerSource getModelSource() {
    return source;
  }
    

  public List<FieldMetaData> getFields() {
    return inPlayFields;
  }
  
  public void setFields(List<FieldMetaData> fields){
    this.inPlayFields.clear();
    this.inPlayFields.addAll(fields);
    
  }

 public void refresh(){
    try {
      Domain newDomain = source.generateDomain();
      //ModelerWorkspaceUtil.updateDomain(domain, newDomain);
      
      // Add in new logicalColumns
      outer:
      for(LogicalColumn lc : newDomain.getLogicalModels().get(0).getLogicalTables().get(0).getLogicalColumns()){
        boolean exists = false;
        inner:
        for(FieldMetaData fmd : this.availableFields){
          if(fmd.getLogicalColumn().getId().equals(lc.getId())){
            fmd.setLogicalColumn(lc);
            exists = true;
            break inner;
          }
        }
        if(!exists){
          FieldMetaData fm = new FieldMetaData();
          fm.setLogicalColumn(lc);
          fm.setFieldName(lc.getName(Locale.getDefault().toString()));
          availableFields.add(fm);
          Collections.sort(availableFields, new Comparator<FieldMetaData>(){
            public int compare(FieldMetaData arg0, FieldMetaData arg1) {
              return arg0.getLogicalColumn().getId().compareTo(arg1.getLogicalColumn().getId());
            }
          });
        }
      }
      
      // Remove logicalColumns that no longer exist.
      List<FieldMetaData> toRemove = new ArrayList<FieldMetaData>();
      for(FieldMetaData fm : availableFields){
        boolean exists = false;
        LogicalColumn fmlc = fm.getLogicalColumn();
        inner:
        for(LogicalColumn lc : newDomain.getLogicalModels().get(0).getLogicalTables().get(0).getLogicalColumns()){
          if(lc.getId().equals(fmlc.getId())){
            exists = true;
            break inner;
          }
        }
        if(!exists){
          toRemove.add(fm);
        }
      }
      availableFields.removeAll(toRemove);
      fireFieldsChanged();
      
      for(DimensionMetaData dm : dimensions){
        for(HierarchyMetaData hm : dm.getChildren()){
          for(LevelMetaData lm : hm.getChildren()){
            String existingLmId = lm.getLogicalColumn().getId();
            boolean found = false;
            inner:
            for(FieldMetaData fm : availableFields){
              if(fm.getLogicalColumn().getId().equals(existingLmId)){
                found = true;
                break inner;
              }
            }
            if(!found){
              lm.getParent().remove(lm);
            }
          }
        }
      }
      //fireDimensionsChanged();
      
    } catch (PentahoMetadataException e) {
      e.printStackTrace();
    }
  }

  public DimensionMetaDataCollection getDimensions(){
    return dimensions;
  }
  
  public String getDatabaseName(){
    return source.getDatabaseName();
  }
  
  public String getSchemaName(){
    return schemaName; 
  }
  
  public void setSchemaName(String schemaName){
    this.schemaName = schemaName;
  }
  
  public void setDomain(Domain d){
    this.domain = d;
    this.dimensions.clear();
    this.inPlayFields.clear();
    this.availableFields.clear();
    
    LogicalTable table = domain.getLogicalModels().get(0).getLogicalTables().get(0);
    for(LogicalColumn c : table.getLogicalColumns()){
      FieldMetaData fm = new FieldMetaData();
      fm.setLogicalColumn(c);
      fm.setFieldName(c.getName(Locale.getDefault().toString()));
      availableFields.add(fm);
    }
    
    firePropertyChange("availableFields", null, getAvailableFields());
  }
  
  public Domain getDomain(){
    return updateDomain();
  }
  
  private Domain updateDomain(){
    // TODO: update domain with changes
    return domain;
  }
  
  

  public static class FieldsCollection extends AbstractModelList<FieldMetaData>{
    
  }

  public static class DimensionMetaDataCollection extends AbstractModelList<DimensionMetaData>{
    
  }
  
  
}