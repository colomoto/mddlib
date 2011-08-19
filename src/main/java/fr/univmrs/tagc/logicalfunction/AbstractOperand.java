package fr.univmrs.tagc.logicalfunction;

import fr.univmrs.tagc.javaMDD.MDDFactory;

/**
 * An operand in a logical function.
 * <p>
 * This object depends on the type of logical function and needs specialised implementations.
 * 
 * @author Fabrice Lopez: initial implementation
 * @author Aurelien Naldi: adaptation
 */
public abstract class AbstractOperand implements FunctionNode {

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * @return the key associated to this MDD variable.
	 * 
	 * @see MDDFactory#getVariableID(Object)
	 */
	abstract public Object getMDDVariableKey();
	
	/**
	 * @return the first value of the range for which this operand is true
	 */
	public int getRangeStart() {
		return 1;
	}

	/**
	 * @return the last value of the range for which this operand is true
	 */
	public int getRangeEnd() {
		return 1;
	}

	@Override
	public int getMDD(MDDFactory factory) {
		return getMDD(factory, false);
	}

	@Override
	public int getMDD(MDDFactory factory, boolean reversed) {
		int var = factory.getVariableID(getMDDVariableKey());
		if (reversed) {
			return factory.getSimpleNode(var, 1, 0, getRangeStart(), getRangeEnd());
		}
		return factory.getSimpleNode(var, 0, 1, getRangeStart(), getRangeEnd());
	}
}
