package org.colomoto.mddlib;

/**
 * Definition of operators that can be used to combine MDD nodes.
 * <p>
 * An operator typically takes two nodes from the same MDDFactory, and returns a node
 * representing the combination of the input nodes.
 * <p>
 * For Boolean nodes, classical operators include "AND", "OR", "XOR"...
 * Here we aim to encourage custom operators to take advantage of multi-valued nodes or
 * to perform specialised operations.
 * 
 * @author Aurelien Naldi 
 */
public interface MDDOperator {

	/**
	 * Combine two MDDs and return the result.
	 * 
	 * @param f 	the factory in which the nodes are stored
	 * @param first the root node of the first MDD
	 * @param other the root node of the other MDD
	 * @return 		the root node of the combined MDD. It can be a new or an existing node.
	 */
	public int combine(MDDManager f, int first, int other);

	/**
	 * Combine a group of nodes at once.
	 * 
	 * @param f			the factory in which the nodes are stored
	 * @param nodes		the roots of the MDDs to combine
	 * @return			the root node of the combined MDD. It can be a new or an existing node.
	 */
	public int combine(MDDManager f, int[] nodes);
	
}