package org.colomoto.mddlib.logicalfunction;

import java.util.List;

import org.colomoto.mddlib.MDDManager;


/**
 * A factory to create operands (leaves in a logical function) and associate them to MDDVariables.
 * 
 * @author Aurelien Naldi
 */
public interface OperandFactory {
	
	/**
	 * get (or refresh) the MDDManager associated to this parser.
	 * If it was already created calling this method may refresh it:
	 * add newly added variables, apply name changes...
	 * 
	 * @return a MDD manager with the same variables as this parser.
	 */
	MDDManager getMDDManager();
	
	/**
	 * Check that a list of names matches valid operands for this parser.
	 * 
	 * @param list
	 * 
	 * @return true if all these operands are valid
	 */
	boolean verifOperandList(List<String> list);

	/**
	 * Create an operand object for a given name.
	 * 
	 * @param name
	 * 
	 * @return an operand (BooleanNode) corresponding to the provided string 
	 */
	FunctionNode createOperand(String name);
}
