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

import java.io.File;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.i18n.BaseMessages;

/**
 * A dialog for administering a connections to a BI server
 * @author jamesdixon
 *
 */
public class BiServerConfigUtil implements IBiServerConfigUtil {
  
  protected ModelServerPublish createModelServerPublish() {
    return new ModelServerPublish();
  }
  
  protected SpoonInterface getSpoonFactory() {
    return SpoonFactory.getInstance();
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.agilebi.spoon.publish.IBiServerConfigUtil#testServerConnection(org.pentaho.agilebi.spoon.publish.BiServerConnection)
   */
  @Override
  public void testServerConnection( BiServerConnection biServerConnection ) {
    ModelServerPublish publish = createModelServerPublish();
    publish.setBiServerConnection(biServerConnection);
    try {
      File file = new File("plugins/spoon/agile-bi/testfile.nonConvertableFileExtension"); //$NON-NLS-1$
      
      File files[] = { file };
      int result = publish.publishFile("/etc/system/tmp", files, false);

      if( result == ModelServerPublish.PUBLISH_SUCCESS || result == ModelServerPublish.PUBLISH_FILE_EXISTS ) {
        getSpoonFactory().messageBox( BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Passed" ),  //$NON-NLS-1$
            BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Title" ), false, Const.INFO);  //$NON-NLS-1$
      }
      else if( result == ModelServerPublish.PUBLISH_INVALID_USER_OR_PASSWORD ) {
        getSpoonFactory().messageBox( BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.BadCredential" ),  //$NON-NLS-1$
            BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
      }
      else if( result == ModelServerPublish.PUBLISH_INVALID_PASSWORD ) {
        getSpoonFactory().messageBox( BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.BadPublishPassword" ),  //$NON-NLS-1$
            BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
      }
      else if( result == ModelServerPublish.PUBLISH_FAILED ) {
        getSpoonFactory().messageBox( BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.UnknownFail" ),  //$NON-NLS-1$
            BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
      }
      
    } catch (Exception e) {
      getSpoonFactory().messageBox( e.getLocalizedMessage(), BaseMessages.getString(ModelerWorkspace.class, "XulDialogBiServerConfig.Test.Failed" ), false, Const.ERROR);  //$NON-NLS-1$
    }
  }
  
}
