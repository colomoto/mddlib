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
		SimpleOperandFactory<String> opFactory = new SimpleOperandFactory<String>(operands);
		FunctionParser parser = new FunctionParser();
		
		debug(parser, opFactory, "var1", 1, 1);
		debug(parser, opFactory, "!var1", 1, 1);
		debug(parser, opFactory, "var1 & (var2 & var3)", 3, 1);
		debug(parser, opFactory, "var1 | var5", 2, 2);
		debug(parser, opFactory, "var1 & (var2 & var3) & var4 | var5", 5, 5);
		debug(parser, opFactory, "var1 & (var2 & var3) & var4 | !var5", 5, 5);
		debug(parser, opFactory, "var1 & (var2 & var3) & var4 | var5 | (var6 & var7)", 7, 9);
	}

	public static void debug(FunctionParser parser, OperandFactory opFactory, String function, int nodeCount, int solutionCount) {
		FunctionNode f = parser.compile(opFactory, function);
		MDDFactory ddFactory = opFactory.getMDDFactory();
		int node = f.getMDD(ddFactory);

//		System.out.println("------------------\n"+function);
//		factory.print(node);
		assertEquals(nodeCount, ddFactory.getNodeCount());
		assertEquals(solutionCount, ddFactory.count_positive_path(node));
		ddFactory.free(node);
		assertEquals(0, ddFactory.getNodeCount());
	}
}
