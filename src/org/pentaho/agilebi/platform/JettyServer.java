package org.pentaho.agilebi.platform;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;
import org.pentaho.di.core.logging.LogWriter;

public class JettyServer {

  private static Class<?> PKG = JettyServer.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private static LogWriter log = LogWriter.getInstance();

  public static final int PORT = 80;

  private Server server;

  private String hostname;

  private int port;

  public JettyServer(String hostname, int port) throws Exception {
    this.hostname = hostname;
    this.port = port;
  }

  public Server getServer() {
    return server;
  }

  public void startServer() throws Exception {
    server = new Server();

    WebAppContext pentahoContext = new WebAppContext();
    pentahoContext.setContextPath("/pentaho"); //$NON-NLS-1$
    pentahoContext.setWar("agile-bi/platform/webapps/pentaho"); //$NON-NLS-1$
    
    pentahoContext.setParentLoaderPriority(true);
    
    HandlerCollection handlers= new HandlerCollection();
    handlers.setHandlers(new Handler[]{pentahoContext, new DefaultHandler()});

    server.setHandler(handlers);

    // Start execution
    createListeners();

    server.start();
  }

  protected void setupListeners() {

  }

  public void stopServer() {
    try {
      if (server != null) {
        server.stop();
      }
    } catch (Exception e) {
      log.logError(toString(), "WebServer.Error.FailedToStop.Title");
    }
  }

  private void createListeners() {
    SocketConnector connector = new SocketConnector();
    connector.setPort(port);
    connector.setHost(hostname);
    connector.setName(hostname);
    log.logBasic(toString(), "WebServer.Log.CreateListener" + hostname + ":" + port);

    server.setConnectors(new Connector[] { connector });
  }

  /**
   * @return the hostname
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * @param hostname the hostname to set
   */
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

}
