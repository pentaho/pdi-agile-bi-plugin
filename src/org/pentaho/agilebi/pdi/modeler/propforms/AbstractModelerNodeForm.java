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
 * Copyright (c) 2010 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.agilebi.pdi.modeler.propforms;

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
    this.setObject(obj);
    deck.setSelectedIndex(deck.getChildNodes().indexOf(panel));
  }


  public void init() {
    bf = new DefaultBindingFactory();
    bf.setDocument(document);
    deck = (XulDeck) document.getElementById("propertiesdeck");
    panel = (XulVbox) document.getElementById(id);
  }
  
}
