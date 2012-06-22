package org.colomoto.mddlib.internal;

import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;

public class MDDManagerOrderingProxy {
	
	public final MDDStore factory;
	public final int[] factory2custom, custom2factory;
	
	public MDDManagerOrderingProxy(MDDStore factory, List<?> variables) {
		this.factory = factory;

		MDDVariable[] rawVariables = factory.getAllVariables();
		
		// build order mapping
		boolean sameOrder = true;
		int[] tmporder = new int[rawVariables.length];
		for (int i=0 ; i<tmporder.length ; i++) {
			tmporder[i] = -1;
		}
		int i = 0;
		for (Object v: variables) {
			MDDVariable var = factory.getVariableForKey(v);
			if (var.order != i) {
				sameOrder = false;
			}
			tmporder[var.order] = i;
			i++;
		}
		
		
		if (sameOrder) {
			// no order mapping is needed
			custom2factory = null;
			factory2custom = null;
		} else {
			// save the order mapping and compute the reverse one
			custom2factory = tmporder;
			factory2custom = new int[variables.size()];
			i=0;
			for (int k: factory2custom) {
				if (k >= 0) {
					factory2custom[k] = i;
				}
				i++;
			}
		}
	}
	
	public byte reach(int node, byte[] values) {
		return factory.reach(node, values, custom2factory);
	}
	
	
}
