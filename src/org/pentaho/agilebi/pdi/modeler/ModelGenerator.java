package org.pentaho.agilebi.pdi.modeler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.metadata.automodel.AutoModeler;
import org.pentaho.metadata.automodel.SchemaTable;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.engine.core.system.StandaloneSession;

public class ModelGenerator extends AutoModeler {

  public ModelGenerator() {
    super( null, null, null, null );
  }

  /**
   * Builds an OLAP model that is attribute based.
   * @param modelId
   * @param rowMeta
   * @param locale
   * @param user
   * @param tableName
   */
//  public void generateFlatModel( String modelId, RowMetaInterface rowMeta, DatabaseMeta databaseMeta, String locale, String user, String tableName ) {
//    
//    // 
//    List<ColumnProfile> profileList = new ArrayList<ColumnProfile>();
//    List<ValueMetaInterface> valueMetaList = rowMeta.getValueMetaList();
//    
//    for( ValueMetaInterface valueMeta : valueMetaList ) {
//      // create one or more profiles for each value meta
//      ColumnProfile profile = new ColumnProfile();
//      int dataType = valueMeta.getType();
//      if( dataType ==  ValueMetaInterface.TYPE_BIGNUMBER || 
//          dataType == ValueMetaInterface.TYPE_INTEGER ||
//          dataType == ValueMetaInterface.TYPE_NUMBER ) {
//        // create a measure
//        profile.setFieldType( ColumnProfile.FIELD_FACT );
//        profile.setType( valueMeta.getType() );
//        profile.setId( valueMeta.getName()+" (m)" );
//        profile.setColumnName( valueMeta.getName() );
//        profile.setTitle( valueMeta.getName()+"(m)" );
//        profile.setUsed(true);
//        profileList.add( profile );
//        profile.setFormat("#");
//      }
//      profile = new ColumnProfile();
//      profile.setFieldType( ColumnProfile.FIELD_DIMENSION );
//      profile.setType( valueMeta.getType() );
//      profile.setId( valueMeta.getName() );
//      profile.setColumnName( valueMeta.getName() );
//      profile.setDimensionTitle( valueMeta.getName() );
//      profile.setTitle( valueMeta.getName() );
//      profile.setUsed(true);
//      profileList.add( profile );
//    }
//    ColumnProfile profiles[] = profileList.toArray( new ColumnProfile[profileList.size()] );
//    generateModels( modelId, databaseMeta, locale, user, tableName, profiles );
//  }
//  


}
