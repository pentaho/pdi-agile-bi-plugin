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
package org.pentaho.agilebi.pdi.visualizations.reportwizard;

import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.di.core.EngineMetaInterface;
import org.w3c.dom.Node;

public class ReportWizardVisualization implements IVisualization {

	private String description;
	private int order;
	
	public String getTitle() {
		return description;
	}

	public void setTitle(String aDescription) {
		description = aDescription;
	}
	
  public void setOrder(int order) {
    this.order = order;
  }
  
  public int getOrder() {
    return order;
  }

	public void openVisualizer(String aModelName, String aDatabaseName) {
	}

  public boolean open(Node arg0, String arg1, boolean arg2) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean save(EngineMetaInterface arg0, String arg1, boolean arg2) {
    // TODO Auto-generated method stub
    return false;
  }

  public void syncMetaName(EngineMetaInterface arg0, String arg1) {
    // TODO Auto-generated method stub
    
  }

  public String getExtension() {
    return "prpt";
  }
}
