package org.pentaho.agilebi.modeler.services;

import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.XulServiceCallback;

/**
 * User: nbaker
 * Date: Jun 23, 2010
 */
public interface IModelerServiceAsync {
  void generateDomain(String tableName, String query, String datasourceName, XulServiceCallback<Domain> callback);
  void serializeModels(Domain domain, String name, XulServiceCallback<Void> callback);
}
