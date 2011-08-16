package fr.univmrs.tagc.logicalfunction;

import junit.framework.TestCase;
import fr.univmrs.tagc.javaMDD.MDDFactory;

/**
 * Simple test suite for the logical function parser.
 * 
 * @author Aurelien Naldi
 */
public class TestLogicalFunctionParser extends TestCase {

	public void testLogicalFunction() {
		String[] operands = new String[] {"var1", "var2", "var3", "var4", "var5", "var6", "var7"};
		SampleParser<String> parser = new SampleParser<String>(operands);
		
		debug(parser, "var1", 1, 1);
		debug(parser, "!var1", 1, 1);
		debug(parser, "var1 & (var2 & var3)", 3, 1);
		debug(parser, "var1 | var5", 2, 2);
		debug(parser, "var1 & (var2 & var3) & var4 | var5", 5, 5);
		debug(parser, "var1 & (var2 & var3) & var4 | !var5", 5, 5);
		debug(parser, "var1 & (var2 & var3) & var4 | var5 | (var6 & var7)", 7, 9);
	}

	public static void debug(SampleParser<String> parser, String function, int nodeCount, int solutionCount) {
		BooleanNode f = parser.compile(function);
		MDDFactory factory = parser.getMDDFactory();
		int node = f.getMDD(factory);

//		System.out.println("------------------\n"+function);
//		factory.print(node);
		assertEquals(nodeCount, factory.getNodeCount());
		assertEquals(solutionCount, factory.count_positive_path(node));
		factory.free(node);
		assertEquals(0, factory.getNodeCount());
	}
}
