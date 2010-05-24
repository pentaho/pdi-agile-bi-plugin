package org.pentaho.agilebi.pdi.publish;

import org.apache.commons.lang.StringUtils;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class BiServerConnectionForm extends XulEventSourceAdapter{

  BiServerConnection conn;
  String name, url, userId, password, publishPassword;
  
  public void setConnection(BiServerConnection conn){
    this.conn = conn;
    setValues();
  }
  
  private void setValues(){
    if(conn == null){
      return;
    }
    this.setName(conn.getName());
    this.setPassword(conn.getPassword());
    this.setPublishPassword(conn.getPublishPassword());
    this.setUrl(conn.getUrl());
    this.setUserId(conn.getUserId());
  }
  
  public void reset(){
    setValues();
  }

  public BiServerConnection saveConn() {
    conn.setName(this.name);
    conn.setPassword(this.password);
    conn.setPublishPassword(this.publishPassword);
    conn.setUserId(this.userId);
    conn.setUrl(this.url);
    return conn;
  }
  
  public BiServerConnection getConnectionCopy() {
    BiServerConnection conn = new BiServerConnection();
    conn.setName(this.name);
    conn.setPassword(this.password);
    conn.setPublishPassword(this.publishPassword);
    conn.setUserId(this.userId);
    conn.setUrl(this.url);
    return conn;
  }

  public void setConn(BiServerConnection conn) {
    this.conn = conn;
    setValues();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    String prevVal = this.name;
    this.name = name;
    firePropertyChange("name", prevVal, this.name);
    calculateValidity();
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    String prevVal = this.url;
    this.url = url;
    firePropertyChange("url", prevVal, this.url);
    calculateValidity();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    String prevVal = this.userId;
    this.userId = userId;
    firePropertyChange("userId", prevVal, this.userId);
    calculateValidity();
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    String prevVal = this.password;
    this.password = password;
    firePropertyChange("password", prevVal, this.password);
    calculateValidity();
  }

  public String getPublishPassword() {
    return publishPassword;
  }

  public void setPublishPassword(String publishPassword) {
    String prevVal = this.publishPassword;
    this.publishPassword = publishPassword;
    firePropertyChange("publishPassword", prevVal, this.publishPassword);
    calculateValidity();
  }
  
  private boolean valid;
  private boolean calculateValidity(){
    this.valid = StringUtils.isNotEmpty(this.url)
      && StringUtils.isNotEmpty(this.publishPassword)
      && StringUtils.isNotEmpty(this.name);
    
    firePropertyChange("valid", null, valid);
    return this.valid;
  }
  
  public boolean isValid(){
    return this.valid;
  }
  
  
  
  
}
