package org.colomoto.mddlib.logicalfunction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.internal.MDDStoreImpl;


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
