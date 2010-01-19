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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.di.i18n.BaseMessages;

public class Messages
{
  public static final String packageName = Messages.class.getPackage().getName();

  public static String getString(String key)
  {
    return BaseMessages.getString(packageName, key);
  }

  public static String getString(String key, String param1)
  {
    return BaseMessages.getString(packageName, key, param1);
  }

  public static String getString(String key, String param1, String param2)
  {
    return BaseMessages.getString(packageName, key, param1, param2);
  }

  public static String getString(String key, String param1, String param2, String param3)
  {
    return BaseMessages.getString(packageName, key, param1, param2, param3);
  }

  public static String getString(String key, String param1, String param2, String param3, String param4)
  {
    return BaseMessages.getString(packageName, key, param1, param2, param3, param4);
  }

  public static String getString(String key, String param1, String param2, String param3, String param4, String param5)
  {
    return BaseMessages.getString(packageName, key, param1, param2, param3, param4, param5);
  }

  public static String getString(String key, String param1, String param2, String param3, String param4, String param5, String param6)
  {
    return BaseMessages.getString(packageName, key, param1, param2, param3, param4, param5, param6);
  }
}