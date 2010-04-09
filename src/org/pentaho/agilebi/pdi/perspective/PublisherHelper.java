package org.pentaho.agilebi.pdi.perspective;

import org.pentaho.agilebi.pdi.modeler.BiServerConnection;
import org.pentaho.agilebi.pdi.modeler.ModelServerPublish;
import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.XulDialogPublish;
import org.pentaho.agilebi.pdi.modeler.XulUI;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.ui.xul.XulException;

public class PublisherHelper {

  public static void publish(ModelerWorkspace workspace, String publishingFile,
      String comment, int treeDepth, DatabaseMeta databaseMeta, String filename, boolean checkDatasources, 
      boolean showServerSelection, boolean showFolders, boolean showCurrentFolder, String serverPathTemplate, String extension, String databaseName ) throws ModelerException {
    try {

      if (StringUtils.isEmpty(publishingFile)) {
        SpoonFactory.getInstance().messageBox(BaseMessages.getString(XulUI.class,"ModelServerPublish.Publish.UnsavedModel"), //$NON-NLS-1$
            "Dialog Error", false, Const.ERROR); //$NON-NLS-1$
        return;
      }

      ModelServerPublish publisher = new ModelServerPublish();
      publisher.setModel(workspace);
      Spoon spoon = ((Spoon) SpoonFactory.getInstance());
      try {
        XulDialogPublish publishDialog = new XulDialogPublish(spoon.getShell());
        publishDialog.setFolderTreeDepth(treeDepth);
        publishDialog.setComment(comment); //$NON-NLS-1$
        publishDialog.setDatabaseMeta(databaseMeta);
        publishDialog.setFilename(filename);
        publishDialog.setCheckDatasources(checkDatasources);
        publishDialog.setShowLocation(showServerSelection, showFolders, showCurrentFolder);
        publishDialog.setPathTemplate(serverPathTemplate);
        publishDialog.showDialog();
        if (publishDialog.isAccepted()) {
          // now try to publish
          String path = publishDialog.getPath();
          // we always publish to {solution}/resources/metadata
          StringBuilder sb = new StringBuilder();
          BiServerConnection biServerConnection = publishDialog.getBiServerConnection();
          publisher.setBiServerConnection(biServerConnection);
          boolean publishDatasource = publishDialog.isPublishDataSource();
          String repositoryPath = path;
          if( serverPathTemplate != null ) {
            repositoryPath = serverPathTemplate.replace("{path}", path);
          }
          filename = publishDialog.getFilename();

          publisher
              .publishToServer(
                  filename + extension, databaseName, filename, repositoryPath, publishDatasource, true, publishDialog.isExistentDatasource(), publishingFile); //$NON-NLS-1$
        }
      } catch (XulException e) {
        e.printStackTrace();
        SpoonFactory.getInstance().messageBox("Could not create dialog: " + e.getLocalizedMessage(), "Dialog Error",
            false, Const.ERROR);
      }
    } catch (Exception e) {
      throw new ModelerException(e);
    }
  }

}
