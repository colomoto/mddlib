package org.colomoto.mddlib.internal;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;

abstract public class MDDManagerAbstract implements MDDManager {

	@Override
	public int getSign(int node, MDDVariable pivot) {
		return getSign(node, pivot, 0);
	}
	
	private int getSign(int node, MDDVariable pivot, int curSign) {
		if (isleaf(node)) {
			return curSign;
		}

		MDDVariable var = getNodeVariable(node);
		if (var.order < pivot.order) {
			// recursive call
			for (int i=0 ; i<var.nbval ; i++) {
				curSign = getSign(getChild(node, i), pivot, curSign);
			}
		} else if (var == pivot) {
			for (int i=1 ; i<var.nbval ; i++) {
				curSign = getSign_sub(getChild(node, i-1), getChild(node, i), curSign);
			}
		}
		return curSign;
	}
	
	private int getSign_sub(int n1, int n2, int curSign) {
		if (n1 == n2) {
			return curSign;
		}
		int nbval;
		switch (getRelation(n1, n2)) {
		case LL:
			// make the choice!
			if (n1 > n2) {
				switch (curSign) {
				case 0:
					curSign = -1;
					break;
				case 1:
					curSign = 2;
					break;
				}
			} else if (n1 < n2) {
				switch (curSign) {
				case 0:
					curSign = 1;
					break;
				case -1:
					curSign = 2;
					break;
				}
			}
			break;
		case LN:
		case NNf:
			nbval = getNodeVariable(n2).nbval;
			for (int i=0 ; i<nbval ; i++) {
				curSign = getSign_sub(n1, getChild(n2, i), curSign);
			}
			break;
		case NL:
		case NNn:
			nbval = getNodeVariable(n1).nbval;
			for (int i=0 ; i<nbval ; i++) {
				curSign = getSign_sub(getChild(n1, i), n2, curSign);
			}
			break;
		case NN:
			nbval = getNodeVariable(n1).nbval;
			for (int i=0 ; i<nbval ; i++) {
				curSign = getSign_sub(getChild(n1, i), getChild(n2, i), curSign);
			}
			break;
		}
		return curSign;
	}

}
