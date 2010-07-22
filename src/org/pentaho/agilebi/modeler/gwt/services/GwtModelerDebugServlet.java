package org.pentaho.agilebi.modeler.gwt.services;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.agilebi.spoon.ModelerSourceUtil;
import org.pentaho.agilebi.spoon.SpoonModelerWorkspaceHelper;
import org.pentaho.agilebi.spoon.TableModelerSource;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.HypersonicDatabaseMeta;
import org.pentaho.di.core.database.MySQLDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;

import java.util.ArrayList;
import java.util.List;

/**
 * User: nbaker
 * Date: Jun 18, 2010
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
  public Domain generateDomain(String connectionName, String tableName, String dbType, String query, String datasourceName) throws Exception{

    try{
      DatabaseMeta database = new DatabaseMeta();
      database.setDatabaseInterface(new MySQLDatabaseMeta());
      database.setDatabaseType("mysql");
      database.setUsername("root");
      database.setPassword("");
      database.setAccessType(DatabaseMeta.TYPE_ACCESS_NATIVE);
      database.setHostname("localhost");
      database.setDBName("hibernate");
      database.setDBPort("3306");

      TableModelerSource source = new TableModelerSource(database, tableName, null);
      ModelerWorkspace model = new ModelerWorkspace(new SpoonModelerWorkspaceHelper());
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

  public String serializeModels(Domain domain, String name) throws Exception{
    // Do nothing in debug mode.
    return null;
  }
}
