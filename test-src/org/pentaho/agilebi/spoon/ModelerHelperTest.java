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

package org.pentaho.agilebi.spoon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ProvidesDatabaseConnectionInformation;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metadata.registry.Type;
import org.w3c.dom.Node;

public class ModelerHelperTest {

  /**
   * A simple {@link ProvidesDatabaseConnectionInformation} implementation
   */
  public static class MockProvidesDatabaseConnectionInformation implements ProvidesDatabaseConnectionInformation {
    private DatabaseMeta databaseMeta;
    private String tableName;
    private String schemaName;
    private String missingDatabaseConnectionInformationMessage;

    public MockProvidesDatabaseConnectionInformation(DatabaseMeta databaseMeta, String tableName, String schemaName, String missingDatabaseConnectionInformationMessage) {
      this.databaseMeta = databaseMeta;
      this.tableName = tableName;
      this.schemaName = schemaName;
      this.missingDatabaseConnectionInformationMessage = missingDatabaseConnectionInformationMessage;
    }

    @Override
    public DatabaseMeta getDatabaseMeta() {
      return databaseMeta;
    }

    @Override
    public String getTableName() {
      return tableName;
    }

    @Override
    public String getSchemaName() {
      return schemaName;
    }

    @Override
    public String getMissingDatabaseConnectionInformationMessage() {
      return missingDatabaseConnectionInformationMessage;
    }
  }

  /**
   * A simple generic object that has the methods {@link ModelerHelper#getDatabaseConnectionInformation(Object)} is looking for.
   */
  public static class MockUndecoratedClass {
    private DatabaseMeta databaseMeta;
    private String tableName;
    private String schemaName;

    public MockUndecoratedClass(DatabaseMeta databaseMeta, String tableName, String schemaName) {
      this.databaseMeta = databaseMeta;
      this.tableName = tableName;
      this.schemaName = schemaName;
    }

    public DatabaseMeta getDatabaseMeta() {
      return databaseMeta;
    }

    public String getTableName() {
      return tableName;
    }

    public String getSchemaName() {
      return schemaName;
    }
  }

  /**
   * A simple job entry that does nothing.
   */
  public static class MockJobEntry extends JobEntryBase implements JobEntryInterface {
    @Override
    public Result execute(Result result, int i) throws KettleException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void loadXML(Node node, List<DatabaseMeta> databaseMetas, List<SlaveServer> slaveServers, Repository repository) throws KettleXMLException {
      throw new UnsupportedOperationException();
    }
  }

  @BeforeClass
  public static void setup() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void getDatabaseConnectionInformation() {
    DatabaseMeta databaseMeta = new DatabaseMeta("test", "MYSQL", null, null, null, null, null, null);
    String table = "table";
    String schema = "schema";
    String missingInfoMsg = "Missing info!";
    ProvidesDatabaseConnectionInformation connectionInfo = new MockProvidesDatabaseConnectionInformation(databaseMeta, table, schema, missingInfoMsg);
    ProvidesDatabaseConnectionInformation discovered = ModelerHelper.getDatabaseConnectionInformation(connectionInfo);

    assertEquals(connectionInfo, discovered);
  }

  @Test
  public void getDatabaseConnectionInformation_null() {
    assertNull(ModelerHelper.getDatabaseConnectionInformation(null));
  }

  @Test
  public void getDatabaseConnectionInformation_generic_object() throws NoSuchMethodException {
    DatabaseMeta databaseMeta = new DatabaseMeta("test", "MYSQL", null, null, null, null, null, null);
    MockUndecoratedClass object = new MockUndecoratedClass(databaseMeta, "table", "schema");
    ProvidesDatabaseConnectionInformation discovered = ModelerHelper.getDatabaseConnectionInformation(object);

    assertNull(discovered);
  }

  @Test
  public void isValidConnectionInformation() {
    DatabaseMeta databaseMeta = new DatabaseMeta("test", "MYSQL", null, null, null, null, null, null);
    String table = "table";

    assertFalse(ModelerHelper.isValidConnectionInformation(null, table));
    assertFalse(ModelerHelper.isValidConnectionInformation(databaseMeta, null));
    assertFalse(ModelerHelper.isValidConnectionInformation(databaseMeta, ""));

    assertTrue(ModelerHelper.isValidConnectionInformation(databaseMeta, table));
  }
  
  @Test
  public void getModelerSourceForJobEntry() {
    String dbName = "test";
    String table = "table";
    String schema = "schema";
    String missingInfoMsg = "Missing info!";
    DatabaseMeta databaseMeta = new DatabaseMeta(dbName, "MYSQL", null, null, null, null, null, null);
    ProvidesDatabaseConnectionInformation connectionInfo = new MockProvidesDatabaseConnectionInformation(databaseMeta, table, schema, missingInfoMsg);

    String jobName = "Job 1";
    String filename = "job.kjb";
    String pluginId = "TestPlugin";

    JobMeta jobMeta = new JobMeta();
    jobMeta.setName(jobName);
    jobMeta.setFilename(filename);
    JobEntryInterface jobEntry = new MockJobEntry();
    jobEntry.setPluginId(pluginId);
    JobEntryCopy jobEntryCopy = new JobEntryCopy();
    jobEntryCopy.setEntry(jobEntry);

    KettleModelerSource source = ModelerHelper.getModelerSourceForJobEntry(connectionInfo, jobMeta, jobEntryCopy);

    assertEquals(table, source.getTableName());
    assertEquals(schema, source.getSchemaName());
    assertEquals(dbName, source.getDatabaseName());
    assertEquals(Type.TYPE_JOB, source.getMetaType());
    assertEquals(jobName, source.getMetaName());
    assertEquals(pluginId, source.getMetaId());
  }

  @Test
  public void getModelerSourceForTransformation() {
    String dbName = "test";
    String table = "table";
    String schema = "schema";
    String missingInfoMsg = "Missing info!";
    DatabaseMeta databaseMeta = new DatabaseMeta(dbName, "MYSQL", null, null, null, null, null, null);
    ProvidesDatabaseConnectionInformation connectionInfo = new MockProvidesDatabaseConnectionInformation(databaseMeta, table, schema, missingInfoMsg);

    String transName = "Transformation 1";
    String filename = "trans.ktr";
    String stepId = "StepTypeName";

    TransMeta transMeta = new TransMeta();
    transMeta.setName(transName);
    transMeta.setFilename(filename);
    StepMeta stepMeta = new StepMeta();
    stepMeta.setStepID(stepId);

    KettleModelerSource source = ModelerHelper.getModelerSourceForStepMeta(connectionInfo, transMeta, stepMeta);

    assertEquals(table, source.getTableName());
    assertEquals(schema, source.getSchemaName());
    assertEquals(dbName, source.getDatabaseName());
    assertEquals(Type.TYPE_TRANSFORMATION, source.getMetaType());
    assertEquals(transName, source.getMetaName());
    assertEquals(stepId, source.getMetaId());
  }
}
