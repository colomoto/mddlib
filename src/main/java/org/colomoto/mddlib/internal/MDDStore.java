package org.colomoto.mddlib.internal;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;

/**
 * Interface for the raw MDD store.
 * A <code>MDDStore</code> is a <code>MDDManager</code>, which can also be used as backend for  <code>MDDManagerProxy</code>
 * 
 * @author Aurelien Naldi
 */
public interface MDDStore extends MDDManager {

	/**
	 * Retrieve or create a node in the backend.
	 * This is called by <code>MDDVariable.getNode()</code> or by
	 * internal store methods: it should not be used directly.
	 * 
	 * @param var
	 * @param lchild
	 * @param rchild
	 * 
	 * @return the index of the requested node
	 */
	int getNode(int var, int lchild, int rchild);
	
	/**
	 * Retrieve or create a node in the backend.
	 * This is called by <code>MDDVariable.getNode()</code> or by
	 * internal store methods: it should not be used directly.
	 * 
	 * @param var
	 * @param children
	 * 
	 * @return the index of the requested node
	 */
	int getNode(int var, int[] children);

	
	/**
	 * Find the leaf reached for a given variable assignment and custom order.
	 * This is used by proxy MDDManager to implement <code>reach(int byte[])</code>
	 * 
	 * @param node
	 * @param values
	 * 
	 * @return the reached leaf
	 */
	byte reach(int node, byte[] values, int[] orderMap);

}
