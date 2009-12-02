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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.client.ConnectionServiceClient;
import org.pentaho.platform.util.client.PublisherUtil;

public class ModelServerPublish {

  public static final int PUBLISH_UNKNOWN_PROBLEM = -1;
  public static final int PUBLISH_FILE_EXISTS = 1;
  public static final int PUBLISH_FAILED = 2;
  public static final int PUBLISH_SUCCESS = 3;
  public static final int PUBLISH_INVALID_PASSWORD = 4;
  public static final int PUBLISH_INVALID_USER_OR_PASSWORD = 5;
  public static final int PUBLISH_DATASOURCE_PROBLEM = 6;
  public static final int PUBLISH_CATALOG_EXISTS = 7;

  public static final int REMOTE_CONNECTION_MISSING = 1;
  public static final int REMOTE_CONNECTION_DIFFERENT = 2;
  public static final int REMOTE_CONNECTION_SAME = 4;
  public static final int REMOTE_CONNECTION_MUST_BE_JNDI = 8;
  
  private BiServerConnection biServerConnection;

  private IConnection remoteConnection;
  
  private ModelerWorkspace model;
  
  public ModelServerPublish() {
  }
  
  /**
   * Returns the remote connection. If the force flag is set the connection is 
   * always refreshed from the remote BI server. If the force flag is not set
   * a cached connection is returned.
   * @return
   */
  public IConnection getRemoteConnection( String connectionName, boolean force ) throws ConnectionServiceException {
    if( remoteConnection == null || force ) {
      // get information about the remote connection
      ConnectionServiceClient serviceClient = new ConnectionServiceClient();
      serviceClient.setHost(biServerConnection.getUrl());
      serviceClient.setUserId(biServerConnection.getUserId());
      serviceClient.setPassword(biServerConnection.getPassword());
      
      remoteConnection = serviceClient.getConnectionByName(connectionName);
    }
    return remoteConnection;
  }
  
  public int compareDataSourceWithRemoteConnection( DatabaseMeta databaseMeta ) throws ConnectionServiceException, KettleDatabaseException {
    
    int result = 0;
    if( databaseMeta.getAccessType() != DatabaseMeta.TYPE_ACCESS_NATIVE) {
      result += REMOTE_CONNECTION_MUST_BE_JNDI;
    }
    
    // compare the local database meta with the remote connection
    String connectionName = databaseMeta.getName();
    IConnection connection = getRemoteConnection( connectionName, false );
    if( connection == null ) {
      // the connection does not exist (with the same name) on the remote BI server 
      result += REMOTE_CONNECTION_MISSING;
      return result;
    }
    // see if the driver, url, and user are the same for both connections...
    String url = databaseMeta.getURL();
    String userName = databaseMeta.getUsername();
    String driverClass = databaseMeta.getDriverClass();
    boolean urlMatch = url.equals( connection.getUrl() );
    boolean userMatch = (userName == null && connection.getUsername() == null )||
      userName.equals( connection.getUsername() );
    boolean driverMatch = (driverClass == null && connection.getDriverClass() == null )||
        driverClass.equals( connection.getDriverClass() );
    // return 'same' or 'different'
    if( urlMatch && userMatch && driverMatch) {
      result += REMOTE_CONNECTION_SAME;
    } else {
      result += REMOTE_CONNECTION_DIFFERENT;
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
  private boolean publishDataSource( DatabaseMeta databaseMeta, boolean update ) throws KettleDatabaseException, ConnectionServiceException {
   
    // Create a connection service client and give it the connection for the current BI server
    ConnectionServiceClient serviceClient = new ConnectionServiceClient();
    serviceClient.setHost(biServerConnection.getUrl());
    serviceClient.setUserId(biServerConnection.getUserId());
    serviceClient.setPassword(biServerConnection.getPassword());

    // create a new connection object and populate it from the databaseMeta
    Connection connection = new Connection();
    connection.setDriverClass(databaseMeta.getDriverClass());
    connection.setName(databaseMeta.getName());
    connection.setPassword(databaseMeta.getPassword());
    connection.setUrl(databaseMeta.getURL());
    connection.setUsername(databaseMeta.getUsername());

    // call updateConnection or addConnection
    if( update ) {
      return serviceClient.updateConnection(connection);
    } else {
      return serviceClient.addConnection(connection);
    }
    
  }
  
  private int publish(
      String publishPath,
      File publishFile, 
      String jndiName,
      String modelId,
      boolean enableXmla) throws Exception, UnsupportedEncodingException
{

    String fullURL = biServerConnection.getUrl() + "MondrianCatalogPublisher?publishPath=" + URLEncoder.encode(publishPath, "UTF-8");// NON-NLS //$NON-NLS-1$ //$NON-NLS-2$
  fullURL += "&publishKey=" + getPasswordKey(new String(biServerConnection.getPublishPassword())); //$NON-NLS-1$
  fullURL += "&overwrite=true"; //$NON-NLS-1$
  fullURL += "&jndiName=" + jndiName; //$NON-NLS-1$
  fullURL += "&enableXmla=" + enableXmla; //$NON-NLS-1$
  fullURL += "&userid="+biServerConnection.getUserId()+"&password="+biServerConnection.getPassword(); //$NON-NLS-1$ //$NON-NLS-2$
  
  PostMethod filePost = new PostMethod(fullURL);
  ArrayList<Part> parts = new ArrayList<Part>();
  try {
      parts.add(new FilePart(publishFile.getName(), publishFile));
  } catch (FileNotFoundException e) {
      // file is not existing or not readable, this should not happen
      e.printStackTrace();
  }
  filePost.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), filePost.getParams()));
  HttpClient client = getClient(biServerConnection.getUserId(), biServerConnection.getPassword());
  int status;
  try {
      status = client.executeMethod(filePost);
  } catch (IOException e) {
      throw new Exception(e.getMessage(), e);
  }
  if (status != HttpStatus.SC_OK) {
      if (status == HttpStatus.SC_MOVED_TEMPORARILY) {
          throw new Exception("Invalid Username or Password");
      } else {
          throw new Exception("Unknown server error: HTTP status code " + status);
      }
  } else {
      try {
          String postResult = filePost.getResponseBodyAsString();
          int publishResult = Integer.parseInt(postResult.trim());
          if( publishResult == ModelServerPublish.PUBLISH_SUCCESS ) {
            // launch URLs to refresh the server-side caches
            refreshOlapCaches( modelId, client );
          }
          return publishResult;
      } catch (IOException e) {
          throw new Exception(e);
      }
  }
}
  
  private HttpClient getClient( String serverUserId, String serverPassword) {
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
  
  public void refreshOlapCaches(       
      String modelId ) throws IOException {
    
    HttpClient client = getClient( biServerConnection.getUserId(), biServerConnection.getPassword() );
    refreshOlapCaches( modelId, client );
  }
  
  public void refreshOlapCaches( String modelId, HttpClient client ) throws IOException {
    String urlModelId = URLEncoder.encode( modelId );
    GetMethod get = new GetMethod( biServerConnection.getUrl()+"ViewAction?solution=admin&path=&action=clear_mondrian_schema_cache.xaction&userid="+biServerConnection.getUserId()+"&password="+biServerConnection.getPassword() ); //$NON-NLS-1$ //$NON-NLS-2$
    client.executeMethod( get );
    // assume we are ok
    get = new GetMethod( biServerConnection.getUrl()+"content/analyzer/ajax/clearCache?catalog="+urlModelId+"%20Model&userid="+biServerConnection.getUserId()+"&password="+biServerConnection.getPassword() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    client.executeMethod( get );
  }
  
  public String getPasswordKey(String passWord) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
        md.reset();
        md.update(passWord.getBytes("UTF-8")); //$NON-NLS-1$
        byte[] digest = md.digest("P3ntah0Publ1shPa55w0rd".getBytes("UTF-8"));  //$NON-NLS-1$//$NON-NLS-2$
        StringBuilder buf = new StringBuilder(digest.length + 1);
        String s;
        for (byte aDigest : digest)
        {
            s = Integer.toHexString(0xFF & aDigest);
            buf.append((s.length() == 1) ? "0" : "").append(s); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return buf.toString();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    return null;
}

  public void publishToServer( String schemaName, String jndiName, String modelName, boolean showFeedback ) throws Exception {

    checkDataSource();
    publishOlapSchemaToServer( schemaName, jndiName , modelName, showFeedback  );
    publishMetadataModel( modelName );
  }
  
  private int publishMetadataModel( String modelName ) {
    String DEFAULT_PUBLISH_URL = biServerConnection.getUrl()+"RepositoryFilePublisher"; //$NON-NLS-1$
    File files[] = {new File("models/" + modelName + ".xmi")}; //$NON-NLS-1$ //$NON-NLS-2$
    int result = PublisherUtil.publish(DEFAULT_PUBLISH_URL, "models/resources/metadata", files, biServerConnection.getPublishPassword(), biServerConnection.getUserId(), biServerConnection.getPassword(), true); //$NON-NLS-1$
    return result;
  }
  
  private void checkDataSource() throws KettleDatabaseException, ConnectionServiceException {
    // check the data source
    
    DatabaseMeta databaseMeta = model.getModelSource().getDatabaseMeta();
    int compare = compareDataSourceWithRemoteConnection(databaseMeta);

    String serverName = biServerConnection.getName();
    
    boolean nonJndi = (compare & ModelServerPublish.REMOTE_CONNECTION_MUST_BE_JNDI) > 0;
    boolean missing = (compare & ModelServerPublish.REMOTE_CONNECTION_MISSING) > 0;
    boolean different = (compare & ModelServerPublish.REMOTE_CONNECTION_DIFFERENT) > 0;
//    boolean same = (compare | ModelServerPublish.REMOTE_CONNECTION_SAME) > 0;
    
    if(missing && !nonJndi) {
      if( !SpoonFactory.getInstance().messageBox( "Datasource does not exist on server, ok to publish it?", "Publish To Server: "+serverName, true, Const.INFO) ) {
        SpoonFactory.getInstance().messageBox( "Publish cancelled", "Publish To Server: "+serverName, false, Const.ERROR);
        return;
      }
      boolean ok = publishDataSource(databaseMeta, false);
      if( ok ) {
        SpoonFactory.getInstance().messageBox( "Datasource added", "Publish To Server: "+serverName, false, Const.ERROR);
      }
    }
    else if(missing && nonJndi) {
      SpoonFactory.getInstance().messageBox( "The datasource does not exist on the server, but only JNDI datasources can be published", "Publish To Server: "+serverName, false, Const.ERROR);
      return;
    }
    else if( different && !nonJndi ) {
      if( !SpoonFactory.getInstance().messageBox( "Datasource exists on server, but is different. Ok to update it?", "Publish To Server: "+serverName, true, Const.INFO) ) {
        // replace the data source
        boolean ok = publishDataSource(databaseMeta, true);
        if( ok ) {
          SpoonFactory.getInstance().messageBox( "Datasource updated", "Publish To Server: "+serverName, false, Const.ERROR);
        }
      }
    }
    else if(different && nonJndi) {
      SpoonFactory.getInstance().messageBox( "Datasource exists on server, but is different. Cannot update it.", "Publish To Server: "+serverName, false, Const.ERROR);
      return;
    }
    
  }
  
  private void publishOlapSchemaToServer( String schemaFilePath, String jndiName, String modelName, boolean showFeedback  ) throws Exception {
    
    String publishPath = "models"; //$NON-NLS-1$
    File publishFile = new File( "models/"+schemaFilePath ); //$NON-NLS-1$
    boolean enableXmla = false;
    
    int result = publish(publishPath, publishFile, jndiName, modelName, enableXmla);
    if( showFeedback ) {
    switch (result) {
      case ModelServerPublish.PUBLISH_CATALOG_EXISTS: {
        SpoonFactory.getInstance().messageBox( "Catalog exists already", "Publish To Server", false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_DATASOURCE_PROBLEM: {
        SpoonFactory.getInstance().messageBox( "Datasource problem", "Publish To Server", false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_FAILED: {
        SpoonFactory.getInstance().messageBox( "Publish failed", "Publish To Server", false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_FILE_EXISTS: {
        SpoonFactory.getInstance().messageBox( "File exists already", "Publish To Server", false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_INVALID_PASSWORD: {
        SpoonFactory.getInstance().messageBox( "Invalid pssword", "Publish To Server", false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_INVALID_USER_OR_PASSWORD: {
        SpoonFactory.getInstance().messageBox( "Invalid user id or password", "Publish To Server", false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_SUCCESS: {
        SpoonFactory.getInstance().messageBox( "Publish was successful", "Publish To Server", false, Const.ERROR);
        break;
      }
      case ModelServerPublish.PUBLISH_UNKNOWN_PROBLEM: {
        SpoonFactory.getInstance().messageBox( "Unknown problem encountered while publishing", "Publish To Server", false, Const.ERROR);
        break;
      }
    }
    }
  }

  public void setBiServerConnection(BiServerConnection biServerConnection) {
    this.biServerConnection = biServerConnection;
  }

  public void setModel(ModelerWorkspace model) {
    this.model = model;
  }

  
}
