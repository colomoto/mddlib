package org.colomoto.mddlib;

/**
 * Define the possible effects of a variable on a MDD.
 * 
 * @author Aurelien Naldi
 */
public enum VariableEffect {

	NONE, POSITIVE, NEGATIVE, DUAL;
	
	public VariableEffect combine(VariableEffect other) {
		if (this == NONE) {
			return other;
		}
		
		if (this == other || other == NONE) {
			return this;
		}
		
		return DUAL;
	}
}
