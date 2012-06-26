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
package org.pentaho.agilebi.spoon.visualizations.reportwizard;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.spoon.XulUI;
import org.pentaho.agilebi.spoon.visualizations.IVisualization;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

import java.util.Locale;

public class ReportWizardVisualization implements IVisualization {

	private String description;
	private int order;
	
	  @Override
	  public String getId() {
		  return "REPORT-WIZARD";
	  }  
	
	public String getTitle() {
		return BaseMessages.getString(ModelerWorkspace.class, "ReportWizard.Option");
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

  public String getTabIconLocation() {
    // TODO Auto-generated method stub
    return null;
  }

  public void createVisualizationFromModel(ModelerWorkspace model, boolean tempModel) {
    // TODO Auto-generated method stub
    
  }
  

  public boolean accepts(String fileName) {
    return false;
  }

  public boolean acceptsXml(String nodeName) {
    return false;
  }

  public String[] getFileTypeDisplayNames(Locale locale) {
    return null;
  }

  public String getRootNodeName() {
    return null;
  }

  public String[] getSupportedExtensions() {
    return null;
  }
  
  @Override
  public void setCaller( String callerId ) {
  }
  
}
