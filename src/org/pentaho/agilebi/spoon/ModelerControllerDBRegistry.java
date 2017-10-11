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

package org.pentaho.agilebi.spoon;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.trans.HasDatabasesInterface;

public class ModelerControllerDBRegistry implements HasDatabasesInterface, UndoInterface {

  private List<DatabaseMeta> databases = null;

  public ModelerControllerDBRegistry() {
    this.databases = new ArrayList<DatabaseMeta>();
  }

  public void addDatabase(DatabaseMeta databaseMeta) {
    this.databases.add(databaseMeta);
  }

  public void addDatabase(int p, DatabaseMeta ci) {
    this.databases.add(p, ci);
  }

  public void addOrReplaceDatabase(DatabaseMeta databaseMeta) {
    this.databases.add(databaseMeta);
  }

  public DatabaseMeta findDatabase(String name) {
    DatabaseMeta database = null;
    for (DatabaseMeta currentDatabase : this.databases) {
      if (currentDatabase.getName().equals(name)) {
        database = currentDatabase;
        break;
      }
    }
    return database;
  }

  public DatabaseMeta getDatabase(int i) {
    return this.databases.get(i);
  }

  public List<DatabaseMeta> getDatabases() {
    return this.databases;
  }

  public boolean haveConnectionsChanged() {
    return false;
  }

  public int indexOfDatabase(DatabaseMeta ci) {
    return this.databases.indexOf(ci);
  }

  public int nrDatabases() {
    return this.databases.size();
  }

  public void removeDatabase(int i) {
    this.databases.remove(i);
  }

  public void setDatabases(List<DatabaseMeta> databases) {
    this.databases = databases;
  }

  // TODO provide implementation for the UNDO interface.

  public void addUndo(Object from[], Object to[], int pos[], Point prev[], Point curr[], int type_of_change,
      boolean nextAlso) {
  }

  public int getMaxUndo() {
    return 0;
  }

  public void setMaxUndo(int mu) {

  }

  public TransAction previousUndo() {
    return null;
  }

  public TransAction viewThisUndo() {
    return null;
  }

  public TransAction viewPreviousUndo() {
    return null;
  }

  public TransAction nextUndo() {
    return null;
  }

  public TransAction viewNextUndo() {
    return null;
  }
}
