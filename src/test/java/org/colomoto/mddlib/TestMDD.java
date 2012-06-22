package org.colomoto.mddlib;

import java.util.ArrayList;
import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.PathSearcher;
import org.colomoto.mddlib.internal.MDDStoreImpl;

import junit.framework.TestCase;

public class TestMDD extends TestCase {

	public void testConstruction() {
		List<String> keys = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			keys.add("var" + i);
		}
		MDDManager factory = MDDManagerFactory.getManager( keys, 10);
		MDDVariable[] variables = factory.getAllVariables();
		
		int c = 0;
		assertEquals(c, factory.getNodeCount());

		variables[4].getNode(0, 1);
		c++;
		assertEquals(c, factory.getNodeCount());

		int n1 = variables[4].getNode(0, 1);
		assertEquals(c, factory.getNodeCount());

		int n2 = variables[3].getNode(0, 0);
		assertEquals(n2, 0);
		assertEquals(c, factory.getNodeCount());

		int node = variables[4].getNode(1, 0);
		c++;
		assertEquals(c, factory.getNodeCount());

		int newnode = variables[4].getNode(node, node);
		assertEquals(node, newnode);
		assertEquals(c, factory.getNodeCount());

		newnode = variables[2].getNode(n1, n2);
		c++;
		assertEquals(c, factory.getNodeCount());

		PathSearcher paths = new PathSearcher(factory);
		int[] path = paths.setNode(newnode);
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
