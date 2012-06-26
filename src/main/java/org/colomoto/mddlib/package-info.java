/**
 * Simple Multi-valued Decision Diagram (MDD) toolkit.
 * <p>
 * Given the definition of a set of multi-valued variables (represented as <code>MDDVariable</code>),
 * the <code>MDDManager</code> enables the efficient storage of logical functions depending on the value
 * of these variables.
 * The decision diagrams can also be "combined" using <code>MDDOperator</code>.
 * <p>
 * This toolkit main objective is ease of use, portability (pure java) and flexibility (especially the ability to
 * add custom operators). Better performance can be achieved using a native Binary Decision Diagram (BDD) toolkit.
 * <br> 
 * Variable reordering and similar optimisations are not implemented nor planned!
 * If you need them, consider using JavaBDD or another pure-BDD library. If you
 * know a similarly optimised pure-java MDD library, please let me know.
 * 
 * Simple support for custom variable orders is provided by Proxy implementations of MDDManager
 */
package org.colomoto.mddlib;
