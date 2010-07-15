package org.pentaho.agilebi.modeler.gwt.services;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.spoon.ModelerSourceUtil;
import org.pentaho.agilebi.spoon.TableModelerSource;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.HypersonicDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jun 18, 2010
 * Time: 4:16:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class GwtModelerDebugServlet extends RemoteServiceServlet implements IGwtModelerService {

  static{
    try {
      KettleEnvironment.init();
      Props.init(Props.TYPE_PROPERTIES_EMPTY);
    } catch (KettleException e) {
      e.printStackTrace();
    }
  }
  public Domain generateDomain() {

    try{
      DatabaseMeta database = new DatabaseMeta();
      database.setDatabaseInterface(new HypersonicDatabaseMeta());
      database.setDatabaseType("hypersonic");
      database.setUsername("sa");
      database.setPassword("");
      database.setAccessType(DatabaseMeta.TYPE_ACCESS_NATIVE);
      database.setHostname("localhost");
      database.setDBName("sampledata");
      database.setDBPort("9001");

      TableModelerSource source = new TableModelerSource(database, "ORDERS", null);
      ModelerWorkspace model = new ModelerWorkspace();
      Domain d = null;
      try {
        d = source.generateDomain();
      } catch (ModelerException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }

      return d;
    } catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }


  public BogoPojo gwtWorkaround ( BogoPojo pojo){
    return new BogoPojo();
  }
}
