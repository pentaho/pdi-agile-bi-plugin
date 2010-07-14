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

import org.junit.Test;
import org.pentaho.agilebi.spoon.visualizations.xul.PrptViewerTag;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.swt.SwtXulLoader;

/**
 * Not much we can test. The two cases mainly ensures there's no excpetions thrown by the component.
 * @author nbaker
 *
 */
public class TestPrptComponent {
  @Test
  public void testPrptSrc() throws Exception{

    if(ClassicEngineBoot.getInstance().isBootDone() == false){
      ClassicEngineBoot engineBoot = ClassicEngineBoot.getInstance();
      engineBoot.start();
    }
    
    SwtXulLoader loader = new SwtXulLoader();
    loader.register("PRPT", PrptViewerTag.class.getCanonicalName());
    XulDomContainer container = loader.loadXul("org/pentaho/agilebi/test/res/prpt.xul");
    
    PrptViewerTag prpt = (PrptViewerTag) container.getDocumentRoot().getElementById("prptViewer");
    
    prpt.setSrc("test-res/test.prpt");
    
  }
  
  @Test
  public void testPrptSetMasterReport() throws Exception{

    if(ClassicEngineBoot.getInstance().isBootDone() == false){
      ClassicEngineBoot engineBoot = ClassicEngineBoot.getInstance();
      engineBoot.start();
    }
    
    SwtXulLoader loader = new SwtXulLoader();
    loader.register("PRPT", PrptViewerTag.class.getCanonicalName());
    XulDomContainer container = loader.loadXul("org/pentaho/agilebi/test/res/prpt.xul");
    
    PrptViewerTag prpt = (PrptViewerTag) container.getDocumentRoot().getElementById("prptViewer");
    
    ResourceManager theResourceManager = new ResourceManager();
    theResourceManager.registerDefaults();
    File theReportFile = new File("test-res/test.prpt");
    Resource theResource = theResourceManager.createDirectly(theReportFile, MasterReport.class);
    MasterReport masterReport = (MasterReport) theResource.getResource();
    
    prpt.setMasterReport(masterReport);
    
  }
}
