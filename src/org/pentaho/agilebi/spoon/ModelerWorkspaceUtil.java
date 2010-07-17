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
package org.pentaho.agilebi.spoon;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;
import org.pentaho.agilebi.modeler.*;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.spoon.ModelerSourceFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metadata.model.*;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.olap.*;
import org.pentaho.metadata.util.Util;
import org.pentaho.metadata.util.XmiParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 
 * Utility class for generating ModelerModels for the User Interface.
 * 
 * @author nbaker
 *
 */
public class ModelerWorkspaceUtil {

  private static Log logger = LogFactory.getLog(ModelerWorkspaceUtil.class);

  
  public static ModelerWorkspace populateModelFromSource( ModelerWorkspace model, IModelerSource source) throws
                                                                                                         ModelerException {
    Domain d = source.generateDomain();
    
    model.setModelSource(source);
    model.setModelName(source.getTableName());
    model.setDomain(d);
    
    return model;  
  }
  
  private static String MODELER_NAME = "OutputStepModeler"; //$NON-NLS-1$

  
  public static void saveWorkspace(ModelerWorkspace aModel, String fileName) throws ModelerException {
  	try {
  	  
	    String xmi = getMetadataXML(aModel);
	  
	    // write the XMI to a tmp file
	    // models was created earlier.
	    try{

	      File file = new File(fileName);
	      OutputStream out = new FileOutputStream(file);
	      out.write(xmi.getBytes("UTF-8"));
	      out.flush();
	      out.close();
	      
	    } catch(IOException e){
	      logger.info(BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.BadGenerateMetadata"),e); //$NON-NLS-1$
	      throw new ModelerException(BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.Populate.BadGenerateMetadata"),e); //$NON-NLS-1$
	    }

  	} catch (Exception e) {
  		logger.error("error", e);
  		throw new ModelerException(e);
  	}
  }
  
  public static String getMetadataXML(ModelerWorkspace aModel) throws ModelerException {
    aModel.getWorkspaceHelper().populateDomain(aModel);
    XmiParser parser = new XmiParser();
    return parser.generateXmi(aModel.getDomain());
  
  }
  
  public static void loadWorkspace(String fileName, String aXml, ModelerWorkspace aModel) throws ModelerException {

    try{
      XmiParser parser = new XmiParser();
      Domain domain = parser.parseXmi(new ByteArrayInputStream(aXml.getBytes("UTF-8")));

      LogicalModel logical = domain.getLogicalModels().get(0);
      
      Object agileBiProp = logical.getProperty("AGILE_BI_GENERATED_SCHEMA");
      if(agileBiProp == null || "FALSE".equals(agileBiProp)){
        throw new IncompatibleModelerException();
      }
        	
      // re-hydrate the source
      Object property = logical.getProperty("source_type"); //$NON-NLS-1$
      if( property != null ) {
        IModelerSource theSource = ModelerSourceFactory.generateSource(property.toString());
        theSource.initialize(domain);   
        aModel.setModelSource(theSource);
      }
  
    	aModel.setDomain(domain);
    	aModel.setFileName(fileName);
    	aModel.resolveConnectionFromDomain();
    	aModel.refresh();
        aModel.getWorkspaceHelper().populateDomain(aModel);
    	aModel.setDirty(false);
    } catch (Exception e){
      logger.error("error", e);
      if(e instanceof ModelerException){
        throw (ModelerException) e;
      }
      throw new ModelerException(BaseMessages.getString(ModelerWorkspaceUtil.class, "ModelerWorkspaceUtil.LoadWorkspace.Failed"),e); //$NON-NLS-1$
    }
  }  
}
