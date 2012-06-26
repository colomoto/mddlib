package org.colomoto.mddlib.logicalfunction;

import java.util.Stack;


/**
 * Create FunctionNode for a given operator.
 * <p>
 * Each operator implementation should provide a single-instance factory to the parser.
 * <p>
 * <code>OperatorFactory</code> implementations are provided for the most common operators in
 * the <code>org.colomoto.mddlib.logicalfunction.operators</code> package.
 * 
 * @author Aurelien Naldi
 */
public interface OperatorFactory {

	/**
	 * Get the symbol for this operator.
	 * The symbol is used in the text representation of logical function.
	 * It can be used to print the operator or to parse a logical function.
	 * 
	 * @return the String used for this operator.
	 */
	String getSymbol();

	/**
	 * Get the priority level of a given operator.
	 * The priority is used to decide which operator should be applied first,
	 * especially when parenthesis are missing.
	 * 
	 * @return the priority level of the corresponding operator
	 */
	int getPriority();
	
	/**
	 * Build a node representing this operator applied to the current parsing stack.
	 * The FunctionNode used as children for this operator will be extracted from the stack.
	 * 
	 * @param stack current parsing stack
	 * @return a FunctionNode for this operator
	 */
	FunctionNode getNode(Stack<FunctionNode> stack);
}
