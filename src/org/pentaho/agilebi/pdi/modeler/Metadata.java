package org.pentaho.agilebi.pdi.modeler;

import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.model.concept.security.Security;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
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
