package org.pentaho.agilebi.pdi.visualizations;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VisualizationManager {

	private File pluginsLocation;
	private List<IVisualization> visualizations;
	private static VisualizationManager instance;

	public static final String PLUGIN_FILE = "plugin.xml";

	public VisualizationManager(String aLocation) {
		visualizations = new ArrayList<IVisualization>();
		pluginsLocation = new File(aLocation);
		loadVisualizations(pluginsLocation);
	}

	public static VisualizationManager getInstance() {
		if (instance == null) {
			instance = new VisualizationManager("package-res/visualizations");
		}
		return instance;
	}

	private void loadVisualizations(File aFile) {
		try {
			VisualizationMetaData theMetaData = null;
			File[] theFiles = aFile.listFiles();
			for (int i = 0; i < theFiles.length; i++) {
				File theFile = theFiles[i];
				if (theFile.isFile()) {
					if (theFile.getName().equals(PLUGIN_FILE)) {
						theMetaData = new VisualizationMetaData(theFile);
						if (theMetaData.isDefaultVisualization()) {

							// Default visualization. It can be loaded from
							// the classpath.
							Class theClass = Class.forName(theMetaData.getImplementation());
							createVisualization(theMetaData, theClass);

						} else {
							// Custom visualization. It would be loaded from
							// a jar file inside the lib folder.
							loadCustomVisualization(theMetaData, theFile.getParentFile());
						}
					}

				} else {
					loadVisualizations(theFile);
				}
			}
		} catch (Exception e) {
		}
	}

	private void loadCustomVisualization(VisualizationMetaData aMetaData, File aFile) throws Exception {
		File[] theFiles = aFile.listFiles();
		for (int i = 0; i < theFiles.length; i++) {
			File theFile = theFiles[i];
			if (theFile.isFile()) {
				if (theFile.getName().endsWith(".jar")) {

					ClassLoader theClassLoader = createClassLoader(theFile);
					Class theClass = theClassLoader.loadClass(aMetaData.getImplementation());
					if (theClass != null) {
						createVisualization(aMetaData, theClass);
					}

				}
			} else {
				loadCustomVisualization(aMetaData, theFile);
			}
		}

	}

	private ClassLoader createClassLoader(File aFile) throws Exception {
		URL theJarLocation = new URL("file", "", aFile.getAbsolutePath());
		return URLClassLoader.newInstance(new URL[] { theJarLocation });
	}

	private void createVisualization(VisualizationMetaData aMetaData, Class aClass) throws Exception {
		IVisualization theVisualization = null;
		Class[] theInterfaces = aClass.getInterfaces();
		for (int i = 0; i < theInterfaces.length; i++) {
			if (theInterfaces[i].isAssignableFrom(IVisualization.class)) {
				theVisualization = (IVisualization) aClass.newInstance();
				theVisualization.setTitle(aMetaData.getTitle());
				theVisualization.setUrl(aMetaData.getUrl());
				this.visualizations.add(theVisualization);
			}
		}
	}

	public List<IVisualization> getVisualizations() {
		return visualizations;
	}

	public List<String> getVisualizationNames() {

		List<String> theNames = new ArrayList<String>();
		List<IVisualization> theVisualizations = getVisualizations();
		Iterator<IVisualization> theItr = theVisualizations.iterator();
		while (theItr.hasNext()) {
			IVisualization theVisualization = theItr.next();
			theNames.add(theVisualization.getTitle());
		}
		return theNames;
	}

	public IVisualization getVisualization(String aVisualization) {

		List<IVisualization> theVisualizations = getVisualizations();
		Iterator<IVisualization> theItr = theVisualizations.iterator();
		IVisualization theVisualization = null;
		while (theItr.hasNext()) {
			theVisualization = (IVisualization) theItr.next();
			if (theVisualization.getTitle().equals(aVisualization)) {
				break;
			} else {
				theVisualization = null;
			}
		}
		return theVisualization;
	}

}
