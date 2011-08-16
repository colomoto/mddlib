package fr.univmrs.tagc.javaMDD;

import junit.framework.TestCase;


public class TestMDD extends TestCase {

	public void testConstruction() {
		int base, c;
		MDDVariable[] variables = new MDDVariable[5];
		for (int i=0 ; i<variables.length ; i++) {
			variables[i] = new MDDVariable("var"+i);
		}
		int[] leaves = {0,1};
		MDDFactory factory = new MDDFactory(variables, leaves);
		base = c = factory.getNodeCount();
		assertEquals(0, base);

		factory.get_bnode(4, 0, 1);
		c++;

		assertEquals(c, factory.getNodeCount());

		factory.get_bnode(4, 0, 1);
		assertEquals(c, factory.getNodeCount());

		factory.get_bnode(4, 0, 0);
		assertEquals(c, factory.getNodeCount());

		int node = factory.get_bnode(4, 1, 0);
		c++;
		assertEquals(c, factory.getNodeCount());

		int newnode = factory.get_bnode(4, node, node);
		assertEquals(node, newnode);
		assertEquals(c, factory.getNodeCount());
	}

}
