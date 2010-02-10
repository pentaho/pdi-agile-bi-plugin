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
package org.pentaho.agilebi.pdi.spoon;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.agilebi.pdi.modeler.ModelerHelper;
import org.pentaho.agilebi.pdi.perspective.AgileBiPerspective;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;

public class AgileBISpoonPlugin implements SpoonPlugin{

  public Map<String, List<XulEventHandler>> getEventHandlers() {
    HashMap<String, List<XulEventHandler>> hash = new HashMap<String, List<XulEventHandler>>();
    hash.put("spoon", Collections.singletonList((XulEventHandler) ModelerHelper.getInstance())); //$NON-NLS-1$
    hash.put("trans-graph", Collections.singletonList((XulEventHandler) ModelerHelper.getInstance())); //$NON-NLS-1$
    hash.put("database_dialog", Collections.singletonList((XulEventHandler) new AgileBiDatabaseController())); //$NON-NLS-1$
    return hash;
  }

  public Map<String, List<XulOverlay>> getOverlays() {
   
  	HashMap<String, List<XulOverlay>> hash = new HashMap<String, List<XulOverlay>>();
  	
  	XulOverlay overlay = new DefaultXulOverlay("org/pentaho/agilebi/pdi/spoon/spoon_overlays.xul"); //$NON-NLS-1$ 
    hash.put("spoon", Collections.singletonList((XulOverlay) overlay)); //$NON-NLS-1$
    

    overlay = new DefaultXulOverlay("org/pentaho/agilebi/pdi/spoon/trans_overlay.xul"); //$NON-NLS-1$ 
    hash.put("trans-graph", Collections.singletonList((XulOverlay) overlay)); //$NON-NLS-1$
    
    overlay = new DefaultXulOverlay("org/pentaho/agilebi/pdi/spoon/database_dialog_overlay.xul"); //$NON-NLS-1$  
    hash.put("database_dialog", Collections.singletonList((XulOverlay) overlay)); //$NON-NLS-1$
    
    return hash;
  }

  public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  public SpoonPerspective getPerspective() {
    return AgileBiPerspective.getInstance();
  }
  
  
}
