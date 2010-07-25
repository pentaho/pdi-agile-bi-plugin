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
package org.pentaho.agilebi.spoon.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.spoon.publish.ModelServerPublish;
import org.pentaho.agilebi.spoon.XulUI;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.platform.util.client.PublisherUtil;

import java.io.File;

/**
 * A dialog for administering a connections to a BI server
 * @author jamesdixon
 *
 */
public class BiServerConfigUtil {

  private static Log logger = LogFactory.getLog(BiServerConfigUtil.class);

  
  /**
   * Tests a connection to the provided BI server connection object
   * @param serverConnection
   */
  public static void testServerConnection( BiServerConnection biServerConnection ) {
    ModelServerPublish publish = new ModelServerPublish();
    publish.setBiServerConnection(biServerConnection);
    try {
      // try to get a list of database connections
//      publish.listRemoteConnections();
      // now try to publish to the system solution
      File file = new File("plugins/spoon/agile-bi/testfile.txt"); //$NON-NLS-1$
      
      String DEFAULT_PUBLISH_URL = biServerConnection.getUrl()+"/RepositoryFilePublisher"; //$NON-NLS-1$
      File files[] = { file }; 
      int result = PublisherUtil.publish(DEFAULT_PUBLISH_URL, "system/tmp", files, biServerConnection.getPublishPassword(), biServerConnection.getUserId(), biServerConnection.getPassword(), true, false); //$NON-NLS-1$

      if( result == PublisherUtil.FILE_ADD_SUCCESSFUL ) {
        SpoonFactory.getInstance().messageBox( BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Passed" ),  //$NON-NLS-1$
            BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Title" ), false, Const.INFO);  //$NON-NLS-1$
      }
      else if( result == PublisherUtil.FILE_ADD_INVALID_USER_CREDENTIALS ) {
        SpoonFactory.getInstance().messageBox( BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.BadCredential" ),  //$NON-NLS-1$
            BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
      }
      else if( result == PublisherUtil.FILE_ADD_INVALID_PUBLISH_PASSWORD ) {
        SpoonFactory.getInstance().messageBox( BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.BadPublishPassword" ),  //$NON-NLS-1$
            BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
      }
      else if( result == PublisherUtil.FILE_ADD_FAILED ) {
        SpoonFactory.getInstance().messageBox( BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.UnknownFail" ),  //$NON-NLS-1$
            BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
      }
      
    } catch (Exception e) {
      SpoonFactory.getInstance().messageBox( e.getLocalizedMessage(), BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
    }
  }
  
}
