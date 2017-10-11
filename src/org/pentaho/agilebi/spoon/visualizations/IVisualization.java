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

package org.pentaho.agilebi.spoon.visualizations;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.ui.spoon.FileListener;

public interface IVisualization extends FileListener {

	public String getTitle();

	public void setTitle(String aDescription);
	
	/**
	 * The order is used to determine the order of visualizations in the GUI.
	 * @param order
	 */
	public void setOrder(int order);
	
	public int getOrder();

	// should change this to "new"
	public void createVisualizationFromModel(ModelerWorkspace model, boolean tempModel);

	public String getExtension();
	
	public String getTabIconLocation();
	
	public String getId();
	
	public void setCaller( String callerId );
	
}
