package org.pentaho.agilebi.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.util.ThinModelConverter;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.api.data.IDatasourceService;

public class AgileBIDatasourceService implements IDatasourceService {

  public void clearCache() {
    // TODO impl cache
  }

  public void clearDataSource(String dsName) {
    // TODO impl cache
  }

  public String getDSBoundName(String dsName) throws DatasourceServiceException {
    return dsName;
  }

  public String getDSUnboundName(String dsName) {
    return dsName;
  }

  public DataSource getDataSource(String dsName) throws DatasourceServiceException {
    Domain domain = null;
    try {
      XmiParser parser = new XmiParser();
      FileInputStream fis = new FileInputStream(new File(dsName));
      domain = parser.parseXmi(fis);
    } catch (Exception e) {
      throw new DatasourceServiceException(e);
    }

    if (domain.getPhysicalModels().size() == 0 || 
        !(domain.getPhysicalModels().get(0) instanceof SqlPhysicalModel)) {
      throw new DatasourceServiceException("No SQL Physical Model Available");
      
    }
    
    SqlPhysicalModel model = (SqlPhysicalModel)domain.getPhysicalModels().get(0);
    
    DatabaseMeta databaseMeta = ThinModelConverter.convertToLegacy(model.getId(), model.getDatasource());
    return new DatabaseMetaDataSource(databaseMeta);
  }

  class DatabaseMetaDataSource implements DataSource {

    DatabaseMeta databaseMeta;
    
    public DatabaseMetaDataSource(DatabaseMeta databaseMeta) {
      this.databaseMeta = databaseMeta;
    }
    
    public Connection getConnection() throws SQLException {
      Database database = new Database(databaseMeta);
      try {
        database.connect();
      } catch (KettleException e) {
        e.printStackTrace();
        throw new SQLException(e.getMessage());
      }
      return database.getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
      return null;
    }

    public PrintWriter getLogWriter() throws SQLException {
      return null;
    }

    public int getLoginTimeout() throws SQLException {
      return 0;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    public void setLoginTimeout(int seconds) throws SQLException {
    }
   
    public boolean  isWrapperFor(Class<?> iface) {
      return false;
    }
    
    public <T> T unwrap(Class<T> iface) {
      return null;
    } 
    
  }
  
}
