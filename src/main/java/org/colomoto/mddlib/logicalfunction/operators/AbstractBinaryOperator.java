package org.colomoto.mddlib.logicalfunction.operators;

import java.util.Stack;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDOperator;
import org.colomoto.mddlib.logicalfunction.FunctionNode;


/**
 * Common methods for nodes denoting binary operators (and, or).
 *  
 * @author Fabrice Lopez: initial implementation
 * @author Aurelien Naldi: adaptation
 */
public abstract class AbstractBinaryOperator extends AbstractOperator {
	protected final FunctionNode leftArg, rightArg;
	
	public AbstractBinaryOperator(FunctionNode leftArg, FunctionNode rightArg) {
		this.leftArg = leftArg;
		this.rightArg = rightArg;
		if (leftArg == null || rightArg == null) {
			throw new RuntimeException("Wrong args?");
		}
	}
	public AbstractBinaryOperator(Stack<FunctionNode> stack) {
		System.out.println(stack);
		rightArg = stack.pop();
		leftArg = stack.pop();
		if (leftArg == null || rightArg == null) {
			throw new RuntimeException("Wrong args?");
		}
	}
	  
	@Override
	public String toString(boolean par) {
	  	boolean leftPar = true;
	  	if (leftArg.isLeaf()) {
	  		leftPar = false;
		} else if (((AbstractOperator)leftArg).getSymbol().equals(getSymbol())) {
			leftPar = false;
		}
	  	boolean rightPar = true;
	  	if (rightArg.isLeaf()) {
			rightPar = false;
		} else if (((AbstractOperator)rightArg).getSymbol().equals(getSymbol())) {
			rightPar = false;
		}
	  	String s = leftArg.toString(leftPar) + " " + getSymbol() + " " + rightArg.toString(rightPar);
	  	if (par) {
			s = "(" + s + ")";
		}
	    return s;
	}
	
	@Override
	public int getNbArgs() {
		return 2;
	}
	
	@Override
	public FunctionNode[] getArgs() {
	    FunctionNode[] r = new FunctionNode[2];
	    r[0] = leftArg;
	    r[1] = rightArg;
	    return r;
	}
	
	@Override
	public int getMDD(MDDManager factory, boolean reversed) {
		// TODO: use group merging if the children are binary operators of the same type
		int l = leftArg.getMDD(factory, reversed);
		int r = rightArg.getMDD(factory, reversed);
		int ret = getMDDOperation(reversed).combine(factory, l, r);
		factory.free(l);
		factory.free(r);
		return ret;
	}

	abstract protected MDDOperator getMDDOperation(boolean reversed);
}
