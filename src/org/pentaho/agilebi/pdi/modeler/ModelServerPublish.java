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
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
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

  private String biServerId;
  
  private String serverURL;
  private String serverUserId;
  private String serverPassword;   
  private String publishPassword;
  private String serverName;

  public ModelServerPublish() {
    this( "biserver1" );
  }
  
  public ModelServerPublish( String biServerId ) {
    setBiServerId(biServerId);
  }
  
  public  int publish(
      String publishPath,
      File publishFile, 
      String jndiName,
      String modelId,
      boolean enableXmla) throws Exception, UnsupportedEncodingException
{

    String fullURL = serverURL + "MondrianCatalogPublisher?publishPath=" + URLEncoder.encode(publishPath, "UTF-8");// NON-NLS
  fullURL += "&publishKey=" + getPasswordKey(new String(publishPassword)); //$NON-NLS-1$
  fullURL += "&overwrite=true"; //$NON-NLS-1$
  fullURL += "&jndiName=" + jndiName;
  fullURL += "&enableXmla=" + enableXmla;
  fullURL += "&userid="+serverUserId+"&password="+serverPassword;
  
  PostMethod filePost = new PostMethod(fullURL);
  ArrayList<Part> parts = new ArrayList<Part>();
  try {
      parts.add(new FilePart(publishFile.getName(), publishFile));
  } catch (FileNotFoundException e) {
      // file is not existing or not readable, this should not happen
      e.printStackTrace();
  }
  filePost.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), filePost.getParams()));
  HttpClient client = getClient(serverUserId, serverPassword);
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
    
    HttpClient client = getClient( serverUserId, serverPassword );
    refreshOlapCaches( modelId, client );
  }
  
  public void refreshOlapCaches( String modelId, HttpClient client ) throws IOException {
    String urlModelId = URLEncoder.encode( modelId );
    GetMethod get = new GetMethod( serverURL+"ViewAction?solution=admin&path=&action=clear_mondrian_schema_cache.xaction&userid="+serverUserId+"&password="+serverPassword );
    client.executeMethod( get );
    // assume we are ok
    get = new GetMethod( serverURL+"content/analyzer/ajax/clearCache?catalog="+urlModelId+"%20Model&userid="+serverUserId+"&password="+serverPassword );
    client.executeMethod( get );
  }
  
  public String getPasswordKey(String passWord) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");// NON-NLS
        md.reset();
        md.update(passWord.getBytes("UTF-8"));// NON-NLS
        byte[] digest = md.digest("P3ntah0Publ1shPa55w0rd".getBytes("UTF-8"));// NON-NLS
        StringBuilder buf = new StringBuilder(digest.length + 1);
        String s;
        for (byte aDigest : digest)
        {
            s = Integer.toHexString(0xFF & aDigest);
            buf.append((s.length() == 1) ? "0" : "").append(s);
        }
        return buf.toString();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    return null;
}

  public void publishToServer( String schemaName, String jndiName, String modelName, boolean showFeedback ) throws Exception {
    publishOlapSchemaToServer( schemaName, jndiName , modelName, showFeedback  );
    String DEFAULT_PUBLISH_URL = serverURL+"RepositoryFilePublisher"; //$NON-NLS-1$
    File files[] = {new File("models/" + modelName + "/metadata.xmi")};
    int result = PublisherUtil.publish(DEFAULT_PUBLISH_URL, "models", files, publishPassword, serverUserId, serverPassword, true);
  }
  
  public void publishOlapSchemaToServer( String schemaFilePath, String jndiName, String modelName, boolean showFeedback  ) throws Exception {
    
    String publishPath = "models";
    File publishFile = new File( "models/"+schemaFilePath );
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

  public String getBiServerId() {
    return biServerId;
  }

  public void setBiServerId(String biServerId) {
    this.biServerId = biServerId;
    Spoon spoon = ((Spoon)SpoonFactory.getInstance());
    PropsUI props = spoon.getProperties();
    
    serverURL = props.getProperty(biServerId+"/url");
    serverUserId = props.getProperty(biServerId+"/user");
    serverPassword = props.getProperty(biServerId+"/password");   
    publishPassword = props.getProperty(biServerId+"/publishpassword");
    serverName = props.getProperty(biServerId+"/name");

    if( serverURL == null ) {
      serverURL = "http://localhost:8080/pentaho/";
      serverUserId = "joe";
      serverPassword = "password";
      publishPassword = "spike";
      serverName = "Localhost";
    }
  }

  public String getServerName() {
    return serverName;
  }
  
}
