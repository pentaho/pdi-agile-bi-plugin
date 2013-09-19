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
 * Copyright (c) 2010 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.pentaho.agilebi.spoon.visualizations.IVisualization;
import org.pentaho.agilebi.spoon.visualizations.VisualizationManager;

@SuppressWarnings("nls")
public class VisualizationApiTest {
  
  @Test
  public void testAnalyzerPlugin() {
    TestVisualizationManager mgr = new TestVisualizationManager();
    mgr.loadVisualizationFile(new File("package-res/visualizations/analyzer/plugin.xml"));
    
    Assert.assertEquals(1, mgr.getVisualizations().size());
    IVisualization vis = mgr.getVisualizations().get(0);
    Assert.assertEquals(0, vis.getOrder());
    Assert.assertEquals("Analyzer", vis.getTitle());
  }
  
  static class TestVisualizationManager extends VisualizationManager {
    
    public TestVisualizationManager() {
      super();
    }
    
    public TestVisualizationManager(String dirloc) {
      super(dirloc);
    }
    // make this method public for testing
    public void loadVisualizationFile(File file) {
      super.loadVisualizationFile(file);
    }
  }
  
  @Test
  public void testVizApi() {
    VisualizationManager mgr = new VisualizationManager("package-res/visualizations");
    Assert.assertEquals(2, mgr.getVisualizations().size());
  }
}
