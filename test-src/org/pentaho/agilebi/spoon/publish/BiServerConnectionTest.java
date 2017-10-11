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

import org.junit.Test;

import static org.junit.Assert.*;

public class BiServerConnectionTest {
  @Test
  public void testUrlAcceptsEmptyString() throws Exception {
    BiServerConnection connection = new BiServerConnection();
    connection.setUrl( "" );
    assertEquals( "", connection.getUrl() );
  }

  @Test
  public void testUrlNoSlashInKettleVar() throws Exception {
    BiServerConnection connection = new BiServerConnection();
    connection.setUrl( "${some.var}" );
    assertEquals( "${some.var}", connection.getUrl() );
    connection.setUrl( "%%win.var%%" );
    assertEquals( "%%win.var%%", connection.getUrl() );
  }

  @Test
  public void testUrlEnsuresTrailingSlash() throws Exception {
    BiServerConnection connection = new BiServerConnection();
    connection.setUrl( "http://some.server/webapp" );
    assertEquals( "http://some.server/webapp/", connection.getUrl() );
    connection.setUrl( "http://some.server/pho/" );
    assertEquals( "http://some.server/pho/", connection.getUrl() );
  }

}
