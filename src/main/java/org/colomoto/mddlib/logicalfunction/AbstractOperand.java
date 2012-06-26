package org.colomoto.mddlib.logicalfunction;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;

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
	 * @see MDDManager#getVariableForKey(Object)
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
	public int getMDD(MDDManager ddmanager) {
		return getMDD(ddmanager, false);
	}

	@Override
	public int getMDD(MDDManager ddmanager, boolean reversed) {
		MDDVariable var = ddmanager.getVariableForKey(getMDDVariableKey());
		if (reversed) {
			return var.getSimpleNode(1, 0, getRangeStart(), getRangeEnd());
		}
		return var.getSimpleNode(0, 1, getRangeStart(), getRangeEnd());
	}
}
