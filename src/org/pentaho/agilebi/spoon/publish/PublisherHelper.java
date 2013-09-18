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

package org.pentaho.agilebi.spoon.publish;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jfree.util.Log;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.ui.xul.XulException;

public class PublisherHelper {

  private static final String ANALYZER_EXT = ".xanalyzer";

  private static final String XMI_EXT = ".xmi";

  private static final String MONDRIAN_XML = ".mondrian.xml";

  private static String cachedPath = null;

  private static BiServerConnection cachedServer;
  
  /**
   * 
   * @param workspace
   * @param publishingFile
   * @param treeDepth
   * @param databaseMeta
   * @param fullPathtoFile
   * @param checkDatasources
   * @param setShowModel
   * @param showFolders
   * @param showCurrentFolder
   * @param serverPathTemplate
   * @param extension
   * @param databaseName
   * @return
   * @throws ModelerException
   */
  public static String publishAnalysis(ModelerWorkspace workspace, String publishingFile, int treeDepth,
      DatabaseMeta databaseMeta, String fullPathtoFile, boolean checkDatasources, boolean setShowModel,
      boolean showFolders, boolean showCurrentFolder, String serverPathTemplate, String extension, String databaseName)
      throws ModelerException {
    try {

      if (StringUtils.isEmpty(publishingFile)) {
        SpoonFactory.getInstance().messageBox(
            BaseMessages.getString(ModelerWorkspace.class, "ModelServerPublish.Publish.UnsavedModel"), //$NON-NLS-1$
            "Dialog Error", false, Const.ERROR); //$NON-NLS-1$
        throw new ModelerException(BaseMessages.getString(ModelerWorkspace.class,
            "ModelServerPublish.Publish.UnsavedModel"));
      }

      ModelServerPublish publisher = new ModelServerPublish();
      publisher.setModel(workspace);
      Spoon spoon = ((Spoon) SpoonFactory.getInstance());
      try {
        XulDialogPublish publishDialog = new XulDialogPublish(spoon.getShell());
        publishDialog.setFolderTreeDepth(treeDepth);
        publishDialog.setDatabaseMeta(databaseMeta);

        publishDialog.setFilename(publishingFile);
        publishDialog.setCheckDatasources(checkDatasources);
        publishDialog.setPathTemplate(serverPathTemplate);
        publishDialog.setPath(cachedPath);
        publishDialog.setSelectedServer(cachedServer);
        publishDialog.setModelName(workspace.getModelName());
        if(showFolders)
          publishDialog.showFileGroupBoxFolder();
        else
          publishDialog.hideFileGroupBoxFolder();
        try {          
          publishDialog.showDialog();
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (publishDialog.isAccepted()) {
          // now try to publish
          String selectedPath = publishDialog.getPath();          
          if(selectedPath != null){
            cachedPath = selectedPath;
          } else {
            selectedPath = cachedPath;
          }        
          // we always publish to {solution}/resources/metadata
          BiServerConnection biServerConnection = publishDialog.getBiServerConnection();
          publisher.setBiServerConnection(biServerConnection);
          cachedServer = biServerConnection;
          boolean publishDatasource = publishDialog.isPublishDataSource();
          String repositoryPath = null;
          if (serverPathTemplate != null) {
            String selectedSolution = null;
            if (selectedPath.indexOf(RepositoryFile.SEPARATOR) != -1) { //$NON-NLS-1$
              selectedSolution = selectedPath.substring(0, selectedPath.indexOf(RepositoryFile.SEPARATOR)); //$NON-NLS-1$   
            } else {
              selectedSolution = selectedPath;
            }
            repositoryPath = serverPathTemplate.replace("{path}", selectedSolution); //$NON-NLS-1$
          }
          if (publishingFile.endsWith(XMI_EXT)) { //$NON-NLS-1$
            selectedPath = repositoryPath;
          }

          String filename = publishDialog.getFilename();

          try {
             //need to create and store the XMI file in /tmp first
            String tempXmiFilename =workspace.getModel().getName()+XMI_EXT;
            File tempXmi = createTempFile(workspace.getFileName(), tempXmiFilename );
            File tempF =   createTempFile(fullPathtoFile,filename);        

            replaceAttributeValue("report", "catalog", workspace.getModelName(), tempXmi.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$           
            replaceAttributeValue("report", "catalog", workspace.getModelName(), tempF.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
            
            
            publisher
              .publishToServer(
                workspace.getModelName() + MONDRIAN_XML, databaseName, workspace.getModelName(), repositoryPath, selectedPath, publishDatasource, true, publishDialog.isExistentDatasource(), true, tempF.getAbsolutePath());
            
          } catch(Exception ex) {
            Log.error(ex.getLocalizedMessage(),ex);
            throw new ModelerException(ex);
          }
        }
      } catch (XulException e) {
        Log.error(e.getLocalizedMessage(),e);
        SpoonFactory.getInstance().messageBox("Could not create dialog: " + e.getLocalizedMessage(), "Dialog Error", //$NON-NLS-1$ //$NON-NLS-2$
            false, Const.ERROR);
      }
    } catch (Exception e) {
      Log.error(e.getLocalizedMessage(),e);          
      SpoonFactory.getInstance().messageBox(BaseMessages.getString(PublisherHelper.class, "PublisherHelper.PublishError") , 
          BaseMessages.getString(PublisherHelper.class, "PublisherHelper.PublishErrorTitle"), 
          false, Const.ERROR);
      throw new ModelerException(e);
    }
    return fullPathtoFile;
  }

  private static String replaceAttributeValue(String aElement, String anAttribute, String aValue, String aFile)
      throws Exception {

    String originalValue = null;
    if (aFile != null) {
      SAXReader reader = new SAXReader();
      Document doc = reader.read(new File(aFile));
      Element root = doc.getRootElement();

      for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
        Element element = i.next();
        if (element.getName().equals(aElement)) {
          Attribute attr = element.attribute(anAttribute);
          originalValue = attr.getValue();
          attr.setValue(aValue);
        }
      }

      XMLWriter writer = new XMLWriter(new FileWriter(aFile));
      writer.write(doc);
      writer.close();
    }
    return originalValue;
  }

  public static String publish(ModelerWorkspace workspace, String publishingFile, int treeDepth,
      DatabaseMeta databaseMeta, String filename, boolean checkDatasources, boolean setShowModel, boolean showFolders,
      boolean showCurrentFolder, boolean isExistentDatasource,String serverPathTemplate, String extension, String databaseName) throws ModelerException {
    try {

      if (StringUtils.isEmpty(publishingFile)) {
        SpoonFactory.getInstance().messageBox(
            BaseMessages.getString(ModelerWorkspace.class, "ModelServerPublish.Publish.UnsavedModel"), 
            "Dialog Error", false, Const.ERROR); //$NON-NLS-1$
        throw new ModelerException(BaseMessages.getString(ModelerWorkspace.class,
            "ModelServerPublish.Publish.UnsavedModel"));
      }

      ModelServerPublish publisher = new ModelServerPublish();
      publisher.setModel(workspace);
      Spoon spoon = ((Spoon) SpoonFactory.getInstance());
      try {
        XulDialogPublish publishDialog = new XulDialogPublish(spoon.getShell());
        publishDialog.setDatabaseMeta(databaseMeta);
        publishDialog.setFilename(filename);
        publishDialog.setCheckDatasources(checkDatasources);
        publishDialog.setSelectedServer(cachedServer);
        if(showFolders)
          publishDialog.showFileGroupBoxFolder();
        else
          publishDialog.hideFileGroupBoxFolder();
        try {
          
          publishDialog.showDialog();
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (publishDialog.isAccepted()) {
          // now try to publisher.setModelerHelperInstance()
          String selectedPath = publishDialog.getPath();
          cachedPath = selectedPath;
          // we always publish to {solution}/resources/metadata
          BiServerConnection biServerConnection = publishDialog.getBiServerConnection();
          cachedServer = biServerConnection;
          publisher.setBiServerConnection(biServerConnection);
          boolean publishDatasource = publishDialog.isPublishDataSource();
          String repositoryPath = "/";
          if(serverPathTemplate != null) {
            String selectedSolution = null;
            if(selectedPath != null && selectedPath.indexOf("/") != -1) { //$NON-NLS-1$
              selectedSolution = selectedPath.substring(0, selectedPath.indexOf("/")); //$NON-NLS-1$   
            } else {
              if(selectedPath != null)
                selectedSolution = selectedPath;
            }
            if(selectedSolution != null)
              repositoryPath = serverPathTemplate.replace("{path}", selectedSolution); //$NON-NLS-1$
          }
          if(publishingFile.endsWith(XMI_EXT)) { //$NON-NLS-1$
            selectedPath = repositoryPath;
          }

          filename = publishDialog.getFilename();
          String tempFilename = filename + extension;

          File tempF = createTempFile(publishingFile, tempFilename);
         
          replaceAttributeValue("report", "catalog", workspace.getModelName(), tempF.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
         
          publisher
            .publishToServer(
              workspace.getModelName() + MONDRIAN_XML, databaseName, workspace.getModelName(), repositoryPath, selectedPath, publishDatasource, true, publishDialog.isExistentDatasource(), false, tempF.getAbsolutePath());

        }
             
      } catch (XulException e) {
       Log.error(e.getLocalizedMessage(),e);
        SpoonFactory.getInstance().messageBox("Could not create dialog: " + e.getLocalizedMessage(), "Dialog Error", //$NON-NLS-1$ //$NON-NLS-2$
            false, Const.ERROR);
      }
    } catch (Exception e) {
      Log.error(e.getLocalizedMessage(),e);          
      SpoonFactory.getInstance().messageBox(BaseMessages.getString(PublisherHelper.class, "PublisherHelper.PublishError") , 
          BaseMessages.getString(PublisherHelper.class, "PublisherHelper.PublishErrorTitle"), 
          false, Const.ERROR);
      throw new ModelerException(e);
    }
    return filename;
  }

  /**
   * Copy the source file to a local input File stream
   * @param publishingFile (full path to location of file)
   * @param tempFilename (this is just the filename and extension)
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   */
  private static File createTempFile(String publishingFile, String tempFilename) throws IOException,
      FileNotFoundException {
    File tempDir = new File("tmp");
    if(tempDir.exists() == false){
      tempDir.mkdir();
    }
    File tempF = new File(tempDir,tempFilename);
    if(tempF.exists() == false){
      tempF.createNewFile();
    }
    tempF.deleteOnExit();
    IOUtils.copy(new FileInputStream(new File(publishingFile)), new FileOutputStream(tempF));
    return tempF;
  }

  /**
   * Reports will be removed in future release
   * @param report
   * @param workspace
   * @param modelName
   * @param prpt
   * @param treeDepth
   * @param databaseMeta
   * @param xmiFile
   * @param checkDatasources
   * @param showServerSelection
   * @param showFolders
   * @param showCurrentFolder
   * @param serverPathTemplate
   * @param databaseName
   * @throws ModelerException
   */
  @Deprecated
  public static void publishPrpt(MasterReport report, ModelerWorkspace workspace, String modelName, String prpt,
      int treeDepth, DatabaseMeta databaseMeta, String xmiFile, boolean checkDatasources, boolean showServerSelection,
      boolean showFolders, boolean showCurrentFolder, String serverPathTemplate, String databaseName)
      throws ModelerException {
    try {

      if (StringUtils.isEmpty(prpt)) {
        SpoonFactory.getInstance().messageBox(
            BaseMessages.getString(ModelerWorkspace.class, "ModelServerPublish.Publish.UnsavedModel"), //$NON-NLS-1$
            "Dialog Error", false, Const.ERROR); //$NON-NLS-1$
        return;
      }

      ModelServerPublish publisher = new ModelServerPublish();
      publisher.setModel(workspace);
      Spoon spoon = ((Spoon) SpoonFactory.getInstance());
      try {
        XulDialogPublish publishDialog = new XulDialogPublish(spoon.getShell());
        publishDialog.setDatabaseMeta(databaseMeta);
        String name = prpt.substring(prpt.lastIndexOf(File.separator) + 1);
        publishDialog.setFilename(name);
        publishDialog.setCheckDatasources(checkDatasources);
        publishDialog.setPathTemplate(serverPathTemplate);
        publishDialog.setPath(cachedPath);
        publishDialog.setSelectedServer(cachedServer);
        publishDialog.setModelName(workspace.getModelName());
        if(showFolders)
          publishDialog.showFileGroupBoxFolder();
        else
          publishDialog.hideFileGroupBoxFolder();
        
        publishDialog.showDialog();
        if (publishDialog.isAccepted()) {
          // now try to publish
          String thePrptPublishingPath = publishDialog.getPath();
          cachedPath = thePrptPublishingPath;
          // we always publish to {solution}/resources/metadata
          BiServerConnection biServerConnection = publishDialog.getBiServerConnection();
          publisher.setBiServerConnection(biServerConnection);

          cachedServer = biServerConnection;
          boolean publishDatasource = publishDialog.isPublishDataSource();
          String theXmiPublishingPath = null;
          if (serverPathTemplate != null) {
            String theSolution = null;
            if (thePrptPublishingPath.indexOf("/") != -1) { //$NON-NLS-1$
              theSolution = thePrptPublishingPath.substring(0, thePrptPublishingPath.indexOf("/")); //$NON-NLS-1$   
            } else {
              theSolution = thePrptPublishingPath;
            }
            theXmiPublishingPath = serverPathTemplate.replace("{path}", theSolution); //$NON-NLS-1$
          }

          // Set the domain id to the xmi.
          String theXmiFile = xmiFile.substring(xmiFile.lastIndexOf(File.separator) + 1, xmiFile.length()); //$NON-NLS-1$
          PmdDataFactory thePmdDataFactory = (PmdDataFactory) report.getDataFactory();
          String theDomainId = theXmiPublishingPath + "/" + theXmiFile; //$NON-NLS-1$
          thePmdDataFactory.setDomainId(theDomainId);

          // Point the mql query to the xmi instead of default.
          String theMQLQuery = thePmdDataFactory.getQuery("default"); //$NON-NLS-1$
          String theQuery = theMQLQuery.substring(0, theMQLQuery.indexOf("<domain_id>") + 11) //$NON-NLS-1$
              + theDomainId + theMQLQuery.substring(theMQLQuery.indexOf("</domain_id>")); //$NON-NLS-1$
          thePmdDataFactory.setQuery("default", theQuery); //$NON-NLS-1$

          try {
            File tempDir = new File("tmp");
            if (tempDir.exists() == false) {
              tempDir.mkdir();
            }
            File tempF = new File(tempDir, publishDialog.getFilename());
            if (tempF.exists() == false) {
              tempF.createNewFile();
            }
            tempF.deleteOnExit();
            BundleWriter.writeReportToZipFile(report, tempF);
            publisher.publishPrptToServer(theXmiPublishingPath, thePrptPublishingPath, publishDatasource,
                publishDialog.isExistentDatasource(), publishDialog.isPublishXmi(), xmiFile, tempF.getAbsolutePath());

          } catch (Exception e) {
            throw new ModelerException(e);
          } finally {
            thePmdDataFactory.setQuery("default", theMQLQuery); //$NON-NLS-1$
          }
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

  public static String getBiServerCompatibleDatabaseName(String name) {
    if (name == null) {
      return null;
    }
    // replace spaces with underscores
    return name.replaceAll("\\s", "_");
  }

}
