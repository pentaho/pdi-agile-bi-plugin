package org.pentaho.agilebi.pdi.perspective;

import org.pentaho.agilebi.pdi.modeler.BiServerConnection;
import org.pentaho.agilebi.pdi.modeler.Messages;
import org.pentaho.agilebi.pdi.modeler.ModelServerPublish;
import org.pentaho.agilebi.pdi.modeler.ModelerException;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.pdi.modeler.ModelerWorkspaceUtil;
import org.pentaho.agilebi.pdi.modeler.XulDialogPublish;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.ui.xul.XulException;

public class PublisherHelper {

  public static void publish(ModelerWorkspace workspace, String publishingFile) throws ModelerException {
    try {

      if (StringUtils.isEmpty(publishingFile)) {
        SpoonFactory.getInstance().messageBox(Messages.getString("ModelServerPublish.Publish.UnsavedModel"),
            "Dialog Error", false, Const.ERROR);
        return;
      }

      ModelerWorkspaceUtil.populateDomain(workspace);
      ModelServerPublish publisher = new ModelServerPublish();
      publisher.setModel(workspace);

      Spoon spoon = ((Spoon) SpoonFactory.getInstance());
      try {
        XulDialogPublish publishDialog = new XulDialogPublish(spoon.getShell());
        publishDialog.setFolderTreeDepth(1);
        publishDialog.setComment(Messages.getString("ModelServerPublish.Publish.ModelPublishComment")); //$NON-NLS-1$
        DatabaseMeta databaseMeta = workspace.getModelSource().getDatabaseMeta();
        publishDialog.setDatabaseMeta(databaseMeta);
        publishDialog.setFilename(workspace.getModelName());
        publishDialog.setCheckDatasources(true);
        publishDialog.setShowLocation(true, true, false);
        String template = "{path}" + //$NON-NLS-1$
            "resources" + ISolutionRepository.SEPARATOR + //$NON-NLS-1$
            "metadata" + ISolutionRepository.SEPARATOR + //$NON-NLS-1$
            "{file}.xmi"; //$NON-NLS-1$ 
        publishDialog.setPathTemplate(template);
        publishDialog.showDialog();
        if (publishDialog.isAccepted()) {
          // now try to publish
          String path = publishDialog.getPath();
          // we always publish to {solution}/resources/metadata
          StringBuilder sb = new StringBuilder();
          BiServerConnection biServerConnection = publishDialog.getBiServerConnection();
          publisher.setBiServerConnection(biServerConnection);
          boolean publishDatasource = publishDialog.isPublishDataSource();
          sb.append(path).append(ISolutionRepository.SEPARATOR).append("resources") //$NON-NLS-1$
              .append(ISolutionRepository.SEPARATOR).append("metadata"); //$NON-NLS-1$
          String repositoryPath = sb.toString();
          String filename = publishDialog.getFilename();

          publisher
              .publishToServer(
                  filename + ".mondrian.xml", workspace.getDatabaseName(), filename, repositoryPath, publishDatasource, true, publishDialog.isExistentDatasource(), publishingFile); //$NON-NLS-1$
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
