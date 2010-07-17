package org.pentaho.agilebi.modeler.services.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.services.IGwtModelerService;
import org.pentaho.agilebi.modeler.gwt.services.IGwtModelerServiceAsync;
import org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.XulServiceCallback;

import java.util.List;

/**
 * User: nbaker
 * Date: Jun 18, 2010
 */
public class GwtModelerServiceImpl implements IModelerServiceAsync {
  IGwtModelerServiceAsync delegate;


  private IGwtModelerServiceAsync getDelegate(){
    if(delegate == null){
      delegate = (IGwtModelerServiceAsync) GWT.create(IGwtModelerService.class);
      ServiceDefTarget endpoint = (ServiceDefTarget) delegate;
      endpoint.setServiceEntryPoint(getBaseUrl());
    }
    return delegate;
  }

  public void generateDomain(String tableName, String query, String datasourceName, final XulServiceCallback<Domain> callback){
    getDelegate().generateDomain(tableName, query, datasourceName, new AsyncCallback<Domain>() {
      public void onFailure( Throwable throwable ) {
        callback.error("Error getting Workspace", throwable);
      }

      public void onSuccess( Domain domain) {
        callback.success(domain);
      }
    });
  }

  private static String getBaseUrl() {
    String moduleUrl = GWT.getModuleBaseURL();

    if (moduleUrl.indexOf("content") > -1) {//$NON-NLS-1$
      String baseUrl = moduleUrl.substring(0, moduleUrl.indexOf("content"));//$NON-NLS-1$
      return baseUrl + "gwtrpc/modelerService";//$NON-NLS-1$
    }
    return moduleUrl + "modelerService";//$NON-NLS-1$
  }

  public void serializeModels( Domain domain, String name, final XulServiceCallback<Void> callback ) {
    getDelegate().serializeModels(domain, name, new AsyncCallback<Void>() {
      public void onFailure( Throwable throwable ) {
        callback.error("Error saving models", throwable);
      }

      public void onSuccess( Void v) {
        callback.success(v);
      }
    });
  }
}
