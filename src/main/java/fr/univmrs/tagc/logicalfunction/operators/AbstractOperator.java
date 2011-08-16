package fr.univmrs.tagc.logicalfunction.operators;

import fr.univmrs.tagc.javaMDD.MDDFactory;
import fr.univmrs.tagc.logicalfunction.AbstractBooleanParser;
import fr.univmrs.tagc.logicalfunction.BooleanNode;

/**
 * Common methods for internal nodes (operators).
 *
 * @author Fabrice Lopez: initial implementation
 * @author Aurelien Naldi: adaptation
 */
public abstract class AbstractOperator implements BooleanNode {
  protected String returnClassName;
  protected AbstractBooleanParser parser;

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
  public abstract BooleanNode[] getArgs();
}
