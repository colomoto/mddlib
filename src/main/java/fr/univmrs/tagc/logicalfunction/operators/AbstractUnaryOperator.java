package fr.univmrs.tagc.logicalfunction.operators;

import java.util.Stack;

import fr.univmrs.tagc.logicalfunction.BooleanNode;

/**
 * Common methods for unary operators (only not for now).
 * 
 * @author Fabrice Lopez: initial implementation
 * @author Aurelien Naldi: adaptation
 */
public abstract class AbstractUnaryOperator extends AbstractOperator {

	protected BooleanNode arg;

	public AbstractUnaryOperator(Stack<BooleanNode> stack) {
		arg = stack.pop();
	}
  
	public AbstractUnaryOperator(BooleanNode f) {
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
	public BooleanNode[] getArgs() {
	    BooleanNode[] r = new BooleanNode[1];
	    r[0] = arg;
	    return r;
	}
}
