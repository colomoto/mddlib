package fr.univmrs.tagc.javaMDD;

import java.util.Iterator;

/**
 * Iterate over the paths in MDDs, optionally filtering on the leaf values.
 * <p>
 * It provides an integer array defining the path (reused for each path) and
 * returns the leaf value for each path found. In the path representation, some
 * variables may have undefined values, meaning that their value does not change
 * the reached leaf.
 * <p>
 * It is Iterable searcher, so you can use a for loop on it.
 * Note that the bundled iterator will fill the path automatically, so that you do not have to call
 * <code>fillPath</code> explicitly but you will have to do it when using the raw searcher.
 * <p>
 * The same path searcher can be reused for different MDDs.
 * 
 * @author Aurelien Naldi
 */
public class PathSearcher implements Iterable<Integer> {

	private final int minvalue, maxvalue;

	PathBacktrack backtrack;
	private int[] path;
	private int leaf;

	/**
	 * Create a new path searcher accepting any value (negative leaves are not
	 * allowed)
	 */
	public PathSearcher() {
		this(0, Integer.MAX_VALUE);
	}

	/**
	 * Create a new path searcher, with a single value.
	 * 
	 * @param value
	 *            value of the reached leaf
	 */
	public PathSearcher(int value) {
		this(value, value);
	}

	/**
	 * Create a new path searcher, with a value range.
	 * 
	 * @param minvalue
	 *            minimal value of the reached leaves
	 * @param maxvalue
	 *            maximal value of the reached leaves
	 */
	public PathSearcher(int minvalue, int maxvalue) {
		this.minvalue = minvalue;
		this.maxvalue = maxvalue;
	}

	/**
	 * Start looking up path for a new node.
	 * <p>
	 * For performance reasons, the same array will be reused when searching for
	 * the next path, copy its content if you need to keep it.
	 * 
	 * @param factory
	 *            the MDDFactory in which the node is stored
	 * @param node
	 *            the node index
	 * 
	 * @return a reusable array denoting a path to a valid leaf or null if no
	 *         more leaf are found
	 */
	public int[] setNode(MDDFactory factory, int node) {
		backtrack = new PathBacktrack(factory);
		path = new int[factory.getNbVariables()];

		return setNode(node);
	}

	/**
	 * Start looking up path for a new node, keeping the previous factory.
	 * <p>
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
		if (this.backtrack == null) {
			throw new RuntimeException("Calling setNode() without a factory");
		}

		// reset data structure
		backtrack.reset(node);
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

	final MultiValuedVariable[] variables;
	final MDDFactory factory;
	final int[] indices, values;

	public int pos;

	public PathBacktrack(MDDFactory factory) {
		this.factory = factory;
		this.variables = factory.getVariables();
		this.indices = new int[variables.length];
		this.values = new int[variables.length];
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
			int i = factory.getLevel(indices[idx]);
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
		if (factory.isleaf(node)) {
			throw new RuntimeException("findNext went too far");
		}

		int curValue = values[pos] + 1;

		MultiValuedVariable var = variables[factory.getLevel(node)];
		if (curValue < var.nbval) {
			values[pos]++;
			int next = factory.getChild(node, curValue);
			if (factory.isleaf(next)) {
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