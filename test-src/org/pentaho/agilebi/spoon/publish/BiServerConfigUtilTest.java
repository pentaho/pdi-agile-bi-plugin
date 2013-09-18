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

package org.pentaho.agilebi.spoon.publish;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.platform.api.engine.PentahoAccessControlException;

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
