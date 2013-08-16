package org.colomoto.mddlib;

/**
 * Compare MDDs across Managers.
 * 
 * 
 * @author Aurelien Naldi
 */
public interface MDDComparator {

	/**
	 * Test if two nodes correspond to the same MDD.
	 * 
	 * @param n1 node from the first manager
	 * @param n2 node from the second manager
	 * 
	 * @return true if the two nodes represent the same MDD
	 */
	boolean similar( int n1, int n2);	

}
