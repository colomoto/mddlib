package org.colomoto.mddlib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * List of variables with an associated maximal value.
 * This can be used to help <code>MDDManagerFactory</code> creating multi-valued variables.
 * It is a simple list of variables, with a map to remember some maximal level and convenience method to add them.
 * Variables added using the standard <code>List.add()</code> method, will be considered as Boolean.
 * 
 * @author aurelien
 */
public class MDDVariableFactory extends ArrayList<Object> {

	private final Map<Object, Byte> values = new HashMap<Object, Byte>();
	

	/**
	 * Add a multi-valued variable to the list.
	 * The default <code>add()</code> method will add a Boolean variable.
	 * 
	 * @param key 		object used as key for the variable
	 * @param nbvalues	number of possible values, must be at least 2
	 * 
	 * @return true if it was added successfully.
	 */
	public boolean add(Object key, byte nbvalues) {
		if (nbvalues < 2) {
			return false;
		}
		
		boolean ret = super.add(key);
		
		if (ret && nbvalues > 2) {
			values.put(key, nbvalues);
		}
		
		return ret;
	}

	/**
	 * Retrieve the number of values for a given variable.
	 * If no number was explicitly provided, it defaults to 2.
	 * 
	 * @param key
	 * @return the number of values (2 for Boolean variables)
	 */
	public byte getNbValue(Object key) {
		byte ret = 2;
		
		Byte v = values.get(key);
		if (v != null) {
			ret = v.byteValue();
		}
		return ret;
	}
}
