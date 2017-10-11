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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.agilebi.spoon.publish;

import org.apache.commons.lang.StringUtils;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class BiServerConnectionForm extends XulEventSourceAdapter{

  BiServerConnection conn;
  String name, url, userId, password;
  
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
    this.setUrl(conn.getUrl());
    this.setUserId(conn.getUserId());
  }
  
  public void reset(){
    setValues();
  }

  public BiServerConnection saveConn() {
    conn.setName(this.name);
    conn.setPassword(this.password);
     conn.setUserId(this.userId);
    conn.setUrl(this.url);
    return conn;
  }
  
  public BiServerConnection getConnectionCopy() {
    BiServerConnection conn = new BiServerConnection();
    conn.setName(this.name);
    conn.setPassword(this.password);
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
  
  private boolean valid;
  private boolean calculateValidity(){
    this.valid = StringUtils.isNotEmpty(this.url)     
      && StringUtils.isNotEmpty(this.name);
    
    firePropertyChange("valid", null, valid);
    return this.valid;
  }
  
  public boolean isValid(){
    return this.valid;
  }
  
}
