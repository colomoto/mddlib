package org.colomoto.mddlib.internal;

import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.NodeRelation;

/**
 * Set of method shared by MDDFactory and MDDStore:
 * they must be implemented in the backend, yet accessible to everyone.
 * 
 * @author Aurelien Naldi
 */
public interface MDDStoreCommon {

	
	/**
	 * Get the variable associated to a given node.
	 * 
	 * @param n the node ID
	 * 
	 * @return the variable of null if the node is a leaf.
	 */
	MDDVariable getNodeVariable(int n);

	MDDVariable getVariableForKey(Object key);
	
	MDDVariable[] getAllVariables();
	
	/**
	 * Free a node. If it is not used at all anymore, it will be removed from the data structure.
	 * <p>
	 * This should be used for each node id that was obtained through one of the <code>get_?node()</code> method,
	 * or for the one explicitly marked as used.
	 * <p>
	 * Note that the space will be made available for new nodes, but it will NOT be returned to the garbage collector.
	 * 
	 * @param pos
	 * 
	 * @see <code>use(int)</code>
	 * @see <code>get_bnode(int, int, int)</code>
	 * @see <code>get_mnode(int, int[])</code>
	 */
	void free(int pos);
	
	/**
	 * Mark a node as used. This is done automatically when creating or reusing a node.
	 * In most cases, manually calling this should not be necessary.
	 * If you do call it, remember to call <code>free(int)</code> when you release it.
	 * 
	 * @param node
	 * 
	 * @return the ID of the node (yes, the same as the parameter, just as convenience)
	 */
	int use(int node);

	/**
	 * @param node the ID of the tested node.
	 * 
	 * @return true if id denotes a leaf in this manager
	 */
	boolean isleaf(int node);
	
	/**
	 * Get a specific child for a node.
	 * Note: the child is not counted as used, do not free it (or call manager.use() before)
	 * 
	 * @param node    id of a node
	 * @param value   assignment value
	 * 
	 * @return the id of the child node reached when assigning this value to the variable of the provided node.
	 */
	int getChild(int node, int value);

	/**
	 * Get all the children of a given node.
	 * Node: this will create a new array to hold the children.
	 * Children are not counted as used, do not free them (or call manager.use() before)
	 * 
	 * @param node
	 * @return an array holding all the children, or null if it is a leaf.
	 */
	int[] getChildren(int node);


	/**
	 * Logical not, performed by flipping leaves 0 and 1.
	 * @param node
	 * @return the ID of the flipped MDD root
	 */
	int not(int node);


	/**
	 * Determine the relation between two nodes.
	 * Mainly used by operators to select the appropriate code path.
	 * 
	 * @param first
	 * @param other
	 * 
	 * @return  an NodeRelation value denoting which node is a leaf or has the highest ranked variable
	 */
	NodeRelation getRelation(int first, int other);
	
	
	/**
	 * @return the number of non-leaf nodes stored in the manager.
	 */
	int getNodeCount();

	
	/**
	 * Infer the effect of a variable in a given MDD.
	 * 
	 * @param node   the MDD
	 * @param pivot  the variable of interest
	 * @return 0 for no effect, 1 for positive effect, -1 for negative effect or 2 for dual effect
	 */
	int getSign(int node, MDDVariable pivot);

}
