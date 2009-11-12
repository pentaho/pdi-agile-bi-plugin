package org.pentaho.agilebi.pdi.modeler;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.pms.core.exception.PentahoMetadataException;

/**
 * Provides a ModelerModel with information needed to generate the UI and well as the final
 * Mondrian and Metadata models. All information from the context in which the modeling is 
 * initiated should be contained within implementors of this interface.
 *
 * @author nbaker
 */
public interface IModelerSource {
  public Domain generateDomain() throws PentahoMetadataException;
  public String getDatabaseName();
}
