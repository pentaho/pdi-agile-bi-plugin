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
 * Copyright (c) 2012 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.spoon;

import java.lang.reflect.Method;

import org.pentaho.di.core.database.DatabaseMeta;

/**
 * A utility class to hold methods implemented by a step that can be used to connect to a database table
 * @author jamesdixon
 *
 */
public class DatabaseConnectionMethods {

	private Method databaseMetaMethod;
	
	private Method tableNameMethod;

	private Method schemaNameMethod;

	private DatabaseMeta databaseMeta;
	
	private String tableName;
	
	private String schemaName;
	
	
	public DatabaseMeta getDatabaseMeta() {
		return databaseMeta;
	}

	public void setDatabaseMeta(DatabaseMeta databaseMeta) {
		this.databaseMeta = databaseMeta;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public Method getDatabaseMetaMethod() {
		return databaseMetaMethod;
	}

	public void setDatabaseMetaMethod(Method databaseMetaMethod) {
		this.databaseMetaMethod = databaseMetaMethod;
	}

	public Method getTableNameMethod() {
		return tableNameMethod;
	}

	public void setTableNameMethod(Method tableNameMethod) {
		this.tableNameMethod = tableNameMethod;
	}

	public Method getSchemaNameMethod() {
		return schemaNameMethod;
	}

	public void setSchemaNameMethod(Method schemaNameMethod) {
		this.schemaNameMethod = schemaNameMethod;
	}


}
