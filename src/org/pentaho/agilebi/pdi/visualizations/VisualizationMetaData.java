package org.pentaho.agilebi.pdi.visualizations;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pentaho.agilebi.pdi.visualizations.web.WebVisualization;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VisualizationMetaData {

	private String implementation;
	private String title;
	private String url;

	public static final String TITLE = "title";
	public static final String PROPERTY_TAG = "property";
	public static final String NAME_ATTRIBUTE = "name";
	public static final String IMPLEMENTATION = "implementation";
	public static final String URL = "url";
	
	public VisualizationMetaData(File aFile) {
		loadMetaData(aFile);
	}

	private void loadMetaData(File aFile) {

		try {
			DocumentBuilderFactory theFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder theBuilder = theFactory.newDocumentBuilder();
			Document theDocument = theBuilder.parse(aFile);
			NodeList theProperties = theDocument.getElementsByTagName(PROPERTY_TAG);

			for (int i = 0; i < theProperties.getLength(); i++) {
				Node theNode = theProperties.item(i);
				NamedNodeMap theAttributes = theNode.getAttributes();
				for (int j = 0; j < theAttributes.getLength(); j++) {
					Node theAttribute = theAttributes.item(j);
					if (theAttribute.getNodeName().equals(NAME_ATTRIBUTE)) {

						if (theAttribute.getNodeValue().equals(IMPLEMENTATION)) {
							implementation = theNode.getFirstChild().getNodeValue();
						}
						if (theAttribute.getNodeValue().equals(TITLE)) {
							title = theNode.getFirstChild().getNodeValue();
						}
						if (theAttribute.getNodeValue().equals(URL)) {
							url = theNode.getFirstChild().getNodeValue();
						}

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getTitle() {
		return title;
	}

	public String getImplementation() {
		return implementation;
	}

	public boolean isDefaultVisualization() {
		return implementation.equals(WebVisualization.class.getName());
	}
	
	public String getUrl() {
		return url;
	}
}
