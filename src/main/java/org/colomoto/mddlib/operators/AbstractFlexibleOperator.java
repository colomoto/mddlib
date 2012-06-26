package org.colomoto.mddlib.operators;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.NodeRelation;

/**
 * Base class for quick prototyping of new complex operators.
 * <p>
 * The AbstractGenericOperator relies on AbstractOperator and adds a mapping between "status"
 * and a set of likely behaviours, including some hooks to quickly write custom operators
 * without bothering with "common" cases.
 * <p>
 * A status is the relationship between the nodes being merged
 * 
 * @author Aurelien Naldi
 */
abstract public class AbstractFlexibleOperator extends AbstractOperator {

	/**
	 * Supported actions while merging.
	 */
	protected enum MergeAction {
		/** just continue with the children */
		RECURSIVE,
		
		/** return the first node */
		THIS,
		/** return the second node */
		OTHER,
		
		/** return the node with the smaller value. Applies when bother are leaves. */
		MIN,
		/** return the node with the larger value. Applies when bother are leaves. */
		MAX,
		
		/** call <code>ask</code> to get the appropriate action */
		ASKME,
		/** call <code>custom</code> to get the return value */
		CUSTOM
	}

	private boolean locked = false;
	private final MergeAction[] t;
	

	
	/**
	 * Base (private) constructor:  define multiple merge and set all actions as recursive.
	 * 
	 * @param multiple_merges
	 */
	private AbstractFlexibleOperator (boolean multipleMerge) {
		super(multipleMerge);
		this.t = new MergeAction[NodeRelation.values().length];
		for (int i=0 ; i<t.length ; i++) {
			t[i] = MergeAction.RECURSIVE;
		}
	}

	/**
	 * Create a new operator
	 * <p>
	 * By default, only the action for two leaves have to be defined, all others are set to recursive.
	 * You can define additional actions by calling <code>setAction</code>.
	 * 
	 * @param A_LL
	 * @param multipleMerge
	 */
	public AbstractFlexibleOperator (MergeAction A_LL, boolean multipleMerge) {
		this(multipleMerge);
		setAction(NodeRelation.LL, A_LL);
	}

	/**
	 * Create a new operator.
	 * <p>
	 * By default, only the action for two leaves have to be defined, all others are set to recursive.
	 * You can define additional actions by calling <code>setAction</code>.
	 * 
	 * @param A_LL
	 */
	public AbstractFlexibleOperator (MergeAction A_LL) {
		this(A_LL, false);
	}
	
	/**
	 * Define the action applied for a given relation between the merged nodes.
	 * <p>
	 * Note: once the operation is locked, this method does nothing.
	 * It should be defined in the constructor and never change.
	 * <p>
	 * Override <code>ask</code> and/or <code>custom</code> if you plan to use the
	 * <code>ASKME</code> and <code>CUSTOM</code> actions.
	 * 
	 * @param rel
	 * @param action
	 */
	protected void setAction(NodeRelation rel, MergeAction action) {
		if (locked) {
			return;
		}
		t[rel.ordinal()] = action;
	}

	/**
	 * Lock the operator so that the actions can no more be changed.
	 * <p>
	 * This should be done at the end of the constructor.
	 */
	protected void lock() {
		this.locked = true;
	}

	@Override
	public int combine(MDDManager ddmanager, int first, int other) {
		// NOTE: all operations do not return a node when merging it with itself (stable states, xor, ...)
		NodeRelation status = ddmanager.getRelation(first, other);

		MergeAction action = t[status.ordinal()];
		if (action == MergeAction.ASKME) {
			action = ask(ddmanager, status, first, other);
		}
		switch (action) {
			case CUSTOM:
				return custom(ddmanager, status, first, other);
				
			case RECURSIVE:
				return recurse(ddmanager, status, first, other);
				
			case THIS:
				return ddmanager.use(first);
			case OTHER:
				return ddmanager.use(other);
			case MIN:
				if (first > other) {
					return ddmanager.use(other);
				}
				return ddmanager.use(first);
			case MAX:
				if (first > other) {
					return ddmanager.use(first);
				}
				return ddmanager.use(other);
		}
		return -1;
	}
	
	/**
	 * if some cases need more info to be tested, put the ASKME value in the array t
	 * and implement this complementary function.
	 *
	 * @param ddmanager
	 * @param status
	 * @param first value or level of the first node
	 * @param other value or level of the other node
	 * 
	 * @return the action to perform
	 */
	public MergeAction ask(MDDManager ddmanager, NodeRelation status, int first, int other) {
		return null;
	}

	/**
	 * put the CUSTOM value in t and implement this function to add more complex behaviours.
	 * Warning: it is responsible for dealing with usage counter: don't forget to
	 * call the "use" method if you return one of the existing nodes without going through
	 * get_bnode or get_mnode.
	 * @param ddmanager
	 * @param status
	 * @param first value or level of the first node
	 * @param other value or level of the other node
	 * 
	 * @return the resulting node index
	 */
	public int custom(MDDManager ddmanager, NodeRelation status, int first, int other) {
		return -1;
	}

}
