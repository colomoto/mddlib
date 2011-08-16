package fr.univmrs.tagc.javaMDD;

/**
 * List of possible relations between two nodes.
 * <p>
 * This is used during MDD combinations, to determine the "top" node
 * and help the operator to pick the right code path.
 */
public enum NodeRelation {

	/** two leaves */ 
	LL,

	/** a leaf and then a node */ 
	LN,

	/** a node and then a leaf */ 
	NL,

	/** two nodes of the same level */
	NN,

	/** two nodes of different levels, the second comes first */
	NNf,

	/** two nodes of different levels, the second comes next */
	NNn;
}
