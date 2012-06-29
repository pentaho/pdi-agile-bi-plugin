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
package org.pentaho.agilebi.spoon;

import org.pentaho.agilebi.spoon.perspective.AgileBiModelerPerspective;
import org.pentaho.di.ui.spoon.*;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

@SpoonPlugin(id = "AgileBi", image = "")
@SpoonPluginCategories({"spoon", "trans-graph", "database_dialog"})
public class AgileBISpoonPlugin implements SpoonPluginInterface{

  public AgileBISpoonPlugin(){
//		need to find a more general place to do this
//AgileBiInstaPerspective.getInstance().onStart();
try {
	  String className = "org.pentaho.agilebi.insta.perspective.InstaStarter"; //$NON-NLS-1$
	  Class<?> clazz = Class.forName(className);
	  Object obj = clazz.newInstance();
} catch (Exception e) {
	  e.printStackTrace();
}
  
  }
  
  public void applyToContainer(String category, XulDomContainer container) throws XulException {
    container.registerClassLoader(getClass().getClassLoader());
    if(category.equals("spoon")){
      container.loadOverlay("org/pentaho/agilebi/spoon/spoon_overlays.xul");
      container.addEventHandler(ModelerHelper.getInstance());
    } else if(category.equals("trans-graph")){
      container.loadOverlay("org/pentaho/agilebi/spoon/trans_overlay.xul");
      container.addEventHandler(ModelerHelper.getInstance());
    } else if(category.equals("database_dialog")){
      container.loadOverlay("org/pentaho/agilebi/spoon/database_dialog_overlay.xul");
      container.addEventHandler(new AgileBiDatabaseController());
    } 
  }

  public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  public SpoonPerspective getPerspective() {
    return AgileBiModelerPerspective.getInstance();
  }
  
  
}
