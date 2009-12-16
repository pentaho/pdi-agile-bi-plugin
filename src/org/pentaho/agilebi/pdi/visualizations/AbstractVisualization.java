package org.pentaho.agilebi.pdi.visualizations;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.xul.swt.tab.TabItem;

public abstract class AbstractVisualization implements IVisualization {
  
  private int order;
  private String title;
  private String extension;
  private String tabIconLocation;
  
  public String getExtension() {
    return extension;
  }
  
  public void setExtension(String extension) {
    this.extension = extension;
  }
  
  
  public String getTitle() {
    return title;
  }

  public void setTitle(String aDescription) {
    title = aDescription;
  }
  
  public void setOrder(int order) {
    this.order = order;
  }
  
  public int getOrder() {
    return order;
  }
  
  public void setTabIconLocation(String tabIconLocation) {
    this.tabIconLocation = tabIconLocation;
  }
  
  public String getTabIconLocation() {
    return tabIconLocation;
  }
  
}
