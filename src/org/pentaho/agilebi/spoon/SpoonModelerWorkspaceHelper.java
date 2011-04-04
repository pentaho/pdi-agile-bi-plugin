package org.pentaho.agilebi.spoon;

import org.eclipse.swt.widgets.Display;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.IModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.*;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.concept.types.DataType;
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

    final Display display = Display.findDisplay(Thread.currentThread());
    Runnable worker = new Runnable(){

      public void run() {
        MainModelNode mainModel = new MainModelNode();
        mainModel.setName(workspace.getModelName());
        workspace.setModel(mainModel);
        final boolean prevChangeState = workspace.isModelChanging();
        workspace.setModelIsChanging(true, false);

        List<AvailableField> fields = workspace.getAvailableOlapFields();
        for( AvailableField field : fields ) {
          DataType dataType = field.getLogicalColumn().getDataType();
          if( dataType == DataType.NUMERIC) {
            // create a measure
            MeasureMetaData measure = workspace.createMeasureForNode(field);
            workspace.addMeasure(measure);
          }
          // create a dimension
          workspace.addDimensionFromNode(field);
        }
        display.syncExec(new Runnable(){

          public void run() {
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

    final Display display = Display.findDisplay(Thread.currentThread());
    Runnable worker = new Runnable(){

      public void run() {
        RelationalModelNode relationalModelNode = new RelationalModelNode();
        relationalModelNode.setName(workspace.getRelationalModelName());

        workspace.setRelationalModel(relationalModelNode);
        final boolean prevChangeState = workspace.isModelChanging();
        workspace.setRelationalModelIsChanging(true, false);

        CategoryMetaData category = new CategoryMetaData("Category");

        List<AvailableField> fields = workspace.getAvailableFields();
        for( AvailableField field : fields ) {
          category.add(workspace.createFieldForParentWithNode(category, field));
        }
        workspace.addCategory(category);

        display.syncExec(new Runnable(){

          public void run() {
            workspace.setModelIsChanging(prevChangeState, true);
            workspace.setSelectedRelationalNode(workspace.getRelationalModel());
          }
        });
      }
    };
    new Thread(worker).start();
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
}

