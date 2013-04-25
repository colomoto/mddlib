package org.colomoto.mddlib;

import java.util.ArrayList;
import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.PathSearcher;
import org.colomoto.mddlib.internal.MDDStoreImpl;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Rough test case for MDD creation, duplicate detection and usage counter.
 * 
 * @author Aurelien Naldi
 */
public class TestMDD extends TestCase {

	@Test
	public void testConstruction() {
		MDDManager ddmanager = getSimpleManager(5);
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

	@Test
	public void testOrderProxy() {
		MDDManager manager = getSimpleManager(5);
		MDDVariable[] variables = manager.getAllVariables();
		
		List<String> keys2 = new ArrayList<String>();
		int[] altOrder = {3,1,4,0,2};
		for (int i: altOrder) {
			keys2.add( (String)variables[i].key );
		}
		
		MDDManager pManager = manager.getManager(keys2);

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

		
		// stress path searcher
		ps.setNode(1);
		boolean first = true;
		for (int l: ps) {
			if (!first) {
				fail("Should get only one path");
			}
			
			for (int v: ps.getPath()) {
				assertEquals(-1, v);
			}
			first = false;
		}
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
	
	@Test
	public void testInferSign() {
		MDDManager ddmanager = getSimpleManager(5);
		MDDVariable[] variables = ddmanager.getAllVariables();
		
		int n1 = variables[4].getNode(0, 1);
		int n2 = variables[4].getNode(1, 0);
		int n3 = variables[2].getNode(n1, n2);

		
		assertEquals(VariableEffect.NONE, ddmanager.getVariableEffect(variables[0], 0));
		assertEquals(VariableEffect.NONE, ddmanager.getVariableEffect(variables[0], n1));
		assertEquals(VariableEffect.NONE, ddmanager.getVariableEffect(variables[0], n2));
		assertEquals(VariableEffect.NONE, ddmanager.getVariableEffect(variables[0], n3));
		
		assertEquals(VariableEffect.POSITIVE, ddmanager.getVariableEffect(variables[4], n1));
		assertEquals(VariableEffect.NEGATIVE, ddmanager.getVariableEffect(variables[4], n2));
		assertEquals(VariableEffect.DUAL, ddmanager.getVariableEffect(variables[4], n3));
		assertEquals(VariableEffect.DUAL, ddmanager.getVariableEffect(variables[2], n3));
		assertEquals(VariableEffect.NONE, ddmanager.getVariableEffect(variables[3], n3));
		
	}
	
	@Test
	public void testIntervalPathSearcher() {
		MDDVariableFactory varFactory = new MDDVariableFactory();
		for (int i = 0; i < 5; i++) {
			varFactory.add("var" + i, (byte)3);
		}
		MDDManager ddmanager = MDDManagerFactory.getManager( varFactory, 10);
		MDDVariable[] variables = ddmanager.getAllVariables();
		
		int n1 = variables[4].getNode(new int[]{0, 0, 1});
		int n2 = variables[4].getNode(new int[]{1, 0, 0});
		int n3 = variables[2].getNode(new int[]{1, n1, n2});

		PathSearcher ps = new PathSearcher(ddmanager, true);
		checkPath(ps, n3, 	new int[][] { { -1, -1,  0, -1,  -1},  { -1, -1,  1, -1,  0}, { -1, -1,  1, -1,  2}, { -1, -1,  2, -1,  0}, { -1, -1,  2, -1,  1},});
	}
	
	@Test
	public void testMultivaluedNot() {
		MDDVariableFactory varFactory = new MDDVariableFactory();
		for (int i = 0; i < 5; i++) {
			varFactory.add("var" + i, (byte)3);
		}
		MDDManager ddmanager = MDDManagerFactory.getManager( varFactory, 10);
		MDDVariable[] variables = ddmanager.getAllVariables();
		
		int n1 = variables[4].getNode(new int[]{0, 0, 1});
		int n2 = variables[4].getNode(new int[]{1, 0, 0});
		int n3 = variables[2].getNode(new int[]{1, n1, n2});

		int n4 = ddmanager.not(n1);
		n4 = ddmanager.not(n4);
		assertEquals(n1, n4);

		n4 = ddmanager.not(n2);
		n4 = ddmanager.not(n4);
		assertEquals(n2, n4);

		n4 = ddmanager.not(n3);
		n4 = ddmanager.not(n4);
		assertEquals(n3, n4);
	}
	
	public static MDDManager getSimpleManager(int size) {
		List<String> keys = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			keys.add("var" + i);
		}
		return MDDManagerFactory.getManager( keys, 10);
	}
}
