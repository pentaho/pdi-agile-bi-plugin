package org.pentaho.agilebi.spoon.publish;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.platform.api.engine.PentahoAccessControlException;

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

public class BiServerConfigUtilTest {
  
  private SpoonInterface mockSpoonFactory;
  
  private ModelServerPublish mockModelServerPublish;
  
  private BiServerConnection mockBiServerConnection;
  
  private BiServerConfigUtil biServerConfigUtil;
  
  private class BiServerConfigUtilTestClass extends BiServerConfigUtil {
    @Override
    protected SpoonInterface getSpoonFactory() {
      return mockSpoonFactory;
    }
    
    @Override
    protected ModelServerPublish createModelServerPublish() {
      return mockModelServerPublish;
    }
  }
  
  @Before
  public void setup() {
    mockSpoonFactory = mock(SpoonInterface.class);
    mockModelServerPublish = mock(ModelServerPublish.class);
    biServerConfigUtil = new BiServerConfigUtilTestClass();
  }
  
  @Test
  public void testTestPublishHasNonConvertableExtension() throws PentahoAccessControlException {
    biServerConfigUtil.testServerConnection(mockBiServerConnection);
    ArgumentCaptor<File[]> ac = ArgumentCaptor.forClass(File[].class);
    verify(mockModelServerPublish).publishFile(anyString(), ac.capture(), eq(false));
    File[] argArray = ac.getValue();
    assertEquals(1, argArray.length);
    assertEquals("testfile.nonConvertableFileExtension", argArray[0].getName());
  }
  
  @Test
  public void testTestShowsSuccessOnSuccess() throws PentahoAccessControlException {
    when(mockModelServerPublish.publishFile(anyString(), any(File[].class), eq(false))).thenReturn(ModelServerPublish.PUBLISH_SUCCESS);
    biServerConfigUtil.testServerConnection(mockBiServerConnection);
    verify(mockSpoonFactory).messageBox(BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Passed" ),  //$NON-NLS-1$
            BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Title" ), false, Const.INFO);
  }
  
  @Test
  public void testTestShowsSuccessOnFileExists() throws PentahoAccessControlException {
    when(mockModelServerPublish.publishFile(anyString(), any(File[].class), eq(false))).thenReturn(ModelServerPublish.PUBLISH_FILE_EXISTS);
    biServerConfigUtil.testServerConnection(mockBiServerConnection);
    verify(mockSpoonFactory).messageBox(BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Passed" ),  //$NON-NLS-1$
            BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Title" ), false, Const.INFO);
  }
  
  @Test
  public void testTestShowsFailedOnBadUsernameOrPassword() throws PentahoAccessControlException {
    when(mockModelServerPublish.publishFile(anyString(), any(File[].class), eq(false))).thenReturn(ModelServerPublish.PUBLISH_INVALID_USER_OR_PASSWORD);
    biServerConfigUtil.testServerConnection(mockBiServerConnection);
    verify(mockSpoonFactory).messageBox(BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.BadCredential" ),  //$NON-NLS-1$
        BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);
  }
  
  @Test
  public void testTestShowsFailedOnBadPassword() throws PentahoAccessControlException {
    when(mockModelServerPublish.publishFile(anyString(), any(File[].class), eq(false))).thenReturn(ModelServerPublish.PUBLISH_INVALID_PASSWORD);
    biServerConfigUtil.testServerConnection(mockBiServerConnection);
    verify(mockSpoonFactory).messageBox(BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.BadPublishPassword" ),  //$NON-NLS-1$
        BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);
  }
  
  @Test
  public void testTestShowsFailedOnPublishFailed() throws PentahoAccessControlException {
    when(mockModelServerPublish.publishFile(anyString(), any(File[].class), eq(false))).thenReturn(ModelServerPublish.PUBLISH_FAILED);
    biServerConfigUtil.testServerConnection(mockBiServerConnection);
    verify(mockSpoonFactory).messageBox(BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.UnknownFail" ),  //$NON-NLS-1$
        BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);
  }
  
  @Test
  public void testTestShowsFailedOnException() throws PentahoAccessControlException {
    PentahoAccessControlException e = new PentahoAccessControlException("test message");
    when(mockModelServerPublish.publishFile(anyString(), any(File[].class), eq(false))).thenThrow(e);
    biServerConfigUtil.testServerConnection(mockBiServerConnection);
    verify(mockSpoonFactory).messageBox(e.getLocalizedMessage(), BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);
  }
}
