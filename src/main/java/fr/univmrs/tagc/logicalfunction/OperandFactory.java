package fr.univmrs.tagc.logicalfunction;

import java.util.List;

import fr.univmrs.tagc.javaMDD.MDDFactory;
import fr.univmrs.tagc.javaMDD.MultiValuedVariable;

/**
 * A factory to create operands (leaves in a logical function) and associate them to MDDVariables.
 * 
 * @author Aurelien Naldi
 */
public interface OperandFactory {
	
	/**
	 * Get MDDVariables corresponding to the operands used in this factory.
	 * 
	 * @return MDDVariables that can be used to created a related MDDFactory
	 */
	public MultiValuedVariable[] getMDDVariables();

	/**
	 * get (or refresh) the MDDFactory associated to this parser.
	 * If it was already created calling this method may refresh it:
	 * add newly added variables, apply name changes...
	 * 
	 * @return a MDD factory with the same variables as this parser.
	 */
	public MDDFactory getMDDFactory();
	
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
