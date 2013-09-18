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

package org.pentaho.agilebi.vfs;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.operations.FileOperations;
import org.apache.commons.vfs.provider.AbstractFileName;

public class MetadataToMondrianVfsFileObject implements FileObject {

  public class MetadataToMondrianVfsFileName extends AbstractFileName {

    public MetadataToMondrianVfsFileName(final String absPath, final FileType type) {
      super("mtm", absPath, type);//$NON-NLS-1$
    }

    @Override
    protected void appendRootUri(final StringBuffer arg0, final boolean arg1) {
    }

    @Override
    public FileName createName(final String absPath, final FileType fileType) {

      FileName name = new MetadataToMondrianVfsFileName(absPath, fileType);
      return name;
    }

  }
  
  private String fileRef;

  private FileContent content = null;

  private FileName name;

  private FileType type;

  public MetadataToMondrianVfsFileObject(final String fileRef) {
    this.fileRef = fileRef;
    // try to guess the file type
    type = FileType.FILE;
    name = new MetadataToMondrianVfsFileName(fileRef, type);

  }

  public MetadataToMondrianVfsFileObject(final String fileRef, final FileType type) {
    this.fileRef = fileRef;
    this.type = type;
    name = new MetadataToMondrianVfsFileName(fileRef, type);

  }

  public String getFileRef() {
    return fileRef;
  }

  public FileName getName() {
    return name;
  }

  public URL getURL() throws FileSystemException {
    URL url = null;
    try {
      url = new URL("mtm:/" + fileRef); //$NON-NLS-1$
    } catch (Exception e) {

    }
    return url;
  }

  public boolean exists() throws FileSystemException {
    return new File(fileRef).exists();
  }

  public boolean isHidden() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public boolean isReadable() throws FileSystemException {
    // not needed for our usage
    return exists();
  }

  public boolean isWriteable() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public FileType getType() throws FileSystemException {
    return type;
  }

  public FileObject getParent() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileSystem getFileSystem() {
    // not needed for our usage
    return null;
  }

  public FileObject[] getChildren() throws FileSystemException {
    return null;
  }

  public FileObject getChild(final String arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject resolveFile(final String arg0, final NameScope arg1) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject resolveFile(final String arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject[] findFiles(final FileSelector arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void findFiles(final FileSelector arg0, final boolean arg1, final List arg2) throws FileSystemException {
    // not needed for our usage
  }

  public boolean delete() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public int delete(final FileSelector arg0) throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  public void createFolder() throws FileSystemException {
    // not needed for our usage

  }

  public void createFile() throws FileSystemException {
    // not needed for our usage

  }

  public void copyFrom(final FileObject arg0, final FileSelector arg1) throws FileSystemException {
    // not needed for our usage

  }

  public void moveTo(final FileObject arg0) throws FileSystemException {
    // not needed for our usage
  }

  public boolean canRenameTo(final FileObject arg0) {
    // not needed for our usage
    return false;
  }

  public FileContent getContent() throws FileSystemException {
    content = new MetadataToMondrianVfsFileContent(this);
    return content;
  }

  public void close() throws FileSystemException {
    if (content != null) {
      content.close();
      content = null;
    }
  }

  public void refresh() throws FileSystemException {
    // not needed for our usage
  }

  public boolean isAttached() {
    // not needed for our usage
    return false;
  }

  public boolean isContentOpen() {
    return (content != null) && content.isOpen();
  }

  public FileOperations getFileOperations() throws FileSystemException {
    // not needed for our usage
    return null;
  }

}
