package fr.univmrs.tagc.javaMDD.operators;

import fr.univmrs.tagc.javaMDD.MDDFactory;
import fr.univmrs.tagc.javaMDD.MDDOperator;
import fr.univmrs.tagc.javaMDD.NodeRelation;

/**
 * Collection of classical MDDOperators.
 * 
 * @author Aurelien Naldi
 */
public class MDDBaseOperators {

	/**
	 * AND operator.
	 */
	public static final MDDOperator AND = new MDDAndOperator();
	public static final MDDOperator OR = new MDDOrOperator();
	
	private MDDBaseOperators() {
		// no instance of this class
	}
}

/**
 * MDDOperator implementation for the "AND" operation.
 */
class MDDAndOperator extends AbstractOperator {

	protected MDDAndOperator() {
		super(true);
	}

	@Override
	public int combine(MDDFactory f, int first, int other) {
		if (first == other) {
			return first;
		}
		NodeRelation status = f.getRelation(first, other);

		switch (status) {
		case LN:
		case LL:
			if (f.getLeafValue(first) < 1) {
				// no need to "use" it: it is a leaf
				return first;
			}
			return f.use(other);
		case NL:
			if (f.getLeafValue(other) < 1) {
				// no need to "use" it: it is a leaf
				return other;
			}
			return f.use(first);
		default:
			return recurse(f, status, first, other);
		}
	}

	@Override
	public int recurse_multiple(MDDFactory factory, int[] nodes, int leafcount, int minlevel) {
		for (int i=0 ; i<leafcount ; i++) {
			if (nodes[i] <= 0) {
				return 0;
			}
		}
		nodes = prune_start(nodes, leafcount);
		return super.recurse_multiple(factory, nodes, 0, minlevel);
	}

	@Override
	protected int multiple_leaves(MDDFactory f, int[] leaves) {
		for (int i:leaves) {
			if (i<1) {
				return i;
			}
		}
		return leaves[0];
	}
}


/**
 * MDDOperator implementation for the "OR" operation.
 */
class MDDOrOperator extends AbstractOperator {

	protected MDDOrOperator() {
		super(true);
	}
	
	@Override
	public int combine(MDDFactory f, int first, int other) {
		if (first == other) {
			return first;
		}
		NodeRelation status = f.getRelation(first, other);

		switch (status) {
		case LN:
		case LL:
			if (f.getLeafValue(first) > 0) {
				return first;
			}
			return f.use(other);
		case NL:
			if (f.getLeafValue(other) > 0) {
				return other;
			}
			return f.use(first);
		default:
			return recurse(f, status, first, other);
		}
	}
	
	@Override
	public int recurse_multiple(MDDFactory factory, int[] nodes, int leafcount, int minlevel) {
		for (int i=0 ; i<leafcount ; i++) {
			if (nodes[i] > 0) {
				return 1;
			}
		}
		nodes = prune_start(nodes, leafcount);
		return super.recurse_multiple(factory, nodes, 0, minlevel);
	}
	
	@Override
	protected int multiple_leaves(MDDFactory f, int[] leaves) {
		for (int i:leaves) {
			if (i>0) {
				return i;
			}
		}
		return leaves[0];
	}
}
