package fr.univmrs.tagc.logicalfunction.operators;

import fr.univmrs.tagc.javaMDD.MDDFactory;
import fr.univmrs.tagc.logicalfunction.FunctionParser;
import fr.univmrs.tagc.logicalfunction.FunctionNode;

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
  public int getMDD(MDDFactory factory) {
	  return getMDD(factory, false);
  }

  
  public abstract String getSymbol();
  public abstract int getNbArgs();
  public abstract FunctionNode[] getArgs();
}
