/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * Copyright 2007-2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.agilebi.vfs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Map;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileContentInfo;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.util.RandomAccessMode;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metadata.util.XmiParser;

public class MetadataToMondrianVfsFileContent implements FileContent {

  private MetadataToMondrianVfsFileObject fileObject;

  private InputStream inputStream = null;

  private boolean isOpen = false;

  public MetadataToMondrianVfsFileContent(final MetadataToMondrianVfsFileObject fileObject) {
    super();
    this.fileObject = fileObject;
  }

  public FileObject getFile() {
    return fileObject;
  }

  public long getSize() throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  public long getLastModifiedTime() throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  public void setLastModifiedTime(final long arg0) throws FileSystemException {
    // not needed for our usage

  }

  public boolean hasAttribute(final String attrName) {
    return false;
  }

  public void removeAttribute(final String attrName) {
  }

  public Map getAttributes() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public String[] getAttributeNames() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public Object getAttribute(final String arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void setAttribute(final String arg0, final Object arg1) throws FileSystemException {
    // not needed for our usage

  }

  public Certificate[] getCertificates() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public InputStream getInputStream() throws FileSystemException {

    try {
      // read in stream, generate mondrian model, write out stream.
      XmiParser parser = new XmiParser();
      FileInputStream fis = new FileInputStream(new File(fileObject.getFileRef()));
      Domain domain = parser.parseXmi(fis);
      String locale = "en_US";
      if (domain.getLocales().size() > 0) {
        locale = domain.getLocales().get(0).getCode();
      }
      
      if (domain.getLogicalModels().size() == 0) {
        throw new Exception("Domain " + fileObject.getFileRef() + " does not contain model.");
      }

      MondrianModelExporter exporter = new MondrianModelExporter(domain.getLogicalModels().get(0), locale);
      String mondrianSchema = exporter.createMondrianModelXML();
      inputStream = new ByteArrayInputStream(mondrianSchema.getBytes());
    } catch (Exception e) {
      throw new FileSystemException(e.getLocalizedMessage(), e);
    }
    isOpen = true;
    return inputStream;
  }

  public OutputStream getOutputStream() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public RandomAccessContent getRandomAccessContent(final RandomAccessMode arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public OutputStream getOutputStream(final boolean arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void close() throws FileSystemException {

    if (!isOpen) {
      return;
    }
    if (inputStream != null) {
      try {
        inputStream.close();
      } catch (Exception e) {
        // not much we can do here
      }
    }
    isOpen = false;
    fileObject.close();
  }

  public FileContentInfo getContentInfo() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public boolean isOpen() {
    // not needed for our usage
    return isOpen;
  }

}
