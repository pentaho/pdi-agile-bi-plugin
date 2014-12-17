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

package org.pentaho.agilebi.spoon.wizard.ui.xul;

import org.pentaho.reporting.engine.classic.core.DataFactoryContext;
import org.pentaho.reporting.engine.classic.core.designtime.datafactory.DesignTimeDataFactoryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.designtime.DesignTimeContext;
import org.pentaho.reporting.engine.classic.core.metadata.MaturityLevel;
import org.pentaho.reporting.engine.classic.core.wizard.DataSchemaModel;
import org.pentaho.reporting.engine.classic.wizard.ui.xul.WizardEditorModel;
import org.pentaho.reporting.libraries.designtime.swing.settings.DefaultLocaleSettings;
import org.pentaho.reporting.libraries.designtime.swing.settings.LocaleSettings;

import java.awt.*;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class DefaultWizardDesignTimeContext implements DesignTimeContext
{
  private static final Logger logger = LoggerFactory.getLogger(DefaultWizardDesignTimeContext.class);
  private WizardEditorModel editorModel;

  public DefaultWizardDesignTimeContext(final WizardEditorModel editorModel)
  {
    this.editorModel = editorModel;
  }

  public AbstractReportDefinition getReport()
  {
    return editorModel.getReportDefinition();
  }

  public Window getParentWindow()
  {
    return null;
  }

  public DataSchemaModel getDataSchemaModel()
  {
    return editorModel.getDataSchema();
  }

  public void error(final Exception e)
  {
//    ExceptionDialog.showExceptionDialog(parentWindow, "Error", e.getMessage(), e);
    logger.error("Context received fatal error", e);
  }

  public void userError(final Exception e)
  {
//    ExceptionDialog.showExceptionDialog(parentWindow, "Non-Fatal Error", e.getMessage(), e);
    logger.info("Context received user error", e);
  }

  public LocaleSettings getLocaleSettings() {
    return new DefaultLocaleSettings();
  }

  public boolean isShowDeprecatedItems() {
    return true;
  }

  public boolean isShowExpertItems() {
    return false;
  }

  public DataFactoryContext getDataFactoryContext() {
    return new DesignTimeDataFactoryContext();
  }

  public MaturityLevel getMaturityLevel() {
    return MaturityLevel.Production;
  }
}
