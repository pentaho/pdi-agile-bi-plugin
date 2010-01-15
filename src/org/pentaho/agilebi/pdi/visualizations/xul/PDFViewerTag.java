package org.pentaho.agilebi.pdi.visualizations.xul;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.swt.SwtElement;

public class PDFViewerTag extends SwtElement{

  private String src;
  
  private SimpleViewer viewer;
  
  public PDFViewerTag(Element self, XulComponent parent, XulDomContainer container, String tagName) {
    super("pdf");

    GridData gData = new GridData(GridData.FILL_BOTH);
    
    Composite parentComposite = (Composite) parent.getManagedObject();

    Composite swingComposite = new Composite(parentComposite, SWT.EMBEDDED);
    swingComposite .setLayoutData(gData);

    Frame swingFrame = SWT_AWT.new_Frame(swingComposite);

    JPanel browserPanel = new JPanel();
    browserPanel.setLayout(new BorderLayout());
    
    swingFrame.add(browserPanel);
    
    viewer = new SimpleViewer(browserPanel, "plugins/spoon/agile-bi/visualizations/pdf/jPedalPrefs.xml");
    setManagedObject(swingComposite);
    
  }
  
  public void layout(){
    
  }

  public void forward(){
    viewer.executeCommand(Commands.FORWARDPAGE, null);
  }
  
  public void back(){
    viewer.executeCommand(Commands.BACKPAGE, null);
    
  }
  
  public void start(){
    viewer.executeCommand(Commands.GOTO, new Integer[]{0});
    
  }
  
  public void end(){
  }
  
  public void go(int page){
    
  }
  
  public String getSrc() {
    return src;
  }

  public void setSrc(String src) {
    this.src = src;
  }
  
  
}
