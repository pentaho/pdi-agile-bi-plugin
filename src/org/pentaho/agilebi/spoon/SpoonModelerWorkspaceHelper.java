package org.pentaho.agilebi.spoon;

import org.eclipse.swt.widgets.Display;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.IModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.geo.GeoContextPropertiesProvider;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.agilebi.modeler.strategy.AutoModelStrategy;
import org.pentaho.agilebi.modeler.strategy.SimpleAutoModelStrategy;
import org.pentaho.agilebi.modeler.strategy.StarSchemaAutoModelStrategy;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.concept.types.LocalizedString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * User: nbaker
 * Date: Jul 14, 2010
 */
public class SpoonModelerWorkspaceHelper extends BaseModelerWorkspaceHelper implements IModelerWorkspaceHelper {

  ModelerWorkspaceHelper helper;

  GeoContext geoContext;
  private static final String GEO_CONFIG_FILE_LOCATION = "plugins/spoon/agile-bi/geoRoles.properties"; //$NON-NLS-1$

  public SpoonModelerWorkspaceHelper() {
    super(LocalizedString.DEFAULT_LOCALE);
    helper = new ModelerWorkspaceHelper(LocalizedString.DEFAULT_LOCALE);
    this.geoContext = initGeoContext();
  }

  /**
   * Builds an OLAP model that is attribute based.
   * @param workspace
   */
  public void autoModelFlat( ModelerWorkspace workspace ) throws ModelerException {
    if(this.geoContext == null) {
      this.geoContext = initGeoContext();
    }
    workspace.setGeoContext(this.geoContext);
    helper.autoModelFlat(workspace);
    helper.autoModelRelationalFlat(workspace);
  }


  /**
   * Builds an OLAP model that is attribute based.
   * @param workspace
   */
  public void autoModelFlatInBackground( ModelerWorkspace workspace ) throws ModelerException {

    if(this.geoContext == null) {
      this.geoContext = initGeoContext();
    }
    workspace.setGeoContext(this.geoContext);
    final ModelerWorkspace ws = workspace;
    final AutoModelStrategy strategy = getAutoModelStrategy();

    final Display display = Display.findDisplay(Thread.currentThread());
    Runnable worker = new Runnable(){

      public void run() {
        final boolean prevChangeState = ws.isModelChanging();
        try {

          MainModelNode node = ws.getModel();
          node.setSupressEvents(true);
          strategy.autoModelOlap(ws, node);
        } catch (ModelerException e) {
        }
        display.syncExec(new Runnable(){

          public void run() {
            ws.getModel().setSupressEvents(false);
            ws.setModelIsChanging(prevChangeState, true);
            ws.setSelectedNode(ws.getModel());
          }
        });
      }
    };
    new Thread(worker).start();


  }

  /**
   * Builds a Relational Model that is attribute based, all available fields are added into a single Category
   * @param workspace
   * @throws ModelerException
   */
  public void autoModelRelationalFlat(ModelerWorkspace workspace) throws ModelerException {
    helper.autoModelRelationalFlat(workspace);
  }

  /**
   * Builds a Relational Model that is attribute based, all available fields are added into a single Category
   * @param workspace
   * @throws ModelerException
   */
  public void autoModelRelationalFlatInBackground(final ModelerWorkspace workspace) throws ModelerException {

    final AutoModelStrategy strategy = getAutoModelStrategy();

    final Display display = Display.findDisplay(Thread.currentThread());
    Runnable worker = new Runnable(){

      public void run() {
        final boolean prevChangeState = workspace.isModelChanging();
        try {
          RelationalModelNode node = workspace.getRelationalModel();
          node.setSupressEvents(true);
          strategy.autoModelRelational(workspace, node);
        } catch (ModelerException e) {

        }
        display.syncExec(new Runnable(){

          public void run() {
            workspace.getRelationalModel().setSupressEvents(false);
            workspace.setRelationalModelIsChanging(prevChangeState, true);
            workspace.setSelectedRelationalNode(workspace.getRelationalModel());
          }
        });
      }
    };
    new Thread(worker).start();
  }

  @Override
  protected MainModelNode getMainModelNode(ModelerWorkspace workspace) {
    return new MainModelNode();
  }

  @Override
  protected RelationalModelNode getRelationalModelNode(ModelerWorkspace workspace) {
    return new RelationalModelNode();
  }

  public void sortFields( List<AvailableField> availableFields) {
    Collections.sort(availableFields, new Comparator<AvailableField>() {
      public int compare( AvailableField o1, AvailableField o2 ) {
        if (o1 == null && o2 == null) {
          return 0;
        } else if (o1 == null) {
          return -1;
        } else if (o2 == null) {
          return 1;
        }
        String name1 = ((AvailableField) o1).getDisplayName();
        String name2 = ((AvailableField) o2).getDisplayName();
        if (name1 == null && name2 == null) {
          return 0;
        } else if (name1 == null) {
          return -1;
        } else if (name2 == null) {
          return 1;
        }
        return name1.compareToIgnoreCase(name2);
      }
    });
  }

  public AutoModelStrategy getAutoModelStrategy() {
    AutoModelStrategy strategy = super.getAutoModelStrategy();

    if (strategy instanceof SimpleAutoModelStrategy) {
      ((SimpleAutoModelStrategy) strategy).setGeoContext(initGeoContext());
    }
    return strategy;
  }

  public static GeoContext initGeoContext() {

    try {
      FileInputStream fis = new FileInputStream(new File(GEO_CONFIG_FILE_LOCATION));
      Properties props = new Properties();
      props.load(fis);
      GeoContextPropertiesProvider config = new GeoContextPropertiesProvider(props);

      return GeoContextFactory.create(config);
    } catch (Exception e) {
      //
    }
    return null;
  }

}

