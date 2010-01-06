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
package org.pentaho.agilebi.pdi.modeler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.PropertiesBase;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.util.client.BiPlatformRepositoryClient;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.containers.XulTree;

/**
 * A dialog for publishing to a BI server
 * @author jamesdixon
 *
 */
public class XulDialogPublish extends AbstractSwtXulDialogController {

  private static Log logger = LogFactory.getLog(XulDialogPublish.class);
  
  private int folderTreeDepth = 99;
  
  private BindingFactory bf = new DefaultBindingFactory();
  
  private Binding serverNamesBinding, filesBinding, filenameBinding, foldersBinding;
  
  private XulMenuList serverMenuList;
  
  private XulMenuList folderMenuList;
  
  private XulTree filesBox;
  
  private XulButton okButton;
  
  private XulLabel datasourceLabel, commentLabel, pathLabel;
  
  private XulRadio publishDatasourceRadio, noPublishDatasourceRadio;
  
  private BiServerConfig biServerConfig;
  
  private List<String> serverNames;
  
  private String selectedServer;
  
//  private String selectedFolder;
  
  private ModelServerPublish publisher;

  private NamedCollection fileNames;  
  
  private DatabaseMeta databaseMeta;
  
  private BiServerConnection biServerConnection;
  
  private String filename;
  
  private boolean publishDataSource;
  
  private boolean accepted = false;
  
  private NamedCollection folderNames; 
  
  private CmisObject currentFolder;
  
  private String pathTemplate = "{path}{file}"; //$NON-NLS-1$ 
  
  /**
   * Creates a new dialog. Gets the current list of BI server connections from the properties file
   * @param shell
   * @throws XulException
   */
  public XulDialogPublish( Shell shell ) throws XulException {
    super( shell ); 
    
    setName("dialog"); //$NON-NLS-1$

    // create the 'biserverlist' dialog
    initDialogController( "org/pentaho/agilebi/pdi/modeler/publish.xul", "publish", null, null ); //$NON-NLS-1$ //$NON-NLS-2$
    
  }

  @Override
  protected Object getDialogResult() {
    return null;
  }

  @Override
  public Log getLogger() {
    return logger;
  }
  
  /**
   * Initializes the dialog. Finds the buttons so we can dis/enable them as 
   * needed. Displays the list of current connections. Sets the initial
   * buttons status.
   */
  public void init() {
    
    biServerConfig = BiServerConfig.getInstance();

    createServerList();
    
    fileNames = new NamedCollection();
    folderNames = new NamedCollection();

    bf.setDocument(document);

    serverMenuList = (XulMenuList)document.getElementById("serverlist"); //$NON-NLS-1$
    folderMenuList = (XulMenuList)document.getElementById("folderparents"); //$NON-NLS-1$
    filesBox = (XulTree)document.getElementById("filesBox"); //$NON-NLS-1$
    okButton = (XulButton) document.getElementById("publish_accept"); //$NON-NLS-1$
    datasourceLabel = (XulLabel) document.getElementById("datasourceLabel"); //$NON-NLS-1$
    commentLabel = (XulLabel) document.getElementById("comment"); //$NON-NLS-1$
    pathLabel = (XulLabel) document.getElementById("publishpath"); //$NON-NLS-1$
    publishDatasourceRadio = (XulRadio) document.getElementById("publishradio"); //$NON-NLS-1$
    noPublishDatasourceRadio = (XulRadio) document.getElementById("nopublishradio"); //$NON-NLS-1$
    
    commentLabel.setVisible( false );

    bf.setBindingType(Type.BI_DIRECTIONAL);
    
    bf.createBinding(this, "selectedServer", serverMenuList, "selectedItem");    //$NON-NLS-1$ //$NON-NLS-2$
//    bf.createBinding(this, "selectedFolder", folderMenuList, "selectedItem");    //$NON-NLS-1$ //$NON-NLS-2$
    filenameBinding = bf.createBinding(this, "filename", "filename", "value");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    
    bf.setBindingType(Type.ONE_WAY);
    serverNamesBinding = bf.createBinding(this, "serverNames", serverMenuList, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
    foldersBinding = bf.createBinding(this, "folderNames", folderMenuList, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
    filesBinding = bf.createBinding(this, "fileNames", filesBox, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
    
    fireServerNamesBinding();
    updateButtonStatus();
    fireFileNameBinding();
    
  }
  
  protected void changePath() {
    StringBuilder sb = new StringBuilder();
    List<String> folders = new ArrayList<String>();
    // get all the parents of this folder
    if( currentFolder != null ) {
      CmisObject folder = currentFolder;
      while( folder != null ) {
        folders.add(0, folder.findStringProperty(CmisObject.LOCALIZEDNAME) );
        List<CmisObject> objects;
        try {
          objects = publisher.getNavigationService()
            .getFolderParent(BiPlatformRepositoryClient.PLATFORMORIG, folder.findIdProperty( PropertiesBase.OBJECTID ), null, false, false, false);
          if( objects != null && objects.size() > 0 ) {
            folder = objects.get(0);
          } else {
            folder = null;
            break;
          }
        } catch (Exception e) {
          folder = null;
          logger.error(e);
        }
      }
    }
    
    for( String folderName : folders ) {
      if( !"".equals( folderName ) ) { //$NON-NLS-1$
        sb.append( ISolutionRepository.SEPARATOR )
        .append( folderName );
      }
    }
    
    sb.append( ISolutionRepository.SEPARATOR );
    /*
    // create the folder path
    for( NamedObject folder : folderNames ) {
      if( folder instanceof NamedCmisObject ) {
        sb.append( ISolutionRepository.SEPARATOR )
        .append( folder.getName() );
      }
    }
    if( currentFolder != null ) {
      String currentFolderName = currentFolder.findStringProperty(CmisObject.LOCALIZEDNAME);
      if( !currentFolderName.equals( folderNames.get( folderNames.size()-1 ).getName()) ) {
        sb.append( ISolutionRepository.SEPARATOR )
        .append( currentFolderName );
      }
    }
    */
    String tmp = sb.toString();
    String path = pathTemplate.replace("{path}", tmp); //$NON-NLS-1$
    path = path.replace("{file}", filename); //$NON-NLS-1$
    pathLabel.setValue( path );
  }
  
  public List<String> getServerNames() {
    return serverNames;
  }

  private void fireFileNameBinding() {
    try {
      filenameBinding.fireSourceChanged();
    } catch (Exception e) {
      logger.info(Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Xul.BindingError"), e); //$NON-NLS-1$
    }
  }

  private void fireServerNamesBinding() {
    try {
      serverNamesBinding.fireSourceChanged();
    } catch (Exception e) {
      logger.info(Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Xul.BindingError"), e); //$NON-NLS-1$
    }
  }

  private void fireFilesBinding() {
    try {
      filesBinding.fireSourceChanged();
      updateButtonStatus();
    } catch (Exception e) {
      logger.info(Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Xul.BindingError"), e); //$NON-NLS-1$
    }
  }

  private void fireFoldersBinding() {
    try {
      foldersBinding.fireSourceChanged();
      updateButtonStatus();
    } catch (Exception e) {
      logger.info(Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Xul.BindingError"), e); //$NON-NLS-1$
    }
  }
  
  public void okClick() {
    
    publishDataSource = publishDatasourceRadio.isSelected();
    accepted = true;
    onDialogAccept();
  }

  public void cancelClick() {
    // perform an undo by reloading from the properties file
    onDialogCancel();
  }

  public void setFolderTreeDepth(int folderTreeDepth) {
    this.folderTreeDepth = folderTreeDepth;
  }

  private void createServerList() {
    serverNames = biServerConfig.getServerNames();
    serverNames.add(0, Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.NullServerOption")); //$NON-NLS-1$
  }

  protected List<CmisObject> getFiles( CmisObject folder, int depth, boolean foldersOnly ) {
    try {
      List<CmisObject >files = publisher.getRepositoryFiles( folder, depth, foldersOnly );
      return files;
    } catch (Exception e) {
      logger.error(e);
      SpoonFactory.getInstance().messageBox( Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.CouldNotGetFolders", e.getLocalizedMessage() ),  //$NON-NLS-1$
          Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.Error"), false, Const.ERROR); //$NON-NLS-1$
    }
    return null;
  }
  
  protected void createFilesList( List<CmisObject >files ) {
    fileNames.clear();
    if( files != null ) {
      for( CmisObject file : files ) {
        fileNames.add( new NamedCmisObject(file) );
      }
    }
    fireFilesBinding();
  }
  
  protected void connect() {
    
    // create the publisher object
    publisher = new ModelServerPublish();
    biServerConnection = biServerConfig.getServerByName( getSelectedServer() );
    publisher.setBiServerConnection(biServerConnection);
    currentFolder = null;
    updateButtonStatus();
    List<CmisObject >files = getFiles(null, 1, true );
    createFilesList( files );
    
    // compare data sources
    try {
      int result = publisher.compareDataSourceWithRemoteConnection(databaseMeta);
      switch (result) {
        case ModelServerPublish.REMOTE_CONNECTION_SAME : {
          datasourceLabel.setValue( Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.DatasourceSame") ); //$NON-NLS-1$
          noPublishDatasourceRadio.setSelected( true );
          publishDatasourceRadio.setSelected( false );
          break;
        }
        case ModelServerPublish.REMOTE_CONNECTION_MISSING : {
          datasourceLabel.setValue( Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.DatasourceMissing") ); //$NON-NLS-1$
          break;
        }
        case ModelServerPublish.REMOTE_CONNECTION_MUST_BE_JNDI : {
          datasourceLabel.setValue( Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.DatasourceCannotPublish") ); //$NON-NLS-1$
          noPublishDatasourceRadio.setSelected( true );
          publishDatasourceRadio.setSelected( false );
          break;
        }
        case ModelServerPublish.REMOTE_CONNECTION_DIFFERENT : {
          datasourceLabel.setValue( Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.DatasourceDifferent") ); //$NON-NLS-1$
          break;
        }
      }
    } catch (Exception e) {
      logger.error(e);
      SpoonFactory.getInstance().messageBox( Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.CouldNotGetDataSources", e.getLocalizedMessage() ),  //$NON-NLS-1$
          Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.Error"), false, Const.ERROR); //$NON-NLS-1$
    }
  }
  
  public void setSelectedServer(String server){
    if( server.equals( selectedServer ) ) {
      // nothing changed
      return;
    }
    selectedServer = server;
    currentFolder = null;
    // first option - the blank one
    fileNames.clear();
    folderNames.clear();
    if ( serverMenuList.getSelectedIndex() > 0 ) {
      folderNames.add( new NamedObject("/") ); //$NON-NLS-1$
    }
    fireFilesBinding();
    fireFoldersBinding();
    datasourceLabel.setValue( Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.DatasourceUnknown") ); //$NON-NLS-1$
    if ( serverMenuList.getSelectedIndex() > 0 ) {
      // try to connect
      connect();
    }
  }

  public String getSelectedServer() {
    return selectedServer;
  }

  public void editBiServers() {
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    try {
    XulDialogBiServerList biServerConfigDialog = new XulDialogBiServerList( spoon.getShell() );
    biServerConfigDialog.showDialog();
    // refresh the server list
    biServerConfig.refreshServerList();
    createServerList();
    fireServerNamesBinding();
    
    } catch (XulException e) {
      logger.error(e);
      SpoonFactory.getInstance().messageBox( Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Xul.DialogError", e.getLocalizedMessage() ),  //$NON-NLS-1$
          Messages.getInstance().getString("Spoon.Perspectives.AgileBi.Publish.Error"), false, Const.ERROR); //$NON-NLS-1$
    }
  }

  public NamedCollection getFileNames() {
    return fileNames;
  }

  public void setFileNames(NamedCollection fileNames) {
    this.fileNames = fileNames;
  }

  public NamedCollection getFolderNames() {
    return folderNames;
  }

  public void setFolderNames(NamedCollection folderNames) {
    this.folderNames = folderNames;
  }

  public void updateButtonStatus() {
    okButton.setDisabled( currentFolder == null );
  }

  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setDatabaseMeta(DatabaseMeta databaseMeta) {
    this.databaseMeta = databaseMeta;
  }

  public BiServerConnection getBiServerConnection() {
    return biServerConnection;
  }

  public boolean isPublishDataSource() {
    return publishDataSource;
  }

  public void setPublishDataSource(boolean publishDataSource) {
    this.publishDataSource = publishDataSource;
  }

  public boolean isAccepted() {
    return accepted;
  }

  public String getPath() {
    CmisObject folder = getCurrentFile();
    if( folder == null ) {
      return null;
    }
    return publisher.getNavigationService().getRepositoryPath(folder);
  }

  public void setShowLocation(boolean showServerSelection, boolean showFolders, boolean showCurrentFolder ) {
    XulComponent component = document.getElementById("serverControls"); //$NON-NLS-1$
    component.setVisible( showServerSelection );
    component = document.getElementById("folderControls"); //$NON-NLS-1$
    component.setVisible( showFolders );
    component = document.getElementById("currentFolderControls"); //$NON-NLS-1$
    component.setVisible( showCurrentFolder );
    component = document.getElementById("locationGroupBox"); //$NON-NLS-1$
    component.setVisible( showServerSelection || showFolders || showCurrentFolder );
  }

  public void setShowDatasourceStatus(boolean showDatasourceStatus) {
    XulComponent component = document.getElementById("datasourceGroupBox"); //$NON-NLS-1$
    component.setVisible( showDatasourceStatus );
  }

  public void setShowComment(boolean showComment) {
    XulComponent component = document.getElementById("comment"); //$NON-NLS-1$
    component.setVisible( showComment );
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
    changePath();
    fireFileNameBinding();
  }
  
  private CmisObject getCurrentFile() {
    int folderNos[] = filesBox.getSelectedRows();
    if( folderNos == null || folderNos.length == 0 ) {
      return null;
    }
    CmisObject folder = ((NamedCmisObject)fileNames.get( folderNos[0] )).getCmisObject();
    return folder;
  }
  
  public void fileClick( int idx ) {
    CmisObject object = getCurrentFile();
    if( object == null ) {
      return;
    }
    String objectType = object.findStringProperty( PropertiesBase.OBJECTTYPEID );
    if( !CmisObject.OBJECT_TYPE_FOLDER.equals( objectType ) ) {
      setFilename( object.findStringProperty( CmisObject.LOCALIZEDNAME ) );
    } else {
      currentFolder = object;
    }
    updateButtonStatus();
    changePath();
  }

  public void fileDblClick() {
    int level = folderMenuList.getSelectedIndex();
    if( level >= folderTreeDepth ) {
      // we are at the maximum depth
      return;
    }
    CmisObject object = getCurrentFile();
    // is the current object a file or folder?
    String objectType = object.findStringProperty( PropertiesBase.OBJECTTYPEID );
    if( CmisObject.OBJECT_TYPE_FOLDER.equals( objectType ) ) {
      folderNames.add( new NamedCmisObject(object) );
      fireFoldersBinding();
      folderMenuList.setSelectedIndex( folderNames.size()-1 );
      currentFolder = object;
      updateFiles();
      updateButtonStatus();
      changePath();
    }
  }

  private void updateFiles() {
    int level = folderMenuList.getSelectedIndex();
    fileNames.clear();
    CmisObject folder = null;
    if( level < folderTreeDepth && level > 0 ) {
      // get the next level of files
      folder = ((NamedCmisObject)folderNames.get( level )).getCmisObject();
    }
    List<CmisObject >files = getFiles(folder, 1, false );
    createFilesList( files );
  }
  
  private void updateFolderList() {
    // remove all of the folders that come after the selected one
    String selectedFolder = null;
    if( currentFolder != null ) {
      selectedFolder = currentFolder.findStringProperty(CmisObject.LOCALIZEDNAME);
    } else {
      selectedFolder = folderNames.get(0).getName();
    }
    int idx = 0;
    for( NamedObject folder : folderNames ) {
      if( folder.getName().equals( selectedFolder ) ) {
        break;
      } else {
        idx++;
      }
    }
    boolean modified = false;
    for( int i=folderNames.size()-1; i > idx; i-- ) {
      folderNames.remove( i );
      modified = true;
    }
    if( modified ) {
      fireFoldersBinding();
      folderMenuList.setSelectedIndex( folderNames.size()-1 );
    }
    changePath();    
  }
  
  public void folderChange() {
    if( folderMenuList.getSelectedIndex() < 1 ) {
      currentFolder = null;
    } else {
      currentFolder = ((NamedCmisObject)(folderNames.get( folderMenuList.getSelectedIndex() ))).getCmisObject();
    }
    updateFolderList();
    updateButtonStatus();
    changePath();
    updateFiles();
  }
  public void folderUpClick() {
    currentFolder = null;
    if( folderNames.size() == 1 ) {
      // can't go up higher
      return;
    }
    if( folderNames.size() > 2 ) {
      currentFolder = ((NamedCmisObject)(folderNames.get( folderNames.size() -2 ))).getCmisObject();
    }
    updateFolderList();
    updateFiles();
    changePath();
    updateButtonStatus();
  }

  public void setComment(String comment) {
    commentLabel.setVisible( true );
    commentLabel.setValue( comment );
  }

  public String getPathTemplate() {
    return pathTemplate;
  }

  public void setPathTemplate(String pathTemplate) {
    this.pathTemplate = pathTemplate;
  }
  
}

