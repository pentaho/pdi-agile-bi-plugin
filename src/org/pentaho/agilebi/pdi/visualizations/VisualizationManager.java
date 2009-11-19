package org.pentaho.agilebi.pdi.visualizations;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.emory.mathcs.backport.java.util.Collections;

public class VisualizationManager {

	private File pluginsLocation;
	private List<IVisualization> visualizations;
	private static VisualizationManager instance;

	public static final String PLUGIN_FILE = "plugin.xml"; //$NON-NLS-1$

	public static VisualizationManager getInstance() {
	  if (instance == null) {
	    instance = new VisualizationManager("plugins/spoon/agile-bi/visualizations"); //$NON-NLS-1$
	  }
	  return instance;
	}
	
	protected VisualizationManager() {
	  visualizations = new ArrayList<IVisualization>();
	}
	
	public VisualizationManager(String aLocation) {
		visualizations = new ArrayList<IVisualization>();
		pluginsLocation = new File(aLocation);
		loadVisualizations(pluginsLocation);
	}

	@SuppressWarnings("unchecked")
  private void loadVisualizations(File aDir) {
		File[] theFiles = aDir.listFiles();
		for (int i = 0; i < theFiles.length; i++) {
			File theFile = theFiles[i];
			if (theFile.isDirectory()) {
			  File[] dirFiles = theFile.listFiles();
			  for (int j = 0; j < dirFiles.length; j++) {
			    File pluginFile = dirFiles[j];
			    if (pluginFile.getName().equals(PLUGIN_FILE)) {
			      loadVisualizationFile(pluginFile);
			    }
			  }
			}
		}
		
		// sort the list based on order and alpha
		Collections.sort(visualizations, new Comparator() {
      public int compare(Object o1, Object o2) {
        IVisualization v1 = (IVisualization)o1;
        IVisualization v2 = (IVisualization)o2;
        if (v1.getOrder() > v2.getOrder()) {
          return -1;
        } else if (v1.getOrder() < v2.getOrder()) {
          return 1;
        } else {
          return v1.getTitle().compareTo(v2.getTitle());
        }
      }
		  
		});
	}
	
	@SuppressWarnings("unchecked")
  protected void loadVisualizationFile(File file) {
    try {
      ApplicationContext context = new FileSystemXmlApplicationContext(file.getPath());
      Map beans = context.getBeansOfType(IVisualization.class);
      for (Object key : beans.keySet()) {
        IVisualization vis = (IVisualization)beans.get(key);
        if (vis.getOrder() >= 0) {
          visualizations.add(vis);
        }
      }
    } catch (XmlBeanDefinitionStoreException e) {
      // TODO: introduce logging
      e.printStackTrace();
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
