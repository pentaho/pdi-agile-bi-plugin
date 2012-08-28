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

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.*;
import org.pentaho.metadata.registry.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class KettleModelerSourceTest {

  @BeforeClass
  public static void setup() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void instantiation() {
    assertNotNull(new KettleModelerSource());
  }

  @Test
  public void instantiation_parameters() {
    assertNotNull(new KettleModelerSource(null, null, null, Type.TYPE_JOB, "name", null, null, "id"));
  }

  @Test
  public void instantiation_metaName_required() {
    try {
      new KettleModelerSource(null, null, null, null, null, null, null, null);
      fail("Expected " + NullPointerException.class.getSimpleName());
    } catch (NullPointerException ex) {
      assertTrue("Unexpected exception message: " + ex.getMessage(), ex.getMessage().contains("metaName"));
    }
  }

  @Test
  public void instantiation_metaId_required() {
    try {
      new KettleModelerSource(null, null, null, null, "name", null, null, null);
      fail("Expected " + NullPointerException.class.getSimpleName());
    } catch (NullPointerException ex) {
      assertTrue("Unexpected exception message: " + ex.getMessage(), ex.getMessage().contains("metaId"));
    }
  }
  @Test
  public void instantiation_metaType_required() {
    try {
      new KettleModelerSource(null, null, null, null, "name", null, null, "id");
      fail("Expected " + IllegalArgumentException.class.getSimpleName());
    } catch (IllegalArgumentException ex) {
      assertTrue("Unexpected exception message: " + ex.getMessage(), ex.getMessage().contains("unsupported type"));
    }
  }

  @Test
  public void getPropertyNameFields() {
    KettleModelerSource source = new KettleModelerSource();

    assertEquals("enginemeta_filename", source.getFilenamePropertyName());
    assertEquals("enginemeta_repositoryName", source.getRepositoryNamePropertyName());
    assertEquals("enginemeta_id", source.getMetaIdPropertyName());
    assertEquals("source_type", source.getSourceTypePropertyName());
  }

  @Test
  public void getSourceType() {
    KettleModelerSource source = new KettleModelerSource();

    assertEquals(KettleModelerSource.class.getSimpleName(), source.getSourceType());
  }

  @Test
  public void gettersAndSetters() {
    KettleModelerSource source = new KettleModelerSource();
    String filename = "testing.kjb";
    String metaId = "012383adsfb";
    String repo = "bogus ee repo";
    Type metaType = Type.TYPE_TRANSFORMATION;
    String metaName = "Job 1";

    source.setFilename(filename);
    source.setMetaId(metaId);
    source.setRepositoryName(repo);
    source.setMetaType(metaType);
    source.setMetaName(metaName);

    assertEquals(filename, source.getFilename());
    assertEquals(metaId, source.getMetaId());
    assertEquals(repo, source.getRepositoryName());
    assertEquals(metaType, source.getMetaType());
    assertEquals(metaName, source.getMetaName());
  }

  @Test
  public void setRepositoryName() {
    KettleModelerSource source = new KettleModelerSource();

    source.setRepositoryName(null);
    assertNull(source.getRepositoryName());

    source.setRepositoryName("");
    assertNull(source.getRepositoryName());
  }

  @Test
  public void initialize() throws ModelerException {
    KettleModelerSource source = new KettleModelerSource();

    String tableName = "tableName";
    String schemaName = "schemaName";
    String fileName = "fileName";
    String repositoryName = "repositoryName";
    String metaId = "metaId";

    SqlPhysicalTable table = new SqlPhysicalTable();
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setHostname("hostname");

    SqlPhysicalModel model = new SqlPhysicalModel();
    model.setDatasource(dataSource);
    model.addPhysicalTable(table);

    LogicalModel lm = new LogicalModel();

    Domain domain = new Domain();
    domain.addPhysicalModel(model);
    domain.addLogicalModel(lm);

    // Set the properties KettleModelerSource will look for when extracting values
    lm.setProperty(source.getFilenamePropertyName(), fileName);
    lm.setProperty(source.getRepositoryNamePropertyName(), repositoryName);
    lm.setProperty(source.getMetaIdPropertyName(), metaId);
    table.setProperty("target_table", tableName);
    table.setProperty("target_schema", schemaName);

    source.initialize(domain);

    assertEquals(tableName, source.getTableName());
    assertEquals(schemaName, source.getSchemaName());
    assertEquals(repositoryName, source.getRepositoryName());
    assertEquals(metaId , source.getMetaId());
  }

  @Test
  public void serializeIntoDomain() {
    KettleModelerSource source = new KettleModelerSource();
    source.setFilename("filename");
    source.setMetaId("metaId");
    source.setRepositoryName("repositoryName");

    Domain domain = new Domain();
    LogicalModel lm = new LogicalModel();

    domain.addLogicalModel(lm);

    source.serializeIntoDomain(domain);

    assertEquals(source.getSourceType(), lm.getProperty(source.getSourceTypePropertyName()));
    assertEquals(source.getFilename(), lm.getProperty(source.getFilenamePropertyName()));
    assertEquals(source.getMetaId(), lm.getProperty(source.getMetaIdPropertyName()));
    assertEquals(source.getRepositoryName(), lm.getProperty(source.getRepositoryNamePropertyName()));
  }

  @Test
  public void setMetaType() {
    KettleModelerSource source = new KettleModelerSource();

    // Setting the meta type to JOB or TRANSFORMATION should work
    source.setMetaType(Type.TYPE_JOB);
    assertEquals(Type.TYPE_JOB, source.getMetaType());
    source.setMetaType(Type.TYPE_TRANSFORMATION);
    assertEquals(Type.TYPE_TRANSFORMATION, source.getMetaType());
  }

  @Test
  public void setMetaType_null() {
    KettleModelerSource source = new KettleModelerSource();

    try {
      source.setMetaType(null);
      fail("expected " + IllegalArgumentException.class.getSimpleName());
    } catch (IllegalArgumentException ex) {
      assertTrue("Unexpected exception message: " + ex.getMessage(), ex.getMessage().contains("unsupported type"));
    }
  }

  @Test
  public void setMetaType_invalid() {
    KettleModelerSource source = new KettleModelerSource();

    try {
      source.setMetaType(Type.TYPE_ANALYZER_VIEW);
      fail("expected " + IllegalArgumentException.class.getSimpleName());
    } catch (IllegalArgumentException ex) {
      assertTrue("Unexpected exception message: " + ex.getMessage(), ex.getMessage().contains("unsupported type"));
    }
  }

  @Test
  public void registerLineageMetadata() throws Exception {
    String dbConnection = "test";
    DatabaseMeta databaseMeta = new DatabaseMeta(dbConnection, "MYSQL", null, null, null, null, null, null);
    Type type = Type.TYPE_JOB;
    String filename = "filename";
    String repositoryName = "repo";
    String schema = "schema";
    String table = "table";
    String jobName = "job";
    String jobEntryId = "entry1";
    KettleModelerSource source = new KettleModelerSource(databaseMeta, table, schema, type, jobName, filename, repositoryName, jobEntryId);
    IMetadataRegistry registry = new SimpleRegistry() {
      @Override
      protected void load() throws Exception {
      }

      //@Override
      public void commit() throws Exception {
      }
    };

    source.registerLineageMetadata(registry);
    assertEquals(1, registry.getLinks().size());
    Link link = registry.getLinks().get(0);
    assertEquals(Verb.VERB_POPULATES.getId(), link.getVerbId());
    assertEquals(type.getId(), link.getSubjectTypeId());
    assertEquals(repositoryName + "~" + filename, link.getSubjectId());
    assertEquals(Type.TYPE_PHYSICAL_TABLE.getId(), link.getObjectTypeId());
    assertEquals(dbConnection + "~" + schema + "~" + table, link.getObjectId());
  }
}
