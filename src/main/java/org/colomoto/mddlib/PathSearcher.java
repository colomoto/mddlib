package org.colomoto.mddlib;

import java.util.Iterator;
import java.util.NoSuchElementException;

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
	private final MDDManager ddmanager;
	private final int[] path;
	private final int[] max;
	
	private int node;
	
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
	 * Create a new path searcher accepting any value (negative leaves are not
	 * allowed) and detecting intervals
	 * 
	 * @param ddmanager The MDD manager in which this search works.
	 * @param detectIntervals
	 */
	public PathSearcher(MDDManager ddmanager, boolean detectIntervals) {
		this(ddmanager, 0, Integer.MAX_VALUE, detectIntervals);
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
	 * Create a new path searcher, with a single value and detecting intervals
	 * 
	 * @param ddmanager
	 *            The MDD manager in which this search works.
	 * @param value
	 *            The value of the reached leaf
	 */
	public PathSearcher(MDDManager ddmanager, int value, boolean detectIntervals) {
		this(ddmanager, value, value, detectIntervals);
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
		this(ddmanager, minvalue, maxvalue, false);
	}
	
	public PathSearcher(MDDManager ddmanager, int minvalue, int maxvalue, boolean detectIntervals) {
		this.minvalue = minvalue;
		this.maxvalue = maxvalue;
		this.ddmanager = ddmanager;
		
		path = new int[ddmanager.getAllVariables().length];
		
		if (detectIntervals) {
			max = new int[path.length];
		} else {
			max = null;
		}
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
		this.node = node;
		return getPath();
	}

	/**
	 * Get the int[] used to store the found path.
	 * This returns the same array as setNode(int) and is only provided as convenience
	 * when you obtained a pre-configured searcher.
	 * 
	 * @return a reusable array denoting a path to a valid leaf or null if no
	 *         more leaf are found
	 */
	public int[] getPath() {
		return path;
	}
	
	/**
	 * Get the int[] used to store max values if this searcher detects intervals.
	 * 
	 * @return a reusable array in which intervals will be stored, or null if the searcher does not support it.
	 */
	public int[] getMax() {
		if (max == null) {
			throw new RuntimeException("This path searcher does not support intervals");
		}
		return max;
	}
	
	@Override
	public Iterator<Integer> iterator() {
		if (ddmanager.isleaf(node)) {
			if (node >= minvalue && node <= maxvalue) {
				for (int i=0 ; i<path.length ; i++) {
					path[i] = -1;
				}
				if (max != null) {
					for (int i=0 ; i<max.length ; i++) {
						max[i] = -1;
					}
				}
				return new SingleLeafIterator(node);
			}
			
			return EmptyIterator.EMPTYITERATOR;
		}
		
		if (max == null) {
			return new PathFoundIterator(ddmanager, node, path, minvalue, maxvalue);
		}
		
		return new PathFoundIterator(ddmanager, node, path, max, minvalue, maxvalue);
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

class EmptyIterator implements Iterator<Integer> {

	public static final EmptyIterator EMPTYITERATOR = new EmptyIterator();

	private EmptyIterator() {
		// private constructor: single instance class
	}
	
	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Integer next() {
		throw new NoSuchElementException();
	}

	@Override
	public void remove() {
	}
}

class SingleLeafIterator implements Iterator<Integer> {
	private int leaf;

	public SingleLeafIterator(int node) {
		this.leaf = node;
	}

	@Override
	public boolean hasNext() {
		return leaf >= 0;
	}

	@Override
	public Integer next() {
		if (leaf < 0) {
			throw new NoSuchElementException();
		}
		int ret = leaf;
		leaf = -1;
		return ret;
	}

	@Override
	public void remove() {
	}
}


class PathFoundIterator implements Iterator<Integer> {
	private final PathBacktrack backtrack;
	private final int[] path;
	private final int[] tmax;
	private final int minvalue, maxvalue;
	
	private int leaf;

	public PathFoundIterator(MDDManager ddmanager, int node, int[] path, int min, int max) {
		this(ddmanager, node, path, null, min, max);
	}
	public PathFoundIterator(MDDManager ddmanager, int node, int[] path, int[] tmax, int min, int max) {
		this.path = path;
		this.minvalue = min;
		this.maxvalue = max;
		this.tmax = tmax;

		this.backtrack = new PathBacktrack(ddmanager);
		backtrack.reset(node);
		
		leaf = getNextLeaf(tmax != null);
	}

	@Override
	public boolean hasNext() {
		return leaf >= 0;
	}

	@Override
	public Integer next() {
		if (leaf < 0) {
			throw new NoSuchElementException();
		}
		
		int ret = leaf;
		
		if (tmax == null) {
			backtrack.fillPath(path);
			leaf = getNextLeaf(false);
		} else {
			backtrack.fillPathAndMax(path, tmax);
			leaf = getNextLeaf(true);
		}
		
		return ret;
	}

	@Override
	public void remove() {
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
	public int getNextLeaf(boolean intervals) {
		while (leaf >= 0) {
			if (intervals) {
				leaf = backtrack.findNextLeafMaxVersion();
			} else {
				leaf = backtrack.findNextLeaf();
			}
			if (leaf >= minvalue && leaf <= maxvalue) {
				return leaf;
			}
		}
		return -1;
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

	public void fillPathAndMax(int[] path, int[] tmax) {
		for (int i = 0; i < path.length; i++) {
			path[i] = -1;
			tmax[i] = -1;
		}
		for (int idx = 0; idx <= pos; idx++) {
			int mdd = indices[idx];
			int value = values[idx];
			int child = ddmanager.getChild(mdd, value);
			MDDVariable var = ddmanager.getNodeVariable(mdd);

			// lookup the max value with the same child
			int max = value;
			int absolutemax = var.nbval-1;
			while ( max < absolutemax) {
				int nextvalue = max+1;
				if (ddmanager.getChild(mdd, nextvalue) != child) {
					break;
				}
				max = nextvalue;
			}
			if (max > value && max >= absolutemax) {
				max = -1;
			}

			int i = ddmanager.getVariableIndex(var);
			path[i] = values[idx];
			tmax[i] = max;
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
	
	public int findNextLeafMaxVersion() {
		if (pos < 0) {
			throw new RuntimeException("findNext called after exploration is finished");
		}

		int node = indices[pos];
		if (ddmanager.isleaf(node)) {
			throw new RuntimeException("findNext went too far");
		}
		
		int curValue = values[pos];
		MDDVariable var = ddmanager.getNodeVariable(node);
		if (curValue >= 0) {
			int child = ddmanager.getChild(node, curValue);


			for ( curValue++ ; curValue<var.nbval ; curValue++) {
				int curChild = ddmanager.getChild(node, curValue);
				if (curChild != child) {
					break;
				}
			}
		} else {
			curValue++;
		}
		
		if (curValue < var.nbval) {
			values[pos] = curValue;
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
		return findNextLeafMaxVersion();
	}
}