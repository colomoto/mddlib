package org.colomoto.mddlib.internal;

import org.colomoto.mddlib.MDDVariable;

/**
 * The raw store used as backend for a MDDManager.
 * 
 * @author Aurelien Naldi
 *
 */
public interface MDDStore extends MDDStoreCommon {

	
	int getNode(int var, int lchild, int rchild);
	
	int getNode(int var, int[] children);

	
	/**
	 * Find the leaf reached for a given variable assignment and custom order
	 * 
	 * @param node
	 * @param values
	 * @return
	 */
	byte reach(int node, byte[] values, int[] orderMap);

}
