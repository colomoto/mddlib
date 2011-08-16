package fr.univmrs.tagc.logicalfunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.univmrs.tagc.javaMDD.MDDVariable;

/**
 * Simple logical function parser using a list of predefined operands.
 * 
 * @author Aurelien Naldi
 */
public class SampleParser<T> extends AbstractBooleanParser {

	private final Map<String, SampleOperand<T>> operandMap = new HashMap<String, SampleOperand<T>>();
	private final T[] operands;
	
	/**
	 * Create a parser with a predefined list of operands.
	 * 
	 * @param operands
	 */
	public SampleParser(T[] operands) {
		this.operands = operands;
		int i=0;
		for (T obj: operands) {
			SampleOperand<T> operand = new SampleOperand<T>(obj, i);
			i++;
			operandMap.put(operand.toString(), operand);
		}
	}
	
	@Override
	public boolean verifOperandList(List<String> list) {
		return true;
	}

	@Override
	public AbstractBooleanOperand createOperand(String name) {
		return operandMap.get(name);
	}

	@Override
	public MDDVariable[] getMDDVariables() {
		MDDVariable[] variables = new MDDVariable[operandMap.size()];
		int i = 0;
		for (T operand: operands) {
			variables[i] = new MDDVariable(operand, operand.toString(), 2);
			i++;
		}
		return variables;
	}
}

class SampleOperand<T> extends AbstractBooleanOperand {

	T object;
	int variable;
	
	public SampleOperand(T object, int variable) {
		this.object = object;
		this.variable = variable;
	}
	@Override
	public String toString(boolean par) {
		return object.toString();
	}
	@Override
	public T getMDDVariableKey() {
		return object;
	}
}
