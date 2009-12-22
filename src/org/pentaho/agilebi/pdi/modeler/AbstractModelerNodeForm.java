package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public abstract class AbstractModelerNodeForm<T> extends AbstractXulEventHandler implements ModelerNodePropertiesForm<T>{

  protected BindingFactory bf;
  protected XulDeck deck;
  protected XulVbox panel;
  private String id;
  
  public AbstractModelerNodeForm(String panelId){
    this.id = panelId;
  }
  
  public void activate(T obj) {
    deck.setSelectedIndex(deck.getChildNodes().indexOf(panel));
    this.setObject(obj);
  }


  public void init() {
    bf = new DefaultBindingFactory();
    bf.setDocument(document);
    deck = (XulDeck) document.getElementById("propertiesdeck");
    panel = (XulVbox) document.getElementById(id);
  }
  
}
