package org.pentaho.agilebi.pdi.perspective;

import org.pentaho.agilebi.pdi.modeler.ModelerEngineMeta;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class AgileBiPerspectiveController extends AbstractXulEventHandler {

  private XulMenuitem modelPropItem;
  BindingFactory bf = new DefaultBindingFactory();
  
  @Override
  public String getName() {
    return "agileBiPerspective";
  }

  @Override
  public void setXulDomContainer(XulDomContainer xulDomContainer) {
    super.setXulDomContainer(xulDomContainer);
    modelPropItem = (XulMenuitem) document.getElementById("view-modeler-props");
    bf.setDocument(document);
    
  }
  
  public void setPropVisible(boolean vis){
    modelPropItem.setSelected(vis);
  }

  private Binding itemBinding;
  private ModelerEngineMeta meta;
  public void setSelectedModelerMeta(ModelerEngineMeta meta){
    this.meta = meta;
    if(itemBinding != null){
      itemBinding.destroyBindings();
    }
    if(meta != null){
      bf.setBindingType(Binding.Type.ONE_WAY);
      itemBinding = bf.createBinding(meta.getController(), "propVisible", this, "propVisible");
      try {
        itemBinding.fireSourceChanged();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
  }

  public void toggleProperties(){
    meta.getController().togglePropertiesPanel();
  }
  
  
}
