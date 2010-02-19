package org.pentaho.agilebi.test;
import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.pdi.modeler.IModelerSource;
import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;

@SuppressWarnings("nls")
public class WorkspaceModelTest {

  IModelerSource source;
  Domain domain;
  @Before
  public void testAnalyzerPlugin() {
    source = new TestModelerSource();
    try {
      domain = source.generateDomain();
    } catch (ModelerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  

  @Test
  public void testVizApi() {
    VisualizationManager mgr = new VisualizationManager("package-res/visualizations");
    Assert.assertEquals(2, mgr.getVisualizations().size());
  }
  
  
  
  private static class TestModelerSource implements IModelerSource{

    public Domain generateDomain() throws ModelerException {
      Domain d = new Domain();
      d.getLogicalModels().add(new LogicalModel());
      return d;
    }

    public String getDatabaseName() {
      return "TestDatabase";
    }

    public void initialize(Domain domain) throws ModelerException {
      
    }

    public void serializeIntoDomain(Domain d) {
      
    }

    public DatabaseMeta getDatabaseMeta() {
      // TODO Auto-generated method stub
      return null;
    }

    public void setDatabaseMeta(DatabaseMeta databaseMeta) {
      // TODO Auto-generated method stub
      
    }

    public String getSchemaName() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getTableName() {
      // TODO Auto-generated method stub
      return null;
    }
    
    
  }
}
