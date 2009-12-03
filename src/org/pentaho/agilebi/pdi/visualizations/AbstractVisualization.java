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
  
  /**
   * this utility method adds a tab to the tabfolder for this visualization.
   * 
   * @param spoon
   * @param item
   * @param composite
   * @param tabName
   * @param vis
   */
  protected void addAndSelectTab(Spoon spoon, TabItemInterface item, Composite composite, String tabName) {
    TabItem tabItem = new TabItem(spoon.tabfolder, tabName, tabName);
    Image visualizeTabImage = ImageUtil.getImageAsResource(spoon.getDisplay(), this.getTabIconLocation());
    tabItem.setImage(visualizeTabImage);
    tabItem.setControl(composite);
    // TODO: Add new plugin object type to spoon
    spoon.delegates.tabs.addTab(new TabMapEntry(tabItem, tabName, item, TabMapEntry.OBJECT_TYPE_BROWSER));

    int idx = spoon.tabfolder.indexOf(tabItem);
    spoon.tabfolder.setSelected(idx);

  }

  /**
   * this method generates a unique tab name for the current visualization.
   * 
   * @param spoon
   * @return
   */
  protected String getUniqueUntitledTabName(Spoon spoon) {
    int num = 1;
    String tabName = getTitle() + " " + num; //$NON-NLS-1$
    // TODO: Add new plugin object type to spoon
    TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_BROWSER);
    while (tabItem != null) {
      tabName = getTitle() + " " + (++num); //$NON-NLS-1$
      // TODO: Add new plugin object type to spoon
      tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_BROWSER);
    }
    return tabName;
  }
}
