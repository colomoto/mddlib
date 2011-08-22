package fr.univmrs.tagc.javaMDD;

import junit.framework.TestCase;

public class TestMDD extends TestCase {

	public void testConstruction() {
		MultiValuedVariable[] variables = new MultiValuedVariable[5];
		for (int i = 0; i < variables.length; i++) {
			variables[i] = new MultiValuedVariable("var" + i);
		}
		MDDFactory factory = new MDDFactory(variables, 10);

		int c = 0;
		assertEquals(c, factory.getNodeCount());

		factory.get_bnode(4, 0, 1);
		c++;
		assertEquals(c, factory.getNodeCount());

		int n1 = factory.get_bnode(4, 0, 1);
		assertEquals(c, factory.getNodeCount());

		int n2 = factory.get_bnode(3, 0, 0);
		assertEquals(n2, 0);
		assertEquals(c, factory.getNodeCount());

		int node = factory.get_bnode(4, 1, 0);
		c++;
		assertEquals(c, factory.getNodeCount());

		int newnode = factory.get_bnode(4, node, node);
		assertEquals(node, newnode);
		assertEquals(c, factory.getNodeCount());

		newnode = factory.get_bnode(2, n1, n2);
		c++;
		assertEquals(c, factory.getNodeCount());

		PathSearcher paths = new PathSearcher();
		int[] path = paths.setNode(factory, newnode);
		int nbpaths = 0;
		for (int leaf : paths) {
			nbpaths++;
			assertEquals(-1, path[0]);
			assertEquals(-1, path[1]);
			assertEquals(-1, path[3]);
			switch (nbpaths) {
			case 1:
				assertEquals(0, path[2]);
				assertEquals(0, path[4]);
				break;

			case 2:
				assertEquals(0, path[2]);
				assertEquals(1, path[4]);
				break;
			case 3:
				assertEquals(1, path[2]);
				assertEquals(-1, path[4]);
				break;
			default:
				throw new RuntimeException("bad number of paths");
			}
		}
	}
}
