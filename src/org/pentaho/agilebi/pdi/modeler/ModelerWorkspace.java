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

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.pentaho.metadata.util.ThinModelConverter;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelList;
import org.pentaho.ui.xul.util.AbstractModelNode;


/**
 * UI model behind the XUL-based interface. This class contains a reference from the context in
 * which the modeling was initiated through an IModelerSource which also provides model generation.
 * 
 * @author nbaker
 *
 */
@SuppressWarnings("unchecked")
public class ModelerWorkspace extends XulEventSourceAdapter{

  private FieldsCollection inPlayFields = new FieldsCollection();
  
  private DimensionMetaDataCollection dimensions = new DimensionMetaDataCollection();
  
  private List<FieldMetaData> availableFields = new ArrayList<FieldMetaData>();
  
  private MainModelNode model = new MainModelNode();
  
  private String sourceName;
  
  private String modelName;
  
  private IModelerSource source;

  private String selectedServer;
  
  private String selectedVisualization;
  
  private String schemaName;
  
  private Domain domain;
  
  private boolean dirty;
  
  // full path to file
  private String fileName;
  
  public ModelerWorkspace(){
    inPlayFields.addPropertyChangeListener(new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent arg0) {
        fireFieldsChanged();
      }
    });
    dimensions.addPropertyChangeListener(new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent arg0) {
        fireFieldsChanged();
      }
    });
    model.add(inPlayFields); 
    model.add(dimensions);

    BiServerConfig biServerConfig = BiServerConfig.getInstance();
    List<String> serverNames = biServerConfig.getServerNames();
    if( serverNames.size() > 0 ) {
      selectedServer = serverNames.get(0);
    }
   
  }

  public MainModelNode getModel() {
    return model;
  }

  public void setModel(MainModelNode model) {
    this.model = model;
  }

  public void setFileName(String fileName) {
    String prevVal = this.fileName;
    String prevFriendly = getShortFileName();
    
    this.fileName = fileName;
    firePropertyChange("fileName", prevVal, fileName);
    firePropertyChange("shortFileName", prevFriendly, getShortFileName());
  }
  
  public String getShortFileName(){

    if( fileName == null ) {
      return null;
    }
    int extensionPos = fileName.lastIndexOf('.');
    if( extensionPos == -1 ) {
      extensionPos = fileName.length();
    }
    int sepPos = fileName.replace('\\', '/').lastIndexOf('/');
    if( sepPos == -1 ) {
      sepPos = 0;
    } else {
      sepPos++;
    }
    return fileName.substring(sepPos, extensionPos);    
  }
  
  public String getFileName() {
    return fileName;
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
    String prevVal = this.modelName;
    this.modelName = modelName;
    setDirty(true);
    this.firePropertyChange("modelName", prevVal, this.modelName);
  }
  
  public boolean isDirty(){
    return dirty;
  }
 
  public void setDirty(boolean dirty){
    boolean prevVal = this.dirty;
    this.dirty = dirty;
    this.firePropertyChange("dirty", prevVal, this.dirty);
  }

  public int getNumberLevels() {
    int v = 0;
    for (DimensionMetaData dim : dimensions) {
      for (HierarchyMetaData hier : dim) {
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

  public DimensionMetaData createDimension(Object obj) {
    DimensionMetaData dimension = new DimensionMetaData(obj.toString());
    HierarchyMetaData hierarchy = createHierarchy(dimension, obj);
    hierarchy.setParent(dimension);
    dimension.add(hierarchy);
    return dimension;
  }
  
  public void addDimension(Object obj){
    addDimension(createDimension(obj));
  }

  public void addDimension(DimensionMetaData dim){
    this.dimensions.add(dim);
  }
  
  public DimensionMetaData findDimension(DimensionMetaData dim) {
    for (DimensionMetaData d : dimensions) {
      if (d.equals(dim)) {
        return d;
      }
    }
    return null;
  }

  public HierarchyMetaData findHierarchy(HierarchyMetaData hier) {
    for (DimensionMetaData d : dimensions) {
      if (d.equals(hier.getParent())) {
        for (HierarchyMetaData h : d) {
          if (h.equals(hier)) {
            return h;
          }
        }
      }
    }
    return null;
  }

  public LevelMetaData createLevel(HierarchyMetaData parent, Object obj) {
    LevelMetaData level = new LevelMetaData(parent, obj.toString());
    level.setParent(parent);
    // TODO: remove lookup
    LogicalColumn col = findLogicalColumn(obj.toString());
    level.setLogicalColumn(col);
    return level;
  }
  
  public HierarchyMetaData createHierarchy(DimensionMetaData parent, Object obj) {
    HierarchyMetaData hier = new HierarchyMetaData(obj.toString());
    hier.setParent(parent);
    LevelMetaData level = createLevel(hier, obj.toString());
    hier.add(level);
    return hier;
  }
  
  public void addToHeirarchy(Object selectedItem, Object newItem){
    if (selectedItem instanceof LevelMetaData) {
      LevelMetaData sib = (LevelMetaData)selectedItem;
      LevelMetaData level = createLevel(sib.getParent(), newItem);
      sib.getParent().add(level);
      this.firePropertyChange("dimensions", null , dimensions);
    } else if (selectedItem instanceof HierarchyMetaData) {
      HierarchyMetaData hier = (HierarchyMetaData) selectedItem;
      LevelMetaData level = createLevel(hier, newItem);
      hier.add(level);
      this.firePropertyChange("dimensions", null , dimensions);
    } else if (selectedItem instanceof DimensionMetaData) {
      DimensionMetaData dim = (DimensionMetaData)selectedItem;
      HierarchyMetaData hier = null;

      if (dim.size() > 0) {
        hier = dim.get(0);
      } else {
        hier = new HierarchyMetaData(newItem.toString());
        hier.setParent(dim);
        dim.add(hier);
      }
      LevelMetaData level = createLevel(hier, newItem);
      hier.add(level);
      this.firePropertyChange("dimensions", null , dimensions);
    }
  }

  private void fireFieldsChanged(){
    firePropertyChange("model", null, model);
    setDirty(true);
  }
  
  private void fireDimensionsChanged(){
    firePropertyChange("dimensions", null, dimensions);
    setDirty(true);
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
  
  public LogicalColumn findLogicalColumn(String id) {
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

 public void refresh() throws ModelerException {
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
          fm.setName(lc.getName(Locale.getDefault().toString()));
          fm.setDisplayName(lc.getName(Locale.getDefault().toString()));
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
      sortFields();

      firePropertyChange("availableFields", null, getAvailableFields()); //$NON-NLS-1$
      fireFieldsChanged();
      
      for(DimensionMetaData dm : dimensions){
        for(HierarchyMetaData hm : dm){
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
  
  private void sortFields() {
    Collections.sort(availableFields, new Comparator() { public int compare(Object o1, Object o2) { 
      if( o1 == null && o2 == null ) {
        return 0;
      }
      else if( o1 == null ) {
        return -1;
      }
      else if( o2 == null ) {
        return 1;
      }
      String name1 = ((FieldMetaData) o1).getDisplayName();
      String name2 = ((FieldMetaData) o2).getDisplayName();
      if( name1 == null && name2 == null ) {
        return 0;
      }
      else if( name1 == null ) {
        return -1;
      }
      else if( name2 == null ) {
        return 1;
      }
      return name1.compareToIgnoreCase(name2);
       } });
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
      fm.setName(c.getPhysicalColumn().getName(Locale.getDefault().toString()));
      fm.setDisplayName(c.getName(Locale.getDefault().toString()));
      fm.setAggTypeDesc(c.getAggregationType().toString());
      availableFields.add(fm);
    }
    
    sortFields();

    firePropertyChange("availableFields", null, getAvailableFields());
    
    LogicalModel lModel = domain.getLogicalModels().get(0);
    
    if(lModel.getCategories().size() > 0){
      setModelName(lModel.getCategories().get(0).getId());
    }
    
    List<OlapDimension> theDimensions = (List) lModel.getProperty("olap_dimensions");
    if(theDimensions != null) {
	    Iterator<OlapDimension> theDimensionItr = theDimensions.iterator();
	    while(theDimensionItr.hasNext()) {
	    	OlapDimension theDimension = theDimensionItr.next();
	    	
	    	DimensionMetaData theDimensionMD = new DimensionMetaData(theDimension.getName());
	    	
	    	List<OlapHierarchy> theHierarchies = (List) theDimension.getHierarchies();
	    	Iterator<OlapHierarchy> theHierarchiesItr = theHierarchies.iterator();
	    	while(theHierarchiesItr.hasNext()) {
	    		OlapHierarchy theHierarchy =  theHierarchiesItr.next();
	    		HierarchyMetaData theHierarchyMD = new HierarchyMetaData(theHierarchy.getName());
	    		
	    		List<OlapHierarchyLevel> theLevels = theHierarchy.getHierarchyLevels();
	    		Iterator<OlapHierarchyLevel> theLevelsItr = theLevels.iterator();
	    		while(theLevelsItr.hasNext()) {
	    			OlapHierarchyLevel theLevel = theLevelsItr.next();
	    			LevelMetaData theLevelMD = new LevelMetaData(theHierarchyMD, theLevel.getName());

	    			theLevelMD.setParent(theHierarchyMD);
	    			theLevelMD.setLogicalColumn(theLevel.getReferenceColumn());
	    			theHierarchyMD.add(theLevelMD);
	    		}
	    		
	    		theHierarchyMD.setParent(theDimensionMD);
	    		theDimensionMD.add(theHierarchyMD);
	    	}
	    	this.dimensions.add(theDimensionMD);
	    }
    }
    
    List<OlapCube> theCubes = (List) lModel.getProperty("olap_cubes");
    if(theCubes != null) {
	    Iterator<OlapCube> theCubeItr = theCubes.iterator();
	    while(theCubeItr.hasNext()) {
	    	OlapCube theCube = theCubeItr.next();
	    	
	    	List<OlapMeasure> theMeasures = theCube.getOlapMeasures();
	    	Iterator<OlapMeasure> theMeasuresItr = theMeasures.iterator();
	    	while(theMeasuresItr.hasNext()) {
	    		OlapMeasure theMeasure = theMeasuresItr.next();
	    		
	    		FieldMetaData theMeasureMD = new FieldMetaData();
	    		theMeasureMD.setName(theMeasure.getName());

	    		theMeasureMD.setName(theMeasure.getLogicalColumn().getPhysicalColumn().getName(Locale.getDefault().toString()));
	    		theMeasureMD.setDisplayName(theMeasure.getLogicalColumn().getName(Locale.getDefault().toString()));
          theMeasureMD.setAggTypeDesc(theMeasure.getLogicalColumn().getAggregationType().toString());
	        
	    		theMeasureMD.setLogicalColumn(theMeasure.getLogicalColumn());
	    		this.inPlayFields.add(theMeasureMD);
	    	}
	    }
    }
    
  }

  public void resolveConnectionFromDomain() {
    // set up the datasource
    if( domain != null && source != null ) {
      SqlPhysicalModel physicalModel = (SqlPhysicalModel)domain.getPhysicalModels().get(0);
      DatabaseMeta databaseMeta = ThinModelConverter.convertToLegacy(physicalModel.getId(), physicalModel.getDatasource());
      source.setDatabaseMeta(databaseMeta);
    }
    
  }
  
  public Domain getDomain(){
    return updateDomain();
  }
  
  private Domain updateDomain(){
    // TODO: update domain with changes
    return domain;
  }  

  public static class FieldsCollection extends AbstractModelList<FieldMetaData>  implements Serializable {
    private String name = "Measures";
    
    public String getName() {
      return name;
    }
    
    public void setName(String name) {
      this.name = name;
    }
    
    public Image getImage() {
      return null;
    }
    
    public boolean isUiExpanded(){
      return true;
    }

  }

  public static class DimensionMetaDataCollection extends AbstractModelNode<DimensionMetaData> implements Serializable {
    private String name = "Dimensions";
    
    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  
    public Image getImage() {
      return null;
    }

    public boolean isUiExpanded(){
      return true;
    }

    private PropertyChangeListener listener = new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt) {
        fireCollectionChanged();
      }
    };

    protected void fireCollectionChanged() {
      this.changeSupport.firePropertyChange("children", null, this);
    }

    @Override
    public void onAdd(DimensionMetaData child) {
      child.addPropertyChangeListener("children", listener);
    }

    @Override
    public void onRemove(DimensionMetaData child) {
      child.removePropertyChangeListener(listener);
    }
    
  }
  
  public class MainModelNode extends AbstractModelNode<XulEventSourceAdapter> implements Serializable {
    String name = "Model";
    
    public String getName() {
      return name;
    }

    public void setName(String name) {
      // noop
    }

    public Image getImage() {
      return null;
    }
    
    public boolean isUiExpanded(){
      return true;
    }

    private PropertyChangeListener listener = new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt) {
        fireCollectionChanged();
      }
    };

    protected void fireCollectionChanged() {
      this.changeSupport.firePropertyChange("children", null, this);
    }

    @Override
    public void onAdd(XulEventSourceAdapter child) {
      child.addPropertyChangeListener("children", listener);
    }

    @Override
    public void onRemove(XulEventSourceAdapter child) {
      child.removePropertyChangeListener(listener);
    }
    
    

  }

}