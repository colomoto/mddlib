package fr.univmrs.tagc.logicalfunction;

import fr.univmrs.tagc.javaMDD.MDDFactory;

/**
 * Common interface for all nodes in the trees representing logical functions.
 * 
 * @author Fabrice Lopez: initial implementation
 * @author Aurelien Naldi: adaptation
 */
public interface BooleanNode {

	/**
	 * @param par if true, add surrounding parenthesis.
	 * 
	 * @return a string representation of this logical function.
	 */
	public String toString(boolean par);
	
	/**
	 * Is it a leaf? Only used to help some toString methods.
	 * @return true if this node is a leaf
	 */
	public boolean isLeaf();
  
	/**
	 * Construct a MDD corresponding to this logical function.
	 * 
	 * @param factory the MDDFactory in which the MDD will be stored.
	 * @return the index of the corresponding MDD root.
	 */
	public int getMDD(MDDFactory factory);
	public int getMDD(MDDFactory factory, boolean reversed);
}
