package org.colomoto.mddlib;

import java.util.ArrayList;
import java.util.List;

/**
 * Create comparators for MDDs across Managers.
 * 
 * @author Aurelien Naldi
 */
public class MDDComparatorFactory {

	/**
	 * Get a comparator for the two specified MDDManagers.
	 * 
	 * It will select appropriate MDDComparator implementation for these two managers.
	 *  
	 * @param ddm1
	 * @param ddm2
	 * 
	 * @return a MDDComparator which can handle these two managers.
	 */
	public static MDDComparator getComparator(MDDManager ddm1, MDDManager ddm2) {
		if (ddm1.isView(ddm2)) {
			System.out.println("Compare in the same store");
			return new IdenticalComparator();
		}
		
		List<MDDVariable[]> shared = getSharedVars(ddm1, ddm2);
		if (areCompatible(shared)) {
			return new CompatibleComparator(ddm1, ddm2);
		}
		
		return new HeavyComparator(ddm1, ddm2);
	}

	/**
	 * Get the list of variables shared between two MDD managers.
	 * 
	 * @param ddm1
	 * @param ddm2
	 * 
	 * @return the list of shared variables, sorted according to the first manager
	 */
	private static List<MDDVariable[]> getSharedVars(MDDManager ddm1, MDDManager ddm2) {
		
		// collect all variables shared between ddm1 and ddm2
		// (sorted according to their order in ddm1)
		List<MDDVariable[]> shared = new ArrayList<MDDVariable[]>();
		for (MDDVariable v1: ddm1.getAllVariables()) {
			MDDVariable v2 = ddm2.getVariableForKey(v1.key);
			if (v2 == null) {
				continue;
			}
			
			int pos = 0;
			for (MDDVariable[] orders: shared) {
				if (orders[0].order > v1.order) {
					break;
				}
				pos++;
			}
			shared.add(pos, new MDDVariable[] {v1, v2});
		}
		return shared;
	}

	/**
	 * Test if the variable shared by two MDD managers are compatible.
	 * 
	 * All variables shared in the two managers must have
	 * the same number of values and relative order.
	 * 
	 * @param shared the list of shared variables from the compared MDD managers
	 * 
	 * @return true if the variables are compatible
	 */
	private static boolean areCompatible(List<MDDVariable[]> shared) {
		// check that the order of the shared variables are compatible
		int o1=-1, o2=-1;
		for (MDDVariable[] orders: shared) {
			if (orders[0].nbval != orders[1].nbval) {
				return false;
			}

			if (orders[0].order <= o1 || orders[1].order <= o2) {
				return false;
			}
			o1 = orders[0].order;
			o2 = orders[1].order;
		}
		return true;
	}
}

/**
 * Compare MDDs from identical managers
 * 
 * @author Aurelien Naldi
 */
class IdenticalComparator implements MDDComparator {
	
	@Override
	public boolean similar( int n1, int n2) {
		return n1 == n2;
	}
}

/**
 * Compare MDDs from compatible managers: browse MDDs and check that the nodes are compatible.
 * 
 * @author Aurelien Naldi
 */
class CompatibleComparator implements MDDComparator {
		
	private final MDDManager ddm1, ddm2;

	public CompatibleComparator(MDDManager ddm1, MDDManager ddm2) {
		this.ddm1 = ddm1;
		this.ddm2 = ddm2;
	}

	/**
	 * Recursive implementation of the MDD comparison for compatible managers:
	 * check if two MDDs are identical. It returns true for
	 * leaves with the same value, or nodes with the same
	 * variable and equal children.
	 * 
	 * @param n1
	 * @param n2
	 * 
	 * @return true if n1 is identical to n2
	 */
	@Override
	public boolean similar( int n1, int n2) {

		MDDVariable v1 = ddm1.getNodeVariable(n1);
		MDDVariable v2 = ddm2.getNodeVariable(n2);

		if (v1 == null) {
			return v2 == null;
		}
		
		if (v2 == null) {
			return false;
		}
		
		if (!v1.equals(v2)) {
			return false;
		}
		
		for (int i=0 ; i<v1.nbval ; i++) {
			int c1 = ddm1.getChild(n1, i);
			int c2 = ddm2.getChild(n2, i);
			if (!similar(c1, c2)) {
				return false;
			}
		}

		return true;
	}
}

/**
 * Compare MDDs from any managers: heavy comparison!
 * 
 * @author Aurelien Naldi
 */
class HeavyComparator implements MDDComparator {
	
	private final MDDManager ddm1, ddm2;
	private final PathSearcher searcher;
	private final int[] pathMap;
	private final byte[] path2;

	public HeavyComparator(MDDManager ddm1, MDDManager ddm2) {
		this.ddm1 = ddm1;
		this.ddm2 = ddm2;
		this.searcher = new PathSearcher(ddm1);
		
		pathMap = new int[ddm1.getAllVariables().length];
		for (int i=0 ; i<pathMap.length ; i++) {
			pathMap[i] = -1;
		}
		int i=-1;
		for (MDDVariable v: ddm1.getAllVariables()) {
			i++;
			MDDVariable v2 = ddm2.getVariableForKey(v.key);
			if (v2 == null || v2.nbval != v.nbval) {
				continue;
			}
			
			int i2 = ddm2.getVariableIndex(v2);
			pathMap[i] = i2;
		}
		
		path2 = new byte[ddm2.getAllVariables().length];
	}

	/**
	 * Compare MDDs from managers with different orders
	 */
	@Override
	public boolean similar(int n1, int n2) {
		int[] path = searcher.setNode(n1);
		for (int value: searcher) {
			byte[] p2 = fillPath(path);
			if (p2 == null) {
				return false;
			}
			
			byte v2 = ddm2.groupReach(n2, p2);
			if (v2 != value) {
				return false;
			}
		}

		return true;
	}
	
	private byte[] fillPath(int[] p) {
		for (int i=0 ; i<p.length ; i++) {
			int v = p[i];
			int i2 = pathMap[i];
			
			if (v < 0 && i2 < 0) {
				continue;
			}
			
			if (i2 < 0) {
				return null;
			}
			
			path2[i2] = (byte)v;
		}
		
		return path2;
	}
}
