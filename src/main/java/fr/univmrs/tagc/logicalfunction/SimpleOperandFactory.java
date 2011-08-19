package fr.univmrs.tagc.logicalfunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.univmrs.tagc.javaMDD.MDDFactory;
import fr.univmrs.tagc.javaMDD.MultiValuedVariable;

/**
 * Simple factory for logical function operands, using a list of predefined operands.
 * 
 * @author Aurelien Naldi
 */
public class SimpleOperandFactory<T> implements OperandFactory {

	private final Map<String, SimpleOperand<T>> operandMap = new HashMap<String, SimpleOperand<T>>();
	private final T[] operands;
	
	private MDDFactory factory = null;
	
	/**
	 * Create a parser with a predefined list of operands.
	 * 
	 * @param operands
	 */
	public SimpleOperandFactory(T[] operands) {
		this.operands = operands;
		int i=0;
		for (T obj: operands) {
			SimpleOperand<T> operand = new SimpleOperand<T>(obj, i);
			i++;
			operandMap.put(operand.toString(), operand);
		}
	}
	
	@Override
	public boolean verifOperandList(List<String> list) {
		return true;
	}

	@Override
	public AbstractOperand createOperand(String name) {
		return operandMap.get(name);
	}

	@Override
	public MultiValuedVariable[] getMDDVariables() {
		MultiValuedVariable[] variables = new MultiValuedVariable[operandMap.size()];
		int i = 0;
		for (T operand: operands) {
			variables[i] = new MultiValuedVariable(operand, operand.toString(), 2);
			i++;
		}
		return variables;
	}
	
	@Override
	public MDDFactory getMDDFactory() {
		if (factory == null) {
			MultiValuedVariable[] t_variables = getMDDVariables();
			factory = new MDDFactory(t_variables, 2);
		}
		return factory;
	}
}

class SimpleOperand<T> extends AbstractOperand {

	T object;
	int variable;
	
	public SimpleOperand(T object, int variable) {
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
