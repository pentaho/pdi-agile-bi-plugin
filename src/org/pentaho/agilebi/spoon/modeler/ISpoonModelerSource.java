package org.pentaho.agilebi.spoon.modeler;

import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.di.core.database.DatabaseMeta;

/**
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jun 15, 2010
 * Time: 2:26:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ISpoonModelerSource extends IModelerSource {
  DatabaseMeta getDatabaseMeta();
}
