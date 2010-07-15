package org.pentaho.agilebi.modeler.services;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.XulServiceCallback;

/**
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jun 23, 2010
 * Time: 10:19:00 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IModelerServiceAsync {
  void generateDomain( XulServiceCallback<Domain> callback);
}
