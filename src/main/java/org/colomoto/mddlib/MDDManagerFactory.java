package org.colomoto.mddlib;

import java.util.Collection;

import org.colomoto.mddlib.internal.MDDStoreImpl;

/**
 * Create MDDManager instances.
 * The creation of a MDDManager requires a list of variables:
 * it can be a standard list of objects, leading to Boolean variables,
 * or rely on a <code>MDDVariableFactory</code> to add multi-valued variables.
 * 
 * @author Aurelien Naldi
 */
public class MDDManagerFactory {

	/**
	 * Get a new MDDManager using a MDDVariableFactory.
	 * 
	 * @param vbuilder
	 * @param nbleaves
	 * @return a factory, which can include multi-valued variables.
	 */
	public static MDDManager getManager(MDDVariableFactory vbuilder, int nbleaves) {
		return new MDDStoreImpl(vbuilder, nbleaves);
	}
	
	/**
	 * Get a new MDDManager using a normal list of variables.
	 * 
	 * @param vbuilder
	 * @param nbleaves
	 * @return a factory, with Boolean variables
	 */
	public static MDDManager getManager(Collection<?> vbuilder, int nbleaves) {
		return new MDDStoreImpl(vbuilder, nbleaves);
	}

}
