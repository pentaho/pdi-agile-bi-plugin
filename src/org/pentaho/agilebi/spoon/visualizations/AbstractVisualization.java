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

package org.pentaho.agilebi.spoon.visualizations;

import org.pentaho.di.i18n.BaseMessages;

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

  public void setTitle(String key) {
    title = BaseMessages.getString(IVisualization.class, key);
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
