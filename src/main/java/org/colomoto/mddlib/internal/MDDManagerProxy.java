package org.colomoto.mddlib.internal;

import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.NodeRelation;

/**
 * MDDManager adding a custom order on top of an existing MDDStore.
 * It acts mostly as proxy to the actual store but provides a uniform API.
 * It should be created through the <code>getProxy</code> method.
 * For convenience, MDDManager offers a wrapper method: <code>getManager()</code>.
 * 
 * @author Aurelien Naldi
 */
public class MDDManagerProxy implements MDDManager {
	
	private final MDDStore store;
	private final int[] factory2custom, custom2factory;
	private final MDDVariable[] variables;
	
	
	public static MDDManager getProxy(MDDStore store, List<?> customOrder) {
		MDDVariable[] rawVariables = store.getAllVariables();
		
		// build order mapping
		boolean sameOrder = true;
		int[] custom2factory = new int[rawVariables.length];
		for (int i=0 ; i<custom2factory.length ; i++) {
			custom2factory[i] = -1;
		}
		int i = 0;
		for (Object v: customOrder) {
			MDDVariable var = store.getVariableForKey(v);
			if (var.order != i) {
				sameOrder = false;
			}
			custom2factory[var.order] = i;
			i++;
		}
		
		if (sameOrder) {
			// no order mapping is needed
			return store;
		}
		
		// save the order mapping and compute the reverse one
		int[] factory2custom = new int[customOrder.size()];
		i=0;
		for (int k: factory2custom) {
			if (k >= 0) {
				factory2custom[k] = i;
			}
			i++;
		}
		return new MDDManagerProxy(store, custom2factory, factory2custom);
	}
	
	private MDDManagerProxy(MDDStore store, int[] custom2factory, int[] factory2custom) {
		this.store = store;
		this.custom2factory = custom2factory;
		this.factory2custom = factory2custom;
		this.variables = new MDDVariable[custom2factory.length];
		
		MDDVariable[] storeVars = store.getAllVariables();
		int i=0;
		for (int j: custom2factory) {
			variables[i] = storeVars[j];
			i++;
		}
	}
	
	@Override
	public byte reach(int node, byte[] values) {
		return store.reach(node, values, custom2factory);
	}

	@Override
	public MDDVariable getVariableForKey(Object key) {
		MDDVariable var = store.getVariableForKey(key);
		int idx = factory2custom[var.order];
		if (idx < 0) {
			// this variable is not is the custom order
			return null;
		}
		return var;
	}

	@Override
	public int getVariableIndex(MDDVariable var) {
		return factory2custom[var.order];
	}

	@Override
	public MDDVariable[] getAllVariables() {
		return variables;
	}

	
	/* **************** Pure proxy ************************* */
	
	@Override
	public MDDManager getManager(List<?> order) {
		return store.getManager(order);
	}

	@Override
	public MDDVariable getNodeVariable(int n) {
		return store.getNodeVariable(n);
	}

	@Override
	public void free(int pos) {
		store.free(pos);
	}

	@Override
	public int use(int node) {
		return store.use(node);
	}

	@Override
	public boolean isleaf(int node) {
		return store.isleaf(node);
	}

	@Override
	public int getChild(int node, int value) {
		return store.getChild(node, value);
	}

	@Override
	public int[] getChildren(int node) {
		return store.getChildren(node);
	}

	@Override
	public int not(int node) {
		return store.not(node);
	}

	@Override
	public NodeRelation getRelation(int first, int other) {
		return store.getRelation(first, other);
	}

	@Override
	public int getNodeCount() {
		return store.getNodeCount();
	}

	@Override
	public int getSign(int node, MDDVariable pivot) {
		return store.getSign(node, pivot);
	}
}
