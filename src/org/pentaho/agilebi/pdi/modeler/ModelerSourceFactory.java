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
package org.pentaho.agilebi.pdi.modeler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ModelerSourceFactory {
  
  private static Map<String, Class<? extends IModelerSource>> outputSources = new HashMap<String, Class<? extends IModelerSource>>();
  static{
    outputSources.put(OutputStepModelerSource.SOURCE_TYPE, OutputStepModelerSource.class);
    outputSources.put(TableModelerSource.SOURCE_TYPE, TableModelerSource.class);
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
