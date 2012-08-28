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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OutputStepModelerSourceTest {
  @Test
  public void instantiation() {
    new OutputStepModelerSource();
  }

  @Test
  public void instantiation_parameters() {
    String transName = "Transformation 1";
    String metaId = "step0";
    OutputStepModelerSource source = new OutputStepModelerSource(null, null, null, transName, null, null, metaId);

    assertEquals(transName, source.getMetaName());
    assertEquals(metaId, source.getMetaId());
  }

  @Test
  public void getFilenamePropertyName() {
    OutputStepModelerSource source = new OutputStepModelerSource();
    assertEquals("trans_file", source.getFilenamePropertyName());
  }

  @Test
  public void getRepositoryNamePropertyName() {
    OutputStepModelerSource source = new OutputStepModelerSource();
    assertEquals("trans_repo", source.getRepositoryNamePropertyName());
  }

  @Test
  public void getMetaIdPropertyName() {
    OutputStepModelerSource source = new OutputStepModelerSource();
    assertEquals("trans_step", source.getMetaIdPropertyName());
  }

  @Test
  public void getSourceType() {
    OutputStepModelerSource source = new OutputStepModelerSource();
    assertEquals(OutputStepModelerSource.OUTPUTSTEP_SOURCE_TYPE, source.getSourceType());
  }
}
