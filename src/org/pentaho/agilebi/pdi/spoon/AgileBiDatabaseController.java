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
 * 
 * Author: Ezequiel Cuellar
 */
package org.pentaho.agilebi.pdi.spoon;

import java.io.File;

import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.modeler.ModelerHelper;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspaceUtil;
import org.pentaho.agilebi.pdi.modeler.TableModelerSource;
import org.pentaho.agilebi.pdi.perspective.AgileBiModelerPerspective;
import org.pentaho.agilebi.pdi.visualizations.IVisualization;
import org.pentaho.agilebi.pdi.visualizations.VisualizationManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.core.database.dialog.XulDatabaseExplorerController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.xul.swt.tab.TabItem;

public class AgileBiDatabaseController extends AbstractXulEventHandler {

	private static final String MODELER_NAME = "Modeler"; //$NON-NLS-1$

	private XulDatabaseExplorerController dbExplorerController;

	public AgileBiDatabaseController() {
	}

	private String getUniqueUntitledTabName(Spoon spoon, String title) {
		int num = 1;
		String tabName = title + " " + num; //$NON-NLS-1$
		// TODO: Add new plugin object type to spoon
		TabItem tabItem = spoon.delegates.tabs.findTabMapEntry(tabName, TabMapEntry.ObjectType.BROWSER).getTabItem();
		while (tabItem != null) {
			tabName = title + " " + (++num); //$NON-NLS-1$
			// TODO: Add new plugin object type to spoon
			tabItem = spoon.delegates.tabs.findTabMapEntry(tabName, TabMapEntry.ObjectType.BROWSER).getTabItem();
		}
		return tabName;
	}

	public void openModeler() {
	  getDbController();
		this.dbExplorerController.close();
		TableModelerSource source = new TableModelerSource(this.dbExplorerController.getDatabaseMeta(), this.dbExplorerController.getSelectedTable(), this.dbExplorerController.getSelectedSchema());
		try {
			ModelerWorkspace model = new ModelerWorkspace();
			ModelerWorkspaceUtil.populateModelFromSource(model, source);

			AgileBiModelerPerspective.getInstance().createTabForModel(model, MODELER_NAME);

		} catch (Exception e) {
			e.printStackTrace();
			SpoonFactory.getInstance().messageBox("Could not create a modeler: " + e.getLocalizedMessage(), "Modeler Error", false, Const.ERROR);
		}

	}

	public void quickVisualize() {
	  getDbController();
		this.dbExplorerController.close();
		TableModelerSource source = new TableModelerSource(this.dbExplorerController.getDatabaseMeta(), this.dbExplorerController.getSelectedTable(), this.dbExplorerController.getSelectedSchema() == null ? "" : this.dbExplorerController.getSelectedSchema()); //$NON-NLS-1$
		if (source.getSchemaName() == null) {
			source.setSchemaName(""); //$NON-NLS-1$
		}
		try {
			ModelerWorkspace model = new ModelerWorkspace();
			ModelerWorkspaceUtil.populateModelFromSource(model, source);
			quickVisualize(model);
		} catch (Exception e) {
			e.printStackTrace();
			SpoonFactory.getInstance().messageBox("Could not create a modeler: " + e.getLocalizedMessage(), "Modeler Error", false, Const.ERROR);
		}
	}

	public void quickVisualize(ModelerWorkspace model) throws ModelerException {
		
		ModelerHelper theHelper = ModelerHelper.getInstance();
		theHelper.createTemporaryModel(model, true);
		
		VisualizationManager theManager = VisualizationManager.getInstance();
		IVisualization theVisualization = theManager.getVisualization(theManager.getVisualizationNames().get(0));
		if (theVisualization != null) {
			if (model.getFileName() != null) {
				// TODO: Find a better name for the cube, maybe just model name?
				theVisualization.createVisualizationFromModel(model);
			} else {
				throw new UnsupportedOperationException("TODO: prompt to save model before visualization");
			}
		}

	}

	public String getName() {
		return "agilebi_database"; //$NON-NLS-1$
	}

	private XulDatabaseExplorerController getDbController() {
	  if (dbExplorerController == null) {
	    try {
        dbExplorerController = (XulDatabaseExplorerController) this.getXulDomContainer().getEventHandler("dbexplorer");
      } catch (XulException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
	  }
	  return dbExplorerController;
	}
	
	public void setData(Object aDatabaseDialog) {
		this.dbExplorerController = (XulDatabaseExplorerController) aDatabaseDialog;
	}
}
