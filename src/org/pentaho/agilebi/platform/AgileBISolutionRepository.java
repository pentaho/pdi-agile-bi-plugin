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
package org.pentaho.agilebi.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IFileFilter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.solution.SolutionRepositoryBase;

public class AgileBISolutionRepository extends SolutionRepositoryBase {

  private static final long serialVersionUID = -1174699308144524442L;
  
  public int publish(String baseUrl, String path, String fileName, byte[] data, boolean overwrite) throws PentahoAccessControlException {
    try {
      path = URLDecoder.decode(path, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    try {
      fileName = URLDecoder.decode(fileName, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    
    // Analyzer puts a "/" at the beginning of the path on windows, remove it.
    if (path.startsWith("/") && path.contains(":")) { //$NON-NLS-1$ //$NON-NLS-2$
      path = path.substring(1);
    }
    
    File f = new File(path, fileName);
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(f);
      fos.write(data);
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
      return ISolutionRepository.FILE_ADD_FAILED;
    }
    return ISolutionRepository.FILE_ADD_SUCCESSFUL;
  }
  
  private File getFile(String solutionPath) {
    
    if (solutionPath.startsWith("system/")) {
      String filePath = PentahoSystem.getApplicationContext().getSolutionPath(solutionPath);
      File file = new File(filePath);
      return file;
    } else {
      File file = new File(solutionPath);
      return file;
    }
  }
  
  public boolean resourceExists(String solutionPath, int actionOperation) {
    return getFile(solutionPath).exists();
  }

  public InputStream getResourceInputStream(String solutionPath, boolean getLocalizedResource, int actionOperation)
  throws FileNotFoundException {
    return new FileInputStream(getFile(solutionPath));
  }

  static class AgileBISolutionFile implements ISolutionFile {
    File file = null;
    public AgileBISolutionFile(File file) {
      this.file = file;
    }
    
    public boolean exists() {
      return file.exists();
    }

    public byte[] getData() {
      try {
        return IOUtils.toByteArray(new FileInputStream(file));
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    public String getExtension() {
      throw new UnsupportedOperationException();
    }

    public String getFileName() {
      throw new UnsupportedOperationException();
    }

    public String getFullPath() {
      throw new UnsupportedOperationException();
    }

    public long getLastModified() {
      throw new UnsupportedOperationException();
    }

    public String getSolution() {
      throw new UnsupportedOperationException();
    }

    public String getSolutionPath() {
      throw new UnsupportedOperationException();
    }

    public boolean isDirectory() {
      throw new UnsupportedOperationException();
    }

    public boolean isRoot() {
      throw new UnsupportedOperationException();
    }

    public ISolutionFile[] listFiles() {
      throw new UnsupportedOperationException();
    }

    public ISolutionFile[] listFiles(IFileFilter filter) {
      throw new UnsupportedOperationException();
    }

    public ISolutionFile retrieveParent() {
      throw new UnsupportedOperationException();
    }
    
  }
  
  
  protected ISolutionFile getLocalizedFile(final ISolutionFile resourceFile, final int actionOperation) {
    return resourceFile;
  }
  
  protected ISolutionFile getFileByPath(final String path, final int actionOperation) {
    File file = getFile(path);
    if(file.exists()) {
      return new AgileBISolutionFile(file);
    }
    return null;
  }
  
  public void addPermission(ISolutionFile file, IPermissionRecipient recipient, IPermissionMask permission) {
    if (true) throw new UnsupportedOperationException();
    
  }

  public IActionSequence getActionSequence(String solutionName, String actionPath, String actionName, int loggingLevel,
      int actionOperation) {
    throw new UnsupportedOperationException();
  }

  public String[] getAllActionSequences(int actionOperation) {
    throw new UnsupportedOperationException();
  }

  public ClassLoader getClassLoader(String path) {
    throw new UnsupportedOperationException();
  }

  public Map<IPermissionRecipient, IPermissionMask> getEffectivePermissions(ISolutionFile file) {
    throw new UnsupportedOperationException();
  }

  public Map<IPermissionRecipient, IPermissionMask> getPermissions(ISolutionFile file) {
    throw new UnsupportedOperationException();
  }

  public String getRepositoryName() {
    throw new UnsupportedOperationException();
  }

  public long getSolutionFileLastModified(String path, int actionOperation) {
    throw new UnsupportedOperationException();
  }

  public Document getSolutionStructure(int actionOperation) {
    throw new UnsupportedOperationException();
  }

  public Document getSolutionTree(int actionOperation) {
    throw new UnsupportedOperationException();
  }

  public Document getSolutionTree(int actionOperation, ISolutionFilter filter) {
    throw new UnsupportedOperationException();
  }

  public Document getSolutions(String solutionName, String pathName, int actionOperation, boolean visibleOnly) {
    throw new UnsupportedOperationException();
  }

  public boolean hasAccess(ISolutionFile file, int actionOperation) {
    throw new UnsupportedOperationException();
  }

  public int publish(String baseUrl, String path, String fileName, File f, boolean overwrite)
      throws PentahoAccessControlException {
    throw new UnsupportedOperationException();
  }

  public void reloadSolutionRepository(IPentahoSession session, int loggingLevel) {
    throw new UnsupportedOperationException();
  }

  public long resourceSize(String solutionPath, int actionOperation) {
    throw new UnsupportedOperationException();
  }

  public void setPermissions(ISolutionFile file, Map<IPermissionRecipient, IPermissionMask> acl)
      throws PentahoAccessControlException {
    throw new UnsupportedOperationException();
  }

  public void share(ISolutionFile file, List<IPermissionRecipient> shareRecipients) {
    throw new UnsupportedOperationException();
  }

  public boolean solutionSynchronizationSupported() {
    throw new UnsupportedOperationException();
  }

  public boolean supportsAccessControls() {
    throw new UnsupportedOperationException();
  }

  public boolean synchronizeSolutionWithSolutionSource(IPentahoSession session) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }  
}
