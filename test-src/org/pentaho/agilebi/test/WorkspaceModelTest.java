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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.agilebi.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.spoon.visualizations.VisualizationManager;
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

    public Domain generateDomain(boolean dualModelingMode) throws ModelerException {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
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
