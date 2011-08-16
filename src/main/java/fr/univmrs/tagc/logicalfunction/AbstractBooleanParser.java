package fr.univmrs.tagc.logicalfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import fr.univmrs.tagc.javaMDD.MDDFactory;
import fr.univmrs.tagc.javaMDD.MDDVariable;
/**
 * Generic parser for logical functions.
 * <p>
 * This parser splits functions into operators and operands and
 * constructs a tree view of the function.
 * <p>
 * Implementors must provide objects to store operands and operand validation methods. 
 * 
 * @author Fabrice Lopez: initial implementation in GINsim
 * @author Aurelien Naldi: adaptation to connect to the MDDFactory
 */
public abstract class AbstractBooleanParser {
	
	final OperatorCollection operatorCollection;
	final String operatorsAndParenthesis;
	final List<String> operators;
	
	// connection with a MDDFactory
	private MDDFactory factory = null;
	private boolean factoryNeedsReset = false;


	/**
	 * Create a parser using the default set of operators.
	 */
	public AbstractBooleanParser() {
		this(OperatorCollection.DEFAULT_OPERATORS);
	}
	
	/**
	 * Create a parser with a custom set of operators.
	 * 
	 * @param operatorCollection
	 */
	public AbstractBooleanParser(OperatorCollection operatorCollection) {
		this.operatorCollection = operatorCollection;
		operatorsAndParenthesis = operatorCollection.getRegex();
		operators = operatorCollection.getOperators();
	}
	
	/**
	 * get (or refresh) the MDDFactory associated to this parser.
	 * If it was already created calling this method may refresh it:
	 * add newly added variables, apply name changes...
	 * 
	 * @return a MDD factory with the same variables as this parser.
	 */
	public MDDFactory getMDDFactory() {
		MDDVariable[] t_variables = getMDDVariables();
		if (factory == null) {
			factory = new MDDFactory(t_variables, new int[] {0,1});
		} else if (factoryNeedsReset){
			factory.setVariables(t_variables);
		}
		factoryNeedsReset = false;
		return factory;
	}

	/**
	 * Mark the associated MDDFactory as dirty: next call to <code>getMDDFactory()</code>
	 * will refresh it.
	 * <p>
	 * This method should be used by parsers that can add new variables dynamically.
	 */
	public void resetMDDFactory() {
		factoryNeedsReset = true;
	}

	/**
	 * Get a mapping between the operands used in this parser and corresponding MDDVariables.
	 * 
	 * @return MDDVariables that can be used to created a related MDDFactory
	 */
	abstract public MDDVariable[] getMDDVariables();

	/**
	 * Check that a list of names matches valid operands for this parser.
	 * 
	 * @param list
	 * 
	 * @return true if all these operands are valid
	 */
	public abstract boolean verifOperandList(List<String> list);

	/**
	 * Create an operand object for a given name.
	 * 
	 * @param name
	 * 
	 * @return an operand (BooleanNode) corresponding to the provided string 
	 */
	public abstract BooleanNode createOperand(String name);

	/**
	 * Compile a logical function.
	 * 
	 * @param s the text form of the function
	 * @return the root of the tree representation of this function.
	 */
	public BooleanNode compile(String s) {
	  int i, j, k;
	  String elem;
	  String[] split = s.split(operatorsAndParenthesis);
	  List<String> operands = new ArrayList<String>();
	  BooleanNode tbtn;

	  for (i = 0; i < split.length; i++) {
		  if (!split[i].equals("")) {
			  operands.add(split[i]);
		  }
	  }
	  if (!verifOperandList(operands)) {
		  // the implementor is responsible to present auto-corrections... 
    	return null;
	  }

	  i = 0;
	  Stack<BooleanNode> operandStack = new Stack<BooleanNode>();
	  Stack<String> operatorStack = new Stack<String>();
	  while (i != s.length()) {
		  elem = readElement(operators, operands, s, i);
		  if (elem == null) {
			  return null;
		  } else if (operands.contains(elem)) {
			  try {
				operandStack.push(createOperand(elem));
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
	  BooleanNode root = operandStack.pop();
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
