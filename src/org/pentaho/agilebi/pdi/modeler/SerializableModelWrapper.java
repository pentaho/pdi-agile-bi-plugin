package org.pentaho.agilebi.pdi.modeler;

import java.io.Serializable;

public class SerializableModelWrapper implements Serializable {

	private IModelerSource source;
	private String domain;

	public IModelerSource getSource() {
		return source;
	}

	public void setSource(IModelerSource source) {
		this.source = source;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
