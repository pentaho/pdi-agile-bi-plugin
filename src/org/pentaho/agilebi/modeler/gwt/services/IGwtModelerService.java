package org.pentaho.agilebi.modeler.gwt.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.metadata.model.Domain;

/**
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jun 18, 2010
 * Time: 4:10:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IGwtModelerService extends RemoteService {

  Domain generateDomain();
  BogoPojo gwtWorkaround ( BogoPojo pojo);
}
