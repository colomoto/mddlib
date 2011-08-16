package fr.univmrs.tagc.javaMDD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a (group of) path leading to the same leaf (i.e. value) in a MDD.
 * Some variables have undefined values, meaning that their value does not change the reached leaf.
 * <p>
 * FIXME: not sure anymore whether it actually works...
 * 
 * @author Aurelien Naldi
 */
public class LeafPath {
	public final int[] path;
	public final int value;
	
	/**
	 * Create a new path description.
	 * 
	 * @param path variable values along this path
	 * @param value value of the reached leaf
	 */
	public LeafPath(int[] path, int value) {
		this.path = path;
		this.value = value;
	}
	
	/**
	 * @param factory
	 * @param idx
	 * 
	 * @return an iterator for all LeafPath objects that can be extracted from this MDD
	 */
	public static Iterator<LeafPath> getLeafIterator(MDDFactory factory, int idx) {
		return getLeafIterator(factory, idx, Integer.MIN_VALUE);
	}
	
	public static Iterator<LeafPath> getLeafIterator(MDDFactory factory, int idx, int value) {
		if (factory.isleaf(idx)) {
			LeafPath p = new LeafPath(null, idx);
			List<LeafPath> l = new ArrayList<LeafPath>();
			l.add(p);
			return l.iterator();
		}
		return new LeafIterator(factory, idx, value);
	}
	
	public String toString() {
		String s = value + ": ";
		for (int i: path) {
			if (i<0) {
				s += " "+i;
			} else {
				s += "  "+i;
			}
		}
		return s;
	}
}


class LeafIterator implements Iterator<LeafPath> {
	
	int[] indices, values;
	int[] tracking;
	MDDFactory factory;
	int requiredValue;
	
	public LeafIterator(MDDFactory factory, int node) {
		this(factory, node, Integer.MIN_VALUE);
	}
	public LeafIterator(MDDFactory factory, int node, int reqValue) {
		this.factory = factory;
		this.requiredValue = reqValue;
		indices = new int[factory.getNbVariables()];
		values  = new int[factory.getNbVariables()];
		for (int i=0 ; i<indices.length ; i++) {
			indices[i] = -1;
			values[i]  = -1;
		}
		tracking = new int[] {0, 0};
		indices[0] = node;
		values[0]  = -1;
		findNext();
	}
	
	public boolean hasNext() {
		return tracking[0] >= 0;
	}
	
	public LeafPath next() {
		if (!hasNext()) {
			return null;
		}
		
		int[] path = new int[indices.length];
		for (int i=0 ; i<indices.length ; i++) {
			path[i] = -1;
		}
		for (int idx=0 ; idx<=tracking[0] ; idx++) {
			int i = factory.getLevel(indices[idx]);
			path[i] = values[idx];
		}
		LeafPath ret = new LeafPath(path, tracking[1]);
		findNext();
		return ret;
	}
	
	private void findNext() {
		do {
			factory.findNextLeaf(indices, values, tracking);
		} while (hasNext() && requiredValue != Integer.MIN_VALUE && requiredValue != tracking[1]);
	}
	
	public void remove() {
	}
}
