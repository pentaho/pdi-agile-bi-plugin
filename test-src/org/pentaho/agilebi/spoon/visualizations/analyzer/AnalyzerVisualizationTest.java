/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

package org.pentaho.agilebi.spoon.visualizations.analyzer;

import java.io.File;

import org.junit.Test;
import org.pentaho.di.core.lifecycle.pdi.AgileBILifecycleListener;

import static junit.framework.Assert.*;

/**
 * User: RFellows
 * Date: 2/8/13
 */
public class AnalyzerVisualizationTest {

  @Test
  public void testConvertPathToRepoUrlFormat() throws Exception {
    String file = "/Users/joe/.kettle/instaview/projects/TestProject/View 1.xanalyzer";
    String expected = ":Users:joe:.kettle:instaview:projects:TestProject:View 1.xanalyzer";
    assertEquals(expected, AnalyzerVisualization.convertPathToRepoUrlFormat(file));
  }

  @Test
  public void testConvertPathToRepoUrlFormat_backslashes() throws Exception {
    String path = "C:\\Program Files\\pentaho";
    String expected = "C::Program Files:pentaho";
    assertEquals(expected, AnalyzerVisualization.convertPathToRepoUrlFormat(path));
  }

  @Test
  public void testGenerateOpenUrl() {
    AnalyzerVisualization viz = new AnalyzerVisualization();
    viz.setOpenUrl("http://localhost:${port}/pentaho/api/repos/${path}/editor?ts=${timestamp}");
    AgileBILifecycleListener.consolePort = 10000;
    String expectedPrefix = "http://localhost:10000/pentaho/api/repos/${path}/editor?ts=";
    String file = "/Users/joe/.kettle/instaview/projects/TestProject/View 1.xanalyzer";

    // path should be URL encoded...
    String expected = "%3AUsers%3Ajoe%3A.kettle%3Ainstaview%3Aprojects%3ATestProject%3AView%201.xanalyzer";
    expectedPrefix = expectedPrefix.replace("${path}", expected);

    String url = viz.generateOpenUrl(file);

    // ignore the timestamp value since it is random
    int index = url.indexOf(expected);
    assertEquals(expected, url.substring(index, index + expected.length()));

  }

  @Test
  public void testGenerateRefreshModelJavascript() {
    String js = "cv.rptEditor.clearCache(); handle_puc_save('${basedir}', 'tmpview.xanalyzer', true);\n" +
        "document.location.href='http://localhost:${port}/pentaho/api/repos/${path}/editor?ts=${timestamp}';";

    AnalyzerVisualization viz = new AnalyzerVisualization();
    viz.setOpenUrl("http://localhost:${port}/pentaho/api/repos/${path}/editor?ts=${timestamp}");
    viz.setRefreshModelJavascript(js);
    viz.reportName = "Untitled Report";
    AgileBILifecycleListener.consolePort = 10000;

    String myPath = AnalyzerVisualization.convertPathToRepoUrlFormat(new File("").getAbsolutePath());
    myPath = AnalyzerVisualization.encodeString(myPath);
    String expectedPrefix = "cv.rptEditor.clearCache(); handle_puc_save('" + myPath + "', 'Untitled Report.xanalyzer', true);\n" +
        "document.location.href='http://localhost:10000/pentaho/api/repos/" + myPath + "%3AUntitled%20Report.xanalyzer/editor?ts=";

    // the params to this method are not used, so the inputs here don't matter
    String script = viz.generateRefreshModelJavascript("", "");

    // ignore the timestamp value since it is random
    assertTrue(script.startsWith(expectedPrefix));

  }

}
