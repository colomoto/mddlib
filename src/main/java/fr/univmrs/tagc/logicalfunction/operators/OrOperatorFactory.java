package fr.univmrs.tagc.logicalfunction.operators;

import java.util.Stack;

import fr.univmrs.tagc.javaMDD.MDDOperator;
import fr.univmrs.tagc.javaMDD.operators.MDDBaseOperators;
import fr.univmrs.tagc.logicalfunction.BooleanNode;
import fr.univmrs.tagc.logicalfunction.OperatorFactory;

/**
 * Factory for the "or" operator.
 * 
 * @author Aurelien Naldi
 */
public class OrOperatorFactory implements OperatorFactory {

	/**
	 * Use this single instance object if you need this factory
	 */
	public static final OrOperatorFactory FACTORY = new OrOperatorFactory();
	
	public static final int PRIORITY = 0;
	public static final String SYMBOL = "|";

	private OrOperatorFactory() {
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
	public BooleanNode getNode(Stack<BooleanNode> stack) {
		return new OrOperator(stack);
	}
	
	public BooleanNode getNode(BooleanNode n1, BooleanNode n2) {
		return new OrOperator(n1, n2);
	}
}

/**
 * The "or" operator itself.
 * 
 * @author Fabrice Lopez: initial implementation
 * @author Aurelien Naldi: adaptation
 */
class OrOperator extends AbstractBinaryOperator {

	public OrOperator(Stack<BooleanNode> stack) {
		super(stack);
	}

	public OrOperator(BooleanNode leftArg, BooleanNode rightArg) {
		super(leftArg, rightArg);
	}

	@Override
	public String getSymbol() {
		return OrOperatorFactory.SYMBOL;
	}
	
	@Override
	protected MDDOperator getMDDOperation(boolean reversed) {
		if (reversed) {
			return MDDBaseOperators.AND;
		}
		return MDDBaseOperators.OR;
	}
}