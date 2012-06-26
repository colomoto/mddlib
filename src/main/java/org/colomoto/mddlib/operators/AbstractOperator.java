package org.colomoto.mddlib.operators;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDOperator;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.NodeRelation;

/**
 * Common (boring) part of a MDDOperator implementation.
 * This provides a helper method for a simple recursion on two nodes and support for merging a list of nodes,
 * either by falling back on two-way merges and by performing real multiple-nodes recursion.
 * <p>
 * The main method for two-way merge is not implemented here and is the only requirement for implementors.
 * <p>
 * To properly support multiple merge, implementors are encouraged to override
 * <code>multiple_leaves(MDDManager, int[])</code>
 * and <code>recurse_multiple(MDDManager, int[], int, int)</code>.
 * 
 * @see MDDBaseOperators
 * @see AbstractFlexibleOperator
 * 
 * @author Aurelien Naldi
 */
abstract public class AbstractOperator implements MDDOperator {

	private final boolean multipleMerge;
	
	/**
	 * Create an operator which does not support multiple merge.
	 */
	public AbstractOperator() {
		this(false);
	}

	/**
	 * Create an operator.
	 * 
	 * @param multipleMerge		if true, optimised multiple merge will be used instead of the fallback
	 */
	public AbstractOperator(boolean multipleMerge) {
		this.multipleMerge = multipleMerge;
	}

	/**
	 * Common logic for recursive operation. This method is a helper
	 * for specialised implementations of <code>combine(MDDManager, int, int)</code>.
	 * 
	 * @param ddmanager
	 * @param status
	 * @param first
	 * @param other
	 * 
	 * @return the resulting node index
	 */
	public int recurse(MDDManager ddmanager, NodeRelation status, int first, int other) {
		switch (status) {
		case LN:
		case NNf:
			MDDVariable var = ddmanager.getNodeVariable(other);
			if (var.nbval == 2) {
				int l = combine(ddmanager, first, ddmanager.getChild(other,0));
				int r = combine(ddmanager, first, ddmanager.getChild(other,1));
				return var.getNodeFree(l, r);
			} else {
				int[] children = new int[var.nbval];
				for (int i=0 ; i<children.length ; i++) {
					children[i] = combine(ddmanager, first, ddmanager.getChild(other, i));
				}
				return var.getNodeFree(children);
			}
		case NL:
		case NNn:
			var = ddmanager.getNodeVariable(first);
			if (var.nbval == 2) {
				int l = combine(ddmanager, ddmanager.getChild(first,0), other);
				int r = combine(ddmanager, ddmanager.getChild(first,1), other);
				return var.getNodeFree(l, r);
			} else {
				int[] children = new int[var.nbval];
				for (int i=0 ; i<children.length ; i++) {
					children[i] = combine(ddmanager, ddmanager.getChild(first,i), other);
				}
				return var.getNodeFree(children);
			}
		case NN:
			var = ddmanager.getNodeVariable(first);
			if (var.nbval == 2) {
				int l = combine(ddmanager, ddmanager.getChild(first,0), ddmanager.getChild(other,0));
				int r = combine(ddmanager, ddmanager.getChild(first,1), ddmanager.getChild(other,1));
				return var.getNodeFree(l, r);
			} else {
				int[] children = new int[var.nbval];
				for (int i=0 ; i<children.length ; i++) {
					children[i] = combine(ddmanager, ddmanager.getChild(first,i), ddmanager.getChild(other,i));
				}
				return var.getNodeFree(children);
			}
		}
		return -1;
	}

	/**
	 * Apply this operation to a set of nodes. This method falls back to applying it multiple times
	 * to pairs of nodes unless multiple merge is marked as supported.
	 * <p>
	 * 
	 * @param ddmanager	the MDD manager in which the nodes are stored
	 * @param nodes		the roots of the MDDs to combine
	 * 
	 * @return			the root node of the combined MDD. It can be a new or an existing node.
	 */
	@Override
	public int combine(MDDManager ddmanager, int[] nodes) {
		switch (nodes.length) {
			case 0:
				throw new RuntimeException("Need at least one node to merge");
			case 1:
				return ddmanager.use(nodes[0]);
			case 2:
				return combine(ddmanager, nodes[0], nodes[1]);
		}
		
		int result = nodes[0];
		if (multipleMerge) {
			return combine(ddmanager, nodes, 0);
		}
		
		// fallback to a set of simple merges if multiple merge is not properly supported
		int oldresult = 0;
		for (int i=1 ; i<nodes.length ; i++) {
			ddmanager.free(oldresult);
			result = combine(ddmanager, result, nodes[i]);
			oldresult = result;
		}
		return result;
	}

	/**
	 * Get the result of a multiple merge when only leaves are left.
	 * If your operator supports multiple merge, it SHOULD override this method.
	 * <p>
	 * A series of two-nodes merges is performed as fallback.
	 * 
	 * @param ddmanager
	 * @param leaves
	 * 
	 * @return the resulting node index
	 */
	protected int multiple_leaves(MDDManager ddmanager, int[] leaves) {
		if (leaves.length < 1) {
			throw new RuntimeException("Need at least one node to merge");
		}
		
		// fallback to a set of simple merges if this method is not provided by the operator
		int result = leaves[0], oldresult = 0;
		for (int i=1 ; i<leaves.length ; i++) {
			ddmanager.free(oldresult);
			result = combine(ddmanager, result, leaves[i]);
			oldresult = result;
		}
		return result;
	}
	
	/**
	 * Actual implementation of the optimised multiple merge.
	 * This method is called by <code>combine(MDDManager, int[])</code>.
	 * 
	 * @param ddmanager
	 * @param nodes
	 * @param leafcount
	 * 
	 * @return the resulting node index
	 */
	private int combine(MDDManager ddmanager, int[] nodes, int leafcount) {
		MDDVariable bestVar = null;
		for (int i=leafcount ; i<nodes.length ; i++ ) {
			int id = nodes[i];
			if (ddmanager.isleaf(id)) {
				nodes[i] = nodes[leafcount];
				nodes[leafcount] = id;
				leafcount++;
			} else {
				MDDVariable var = ddmanager.getNodeVariable(id);
				bestVar = MDDVariable.selectFirstVariable(bestVar, var);
			}
		}

		if (leafcount == nodes.length) {
			return multiple_leaves(ddmanager, nodes);
		}
		return recurse_multiple(ddmanager, nodes, leafcount, bestVar);
	}

	/**
	 * Helper to remove the leaves from an array of nodes.
	 * If <code>skip</code>, it creates a copy of the end of the provided array.
	 * 
	 * @param nodes
	 * @param skip
	 * 
	 * @return the original array or a truncated copy
	 */
	static final int[] prune_start(int[] nodes, int skip) {
		if (skip < 1) {
			return nodes;
		}
		int[] new_nodes = new int[nodes.length-skip];
		System.arraycopy(nodes, skip, new_nodes, 0, new_nodes.length);
		return new_nodes;
	}
	
	/**
	 * The recursive part of the multiple merge.
	 * 
	 * @param ddmanager
	 * @param nodes
	 * @param leafcount
	 * @param bestVar
	 * 
	 * @return the resulting node index
	 */
	protected int recurse_multiple(MDDManager ddmanager, int[] nodes, int leafcount, MDDVariable bestVar) {
		if (bestVar.nbval == 2) {
			int lchild, rchild;
			int[] lnodes = new int[nodes.length], rnodes = new int[nodes.length];
			for (int i=0 ; i<leafcount ; i++) {
				lnodes[i] = rnodes[i] = nodes[i];
			}
			for (int i=leafcount ; i<nodes.length ; i++) {
				int node = nodes[i];
				if (ddmanager.getNodeVariable(node) == bestVar) {
					lnodes[i] = ddmanager.getChild(node, 0);
					rnodes[i] = ddmanager.getChild(node, 1);
				} else {
					lnodes[i] = rnodes[i] = node;
				}
			}
			lchild = combine(ddmanager, lnodes, leafcount);
			rchild = combine(ddmanager, rnodes, leafcount);
			return bestVar.getNodeFree(lchild, rchild);
		} else {
			int[] children = new int[bestVar.nbval];
			int[] nextnodes = new int[nodes.length];
			for (int v=0 ; v<children.length ; v++) {
				System.arraycopy(nodes, 0, nextnodes, 0, leafcount);
				for (int i=leafcount ; i<nodes.length ; i++) {
					int node = nodes[i];
					if (ddmanager.getNodeVariable(node) == bestVar) {
						nextnodes[i] = ddmanager.getChild(node, v);
					} else {
						nextnodes[i] = nodes[i];
					}
				}
				children[v] = combine(ddmanager, nextnodes, leafcount);
			}
			return bestVar.getNodeFree(children);
		}
	}
}
