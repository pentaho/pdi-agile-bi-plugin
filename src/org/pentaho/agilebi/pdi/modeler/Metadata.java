package org.pentaho.agilebi.pdi.modeler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.model.concept.security.Security;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.Alignment;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.FieldType;
import org.pentaho.metadata.model.olap.OlapCube;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.model.olap.OlapDimensionUsage;
import org.pentaho.metadata.model.olap.OlapHierarchy;
import org.pentaho.metadata.model.olap.OlapHierarchyLevel;
import org.pentaho.metadata.model.olap.OlapMeasure;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.pms.schema.concept.DefaultPropertyID;

public class Metadata {

	public void setUserAccess( String user, int rights, IConcept concept ) {
	  SecurityOwner owner = new SecurityOwner(SecurityOwner.OwnerType.USER, user );
	  Security security = (Security)concept.getProperty(DefaultPropertyID.SECURITY.getId());
	  if( security == null ) {
	    security = new Security();
	    concept.setProperty(DefaultPropertyID.SECURITY.getId(), security);
	  }
	  security.putOwnerRights(owner, rights);
	}
	
}
