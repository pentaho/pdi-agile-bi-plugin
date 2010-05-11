package org.pentaho.agilebi.pdi.publish;

public class PublishException extends Exception {

  public PublishException(String msg){
    super(msg);
  }
  
  public PublishException(String msg, Exception e){
    super(msg, e);
  }
}
