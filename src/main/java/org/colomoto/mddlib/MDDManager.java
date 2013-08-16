package org.colomoto.mddlib;

import java.util.List;

/**
 * A MDD manager is responsible for the creation, storage, retrieval, combination and cleanup
 * of a collection of multi-valued decision diagrams (MDDs).
 * <p>
 * <br>The manager has:
 * <ul>
 *  <li> a set of leaves, defined from start and that can not be changed,</li>
 *  <li> a list of variables. New ones can be added afterwards, and the
 *    names can be changed but changing max values resets the manager</li>
 * </ul>
 * <p>
 * A number of leaves are defined when creating the manager. They are numbered
 * <code>[0...nbleaves[</code>. Negative leaves or holes in the values are not allowed, but you
 * are free to map indices to the values of your choice (including inside custom operators).
 * <br>
 * The same leaf can thus refer to different values, depending on the context.
 * <p>
 * Nodes can be created using the get_bnode and get_mnode methods 
 * (for Boolean and multi-valued nodes respectively), which create
 * them if needed and return the bloc ID.
 * <p>
 * Nodes can also be created by combining existing nodes, using the merge method.
 * This method uses MDDOperations to decide how to do the merging.
 * MDDOperation for the common operations are provided (see MDDOperation.ACTION_*),
 * but it was designed to help creating custom ones.
 * A group-merging method allows to efficiently merge many nodes using the same operation.
 * <p>
 * Nodes also have reference counter, which allows to reuse the space
 * when nodes are no longer used. For this, call the free method with a node ID
 * when you stop using it. Make sure to forget the ID as any further use would
 * result in unexpected results.
 * 
 * @author Aurelien Naldi
 */
public interface MDDManager {

	/**
	 * Get a new manager for this MDD Store, using a custom variable order.
	 * The new manager will use the <b>same</b> MDDStore as backend:
	 * it is just a different view of the same MDD dataset.
	 * 
	 * @param order requested variable order.
	 * @return a new manager (<code>this</code> if the order is the same)
	 */
	MDDManager getManager(List<?> order);
	
	/**
	 * Get the variable associated to a given node.
	 * 
	 * @param n the node ID
	 * 
	 * @return the variable of null if the node is a leaf.
	 */
	MDDVariable getNodeVariable(int n);

	/**
	 * Get the MDDVariable associated to a given key.
	 * 
	 * @param key
	 * @return the associated MDDVariable
	 */
	MDDVariable getVariableForKey(Object key);
	
	/**
	 * Get the position of a given variable in this manager's list of variables.
	 * 
	 * @param var
	 * @return the position in <code>getAllVariables()</code>.
	 */
	int getVariableIndex(MDDVariable var);
	
	/**
	 * Get all MDDVariables in this manager.
	 * 
	 * @return the ordered list of available MDDVariables.
	 */
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
	 * Get the number of leaves
	 * @return the number of possible leaves (i.e. max leaf index)
	 */
	int getLeafCount();

	/**
	 * Infer the effect of a variable in a given MDD.
	 * 
	 * @param node   the MDD
	 * @param pivot  the variable of interest
	 * @return 0 for no effect, 1 for positive effect, -1 for negative effect or 2 for dual effect
	 */
	int getSign(int node, MDDVariable pivot);

	/**
	 * Find the leaf reached for a given variable assignment
	 * 
	 * @param node
	 * @param values
	 * 
	 * @return the leaf reached for this assignment
	 */
	byte reach(int node, byte[] values);

	/**
	 * Get the value reached by a group of paths.
	 * 
	 * @param node
	 * @param path
	 * 
	 * @return the leaf reached for this assignment
	 */
	byte groupReach(int node, byte[] path);

	/**
	 * Collect variables on which a MDD depends.
	 * 
	 * @param node
	 * @return a boolean array indicating for each variable if it affects the given MDD.
	 */
	boolean[] collectDecisionVariables(int node);
	
	/**
	 * Determine the effect of a given variable on a MDD.
	 * To find variables of interest, use collectDecisionVariables(node).
	 * 
	 * @param var
	 * @param node
	 * @return the effect of this variable.
	 */
	VariableEffect getVariableEffect(MDDVariable var, int node);
	
	/**
	 * Determine the effect of a given variable on a MDD.
	 * To find variables of interest, use collectDecisionVariables(node).
	 * 
	 * @param var
	 * @param node
	 * @return the effects of each level switch for this variable
	 */
	VariableEffect[] getMultivaluedVariableEffect(MDDVariable var, int node);
	
	boolean isView(MDDManager ddm);
}
