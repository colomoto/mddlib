package org.colomoto.mddlib.logicalfunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;


/**
 * Simple factory for logical function operands, using a list of predefined operands.
 * 
 * @author Aurelien Naldi
 */
public class SimpleOperandFactory<T> implements OperandFactory {

	private final Map<String, SimpleOperand<T>> operandMap = new HashMap<String, SimpleOperand<T>>();
	private final List<T> operands;
	
	private MDDManager ddmanager = null;
	
	/**
	 * Create a parser with a predefined list of operands.
	 * 
	 * @param operands
	 */
	public SimpleOperandFactory(List<T> operands) {
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
	public AbstractOperand createOperand(String name, int threshold) {
		if (threshold < 1 || threshold > 100) {
			return null;
		}

		SimpleOperand<T> operand = operandMap.get(name);
		if (threshold == 1 || operand == null) {
			return operand;
		}

		String key = name+"@"+threshold;
		SimpleOperand<T> thop = operandMap.get(key);
		if (thop == null) {
			thop = new SimpleOperand<T>(operand.object, operand.variable, threshold);
			operandMap.put(key, thop);
		}

		return thop;
	}

	@Override
	public MDDManager getMDDManager() {
		if (ddmanager == null) {
			
			ddmanager = MDDManagerFactory.getManager(operands, 2);
		}
		return ddmanager;
	}
}

class SimpleOperand<T> extends AbstractOperand {

	T object;
	int variable;
	int threshold = 1;

	public SimpleOperand(T object, int variable) {
		this(object, variable, 1);
	}
	public SimpleOperand(T object, int variable, int threshold) {
		this.object = object;
		this.variable = variable;
		this.threshold = threshold;
	}
	@Override
	public String toString(boolean par) {
		String s = object.toString();
		if (threshold != 1) {
			s += "@" + threshold;
		}
		return s;
	}
	@Override
	public T getMDDVariableKey() {
		return object;
	}

	@Override
	public int getRangeStart() {
		return threshold;
	}

	@Override
	public int getRangeEnd() {
		return Byte.MAX_VALUE;
	}
}
