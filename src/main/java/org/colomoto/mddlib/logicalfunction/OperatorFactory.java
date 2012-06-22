package org.colomoto.mddlib.logicalfunction;

import java.util.Stack;


/**
 * Operator factory: creates nodes corresponding to a given operator.
 * <p>
 * Each operator should provide a single-instance factory and provide it to the parser.
 * <p>
 * A default collection of factories will be made available.
 * 
 * @author Aurelien Naldi
 */
public interface OperatorFactory {

	public String getSymbol();
	
	public int getPriority();
	
	public FunctionNode getNode(Stack<FunctionNode> stack);
}
