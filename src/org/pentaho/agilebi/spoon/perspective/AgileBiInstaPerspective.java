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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.lifecycle.pdi.AgileBILifecycleListener;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.spoon.BreadcrumbManager;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveListener;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.components.XulBrowser;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class AgileBiInstaPerspective extends AbstractPerspective implements SpoonPerspectiveListener, SpoonPerspective {

  private static final Class<?> PKG = AgileBiInstaPerspective.class;
	public static final String PERSPECTIVE_ID = "030-agilebiInsta"; //$NON-NLS-1$

  private Logger logger = LoggerFactory.getLogger(AgileBiInstaPerspective.class);
  private static final AgileBiInstaPerspective INSTANCE = new AgileBiInstaPerspective();
  
  private AgileBiInstaPerspectiveController perspectiveController = new AgileBiInstaPerspectiveController();
  
  private XulBrowser browser;
  
  private boolean showTips;
  private boolean showRepositoryDailog;
  
  private SpoonPerspective lastPerspective;
  
  public void onStart() {

	String location = "http://localhost:${port}/pentaho/content/instaview/resources/web/main.html?theme=onyx&embedded=true"; //$NON-NLS-1$

	// turn off tooltips and the repositories dialog
	Spoon spoon = Spoon.getInstance();
	if( spoon.getStartupPerspective() != null && spoon.getStartupPerspective().equals(PERSPECTIVE_ID)) {
		PropsUI props = spoon.getProperties();
		showTips = props.showTips();
		showRepositoryDailog = props.showRepositoriesDialogAtStartup();
		props.setShowTips(false);
		props.setRepositoriesDialogAtStartupShown(false);
	}

	int port = AgileBILifecycleListener.consolePort;
	if( port == 0 ) {
		// TODO - JD - this is bogus, and only needed because the startup sequence is all messed up
		port = 10000;
	}
	location = replaceField(location, "port", ""+port, false); //$NON-NLS-1$ //$NON-NLS-2$
	browser.setSrc(location);
	
	perspectiveController.setBrowser(browser);
  }
  
  public AgileBiInstaPerspective(){
    super("org/pentaho/agilebi/spoon/perspective/insta_browser.xul"); //$NON-NLS-1$
    
    addPerspectiveListener(this);
    
    SpoonPerspectiveManager.getInstance().addPerspective(this);

    final AgileBiInstaPerspective thisPerspective = this;
    // Set the last perspective so we're never without one
    lastPerspective = this;

    // Register perspective listeners after all loading is complete so we
    // get them all
    Display.getCurrent().asyncExec(new Runnable() {
      @Override
      public void run() {
        // Add listeners to all perspectives so we can switch back to the last active one
        // in the event there are unsaved changes and the user elects to not return to Instaview
        for(final SpoonPerspective sp : SpoonPerspectiveManager.getInstance().getPerspectives()) {
          sp.addPerspectiveListener(new SpoonPerspectiveListener() {
            @Override
            public void onActivation() {
              if (sp != thisPerspective) {
                lastPerspective = sp;
              }
            }

            @Override
            public void onDeactication() {
            }
          });
        }
      }
    });

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
	    return Collections.singletonList( (XulEventHandler) perspectiveController);
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

  @Override
  public void onActivation() {
    // On perspective activation close all tabs from the DI and Modeler perspectives.
    // If there are unsaved changes save them.
    Spoon spoonInstance = Spoon.getInstance();
    Shell shell = spoonInstance.getShell();
    boolean savePendingChanges = false;
    if (hasUnsavedChanges()) {
      ShowMessageDialog msgDialog = new ShowMessageDialog(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL, BaseMessages.getString(PKG, "Insta.UnsavedChanges.Warning.Title"), BaseMessages.getString(PKG, "Insta.UnsavedChanges.Warning.Message"), false); //$NON-NLS-1$ //$NON-NLS-2$
      int result = msgDialog.open();
      switch(result) {
        case SWT.YES:
        case SWT.NO:
          savePendingChanges = result == SWT.YES;
          break;
        default:
          // Canceled perspective change, reactivate the last perspective 
          // since perspective activation cannot be vetoed
          reactivateLastPerspective();
          return;
      }
    }

    boolean reactivateLast = false;
    try {
      reactivateLast = !closeETLTabs(savePendingChanges);
    } catch (Exception ex) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Insta.Saving.Error.Title"), BaseMessages.getString(PKG, "Insta.SavingETL.Error.Message"), ex);
      reactivateLast = true;
    }
    if (!reactivateLast) {
      try {
        reactivateLast = !closeModelerTabs(savePendingChanges);
      } catch (Exception ex) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "Insta.Saving.Error.Title"), BaseMessages.getString(PKG, "Insta.SavingModel.Error.Message"), ex);
        reactivateLast = true;
      }
    }

    // If there was a problem saving or closing any DI or Modeler tab reactivate the last perspective
    if (reactivateLast) {
      reactivateLastPerspective();
      return;
    }

    shell.setText("");
    spoonInstance.setMainToolbarVisible(false);
    
  	// TODO - JD - enable this in Spoon
    //	Spoon.getInstance().setMenuBarVisible(false);
  }

  /**
   * Reactivate the last active perspective
   */
  private void reactivateLastPerspective() {
    Display.getDefault().asyncExec( new Runnable() {
      public void run() {
        try {
          SpoonPerspectiveManager.getInstance().activatePerspective(lastPerspective.getClass());
        } catch (Exception ex) {
          // ignore, log error but stay where we're at
          logger.error("Error reactivating last perspective: " + lastPerspective, ex);
        }
      };
    });
  }

  /**
   * Close all Modeler tabs, possibly saving them in the process.
   * 
   * @param save Flag indicating if the tabs' content should be saved ({@code true}) or discarded ({@code false})
   * @return {@code true} if the operation completed successfully; {@code false} otherwise
   */
  private boolean closeModelerTabs(boolean save) {
    AgileBiModelerPerspective modelerPerspective = AgileBiModelerPerspective.getInstance();
    for (int i = 0; i < modelerPerspective.models.size(); i ++) {
      if (!openedFromInstaview(modelerPerspective.models.get(i))) {
        continue;
      }
      if (save) {
        EngineMetaInterface meta = modelerPerspective.metas.get(modelerPerspective.tabbox.getTabs().getChildNodes().get(i));
        if (!modelerPerspective.save(meta, meta.getFilename(), false)) {
          return false;
        }
      }
      modelerPerspective.removeTab(i);
    }
    return true;
  }

  /**
   * Close all transformation ETL tabs, possibly saving them in the process.
   * 
   * @param save Flag indicating if the transformation tabs' content should be saved ({@code true}) or discarded ({@code false})
   * @return {@code true} if the operation completed successfully; {@code false} otherwise
   * @throws KettleException Error saving a transformation tab
   */
  private boolean closeETLTabs(boolean save) throws KettleException {
    Spoon spoonInstance = Spoon.getInstance();
    int numTabs = spoonInstance.delegates.tabs.getTabs().size();
    for (int i = numTabs - 1; i >= 0; i--) {
      spoonInstance.tabfolder.setSelected(i);
      // Logic copied directly out of SpoonTabsDelegate.tabClose(). That needs to be refactored badly.
      Object control = spoonInstance.tabfolder.getSelected().getControl();
      // Save the changes if we're requested to and the control object is a tab item
      if (control instanceof TransGraph) {
        TransMeta transMeta = ((TransGraph) control).getManagedObject();
        if (!openedFromInstaview(transMeta)) {
          // Don't save or close tabs we didn't open
          continue;
        }
        if (save) {
          TabItemInterface tabItem = (TabItemInterface) control;
          if (!tabItem.applyChanges()) {
            return false;
          }
        }
        spoonInstance.delegates.trans.closeTransformation(transMeta);
        spoonInstance.refreshTree();
      }
      // we don't care about any other ETL tab types
    }
    return true;
  }

  /**
   * Determine if the transformation was opened by Instaview.
   * 
   * @return {@code true} if the transformation was opened by Instaview
   */
  private boolean openedFromInstaview(TransMeta transMeta) {
    String caller = BreadcrumbManager.getInstance().getCaller("001-spoon-jobs\t" + transMeta.getFilename());
    return PERSPECTIVE_ID.equals(caller);
  }
  
  /**
   * Determine if the model was opened by Instaview.
   * 
   * @return {@code true} if the model was opened by Instaview
   */
  private boolean openedFromInstaview(ModelerWorkspace model) {
    String caller = BreadcrumbManager.getInstance().getCaller(AgileBiModelerPerspective.PERSPECTIVE_ID + "\t" + model.getFileName());
    return PERSPECTIVE_ID.equals(caller);
  }

  @Override
  public void onDeactication() {
  
  	Spoon.getInstance().setMainToolbarVisible(true);
  	// TODO - JD - enable this in Spoon
  //	Spoon.getInstance().setMenuBarVisible(true);
  	
  }
  
  /**
   * Check for unsaved changes in the PDI or Model perspective
   * @return {@code true} if there are unsaved changes, {@code false} otherwise.
   */
  protected boolean hasUnsavedChanges() {
    Spoon spoonInstance = Spoon.getInstance();
    boolean changed = hasUnsavedETLChanges(spoonInstance.delegates.tabs.getTabs());
    return changed || hasUnsavedModelChanges(AgileBiModelerPerspective.getInstance().models);
  }

  /**
   * Determine if there are unsaved changes for transformations opened from Instaview
   * 
   * @return {@code true} if there are unsaved changes for any transformations opened from Instaview 
   */
  protected boolean hasUnsavedETLChanges(List<TabMapEntry> tabs) {
    for(TabMapEntry tme : tabs) {
      if(tme.getObjectType().equals(TabMapEntry.ObjectType.TRANSFORMATION_GRAPH)) {
        if(tme.getObject().getMeta() instanceof TransMeta) {
          TransMeta tm = (TransMeta)tme.getObject().getMeta();
          if(tm.hasChanged() && openedFromInstaview(tm)) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  /**
   * Determine if there are unsaved changes for models opened from Instaview
   * 
   * @return {@code true} if there are unsaved changes for any models opened from Instaview 
   */
  protected boolean hasUnsavedModelChanges(List<ModelerWorkspace> models) {
    for (ModelerWorkspace model : models) {
      if (model.isDirty() && openedFromInstaview(model)) {
        return true;
      }
    }
    return false;
  }
  
  public void shutdown() {
		// reset tooltips and the repositories dialog
		Spoon spoon = Spoon.getInstance();
		PropsUI props = spoon.getProperties();
		props.setShowTips(showTips);
		props.setRepositoriesDialogAtStartupShown(showRepositoryDailog);
		spoon.saveSettings();
  }
  
}
