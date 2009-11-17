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

import java.util.Collection;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.FileProvider;

/**
 * this VFS provider converts a metadata model on the fly to a 
 * mondrian file, so we don't need to store tmp mondrian files
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 *
 */
public class MetadataToMondrianVfs implements FileProvider {

  public MetadataToMondrianVfs() {
    super();
  }

  public FileObject findFile(final FileObject baseFile, final String uri, final FileSystemOptions arg2)
      throws FileSystemException {

    // for now assume that all URIs are absolute and we don't handle compound URIs
    if (uri != null) {
      // this is a fully qualified file path
      int pos = uri.indexOf(':');
      String filePath = uri.substring(pos + 1);
      MetadataToMondrianVfsFileObject fileInfo = new MetadataToMondrianVfsFileObject(filePath);
      return fileInfo;
    }
    return null;
  }

  public FileObject createFileSystem(final String arg0, final FileObject arg1, final FileSystemOptions arg2)
      throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileSystemConfigBuilder getConfigBuilder() {
    // not needed for our usage
    return null;
  }

  public Collection getCapabilities() {
    // not needed for our usage
    return null;
  }

  public FileName parseUri(final FileName arg0, final String arg1) throws FileSystemException {
    // not needed for our usage
    return null;
  }

}
