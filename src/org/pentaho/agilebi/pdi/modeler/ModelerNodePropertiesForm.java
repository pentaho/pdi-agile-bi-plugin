package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.XulEventHandler;

public interface ModelerNodePropertiesForm<T> extends XulEventHandler{
  void init();
  void activate(T selection);
  void setObject(T t);
}
