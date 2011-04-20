package org.pentaho.agilebi.spoon;

import org.eclipse.swt.widgets.Display;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.IModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;
import org.pentaho.agilebi.modeler.strategy.AutoModelStrategy;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.concept.types.LocalizedString;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: nbaker
 * Date: Jul 14, 2010
 */
public class SpoonModelerWorkspaceHelper extends BaseModelerWorkspaceHelper implements IModelerWorkspaceHelper {

  ModelerWorkspaceHelper helper;

  public SpoonModelerWorkspaceHelper() {
    super(LocalizedString.DEFAULT_LOCALE);
    helper = new ModelerWorkspaceHelper(LocalizedString.DEFAULT_LOCALE);
  }

  /**
   * Builds an OLAP model that is attribute based.
   * @param workspace
   */
  public void autoModelFlat( ModelerWorkspace workspace ) throws ModelerException {
    helper.autoModelFlat(workspace);
  }


  /**
   * Builds an OLAP model that is attribute based.
   * @param workspace
   */
  public void autoModelFlatInBackground( final ModelerWorkspace workspace ) throws ModelerException {

    final AutoModelStrategy strategy = getAutoModelStrategy();

    final Display display = Display.findDisplay(Thread.currentThread());
    Runnable worker = new Runnable(){

      public void run() {
        final boolean prevChangeState = workspace.isModelChanging();
        try {
          MainModelNode node = workspace.getModel();
          node.setSupressEvents(true);
          strategy.autoModelOlap(workspace, node);
        } catch (ModelerException e) {
        }
        display.syncExec(new Runnable(){

          public void run() {
            workspace.getModel().setSupressEvents(false);
            workspace.setModelIsChanging(prevChangeState, true);
            workspace.setSelectedNode(workspace.getModel());
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
//    if (strategy instanceof SimpleAutoModelStrategy) {
//      ((SimpleAutoModelStrategy) strategy).setSuppressUiEvents(true);
//    }
    return strategy;
  }

}

