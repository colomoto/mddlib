package org.colomoto.mddlib;

import java.util.ArrayList;
import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.PathSearcher;
import org.colomoto.mddlib.internal.MDDStoreImpl;

import junit.framework.TestCase;

/**
 * Rough test case for MDD creation, duplicate detection and usage counter.
 * 
 * @author Aurelien Naldi
 */
public class TestMDD extends TestCase {

	public void testConstruction() {
		List<String> keys = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			keys.add("var" + i);
		}
		MDDManager ddmanager = MDDManagerFactory.getManager( keys, 10);
		MDDVariable[] variables = ddmanager.getAllVariables();
		
		int c = 0;
		assertEquals(c, ddmanager.getNodeCount());

		variables[4].getNode(0, 1);
		c++;
		assertEquals(c, ddmanager.getNodeCount());

		int n1 = variables[4].getNode(0, 1);
		assertEquals(c, ddmanager.getNodeCount());

		int n2 = variables[3].getNode(0, 0);
		assertEquals(n2, 0);
		assertEquals(c, ddmanager.getNodeCount());

		int node = variables[4].getNode(1, 0);
		c++;
		assertEquals(c, ddmanager.getNodeCount());

		int newnode = variables[4].getNode(node, node);
		assertEquals(node, newnode);
		assertEquals(c, ddmanager.getNodeCount());

		newnode = variables[2].getNode(n1, n2);
		c++;
		assertEquals(c, ddmanager.getNodeCount());

		PathSearcher paths = new PathSearcher(ddmanager);
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

	public void testOrderProxy() {
		List<String> keys = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			keys.add("var" + i);
		}
		
		MDDManager manager = MDDManagerFactory.getManager( keys, 10);
		MDDVariable[] variables = manager.getAllVariables();
		
		List<String> keys2 = new ArrayList<String>();
		int[] altOrder = {3,1,4,0,2};
		for (int i: altOrder) {
			keys2.add( keys.get(i) );
		}
		
		MDDManager pManager = manager.getManager(keys2);
		MDDVariable[] pVariables = pManager.getAllVariables();

		// build two simple logical functions:
		//   var0 OR ( (NOT var2) AND var4 )
		//   NOT var1 OR ( (NOT var2) AND var4 )
		int node = variables[4].getNode(0, 1);
		node = variables[2].getNode(node, 0);
		int n1 = variables[1].getNode(1, node);
		node = variables[0].getNode(node, 1);
		
		
		// check simple evaluations on the two functions with two orders
		byte[] values = {0,0,1,0,0};
		assertEquals(0, manager.reach(node, values));
		assertEquals(1, pManager.reach(node, values));	// true as NOT var2 AND var4
		assertEquals(1, manager.reach(n1, values));		// true as NOT var1
		assertEquals(1, pManager.reach(n1, values));	// true as NOT var1

		values = new byte[] {0,1,0,0,1};
		assertEquals(1, manager.reach(node, values));	// true as NOT var2 AND var4
		assertEquals(0, pManager.reach(node, values));	
		assertEquals(1, manager.reach(n1, values));		// true as NOT var2 AND var4
		assertEquals(0, pManager.reach(n1, values));	

		values = new byte[] {1,1,1,0,0};
		assertEquals(1, manager.reach(node, values));	// true as NOT var0
		assertEquals(1, pManager.reach(node, values));	// true as NOT var2 AND var4
		assertEquals(0, manager.reach(n1, values));		
		assertEquals(1, pManager.reach(n1, values));	// true as NOT var2 AND var4

		
		// check the paths provided by PathSearch
		PathSearcher ps = new PathSearcher(manager, 1);
		PathSearcher ps2 = new PathSearcher(pManager, 1);

		// {3,1,4,0,2}
		
		checkPath(ps, node, 	new int[][] { { 0, -1,  0, -1,  1},  { 1, -1, -1, -1, -1} });
		checkPath(ps2, node, 	new int[][] { {-1, -1,  1,  0,  0},  {-1, -1, -1,  1, -1} });
		
		checkPath(ps, n1, 		new int[][] { {-1,  0, -1, -1, -1},  {-1,  1,  0, -1,  1} });
		checkPath(ps2, n1, 		new int[][] { {-1,  0, -1, -1, -1},  {-1,  1,  1, -1,  0} });
	}

	private void checkPath(PathSearcher ps, int node, int[][] expected) {
		int[] path = ps.setNode(node);
		int n=0;
		for (int v: ps) {
			// check it
			int[] curExpected = expected[n];
			for (int i=0 ; i<path.length ; i++) {
				assertEquals(curExpected[i], path[i]);
			}

			n++;
		}
	}
}
