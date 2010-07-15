package org.pentaho.agilebi.modeler.services;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.XulServiceCallback;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jun 18, 2010
 * Time: 4:20:31 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IModelerService {
  Domain generateDomain();
}
