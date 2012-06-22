package org.colomoto.mddlib.logicalfunction.operators;

import java.util.Stack;

import org.colomoto.mddlib.logicalfunction.FunctionNode;


/**
 * Common methods for unary operators (only not for now).
 * 
 * @author Fabrice Lopez: initial implementation
 * @author Aurelien Naldi: adaptation
 */
public abstract class AbstractUnaryOperator extends AbstractOperator {

	protected FunctionNode arg;

	public AbstractUnaryOperator(Stack<FunctionNode> stack) {
		arg = stack.pop();
	}
  
	public AbstractUnaryOperator(FunctionNode f) {
		arg = f;
	}

	@Override
	public String toString(boolean par) {
		String s = getSymbol() + arg.toString(true);
	    return s;
	}
	  
	@Override
	public int getNbArgs() {
	    return 1;
	}
	  
	@Override
	public FunctionNode[] getArgs() {
	    FunctionNode[] r = new FunctionNode[1];
	    r[0] = arg;
	    return r;
	}
}
