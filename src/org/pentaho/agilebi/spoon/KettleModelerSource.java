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

package org.pentaho.agilebi.spoon;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.util.ModelerSourceUtil;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.registry.Entity;
import org.pentaho.metadata.registry.IMetadataRegistry;
import org.pentaho.metadata.registry.Link;
import org.pentaho.metadata.registry.Type;
import org.pentaho.metadata.registry.Verb;
import org.pentaho.metadata.registry.util.RegistryUtil;

/**
 * Generic {@link org.pentaho.agilebi.modeler.IModelerSource} that can be configured for any Transformation Step or Job Entry.
 */
public class KettleModelerSource extends TableModelerSource {
  public static final String SOURCE_TYPE = KettleModelerSource.class.getSimpleName();
  public static final String EMPTY_STRING = "";

  private static RegistryUtil UTIL = new RegistryUtil();

  // Name of the EngineMetaInterface (Transformation or Job) referenced by filename
  private String metaName;
  private Type metaType;
  private String filename;
  private String repositoryName;
  private String metaId;

  /**
   * Default no-arg constructor so {@link org.pentaho.agilebi.modeler.util.ModelerSourceFactory} can instantiate us.
   */
  public KettleModelerSource() {
  }

  /**
   * Creates a new modeler source based on a transformation or job.
   *
   * @param databaseMeta Metadata about the database connection we are populating from the transformation or job
   * @param tableName Table name we're populating
   * @param schemaName Schema the table resides in (optional)
   * @param metaType Type of metadata object ({@link Type#TYPE_TRANSFORMATION} or {@link Type#TYPE_JOB})
   * @param metaName Friendly name of the {@link org.pentaho.di.core.EngineMetaInterface}
   * @param filename Name of file the meta object is stored as (optional since we can generate a model before we've saved the transformation/job)
   * @param repositoryName Name of repository the meta object is stored in (optional)
   * @param metaId Identifier for the step name or job entry within the engine meta we're generating a model for
   * @throws IllegalArgumentException when {@code metaType} is not valid
   */
  public KettleModelerSource(DatabaseMeta databaseMeta, String tableName, String schemaName, Type metaType, String metaName, String filename, String repositoryName, String metaId) {
    super(databaseMeta, tableName, schemaName);

    if (metaName == null) {
      throw new NullPointerException("metaName is required");
    }
    if (metaId == null) {
      throw new NullPointerException("metaId is required");
    }

    setMetaType(metaType);
    this.metaName = metaName;
    this.filename = filename;
    this.repositoryName = repositoryName;
    this.metaId = metaId;
  }

  /**
   * @return the name of the property the filename is stored as in the {@link LogicalModel}
   */
  protected String getFilenamePropertyName() {
    return "enginemeta_filename";
  }

  /**
   * @return the name of the property the repository name is stored as in the {@link LogicalModel}
   */
  protected String getRepositoryNamePropertyName() {
    return "enginemeta_repositoryName";
  }

  /**
   * @return the name of the property the meta id is stored as in the {@link LogicalModel}
   */
  protected String getMetaIdPropertyName() {
    return "enginemeta_id";
  }

  /**
   * @return the name of the property the source type is stored as in the {@link LogicalModel}
   */
  public String getSourceTypePropertyName() {
    return "source_type";
  }

  /**
   * @return the name this {@link org.pentaho.agilebi.modeler.IModelerSource} is referenced by
   */
  public String getSourceType() {
    return SOURCE_TYPE;
  }

  /**
   * Generates a {@link Domain} from the database meta, schema name, and table name. It additionally sets "SUPPORTS_OLAP"
   * to {@code true} for all models.
   *
   * @return a domain that is backed by the database meta, schema name, and table name
   * @throws ModelerException if we cannot generate the domain
   */
  public Domain generateDomain() throws ModelerException {
    Domain d = ModelerSourceUtil.generateDomain(getDatabaseMeta(), getSchemaName(), getTableName());
    for(LogicalModel lModel : d.getLogicalModels()) {
      lModel.setProperty("SUPPORTS_OLAP", Boolean.TRUE.toString() );
    }
    return d;
  }

  /**
   * Initialize the first model in the domain with our configuration.
   *
   * @param domain Domain to initialize
   *
   * @throws ModelerException error initializing domain
   */
  @Override
  public void initialize(Domain domain) throws ModelerException {
    super.initialize(domain);
    LogicalModel lm = domain.getLogicalModels().get(0);
    if(lm.getProperty(getFilenamePropertyName()) != null) {
      setFilename(lm.getProperty(getFilenamePropertyName()).toString());
    }
    if (lm.getProperty(getRepositoryNamePropertyName()) != null) {
      setRepositoryName(lm.getProperty(getRepositoryNamePropertyName()).toString());
    }
    if (lm.getProperty(getMetaIdPropertyName()) != null) {
      setMetaId(lm.getProperty(getMetaIdPropertyName()).toString());
    }
  }

  /**
   * Serialize the properties of this source into the domain provided.
   *
   * @param d Domain to serialize to
   */
  @Override
  public void serializeIntoDomain(Domain d) {
    LogicalModel lm = d.getLogicalModels().get(0);
    lm.setProperty(getSourceTypePropertyName(), getSourceType() );
    lm.setProperty(getFilenamePropertyName(), Const.NVL(getFilename(), EMPTY_STRING) );
    lm.setProperty(getRepositoryNamePropertyName(), Const.NVL(getRepositoryName(), EMPTY_STRING) );
    lm.setProperty(getMetaIdPropertyName(), Const.NVL(getMetaId(), EMPTY_STRING) );
  }

  /**
   * @return the filename of the {@link org.pentaho.di.core.EngineMetaInterface}
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Set the filename for the {@link org.pentaho.di.core.EngineMetaInterface}.
   *
   * @param filename Filename to set
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  /**
   * @return the repository name
   */
  public String getRepositoryName() {
    return repositoryName;
  }

  /**
   * Set the repository name.
   *
   * @param repositoryName Repository name to set
   */
  public void setRepositoryName(String repositoryName) {
    if(repositoryName != null && repositoryName.equals(EMPTY_STRING)){
      repositoryName = null;
    }
    this.repositoryName = repositoryName;
  }

  /**
   * @return the id of the {@link org.pentaho.di.core.EngineMetaInterface} object's entry (step or job entry) whose information was used to configure this source.
   */
  public String getMetaId() {
    return metaId;
  }

  /**
   * Set the id of the {@link org.pentaho.di.core.EngineMetaInterface} object's entry (step or job entry) used to configure this source.
   *
   * @param metaId Id
   */
  public void setMetaId(String metaId) {
    this.metaId = metaId;
  }

  /**
   * @return the friendly name of the {@link org.pentaho.di.core.EngineMetaInterface} referenced by {@link #getFilename()}, e.g. "Transformation 1" for a transformation file named trans1.ktr.
   */
  public String getMetaName() {
    return metaName;
  }

  /**
   * Sets the friendly name of the {@link org.pentaho.di.core.EngineMetaInterface}
   *
   * @param metaName Friendly name of the transformation or job referenced by {@link #getFilename()}
   */
  public void setMetaName(String metaName) {
    this.metaName = metaName;
  }

  /**
   * @return the type of {@link org.pentaho.di.core.EngineMetaInterface} referenced by this source
   */
  public Type getMetaType() {
    return metaType;
  }

  /**
   * Set the type of {@link org.pentaho.di.core.EngineMetaInterface} referenced by this source so we can properly create
   * the data lineage metadata.
   *
   * @param metaType Type of meta object
   * @throws IllegalArgumentException if {@code type} is not {@link Type#TYPE_TRANSFORMATION} or {@link Type#TYPE_JOB}.
   */
  public void setMetaType(Type metaType) {
    if (metaType != Type.TYPE_TRANSFORMATION && metaType != Type.TYPE_JOB) {
      throw new IllegalArgumentException("unsupported type");
    }
    this.metaType = metaType;
  }

  /**
   * Register the data lineage information given by the {@link org.pentaho.di.core.EngineMetaInterface} and the database
   * connection information.
   *
   * @param registry Where to register the metadata
   */
  public void registerLineageMetadata(IMetadataRegistry registry) {
    Entity meta = new Entity(UTIL.generateCompositeId(getRepositoryName(), getFilename()), getMetaName(), getMetaType().getId());
    Entity table = new Entity(UTIL.generateCompositeId(getDatabaseName(), getSchemaName(), getTableName()), getTableName(), Type.TYPE_PHYSICAL_TABLE.getId());
    Link link = new Link(meta, Verb.VERB_POPULATES, table);

    registry.addEntity(table);
    registry.addEntity(meta);
    registry.addLink(link);
  }
}
