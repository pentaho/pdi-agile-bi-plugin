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

/**
 * A bean defining a BI server connectpion
 * @author jamesdixon
 *
 */
public class BiServerConnection {

  private String url;
  
  private String userId;
  
  private String password;
  
  private String publishPassword;
  
  private String name;

  /**
   * Gets the URL for the BI server
   * In the form protocol:server:port/context, e.g. http://localhost:8080/pentaho
   * @return
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the URL for the BI server
   * In the form protocol:server:port/context, e.g. http://localhost:8080/pentaho
   * @param url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Gets the user id for the BI server connection
   * @return
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the user id for the BI server
   * @param userId
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Gets the password for the BI server connection
   * @return
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password for the BI server
   * @param password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Gets the publish password for the BI server connection
   * @return
   */
  public String getPublishPassword() {
    return publishPassword;
  }

  /**
   * Sets the publish password for the BI server
   * @param publishPassword
   */
  public void setPublishPassword(String publishPassword) {
    this.publishPassword = publishPassword;
  }

  /**
   * Gets the name for the BI server connection
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name for the BI server
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }
  
}
