package org.pentaho.agilebi.pdi.modeler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ModelerSourceFactory {
  
  private static Map<String, Class<? extends IModelerSource>> outputSources = new HashMap<String, Class<? extends IModelerSource>>();
  static{
    outputSources.put(OutputStepModelerSource.SOURCE_TYPE, OutputStepModelerSource.class);
  }
  
  private static Log logger = LogFactory.getLog(ModelerSourceFactory.class);
  
  public static IModelerSource generateSource(String type){
    Class<? extends IModelerSource> clz = outputSources.get(type);
    if(clz == null){
      throw new IllegalArgumentException("Cannot find IModelerSoruce for type: "+type);
    }
    IModelerSource instance = null;
    try {
      instance = clz.newInstance();
    } catch (InstantiationException e) {
      logger.error("Error generating modeler source",e);
    } catch (IllegalAccessException e) {
      logger.error("Error generating modeler source",e);
    }
    return instance;
  }
}
