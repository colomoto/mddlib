package org.colomoto.mddlib.logicalfunction;

import org.colomoto.mddlib.MDDManager;

/**
 * Common interface for all nodes in the trees representing logical functions.
 * 
 * @author Fabrice Lopez: initial implementation
 * @author Aurelien Naldi: adaptation
 */
public interface FunctionNode {

	/**
	 * @param par if true, add surrounding parenthesis.
	 * 
	 * @return a string representation of this logical function.
	 */
	String toString(boolean par);
	
	/**
	 * Is it a leaf? Only used to help some toString methods.
	 * @return true if this node is a leaf
	 */
	boolean isLeaf();
  
	/**
	 * Construct a MDD corresponding to this logical function.
	 * 
	 * @param ddmanager the MDDManager in which the MDD will be stored.
	 * 
	 * @return the index of the corresponding MDD root.
	 */
	int getMDD(MDDManager ddmanager);
	
	/**
	 * Construct a MDD corresponding to this logical function.
	 * 
	 * @param ddmanager the MDDManager in which the MDD will be stored.
	 * @param reversed
	 * 
	 * @return the index of the corresponding MDD root.
	 */
	int getMDD(MDDManager ddmanager, boolean reversed);
}
