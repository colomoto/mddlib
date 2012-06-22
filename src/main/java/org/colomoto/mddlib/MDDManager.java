package org.colomoto.mddlib;

import java.util.List;

import org.colomoto.mddlib.internal.MDDStoreCommon;

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
public interface MDDManager extends MDDStoreCommon {

//	/**
//	 * Get a variable from this manager.
//	 * 
//	 * @return the variable of the manager.
//	 */
//	MultiValuedVariable getVariable(int v);
//
//
//	/**
//	 * Get the number of variables available in this manager.
//	 * 
//	 * @return the number of valid variables
//	 */
//	int getVariableNumber();
//	
//	/**
//	 * 
//	 * @param o
//	 * @return the ID of this variable in the manager, or -1 if not found
//	 */
//	int getVariableID(Object o);
	
	

	/**
	 * Find the leaf reached for a given variable assignment
	 * 
	 * @param node
	 * @param values
	 * @return
	 */
	byte reach(int node, byte[] values);
	
}
