package fr.univmrs.tagc.logicalfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import fr.univmrs.tagc.logicalfunction.operators.AndOperatorFactory;
import fr.univmrs.tagc.logicalfunction.operators.NotOperatorFactory;
import fr.univmrs.tagc.logicalfunction.operators.OrOperatorFactory;

/**
 * Define a collection of operators to be used by a BooleanParser.
 * It will collect OperatorFactory objects and help the parser dealing with them.
 * 
 * @author Aurelien Naldi
 *
 */
public class OperatorCollection {

	/** a collection of classical operators: and, or, not */
	public static final OperatorCollection DEFAULT_OPERATORS;
	
	static {
		DEFAULT_OPERATORS = new OperatorCollection();

		DEFAULT_OPERATORS.addFactory(AndOperatorFactory.FACTORY);
		DEFAULT_OPERATORS.addFactory(OrOperatorFactory.FACTORY);
		DEFAULT_OPERATORS.addFactory(NotOperatorFactory.FACTORY);
	}
	
	private List<String> operatorList = null;
	private String operatorsAndParenthesis = null;

	private final Map<String,OperatorFactory> m_factories = new HashMap<String,OperatorFactory>();
	
	/**
	 * Create a node corresponding to the parsed logical function.
	 * <p>
	 * The operator symbol is provided, the node will extract its children from the parsing stack.
	 * 
	 * @param value
	 * @param stack
	 * 
	 * @return an operator object for this function
	 */
	public BooleanNode createOperator(String value, Stack<BooleanNode> stack) {
		OperatorFactory of = m_factories.get(value);
		if (of == null) {
			throw new RuntimeException("invalid operator: "+value);
		}
		return of.getNode(stack);
	}

	/**
	 * get the priority level of a given operator.
	 * 
	 * @param value
	 * 
	 * @return the priority level of the corresponding operator
	 */
	public int getPriority(String value) {
		OperatorFactory of = m_factories.get(value);
		if (of == null) {
			// either it is a parenthesis or an error will be raised soon
			return -1;
		}
		return of.getPriority();
	}

	
	public List<String> getOperators() {
		if (operatorList == null) {
			operatorList = new ArrayList<String>();
			for (OperatorFactory of: m_factories.values()) {
				operatorList.add(of.getSymbol());
			}
			operatorList.add("(");
			operatorList.add(")");
		}
		return operatorList;
	}

	public String getRegex() {
		if (operatorsAndParenthesis == null) {
			StringBuffer sb = new StringBuffer();
		    for (OperatorFactory of: m_factories.values()) {
				sb.append("\\" + of.getSymbol() + "|");
			}
		    sb.append("\\(|\\)| ");
		    operatorsAndParenthesis = sb.toString();
		}
		return operatorsAndParenthesis;
	}

	public void addFactory(OperatorFactory factory) {
		m_factories.put(factory.getSymbol(), factory);
		operatorsAndParenthesis = null;
		operatorList = null;
	}

}
