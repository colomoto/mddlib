package org.colomoto.mddlib.logicalfunction.operators;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.logicalfunction.FunctionNode;
import org.colomoto.mddlib.logicalfunction.FunctionParser;


/**
 * Common methods for internal nodes (operators).
 *
 * @author Fabrice Lopez: initial implementation
 * @author Aurelien Naldi: adaptation
 */
public abstract class AbstractOperator implements FunctionNode {
  protected String returnClassName;
  protected FunctionParser parser;

  public AbstractOperator() {
    super();
  }
  @Override
  public String toString() {
	  return toString(false);
  }
  
  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public int getMDD(MDDManager factory) {
	  return getMDD(factory, false);
  }

  
  public abstract String getSymbol();
  public abstract int getNbArgs();
  public abstract FunctionNode[] getArgs();
}
