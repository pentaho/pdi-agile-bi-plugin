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

package org.pentaho.agilebi.spoon.visualizations.xul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewPane;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.internal.PreviewPaneUtilities;
import org.pentaho.reporting.libraries.base.config.ModifiableConfiguration;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.swt.SwtElement;
import org.pentaho.ui.xul.util.SwtXulUtil;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class PrptViewerTag extends SwtElement{

  private String src;
  private PreviewPane viewer;
  private Composite viewerComposite;
  private MasterReport masterReport;
  private Composite mainPanel;
  private boolean showToolbar = true;
  private ToolBar toolbar;
  private Composite toolbarPanel;
  private Combo combo;
  private XulDomContainer domContainer;
  
  private static Logger log = LoggerFactory.getLogger(PrptViewerTag.class);

  private TreeMap<Double, String> zoomMap = new TreeMap<Double, String>();
  {
    zoomMap.put(0.5, "50%");
    zoomMap.put(0.75, "75%");
    zoomMap.put(1.0, "100%");
    zoomMap.put(1.25, "125%");
    zoomMap.put(1.5, "150%");
    zoomMap.put(2.0, "200%");
  }
  
  public PrptViewerTag(Element self, XulComponent parent, XulDomContainer container, String tagName) {
    super("prpt");
 
    
    domContainer = container;
    Composite parentComposite = (Composite) parent.getManagedObject();
    
    mainPanel = new Composite(parentComposite, SWT.BORDER);
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    
    mainPanel.setLayout(layout);
    

    parentComposite.layout(true);
    
    setManagedObject(mainPanel);
  }
  
  public void start(){
    viewer.setPageNumber(1);
  }
  
  public void previous(){
    viewer.setPageNumber(Math.max(1, viewer.getPageNumber() - 1));
  }
  
  public void next(){
    viewer.setPageNumber(Math.min
        (viewer.getNumberOfPages(), viewer.getPageNumber() + 1));
  }
  
  public void last(){
    viewer.setPageNumber(viewer.getNumberOfPages());
  }
  
  public void zoomOut(){

    final double nextZoomOut = PreviewPaneUtilities.getNextZoomOut(viewer.getZoom(), viewer.getZoomFactors());
    if (nextZoomOut < 0.5){
      return;
    }
    viewer.setZoom(nextZoomOut);
    
  }
  
  public void zoomIn(){

    final double nextZoomIn = PreviewPaneUtilities.getNextZoomIn(viewer.getZoom(), viewer.getZoomFactors());
    if (nextZoomIn > 2.0){
      return;
    }
    viewer.setZoom(nextZoomIn);
   
  }
  
  @Override
  public void layout() {
    if(!initialized){
      toolbarPanel = new Composite(mainPanel, SWT.NONE);
      GridData data = new GridData();
      data.horizontalAlignment = SWT.FILL;
      data.verticalIndent = 0;
      data.horizontalIndent = 0;
      data.grabExcessHorizontalSpace = true;
      toolbarPanel.setLayoutData(data);
  
      toolbarPanel.setLayout(new FillLayout());

      Display d = mainPanel.getDisplay();
      if(d == null){
        d = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
      }
      Image img;
      
      toolbar = new ToolBar(toolbarPanel, SWT.HORIZONTAL);
      ToolItem item = new ToolItem(toolbar, SWT.PUSH);
      img = SwtXulUtil.getCachedImage("org/pentaho/agilebi/spoon/visualizations/prpt/images/begining.png", domContainer, d);
      item.setImage(img);
      item.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent se) {
          start();
        }
      });
      
      item = new ToolItem(toolbar, SWT.PUSH);
      img = SwtXulUtil.getCachedImage("org/pentaho/agilebi/spoon/visualizations/prpt/images/back.png", domContainer, d);
      item.setImage(img);
      item.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent se) {
          previous();
        }
      });
      
      item = new ToolItem(toolbar, SWT.PUSH);
      img = SwtXulUtil.getCachedImage("org/pentaho/agilebi/spoon/visualizations/prpt/images/forward.png", domContainer, d);
      item.setImage(img);
      item.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent se) {
          next();
        }
      });
      
      item = new ToolItem(toolbar, SWT.PUSH);
      img = SwtXulUtil.getCachedImage("org/pentaho/agilebi/spoon/visualizations/prpt/images/end.png", domContainer, d);
      item.setImage(img);
      item.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent se) {
          last();
        }
      });

      new ToolItem(toolbar, SWT.SEPARATOR);
      
      item = new ToolItem(toolbar, SWT.PUSH);
      img = SwtXulUtil.getCachedImage("org/pentaho/agilebi/spoon/visualizations/prpt/images/minus.png", domContainer, d);
      item.setImage(img);
      item.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent se) {
          zoomOut();
        }
      });
      

      item = new ToolItem(toolbar, SWT.PUSH);
      img = SwtXulUtil.getCachedImage("org/pentaho/agilebi/spoon/visualizations/prpt/images/plus.png", domContainer, d);
      item.setImage(img);
      item.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent se) {
          zoomIn();
        }
      });

      
      item = new ToolItem(toolbar, SWT.SEPARATOR);
      combo = new Combo(toolbar, SWT.DROP_DOWN);
      combo.setItems(new String[]{"50%","75%", "100%", "125%", "150%", "200%"});
      combo.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent e) {
          int idx = ((Combo) e.widget).getSelectionIndex();
          switch(idx){
            case 5:       // 200%
              viewer.setZoom(2.0);
            default:      // Others are increments of 25
              viewer.setZoom(0.5 + (0.25 *  idx));
          }
        }
      });
      combo.select(2);
      combo.pack();
      item.setControl(combo);
      item.setWidth(80);
      

      new ToolItem(toolbar, SWT.SEPARATOR);
      
      createViewer();
      

      GridData gData = new GridData(GridData.FILL_BOTH);
      viewerComposite .setLayoutData(gData);
      
      setShowtoolbar(getShowtoolbar());
      mainPanel.layout(true);

      if(src != null){
        loadPRPT();
      }
    }
    initialized = true;
  }
  
  private void createViewer(){

    Composite swingComposite = new Composite(mainPanel, SWT.EMBEDDED);
    Frame swingFrame = SWT_AWT.new_Frame(swingComposite);

    JPanel browserPanel = new JPanel();
    browserPanel.setLayout(new BorderLayout());
    
    swingFrame.add(browserPanel);
    
    this.viewer = new PreviewPane();
    browserPanel.add(viewer, BorderLayout.CENTER);
    viewerComposite = swingComposite;
    
  }
  
  protected Browser createBrowser(Composite parent){
    return new Browser(parent, SWT.None);
  }

  public boolean getShowtoolbar() {
    return showToolbar;
  }

  public void setShowtoolbar(boolean flag) {
    this.showToolbar = flag;
    if(toolbarPanel != null){
      toolbarPanel.setVisible(flag);
      ((GridData) toolbarPanel.getLayoutData()).exclude = !showToolbar;
      mainPanel.layout(true);
    }
  }
  
  
  public String getSrc() {
    return src;
  }

  public void setSrc(String src) {
    this.src = src;
    if(this.initialized){
      loadPRPT();
    }
  }
  
  public void setMasterReport(MasterReport aMasterRerport) {
    this.masterReport = aMasterRerport;
    if(this.initialized){
      loadPRPT();
    }
  }
  
  public Double getZoom(){
    return viewer.getZoom();
  }
  
  public void setZoom(Double val){
    viewer.setZoom(val);
  }
  
  private Double curZoom = 1.0;
  
  private void loadPRPT(){

    SwingUtilities.invokeLater(new Runnable(){
      public void run() {
        try {
          if (PrptViewerTag.this.masterReport == null) {
            ResourceManager theResourceManager = new ResourceManager();
            theResourceManager.registerDefaults();
            File theReportFile = new File(src);
            Resource theResource = theResourceManager.createDirectly(theReportFile, MasterReport.class);
            PrptViewerTag.this.masterReport = (MasterReport) theResource.getResource();
          }

          ModifiableConfiguration cfg = (ModifiableConfiguration) masterReport.getConfiguration();

          cfg.setConfigProperty("org.pentaho.reporting.engine.classic.core.modules.gui.base.ToolbarAvailable", "false");
          viewer.setReportJob(PrptViewerTag.this.masterReport);
          viewer.getZoomModel().addListDataListener(new ListDataListener(){

            public void contentsChanged(ListDataEvent arg0) {
              combo.select(new ArrayList<Double>(zoomMap.keySet()).indexOf(viewer.getZoom()));
              Double prevZoom = curZoom;
              PrptViewerTag.this.changeSupport.firePropertyChange("zoom", prevZoom, getZoom());
              curZoom = getZoom();
            }

            public void intervalAdded(ListDataEvent arg0) {}
            public void intervalRemoved(ListDataEvent arg0) {}

          });
        } catch(Exception e){
          log.error("error loading PRPT", e);
        }
      }
    });


  }
  
  
  
}
