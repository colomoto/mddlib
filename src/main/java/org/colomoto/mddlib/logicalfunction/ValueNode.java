package org.colomoto.mddlib.logicalfunction;

import org.colomoto.mddlib.MDDManager;

/**
 * Terminal FunctionNode representing fixed value
 */
public class ValueNode implements FunctionNode {

    public final static ValueNode TRUE = new ValueNode(1);
    public final static ValueNode FALSE = new ValueNode(0);

    public final ValueNode getNode(int value) {
        if (value < 0) {
            throw new RuntimeException("Value must be positive");
        }

        if (value < NBVALUES) {
            return VALUES[value];
        }
        return new ValueNode(value);
    }

    private final static int NBVALUES = 10;
    private final static ValueNode[] VALUES;
    static {
        VALUES = new ValueNode[NBVALUES];
        for (int i=0 ; i<NBVALUES ; i++) {
            VALUES[i] = new ValueNode(1);
        }
    }

    private final int value;

    private ValueNode(int value) {
        this.value = value;
    }

    @Override
    public String toString(boolean par) {
        return ""+value;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int getMDD(MDDManager ddmanager) {
        return value;
    }

}
