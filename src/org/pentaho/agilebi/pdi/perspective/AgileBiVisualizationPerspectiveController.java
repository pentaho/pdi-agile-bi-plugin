package org.pentaho.agilebi.pdi.perspective;

import org.pentaho.agilebi.pdi.HasXulController;
import org.pentaho.agilebi.pdi.modeler.ModelerEngineMeta;
import org.pentaho.agilebi.pdi.visualizations.PropertyPanelController;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class AgileBiVisualizationPerspectiveController extends AbstractXulEventHandler {

  private XulMenuitem modelPropItem;
  private BindingFactory bf = new DefaultBindingFactory();
  private Binding itemBinding;
  private HasXulController meta;
  
  @Override
  public String getName() {
    return "agileBiVisPerspective";
  }

  @Override
  public void setXulDomContainer(XulDomContainer xulDomContainer) {
    super.setXulDomContainer(xulDomContainer);
    modelPropItem = (XulMenuitem) document.getElementById("view-vis-props");
    bf.setDocument(document);
    
  }
  
  public void setPropVisible(boolean vis){
    modelPropItem.setSelected(vis);
  }

  public void setSelectedModelerMeta(EngineMetaInterface meta){
    this.meta = (HasXulController) meta;
    if(itemBinding != null){
      itemBinding.destroyBindings();
    }
    if(meta != null){
      bf.setBindingType(Binding.Type.ONE_WAY);
      itemBinding = bf.createBinding(this.meta.getController(), "propVisible", this, "propVisible");
      try {
        itemBinding.fireSourceChanged();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
  }

  public void toggleProperties(){
    if(meta == null){
      return;
    }
    ((PropertyPanelController) meta.getController()).togglePropertiesPanel();
  }
  
  
}
