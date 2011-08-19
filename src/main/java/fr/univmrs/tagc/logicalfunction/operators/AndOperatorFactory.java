package fr.univmrs.tagc.logicalfunction.operators;

import java.util.Stack;

import fr.univmrs.tagc.javaMDD.MDDOperator;
import fr.univmrs.tagc.javaMDD.operators.MDDBaseOperators;
import fr.univmrs.tagc.logicalfunction.FunctionNode;
import fr.univmrs.tagc.logicalfunction.OperatorFactory;

/**
 * Factory for the "and" operator.
 * 
 * @author Aurelien Naldi
 */
public class AndOperatorFactory implements OperatorFactory {

	/**
	 * Use this single instance object if you need this factory
	 */
	public static final AndOperatorFactory FACTORY = new AndOperatorFactory();
	
	public static final int PRIORITY = 0;
	public static final String SYMBOL = "&";

	private AndOperatorFactory() {
		// single-instance: no constructor
	}
	
	@Override
	public String getSymbol() {
		return SYMBOL;
	}

	@Override
	public int getPriority() {
		return PRIORITY;
	}

	@Override
	public FunctionNode getNode(Stack<FunctionNode> stack) {
		return new AndOperator(stack);
	}
	
	public FunctionNode getNode(FunctionNode n1, FunctionNode n2) {
		return new AndOperator(n1, n2);
	}
}

/**
 * The "and" operator itself
 * 
 * @author Fabrice Lopez: initial implementation
 * @author Aurelien Naldi: adaptation
 */
class AndOperator extends AbstractBinaryOperator {

	public AndOperator(Stack<FunctionNode> stack) {
		super(stack);
	}

	public AndOperator(FunctionNode leftArg, FunctionNode rightArg) {
		super(leftArg, rightArg);
	}

	@Override
	public String getSymbol() {
		return AndOperatorFactory.SYMBOL;
	}

	@Override
	protected MDDOperator getMDDOperation(boolean reversed) {
		if (reversed) {
			return MDDBaseOperators.OR;
		}
		return MDDBaseOperators.AND;
	}
}
