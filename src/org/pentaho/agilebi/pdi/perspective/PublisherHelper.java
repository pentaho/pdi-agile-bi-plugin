package org.pentaho.agilebi.pdi.perspective;

import java.io.File;

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
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
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
        publishDialog.setComment(comment); 
        publishDialog.setDatabaseMeta(databaseMeta);
        publishDialog.setFilename(filename);
        publishDialog.setCheckDatasources(checkDatasources);
        publishDialog.setShowLocation(showServerSelection, showFolders, showCurrentFolder);
        publishDialog.setPathTemplate(serverPathTemplate);
        publishDialog.showDialog();
        if (publishDialog.isAccepted()) {
          // now try to publish
          String selectedPath = publishDialog.getPath();
          // we always publish to {solution}/resources/metadata
          BiServerConnection biServerConnection = publishDialog.getBiServerConnection();
          publisher.setBiServerConnection(biServerConnection);
          boolean publishDatasource = publishDialog.isPublishDataSource();
          String repositoryPath = null;
          if(serverPathTemplate != null) {
            String selectedSolution = null;
            if(selectedPath.indexOf("/") != -1) { //$NON-NLS-1$
              selectedSolution = selectedPath.substring(0, selectedPath.indexOf("/")); //$NON-NLS-1$   
            } else {
              selectedSolution = selectedPath;
            }
            repositoryPath = serverPathTemplate.replace("{path}", selectedSolution); //$NON-NLS-1$
          }
          if(publishingFile.endsWith(".xmi")) { //$NON-NLS-1$
            selectedPath = repositoryPath;
          }

          filename = publishDialog.getFilename();
          publisher
              .publishToServer(
                  filename + extension, databaseName, filename, repositoryPath, selectedPath, publishDatasource, true, publishDialog.isExistentDatasource(), publishingFile);
        }
      } catch (XulException e) {
        e.printStackTrace();
        SpoonFactory.getInstance().messageBox("Could not create dialog: " + e.getLocalizedMessage(), "Dialog Error", //$NON-NLS-1$ //$NON-NLS-2$
            false, Const.ERROR);
      }
    } catch (Exception e) {
      throw new ModelerException(e);
    }
  }
  
  public static void publishPrpt(MasterReport report, ModelerWorkspace workspace, String xmi, String prpt,
      String comment, int treeDepth, DatabaseMeta databaseMeta, String modelName, boolean checkDatasources, 
      boolean showServerSelection, boolean showFolders, boolean showCurrentFolder, String serverPathTemplate, String databaseName ) throws ModelerException {
    try {

      if (StringUtils.isEmpty(prpt)) {
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
        publishDialog.setComment(comment);
        publishDialog.setDatabaseMeta(databaseMeta);
        publishDialog.setFilename(modelName);
        publishDialog.setCheckDatasources(checkDatasources);
        publishDialog.setShowLocation(showServerSelection, showFolders, showCurrentFolder);
        publishDialog.setPathTemplate(serverPathTemplate);
        publishDialog.showDialog();
        if (publishDialog.isAccepted()) {
          // now try to publish
          String thePrptPublishingPath = publishDialog.getPath();
          // we always publish to {solution}/resources/metadata
          BiServerConnection biServerConnection = publishDialog.getBiServerConnection();
          publisher.setBiServerConnection(biServerConnection);
          boolean publishDatasource = publishDialog.isPublishDataSource();
          String theXmiPublishingPath = null;
          if(serverPathTemplate != null) {
            String theSolution = null;
            if(thePrptPublishingPath.indexOf("/") != -1) { //$NON-NLS-1$
              theSolution = thePrptPublishingPath.substring(0, thePrptPublishingPath.indexOf("/")); //$NON-NLS-1$   
            } else {
              theSolution = thePrptPublishingPath;
            }
            theXmiPublishingPath = serverPathTemplate.replace("{path}", theSolution);  //$NON-NLS-1$
          }
          
          // Set the domain id to the xmi.
          String theXmiFile = xmi.substring(xmi.lastIndexOf("\\") + 1, xmi.length()); //$NON-NLS-1$
          PmdDataFactory thePmdDataFactory = (PmdDataFactory) report.getDataFactory();
          String theDomainId = theXmiPublishingPath + "/" + theXmiFile; //$NON-NLS-1$
          thePmdDataFactory.setDomainId(theDomainId);
          
          
          // Point the mql query to the xmi instead of default.
          String theMQLQuery = thePmdDataFactory.getQuery("default"); //$NON-NLS-1$
          String theQuery = theMQLQuery.substring(0, theMQLQuery.lastIndexOf("default")) //$NON-NLS-1$
                            + theDomainId + theMQLQuery.substring(theMQLQuery.lastIndexOf("default") + 7, theMQLQuery.length()); //$NON-NLS-1$
          thePmdDataFactory.setQuery("default", theQuery); //$NON-NLS-1$
          
          try {
            BundleWriter.writeReportToZipFile(report, new File(prpt));
          } catch (Exception e) {
            throw new ModelerException(e);
          }

          publisher.publishPrptToServer(theXmiPublishingPath, thePrptPublishingPath, publishDatasource, publishDialog.isExistentDatasource(), xmi, prpt); 
          
          thePmdDataFactory.setQuery("default", theMQLQuery); //$NON-NLS-1$
        }
      } catch (XulException e) {
        e.printStackTrace();
        SpoonFactory.getInstance().messageBox("Could not create dialog: " + e.getLocalizedMessage(), "Dialog Error", //$NON-NLS-1$ //$NON-NLS-2$
            false, Const.ERROR); 
      }
    } catch (Exception e) {
      throw new ModelerException(e);
    }
  }

}
