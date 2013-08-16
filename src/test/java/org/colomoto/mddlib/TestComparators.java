package org.colomoto.mddlib;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class TestComparators {

	@Test
	public void test() {
		MDDManager ddm1 = TestMDD.getSimpleManager(5);
		MDDManager ddm2 = TestMDD.getSimpleManager(5);
		
		int n1 = getMDDExample1(ddm1);
		int m1 = getMDDExample2(ddm1);

		int m2 = getMDDExample2(ddm2);
		int n2 = getMDDExample1(ddm2);
		
		MDDComparator comparator = MDDComparatorFactory.getComparator(ddm1, ddm2);
		
		Assert.assertTrue(comparator.similar(n1, n2));
		Assert.assertTrue(comparator.similar(m1, m2));
		
		Assert.assertFalse(comparator.similar(n1, m2));
	}

	
	private int getMDDExample1(MDDManager ddmanager) {
		MDDVariable[] variables = ddmanager.getAllVariables();
		
		int c = 0;
		variables[4].getNode(0, 1);
		int n1 = variables[4].getNode(0, 1);
		int n2 = variables[3].getNode(0, 0);
		int node = variables[4].getNode(1, 0);
		int newnode = variables[4].getNode(node, node);
		newnode = variables[2].getNode(n1, n2);

		return newnode;
	}

	private int getMDDExample2(MDDManager ddmanager) {
		MDDVariable[] variables = ddmanager.getAllVariables();
		
		int c = 0;
		variables[3].getNode(0, 1);
		int n1 = variables[3].getNode(0, 1);
		int n2 = variables[1].getNode(0, 0);
		int node = variables[3].getNode(1, 0);
		int newnode = variables[3].getNode(node, node);
		newnode = variables[2].getNode(n1, n2);

		return newnode;
	}

}
