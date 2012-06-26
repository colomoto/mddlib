package org.colomoto.mddlib.operators;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDOperator;
import org.colomoto.mddlib.NodeRelation;

/**
 * Update a MDD by overwriting some of its leaves according to
 * another MDD.
 * 
 * @author Aurélien Naldi
 */
public class OverwriteOperator extends AbstractFlexibleOperator {
	
	static final MDDOperator[] ACTIONS_OVERWRITE = {
		new OverwriteOperator(0),
		new OverwriteOperator(1),
		new OverwriteOperator(2),
	};

	public static MDDOperator getOverwriteAction(int value) {
		if (value >= 0 && value < ACTIONS_OVERWRITE.length) {
			return ACTIONS_OVERWRITE[value];
		}
		return new OverwriteOperator(value);
	}
	
	int value;
	
	OverwriteOperator(int value) {
		super(MergeAction.CUSTOM);
		setAction(NodeRelation.NL, MergeAction.CUSTOM);
		this.value = value;
		lock();
	}

	@Override
	public int custom(MDDManager ddmanager, NodeRelation type, int first, int other) {
		switch (type) {
			case LL:
			case NL:
				if (other > 0) {
					return ddmanager.use(other);
				}
				return ddmanager.use(first);
		}
		System.out.println("DEBUG: AND ask should not come here!");
		return first;
	}
}
