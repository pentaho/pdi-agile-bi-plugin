package org.pentaho.agilebi.spoon;

import org.pentaho.agilebi.modeler.IModelerMessages;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jul 23, 2010
 * Time: 1:09:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpoonModelerMessages implements IModelerMessages {

  public String getString(String key, String... args) {
    return BaseMessages.getString(ModelerWorkspace.class, key, args);
  }
}
