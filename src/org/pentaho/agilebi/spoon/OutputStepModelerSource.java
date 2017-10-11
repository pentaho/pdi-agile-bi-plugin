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

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.registry.Type;

/**
 * Provides information to the ModelerModel to support the User Interface. This
 * class also generates the final artifacts from the UI models.
 * 
 * @author nbaker
 * 
 */
public class OutputStepModelerSource extends KettleModelerSource {

	public static final String OUTPUTSTEP_SOURCE_TYPE = OutputStepModelerSource.class.getSimpleName();

  /**
   * Default no-arg constructor so {@link org.pentaho.agilebi.modeler.util.ModelerSourceFactory} can instantiate us.
   */
  public OutputStepModelerSource() {
  }

  public OutputStepModelerSource(DatabaseMeta databaseMeta, String tableName, String schemaName, String transName, String filename, String repositoryName, String metaId) {
    super(databaseMeta, tableName, schemaName, Type.TYPE_TRANSFORMATION, transName, filename, repositoryName, metaId);
  }

  @Override
  protected String getFilenamePropertyName() {
    return "trans_file";
  }

  @Override
  protected String getRepositoryNamePropertyName() {
    return "trans_repo";
  }

  @Override
  protected String getMetaIdPropertyName() {
    return "trans_step";
  }

  @Override
  public String getSourceType() {
    return OUTPUTSTEP_SOURCE_TYPE;
  }
}
