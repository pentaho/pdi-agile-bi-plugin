package org.pentaho.agilebi.platform;

import org.pentaho.platform.api.repository2.unified.*;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.VersionHelper;

import java.io.File;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * User: nbaker
 * Date: 11/30/12
 */
public class AgileBISugarSolutionRepository implements IUnifiedRepository {

  public boolean canUnlockFile(Serializable serializable) {
    return false;
  }

  public RepositoryFile getFile(String solutionPath) {
//    if (solutionPath.startsWith("system/")) {
//      String filePath = PentahoSystem.getApplicationContext().getSolutionPath(solutionPath);
//      File file = new File(filePath);
//      return file;
//    } else {
//      File file = new File(solutionPath);
//      return file;
//    }
    return null;
  }

  public RepositoryFileTree getTree(String s, int i, String s1, boolean b) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFile getFileAtVersion(Serializable serializable, Serializable serializable1) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFile getFileById(Serializable serializable) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFile getFile(String s, boolean b) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFile getFileById(Serializable serializable, boolean b) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public <T extends IRepositoryFileData> T getDataForRead(Serializable serializable, Class<T> tClass) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForRead(Serializable serializable, Serializable serializable1, Class<T> tClass) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public <T extends IRepositoryFileData> T getDataForExecute(Serializable serializable, Class<T> tClass) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForExecute(Serializable serializable, Serializable serializable1, Class<T> tClass) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public <T extends IRepositoryFileData> List<T> getDataForReadInBatch(List<RepositoryFile> repositoryFiles, Class<T> tClass) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch(List<RepositoryFile> repositoryFiles, Class<T> tClass) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFile createFile(Serializable serializable, RepositoryFile repositoryFile, IRepositoryFileData iRepositoryFileData, String s) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFile createFile(Serializable serializable, RepositoryFile repositoryFile, IRepositoryFileData iRepositoryFileData, RepositoryFileAcl repositoryFileAcl, String s) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFile createFolder(Serializable serializable, RepositoryFile repositoryFile, String s) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFile createFolder(Serializable serializable, RepositoryFile repositoryFile, RepositoryFileAcl repositoryFileAcl, String s) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<RepositoryFile> getChildren(Serializable serializable) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<RepositoryFile> getChildren(Serializable serializable, String s) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFile updateFile(RepositoryFile repositoryFile, IRepositoryFileData iRepositoryFileData, String s) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void deleteFile(Serializable serializable, boolean b, String s) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void deleteFile(Serializable serializable, String s) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void moveFile(Serializable serializable, String s, String s1) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void copyFile(Serializable serializable, String s, String s1) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void undeleteFile(Serializable serializable, String s) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<RepositoryFile> getDeletedFiles(String s) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<RepositoryFile> getDeletedFiles(String s, String s1) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<RepositoryFile> getDeletedFiles() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void lockFile(Serializable serializable, String s) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void unlockFile(Serializable serializable) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFileAcl getAcl(Serializable serializable) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public RepositoryFileAcl updateAcl(RepositoryFileAcl repositoryFileAcl) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean hasAccess(String s, EnumSet<RepositoryFilePermission> repositoryFilePermissions) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<RepositoryFileAce> getEffectiveAces(Serializable serializable) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<RepositoryFileAce> getEffectiveAces(Serializable serializable, boolean b) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public VersionSummary getVersionSummary(Serializable serializable, Serializable serializable1) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<VersionSummary> getVersionSummaryInBatch(List<RepositoryFile> repositoryFiles) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<VersionSummary> getVersionSummaries(Serializable serializable) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void deleteFileAtVersion(Serializable serializable, Serializable serializable1) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void restoreFileAtVersion(Serializable serializable, Serializable serializable1, String s) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<RepositoryFile> getReferrers(Serializable serializable) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void setFileMetadata(Serializable serializable, Map<String, Serializable> stringSerializableMap) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public Map<String, Serializable> getFileMetadata(Serializable serializable) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<Character> getReservedChars() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getProductID() {
    return VersionHelper.getVersionInfo(this.getClass()).getProductID();
  }
}
