package org.colomoto.mddlib.logicalfunction;

import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;


/**
 * A factory to create operands (leaves in a logical function) and associate them to MDDVariables.
 * 
 * @author Aurelien Naldi
 */
public interface OperandFactory {
	
	/**
	 * get (or refresh) the MDDFactory associated to this parser.
	 * If it was already created calling this method may refresh it:
	 * add newly added variables, apply name changes...
	 * 
	 * @return a MDD factory with the same variables as this parser.
	 */
	public MDDManager getMDDFactory();
	
	/**
	 * Check that a list of names matches valid operands for this parser.
	 * 
	 * @param list
	 * 
	 * @return true if all these operands are valid
	 */
	abstract boolean verifOperandList(List<String> list);

	/**
	 * Create an operand object for a given name.
	 * 
	 * @param name
	 * 
	 * @return an operand (BooleanNode) corresponding to the provided string 
	 */
	abstract FunctionNode createOperand(String name);
}
