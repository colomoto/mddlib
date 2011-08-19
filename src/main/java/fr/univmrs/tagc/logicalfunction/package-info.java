/**
 * MDD-aware data-structure for logical functions.
 * 
 * A logical function is represented as a tree, where the leaves are operands (i.e. variables)
 * and intermediate nodes are logical operations (AND, OR, NOT, other can be added).
 * <p>
 * Nodes (operands and operations) in this tree must implement the <code>BooleanNode</code> interface.
 * This interface allows logical functions to be represented as decision diagrams (for evaluation).
 * <p>
 * Logical functions can be built directly by assembling BooleanNode objects, by parsing a text
 * representing the function. For flexibility, the parser is an abstract class and implementors
 * are responsible for creating operands based on their text representation. 
 */
package fr.univmrs.tagc.logicalfunction;
