package fr.univmrs.tagc.logicalfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Parser for logical functions.
 * <p>
 * This parser splits functions into operators and operands and
 * constructs a tree view of the function.
 * <p>
 * It delegates the creation of operands to an <code>OperandFactory</code> 
 * 
 * @author Fabrice Lopez: initial implementation in GINsim
 * @author Aurelien Naldi: adaptation to the MDDFactory and OperandFactory
 */
public class FunctionParser {
	
	final OperatorCollection operatorCollection;
	final String operatorsAndParenthesis;
	final List<String> operators;
	
	/**
	 * Create a parser using the default set of operators.
	 */
	public FunctionParser() {
		this(OperatorCollection.DEFAULT_OPERATORS);
	}

	/**
	 * Create a parser with a custom set of operators.
	 * 
	 * @param operatorCollection
	 */
	public FunctionParser(OperatorCollection operatorCollection) {
		this.operatorCollection = operatorCollection;
		operatorsAndParenthesis = operatorCollection.getRegex();
		operators = operatorCollection.getOperators();
	}
	
	/**
	 * Compile a logical function.
	 * 
	 * @param s the text form of the function
	 * @param opFactory   the factory used to create operands.
	 * @return the root of the tree representation of this function.
	 */
	public FunctionNode compile(OperandFactory opFactory, String s) {
	  int i, j, k;
	  String elem;
	  String[] split = s.split(operatorsAndParenthesis);
	  List<String> operands = new ArrayList<String>();
	  FunctionNode tbtn;

	  for (i = 0; i < split.length; i++) {
		  if (!split[i].equals("")) {
			  operands.add(split[i]);
		  }
	  }
	  if (!opFactory.verifOperandList(operands)) {
		  // the implementor is responsible to present auto-corrections... 
    	return null;
	  }

	  i = 0;
	  Stack<FunctionNode> operandStack = new Stack<FunctionNode>();
	  Stack<String> operatorStack = new Stack<String>();
	  while (i != s.length()) {
		  elem = readElement(operators, operands, s, i);
		  if (elem == null) {
			  return null;
		  } else if (operands.contains(elem)) {
			  try {
				operandStack.push(opFactory.createOperand(elem));
			} catch (Exception e) {
				return null;
			}
		  } else if (elem.equals("(")) {
			  operatorStack.push(elem);
		  } else if (elem.equals(")")) {
			  while (!((String) operatorStack.peek()).equals("(")) {
				  tbtn = operatorCollection.createOperator(operatorStack.pop(), operandStack);
				  if (tbtn != null) {
					  operandStack.push(tbtn);
				  }
			  }
			  operatorStack.pop();
		  } else if (operators.contains(elem)) {
			  j = operatorCollection.getPriority(elem);
			  while (!operatorStack.empty()) {
				  k = operatorCollection.getPriority(operatorStack.peek());
				  if (k < j) {
					  break;
				  }
				  tbtn = operatorCollection.createOperator(operatorStack.pop(), operandStack);
				  if (tbtn != null) {
					  operandStack.push(tbtn);
				  }
			  }
			  operatorStack.push(elem);
		  }
		  i = elem.length() + s.indexOf(elem, i);
	  }
	  
	  while (!operatorStack.empty()) {
		  tbtn = operatorCollection.createOperator(operatorStack.pop(), operandStack);
		  if (tbtn != null) {
			  operandStack.push(tbtn);
		  } else {
			  return null;
		  }
	  }
	  FunctionNode root = operandStack.pop();
	  if (!operandStack.isEmpty()) {
		  return null;
	  }
	  return root;
	}
  
	private String readElement(List<String> operators, List<String> operands, String s, int i) {
  		String s2 = s.substring(i).trim(), ret = "";

	    for (String tmp: operands) {
	    	if (s2.startsWith(tmp) && tmp.length() > ret.length()) {
	    		ret = tmp;
	    	}
	    }
	    if (ret.equals("")) {
	    	for (String tmp: operators ) {
	    		if (s2.startsWith(tmp)) {
	    			ret = tmp;
	    			break;
	    		}
	    	}
	    }
	    if (ret.equals("")) {
	    	return null;
	    }
	    return ret;
  	}
}
