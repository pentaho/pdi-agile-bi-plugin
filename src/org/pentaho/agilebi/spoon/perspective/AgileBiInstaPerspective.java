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
 * Copyright (c) 2010 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.spoon.perspective;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.lifecycle.pdi.AgileBILifecycleListener;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveListener;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.components.XulBrowser;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.w3c.dom.Node;

public class AgileBiInstaPerspective extends AbstractPerspective implements SpoonPerspectiveListener, SpoonPerspective {

	public static final String PERSPECTIVE_ID = "030-agilebiInsta"; //$NON-NLS-1$

//  private Logger logger = LoggerFactory.getLogger(AgileBiModelerPerspective.class);
  private static final AgileBiInstaPerspective INSTANCE = new AgileBiInstaPerspective();
  
  private XulBrowser browser;
  
  public void onStart() {

	String location = "http://localhost:${port}/pentaho/content/instanal/resources/web/insta/main.html?theme=onyx&embedded=true"; //$NON-NLS-1$

	int port = AgileBILifecycleListener.consolePort;
	if( port == 0 ) {
		// TODO - JD - this is bogus, and only needed because the startup sequence is all messed up
		port = 10000;
	}
	location = replaceField(location, "port", ""+port, false); //$NON-NLS-1$ //$NON-NLS-2$
	browser.setSrc(location);
  }
  
  public AgileBiInstaPerspective(){
    super("org/pentaho/agilebi/spoon/perspective/insta_browser.xul"); //$NON-NLS-1$
    
    addPerspectiveListener(this);
    
    SpoonPerspectiveManager.getInstance().addPerspective(this);
    
    try {
    	browser = (XulBrowser) container.getDocumentRoot().getElementById("web_browser"); //$NON-NLS-1$
    } catch (Exception e) {
        e.printStackTrace();//logger.error(e);
    }    
  }
  
	private String replaceField(String str, String fieldName, String value, boolean urlEncode) {
		  if (urlEncode) {
		    value = URLEncoder.encode(value);
		  }
		  return str.replaceAll("\\$\\{"+fieldName+"\\}", value); //$NON-NLS-1$ //$NON-NLS-2$
		}
  
  public static AgileBiInstaPerspective getInstance(){
    return INSTANCE;
  }
  
  public void setModel(ModelerWorkspace aModel) {
  	model = aModel;
  }
  
  public String getDisplayName(Locale l) {
  
    return BaseMessages.getString(this.getClass(), "Insta.Perspective.perspectiveName"); //$NON-NLS-1$
  }

  public InputStream getPerspectiveIcon() {
    ClassLoader loader = getClass().getClassLoader();
    return loader.getResourceAsStream("org/pentaho/agilebi/spoon/perspective/insta.png"); //$NON-NLS-1$
  }

  public String getId() {
    return PERSPECTIVE_ID; 
  }

  public boolean acceptsXml(String nodeName) {
    return false;
  }

  public String[] getFileTypeDisplayNames(Locale locale) {
    return new String[]{};
  }

  public String[] getSupportedExtensions() {
    return new String[]{};
  }

  public boolean open(Node transNode, String fname, boolean importfile) {
    return true;
  }

  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    return false;
  }

  public void syncMetaName(EngineMetaInterface meta, String name) {
	  // don't need to do anything here
  }

  public List<XulEventHandler> getEventHandlers() {
    return new ArrayList<XulEventHandler>();
  }

  public List<XulOverlay> getOverlays() {
    XulOverlay overlay = new XulOverlay(){

      public String getId() {
        return "agileBi"; //$NON-NLS-1$
      }

      public String getOverlayUri() {
        return "org/pentaho/agilebi/spoon/perspective/insta_perspective_overlay.xul"; //$NON-NLS-1$
        
      }

      public String getOverlayXml() {
        return null;
      }

      public String getResourceBundleUri() {
        return null;
      }

      public String getSource() {
        return null;
      }
      
    };
    return Collections.singletonList(overlay);
  }

  @Override
  public String getName() {
    return "instaPerspective"; //$NON-NLS-1$
  }
  
  public boolean onTabClose(final int pos) throws XulException{
    return true;
  }

  @Override
  public void setSelectedMeta(EngineMetaInterface meta) {
    super.setSelectedMeta(meta);
  }

//@Override
public void onActivation() {

	Spoon.getInstance().getShell().setText("");
	Spoon.getInstance().setMainToolbarVisible(false);
	// TODO - JD - enable this in Spoon
//	Spoon.getInstance().setMenuBarVisible(false);
	
}

//@Override
public void onDeactication() {

	Spoon.getInstance().setMainToolbarVisible(true);
	// TODO - JD - enable this in Spoon
//	Spoon.getInstance().setMenuBarVisible(true);
	
}
  
}
