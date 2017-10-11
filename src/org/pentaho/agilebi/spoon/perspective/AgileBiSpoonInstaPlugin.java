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

package org.pentaho.agilebi.spoon.perspective;

import java.io.File;

import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

@SpoonPlugin(id = "AgileBiInsta", image = "")
@SpoonPluginCategories({})
public class AgileBiSpoonInstaPlugin implements SpoonPluginInterface{
  private static final String INSTAVIEW_PLATFORM_PLUGIN_ID = "instaview";

  public void applyToContainer(String category, XulDomContainer container) throws XulException {
     
  }


  public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  /**
   * Verify the Instaview platform plugin is installed in the embedded platform.
   * @return {@code true} if the Instaview platform plugin is installed
   */
  public boolean isInstaviewInstalled() {
    // This is a crude hack to check if the instaview folder exists
    // This should be replaced with a proper IPluginManager interrogation when we
    // can dynamically register perspectives after the SpoonPlugins have loaded.
    // This should be cleaned up as part of PDI-8576.
    return new File("plugins/spoon/agile-bi/platform/pentaho-solutions/system/instaview").exists();
  }

  /**
   * Determine if the Instaview platform plugin is registered with the given
   * plugin manager
   * 
   * @param mgr Plugin manager that may contain a registered Instaview plugin
   * @return {@code true} if the Instaview platform plugin has been registered
   *         with the provided plugin manager
   */
  public static boolean isInstaviewRegistered(IPluginManager mgr) {
    if (mgr != null) {
      for (String plugin : mgr.getRegisteredPlugins()) {
        if (INSTAVIEW_PLATFORM_PLUGIN_ID.equals(plugin)) {
          return true;
        }
      }
    }
    return false;
  }

  public SpoonPerspective getPerspective() {
    if (isInstaviewInstalled()) {
      // Only register the Instaview perspective if the plugin is installed
      return AgileBiInstaPerspective.getInstance();
    }
    return null;
  }
  
}
