package org.colomoto.mddlib.logicalfunction;

import java.util.ArrayList;
import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.PathSearcher;
import org.colomoto.mddlib.logicalfunction.FunctionNode;
import org.colomoto.mddlib.logicalfunction.FunctionParser;
import org.colomoto.mddlib.logicalfunction.OperandFactory;
import org.colomoto.mddlib.logicalfunction.SimpleOperandFactory;

import junit.framework.TestCase;

/**
 * Simple test suite for the logical function parser.
 * 
 * @author Aurelien Naldi
 */
public class TestLogicalFunctionParser extends TestCase {

	public void testLogicalFunction() {
		List<String> operands = new ArrayList<String>();
		for (int i=0 ; i<8 ; i++) {
			operands.add("var"+i);
		}
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
		MDDManager ddFactory = opFactory.getMDDFactory();
		int node = f.getMDD(ddFactory);

//		System.out.println("------------------\n"+function);
//		factory.print(node);
		assertEquals(nodeCount, ddFactory.getNodeCount());
		PathSearcher searcher = new PathSearcher(ddFactory, 1);
		searcher.setNode(node);
		assertEquals(solutionCount, searcher.countPaths());
		ddFactory.free(node);
		assertEquals(0, ddFactory.getNodeCount());
	}
}
