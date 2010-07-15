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
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jun 18, 2010
 * Time: 4:21:54 PM
 * To change this template use File | Settings | File Templates.
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

  public void generateDomain( final XulServiceCallback<Domain> callback){
    getDelegate().generateDomain(new AsyncCallback<Domain>() {
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
}
