package org.colomoto.mddlib;

import java.util.Iterator;

/**
 * Iterate over the paths in MDDs, optionally filtering on the leaf values.
 * <p>
 * It provides an integer array defining the path (reused for each path) and
 * returns the leaf value for each path found. In the path representation, some
 * variables may have undefined values, meaning that their value does not change
 * the reached leaf.
 * <p>
 * It implements Iterable, so you can use a for loop on it directly.
 * Note that the bundled iterator will fill the path automatically, so that you do not have to call
 * <code>fillPath</code> explicitly but you will have to do it when using the raw searcher.
 * <p>
 * The same path searcher can be reused for different MDDs.
 * <p>
 * <b>Warning</b>: the provided iterator changes the instance: you can not use several iterators separately
 * as their next() method will affect each other.
 * 
 * @author Aurelien Naldi
 */
public class PathSearcher implements Iterable<Integer> {

	private final int minvalue, maxvalue;

	private final PathBacktrack backtrack;
	private int[] path;
	private int leaf;

	/**
	 * Create a new path searcher accepting any value (negative leaves are not
	 * allowed)
	 * 
	 * @param ddmanager The MDD manager in which this search works.
	 */
	public PathSearcher(MDDManager ddmanager) {
		this(ddmanager, 0, Integer.MAX_VALUE);
	}

	/**
	 * Create a new path searcher, with a single value.
	 * 
	 * @param ddmanager
	 *            The MDD manager in which this search works.
	 * @param value
	 *            The value of the reached leaf
	 */
	public PathSearcher(MDDManager ddmanager, int value) {
		this(ddmanager, value, value);
	}

	/**
	 * Create a new path searcher, with a value range.
	 * 
	 * @param ddmanager
	 *            The MDD manager in which this search works.
	 * @param minvalue
	 *            minimal value of the reached leaves
	 * @param maxvalue
	 *            maximal value of the reached leaves
	 */
	public PathSearcher(MDDManager ddmanager, int minvalue, int maxvalue) {
		this.minvalue = minvalue;
		this.maxvalue = maxvalue;
		
		backtrack = new PathBacktrack(ddmanager);
		path = new int[ddmanager.getAllVariables().length];
	}

	/**
	 * Start looking up path for a new node.
	 * <p>
	 * Note that the node must come from the same MDD manager.
	 * For performance reasons, the same array will be reused when searching for
	 * the next path, copy its content if you need to keep it.
	 * 
	 * @param node
	 *            a new node index
	 * 
	 * @return a reusable array denoting a path to a valid leaf or null if no
	 *         more leaf are found
	 */
	public int[] setNode(int node) {
		// reset data structure
		backtrack.reset(node);
		leaf = 0;
		return path;
	}
	
	/**
	 * fill the path (returned by <code>setNode(int)</code>) with the last leaf found.
	 * 
 	 * @see #getNextLeaf()
	 */
	public void fillPath() {
		backtrack.fillPath(path);
	}

	/**
	 * Lookup the next leaf in this MDD.
	 * <p>
	 * This will lookup the next leaf and return its value but it will _NOT_ update
	 * the path. for this you have to call <code>fillPath()</code> explicitly.
	 * 
	 * @return the value of the next leaf, or -1 after the last one.
	 * @see #fillPath()
	 */
	public int getNextLeaf() {
		while (leaf >= 0) {
			leaf = backtrack.findNextLeaf();
			if (leaf >= minvalue && leaf <= maxvalue) {
				return leaf;
			}
		}
		return -1;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new PathFoundIterator(this);
	}

	/**
	 * Count the number of paths found for the current node.
	 * Note that this will enumerate all paths (using the associated iterator)
	 * 
	 * @return the number of paths found
	 */
	public int countPaths() {
		int ret = 0;
		for (Integer i: this) {
			ret++;
		}
		
		return ret;
	}
}

class PathFoundIterator implements Iterator<Integer> {
	private int leaf;
	private final PathSearcher searcher;

	public PathFoundIterator(PathSearcher searcher) {
		this.searcher = searcher;
		leaf = searcher.getNextLeaf();
	}

	@Override
	public boolean hasNext() {
		return leaf >= 0;
	}

	@Override
	public Integer next() {
		int ret = leaf;
		searcher.fillPath();
		leaf = searcher.getNextLeaf();
		return ret;
	}

	@Override
	public void remove() {
	}
}

class PathBacktrack {

	final MDDManager ddmanager;
	final int[] indices, values;

	public int pos;

	public PathBacktrack(MDDManager ddmanager) {
		this.ddmanager = ddmanager;
		this.indices = new int[ddmanager.getAllVariables().length];
		this.values = new int[indices.length];
	}

	public void reset(int node) {
		pos = 0;
		indices[0] = node;
		values[0] = -1;
		for (int i = 1; i < indices.length; i++) {
			indices[i] = -1;
			values[i] = -1;
		}
	}

	public void fillPath(int[] path) {
		for (int i = 0; i < path.length; i++) {
			path[i] = -1;
		}
		for (int idx = 0; idx <= pos; idx++) {
			MDDVariable var = ddmanager.getNodeVariable(indices[idx]);
			int i = ddmanager.getVariableIndex(var);
			path[i] = values[idx];
		}
	}

	/**
	 * Find the next leaf in this MDD and store the path in the provided array.
	 */
	public int findNextLeaf() {
		if (pos < 0) {
			throw new RuntimeException("findNext called after exploration is finished");
		}

		int node = indices[pos];
		if (ddmanager.isleaf(node)) {
			throw new RuntimeException("findNext went too far");
		}

		int curValue = values[pos] + 1;

		MDDVariable var = ddmanager.getNodeVariable(node);
		if (curValue < var.nbval) {
			values[pos]++;
			int next = ddmanager.getChild(node, curValue);
			if (ddmanager.isleaf(next)) {
				return next;
			}
			pos++;
			indices[pos] = next;
			values[pos] = -1;
		} else {
			pos--;
			if (pos < 0) {
				return -1;
			}
		}
		return findNextLeaf();
	}
}