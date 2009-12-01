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
