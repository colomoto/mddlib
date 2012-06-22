package org.colomoto.mddlib;

import java.util.Collection;

import org.colomoto.mddlib.internal.MDDStoreImpl;

public class MDDManagerFactory {

	/**
	 * Get a new MDDManager.
	 * 
	 * 
	 * @param vbuilder
	 * @param nbleaves
	 * @return
	 */
	public static MDDManager getManager(MDDVariableFactory vbuilder, int nbleaves) {
		return new MDDStoreImpl(vbuilder, nbleaves);
	}
	
	
	public static MDDManager getManager(Collection<?> vbuilder, int nbleaves) {
		return new MDDStoreImpl(vbuilder, nbleaves);
	}

}
