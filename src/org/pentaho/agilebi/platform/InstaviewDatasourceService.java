/*
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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.pentaho.agilebi.spoon.perspective.AgileBiInstaPerspective;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.util.ThinModelConverter;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.api.data.IDatasourceService;

public class InstaviewDatasourceService implements IDatasourceService {

	private GenericObjectPool gPool = new GenericObjectPool();

	private KeyedObjectPoolFactory kopf =new GenericKeyedObjectPoolFactory(null, 8);

	private Set<String> knownPools = new HashSet<String>();
	
	private int connectionCount = 0;
	
	private Map<String,DatabaseMeta> databaseMetaMap = new HashMap<String,DatabaseMeta>();
	
	public InstaviewDatasourceService() {
		
	}
	
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

	  DatabaseMeta databaseMeta = databaseMetaMap.get(dsName);
	  
	  if( knownPools.contains(dsName)) {
		  try {
				DatabaseMetaDataSource dataSource = new DatabaseMetaDataSource(dsName);
				return dataSource;
		} catch (Exception e) {
			throw new DatasourceServiceException(e);
		}
	  }

    // nothing in the pool so create a new pool
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
    
    databaseMeta = ThinModelConverter.convertToLegacy(model.getId(), model.getDatasource());
	
    ConnectionFactory cf = new DatabaseMetaConnectionFactory(databaseMeta);

    GenericObjectPool.Config config = new GenericObjectPool.Config();
    config.testOnBorrow = true;
    gPool.setConfig(config);

	  PoolableConnectionFactory pcf =  new PoolableConnectionFactory(cf,
            gPool,
            kopf,
            "select 1",
            false,
            true);
	
	try {
//		for(int i = 0; i < 5; i++) {
//		      gPool.addObject();
//		    }

		    // PoolingDataSource pds = new PoolingDataSource(gPool);
		    PoolingDriver pd = new PoolingDriver();
		    pd.registerPool(dsName, gPool);

		    for(int i = 0; i < 5; i++) {
		      gPool.addObject();
		    }
		
	} catch (Exception e) {
		throw new DatasourceServiceException(e);
	}
    
    Database database = new Database(databaseMeta);
    try {
      database.connect();
    } catch (Exception e) {
      throw new DatasourceServiceException(e);
    }
    Connection connection = database.getConnection();
	
	knownPools.add(dsName);
	databaseMetaMap.put(dsName, databaseMeta);
	
    return new DatabaseMetaDataSource(dsName);
  }

  class DatabaseMetaConnectionFactory implements ConnectionFactory {

	    DatabaseMeta databaseMeta;

		public DatabaseMetaConnectionFactory(DatabaseMeta databaseMeta) {
			this.databaseMeta = databaseMeta;
		}
		    

		@Override
		public Connection createConnection() throws SQLException {
		      Database database = new Database(databaseMeta);
		      try {
		        database.connect();
		        connectionCount++;
    			IInstaviewHelper instaviewHelper = InstaviewHelper.getInstaviewHelper();
    			if( instaviewHelper != null ) {
    				instaviewHelper.connectionMade();
    			}
  		      Connection connection = database.getConnection();

		      return connection;
		        
		      } catch (KettleException e) {
		        e.printStackTrace();
		        throw new SQLException(e.getMessage());
		      }
//		      WrappedConnection wrapper = new WrappedConnection(connection);
		}
	  
  }  
  
  class DatabaseMetaDataSource implements DataSource {

	    DatabaseMeta databaseMeta;
	    
	    Connection connection;
	    
	    String dsName;
	    
	    public DatabaseMetaDataSource(String dsName) {
	      this.dsName = dsName;
	    }
	    
	    public Connection getConnection() throws SQLException {
			Connection connection = java.sql.DriverManager.getConnection("jdbc:apache:commons:dbcp:"+dsName); //$NON-NLS-1$
/*
			try {
	        connectionCount++;
	        if( connectionCount == BOUNCE_THREASHOLD ) {
	    		try {
	    			IInstaviewHelper instaviewHelper = InstaviewHelper.getInstaviewHelper();
	    			if( instaviewHelper != null ) {
	    				instaviewHelper.restartDatastore();
			        	connectionCount = 0;
	    			}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	      } catch (Exception e) {
	        e.printStackTrace();
	        throw new SQLException(e.getMessage());
	      }
	      */
	      return connection;
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
