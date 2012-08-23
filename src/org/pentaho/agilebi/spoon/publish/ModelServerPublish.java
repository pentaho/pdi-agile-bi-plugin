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
package org.pentaho.agilebi.spoon.publish;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.dom4j.DocumentHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.util.ISpoonModelerSource;
import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.PropertiesBase;
import org.pentaho.commons.util.repository.type.TypesOfFileableObjects;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.platform.api.repository.ISolutionRepository;

import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.util.client.BiPlatformRepositoryClient;
import org.pentaho.platform.util.client.BiPlatformRepositoryClientNavigationService;
import org.pentaho.platform.util.client.PublisherUtil;
import org.pentaho.platform.util.client.ServiceException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * A utility class for publishing models to a BI server. Also helps synchronize database connections.
 * @author jamesdixon
 *
 */
public class ModelServerPublish {

  private static final String PLUGIN_DATA_ACCESS_API_CONNECTION_ADD = "plugin/data-access/api/connection/add";

  private static final String PLUGIN_DATA_ACCESS_API_CONNECTION_UPDATE = "plugin/data-access/api/connection/update";

  private static final String DATA_ACCESS_API_CONNECTION_GET = "plugin/data-access/api/connection/get/";

  private static final String DATA_ACCESS_API_CONNECTION_LIST = "plugin/data-access/api/connection/list";

  public static final int PUBLISH_UNKNOWN_PROBLEM = -1;

  public static final int PUBLISH_FILE_EXISTS = 1;

  public static final int PUBLISH_FAILED = 2;

  public static final int PUBLISH_SUCCESS = 3;

  public static final int PUBLISH_INVALID_PASSWORD = 4;

  public static final int PUBLISH_INVALID_USER_OR_PASSWORD = 5;

  public static final int PUBLISH_DATASOURCE_PROBLEM = 6;

  public static final int PUBLISH_CATALOG_EXISTS = 8;

  public static final int REMOTE_CONNECTION_MISSING = 1;

  public static final int REMOTE_CONNECTION_DIFFERENT = 2;

  public static final int REMOTE_CONNECTION_SAME = 4;

  public static final int REMOTE_CONNECTION_MUST_BE_JNDI = 8;

  private static final int DATASOURCE_DRIVER_MISSING = 9;

  private BiServerConnection biServerConnection;

  private Connection remoteConnection;

  private ModelerWorkspace model;

  private int serviceClientStatus = 0;

  private BiPlatformRepositoryClientNavigationService navigationService;

  //TODO: find a better way to communicate the UI delegate
  public static PublishOverwriteDelegate overwriteDelegate;

  private Client client = null;

  public ModelServerPublish() {
    // get information about the remote connection
    ClientConfig clientConfig = new DefaultClientConfig();
    this.client = Client.create(clientConfig);
  }

  public ModelServerPublish(BiServerConnection aBiServerConnection) {
    super();
    this.setBiServerConnection(aBiServerConnection);
  }

  /**
   * Lists the database connections that are available on the current BI server
   * @return
   * @throws ConnectionServiceException
   */
  public List<Connection> listRemoteConnections() throws ConnectionServiceException {
    Connection[] connectionArray = null;
    String storeDomainUrl = biServerConnection.getUrl() + DATA_ACCESS_API_CONNECTION_LIST;
    WebResource resource = client.resource(storeDomainUrl);
    String str = resource.type(MediaType.APPLICATION_JSON).get(String.class);
    try {
      connectionArray = resource.type(MediaType.APPLICATION_JSON).get(Connection[].class);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return Arrays.asList(connectionArray);
  }

  /**
   * Returns the remote connection. If the force flag is set the connection is 
   * always refreshed from the remote BI server. If the force flag is not set
   * a cached connection is returned.
   * @return
   */
  public Connection getRemoteConnection(String connectionName, boolean force) {
    if (remoteConnection == null || force) {
      // get information about the remote connection
      String storeDomainUrl = biServerConnection.getUrl() + DATA_ACCESS_API_CONNECTION_GET ;
      WebResource resource = client.resource(storeDomainUrl);
      try {
        remoteConnection = resource
            .type(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_XML)
            .entity(connectionName)
            .get(Connection.class);
      } catch (Exception ex) {
        //ex.printStackTrace();
        remoteConnection = null;
      }
    }
    return remoteConnection;

  }

  /**
   * Compares a provided DatabaseMeta with the database connections available on the current BI server.
   * Returns the result of the comparison - missing, same, different.
   * This only works for native connections (JNDI)
   * @param databaseMeta
   * @return
   * @throws ConnectionServiceException
   * @throws KettleDatabaseException
   */
  public int compareDataSourceWithRemoteConnection(DatabaseMeta databaseMeta) throws ConnectionServiceException,
      KettleDatabaseException {

    int result = 0;
    if (databaseMeta.getAccessType() != DatabaseMeta.TYPE_ACCESS_NATIVE) {
      result += REMOTE_CONNECTION_MUST_BE_JNDI;
      return result;
    }

    // compare the local database meta with the remote connection
    String connectionName = PublisherHelper.getBiServerCompatibleDatabaseName(databaseMeta.getName());
    Connection connection = getRemoteConnection(connectionName, false);
    if (connection == null) {
      // the connection does not exist (with the same name) on the remote BI server 
      result += REMOTE_CONNECTION_MISSING;
      return result;
    }
    // see if the driver, url, and user are the same for both connections...
    String url = databaseMeta.getURL();
    String userName = databaseMeta.getUsername();
    String driverClass = databaseMeta.getDriverClass();
    boolean urlMatch = url.equals(connection.getUrl());
    boolean userMatch = (userName == null && connection.getUsername() == null)
        || userName.equals(connection.getUsername());
    boolean driverMatch = (driverClass == null && connection.getDriverClass() == null)
        || driverClass.equals(connection.getDriverClass());
    // return 'same' or 'different'
    if (urlMatch && userMatch && driverMatch) {
      result += REMOTE_CONNECTION_SAME;
    } else {
      result += REMOTE_CONNECTION_DIFFERENT;
    }

    return result;
  }

  /**
   * Returns a list of repository folders
   * @param depth
   * @return
   * @throws Exception
   */
  @Deprecated
  public List<CmisObject> getRepositoryFiles(CmisObject folder, int depth, boolean foldersOnly) throws Exception {
    getNavigationService();

    TypesOfFileableObjects folderTypes;
    if (foldersOnly) {
      folderTypes = new TypesOfFileableObjects(TypesOfFileableObjects.FOLDERS);
    } else {
      folderTypes = new TypesOfFileableObjects(TypesOfFileableObjects.ANY);

    }
    String startLocation = ""; //$NON-NLS-1$
    if (folder != null) {
      startLocation = folder.findIdProperty(PropertiesBase.OBJECTID, null);
    }
    List<CmisObject> objects = getNavigationService().getDescendants(BiPlatformRepositoryClient.PLATFORMORIG,
        startLocation, folderTypes, depth, null, false, false);
    return objects;
  }

  public int publishFile(String repositoryPath, File[] files, boolean showFeedback) {

    for (int i = 0; i < files.length; i++) {
      if (checkForExistingFile(repositoryPath, files[i].getName())) {
        boolean overwrite = overwriteDelegate.handleOverwriteNotification(files[i].getName());
        if (overwrite == false) {
          return PublisherUtil.FILE_EXISTS;
        }
      }
    }

    String DEFAULT_PUBLISH_URL = biServerConnection.getUrl() + "RepositoryFilePublisher"; //$NON-NLS-1$
    int result = PublisherUtil.publish(DEFAULT_PUBLISH_URL, repositoryPath, files, "publishPasswordNotUsed",
        biServerConnection.getUserId(), biServerConnection.getPassword(), true, true);

    if (showFeedback) {
      showFeedback(result);
    }
    return result;
  }

  /**
   * Publishes a datasource to the current BI server
   * @param databaseMeta
   * @param update
   * @return
   * @throws KettleDatabaseException
   */
  private boolean publishDataSource(DatabaseMeta databaseMeta, boolean update) throws KettleDatabaseException,
      ConnectionServiceException {

    // create a new connection object and populate it from the databaseMeta
    Connection connection = new Connection();
    connection.setDriverClass(databaseMeta.getDriverClass());
    connection.setName(PublisherHelper.getBiServerCompatibleDatabaseName(databaseMeta.getName()));
    connection.setPassword(databaseMeta.getPassword());
    connection.setUrl(databaseMeta.getURL());
    connection.setUsername(databaseMeta.getUsername());

    boolean result = updateConnection(connection, update);

    return result;

  }

  /**
   * Jersey call to add or update connection
   * @param connection
   * @param update
   * @return
   */
  private boolean updateConnection(Connection connection, boolean update) {
    boolean result = false;
    String storeDomainUrl;
    try {     
      if (update) {
        storeDomainUrl = biServerConnection.getUrl() + PLUGIN_DATA_ACCESS_API_CONNECTION_UPDATE;
        //updateConnection url
      } else {
        storeDomainUrl = biServerConnection.getUrl() + PLUGIN_DATA_ACCESS_API_CONNECTION_ADD;
        //addConnection url
      }
      WebResource resource = client.resource(storeDomainUrl);
      result = resource.accept(MediaType.APPLICATION_JSON)
          .entity(connection)
          .type( MediaType.TEXT_PLAIN )
          .post(Boolean.class);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return result;
  }

  /**
   * Publishes a file to the current BI server
   * @param publishPath
   * @param publishFile
   * @param jndiName
   * @param modelId
   * @param enableXmla
   * @return
   * @throws Exception
   * @throws UnsupportedEncodingException
   * @deprecated
   */
  public int publish(String publishPath, File publishFile, String jndiName, String modelId, boolean enableXmla)
      throws Exception, UnsupportedEncodingException {

    String url = biServerConnection.getUrl();
    StringBuilder sb = new StringBuilder();
    sb.append(url);
    if (url.charAt(url.length() - 1) != ISolutionRepository.SEPARATOR) {
      sb.append(ISolutionRepository.SEPARATOR);
    }
    sb.append("MondrianCatalogPublisher?publishPath=") //$NON-NLS-1$
        .append(URLEncoder.encode(publishPath, "UTF-8")) //$NON-NLS-1$
        .append("&publishKey=").append(getPasswordKey(new String("publishPasswordNotUsed"))) //$NON-NLS-1$
        .append("&overwrite=true&mkdirs=true") //$NON-NLS-1$
        .append("&jndiName=").append(jndiName) //$NON-NLS-1$
        .append("&enableXmla=").append(enableXmla) //$NON-NLS-1$
        .append("&userid=").append(biServerConnection.getUserId()) //$NON-NLS-1$
        .append("&password=").append(biServerConnection.getPassword()); //$NON-NLS-1$ 
    String fullUrl = sb.toString();
    PostMethod filePost = new PostMethod(fullUrl);
    ArrayList<Part> parts = new ArrayList<Part>();
    try {
      parts.add(new FilePart(publishFile.getName(), publishFile));
    } catch (FileNotFoundException e) {
      // file is not existing or not readable, this should not happen
      e.printStackTrace();
    }
    filePost.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), filePost.getParams()));
    HttpClient client = getClient(biServerConnection.getUserId(), biServerConnection.getPassword());
    try {
      serviceClientStatus = client.executeMethod(filePost);
    } catch (IOException e) {
      throw new Exception(e.getMessage(), e);
    }
    //  if (serviceClientStatus != HttpStatus.SC_OK) {
    //      if (serviceClientStatus == HttpStatus.SC_MOVED_TEMPORARILY) {
    //          throw new Exception(BaseMessages.getString(this.getClass(), "ModelServerPublish.Errors.InvalidUser")); //$NON-NLS-1$
    //      } else {
    //          throw new Exception(BaseMessages.getString(this.getClass(), "ModelServerPublish.Errors.UnknownError", Integer.toString(serviceClientStatus)) ); //$NON-NLS-1$
    //      }
    //  } else {
    try {
      String postResult = filePost.getResponseBodyAsString();
      int publishResult = Integer.parseInt(postResult.trim());
      return publishResult;

    } catch (IOException e) {
      e.printStackTrace();
      return ModelServerPublish.PUBLISH_UNKNOWN_PROBLEM;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return ModelServerPublish.PUBLISH_UNKNOWN_PROBLEM;
    }
    //  }
  }

  /**
   * Jersey call to use the put service to load a mondrain file into the Jcr repsoitory
   * @param mondrianFile
   * @param catalogName
   * @param datasourceInfo
   * @param overwriteInRepos
   * @throws Exception
   */
  public int publishMondrainSchema(InputStream mondrianFile, String catalogName, String datasourceInfo,
      boolean overwriteInRepos) throws Exception {
    String storeDomainUrl = biServerConnection.getUrl() + "plugin/data-access/api/mondrian/postAnalysis";
    WebResource resource = client.resource(storeDomainUrl);
    String parms = "Datasource=" + datasourceInfo;
    String response = "-1";
    FormDataMultiPart part = new FormDataMultiPart();
    part.field("parameters", parms, MediaType.MULTIPART_FORM_DATA_TYPE)
        .field("uploadAnalysis", mondrianFile, MediaType.MULTIPART_FORM_DATA_TYPE)
        .field("catalogName", catalogName, MediaType.MULTIPART_FORM_DATA_TYPE)
        .field("overwrite", overwriteInRepos ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE)
        .field("xmlaEnabledFlag", "true", MediaType.MULTIPART_FORM_DATA_TYPE);

    // If the import service needs the file name do the following.
    part.getField("uploadAnalysis").setContentDisposition(
        FormDataContentDisposition.name("uploadAnalysis").fileName(catalogName).build());
    try {
      response = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(String.class, part);
    } catch (Exception ex) {
      ex.printStackTrace();
      //response = "-1";
    }
    return new Integer(response).intValue();
  }

  /**
   * Validate username and password on server
   * @param serverUserId
   * @param serverPassword
   * @return
   * @deprecated
   */
  private HttpClient getClient(String serverUserId, String serverPassword) {
    HttpClient client = new HttpClient();
    // If server userid/password was supplied, use basic authentication to
    // authenticate with the server.
    if (serverUserId.length() > 0 && serverPassword.length() > 0) {
      Credentials creds = new UsernamePasswordCredentials(serverUserId, serverPassword);
      client.getState().setCredentials(AuthScope.ANY, creds);
      client.getParams().setAuthenticationPreemptive(true);
    }
    return client;

  }

  private String getPasswordKey(String passWord) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
      md.reset();
      md.update(passWord.getBytes("UTF-8")); //$NON-NLS-1$
      byte[] digest = md.digest("P3ntah0Publ1shPa55w0rd".getBytes("UTF-8")); //$NON-NLS-1$//$NON-NLS-2$
      StringBuilder buf = new StringBuilder(digest.length + 1);
      String s;
      for (byte aDigest : digest) {
        s = Integer.toHexString(0xFF & aDigest);
        buf.append((s.length() == 1) ? "0" : "").append(s); //$NON-NLS-1$ //$NON-NLS-2$
      }
      return buf.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Publishes the specified model, schema, and connection to the current BI server
   * @param schemaName
   * @param jndiName
   * @param modelName
   * @param showFeedback
   * @throws Exception
   */
  public void publishToServer(String schemaName, String jndiName, String modelName, String repositoryPath,
      String selectedPath, boolean publishDatasource, boolean showFeedback, boolean isExistentDatasource,
      String fileName) throws Exception {

    // File files[] = { new File(fileName) };
    //publishFile(selectedPath, files, false);

    if (publishDatasource) {
      DatabaseMeta databaseMeta = ((ISpoonModelerSource) model.getModelSource()).getDatabaseMeta();
      publishDataSource(databaseMeta, isExistentDatasource);
    }
    boolean overwriteInRepository = false;
    publishOlapSchemaToServer(schemaName, jndiName, modelName, repositoryPath, overwriteInRepository, true);
  }

  public void publishPrptToServer(String theXmiPublishingPath, String thePrptPublishingPath, boolean publishDatasource,
      boolean isExistentDatasource, boolean publishXmi, String xmi, String prpt) throws Exception {

    File thePrpt[] = { new File(prpt) };
    int result = publishFile(thePrptPublishingPath, thePrpt, !publishXmi /*show feedback here if not publishing xmi*/);
    if (result != PublisherUtil.FILE_ADD_SUCCESSFUL) {
      return;
    }

    if (publishXmi) {
      File theXmi[] = { new File(xmi) };
      publishFile(theXmiPublishingPath, theXmi, true);
    }
    if (publishDatasource) {
      DatabaseMeta databaseMeta = ((ISpoonModelerSource) model.getModelSource()).getDatabaseMeta();
      publishDataSource(databaseMeta, isExistentDatasource);
    }
  }

  @Deprecated
  public boolean checkForExistingFile(String path, String name) {
    try {
      if (path == null || name == null) {
        return false;
      }
      List<String> folders = new ArrayList(Arrays.asList(path.split("" + ISolutionRepository.SEPARATOR)));
      int idx = 0;
      CmisObject folder = null;
      while (folders.size() > 0) {
        folder = findFolder(folders.get(idx), folder);
        if (folder == null) {
          return false;
        }
        folders.remove(idx);
      }
      List<CmisObject> files = getNavigationService().getDescendants(BiPlatformRepositoryClient.PLATFORMORIG,
          folder.findIdProperty(PropertiesBase.OBJECTID, null), new TypesOfFileableObjects(TypesOfFileableObjects.ANY),
          1, null, false, false);
      for (CmisObject f : files) {
        if (f.findStringProperty(CmisObject.NAME, null).equals(name)) {
          return true;
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  private CmisObject findFolder(String folder, CmisObject parent) throws Exception {
    List<CmisObject> solutions = getNavigationService().getDescendants(BiPlatformRepositoryClient.PLATFORMORIG,
        (parent != null) ? parent.findIdProperty(PropertiesBase.OBJECTID, null) : "",
        new TypesOfFileableObjects(TypesOfFileableObjects.FOLDERS), 1, null, false, false);
    for (CmisObject obj : solutions) {
      if (obj.findStringProperty(CmisObject.NAME, null).equals(folder)) {
        return obj;
      }
    }
    return null;
  }

  public boolean checkDataSource(boolean autoMode) throws KettleDatabaseException, ConnectionServiceException {
    // check the data source

    DatabaseMeta databaseMeta = ((ISpoonModelerSource) model.getModelSource()).getDatabaseMeta();
    int compare = compareDataSourceWithRemoteConnection(databaseMeta);

    String serverName = biServerConnection.getName();

    boolean nonJndi = (compare & ModelServerPublish.REMOTE_CONNECTION_MUST_BE_JNDI) > 0;
    boolean missing = (compare & ModelServerPublish.REMOTE_CONNECTION_MISSING) > 0;
    boolean different = (compare & ModelServerPublish.REMOTE_CONNECTION_DIFFERENT) > 0;
    //    boolean same = (compare | ModelServerPublish.REMOTE_CONNECTION_SAME) > 0;

    if (missing && !nonJndi) {
      if (!autoMode
          && !SpoonFactory
              .getInstance()
              .messageBox(
                  BaseMessages.getString(this.getClass(), "ModelServerPublish.Datasource.OkToPublish"), //$NON-NLS-1$
                  BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), true, Const.INFO)) { //$NON-NLS-1$
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Datasource.PublishCancelled"), //$NON-NLS-1$ 
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
        return false;
      }
      boolean ok = publishDataSource(databaseMeta, false);
      if (!autoMode && ok) {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Datasource.Added"), //$NON-NLS-1$
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.INFO); //$NON-NLS-1$
      }
      return ok;
    } else if (missing && nonJndi) {
      if (!autoMode) {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Datasource.NonJNDI"), //$NON-NLS-1$
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
      }
      return false;
    } else if (different && !nonJndi) {
      if (!autoMode
          && !SpoonFactory
              .getInstance()
              .messageBox(
                  BaseMessages.getString(this.getClass(), "ModelServerPublish.Datasource.IsDifferent"), //$NON-NLS-1$
                  BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), true, Const.INFO)) { //$NON-NLS-1$
        return false;
      }
      // replace the data source
      boolean ok = publishDataSource(databaseMeta, true);
      if (!autoMode && ok) {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Datasource.Updated"), //$NON-NLS-1$
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
      }
      return ok;
    } else if (different && nonJndi) {
      if (!autoMode) {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Datasource.CannotUpdate"), //$NON-NLS-1$
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
      }
      return false;
    }
    return false;

  }

  protected boolean showFeedback(int result) {
    String serverName = biServerConnection.getName();
    String fileName = this.model.getModelName();
    switch (result) {
      // String message, String rememberText, String rememberPropertyName )
      case ModelServerPublish.PUBLISH_CATALOG_EXISTS: {
        boolean ans = SpoonFactory.getInstance()
            .overwritePrompt(            
            BaseMessages.getString(this.getClass(), "Publish.Overwrite.Title"), 
            BaseMessages.getString(this.getClass(), "Publish.Overwrite.Message", fileName),
            BaseMessages.getString(this.getClass(),"ModelServerPublish.Publish.CatalogExists"));
        //.messageBox(
        //    BaseMessages.getString(this.getClass(), "ModelServerPublish.Publish.CatalogExists"), //$NON-NLS-1$
        //    BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
        return ans;
        // break;
      }
      case ModelServerPublish.PUBLISH_DATASOURCE_PROBLEM: {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Publish.DataSourceProblem"), //$NON-NLS-1$
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
        break;
      }
      case ModelServerPublish.DATASOURCE_DRIVER_MISSING: {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Publish.DriverMissing"), //$NON-NLS-1$
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
        break;
      }
      case ModelServerPublish.PUBLISH_FAILED: {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Publish.Failed"), //$NON-NLS-1$  
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
        break;
      }
      case ModelServerPublish.PUBLISH_FILE_EXISTS: {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Publish.FileExists"), //$NON-NLS-1$  
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
        break;
      }
      case ModelServerPublish.PUBLISH_INVALID_PASSWORD: {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Publish.BadPassword"), //$NON-NLS-1$  
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
        break;
      }
      case ModelServerPublish.PUBLISH_INVALID_USER_OR_PASSWORD: {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Errors.InvalidUser"), //$NON-NLS-1$  
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
        break;
      }
      case ModelServerPublish.PUBLISH_SUCCESS: {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Publish.Success"), //$NON-NLS-1$  
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.INFO); //$NON-NLS-1$
        break;
      }
      case ModelServerPublish.PUBLISH_UNKNOWN_PROBLEM: {
        SpoonFactory
            .getInstance()
            .messageBox(
                BaseMessages.getString(this.getClass(), "ModelServerPublish.Publish.UnknownProblem"), //$NON-NLS-1$  
                BaseMessages.getString(this.getClass(), "ModelServerPublish.MessageBox.Title", serverName), false, Const.ERROR); //$NON-NLS-1$
        break;
      }
    }
    return false;
  }

  private void publishOlapSchemaToServer(String schemaFilePath, String jndiName, String modelName,
      String repositoryPath, boolean overwriteInRepository, boolean showFeedback) throws Exception {

    File modelsDir = new File("models"); //$NON-NLS-1$
    if (!modelsDir.exists()) {
      modelsDir.mkdir();
    }
    File publishFile = new File(modelsDir, schemaFilePath);
    publishFile.createNewFile();

    LogicalModel lModel = this.model.getLogicalModel(ModelerPerspective.ANALYSIS);

    MondrianModelExporter exporter = new MondrianModelExporter(lModel, LocalizedString.DEFAULT_LOCALE);
    String mondrianSchema = exporter.createMondrianModelXML();

    org.dom4j.Document schemaDoc = DocumentHelper.parseText(mondrianSchema);
    byte schemaBytes[] = schemaDoc.asXML().getBytes();

    if (!publishFile.exists()) {
      throw new ModelerException("Schema file does not exist"); //$NON-NLS-1$
    }
    //local file
    OutputStream out = new FileOutputStream(publishFile);
    out.write(schemaBytes);
    out.flush();
    out.close();
    //file to send to Jcr Repository
    InputStream schema = new ByteArrayInputStream(schemaBytes);

    int result = publishMondrainSchema(schema, modelName, jndiName, overwriteInRepository);
    if (showFeedback) {
      if (showFeedback(result)) {
        result = publishMondrainSchema(schema, modelName, jndiName, true);
        showFeedback(result);
      }
    }

  }

  /**
   * Sets the current BI server connection
   * @param biServerConnection
   */
  public void setBiServerConnection(BiServerConnection biServerConnection) {
    this.biServerConnection = biServerConnection;
    if (this.client != null) {
      client.addFilter(new HTTPBasicAuthFilter(biServerConnection.getUserId(), biServerConnection.getPassword()));
    }
  }

  /**
   * Sets the metadata model
   * @param model
   */
  public void setModel(ModelerWorkspace model) {
    this.model = model;
  }

  public int getServerConnectionStatus() {
    return serviceClientStatus;
  }

  public BiPlatformRepositoryClientNavigationService getNavigationService() {
    if (navigationService == null) {

      BiPlatformRepositoryClient biReposClient = new BiPlatformRepositoryClient();

      biReposClient.setServerUri(biServerConnection.getUrl());
      biReposClient.setUserId(biServerConnection.getUserId());
      biReposClient.setPassword(biServerConnection.getPassword());

      try {
        biReposClient.connect();
      } catch (ServiceException e) {
        e.printStackTrace();
        return null;
      }
      navigationService = biReposClient.getNavigationService();
    }
    return navigationService;
  }

}
